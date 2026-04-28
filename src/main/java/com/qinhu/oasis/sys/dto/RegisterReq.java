package com.qinhu.oasis.sys.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求参数
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class RegisterReq {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度须在 4-20 个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度须在 6-50 个字符之间")
    private String password;

    /** 昵称（可选，不填默认与用户名相同） */
    private String nickname;

    /** 手机号（可选，用于找回密码） */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 邮箱（可选） */
    @Email(message = "邮箱格式不正确")
    private String email;
}
