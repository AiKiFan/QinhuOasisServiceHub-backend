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
