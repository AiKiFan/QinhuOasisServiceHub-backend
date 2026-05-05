package com.qinhu.oasis.common.controller;

import com.qinhu.oasis.common.dto.WeatherVO;
import com.qinhu.oasis.common.result.Result;
import com.qinhu.oasis.common.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 天气接口控制器
 * <p>
 * 作为和风天气 API 的后端代理，解决前端小程序无法直接访问第三方域名的跨域问题，
 * 同时将 API Key 收敛在后端，避免密钥泄露。
 * 接口无需登录，结果由 Redis 缓存 30 分钟。
 * </p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@RestController
@RequestMapping("/weather")
@RequiredArgsConstructor
public class WeatherController {

    /** 明月山景区默认经度 */
    private static final double DEFAULT_LON = 114.37;

    /** 明月山景区默认纬度 */
    private static final double DEFAULT_LAT = 27.62;

    private final WeatherService weatherService;

    /**
     * 获取景区天气（实时 + 3日预报）
     * 接口：GET /api/weather/now
     *
     * @param lon 经度，默认为明月山景区经度 114.37
     * @param lat 纬度，默认为明月山景区纬度 27.62
     * @return 天气数据（{@link WeatherVO}）
     */
    @GetMapping("/now")
    public Result<WeatherVO> getWeather(
            @RequestParam(defaultValue = "114.37") double lon,
            @RequestParam(defaultValue = "27.62") double lat) {
        return Result.ok(weatherService.getWeather(lon, lat));
    }
}
