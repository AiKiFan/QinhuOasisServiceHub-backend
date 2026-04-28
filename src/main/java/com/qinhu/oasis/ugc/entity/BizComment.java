package com.qinhu.oasis.ugc.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价/评论实体，对应 biz_comment 表（多态设计，通过 target_type 区分被评对象）
 * <p>target_type：参见 {@link com.qinhu.oasis.common.constant.CommentTargetType}</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class BizComment {

    private Long id;
    private Long userId;
    private Long targetId;
    /** 目标类型：1-餐厅 2-攻略 3-译员订单 4-车位订单 */
    private Integer targetType;
    private String content;
    /** 评分（1-5星，仅餐厅/服务评价有效） */
    private Integer rating;
    /** 图片列表（JSON 数组字符串，最多3张） */
    private String images;
    private Integer likeCount;
    /** 父评论 ID（NULL 代表一级评论） */
    private Long parentId;
    private Long replyToUserId;
    /** 关联订单 ID（服务评价时使用） */
    private Long orderId;
    /** 状态：0-已屏蔽 1-正常 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
