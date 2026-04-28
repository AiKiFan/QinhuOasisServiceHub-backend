package com.qinhu.oasis.sys.controller;

import com.qinhu.oasis.common.exception.BizException;
import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.result.Result;
import com.qinhu.oasis.common.result.ResultCode;
import com.qinhu.oasis.common.security.LoginUser;
import com.qinhu.oasis.sys.dto.UserInfoVO;
import com.qinhu.oasis.sys.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息 REST 接口控制器（需携带有效 JWT Token）
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final SysUserService sysUserService;
    private final I18nUtil i18nUtil;

    /**
     * 获取当前登录用户信息
     *
     * @return 当前用户信息视图对象
     */
    @GetMapping("/me")
    public Result<UserInfoVO> me() {
        Long userId = LoginUser.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, i18nUtil.msg(ResultCode.UNAUTHORIZED));
        }
        return Result.ok(sysUserService.getUserInfo(userId));
    }
}
