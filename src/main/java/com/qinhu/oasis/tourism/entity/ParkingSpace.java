package com.qinhu.oasis.tourism.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 停车区域实体类，对应数据库表 parking_space
 * <p>available_count 为 MySQL 持久化镜像，真实可用库存以 Redis key: parking:stock:{id} 为准</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class ParkingSpace {

    private Long id;
    /** 区域名称（中文，如：A区停车场） */
    private String zoneName;
    /** 区域名称（英文，i18n） */
    private String zoneNameEn;
    /** 区域编码（如：ZONE_A） */
    private String zoneCode;
    /** 类型：0-普通 1-残障专用 2-新能源充电 */
    private Integer spaceType;
    /** 总车位数量 */
    private Integer totalCapacity;
    /** 位置描述 */
    private String locationDesc;
    /** 停车费率（元/小时） */
    private BigDecimal hourlyRate;
    /** 状态：0-关闭 1-开放 2-维护中（参见 SpaceStatus） */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
