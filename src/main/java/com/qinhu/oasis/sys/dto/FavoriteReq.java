package com.qinhu.oasis.sys.dto;

import lombok.Data;

/**
 * 收藏操作请求DTO
 *
 * @author AiKiFan
 * @date 2026-05-06
 */
@Data
public class FavoriteReq {

    /** 收藏对象类型：restaurant/interpreter/scenic */
    private String targetType;
    /** 收藏对象ID */
    private Long targetId;
    /** 收藏夹ID（可选） */
    private Long folderId;
}