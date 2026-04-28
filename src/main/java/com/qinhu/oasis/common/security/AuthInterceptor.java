package com.qinhu.oasis.common.security;

import com.qinhu.oasis.common.exception.BizException;
import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.result.ResultCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 鉴权拦截器，解析 Authorization Header 中的 Bearer Token
 * <ul>
 *   <li>Token 有效：写入 {@link LoginUser}，允许继续请求</li>
 *   <li>Token 过期：抛出 {@link BizException}（TOKEN_EXPIRED）</li>
 *   <li>Token 无效：抛出 {@link BizException}（TOKEN_INVALID）</li>
 *   <li>Token 缺失：允许匿名访问，具体权限由业务层控制</li>
 * </ul>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final I18nUtil i18nUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 无 Token，允许匿名访问
            return true;
        }
        String token = authHeader.substring(7).trim();
        try {
            Claims claims = jwtUtil.parseToken(token);
            Long userId = Long.valueOf(claims.getSubject());
            Integer role = claims.get("role", Integer.class);
            LoginUser.set(userId, role);
        } catch (ExpiredJwtException e) {
            throw new BizException(ResultCode.TOKEN_EXPIRED, i18nUtil.msg(ResultCode.TOKEN_EXPIRED));
        } catch (Exception e) {
            throw new BizException(ResultCode.TOKEN_INVALID, i18nUtil.msg(ResultCode.TOKEN_INVALID));
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 请求结束清除 ThreadLocal，防止内存泄漏
        LoginUser.clear();
    }
}
