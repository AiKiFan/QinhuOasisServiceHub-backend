package com.qinhu.oasis.common.i18n;

import com.qinhu.oasis.common.result.ResultCode;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 国际化消息解析工具，结合 {@link LocaleContextHolder} 自动匹配当前请求语言环境
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Component
public class I18nUtil {

    private final MessageSource messageSource;

    public I18nUtil(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * 根据消息键解析国际化文本
     *
     * @param key  messages.properties 中的消息键
     * @param args 消息占位符参数（可选）
     * @return 当前语言环境下的消息文本，找不到时返回 key 本身
     */
    public String msg(String key, Object... args) {
        Locale locale = LocaleContextHolder.get();
        return messageSource.getMessage(key, args, key, locale);
    }

    /**
     * 根据 {@link ResultCode} 解析对应的国际化错误消息
     *
     * @param rc   错误码枚举
     * @param args 消息占位符参数（可选）
     * @return 当前语言环境下的错误消息
     */
    public String msg(ResultCode rc, Object... args) {
        return msg(rc.getMessageKey(), args);
    }
}
