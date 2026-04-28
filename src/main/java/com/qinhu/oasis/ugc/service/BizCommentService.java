package com.qinhu.oasis.ugc.service;

import com.qinhu.oasis.common.result.PageResult;
import com.qinhu.oasis.ugc.dto.CommentVO;
import com.qinhu.oasis.ugc.dto.CreateCommentReq;

/**
 * 评价/评论业务服务接口
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public interface BizCommentService {

    /**
     * 发表评论/评价（需登录）
     * <p>target_type=2（攻略）：同时触发 ugc_post.comment_count +1</p>
     *
     * @param req    请求参数
     * @param userId 当前登录用户 ID
     * @return 新建评论的 VO
     */
    CommentVO createComment(CreateCommentReq req, Long userId);

    /**
     * 分页查询指定目标的一级评论列表
     *
     * @param targetId   目标 ID
     * @param targetType 目标类型（参见 CommentTargetType）
     * @param page       页码（从1开始）
     * @param size       每页条数
     * @return 分页结果
     */
    PageResult<CommentVO> listComments(Long targetId, Integer targetType, int page, int size);
}
