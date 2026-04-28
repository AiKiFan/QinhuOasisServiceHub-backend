package com.qinhu.oasis.sys.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体，对应 sys_user 表
 * <p>角色: 0=游客 1=学生译员 2=管理员（参见 {@link com.qinhu.oasis.common.constant.UserRole}）</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class SysUser {

    private Long id;
    private String username;
    /** BCrypt 加密密码 */
    private String password;
    private String phone;
    private String email;
    private String nickname;
    /** 头像 URL（存储于 Minio user-avatars bucket） */
    private String avatar;
    /** 角色：0-游客 1-学生译员 2-管理员 */
    private Integer role;
    /** 账号状态：0-禁用 1-正常 */
    private Integer status;
    /** 语言偏好：zh_CN / en_US */
    private String locale;
    private String lastLoginIp;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    /** 软删除标志：0-正常 1-已删除 */
    private Integer deleted;
}
