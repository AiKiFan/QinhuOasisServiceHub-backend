package com.qinhu.oasis.tourism.service;

import com.qinhu.oasis.common.result.PageResult;
import com.qinhu.oasis.tourism.dto.ScenicSpotDetailVO;
import com.qinhu.oasis.tourism.dto.ScenicSpotListVO;
import com.qinhu.oasis.tourism.entity.ScenicSpot;

import java.util.List;

/**
 * 景点服务接口
 *
 * @author AiKiFan
 * @date 2026-05-06
 */
public interface ScenicSpotService {

    /**
     * 获取景点列表（分页）
     */
    List<ScenicSpotListVO> getScenicSpotList(int page, int size);

    /**
     * 获取景点详情
     */
    ScenicSpotDetailVO getScenicSpotDetail(Long id);

    /**
     * 根据ID列表获取景点列表
     */
    List<ScenicSpotListVO> getScenicSpotsByIds(List<Long> ids);

    // ───────────── 管理员接口 ─────────────

    /**
     * 管理员分页查询景点列表（支持关键词搜索）
     */
    PageResult<ScenicSpot> adminList(String keyword, int page, int size);

    /**
     * 管理员新增景点
     */
    ScenicSpot adminCreate(ScenicSpot scenicSpot);

    /**
     * 管理员更新景点
     */
    ScenicSpot adminUpdate(ScenicSpot scenicSpot);

    /**
     * 管理员删除景点（软删除）
     */
    void adminDelete(Long id);

    /**
     * 管理员切换景点状态
     */
    void adminUpdateStatus(Long id, Integer status);
}