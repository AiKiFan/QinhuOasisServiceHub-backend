package com.qinhu.oasis.ugc.controller;

import com.qinhu.oasis.common.exception.BizException;
import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.result.PageResult;
import com.qinhu.oasis.common.result.Result;
import com.qinhu.oasis.common.result.ResultCode;
import com.qinhu.oasis.common.security.LoginUser;
import com.qinhu.oasis.ugc.dto.CommentVO;
import com.qinhu.oasis.ugc.dto.CreateCommentReq;
import com.qinhu.oasis.ugc.service.BizCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评价/评论 REST 接口控制器
 * <p>查询评论无需登录；发表评论需携带有效 JWT Token</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final BizCommentService bizCommentService;
    private final I18nUtil i18nUtil;

    /**
     * 分页查询指定目标的评论列表（无需登录）
     *
     * @param targetId   目标 ID
     * @param targetType 目标类型：1=餐厅 2=攻略 3=译员订单 4=车位订单
     * @param page       页码，默认1
     * @param size       每页条数，默认20
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<CommentVO>> listComments(
            @RequestParam Long targetId,
            @RequestParam Integer targetType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(bizCommentService.listComments(targetId, targetType, page, size));
    }

    /**
     * 发表评论/评价（需登录）
     *
     * @param req 评论请求参数
     * @return 新建评论 VO
     */
    @PostMapping
    public Result<CommentVO> createComment(@Valid @RequestBody CreateCommentReq req) {
        Long userId = LoginUser.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, i18nUtil.msg(ResultCode.UNAUTHORIZED));
        }
        return Result.ok(bizCommentService.createComment(req, userId));
    }
}
