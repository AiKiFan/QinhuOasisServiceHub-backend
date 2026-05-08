package com.qinhu.oasis.feedback.service;

import com.qinhu.oasis.common.result.PageResult;
import com.qinhu.oasis.feedback.dto.CreateFeedbackReq;
import com.qinhu.oasis.feedback.dto.FeedbackVO;
import com.qinhu.oasis.feedback.dto.ReplyFeedbackReq;
import com.qinhu.oasis.feedback.dto.UpdateFeedbackReq;
import com.qinhu.oasis.feedback.dto.AppendReplyReq;

/**
 * 投诉建议业务服务接口
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public interface FeedbackService {

    /**
     * 提交投诉建议（支持匿名，userId 为 null 时视为匿名）
     *
     * @param req    提交参数
     * @param userId 提交者用户 ID（匿名时为 null）
     * @return 新建记录 VO
     */
    FeedbackVO createFeedback(CreateFeedbackReq req, Long userId);

    /**
     * 管理员分页查询投诉建议列表（可按状态和类型筛选）
     *
     * @param status       状态筛选（null=全部）
     * @param feedbackType 类型筛选（null=全部）
     * @param page         页码（从 1 开始）
     * @param size         每页条数
     * @return 分页结果
     */
    PageResult<FeedbackVO> adminListFeedback(Integer status, Integer feedbackType, int page, int size);

    /**
     * 管理员回复并处理投诉建议
     *
     * @param feedbackId 记录 ID
     * @param req        回复参数
     * @param adminId    操作管理员 ID
     */
    void adminReply(Long feedbackId, ReplyFeedbackReq req, Long adminId);

    /**
     * 当前用户分页查询本人投诉建议
     */
    PageResult<FeedbackVO> getMyFeedbackList(Long userId, int page, int size);

    /**
     * 当前用户查询本人投诉建议详情
     */
    FeedbackVO getMyFeedbackDetail(Long feedbackId, Long userId);

    /**
     * 当前用户更新本人投诉建议（仅 PENDING）
     */
    void updateFeedback(Long feedbackId, UpdateFeedbackReq req, Long userId);

    /**
     * 当前用户追加回复（仅 PROCESSING）
     */
    void appendUserReply(Long feedbackId, AppendReplyReq req, Long userId);

    /**
     * 当前用户关闭本人投诉建议（PENDING 或 PROCESSING）
     */
    void closeFeedback(Long feedbackId, Long userId);

    /**
     * 当前用户标记本人投诉建议为已解决（仅 PROCESSING）
     */
    void resolveFeedback(Long feedbackId, Long userId);
}
