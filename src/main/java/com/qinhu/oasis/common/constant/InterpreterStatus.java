package com.qinhu.oasis.common.constant;

/**
 * 译员档案审核状态常量，对应 interpreter_profile.status 字段
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public final class InterpreterStatus {

    private InterpreterStatus() {}

    /** 待审核 */
    public static final int PENDING = 0;
    /** 已通过 */
    public static final int APPROVED = 1;
    /** 已拒绝 */
    public static final int REJECTED = 2;
    /** 暂停接单 */
    public static final int SUSPENDED = 3;
}
