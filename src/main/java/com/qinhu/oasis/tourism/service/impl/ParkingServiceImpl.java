package com.qinhu.oasis.tourism.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.qinhu.oasis.common.constant.OrderStatus;
import com.qinhu.oasis.common.constant.OrderType;
import com.qinhu.oasis.common.constant.SpaceStatus;
import com.qinhu.oasis.common.exception.BizException;
import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.i18n.LocaleContextHolder;
import com.qinhu.oasis.common.result.ResultCode;
import com.qinhu.oasis.tourism.dto.ParkingOrderReq;
import com.qinhu.oasis.tourism.dto.ParkingOrderVO;
import com.qinhu.oasis.tourism.dto.ParkingSpaceVO;
import com.qinhu.oasis.tourism.entity.BizOrder;
import com.qinhu.oasis.tourism.entity.ParkingSpace;
import com.qinhu.oasis.tourism.mapper.BizOrderMapper;
import com.qinhu.oasis.tourism.mapper.ParkingSpaceMapper;
import com.qinhu.oasis.tourism.service.ParkingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 车位预约业务服务实现，通过 Redis Lua 脚本实现原子扣减防超卖
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingServiceImpl implements ParkingService {

    private static final Snowflake SNOWFLAKE = IdUtil.createSnowflake(1, 1);

    /** Redis 车位库存 Key 前缀 */
    private static final String STOCK_KEY_PREFIX = "parking:stock:";

    /**
     * Lua 原子扣减脚本
     * <p>返回值：-1=key不存在, 0=库存不足, 1=扣减成功</p>
     */
    private static final RedisScript<Long> DEDUCT_STOCK_SCRIPT = RedisScript.of(
            "local stock = redis.call('GET', KEYS[1])\n" +
            "if stock == false then return -1 end\n" +
            "if tonumber(stock) <= 0 then return 0 end\n" +
            "redis.call('DECR', KEYS[1])\n" +
            "return 1",
            Long.class
    );

    private final ParkingSpaceMapper parkingSpaceMapper;
    private final BizOrderMapper bizOrderMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final I18nUtil i18nUtil;

    @Override
    public List<ParkingSpaceVO> listSpaces() {
        List<ParkingSpace> spaces = parkingSpaceMapper.selectAll();
        return spaces.stream().map(s -> {
            ParkingSpaceVO vo = new ParkingSpaceVO();
            vo.setId(s.getId());
            vo.setDisplayName(resolveDisplayName(s.getZoneName(), s.getZoneNameEn()));
            vo.setZoneCode(s.getZoneCode());
            vo.setSpaceType(s.getSpaceType());
            vo.setTotalCapacity(s.getTotalCapacity());
            // 从 Redis 获取实时库存，Redis 未命中时降级到 MySQL 镜像值
            String stock = stringRedisTemplate.opsForValue().get(STOCK_KEY_PREFIX + s.getId());
            vo.setAvailableCount(stock != null ? Integer.parseInt(stock) : s.getAvailableCount());
            vo.setLocationDesc(s.getLocationDesc());
            vo.setHourlyRate(s.getHourlyRate());
            vo.setStatus(s.getStatus());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ParkingOrderVO bookParking(ParkingOrderReq req, Long userId) {
        ParkingSpace space = parkingSpaceMapper.selectById(req.getParkingSpaceId());
        if (space == null || space.getStatus() != SpaceStatus.OPEN) {
            throw new BizException(ResultCode.NOT_FOUND, i18nUtil.msg(ResultCode.NOT_FOUND));
        }

        String redisKey = STOCK_KEY_PREFIX + req.getParkingSpaceId();
        Long result = stringRedisTemplate.execute(
                DEDUCT_STOCK_SCRIPT, Collections.singletonList(redisKey));

        if (result == null || result == -1L) {
            throw new BizException(ResultCode.NOT_FOUND, i18nUtil.msg(ResultCode.NOT_FOUND));
        }
        if (result == 0L) {
            throw new BizException(ResultCode.PARKING_STOCK_EMPTY,
                    i18nUtil.msg(ResultCode.PARKING_STOCK_EMPTY));
        }

        long hours = ChronoUnit.HOURS.between(req.getStartTime(), req.getEndTime());
        if (hours <= 0) {
            hours = 1;
        }
        BigDecimal totalAmount = space.getHourlyRate().multiply(BigDecimal.valueOf(hours));

        BizOrder order = new BizOrder();
        order.setOrderNo(SNOWFLAKE.nextIdStr());
        order.setOrderType(OrderType.PARKING);
        order.setUserId(userId);
        order.setParkingSpaceId(req.getParkingSpaceId());
        order.setVehicleNo(req.getVehicleNo());
        order.setStartTime(req.getStartTime());
        order.setEndTime(req.getEndTime());
        order.setTotalAmount(totalAmount);
        order.setPaidAmount(BigDecimal.ZERO);
        order.setStatus(OrderStatus.PENDING);
        order.setRemark(req.getRemark());

        try {
            bizOrderMapper.insert(order);
            int rows = parkingSpaceMapper.decrementAvailable(req.getParkingSpaceId());
            if (rows == 0) {
                // 理论上不会走到此处（Redis 已保障库存），防御性处理
                throw new RuntimeException("MySQL 库存扣减失败，可能存在并发冲突");
            }
        } catch (Exception e) {
            // DB 失败 → 补偿 Redis，恢复刚才扣减的库存
            stringRedisTemplate.opsForValue().increment(redisKey);
            throw e;
        }

        ParkingOrderVO vo = new ParkingOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setDisplayZoneName(resolveDisplayName(space.getZoneName(), space.getZoneNameEn()));
        vo.setVehicleNo(order.getVehicleNo());
        vo.setStartTime(order.getStartTime());
        vo.setEndTime(order.getEndTime());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setCreateTime(order.getCreateTime());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long userId) {
        BizOrder order = bizOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ResultCode.ORDER_NOT_EXIST, i18nUtil.msg(ResultCode.ORDER_NOT_EXIST));
        }
        if (!order.getUserId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN, i18nUtil.msg(ResultCode.FORBIDDEN));
        }
        if (order.getOrderType() != OrderType.PARKING) {
            throw new BizException(ResultCode.ORDER_STATUS_INVALID,
                    i18nUtil.msg(ResultCode.ORDER_STATUS_INVALID));
        }
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.ACCEPTED) {
            throw new BizException(ResultCode.ORDER_STATUS_INVALID,
                    i18nUtil.msg(ResultCode.ORDER_STATUS_INVALID));
        }

        bizOrderMapper.updateStatus(orderId, OrderStatus.CANCELLED);
        parkingSpaceMapper.incrementAvailable(order.getParkingSpaceId());
        // 同步恢复 Redis 库存
        stringRedisTemplate.opsForValue().increment(STOCK_KEY_PREFIX + order.getParkingSpaceId());
    }

    @Override
    public void initStockToRedis() {
        List<ParkingSpace> spaces = parkingSpaceMapper.selectAll();
        spaces.forEach(s -> stringRedisTemplate.opsForValue()
                .set(STOCK_KEY_PREFIX + s.getId(), String.valueOf(s.getAvailableCount())));
        log.info("Initialized parking stock for {} zones", spaces.size());
    }

    // ───────────── 私有辅助方法 ─────────────

    private boolean isEnglish() {
        return Locale.ENGLISH.getLanguage().equals(LocaleContextHolder.get().getLanguage());
    }

    private String resolveDisplayName(String name, String nameEn) {
        return (isEnglish() && nameEn != null && !nameEn.isBlank()) ? nameEn : name;
    }
}
