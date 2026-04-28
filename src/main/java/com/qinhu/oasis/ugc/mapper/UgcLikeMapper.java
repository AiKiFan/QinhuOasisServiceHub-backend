package com.qinhu.oasis.ugc.mapper;

import com.qinhu.oasis.ugc.entity.UgcLike;
import org.apache.ibatis.annotations.Param;

/**
 * 点赞记录 MyBatis Mapper 接口（XML 模式，对应 ugc_like 表）
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public interface UgcLikeMapper {

    /**
     * 查询点赞记录（用于判断是否已点赞）
     *
     * @param userId     用户 ID
     * @param targetId   目标 ID
     * @param targetType 目标类型
     * @return 点赞记录，不存在时返回 null
     */
    UgcLike selectByUserAndTarget(@Param("userId") Long userId,
                                  @Param("targetId") Long targetId,
                                  @Param("targetType") int targetType);

    /**
     * 插入点赞记录
     *
     * @param like 点赞实体
     * @return 影响行数
     */
    int insert(UgcLike like);

    /**
     * 删除点赞记录（取消点赞）
     *
     * @param userId     用户 ID
     * @param targetId   目标 ID
     * @param targetType 目标类型
     * @return 影响行数
     */
    int delete(@Param("userId") Long userId,
               @Param("targetId") Long targetId,
               @Param("targetType") int targetType);
}
