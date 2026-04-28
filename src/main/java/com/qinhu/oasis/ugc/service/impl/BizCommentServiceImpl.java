package com.qinhu.oasis.ugc.service.impl;

import cn.hutool.json.JSONUtil;
import com.qinhu.oasis.common.constant.CommentTargetType;
import com.qinhu.oasis.common.exception.BizException;
import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.result.PageResult;
import com.qinhu.oasis.common.result.ResultCode;
import com.qinhu.oasis.ugc.dto.CommentVO;
import com.qinhu.oasis.ugc.dto.CreateCommentReq;
import com.qinhu.oasis.ugc.entity.BizComment;
import com.qinhu.oasis.ugc.mapper.BizCommentMapper;
import com.qinhu.oasis.ugc.mapper.UgcPostMapper;
import com.qinhu.oasis.ugc.service.BizCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 评价/评论业务服务实现
 * <p>当 target_type=POST 时，自动触发 ugc_post.comment_count +1</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Service
@RequiredArgsConstructor
public class BizCommentServiceImpl implements BizCommentService {

    /** 评论默认正常状态 */
    private static final int COMMENT_STATUS_NORMAL = 1;

    private final BizCommentMapper bizCommentMapper;
    private final UgcPostMapper ugcPostMapper;
    private final I18nUtil i18nUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVO createComment(CreateCommentReq req, Long userId) {
        BizComment comment = new BizComment();
        comment.setUserId(userId);
        comment.setTargetId(req.getTargetId());
        comment.setTargetType(req.getTargetType());
        comment.setContent(req.getContent());
        comment.setRating(req.getRating());
        comment.setImages(req.getImages() != null ? JSONUtil.toJsonStr(req.getImages()) : null);
        comment.setParentId(req.getParentId());
        comment.setOrderId(req.getOrderId());
        comment.setStatus(COMMENT_STATUS_NORMAL);

        bizCommentMapper.insert(comment);

        // 评论攻略时同步更新 comment_count
        if (CommentTargetType.POST == req.getTargetType()) {
            ugcPostMapper.incrementCommentCount(req.getTargetId());
        }

        // 重新查询以获取 author 信息（JOIN sys_user）
        List<CommentVO> results = bizCommentMapper.selectByTarget(
                req.getTargetId(), req.getTargetType(), 0, 1);
        if (!results.isEmpty()) {
            return results.get(0);
        }

        // 兜底：手动构建 VO（理论上不会走到此处）
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setUserId(userId);
        vo.setContent(comment.getContent());
        vo.setRating(comment.getRating());
        return vo;
    }

    @Override
    public PageResult<CommentVO> listComments(Long targetId, Integer targetType, int page, int size) {
        if (targetType == null) {
            throw new BizException(ResultCode.PARAM_ERROR, i18nUtil.msg(ResultCode.PARAM_ERROR));
        }
        int offset = (page - 1) * size;
        long total = bizCommentMapper.countByTarget(targetId, targetType);
        List<CommentVO> list = bizCommentMapper.selectByTarget(targetId, targetType, offset, size);
        return PageResult.of(total, list);
    }
}
