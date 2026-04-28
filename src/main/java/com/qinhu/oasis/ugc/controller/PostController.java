package com.qinhu.oasis.ugc.controller;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 攻略/动态 REST 接口控制器
 * <p>列表和详情无需登录；发布和点赞需携带有效 JWT Token</p>
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
     * 分页查询攻略/动态列表（无需登录）
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
     * 查询攻略/动态详情（无需登录，自动 +1 浏览量）
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
        Long userId = LoginUser.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, i18nUtil.msg(ResultCode.UNAUTHORIZED));
        }
        return Result.ok(ugcPostService.createPost(req, userId));
    }

    /**
     * 点赞/取消点赞（需登录，同一接口自动切换）
     *
     * @param id 攻略 ID
     * @return liked=true 已点赞，liked=false 已取消
     */
    @PostMapping("/{id}/like")
    public Result<Map<String, Boolean>> toggleLike(@PathVariable Long id) {
        Long userId = LoginUser.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, i18nUtil.msg(ResultCode.UNAUTHORIZED));
        }
        boolean liked = ugcPostService.toggleLike(id, userId);
        return Result.ok(Map.of("liked", liked));
    }
}
