package com.qinhu.oasis.tourism.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 车位视图对象（前端渲染用）
 *
 * @author AiKiFan
 * @date 2026-05-15
 */
@Data
public class ParkingSpotVO {

    private Long id;
    private Long zoneId;
    /** 车位编号 */
    private String spotCode;
    /** 状态：0=空闲 1=已占用 2=超时 */
    private Integer status;
    /** 充电桩类型：0=普通车位 1=快充桩 2=慢充桩 */
    private Integer chargerType;
    /** 车牌号 */
    private String vehicleNo;
    /** 预约用户ID（用于前端判断是否为自己预约） */
    private Long userId;
    /** 入场时间 */
    private LocalDateTime startTime;
    /** 预计离场时间 */
    private LocalDateTime plannedEndTime;
    /** 实际离场时间 */
    private LocalDateTime actualEndTime;

    // ── 结算返回额外字段 ──
    /** 应付金额 */
    private BigDecimal totalAmount;
    /** 正常时长费 */
    private BigDecimal normalFee;
    /** 超时费 */
    private BigDecimal overtimeFee;
    /** 正常时长（小时） */
    private Double normalHours;
    /** 超时时长（小时） */
    private Double overtimeHours;
}
