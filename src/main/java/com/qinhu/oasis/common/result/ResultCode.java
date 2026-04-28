package com.qinhu.oasis.common.result;

import lombok.Getter;

/**
 * 业务响应码枚举，messageKey 对应 i18n/messages*.properties 中的消息键
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Getter
public enum ResultCode {

    // 通用
    SUCCESS(200, "common.success"),
    PARAM_ERROR(400, "common.param.error"),
    UNAUTHORIZED(401, "common.unauthorized"),
    FORBIDDEN(403, "common.forbidden"),
    NOT_FOUND(404, "common.not.found"),
    SERVER_ERROR(500, "common.server.error"),

    // 用户域
    USER_NOT_EXIST(1001, "user.not.exist"),
    USER_DISABLED(1002, "user.disabled"),
    USERNAME_DUPLICATE(1003, "user.username.duplicate"),
    PASSWORD_WRONG(1004, "user.password.wrong"),
    TOKEN_INVALID(1005, "user.token.invalid"),
    TOKEN_EXPIRED(1006, "user.token.expired"),

    // 译员域
    INTERPRETER_NOT_APPROVED(2001, "interpreter.not.approved"),
    INTERPRETER_PROFILE_EXISTS(2002, "interpreter.profile.exists"),

    // 订单域
    ORDER_NOT_EXIST(3001, "order.not.exist"),
    ORDER_STATUS_INVALID(3002, "order.status.invalid"),
    ORDER_ALREADY_COMMENTED(3003, "order.already.commented"),

    // 车位域
    PARKING_STOCK_EMPTY(4001, "parking.stock.empty"),
    PARKING_TIME_CONFLICT(4002, "parking.time.conflict"),

    // 内容域
    POST_NOT_EXIST(5001, "post.not.exist"),
    ALREADY_LIKED(5002, "like.already"),

    // 文件域
    FILE_UPLOAD_FAIL(6001, "file.upload.fail"),
    FILE_TYPE_NOT_ALLOWED(6002, "file.type.not.allowed");

    private final int code;
    /** i18n 消息 key，由 I18nUtil 解析为对应语言文本 */
    private final String messageKey;

    ResultCode(int code, String messageKey) {
        this.code = code;
        this.messageKey = messageKey;
    }
}
