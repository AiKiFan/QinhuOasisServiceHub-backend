package com.qinhu.oasis.tourism.controller;

import com.qinhu.oasis.common.constant.UserRole;
import com.qinhu.oasis.common.exception.BizException;
import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.result.PageResult;
import com.qinhu.oasis.common.result.Result;
import com.qinhu.oasis.common.result.ResultCode;
import com.qinhu.oasis.common.security.LoginUser;
import com.qinhu.oasis.tourism.dto.ScenicSpotDetailVO;
import com.qinhu.oasis.tourism.dto.ScenicSpotListVO;
import com.qinhu.oasis.tourism.entity.ScenicSpot;
import com.qinhu.oasis.tourism.service.ScenicSpotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 景点Controller
 *
 * @author AiKiFan
 * @date 2026-05-06
 */
@RestController
@RequestMapping("/scenic-spots")
@RequiredArgsConstructor
public class ScenicSpotController {

    private final ScenicSpotService scenicSpotService;
    private final I18nUtil i18nUtil;

    /**
     * 获取景点列表
     */
    @GetMapping
    public Result<List<ScenicSpotListVO>> getScenicSpotList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<ScenicSpotListVO> list = scenicSpotService.getScenicSpotList(page, size);
        return Result.ok(list);
    }

    /**
     * 获取景点详情
     */
    @GetMapping("/{id}")
    public Result<ScenicSpotDetailVO> getScenicSpotDetail(@PathVariable Long id) {
        ScenicSpotDetailVO detail = scenicSpotService.getScenicSpotDetail(id);
        return Result.ok(detail);
    }

    // ───────────── 管理员接口（需管理员角色） ─────────────

    /**
     * 管理员分页查询景点列表
     */
    @GetMapping("/admin/list")
    public Result<PageResult<ScenicSpot>> adminList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        requireAdmin();
        return Result.ok(scenicSpotService.adminList(keyword, page, size));
    }

    /**
     * 管理员新增景点
     */
    @PostMapping("/admin/create")
    public Result<ScenicSpot> adminCreate(@RequestBody ScenicSpot scenicSpot) {
        requireAdmin();
        return Result.ok(scenicSpotService.adminCreate(scenicSpot));
    }

    /**
     * 管理员更新景点
     */
    @PutMapping("/admin/update")
    public Result<ScenicSpot> adminUpdate(@RequestBody ScenicSpot scenicSpot) {
        requireAdmin();
        return Result.ok(scenicSpotService.adminUpdate(scenicSpot));
    }

    /**
     * 管理员删除景点（软删除）
     */
    @DeleteMapping("/admin/{id}")
    public Result<Void> adminDelete(@PathVariable Long id) {
        requireAdmin();
        scenicSpotService.adminDelete(id);
        return Result.ok(null);
    }

    /**
     * 管理员切换景点状态（发布/下架）
     */
    @PutMapping("/admin/{id}/status")
    public Result<Void> adminUpdateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        requireAdmin();
        scenicSpotService.adminUpdateStatus(id, body.get("status"));
        return Result.ok(null);
    }

    // ───────────── 私有辅助方法 ─────────────

    private Long requireAdmin() {
        Long userId = LoginUser.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, i18nUtil.msg(ResultCode.UNAUTHORIZED));
        }
        Integer role = LoginUser.getRole();
        if (role == null || role != UserRole.ADMIN) {
            throw new BizException(ResultCode.FORBIDDEN, i18nUtil.msg(ResultCode.FORBIDDEN));
        }
        return userId;
    }
}