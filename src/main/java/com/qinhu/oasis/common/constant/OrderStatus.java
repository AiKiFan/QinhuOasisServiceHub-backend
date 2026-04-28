package com.qinhu.oasis.common.constant;

/**
 * 订单状态常量
 * <p>状态机流转：PENDING → ACCEPTED → IN_PROGRESS → COMPLETED</p>
 * <p>异常流转：任意状态 → CANCELLED；COMPLETED → REFUNDING → REFUNDED</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public final class OrderStatus {

    private OrderStatus() {}

    /** 待接单 / 待支付 */
    public static final int PENDING = 0;
    /** 已接单 / 已支付 */
    public static final int ACCEPTED = 1;
    /** 进行中 / 使用中 */
    public static final int IN_PROGRESS = 2;
    /** 已完成 */
    public static final int COMPLETED = 3;
    /** 已取消 */
    public static final int CANCELLED = 4;
    /** 退款中 */
    public static final int REFUNDING = 5;
    /** 已退款 */
    public static final int REFUNDED = 6;
}
