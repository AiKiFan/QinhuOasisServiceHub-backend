package com.qinhu.oasis.sys.controller;

import com.qinhu.oasis.common.result.Result;
import com.qinhu.oasis.common.security.LoginUser;
import com.qinhu.oasis.sys.dto.FavoriteReq;
import com.qinhu.oasis.sys.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    public Result<Void> addFavorite(@RequestBody FavoriteReq req) {
        Long userId = LoginUser.getUserId();
        if (userId == null) {
            return Result.fail(401, "not logged in");
        }
        favoriteService.addFavorite(userId, req);
        return Result.ok();
    }

    @DeleteMapping("/{targetType}/{targetId}")
    public Result<Void> removeFavorite(@PathVariable String targetType, @PathVariable Long targetId) {
        Long userId = LoginUser.getUserId();
        if (userId == null) {
            return Result.fail(401, "not logged in");
        }
        favoriteService.removeFavorite(userId, targetType, targetId);
        return Result.ok();
    }

    @GetMapping("/check/{targetType}/{targetId}")
    public Result<Boolean> checkFavorite(@PathVariable String targetType, @PathVariable Long targetId) {
        Long userId = LoginUser.getUserId();
        if (userId == null) {
            return Result.ok(false);
        }
        boolean isFavorited = favoriteService.isFavorited(userId, targetType, targetId);
        return Result.ok(isFavorited);
    }

    @GetMapping("/{targetType}")
    public Result<Map<String, Object>> getFavoritesByType(@PathVariable String targetType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = LoginUser.getUserId();
        if (userId == null) {
            return Result.fail(401, "not logged in");
        }
        Map<String, Object> result = favoriteService.getUserFavorites(userId, targetType, page, size);
        return Result.ok(result);
    }

    @GetMapping
    public Result<Map<String, Object>> getAllFavorites() {
        Long userId = LoginUser.getUserId();
        if (userId == null) {
            return Result.fail(401, "not logged in");
        }
        Map<String, Object> result = favoriteService.getAllFavorites(userId);
        return Result.ok(result);
    }
}
