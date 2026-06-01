package com.qinhu.oasis.tourism.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.qinhu.oasis.common.constant.OrderStatus;
import com.qinhu.oasis.common.constant.OrderType;
import com.qinhu.oasis.common.exception.BizException;
import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.result.ResultCode;
import com.qinhu.oasis.tourism.dto.BookSpotReq;
import com.qinhu.oasis.tourism.dto.ParkingSpotVO;
import com.qinhu.oasis.tourism.dto.ZoneVO;
import com.qinhu.oasis.tourism.entity.BizOrder;
import com.qinhu.oasis.tourism.entity.ParkingSpace;
import com.qinhu.oasis.tourism.mapper.BizOrderMapper;
import com.qinhu.oasis.tourism.mapper.ParkingSpaceMapper;
import com.qinhu.oasis.tourism.service.ParkingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 车位预约业务服务实现（按车位预约新版）
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingServiceImpl implements ParkingService {

    private static final Snowflake SNOWFLAKE = IdUtil.createSnowflake(1, 1);

    /** 分布式锁 Key 前缀 */
    private static final String LOCK_KEY_PREFIX = "parking:lock:";
    /** 锁持有超时时间（秒） */
    private static final long LOCK_TIMEOUT_SECONDS = 5L;

    private final ParkingSpaceMapper parkingSpaceMapper;
    private final BizOrderMapper bizOrderMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final I18nUtil i18nUtil;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.qinhu.oasis.tourism.mapper.ParkingSpotMapper parkingSpotMapper;

    @Override
    public List<ZoneVO> listZones() {
        return parkingSpaceMapper.selectAll().stream().map(s -> {
            ZoneVO vo = new ZoneVO();
            vo.setId(s.getId());
            vo.setZoneName(s.getZoneName());
            vo.setZoneNameEn(s.getZoneNameEn());
            vo.setZoneCode(s.getZoneCode());
            vo.setHourlyRate(s.getHourlyRate());
            vo.setLocationDesc(s.getLocationDesc());
            vo.setStatus(s.getStatus());
            return vo;
        }).collect(Collectors.toList());
    }

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

        // 分布式锁（针对具体车位）
        String lockKey = LOCK_KEY_PREFIX + "spot:" + req.getSpotId();
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(acquired)) throw new BizException(ResultCode.PARKING_STOCK_EMPTY, "车位繁忙，请重试");

        try {
            LocalDateTime now = LocalDateTime.now();

            // 生成订单（入场时 totalAmount=0，离场时计算实际费用）
            BizOrder order = new BizOrder();
            order.setOrderNo(SNOWFLAKE.nextIdStr());
            order.setOrderType(OrderType.PARKING);
            order.setUserId(userId);
            order.setParkingSpaceId(spot.getZoneId());
            order.setVehicleNo(req.getVehicleNo());
            order.setStartTime(now);
            order.setEndTime(now);
            order.setTotalAmount(BigDecimal.ZERO);
            order.setPaidAmount(BigDecimal.ZERO);
            order.setStatus(OrderStatus.PENDING);
            bizOrderMapper.insert(order);

            // 更新车位状态为已占用
            parkingSpotMapper.updateSpot(req.getSpotId(), 1,
                    req.getVehicleNo(), userId, order.getId(), now, null);

            return toSpotVO(parkingSpotMapper.selectById(req.getSpotId()));
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

        // 权限验证：只能结算自己预约的车位
        if (spot.getUserId() == null || !spot.getUserId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN, "只能结算自己预约的车位");
        }

        // 计算实际时长（向上取整，不足1小时按1小时算）
        LocalDateTime actualEnd = LocalDateTime.now();
        LocalDateTime start = spot.getStartTime() != null ? spot.getStartTime() : actualEnd;

        long minutes = ChronoUnit.MINUTES.between(start, actualEnd);
        int hours = (int) Math.ceil(minutes / 60.0);
        if (hours < 1) hours = 1;

        // 计算费用
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
}
