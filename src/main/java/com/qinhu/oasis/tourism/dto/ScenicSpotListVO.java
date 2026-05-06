package com.qinhu.oasis.tourism.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 景点列表视图对象，用于列表页展示
 *
 * @author AiKiFan
 * @date 2026-05-06
 */
@Data
public class ScenicSpotListVO {

    private Long id;
    /** 根据请求语言自动选择 name 或 name_en */
    private String displayName;
    private String coverImg;
    private BigDecimal rating;
    private Integer reviewCount;
    private String openingHours;
    private BigDecimal ticketPrice;
    private String tags;
}