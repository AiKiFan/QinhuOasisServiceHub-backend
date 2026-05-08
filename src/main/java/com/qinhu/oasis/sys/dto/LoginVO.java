package com.qinhu.oasis.sys.dto;

import lombok.Data;

/**
 * 登录成功响应视图对象，包含 JWT Token 及当前用户基本信息
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class LoginVO {

    /** JWT Token，前端存入本地存储后附带在 Authorization: Bearer {token} 中 */
    private String token;
    private Long userId;
    private String username;
    private String nickname;
    /** 用户角色（参见 {@link com.qinhu.oasis.common.constant.UserRole}） */
    private Integer role;
    private String avatar;
    /** 译员档案 ID（仅当 role=1 且已通过审核时有值，用于前端判断译员身份） */
    private Long profileId;
    /** Token 有效期（秒） */
    private Long expiresIn;
}
