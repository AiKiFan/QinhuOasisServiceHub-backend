package com.qinhu.oasis.restaurant.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 餐厅热度排行视图对象，包含排名序号及热度分值
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class RankListVO {

    /** 排名（从 1 开始） */
    private Integer rank;
    private Long id;
    /** 根据请求语言自动选择 name 或 name_en */
    private String displayName;
    private String category;
    private String coverImg;
    private BigDecimal rating;
    private Integer reviewCount;
    /** 热度综合分（Redis ZSet score） */
    private Double sortScore;
}
