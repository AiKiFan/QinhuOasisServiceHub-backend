package com.qinhu.oasis.sys.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录请求参数
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class LoginReq {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
