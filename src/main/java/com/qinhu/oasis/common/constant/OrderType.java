package com.qinhu.oasis.common.constant;

/**
 * 订单类型常量，对应 biz_order.order_type 字段
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public final class OrderType {

    private OrderType() {}

    /** 翻译服务订单 */
    public static final int INTERPRETER = 1;
    /** 车位预约订单 */
    public static final int PARKING = 2;
}
