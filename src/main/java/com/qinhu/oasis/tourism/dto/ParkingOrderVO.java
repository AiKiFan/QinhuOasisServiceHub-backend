package com.qinhu.oasis.tourism.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 车位预约订单视图对象
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class ParkingOrderVO {

    private Long id;
    /** 业务订单号（雪花算法生成） */
    private String orderNo;
    /** 停车区域名称（i18n） */
    private String displayZoneName;
    private String vehicleNo;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    /** 应付金额（元） */
    private BigDecimal totalAmount;
    /** 订单状态（参见 OrderStatus） */
    private Integer status;
    private LocalDateTime createTime;
}
