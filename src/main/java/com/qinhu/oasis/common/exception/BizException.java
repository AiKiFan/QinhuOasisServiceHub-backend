package com.qinhu.oasis.common.exception;

import com.qinhu.oasis.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务运行时异常，携带 ResultCode 与 i18n 解析后的错误消息
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Getter
public class BizException extends RuntimeException {

    /** 业务错误码 */
    private final ResultCode resultCode;
    /** i18n 解析后的错误消息（已根据当前请求语言翻译） */
    private final String i18nMessage;

    /**
     * 构造业务异常
     *
     * @param resultCode  业务错误码枚举
     * @param i18nMessage 已解析的 i18n 错误消息
     */
    public BizException(ResultCode resultCode, String i18nMessage) {
        super(i18nMessage);
        this.resultCode = resultCode;
        this.i18nMessage = i18nMessage;
    }
}
