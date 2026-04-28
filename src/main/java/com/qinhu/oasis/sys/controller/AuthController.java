package com.qinhu.oasis.sys.controller;

import com.qinhu.oasis.common.result.Result;
import com.qinhu.oasis.sys.dto.LoginReq;
import com.qinhu.oasis.sys.dto.LoginVO;
import com.qinhu.oasis.sys.dto.RegisterReq;
import com.qinhu.oasis.sys.service.SysUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证 REST 接口控制器（注册 / 登录，无需携带 Token）
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;

    /**
     * 用户注册
     *
     * @param req 注册参数（用户名、密码等）
     * @return 空数据
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterReq req) {
        sysUserService.register(req);
        return Result.ok();
    }

    /**
     * 用户登录
     *
     * @param req     登录参数（用户名、密码）
     * @param request HTTP 请求（用于提取客户端 IP）
     * @return 包含 JWT Token 的登录视图对象
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginReq req, HttpServletRequest request) {
        String clientIp = resolveClientIp(request);
        return Result.ok(sysUserService.login(req, clientIp));
    }

    // ───────────── 私有辅助方法 ─────────────

    /**
     * 提取客户端真实 IP，兼容反向代理（X-Forwarded-For）
     */
    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            // X-Forwarded-For 可能包含多个 IP，取第一个
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
