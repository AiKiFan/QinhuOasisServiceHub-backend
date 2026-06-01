package com.qinhu.oasis.tourism.controller;

import com.qinhu.oasis.common.exception.BizException;
import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.result.Result;
import com.qinhu.oasis.common.result.ResultCode;
import com.qinhu.oasis.common.security.LoginUser;
import com.qinhu.oasis.tourism.dto.BookSpotReq;
import com.qinhu.oasis.tourism.dto.ParkingSpotVO;
import com.qinhu.oasis.tourism.dto.ZoneVO;
import com.qinhu.oasis.tourism.service.ParkingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 车位预约 REST 接口控制器
 * <p>查看车位无需登录；预约和取消需携带有效 JWT Token</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@RestController
@RequestMapping("/parking")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;
    private final I18nUtil i18nUtil;

    /**
     * 查询所有停车区域（给 detail 页渲染区域 Tab）
     */
    @GetMapping("/spaces")
    public Result<List<ZoneVO>> listZones() {
        return Result.ok(parkingService.listZones());
    }

    /**
     * 查询某区域所有车位状态（给前端渲染可视化布局）
     */
    @GetMapping("/zones/{zoneId}/spots")
    public Result<List<ParkingSpotVO>> getZoneSpots(@PathVariable Long zoneId) {
        return Result.ok(parkingService.getZoneSpots(zoneId));
    }

    /**
     * 预约选位（用户点击某个空闲车位，需登录）
     */
    @PostMapping("/spots/{spotId}/book")
    public Result<ParkingSpotVO> bookSpot(@PathVariable Long spotId, @RequestBody BookSpotReq req) {
        Long userId = LoginUser.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, i18nUtil.msg(ResultCode.UNAUTHORIZED));
        }
        req.setSpotId(spotId);
        return Result.ok(parkingService.bookSpot(req, userId));
    }

    /**
     * 自助结算离场（点击已占用车位，需登录）
     */
    @PostMapping("/spots/{spotId}/settle")
    public Result<ParkingSpotVO> settleSpot(@PathVariable Long spotId) {
        Long userId = LoginUser.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, i18nUtil.msg(ResultCode.UNAUTHORIZED));
        }
        return Result.ok(parkingService.settleSpot(spotId, userId));
    }
}
