package com.qinhu.oasis.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应结果封装，所有接口返回值均通过此类包装
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int code;
    private final String message;
    private final T data;

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功（无返回数据）
     *
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> Result<T> ok() {
        return new Result<>(ResultCode.SUCCESS.getCode(), null, null);
    }

    /**
     * 成功（含返回数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功结果
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), null, data);
    }

    /**
     * 成功（含数据与消息）
     *
     * @param data    响应数据
     * @param message 响应消息
     * @param <T>     数据类型
     * @return 成功结果
     */
    public static <T> Result<T> ok(T data, String message) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败（枚举错误码）
     *
     * @param rc      错误码枚举
     * @param message i18n 解析后的错误消息
     * @param <T>     数据类型
     * @return 失败结果
     */
    public static <T> Result<T> fail(ResultCode rc, String message) {
        return new Result<>(rc.getCode(), message, null);
    }

    /**
     * 失败（自定义整型错误码）
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 失败结果
     */
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 判断本次请求是否成功
     *
     * @return true 表示成功
     */
    public boolean isSuccess() {
        return this.code == ResultCode.SUCCESS.getCode();
    }
}
