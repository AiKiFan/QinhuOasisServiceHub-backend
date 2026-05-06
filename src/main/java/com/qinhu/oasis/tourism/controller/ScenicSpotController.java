package com.qinhu.oasis.tourism.controller;

import com.qinhu.oasis.common.result.Result;
import com.qinhu.oasis.tourism.dto.ScenicSpotDetailVO;
import com.qinhu.oasis.tourism.dto.ScenicSpotListVO;
import com.qinhu.oasis.tourism.service.ScenicSpotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}