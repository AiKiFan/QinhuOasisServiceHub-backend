package com.qinhu.oasis.sys.service;

import com.qinhu.oasis.sys.dto.FavoriteReq;

import java.util.List;
import java.util.Map;

/**
 * 用户收藏服务接口
 *
 * @author AiKiFan
 * @date 2026-05-06
 */
public interface FavoriteService {

    /**
     * 添加收藏
     */
    void addFavorite(Long userId, FavoriteReq req);

    /**
     * 删除收藏
     */
    void removeFavorite(Long userId, String targetType, Long targetId);

    /**
     * 查询是否已收藏
     */
    boolean isFavorited(Long userId, String targetType, Long targetId);

    /**
     * 获取用户收藏列表（按类型）
     */
    Map<String, Object> getUserFavorites(Long userId, String targetType, int page, int size);

    /**
     * 获取用户所有收藏（分类型返回）
     */
    Map<String, Object> getAllFavorites(Long userId);
}