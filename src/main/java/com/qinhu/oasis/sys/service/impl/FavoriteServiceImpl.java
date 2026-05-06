package com.qinhu.oasis.sys.service.impl;

import com.qinhu.oasis.interpreter.dto.InterpreterVO;
import com.qinhu.oasis.interpreter.service.InterpreterService;
import com.qinhu.oasis.restaurant.dto.RestaurantListVO;
import com.qinhu.oasis.restaurant.service.RestaurantService;
import com.qinhu.oasis.sys.dto.FavoriteReq;
import com.qinhu.oasis.sys.entity.UserFavorite;
import com.qinhu.oasis.sys.mapper.UserFavoriteMapper;
import com.qinhu.oasis.sys.service.FavoriteService;
import com.qinhu.oasis.tourism.dto.ScenicSpotListVO;
import com.qinhu.oasis.tourism.service.ScenicSpotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 用户收藏服务实现类
 *
 * @author AiKiFan
 * @date 2026-05-06
 */
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final UserFavoriteMapper userFavoriteMapper;
    private final RestaurantService restaurantService;
    private final InterpreterService interpreterService;
    private final ScenicSpotService scenicSpotService;

    @Override
    public void addFavorite(Long userId, FavoriteReq req) {
        // 检查是否已收藏
        UserFavorite existing = userFavoriteMapper.selectByUserAndTarget(userId, req.getTargetType(), req.getTargetId());
        if (existing != null) {
            throw new RuntimeException("已经收藏过了");
        }

        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setTargetType(req.getTargetType());
        favorite.setTargetId(req.getTargetId());
        favorite.setFolderId(req.getFolderId());
        userFavoriteMapper.insert(favorite);
    }

    @Override
    public void removeFavorite(Long userId, String targetType, Long targetId) {
        userFavoriteMapper.deleteByUserAndTarget(userId, targetType, targetId);
    }

    @Override
    public boolean isFavorited(Long userId, String targetType, Long targetId) {
        return userFavoriteMapper.selectByUserAndTarget(userId, targetType, targetId) != null;
    }

    @Override
    public Map<String, Object> getUserFavorites(Long userId, String targetType, int page, int size) {
        List<Long> targetIds = userFavoriteMapper.selectTargetIdsByUserAndType(userId, targetType);

        Map<String, Object> result = new HashMap<>();
        result.put("total", targetIds.size());

        List<Object> items = new ArrayList<>();
        if (!targetIds.isEmpty()) {
            switch (targetType) {
                case "restaurant":
                    List<RestaurantListVO> restaurants = restaurantService.getRestaurantsByIds(targetIds);
                    items.addAll(restaurants);
                    break;
                case "interpreter":
                    List<InterpreterVO> interpreters = interpreterService.getInterpretersByIds(targetIds);
                    items.addAll(interpreters);
                    break;
                case "scenic":
                    List<ScenicSpotListVO> spots = scenicSpotService.getScenicSpotsByIds(targetIds);
                    items.addAll(spots);
                    break;
            }
        }
        result.put("list", items);

        return result;
    }

    @Override
    public Map<String, Object> getAllFavorites(Long userId) {
        Map<String, Object> result = new HashMap<>();

        // 餐厅收藏
        List<Long> restaurantIds = userFavoriteMapper.selectTargetIdsByUserAndType(userId, "restaurant");
        List<RestaurantListVO> restaurants = restaurantIds.isEmpty() ? List.of() : restaurantService.getRestaurantsByIds(restaurantIds);
        result.put("restaurants", restaurants);

        // 译员收藏
        List<Long> interpreterIds = userFavoriteMapper.selectTargetIdsByUserAndType(userId, "interpreter");
        List<InterpreterVO> interpreters = interpreterIds.isEmpty() ? List.of() : interpreterService.getInterpretersByIds(interpreterIds);
        result.put("interpreters", interpreters);

        // 景点收藏
        List<Long> scenicIds = userFavoriteMapper.selectTargetIdsByUserAndType(userId, "scenic");
        List<ScenicSpotListVO> spots = scenicIds.isEmpty() ? List.of() : scenicSpotService.getScenicSpotsByIds(scenicIds);
        result.put("scenicSpots", spots);

        return result;
    }
}