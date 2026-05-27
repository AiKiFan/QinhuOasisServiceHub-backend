package com.qinhu.oasis.ugc.service.impl;

import cn.hutool.json.JSONUtil;
import com.qinhu.oasis.common.constant.CommentTargetType;
import com.qinhu.oasis.common.exception.BizException;
import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.result.PageResult;
import com.qinhu.oasis.common.result.ResultCode;
import com.qinhu.oasis.interpreter.entity.InterpreterProfile;
import com.qinhu.oasis.interpreter.mapper.InterpreterProfileMapper;
import com.qinhu.oasis.restaurant.mapper.RestaurantMapper;
import com.qinhu.oasis.restaurant.service.RestaurantService;
import com.qinhu.oasis.ugc.dto.CommentVO;
import com.qinhu.oasis.ugc.dto.CreateCommentReq;
import com.qinhu.oasis.ugc.entity.BizComment;
import com.qinhu.oasis.ugc.mapper.BizCommentMapper;
import com.qinhu.oasis.ugc.mapper.UgcPostMapper;
import com.qinhu.oasis.ugc.service.BizCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 评价/评论业务服务实现
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BizCommentServiceImpl implements BizCommentService {

    /** 评论默认正常状态 */
    private static final int COMMENT_STATUS_NORMAL = 1;

    private final BizCommentMapper bizCommentMapper;
    private final UgcPostMapper ugcPostMapper;
    private final InterpreterProfileMapper interpreterProfileMapper;
    private final RestaurantMapper restaurantMapper;
    private final RestaurantService restaurantService;
    private final I18nUtil i18nUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVO createComment(CreateCommentReq req, Long userId) {
        // 防止译员自我评价
        if (CommentTargetType.INTERPRETER == req.getTargetType()) {
            InterpreterProfile profile = interpreterProfileMapper.selectById(req.getTargetId());
            if (profile != null && userId.equals(profile.getUserId())) {
                throw new BizException(ResultCode.SELF_REVIEW_NOT_ALLOWED,
                        i18nUtil.msg(ResultCode.SELF_REVIEW_NOT_ALLOWED));
            }
        }

        BizComment existing = bizCommentMapper.selectByUserAndTarget(
                userId, req.getTargetId(), req.getTargetType());

        boolean isNew = (existing == null);
        if (isNew) {
            // 首次评价：插入新记录
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
        } else {
            // 已评价过：更新原记录
            existing.setContent(req.getContent());
            existing.setRating(req.getRating());
            existing.setImages(req.getImages() != null ? JSONUtil.toJsonStr(req.getImages()) : null);
            bizCommentMapper.updateById(existing);
        }

        // 评论攻略时同步更新 comment_count（仅首次）
        if (isNew && CommentTargetType.POST == req.getTargetType()) {
            ugcPostMapper.incrementCommentCount(req.getTargetId());
        }

        // 同步更新目标评分（餐厅/译员）
        syncRating(req.getTargetId(), req.getTargetType(), isNew);
        if (req.getTargetType() == CommentTargetType.RESTAURANT) {
            restaurantService.refreshRankScore(req.getTargetId());
        }

        // 返回评论详情
        List<CommentVO> results = bizCommentMapper.selectByTarget(
                req.getTargetId(), req.getTargetType(), 0, 1);
        if (!results.isEmpty()) {
            return results.get(0);
        }

        // 兜底
        CommentVO vo = new CommentVO();
        vo.setId(existing != null ? existing.getId() : null);
        vo.setUserId(userId);
        vo.setContent(req.getContent());
        vo.setRating(req.getRating());
        return vo;
    }

    /** 最小可信评论数 */
    private static final int BAYESIAN_C = 5;
    /** 全局默认平均分 */
    private static final double BAYESIAN_M = 3.5;

    /**
     * 重新统计并更新目标评分
     *
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @param isNewReview 是否为首次评价（决定是否 +1 reviewCount）
     */
    private void syncRating(Long targetId, int targetType, boolean isNewReview) {
        Double avg = bizCommentMapper.avgRating(targetId, targetType);
        if (avg == null) return;

        BigDecimal rating = BigDecimal.valueOf(avg).setScale(2, java.math.RoundingMode.HALF_UP);

        if (targetType == CommentTargetType.RESTAURANT) {
            restaurantMapper.updateRating(targetId, rating);
            // 先查当前 review_count，再决定是否 +1
            Integer reviewCount = restaurantMapper.selectReviewCount(targetId);
            if (reviewCount == null) reviewCount = 0;
            if (isNewReview) {
                restaurantMapper.incrementReviewCount(targetId);
                reviewCount++; // Java 里同步 +1，确保 sortScore 用最新值
            }
            // 贝叶斯平均计算热度分数并更新
            double bayesian = bayesianRating(avg, reviewCount);
            double sortScore = bayesian * 20 + Math.log10(reviewCount + 1) * 10;
            restaurantMapper.updateSortScore(targetId, sortScore);
        } else if (targetType == CommentTargetType.INTERPRETER) {
            interpreterProfileMapper.updateRating(targetId, rating);
        }
    }

    /**
     * 贝叶斯平均评分：评论少时向全局平均分靠拢，评论多时使用真实评分
     * <p>公式：bayesian = (rating × n + C × M) / (n + C)</p>
     */
    private double bayesianRating(double rating, int reviewCount) {
        return (rating * reviewCount + BAYESIAN_C * BAYESIAN_M) / (reviewCount + BAYESIAN_C);
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
