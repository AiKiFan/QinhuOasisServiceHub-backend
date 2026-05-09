package com.qinhu.oasis.restaurant.controller;

import com.qinhu.oasis.common.constant.UserRole;
import com.qinhu.oasis.common.exception.BizException;
import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.result.PageResult;
import com.qinhu.oasis.common.result.Result;
import com.qinhu.oasis.common.result.ResultCode;
import com.qinhu.oasis.common.security.LoginUser;
import com.qinhu.oasis.restaurant.dto.RankListVO;
import com.qinhu.oasis.restaurant.dto.RestaurantDetailVO;
import com.qinhu.oasis.restaurant.dto.RestaurantListVO;
import com.qinhu.oasis.restaurant.entity.Restaurant;
import com.qinhu.oasis.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 餐厅 REST 接口控制器，所有接口无需登录即可访问
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final I18nUtil i18nUtil;

    /**
     * 餐厅列表（支持按分类筛选 + 分页）
     *
     * @param category 分类名称，不传则查全部
     * @param page     页码，默认 1
     * @param size     每页条数，默认 10
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<RestaurantListVO>> list(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(restaurantService.listRestaurants(category, page, size));
    }

    /**
     * 热门餐厅排行（Redis ZSet 驱动，ZSet 为空时降级查 DB）
     *
     * @param top 取前 N 名，默认 10
     * @return 排行列表
     */
    @GetMapping("/rank")
    public Result<List<RankListVO>> rank(
            @RequestParam(defaultValue = "10") int top) {
        return Result.ok(restaurantService.getTopRank(top));
    }

    /**
     * 餐厅详情
     *
     * @param id 餐厅 ID
     * @return 餐厅详情 VO
     */
    @GetMapping("/{id}")
    public Result<RestaurantDetailVO> detail(@PathVariable Long id) {
        return Result.ok(restaurantService.getById(id));
    }

    // ───────────── 管理员接口（需管理员角色） ─────────────

    /**
     * 管理员分页查询餐厅列表
     */
    @GetMapping("/admin/list")
    public Result<PageResult<Restaurant>> adminList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        requireAdmin();
        return Result.ok(restaurantService.adminList(keyword, page, size));
    }

    /**
     * 管理员新增餐厅
     */
    @PostMapping("/admin/create")
    public Result<Restaurant> adminCreate(@RequestBody Restaurant restaurant) {
        requireAdmin();
        return Result.ok(restaurantService.adminCreate(restaurant));
    }

    /**
     * 管理员更新餐厅
     */
    @PutMapping("/admin/update")
    public Result<Restaurant> adminUpdate(@RequestBody Restaurant restaurant) {
        requireAdmin();
        return Result.ok(restaurantService.adminUpdate(restaurant));
    }

    /**
     * 管理员删除餐厅（软删除）
     */
    @DeleteMapping("/admin/{id}")
    public Result<Void> adminDelete(@PathVariable Long id) {
        requireAdmin();
        restaurantService.adminDelete(id);
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
