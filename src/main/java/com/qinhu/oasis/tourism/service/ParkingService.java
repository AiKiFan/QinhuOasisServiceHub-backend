package com.qinhu.oasis.tourism.service;

import com.qinhu.oasis.tourism.dto.BookSpotReq;
import com.qinhu.oasis.tourism.dto.ParkingSpotVO;
import com.qinhu.oasis.tourism.dto.ZoneVO;

import java.util.List;

/**
 * 车位预约业务服务接口
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public interface ParkingService {

    /**
     * 查询所有停车区域（给 detail 页渲染区域 Tab）
     *
     * @return 停车区域列表
     */
    List<ZoneVO> listZones();

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
}
