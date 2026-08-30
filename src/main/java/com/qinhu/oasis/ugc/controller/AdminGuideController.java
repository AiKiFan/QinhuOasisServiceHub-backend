package com.qinhu.oasis.ugc.controller;

import com.qinhu.oasis.common.constant.UserRole;
import com.qinhu.oasis.common.exception.BizException;
import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.result.PageResult;
import com.qinhu.oasis.common.result.Result;
import com.qinhu.oasis.common.result.ResultCode;
import com.qinhu.oasis.common.security.LoginUser;
import com.qinhu.oasis.ugc.dto.PostListVO;
import com.qinhu.oasis.ugc.service.UgcPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 攻略管理端控制器
 *
 * @author AiKiFan
 * @date 2026-08-30
 */
@RestController
@RequestMapping("/admin/posts")
@RequiredArgsConstructor
public class AdminGuideController {

    private final UgcPostService ugcPostService;
    private final I18nUtil i18nUtil;

    @GetMapping
    public Result<PageResult<PostListVO>> list(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        requireAdmin();
        return Result.ok(ugcPostService.adminList(type, status, page, size));
    }

    @PostMapping("/{id}/publish")
    public Result<Boolean> publish(@PathVariable Long id) {
        requireAdmin();
        return Result.ok(ugcPostService.adminPublish(id));
    }

    @PostMapping("/{id}/take-down")
    public Result<Boolean> takeDown(@PathVariable Long id) {
        requireAdmin();
        return Result.ok(ugcPostService.adminTakeDown(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        requireAdmin();
        ugcPostService.adminDelete(id);
        return Result.ok(null);
    }

    private void requireAdmin() {
        Long userId = LoginUser.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, i18nUtil.msg(ResultCode.UNAUTHORIZED));
        }
        Integer role = LoginUser.getRole();
        if (!Integer.valueOf(UserRole.ADMIN).equals(role)) {
            throw new BizException(ResultCode.FORBIDDEN, i18nUtil.msg(ResultCode.FORBIDDEN));
        }
    }
}
