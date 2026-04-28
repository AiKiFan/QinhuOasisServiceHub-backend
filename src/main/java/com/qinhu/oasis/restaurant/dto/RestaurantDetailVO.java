package com.qinhu.oasis.restaurant.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 餐厅详情视图对象，在 {@link RestaurantListVO} 基础上扩展地理位置与图片等字段
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RestaurantDetailVO extends RestaurantListVO {

    /** 地址描述 */
    private String address;
    /** 纬度（高德 GCJ-02） */
    private BigDecimal lat;
    /** 经度（高德 GCJ-02） */
    private BigDecimal lng;
    /** 联系电话 */
    private String phone;
    /** 图片列表（JSON 字符串） */
    private String images;
    /** 标签列表（JSON 字符串） */
    private String tags;
}
