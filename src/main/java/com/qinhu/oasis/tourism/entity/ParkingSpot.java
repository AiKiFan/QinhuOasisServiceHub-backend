package com.qinhu.oasis.tourism.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 车位实体，对应 parking_spot 表
 * <p>status: 0=空闲 1=已占用 2=超时</p>
 *
 * @author AiKiFan
 * @date 2026-05-15
 */
@Data
public class ParkingSpot {

    private Long id;
    /** 所属区域ID */
    private Long zoneId;
    /** 车位编号，如 A-01 */
    private String spotCode;
    /** 状态：0=空闲 1=已占用 2=超时 */
    private Integer status;
    /** 充电桩类型：0=普通车位 1=快充桩 2=慢充桩 */
    private Integer chargerType;
    /** 车牌号 */
    private String vehicleNo;
    /** 预约用户ID */
    private Long userId;
    /** 关联订单ID */
    private Long orderId;
    /** 入场时间 */
    private LocalDateTime startTime;
    /** 预计离场时间 */
    private LocalDateTime plannedEndTime;
    /** 实际离场时间 */
    private LocalDateTime actualEndTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
