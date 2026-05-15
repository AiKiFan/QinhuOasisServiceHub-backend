package com.qinhu.oasis.common.init;

import com.qinhu.oasis.common.constant.OrderStatus;
import com.qinhu.oasis.tourism.entity.BizOrder;
import com.qinhu.oasis.tourism.mapper.BizOrderMapper;
import com.qinhu.oasis.tourism.mapper.ParkingSpaceMapper;
import com.qinhu.oasis.tourism.service.ParkingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 车位预约超时自动释放定时任务
 *
 * @author AiKiFan
 * @date 2026-05-14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ParkingTimeoutTask {

    private static final String STOCK_KEY_PREFIX = "parking:stock:";

    private final BizOrderMapper bizOrderMapper;
    private final ParkingSpaceMapper parkingSpaceMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ParkingService parkingService;

    /**
     * 每 60 秒扫描一次超时未入场的停车订单（旧版按区域预约）
     */
    @Scheduled(fixedRate = 60000)
    public void releaseTimeoutOrders() {
        List<BizOrder> timeoutOrders = bizOrderMapper.selectTimeoutParkingOrders();
        if (timeoutOrders == null || timeoutOrders.isEmpty()) {
            return;
        }

        log.info("[车位超时任务] 发现 {} 条超时未入场订单，开始自动释放", timeoutOrders.size());
        for (BizOrder order : timeoutOrders) {
            try {
                releaseTimeoutOrder(order);
            } catch (Exception e) {
                log.error("[车位超时任务] 处理订单 {} 失败: {}", order.getOrderNo(), e.getMessage(), e);
            }
        }
    }

    /**
     * 每 60 秒扫描一次已占用车位，超时的改为status=2（新版按车位预约）
     */
    @Scheduled(fixedRate = 60000)
    public void detectSpotOvertime() {
        try {
            parkingService.detectOvertime();
        } catch (Exception e) {
            log.error("[车位超时检测] 失败: {}", e.getMessage());
        }
    }

    private void releaseTimeoutOrder(BizOrder order) {
        bizOrderMapper.updateCancelInfo(
                order.getId(),
                OrderStatus.CANCELLED,
                "超时未入场，系统自动取消",
                "system"
        );

        parkingSpaceMapper.incrementAvailable(order.getParkingSpaceId());

        String redisKey = STOCK_KEY_PREFIX + order.getParkingSpaceId();
        stringRedisTemplate.opsForValue().increment(redisKey);

        log.info("[车位超时任务] 已自动取消超时订单: {}，车牌: {}，区域ID: {}",
                order.getOrderNo(), order.getVehicleNo(), order.getParkingSpaceId());
    }
}
