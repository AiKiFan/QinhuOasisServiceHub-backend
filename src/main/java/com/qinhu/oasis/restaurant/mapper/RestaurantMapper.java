package com.qinhu.oasis.restaurant.mapper;

import com.qinhu.oasis.restaurant.entity.Restaurant;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 餐厅数据访问层（MyBatis Mapper），对应 mapper/restaurant/RestaurantMapper.xml
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public interface RestaurantMapper {

    /**
     * 查询所有正常营业的餐厅（用于初始化 Redis ZSet）
     *
     * @return 餐厅列表
     */
    List<Restaurant> selectAll();

    /**
     * 根据 ID 查询餐厅
     *
     * @param id 餐厅 ID
     * @return 餐厅实体，不存在时返回 null
     */
    Restaurant selectById(@Param("id") Long id);

    /**
     * 批量根据 ID 查询餐厅（用于排行榜回查）
     *
     * @param ids 餐厅 ID 列表
     * @return 餐厅列表
     */
    List<Restaurant> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 分页查询餐厅列表（支持分类筛选）
     *
     * @param category 分类名称，为空时查全部
     * @param offset   分页偏移量
     * @param size     每页条数
     * @return 餐厅列表
     */
    List<Restaurant> selectPage(@Param("category") String category,
                                @Param("offset") int offset,
                                @Param("size") int size);

    /**
     * 统计符合分类条件的餐厅总数
     *
     * @param category 分类名称，为空时统计全部
     * @return 总数
     */
    long countByCategory(@Param("category") String category);

    /**
     * 按热度分值降序查询 Top N 餐厅（Redis 缺失时的降级方案）
     *
     * @param limit 查询条数
     * @return 餐厅列表
     */
    List<Restaurant> selectTopByScore(@Param("limit") int limit);
}
