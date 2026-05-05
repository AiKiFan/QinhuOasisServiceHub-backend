package com.qinhu.oasis.sys.dto;

import lombok.Data;

/**
 * 用户信息更新请求 DTO
 *
 * @author AiKiFan
 * @date 2026-05-05
 */
@Data
public class UpdateUserReq {

    /** 昵称（可选，最长 20 字符） */
    private String nickname;

    /** 邮箱（可选，最长 50 字符） */
    private String email;

    /** 头像 URL（可选，Minio 对象路径） */
    private String avatar;
}