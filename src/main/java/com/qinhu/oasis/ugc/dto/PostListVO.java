package com.qinhu.oasis.ugc.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 攻略/动态列表视图对象
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class PostListVO {

    private Long id;
    private Long userId;
    /** 作者昵称（JOIN sys_user 获取） */
    private String authorNickname;
    /** 作者头像 URL */
    private String authorAvatar;
    /** 类型：1-官方攻略 2-游客攻略 3-游客动态 */
    private Integer postType;
    private String title;
    private String titleEn;
    /** i18n 展示标题（服务层根据 Locale 选中/英文） */
    private String displayTitle;
    private String summary;
    private String coverImg;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer status;
    /** 是否私密：0-公开 1-仅作者可见 */
    private Integer isPrivate;
    private LocalDateTime createTime;
}
