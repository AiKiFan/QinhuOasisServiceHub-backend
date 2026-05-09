package com.qinhu.oasis.restaurant.service;

import com.qinhu.oasis.common.result.PageResult;
import com.qinhu.oasis.restaurant.dto.RankListVO;
import com.qinhu.oasis.restaurant.dto.RestaurantDetailVO;
import com.qinhu.oasis.restaurant.dto.RestaurantListVO;
import com.qinhu.oasis.restaurant.entity.Restaurant;

import java.util.List;

/**
 * 餐厅业务服务接口
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public interface RestaurantService {

    /**
     * 分页查询餐厅列表，支持按分类筛选
     *
     * @param category 分类名称，为空时查全部
     * @param page     页码（从 1 开始）
     * @param size     每页条数
     * @return 分页结果
     */
    PageResult<RestaurantListVO> listRestaurants(String category, int page, int size);

    /**
     * 获取热门餐厅排行榜（由 Redis ZSet 驱动，ZSet 为空时降级查 DB）
     *
     * @param top 取前 N 名
     * @return 排行列表
     */
    List<RankListVO> getTopRank(int top);

    /**
     * 根据 ID 获取餐厅详情
     *
     * @param id 餐厅 ID
     * @return 餐厅详情 VO
     */
    RestaurantDetailVO getById(Long id);

    /**
     * 将所有餐厅的热度分值初始化到 Redis ZSet（key: restaurant:rank）
     * <p>由 {@link com.qinhu.oasis.common.init.RedisDataInitializer} 在应用启动时调用</p>
     */
    void initRankToRedis();

    /**
     * 根据ID列表获取餐厅列表（用于收藏功能）
     *
     * @param ids 餐厅ID列表
     * @return 餐厅列表
     */
    List<RestaurantListVO> getRestaurantsByIds(List<Long> ids);

    /**
     * 管理员分页查询餐厅列表
     *
     * @param keyword 关键词（名称/地址）
     * @param page    页码
     * @param size    每页条数
     * @return 分页结果
     */
    PageResult<Restaurant> adminList(String keyword, int page, int size);

    /**
     * 管理员新增餐厅
     *
     * @param restaurant 餐厅实体
     * @return 新增的餐厅
     */
    Restaurant adminCreate(Restaurant restaurant);

    /**
     * 管理员更新餐厅
     *
     * @param restaurant 餐厅实体（需含 ID）
     * @return 更新后的餐厅
     */
    Restaurant adminUpdate(Restaurant restaurant);

    /**
     * 管理员删除餐厅（软删除）
     *
     * @param id 餐厅 ID
     */
    void adminDelete(Long id);
}
