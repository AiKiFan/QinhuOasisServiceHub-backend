package com.qinhu.oasis.common.constant;

/**
 * 投诉建议处理状态常量，对应 sys_feedback.status 字段
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public final class FeedbackStatus {

    private FeedbackStatus() {}

    /** 待处理 */
    public static final int PENDING = 0;
    /** 处理中 */
    public static final int PROCESSING = 1;
    /** 已解决 */
    public static final int RESOLVED = 2;
    /** 已关闭 */
    public static final int CLOSED = 3;
}
