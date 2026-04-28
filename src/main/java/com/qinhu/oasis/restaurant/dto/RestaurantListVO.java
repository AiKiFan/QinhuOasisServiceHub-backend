package com.qinhu.oasis.restaurant.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 餐厅列表视图对象，用于列表页展示
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class RestaurantListVO {

    private Long id;
    /** 根据请求语言自动选择 name 或 name_en */
    private String displayName;
    private String category;
    private String coverImg;
    private BigDecimal avgPrice;
    private BigDecimal rating;
    private Integer reviewCount;
    private String businessHours;
}
