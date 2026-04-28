package com.qinhu.oasis.tourism.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 车位预约请求参数
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class ParkingOrderReq {

    @NotNull(message = "请选择停车区域")
    private Long parkingSpaceId;

    @NotBlank(message = "请输入车牌号")
    private String vehicleNo;

    @NotNull(message = "请选择入场时间")
    private LocalDateTime startTime;

    @NotNull(message = "请选择离场时间")
    private LocalDateTime endTime;

    /** 用户备注（可选） */
    private String remark;
}
