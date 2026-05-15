package com.qinhu.oasis.ugc.mapper;

import com.qinhu.oasis.ugc.dto.CommentVO;
import com.qinhu.oasis.ugc.entity.BizComment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评价/评论 MyBatis Mapper 接口（XML 模式，对应 biz_comment 表）
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public interface BizCommentMapper {

    /**
     * 插入新评论（useGeneratedKeys 回填 id）
     *
     * @param comment 评论实体
     * @return 影响行数
     */
    int insert(BizComment comment);

    /**
     * 根据用户ID + 目标查询已有评论（用于去重）
     *
     * @param userId     用户ID
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @return 已存在的评论，未找到时返回 null
     */
    BizComment selectByUserAndTarget(@Param("userId") Long userId,
                                    @Param("targetId") Long targetId,
                                    @Param("targetType") int targetType);

    /**
     * 更新已有评论（内容/评分/图片）
     *
     * @param comment 评论实体（必须含 id）
     * @return 影响行数
     */
    int updateById(BizComment comment);

    /**
     * 统计指定目标的有效评论平均分
     *
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @return 平均分（无评论时返回 null）
     */
    Double avgRating(@Param("targetId") Long targetId, @Param("targetType") int targetType);

    /**
     * 分页查询指定目标的一级评论（LEFT JOIN sys_user 获取作者信息）
     *
     * @param targetId   目标 ID
     * @param targetType 目标类型
     * @param offset     偏移量
     * @param size       每页条数
     * @return 评论 VO 列表
     */
    List<CommentVO> selectByTarget(@Param("targetId") Long targetId,
                                   @Param("targetType") int targetType,
                                   @Param("offset") int offset,
                                   @Param("size") int size);

    /**
     * 统计指定目标的一级评论总数
     *
     * @param targetId   目标 ID
     * @param targetType 目标类型
     * @return 总条数
     */
    long countByTarget(@Param("targetId") Long targetId, @Param("targetType") int targetType);
}
