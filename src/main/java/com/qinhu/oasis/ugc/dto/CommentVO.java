package com.qinhu.oasis.ugc.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价/评论视图对象
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class CommentVO {

    private Long id;
    private Long userId;
    /** 评论者昵称 */
    private String authorNickname;
    /** 评论者头像 URL */
    private String authorAvatar;
    private String content;
    /** 评分（1-5星，目标为餐厅/服务时有效） */
    private Integer rating;
    /**
     * 评论图片列表（JSON 数组字符串，直接输出为 JSON 数组）
     */
    @JsonRawValue
    private String images;
    private Integer likeCount;
    /** 父评论 ID（一级评论时为 null） */
    private Long parentId;
    private LocalDateTime createTime;
}
