package com.qinhu.oasis.sys.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.qinhu.oasis.common.constant.UserRole;
import com.qinhu.oasis.common.exception.BizException;
import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.result.ResultCode;
import com.qinhu.oasis.common.security.JwtUtil;
import com.qinhu.oasis.sys.dto.LoginReq;
import com.qinhu.oasis.sys.dto.LoginVO;
import com.qinhu.oasis.sys.dto.RegisterReq;
import com.qinhu.oasis.sys.dto.UserInfoVO;
import com.qinhu.oasis.sys.dto.UpdateUserReq;
import com.qinhu.oasis.sys.entity.SysUser;
import com.qinhu.oasis.sys.mapper.SysUserMapper;
import com.qinhu.oasis.sys.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户业务服务实现
 * <p>密码使用 Hutool BCrypt 加密，登录成功后生成 JWT Token 并记录最后登录信息</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    /** 账号正常状态 */
    private static final int STATUS_ENABLED = 1;

    private final SysUserMapper sysUserMapper;
    private final JwtUtil jwtUtil;
    private final I18nUtil i18nUtil;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Override
    public void register(RegisterReq req) {
        // 用户名唯一性校验
        if (sysUserMapper.selectByUsername(req.getUsername()) != null) {
            throw new BizException(ResultCode.USERNAME_DUPLICATE,
                    i18nUtil.msg(ResultCode.USERNAME_DUPLICATE));
        }

        SysUser user = new SysUser();
        user.setUsername(req.getUsername());
        user.setPassword(BCrypt.hashpw(req.getPassword(), BCrypt.gensalt()));
        user.setNickname(req.getNickname() != null && !req.getNickname().isBlank()
                ? req.getNickname() : req.getUsername());
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());
        user.setRole(UserRole.TOURIST);
        user.setStatus(STATUS_ENABLED);
        user.setLocale("zh_CN");

        sysUserMapper.insert(user);
        log.info("New user registered: id={}, username={}", user.getId(), user.getUsername());
    }

    @Override
    public LoginVO login(LoginReq req, String clientIp) {
        SysUser user = sysUserMapper.selectByUsername(req.getUsername());
        if (user == null || user.getDeleted() == 1) {
            throw new BizException(ResultCode.USER_NOT_EXIST, i18nUtil.msg(ResultCode.USER_NOT_EXIST));
        }
        if (user.getStatus() != STATUS_ENABLED) {
            throw new BizException(ResultCode.USER_DISABLED, i18nUtil.msg(ResultCode.USER_DISABLED));
        }
        if (!BCrypt.checkpw(req.getPassword(), user.getPassword())) {
            throw new BizException(ResultCode.PASSWORD_WRONG, i18nUtil.msg(ResultCode.PASSWORD_WRONG));
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        sysUserMapper.updateLastLogin(user.getId(), clientIp, LocalDateTime.now());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRole(user.getRole());
        vo.setAvatar(user.getAvatar());
        vo.setExpiresIn(jwtExpiration);
        return vo;
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new BizException(ResultCode.USER_NOT_EXIST, i18nUtil.msg(ResultCode.USER_NOT_EXIST));
        }

        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setLocale(user.getLocale());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }

    @Override
    public UserInfoVO updateUserInfo(Long userId, UpdateUserReq req) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new BizException(ResultCode.USER_NOT_EXIST, i18nUtil.msg(ResultCode.USER_NOT_EXIST));
        }

        // 昵称必填校验
        if (req.getNickname() == null || req.getNickname().isBlank()) {
            throw new BizException(ResultCode.PARAM_ERROR, "昵称不能为空");
        }

        // 更新字段（邮箱/头像为空时保留原值）
        user.setNickname(req.getNickname().trim());
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            user.setEmail(req.getEmail().trim());
        }
        if (req.getAvatar() != null && !req.getAvatar().isBlank()) {
            user.setAvatar(req.getAvatar().trim());
        }

        sysUserMapper.updateById(user);
        log.info("User profile updated: userId={}, nickname={}, email={}, hasAvatar={}",
                userId, user.getNickname(), user.getEmail(), req.getAvatar() != null);

        // 返回更新后的用户信息
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setLocale(user.getLocale());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}
