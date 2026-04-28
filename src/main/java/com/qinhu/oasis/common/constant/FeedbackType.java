package com.qinhu.oasis.common.constant;

/**
 * 投诉建议类型常量，对应 sys_feedback.feedback_type 字段
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public final class FeedbackType {

    private FeedbackType() {}

    /** 投诉 */
    public static final int COMPLAINT = 1;
    /** 建议 */
    public static final int SUGGESTION = 2;
    /** 咨询 */
    public static final int INQUIRY = 3;
    /** 其他 */
    public static final int OTHER = 4;
}
