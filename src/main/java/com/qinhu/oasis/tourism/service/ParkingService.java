package com.qinhu.oasis.tourism.service;

import com.qinhu.oasis.tourism.dto.*;

import java.util.List;

/**
 * 车位预约业务服务接口
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public interface ParkingService {

    // ── 旧版：按区域预约 ──
    List<ParkingSpaceVO> listSpaces();
    ParkingOrderVO bookParking(ParkingOrderReq req, Long userId);
    void cancelOrder(Long orderId, Long userId);
    void initStockToRedis();

    // ── 新版：按车位预约 ──

    /**
     * 查询某区域所有车位的实时状态（给前端渲染可视化布局）
     *
     * @param zoneId 区域ID
     * @return 车位VO列表
     */
    List<ParkingSpotVO> getZoneSpots(Long zoneId);

    /**
     * 预约某个具体车位（选位预约）
     *
     * @param req  预约请求（车位ID+车牌+时长）
     * @param userId 当前登录用户ID
     * @return 预约车位VO
     */
    ParkingSpotVO bookSpot(BookSpotReq req, Long userId);

    /**
     * 自助结算离场（点击已占用/超时车位）
     *
     * @param spotId 车位ID
     * @param userId  当前登录用户ID
     * @return 费用明细VO
     */
    ParkingSpotVO settleSpot(Long spotId, Long userId);

    /**
     * 超时检测定时任务：已废弃，改为实时计时制
     * @deprecated 实时计时制无需超时检测，用户可随时结算
     */
    @Deprecated
    void detectOvertime();
}
