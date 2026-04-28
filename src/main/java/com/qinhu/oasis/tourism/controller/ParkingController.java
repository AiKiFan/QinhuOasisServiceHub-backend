package com.qinhu.oasis.tourism.controller;

import com.qinhu.oasis.common.exception.BizException;
import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.result.Result;
import com.qinhu.oasis.common.result.ResultCode;
import com.qinhu.oasis.common.security.LoginUser;
import com.qinhu.oasis.tourism.dto.ParkingOrderReq;
import com.qinhu.oasis.tourism.dto.ParkingOrderVO;
import com.qinhu.oasis.tourism.dto.ParkingSpaceVO;
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
     * 查看所有停车区域及实时库存（无需登录）
     *
     * @return 停车区域列表
     */
    @GetMapping("/spaces")
    public Result<List<ParkingSpaceVO>> listSpaces() {
        return Result.ok(parkingService.listSpaces());
    }

    /**
     * 预约车位（需登录）
     *
     * @param req 预约请求参数
     * @return 预约订单 VO
     */
    @PostMapping("/orders")
    public Result<ParkingOrderVO> bookParking(@Valid @RequestBody ParkingOrderReq req) {
        Long userId = LoginUser.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, i18nUtil.msg(ResultCode.UNAUTHORIZED));
        }
        return Result.ok(parkingService.bookParking(req, userId));
    }

    /**
     * 取消车位预约（需登录，且只能取消自己的订单）
     *
     * @param orderId 订单 ID
     * @return 空数据
     */
    @PostMapping("/orders/{orderId}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long orderId) {
        Long userId = LoginUser.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, i18nUtil.msg(ResultCode.UNAUTHORIZED));
        }
        parkingService.cancelOrder(orderId, userId);
        return Result.ok();
    }
}
