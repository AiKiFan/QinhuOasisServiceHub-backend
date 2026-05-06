package com.qinhu.oasis.sys.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户收藏实体类，对应数据库表 biz_user_favorite
 *
 * @author AiKiFan
 * @date 2026-05-06
 */
@Data
public class UserFavorite {

    private Long id;
    /** 用户ID */
    private Long userId;
    /** 收藏对象类型：restaurant/interpreter/scenic */
    private String targetType;
    /** 收藏对象ID */
    private Long targetId;
    /** 收藏夹ID（可选） */
    private Long folderId;
    private LocalDateTime createTime;
    /** 软删除标记：0-正常 1-已删除 */
    private Integer deleted;
}