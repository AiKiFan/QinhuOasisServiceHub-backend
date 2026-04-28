package com.qinhu.oasis.restaurant.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 餐厅实体类，对应数据库表 biz_restaurant
 * <p>sort_score = rating × 20 + LOG10(review_count + 1) × 10，同步至 Redis ZSet</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class Restaurant {

    private Long id;
    /** 餐厅名称（中文） */
    private String name;
    /** 餐厅名称（英文，i18n） */
    private String nameEn;
    /** 分类：中餐/西餐/小吃/快餐/甜品/其他 */
    private String category;
    /** 封面图 URL（Minio: ugc-images bucket） */
    private String coverImg;
    /** 图片列表（JSON 数组，Minio URL） */
    private String images;
    /** 地址描述 */
    private String address;
    /** 纬度（高德 GCJ-02 坐标） */
    private BigDecimal lat;
    /** 经度（高德 GCJ-02 坐标） */
    private BigDecimal lng;
    /** 人均消费（元） */
    private BigDecimal avgPrice;
    /** 联系电话 */
    private String phone;
    /** 营业时间（如：10:00-21:00） */
    private String businessHours;
    /** 标签列表（JSON 数组） */
    private String tags;
    /** 综合评分（1.00-5.00） */
    private BigDecimal rating;
    /** 评价总数 */
    private Integer reviewCount;
    /** 热度排行综合分（同步至 Redis ZSet） */
    private Double sortScore;
    /** 状态：0-暂停营业 1-正常营业 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    /** 软删除标记：0-正常 1-已删除 */
    private Integer deleted;
}
