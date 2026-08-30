package com.qinhu.oasis.ugc.controller;

import com.qinhu.oasis.common.constant.UserRole;
import com.qinhu.oasis.common.exception.BizException;
import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.result.Result;
import com.qinhu.oasis.common.result.ResultCode;
import com.qinhu.oasis.common.security.LoginUser;
import com.qinhu.oasis.ugc.service.UgcPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 攻略审核配置控制器
 *
 * @author AiKiFan
 * @date 2026-08-30
 */
@RestController
@RequestMapping("/admin/config/guide-review")
@RequiredArgsConstructor
public class AdminGuideConfigController {

    private final UgcPostService ugcPostService;
    private final I18nUtil i18nUtil;

    @GetMapping
    public Result<Map<String, Boolean>> get() {
        requireAdmin();
        return Result.ok(Map.of("enabled", ugcPostService.isReviewEnabled()));
    }

    @PostMapping
    public Result<Map<String, Boolean>> set(@RequestBody Map<String, Boolean> body) {
        requireAdmin();
        Boolean enabled = body == null ? null : body.get("enabled");
        if (enabled == null) {
            throw new BizException(ResultCode.PARAM_ERROR, i18nUtil.msg(ResultCode.PARAM_ERROR));
        }
        return Result.ok(Map.of("enabled", ugcPostService.setReviewEnabled(enabled)));
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
