package com.qinhu.oasis.sys.service;

import com.qinhu.oasis.sys.dto.*;

/**
 * 用户业务服务接口
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public interface SysUserService {

    /**
     * 用户注册
     * <p>校验用户名唯一性，BCrypt 加密密码后写入数据库</p>
     *
     * @param req 注册请求参数
     */
    void register(RegisterReq req);

    /**
     * 用户登录
     * <p>校验用户名/密码，通过后生成 JWT Token 并更新最后登录信息</p>
     *
     * @param req       登录请求参数
     * @param clientIp  客户端 IP（从 HttpServletRequest 中提取）
     * @return 包含 JWT Token 及用户基本信息的视图对象
     */
    LoginVO login(LoginReq req, String clientIp);

    /**
     * 获取当前登录用户信息
     *
     * @param userId 当前登录用户 ID
     * @return 用户信息视图对象
     */
    UserInfoVO getUserInfo(Long userId);

    /**
     * 更新当前登录用户信息
     * <p>支持更新昵称、邮箱、头像；昵称和邮箱不能为空</p>
     *
     * @param userId 当前登录用户 ID
     * @param req    更新请求参数
     * @return 更新后的用户信息视图对象
     */
    UserInfoVO updateUserInfo(Long userId, UpdateUserReq req);
}
