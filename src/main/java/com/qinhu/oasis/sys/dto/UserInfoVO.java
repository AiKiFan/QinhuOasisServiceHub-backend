package com.qinhu.oasis.sys.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 当前登录用户信息视图对象
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class UserInfoVO {

    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
    /** 用户角色（参见 {@link com.qinhu.oasis.common.constant.UserRole}） */
    private Integer role;
    /** 语言偏好：zh_CN / en_US */
    private String locale;
    private LocalDateTime createTime;
    /** 译员档案ID（仅当role=1且译员档案已通过审核时有值） */
    private Long profileId;
}
