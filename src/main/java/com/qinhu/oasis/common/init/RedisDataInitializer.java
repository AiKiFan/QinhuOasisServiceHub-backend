package com.qinhu.oasis.common.init;

import com.qinhu.oasis.restaurant.service.RestaurantService;
import com.qinhu.oasis.tourism.service.ParkingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动时 Redis 数据初始化器
 * <p>将 MySQL 中的车位库存、餐厅排行数据预热到 Redis，保证首次请求即可命中缓存</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisDataInitializer implements ApplicationRunner {

    private final RestaurantService restaurantService;
    private final ParkingService parkingService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("=== Redis 数据初始化开始 ===");
        try {
            parkingService.initStockToRedis();
            restaurantService.initRankToRedis();
            log.info("=== Redis 数据初始化完成 ===");
        } catch (Exception e) {
            log.error("Redis 数据初始化失败，请检查 Redis 连接及数据库数据", e);
        }
    }
}
