package com.qinhu.oasis.tourism.service;

import com.qinhu.oasis.tourism.dto.ParkingOrderReq;
import com.qinhu.oasis.tourism.dto.ParkingOrderVO;
import com.qinhu.oasis.tourism.dto.ParkingSpaceVO;

import java.util.List;

/**
 * 车位预约业务服务接口
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public interface ParkingService {

    /**
     * 查询所有停车区域及其实时可用库存（从 Redis 读取）
     *
     * @return 停车区域列表
     */
    List<ParkingSpaceVO> listSpaces();

    /**
     * 预约车位
     * <p>防超卖流程：Lua 原子扣减 Redis → DB 写订单 → DB CAS 扣减库存；DB 失败时补偿 Redis</p>
     *
     * @param req    预约请求参数
     * @param userId 当前登录用户 ID
     * @return 预约订单 VO
     */
    ParkingOrderVO bookParking(ParkingOrderReq req, Long userId);

    /**
     * 取消车位预约（仅允许取消自己的 PENDING/ACCEPTED 状态订单）
     * <p>取消后同步恢复 MySQL available_count 与 Redis 库存</p>
     *
     * @param orderId 订单 ID
     * @param userId  当前登录用户 ID
     */
    void cancelOrder(Long orderId, Long userId);

    /**
     * 将所有停车区域的库存初始化到 Redis（key: parking:stock:{id}）
     * <p>由 {@link com.qinhu.oasis.common.init.RedisDataInitializer} 在应用启动时调用</p>
     */
    void initStockToRedis();
}
