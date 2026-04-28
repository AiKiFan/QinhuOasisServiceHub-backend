package com.qinhu.oasis.tourism.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 停车区域视图对象，availableCount 来自 Redis 实时库存
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class ParkingSpaceVO {

    private Long id;
    /** 根据请求语言自动选择 zone_name 或 zone_name_en */
    private String displayName;
    private String zoneCode;
    /** 类型：0-普通 1-残障专用 2-新能源充电 */
    private Integer spaceType;
    private Integer totalCapacity;
    /** 实时可用数（从 Redis 读取，key: parking:stock:{id}） */
    private Integer availableCount;
    private String locationDesc;
    private BigDecimal hourlyRate;
    private Integer status;
}
