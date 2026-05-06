package com.qinhu.oasis.tourism.service;

import com.qinhu.oasis.tourism.dto.ScenicSpotDetailVO;
import com.qinhu.oasis.tourism.dto.ScenicSpotListVO;

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
}