package com.qinhu.oasis.feedback.controller;

import com.qinhu.oasis.common.constant.UserRole;
import com.qinhu.oasis.common.exception.BizException;
import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.result.PageResult;
import com.qinhu.oasis.common.result.Result;
import com.qinhu.oasis.common.result.ResultCode;
import com.qinhu.oasis.common.security.LoginUser;
import com.qinhu.oasis.feedback.dto.CreateFeedbackReq;
import com.qinhu.oasis.feedback.dto.FeedbackVO;
import com.qinhu.oasis.feedback.dto.ReplyFeedbackReq;
import com.qinhu.oasis.feedback.dto.UpdateFeedbackReq;
import com.qinhu.oasis.feedback.dto.AppendReplyReq;
import com.qinhu.oasis.feedback.mapper.SysFeedbackMapper;
import com.qinhu.oasis.feedback.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 投诉建议 REST 接口控制器
 * <p>
 * POST /feedback：支持匿名提交（无需登录）<br>
 * GET /admin/feedback, POST /admin/feedback/{id}/reply：需管理员角色
 * </p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@RestController
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final SysFeedbackMapper feedbackMapper;
    private final I18nUtil i18nUtil;

    /**
     * 提交投诉建议（无需登录，匿名提交；已登录则关联用户 ID）
     *
     * @param req 提交参数
     * @return 新建记录 VO
     */
    @PostMapping("/feedback")
    public Result<FeedbackVO> createFeedback(@Valid @RequestBody CreateFeedbackReq req) {
        Long userId = LoginUser.getUserId();
        return Result.ok(feedbackService.createFeedback(req, userId));
    }

    /**
     * 管理员分页查询投诉建议列表（需管理员角色）
     *
     * @param status       状态筛选（不传则查全部）
     * @param feedbackType 类型筛选（不传则查全部）
     * @param page         页码，默认1
     * @param size         每页条数，默认20
     * @return 分页结果
     */
    @GetMapping("/admin/feedback")
    public Result<PageResult<FeedbackVO>> adminList(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer feedbackType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        requireAdmin();
        return Result.ok(feedbackService.adminListFeedback(status, feedbackType, page, size));
    }

    /**
     * 管理员查询投诉建议详情（需管理员角色）
     *
     * @param id 记录 ID
     * @return 完整记录（含正文和图片）
     */
    @GetMapping("/admin/feedback/{id}")
    public Result<FeedbackVO> adminDetail(@PathVariable Long id) {
        requireAdmin();
        FeedbackVO vo = feedbackMapper.selectById(id);
        if (vo == null) {
            throw new BizException(ResultCode.NOT_FOUND, i18nUtil.msg(ResultCode.NOT_FOUND));
        }
        return Result.ok(vo);
    }

    /**
     * 管理员回复并处理投诉建议（需管理员角色）
     *
     * @param id  记录 ID
     * @param req 回复参数
     * @return 操作结果
     */
    @PostMapping("/admin/feedback/{id}/reply")
    public Result<Void> adminReply(@PathVariable Long id,
                                   @Valid @RequestBody ReplyFeedbackReq req) {
        Long adminId = requireAdmin();
        feedbackService.adminReply(id, req, adminId);
        return Result.ok(null);
    }

    /**
     * 当前用户分页查询本人投诉建议
     */
    @GetMapping("/feedback/me")
    public Result<PageResult<FeedbackVO>> myList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = requireLoginUser();
        return Result.ok(feedbackService.getMyFeedbackList(userId, page, size));
    }

    /**
     * 当前用户查询本人投诉建议详情
     */
    @GetMapping("/feedback/{id}")
    public Result<FeedbackVO> myDetail(@PathVariable Long id) {
        Long userId = requireLoginUser();
        return Result.ok(feedbackService.getMyFeedbackDetail(id, userId));
    }

    /**
     * 当前用户更新本人投诉建议（仅 PENDING）
     */
    @PutMapping("/feedback/{id}")
    public Result<FeedbackVO> updateMy(@PathVariable Long id,
                                       @Valid @RequestBody UpdateFeedbackReq req) {
        Long userId = requireLoginUser();
        return Result.ok(feedbackService.updateFeedback(id, req, userId));
    }

    /**
     * 当前用户追加回复（仅 PROCESSING）
     */
    @PostMapping("/feedback/{id}/reply")
    public Result<Void> appendMyReply(@PathVariable Long id,
                                      @Valid @RequestBody AppendReplyReq req) {
        Long userId = requireLoginUser();
        feedbackService.appendUserReply(id, req, userId);
        return Result.ok(null);
    }

    /**
     * 当前用户关闭本人投诉建议
     */
    @PostMapping("/feedback/{id}/close")
    public Result<Void> closeMy(@PathVariable Long id) {
        Long userId = requireLoginUser();
        feedbackService.closeFeedback(id, userId);
        return Result.ok(null);
    }

    /**
     * 当前用户标记本人投诉建议为已解决
     */
    @PostMapping("/feedback/{id}/resolve")
    public Result<Void> resolveMy(@PathVariable Long id) {
        Long userId = requireLoginUser();
        feedbackService.resolveFeedback(id, userId);
        return Result.ok(null);
    }

    // ───────────── 私有辅助方法 ─────────────

    private Long requireLoginUser() {
        Long userId = LoginUser.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, i18nUtil.msg(ResultCode.UNAUTHORIZED));
        }
        return userId;
    }

    private Long requireAdmin() {
        Long userId = LoginUser.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, i18nUtil.msg(ResultCode.UNAUTHORIZED));
        }
        Integer role = LoginUser.getRole();
        if (role == null || role != UserRole.ADMIN) {
            throw new BizException(ResultCode.FORBIDDEN, i18nUtil.msg(ResultCode.FORBIDDEN));
        }
        return userId;
    }
}
