package com.qinhu.oasis.common.service;

import com.qinhu.oasis.common.dto.WeatherVO;

/**
 * 天气服务接口
 * <p>代理和风天气 API，提供实时天气 + 未来3天预报，并通过 Redis 缓存减少外部调用</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public interface WeatherService {

    /**
     * 获取景区当前天气（含3日预报）
     * <p>
     * 优先从 Redis 缓存读取（TTL 30分钟），缓存未命中时调用和风天气 API；
     * 外部 API 调用失败时降级返回明月山典型天气 Mock 数据，确保前端始终有内容展示。
     * </p>
     *
     * @param lon 经度（明月山景区默认 114.37）
     * @param lat 纬度（明月山景区默认 27.62）
     * @return 天气视图对象（实时 + 3日预报）
     */
    WeatherVO getWeather(double lon, double lat);
}
