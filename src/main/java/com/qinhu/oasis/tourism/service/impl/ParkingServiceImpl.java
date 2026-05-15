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
import com.qinhu.oasis.tourism.dto.BookSpotReq;
import com.qinhu.oasis.tourism.dto.ParkingSpotVO;
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
    /** Redis 分布式锁 Key 前缀 */
    private static final String LOCK_KEY_PREFIX = "parking:lock:";
    /** 锁持有超时时间（秒） */
    private static final long LOCK_TIMEOUT_SECONDS = 5L;

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

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.qinhu.oasis.tourism.mapper.ParkingSpotMapper parkingSpotMapper;

    @Override
    public List<ParkingSpaceVO> listSpaces() {
        List<ParkingSpace> spaces = parkingSpaceMapper.selectAll();
        return spaces.stream().map(s -> {
            ParkingSpaceVO vo = new ParkingSpaceVO();
            vo.setId(s.getId());
            vo.setDisplayName(resolveDisplayName(s.getZoneName(), s.getZoneNameEn()));
            vo.setZoneCode(s.getZoneCode());
            vo.setSpaceType(s.getSpaceType());
            // 从车位表实时统计（避免配置值与实际不一致）
            if (parkingSpotMapper != null) {
                List<com.qinhu.oasis.tourism.entity.ParkingSpot> allSpots = parkingSpotMapper.selectByZoneId(s.getId());
                vo.setTotalCapacity(allSpots.size());
                vo.setAvailableCount((int) allSpots.stream().filter(sp -> sp.getStatus() == 0).count());
            } else {
                vo.setTotalCapacity(s.getTotalCapacity());
                vo.setAvailableCount(s.getAvailableCount());
            }
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

        String lockKey = LOCK_KEY_PREFIX + req.getParkingSpaceId();
        String lockValue = Thread.currentThread().getName() + ":" + System.currentTimeMillis();

        // 分布式锁：SETNX + TTL，防止极端并发下重复下单
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, LOCK_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(acquired)) {
            throw new BizException(ResultCode.PARKING_STOCK_EMPTY,
                    i18nUtil.msg(ResultCode.PARKING_STOCK_EMPTY));
        }

        try {
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

            bizOrderMapper.insert(order);
            int rows = parkingSpaceMapper.decrementAvailable(req.getParkingSpaceId());
            if (rows == 0) {
                // 理论上不会走到此处（Redis 已保障库存），防御性处理
                throw new RuntimeException("MySQL 库存扣减失败，可能存在并发冲突");
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
        } catch (Exception e) {
            // DB 失败 → 补偿 Redis，恢复刚才扣减的库存
            stringRedisTemplate.opsForValue().increment(STOCK_KEY_PREFIX + req.getParkingSpaceId());
            throw e;
        } finally {
            // 释放分布式锁
            stringRedisTemplate.delete(lockKey);
        }
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

    // ───────────── 新版：按车位预约 ─────────────

    @Override
    public List<ParkingSpotVO> getZoneSpots(Long zoneId) {
        if (parkingSpotMapper == null) return List.of();
        List<com.qinhu.oasis.tourism.entity.ParkingSpot> spots = parkingSpotMapper.selectByZoneId(zoneId);
        return spots.stream().map(this::toSpotVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ParkingSpotVO bookSpot(BookSpotReq req, Long userId) {
        if (parkingSpotMapper == null) throw new BizException(ResultCode.NOT_FOUND, "车位模块未初始化");

        com.qinhu.oasis.tourism.entity.ParkingSpot spot = parkingSpotMapper.selectById(req.getSpotId());
        if (spot == null) throw new BizException(ResultCode.NOT_FOUND, i18nUtil.msg(ResultCode.NOT_FOUND));
        if (spot.getStatus() != 0) throw new BizException(ResultCode.PARKING_STOCK_EMPTY, i18nUtil.msg(ResultCode.PARKING_STOCK_EMPTY));

        // 分布式锁
        String lockKey = LOCK_KEY_PREFIX + "spot:" + req.getSpotId();
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", LOCK_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(acquired)) throw new BizException(ResultCode.PARKING_STOCK_EMPTY, "车位繁忙，请重试");

        try {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();

            // 生成订单（入场时totalAmount=0，离场时计算实际费用）
            ParkingSpace zone = parkingSpaceMapper.selectById(spot.getZoneId());
            BizOrder order = new BizOrder();
            order.setOrderNo(SNOWFLAKE.nextIdStr());
            order.setOrderType(OrderType.PARKING);
            order.setUserId(userId);
            order.setParkingSpaceId(spot.getZoneId());
            order.setVehicleNo(req.getVehicleNo());
            order.setStartTime(now);
            order.setEndTime(now); // 初始end_time为start_time，离场时更新
            order.setTotalAmount(BigDecimal.ZERO); // 入场时不收费
            order.setPaidAmount(BigDecimal.ZERO);
            order.setStatus(OrderStatus.PENDING);
            bizOrderMapper.insert(order);

            // 更新车位状态：入场时间记录，不再需要预计离场时间
            parkingSpotMapper.updateSpot(req.getSpotId(), 1,
                    req.getVehicleNo(), userId, order.getId(), now, null);

            ParkingSpotVO vo = toSpotVO(parkingSpotMapper.selectById(req.getSpotId()));
            return vo;
        } finally {
            stringRedisTemplate.delete(lockKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ParkingSpotVO settleSpot(Long spotId, Long userId) {
        if (parkingSpotMapper == null) throw new BizException(ResultCode.NOT_FOUND, "车位模块未初始化");

        com.qinhu.oasis.tourism.entity.ParkingSpot spot = parkingSpotMapper.selectById(spotId);
        if (spot == null) throw new BizException(ResultCode.NOT_FOUND, i18nUtil.msg(ResultCode.NOT_FOUND));
        if (spot.getStatus() == 0) throw new BizException(ResultCode.PARKING_STOCK_EMPTY, "该车位当前空闲，无需结算");

        // 新增：权限验证 - 只能结算自己预约的车位
        if (spot.getUserId() == null || !spot.getUserId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN, "只能结算自己预约的车位");
        }

        // 计算实际时长（向上取整，不足1小时按1小时算）
        java.time.LocalDateTime actualEnd = java.time.LocalDateTime.now();
        java.time.LocalDateTime start = spot.getStartTime() != null ? spot.getStartTime() : actualEnd;

        long minutes = ChronoUnit.MINUTES.between(start, actualEnd);
        int hours = (int) Math.ceil(minutes / 60.0);
        if (hours < 1) hours = 1; // 最少1小时

        // 计算费用（去除超时计费逻辑）
        ParkingSpace space = parkingSpaceMapper.selectById(spot.getZoneId());
        BigDecimal hourlyRate = space.getHourlyRate();
        BigDecimal totalFee = hourlyRate.multiply(BigDecimal.valueOf(hours));

        // 更新订单状态为已完成
        if (spot.getOrderId() != null) {
            BizOrder order = bizOrderMapper.selectById(spot.getOrderId());
            if (order != null) {
                order.setEndTime(actualEnd);
                order.setPaidAmount(totalFee);
                order.setStatus(OrderStatus.COMPLETED);
                bizOrderMapper.updateStatus(order.getId(), OrderStatus.COMPLETED);
                bizOrderMapper.updatePaidAmount(order.getId(), totalFee);
            }
        }

        // 重置车位为空闲
        parkingSpotMapper.resetSpot(spotId);

        ParkingSpotVO vo = toSpotVO(parkingSpotMapper.selectById(spotId));
        vo.setTotalAmount(totalFee);
        vo.setNormalFee(totalFee);
        vo.setOvertimeFee(BigDecimal.ZERO);
        vo.setNormalHours((double) hours);
        vo.setOvertimeHours(0.0);
        return vo;
    }

    /**
     * 已废弃：改为实时计时制，不再需要超时检测
     * 保留方法签名以兼容调用方
     */
    @Override
    @Deprecated
    public void detectOvertime() {
        // 已废弃：改为实时计时制，status=2超时状态已不再使用
        // 原逻辑：定时检测超时的车位，设置status=2
        // 现在：用户可随时结算，按实际停车时长计费
        log.debug("[超时检测] detectOvertime已废弃，实时计时制无需超时检测");
    }

    // ───────────── 私有辅助方法 ─────────────

    private ParkingSpotVO toSpotVO(com.qinhu.oasis.tourism.entity.ParkingSpot spot) {
        ParkingSpotVO vo = new ParkingSpotVO();
        vo.setId(spot.getId());
        vo.setZoneId(spot.getZoneId());
        vo.setSpotCode(spot.getSpotCode());
        vo.setStatus(spot.getStatus());
        vo.setChargerType(spot.getChargerType());
        vo.setVehicleNo(spot.getVehicleNo());
        vo.setUserId(spot.getUserId());
        vo.setStartTime(spot.getStartTime());
        vo.setPlannedEndTime(spot.getPlannedEndTime());
        vo.setActualEndTime(spot.getActualEndTime());
        return vo;
    }

    private boolean isEnglish() {
        return Locale.ENGLISH.getLanguage().equals(LocaleContextHolder.get().getLanguage());
    }

    private String resolveDisplayName(String name, String nameEn) {
        return (isEnglish() && nameEn != null && !nameEn.isBlank()) ? nameEn : name;
    }
}
