package com.qinhu.oasis.common.i18n;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Locale;

/**
 * 国际化拦截器，解析请求语言并写入 {@link LocaleContextHolder}
 * <p>
 * 优先级（从高到低）：
 * <ol>
 *   <li>请求参数 {@code ?lang=zh_CN}</li>
 *   <li>请求头  {@code Accept-Language: zh-CN}</li>
 *   <li>兜底    {@code zh_CN}</li>
 * </ol>
 * 请求结束后自动清理 ThreadLocal，防止线程池复用导致的语言污染。
 * </p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public class I18nInterceptor implements HandlerInterceptor {

    private static final String LANG_PARAM = "lang";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        Locale locale = resolveLocale(request);
        LocaleContextHolder.set(locale);
        // 同步到 Spring 的 LocaleContextHolder，供 MessageSource 使用
        org.springframework.context.i18n.LocaleContextHolder.setLocale(locale);
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {
        LocaleContextHolder.clear();
        org.springframework.context.i18n.LocaleContextHolder.resetLocaleContext();
    }

    /**
     * 按优先级解析 Locale
     *
     * @param request HTTP 请求
     * @return 解析到的 Locale
     */
    private Locale resolveLocale(HttpServletRequest request) {
        // 1. 请求参数优先 (?lang=en_US)
        String langParam = request.getParameter(LANG_PARAM);
        if (StringUtils.hasText(langParam)) {
            return parseLocale(langParam);
        }
        // 2. Accept-Language Header
        String acceptLanguage = request.getHeader("Accept-Language");
        if (StringUtils.hasText(acceptLanguage)) {
            String primary = acceptLanguage.split(",")[0].trim().split(";")[0].trim();
            return parseLocale(primary);
        }
        // 3. 兜底中文
        return Locale.SIMPLIFIED_CHINESE;
    }

    /**
     * 解析语言标签字符串，兼容 "zh-CN" 和 "zh_CN" 两种格式
     *
     * @param lang 语言标签
     * @return Locale 对象
     */
    private Locale parseLocale(String lang) {
        String normalized = lang.replace('-', '_');
        String[] parts = normalized.split("_");
        return switch (parts.length) {
            case 1 -> new Locale(parts[0]);
            case 2 -> new Locale(parts[0], parts[1]);
            default -> Locale.SIMPLIFIED_CHINESE;
        };
    }
}
