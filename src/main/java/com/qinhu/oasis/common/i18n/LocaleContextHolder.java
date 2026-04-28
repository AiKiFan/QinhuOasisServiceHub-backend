package com.qinhu.oasis.common.i18n;

import java.util.Locale;

/**
 * 线程级 Locale 持有器，由 {@link I18nInterceptor} 写入，业务代码通过 {@link I18nUtil} 读取
 * <p>请求结束后须调用 {@link #clear()} 防止线程池复用导致的语言污染</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public final class LocaleContextHolder {

    private static final ThreadLocal<Locale> LOCALE_TL = new ThreadLocal<>();

    private LocaleContextHolder() {}

    /**
     * 设置当前线程的 Locale
     *
     * @param locale 目标语言环境
     */
    public static void set(Locale locale) {
        LOCALE_TL.set(locale);
    }

    /**
     * 获取当前线程的 Locale，未设置时默认返回简体中文
     *
     * @return 当前语言环境
     */
    public static Locale get() {
        Locale locale = LOCALE_TL.get();
        return locale != null ? locale : Locale.SIMPLIFIED_CHINESE;
    }

    /**
     * 清除当前线程的 Locale，防止内存泄漏
     */
    public static void clear() {
        LOCALE_TL.remove();
    }
}
