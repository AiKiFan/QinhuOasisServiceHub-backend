package com.qinhu.oasis.common.constant;

/**
 * 评论/评价目标类型常量，对应 biz_comment.target_type
 * <p>1-餐厅 2-攻略 3-译员订单 4-车位订单</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public final class CommentTargetType {

    public static final int RESTAURANT        = 1;
    public static final int POST              = 2;
    public static final int INTERPRETER_ORDER = 3;
    public static final int PARKING_ORDER     = 4;
    public static final int INTERPRETER       = 5;

    private CommentTargetType() {
    }
}
