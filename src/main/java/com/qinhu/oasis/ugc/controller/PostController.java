package com.qinhu.oasis.ugc.controller;

import com.qinhu.oasis.common.constant.UserRole;
import com.qinhu.oasis.common.exception.BizException;
import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.result.PageResult;
import com.qinhu.oasis.common.result.Result;
import com.qinhu.oasis.common.result.ResultCode;
import com.qinhu.oasis.common.security.LoginUser;
import com.qinhu.oasis.ugc.dto.CreatePostReq;
import com.qinhu.oasis.ugc.dto.PostDetailVO;
import com.qinhu.oasis.ugc.dto.PostListVO;
import com.qinhu.oasis.ugc.service.UgcPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 攻略/动态 REST 接口控制器
 * <p>列表和详情无需登录；发布、点赞和个人管理需携带有效 JWT Token</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final UgcPostService ugcPostService;
    private final I18nUtil i18nUtil;

    /**
     * 分页查询已发布公开攻略/动态列表（无需登录）
     *
     * @param type 类型筛选：1=官方攻略 2=游客攻略 3=游客动态（不传则全部）
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<PostListVO>> listPosts(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(ugcPostService.listPosts(type, page, size));
    }

    /**
     * 查询攻略/动态详情（自动 +1 浏览量）
     *
     * @param id 攻略 ID
     * @return 详情 VO
     */
    @GetMapping("/{id}")
    public Result<PostDetailVO> getPostDetail(@PathVariable Long id) {
        return Result.ok(ugcPostService.getPostDetail(id));
    }

    /**
     * 发布攻略/动态（需登录）
     *
     * @param req 发布请求参数
     * @return 新建帖子详情 VO
     */
    @PostMapping
    public Result<PostDetailVO> createPost(@Valid @RequestBody CreatePostReq req) {
        Long userId = requireLogin();
        return Result.ok(ugcPostService.createPost(req, userId));
    }

    /**
     * 查询我的攻略（包含私密和待审核/已下架状态）
     *
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @return 分页结果
     */
    @GetMapping("/mine")
    public Result<PageResult<PostListVO>> listMyPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(ugcPostService.listMyPosts(requireLogin(), page, size));
    }

    /**
     * 编辑未发布攻略内容，已发布攻略正文禁止直接修改。
     *
     * @param id  攻略 ID
     * @param req 编辑请求参数
     * @return 更新后的详情 VO
     */
    @PutMapping("/{id}")
    public Result<PostDetailVO> updatePost(@PathVariable Long id, @Valid @RequestBody CreatePostReq req) {
        return Result.ok(ugcPostService.updateByUser(id, requireLogin(), req));
    }

    /**
     * 设置攻略私密状态。
     *
     * @param id   攻略 ID
     * @param body 请求体，isPrivate=true 表示设为私密
     * @return 是否更新成功
     */
    @PostMapping("/{id}/set-private")
    public Result<Boolean> setPrivate(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        Boolean isPrivate = body == null ? null : body.get("isPrivate");
        if (isPrivate == null) {
            throw new BizException(ResultCode.PARAM_ERROR, i18nUtil.msg(ResultCode.PARAM_ERROR));
        }
        return Result.ok(ugcPostService.setPrivate(id, requireLogin(), isPrivate));
    }

    /**
     * 用户删除本人攻略。
     *
     * @param id 攻略 ID
     * @return 空结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        ugcPostService.softDeleteByUser(id, requireLogin());
        return Result.ok(null);
    }

    /**
     * 点赞/取消点赞。
     *
     * @param id 攻略 ID
     * @return liked=true 表示当前已点赞
     */
    @PostMapping("/{id}/like")
    public Result<Map<String, Boolean>> toggleLike(@PathVariable Long id) {
        return Result.ok(Map.of("liked", ugcPostService.toggleLike(id, requireLogin())));
    }

    /** 兼容旧管理端列表路径：/api/posts/admin */
    @GetMapping("/admin")
    public Result<PageResult<PostListVO>> legacyAdminList(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        requireAdmin();
        return Result.ok(ugcPostService.adminList(type, status, page, size));
    }

    /** 兼容旧管理端删除路径：/api/posts/admin/{id} */
    @DeleteMapping("/admin/{id}")
    public Result<Void> legacyAdminDelete(@PathVariable Long id) {
        requireAdmin();
        ugcPostService.adminDelete(id);
        return Result.ok(null);
    }

    private Long requireLogin() {
        Long userId = LoginUser.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, i18nUtil.msg(ResultCode.UNAUTHORIZED));
        }
        return userId;
    }

    private void requireAdmin() {
        Long userId = requireLogin();
        Integer role = LoginUser.getRole();
        if (userId == null || !Integer.valueOf(UserRole.ADMIN).equals(role)) {
            throw new BizException(ResultCode.FORBIDDEN, i18nUtil.msg(ResultCode.FORBIDDEN));
        }
    }
}
