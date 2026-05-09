package com.qinhu.oasis.common.exception;

import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.result.Result;
import com.qinhu.oasis.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常统一处理器，将各类异常转换为统一 {@link Result} 响应格式
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     * @return 失败响应
     */
    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBizException(BizException e) {
        log.warn("Business exception: code={}, msg={}", e.getResultCode().getCode(), e.getI18nMessage());
        return Result.fail(e.getResultCode(), e.getI18nMessage());
    }

    /**
     * 处理参数校验异常（@Valid 触发）
     *
     * @param e 参数校验异常
     * @return 失败响应，消息为所有字段错误的拼接
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        BindingResult br = e.getBindingResult();
        String msg = br.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        // 字段错误消息已经是 i18n 翻译后的文本（Controller 在入参中注入 Locale 或 LocaleContext），直接返回
        return Result.fail(ResultCode.PARAM_ERROR, msg);
    }

    /**
     * 处理未捕获的系统异常
     *
     * @param e 系统异常
     * @return 失败响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleException(Exception e) {
        log.error("Unexpected exception", e);
        return Result.fail(ResultCode.SERVER_ERROR, e.getMessage());
    }
}
