package com.qinhu.oasis.interpreter.controller;

import com.qinhu.oasis.common.exception.BizException;
import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.result.PageResult;
import com.qinhu.oasis.common.result.Result;
import com.qinhu.oasis.common.result.ResultCode;
import com.qinhu.oasis.common.security.LoginUser;
import com.qinhu.oasis.interpreter.dto.BookInterpreterReq;
import com.qinhu.oasis.interpreter.dto.CancelOrderReq;
import com.qinhu.oasis.interpreter.dto.InterpreterOrderVO;
import com.qinhu.oasis.interpreter.service.InterpreterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 翻译服务订单 REST 接口控制器（均需登录）
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@RestController
@RequestMapping("/interpreter-orders")
@RequiredArgsConstructor
public class InterpreterOrderController {

    private final InterpreterService interpreterService;
    private final I18nUtil i18nUtil;

    /**
     * 预约译员服务，创建翻译订单（需登录）
     *
     * @param req 预约参数
     * @return 新建订单 VO
     */
    @PostMapping
    public Result<InterpreterOrderVO> bookInterpreter(@Valid @RequestBody BookInterpreterReq req) {
        Long userId = requireLogin();
        return Result.ok(interpreterService.bookInterpreter(req, userId));
    }

    /**
     * 译员接单（需登录，且必须是该订单指定的译员）
     *
     * @param id 订单 ID
     * @return 操作结果
     */
    @PostMapping("/{id}/accept")
    public Result<Void> acceptOrder(@PathVariable Long id) {
        Long userId = requireLogin();
        interpreterService.acceptOrder(id, userId);
        return Result.ok(null);
    }

    /**
     * 取消翻译订单（需登录，游客或译员均可操作）
     *
     * @param id  订单 ID
     * @param req 取消理由（可选）
     * @return 操作结果
     */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long id, @RequestBody(required = false) CancelOrderReq req) {
        Long userId = requireLogin();
        String reason = req != null ? req.getReason() : null;
        interpreterService.cancelOrder(id, userId, reason);
        return Result.ok(null);
    }

    /**
     * 分页查询当前用户的翻译订单列表（需登录）
     *
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @return 分页结果
     */
    @GetMapping("/mine")
    public Result<PageResult<InterpreterOrderVO>> listMyOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = requireLogin();
        return Result.ok(interpreterService.listMyOrders(userId, page, size));
    }

    /**
     * 译员查看收到的订单列表（需登录，译员角色）
     *
     * @param status 状态筛选（不传则查全部）
     * @param page   页码，默认1
     * @param size   每页条数，默认10
     * @return 分页结果
     */
    @GetMapping("/received")
    public Result<PageResult<InterpreterOrderVO>> listReceivedOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = requireLogin();
        return Result.ok(interpreterService.listReceivedOrders(userId, status, page, size));
    }

    /**
     * 译员拒绝订单（需登录，且必须是该订单指定的译员）
     *
     * @param id  订单 ID
     * @param req 拒绝理由（可选）
     * @return 操作结果
     */
    @PostMapping("/{id}/reject")
    public Result<Void> rejectOrder(@PathVariable Long id, @RequestBody(required = false) CancelOrderReq req) {
        Long userId = requireLogin();
        String reason = req != null ? req.getReason() : null;
        interpreterService.rejectOrder(id, userId, reason);
        return Result.ok(null);
    }

    /**
     * 译员完成服务（需登录，且必须是该订单指定的译员）
     *
     * @param id 订单 ID
     * @return 操作结果
     */
    @PostMapping("/{id}/complete")
    public Result<Void> completeOrder(@PathVariable Long id) {
        Long userId = requireLogin();
        interpreterService.completeOrder(id, userId);
        return Result.ok(null);
    }

    /**
     * 获取订单详情（需登录，订单所有者或译员可查看）
     *
     * @param id 订单 ID
     * @return 订单 VO
     */
    @GetMapping("/{id}")
    public Result<InterpreterOrderVO> getOrderDetail(@PathVariable Long id) {
        Long userId = requireLogin();
        return Result.ok(interpreterService.getOrderDetail(id, userId));
    }

    // ───────────── 私有辅助方法 ─────────────

    private Long requireLogin() {
        Long userId = LoginUser.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, i18nUtil.msg(ResultCode.UNAUTHORIZED));
        }
        return userId;
    }
}
