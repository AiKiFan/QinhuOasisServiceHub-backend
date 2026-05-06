package com.qinhu.oasis.tourism.mapper;

import com.qinhu.oasis.tourism.entity.ScenicSpot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 景点Mapper接口
 *
 * @author AiKiFan
 * @date 2026-05-06
 */
@Mapper
public interface ScenicSpotMapper {

    /**
     * 查询所有正常开放的景点
     */
    List<ScenicSpot> selectAll();

    /**
     * 根据ID查询景点
     */
    ScenicSpot selectById(@Param("id") Long id);

    /**
     * 根据ID列表查询景点
     */
    List<ScenicSpot> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 分页查询景点列表
     */
    List<ScenicSpot> selectPage(@Param("offset") int offset, @Param("size") int size);

    /**
     * 统计景点总数
     */
    long countAll();

    /**
     * 查询热门景点（按sort_score排序）
     */
    List<ScenicSpot> selectTopByScore(@Param("limit") int limit);
}