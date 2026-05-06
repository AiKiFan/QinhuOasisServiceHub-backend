package com.qinhu.oasis.tourism.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 景点详情视图对象，在 {@link ScenicSpotListVO} 基础上扩展地理位置与图片等字段
 *
 * @author AiKiFan
 * @date 2026-05-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ScenicSpotDetailVO extends ScenicSpotListVO {

    /** 景点描述（根据语言自动选择） */
    private String displayDescription;
    /** 地址描述 */
    private String address;
    /** 纬度（高德GCJ-02） */
    private BigDecimal lat;
    /** 经度（高德GCJ-02） */
    private BigDecimal lng;
    /** 图片列表（JSON字符串） */
    private String images;
}