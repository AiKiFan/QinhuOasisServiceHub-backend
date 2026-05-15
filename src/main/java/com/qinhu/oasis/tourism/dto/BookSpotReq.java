package com.qinhu.oasis.tourism.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 预约车位请求DTO
 * 改为实时计时制：入场时无需预估时长，离场时根据实际停车时长计费
 *
 * @author AiKiFan
 * @date 2026-05-15
 */
@Data
public class BookSpotReq {

    /** 车位ID */
    private Long spotId;

    /** 车牌号 */
    @NotBlank(message = "车牌号不能为空")
    private String vehicleNo;
}
