package com.qinhu.oasis.interpreter.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 翻译服务订单展示 VO
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class InterpreterOrderVO {

    private Long id;
    private String orderNo;
    /** 下单游客用户 ID */
    private Long userId;
    private String userNickname;
    /** 游客头像 URL */
    private String userAvatar;
    /** 游客手机号 */
    private String userPhone;
    /** 游客邮箱 */
    private String userEmail;
    /** 接单译员用户 ID（biz_order.interpreter_id） */
    private Long interpreterId;
    private String interpreterNickname;
    /** 译员头像 URL */
    private String interpreterAvatar;
    /** 译员档案 ID（interpreter_profile.id） */
    private Long profileId;
    /** 服务类型：1=个人 2=团队 */
    private Integer serviceType;
    /** 团队人数 */
    private Integer groupSize;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    /** 订单状态（参见 OrderStatus） */
    private Integer status;
    private String remark;
    /** 订单联系电话（游客预约时填写，区别于 userPhone） */
    private String orderPhone;
    private String cancelReason;
    /** 取消方：user=游客 interpreter=译员 */
    private String cancelledBy;
    /** 是否已评价：0=否 1=是 */
    private Integer isCommented;
    private LocalDateTime createTime;
}
