package com.qinhu.oasis.restaurant.controller;

import com.qinhu.oasis.common.result.PageResult;
import com.qinhu.oasis.common.result.Result;
import com.qinhu.oasis.restaurant.dto.RankListVO;
import com.qinhu.oasis.restaurant.dto.RestaurantDetailVO;
import com.qinhu.oasis.restaurant.dto.RestaurantListVO;
import com.qinhu.oasis.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
