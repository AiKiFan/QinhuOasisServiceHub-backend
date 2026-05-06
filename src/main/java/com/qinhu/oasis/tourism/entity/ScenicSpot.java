package com.qinhu.oasis.tourism.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 景点实体类，对应数据库表 biz_scenic_spot
 *
 * @author AiKiFan
 * @date 2026-05-06
 */
@Data
public class ScenicSpot {

    private Long id;
    /** 景点名称（中文） */
    private String name;
    /** 景点名称（英文） */
    private String nameEn;
    /** 景点描述（中文） */
    private String description;
    /** 景点描述（英文） */
    private String descriptionEn;
    /** 封面图URL（MinIO: ugc-images bucket） */
    private String coverImg;
    /** 图片列表（JSON数组） */
    private String images;
    /** 地址描述 */
    private String address;
    /** 纬度（高德GCJ-02坐标） */
    private BigDecimal lat;
    /** 经度（高德GCJ-02坐标） */
    private BigDecimal lng;
    /** 开放时间 */
    private String openingHours;
    /** 门票价格 */
    private BigDecimal ticketPrice;
    /** 评分（1.00-5.00） */
    private BigDecimal rating;
    /** 评价总数 */
    private Integer reviewCount;
    /** 标签列表（JSON数组） */
    private String tags;
    /** 热度排行综合分 */
    private Double sortScore;
    /** 状态：0-暂停开放 1-正常开放 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    /** 软删除标记：0-正常 1-已删除 */
    private Integer deleted;
}