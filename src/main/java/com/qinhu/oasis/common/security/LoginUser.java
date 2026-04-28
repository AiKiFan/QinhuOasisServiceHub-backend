package com.qinhu.oasis.common.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 线程级登录用户上下文持有器，由 {@link AuthInterceptor} 写入，业务层读取后校验权限
 * <p>请求结束后须调用 {@link #clear()} 防止内存泄漏</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public final class LoginUser {

    private static final ThreadLocal<LoginInfo> HOLDER = new ThreadLocal<>();

    private LoginUser() {}

    /**
     * 设置当前线程的登录用户信息
     *
     * @param userId 用户 ID
     * @param role   用户角色
     */
    public static void set(Long userId, Integer role) {
        HOLDER.set(new LoginInfo(userId, role));
    }

    /**
     * 获取当前线程的登录用户信息，未登录时返回 null
     *
     * @return 登录信息，可能为 null
     */
    public static LoginInfo get() {
        return HOLDER.get();
    }

    /**
     * 获取当前登录用户 ID，未登录时返回 null
     *
     * @return 用户 ID，可能为 null
     */
    public static Long getUserId() {
        LoginInfo info = HOLDER.get();
        return info != null ? info.getUserId() : null;
    }

    /**
     * 获取当前登录用户角色，未登录时返回 null
     *
     * @return 用户角色，可能为 null
     */
    public static Integer getRole() {
        LoginInfo info = HOLDER.get();
        return info != null ? info.getRole() : null;
    }

    /**
     * 清除当前线程的登录信息，防止内存泄漏
     */
    public static void clear() {
        HOLDER.remove();
    }

    /**
     * 登录用户信息载体
     */
    @Getter
    @AllArgsConstructor
    public static class LoginInfo {
        private final Long userId;
        private final Integer role;
    }
}
