package com.qinhu.oasis.feedback.service.impl;

import cn.hutool.json.JSONUtil;
import com.qinhu.oasis.common.constant.FeedbackStatus;
import com.qinhu.oasis.common.exception.BizException;
import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.result.PageResult;
import com.qinhu.oasis.common.result.ResultCode;
import com.qinhu.oasis.feedback.dto.CreateFeedbackReq;
import com.qinhu.oasis.feedback.dto.FeedbackVO;
import com.qinhu.oasis.feedback.dto.ReplyFeedbackReq;
import com.qinhu.oasis.feedback.entity.SysFeedback;
import com.qinhu.oasis.feedback.mapper.SysFeedbackMapper;
import com.qinhu.oasis.feedback.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 投诉建议业务服务实现
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final SysFeedbackMapper feedbackMapper;
    private final I18nUtil i18nUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FeedbackVO createFeedback(CreateFeedbackReq req, Long userId) {
        SysFeedback feedback = new SysFeedback();
        feedback.setUserId(userId);
        feedback.setFeedbackType(req.getFeedbackType());
        feedback.setTitle(req.getTitle());
        feedback.setContent(req.getContent());
        feedback.setImages(req.getImages() != null && !req.getImages().isEmpty()
                ? JSONUtil.toJsonStr(req.getImages()) : null);
        feedback.setContact(req.getContact());
        feedback.setStatus(FeedbackStatus.PENDING);
        feedbackMapper.insert(feedback);
        log.info("Feedback submitted: id={}, type={}, userId={}", feedback.getId(), req.getFeedbackType(), userId);
        return feedbackMapper.selectById(feedback.getId());
    }

    @Override
    public PageResult<FeedbackVO> adminListFeedback(Integer status, Integer feedbackType, int page, int size) {
        int offset = (page - 1) * size;
        long total = feedbackMapper.countAdminPage(status, feedbackType);
        List<FeedbackVO> list = feedbackMapper.selectAdminPage(status, feedbackType, offset, size);
        return PageResult.of(total, list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminReply(Long feedbackId, ReplyFeedbackReq req, Long adminId) {
        FeedbackVO existing = feedbackMapper.selectById(feedbackId);
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND, i18nUtil.msg(ResultCode.NOT_FOUND));
        }
        if (existing.getStatus() == FeedbackStatus.RESOLVED || existing.getStatus() == FeedbackStatus.CLOSED) {
            throw new BizException(ResultCode.ORDER_STATUS_INVALID, i18nUtil.msg(ResultCode.ORDER_STATUS_INVALID));
        }
        feedbackMapper.updateReply(feedbackId, req.getReplyContent(), req.getStatus(), adminId, LocalDateTime.now());
        log.info("Feedback replied: id={}, newStatus={}, adminId={}", feedbackId, req.getStatus(), adminId);
    }
}
