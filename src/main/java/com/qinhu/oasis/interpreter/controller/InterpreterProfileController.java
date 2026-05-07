package com.qinhu.oasis.interpreter.controller;

import com.qinhu.oasis.common.constant.UserRole;
import com.qinhu.oasis.common.exception.BizException;
import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.result.PageResult;
import com.qinhu.oasis.common.result.Result;
import com.qinhu.oasis.common.result.ResultCode;
import com.qinhu.oasis.common.security.LoginUser;
import com.qinhu.oasis.common.service.FileStorageService;
import com.qinhu.oasis.interpreter.dto.ApplyInterpreterReq;
import com.qinhu.oasis.interpreter.dto.InterpreterVO;
import com.qinhu.oasis.interpreter.service.InterpreterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 译员档案 REST 接口控制器
 * <p>
 * GET /interpreters, GET /interpreters/{id}：公开（无需登录）<br>
 * POST /interpreter/cert-upload, POST /interpreter/apply：需登录<br>
 * GET/POST /admin/interpreter-profiles/**：需管理员角色
 * </p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@RestController
@RequiredArgsConstructor
public class InterpreterProfileController {

    private final InterpreterService interpreterService;
    private final FileStorageService fileStorageService;
    private final I18nUtil i18nUtil;

    @Value("${minio.buckets.interpreter-certs}")
    private String certsBucket;

    /**
     * 上传译员资质证书图片（需登录）
     *
     * @param file 证书图片文件
     * @return cert URL
     */
    @PostMapping("/interpreter/cert-upload")
    public Result<Map<String, String>> uploadCert(@RequestParam("file") MultipartFile file) {
        requireLogin();
        if (file.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR, i18nUtil.msg(ResultCode.PARAM_ERROR));
        }
        String url = fileStorageService.uploadImage(file, certsBucket);
        return Result.ok(Map.of("url", url));
    }

    /**
     * 申请成为译员（需登录）
     *
     * @param req 申请参数
     * @return 新建的译员档案 VO
     */
    @PostMapping("/interpreter/apply")
    public Result<InterpreterVO> applyInterpreter(@Valid @RequestBody ApplyInterpreterReq req) {
        Long userId = requireLogin();
        return Result.ok(interpreterService.applyInterpreter(req, userId));
    }

    /**
     * 分页查询已通过审核的译员列表（无需登录）
     *
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @return 分页结果
     */
    @GetMapping("/interpreters")
    public Result<PageResult<InterpreterVO>> listInterpreters(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(interpreterService.listInterpreters(page, size));
    }

    /**
     * 查询译员详情（无需登录）
     *
     * @param id 译员档案 ID
     * @return 译员 VO
     */
    @GetMapping("/interpreters/{id}")
    public Result<InterpreterVO> getDetail(@PathVariable Long id) {
        return Result.ok(interpreterService.getInterpreterDetail(id));
    }

    /**
     * 获取当前用户的译员申请档案（需登录）
     *
     * @return 译员档案 VO，未申请过时返回 null
     */
    @GetMapping("/interpreter/my-profile")
    public Result<InterpreterVO> getMyProfile() {
        Long userId = requireLogin();
        return Result.ok(interpreterService.getMyProfile(userId));
    }

    /**
     * 更新当前用户的译员申请（仅待审核状态可修改，需登录）
     *
     * @param req 申请参数
     * @return 更新后的译员档案 VO
     */
    @PostMapping("/interpreter/my-application")
    public Result<InterpreterVO> updateMyApplication(@Valid @RequestBody ApplyInterpreterReq req) {
        Long userId = requireLogin();
        return Result.ok(interpreterService.updateMyApplication(req, userId));
    }

    /**
     * 管理员分页查询所有译员档案（需管理员角色）
     *
     * @param status 状态筛选（不传则查全部）
     * @param page   页码，默认1
     * @param size   每页条数，默认10
     * @return 分页结果
     */
    @GetMapping("/admin/interpreter-profiles")
    public Result<PageResult<InterpreterVO>> adminList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        requireAdmin();
        return Result.ok(interpreterService.adminListProfiles(status, page, size));
    }

    /**
     * 管理员审核译员申请（需管理员角色）
     *
     * @param id           档案 ID
     * @param approve      是否通过（true/false）
     * @param rejectReason 拒绝原因（approve=false 时必填）
     * @return 操作结果
     */
    @PostMapping("/admin/interpreter-profiles/{id}/review")
    public Result<Void> reviewProfile(
            @PathVariable Long id,
            @RequestParam boolean approve,
            @RequestParam(required = false) String rejectReason) {
        Long adminId = requireAdmin();
        if (!approve && (rejectReason == null || rejectReason.isBlank())) {
            throw new BizException(ResultCode.PARAM_ERROR, i18nUtil.msg(ResultCode.PARAM_ERROR));
        }
        interpreterService.adminReviewProfile(id, approve, rejectReason, adminId);
        return Result.ok(null);
    }

    // ───────────── 私有辅助方法 ─────────────

    private Long requireLogin() {
        Long userId = LoginUser.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, i18nUtil.msg(ResultCode.UNAUTHORIZED));
        }
        return userId;
    }

    private Long requireAdmin() {
        Long userId = requireLogin();
        Integer role = LoginUser.getRole();
        if (role == null || role != UserRole.ADMIN) {
            throw new BizException(ResultCode.FORBIDDEN, i18nUtil.msg(ResultCode.FORBIDDEN));
        }
        return userId;
    }
}
