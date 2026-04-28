package com.qinhu.oasis.ugc.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 攻略/动态实体，对应 ugc_post 表
 * <p>post_type：参见 {@link com.qinhu.oasis.common.constant.PostType}</p>
 * <p>status：参见 {@link com.qinhu.oasis.common.constant.PostStatus}</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class UgcPost {

    private Long id;
    private Long userId;
    /** 类型：1-官方攻略 2-游客攻略 3-游客动态 */
    private Integer postType;
    private String title;
    private String titleEn;
    private String summary;
    /** HTML 富文本正文 */
    private String content;
    private String coverImg;
    /** 图片列表（JSON 数组字符串，最多9张，存储 Minio URL） */
    private String images;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    /** 状态：0-草稿 1-已发布 2-审核中 3-已下架 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
