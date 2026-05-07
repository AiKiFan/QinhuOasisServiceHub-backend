package com.qinhu.oasis.common.init;

import cn.hutool.crypto.digest.BCrypt;
import com.qinhu.oasis.sys.entity.SysUser;
import com.qinhu.oasis.sys.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动时管理员账号初始化器
 * <p>检查管理员账号是否存在，不存在则自动创建</p>
 *
 * @author AiKiFan
 * @date 2026-05-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements ApplicationRunner {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "Admin@123456";
    private static final String ADMIN_NICKNAME = "系统管理员";
    private static final Integer ADMIN_ROLE = 2;
    private static final Integer ADMIN_STATUS = 1;
    private static final String ADMIN_LOCALE = "zh_CN";

    private final SysUserMapper sysUserMapper;

    @Override
    public void run(ApplicationArguments args) {
        log.info("=== 管理员账号初始化开始 ===");
        try {
            SysUser existingAdmin = sysUserMapper.selectByUsername(ADMIN_USERNAME);
            if (existingAdmin == null) {
                // 账号不存在，创建新账号
                SysUser admin = buildAdminUser();
                sysUserMapper.insert(admin);
                log.info("管理员账号创建成功!");
            } else {
                // 账号存在，检查密码是否正确，不正确则重置
                if (!BCrypt.checkpw(ADMIN_PASSWORD, existingAdmin.getPassword())) {
                    log.warn("管理员密码哈希异常，正在重置为正确密码...");
                    String newHash = BCrypt.hashpw(ADMIN_PASSWORD, BCrypt.gensalt(12));
                    sysUserMapper.updatePassword(existingAdmin.getId(), newHash);
                    log.info("管理员密码已重置!");
                } else {
                    log.info("管理员账号已存在且密码正确");
                }
            }
            log.info("用户名: {}", ADMIN_USERNAME);
            log.info("密码: {}", ADMIN_PASSWORD);
            log.info("=== 管理员账号初始化完成 ===");
        } catch (Exception e) {
            log.error("管理员账号初始化失败", e);
        }
    }

    private SysUser buildAdminUser() {
        SysUser admin = new SysUser();
        admin.setUsername(ADMIN_USERNAME);
        admin.setPassword(BCrypt.hashpw(ADMIN_PASSWORD, BCrypt.gensalt(12)));
        admin.setNickname(ADMIN_NICKNAME);
        admin.setRole(ADMIN_ROLE);
        admin.setStatus(ADMIN_STATUS);
        admin.setLocale(ADMIN_LOCALE);
        return admin;
    }
}
