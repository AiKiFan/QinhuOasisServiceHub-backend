package com.qinhu.oasis.sys.controller;

import com.qinhu.oasis.common.constant.UserRole;
import com.qinhu.oasis.common.exception.BizException;
import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.result.Result;
import com.qinhu.oasis.common.result.ResultCode;
import com.qinhu.oasis.common.security.LoginUser;
import com.qinhu.oasis.sys.service.ImageCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统管理接口：图片清理
 *
 * @author AiKiFan
 * @date 2026-05-16
 */
@RestController
@RequestMapping("/sys")
@RequiredArgsConstructor
public class SysController {

    private final ImageCleanupService imageCleanupService;
    private final I18nUtil i18nUtil;

    /**
     * 清理数据库中指向不存在文件的图片路径
     * <p>需管理员权限。</p>
     *
     * @return 清理结果统计
     */
    @PostMapping("/admin/cleanup/images")
    public Result<ImageCleanupService.CleanupResult> cleanupImages() {
        requireAdmin();
        ImageCleanupService.CleanupResult result = imageCleanupService.cleanupAll();
        return Result.ok(result);
    }

    private void requireAdmin() {
        Long userId = LoginUser.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, i18nUtil.msg(ResultCode.UNAUTHORIZED));
        }
        Integer role = LoginUser.getRole();
        if (role == null || role != UserRole.ADMIN) {
            throw new BizException(ResultCode.FORBIDDEN, i18nUtil.msg(ResultCode.FORBIDDEN));
        }
    }
}
