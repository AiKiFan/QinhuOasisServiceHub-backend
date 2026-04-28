package com.qinhu.oasis.tourism.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 业务订单实体类，对应数据库表 biz_order
 * <p>多态设计：order_type 区分翻译服务订单与车位预约订单（参见 OrderType）</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class BizOrder {

    private Long id;
    /** 业务订单号（雪花算法生成，18位数字字符串） */
    private String orderNo;
    /** 订单类型（参见 OrderType）：1-翻译服务 2-车位预约 */
    private Integer orderType;
    /** 下单用户 ID */
    private Long userId;
    /** 接单译员用户 ID（翻译订单专用） */
    private Long interpreterId;
    /** 服务类型：1-个人 2-团队（翻译订单专用） */
    private Integer serviceType;
    /** 团队人数（翻译订单专用，默认 1） */
    private Integer groupSize;
    /** 停车区域 ID（车位订单专用） */
    private Long parkingSpaceId;
    /** 车牌号（车位订单专用） */
    private String vehicleNo;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    /** 订单应付金额（元） */
    private BigDecimal totalAmount;
    /** 实际支付金额（元） */
    private BigDecimal paidAmount;
    /** 订单状态（参见 OrderStatus） */
    private Integer status;
    /** 用户备注 */
    private String remark;
    /** 取消原因 */
    private String cancelReason;
    /** 是否已评价：0-否 1-是 */
    private Integer isCommented;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    /** 软删除标记：0-正常 1-已删除 */
    private Integer deleted;
}
