package com.qinhu.oasis.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 天气数据视图对象
 * <p>聚合实时天气 + 未来3天预报，供前端 WeatherCard 组件直接使用</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherVO {

    // ── 实时天气 ──────────────────────────────────────────────

    /** 温度（℃） */
    private String temp;

    /** 体感温度（℃） */
    private String feelsLike;

    /** 和风天气图标代码（如 "100" = 晴） */
    private String icon;

    /** 天气描述（如 "晴"、"多云"） */
    private String text;

    /** 风向（如 "南风"） */
    private String windDir;

    /** 风力等级（如 "3"） */
    private String windScale;

    /** 相对湿度（%） */
    private String humidity;

    /** 数据观测时间（ISO 8601） */
    private String obsTime;

    // ── 三日预报 ──────────────────────────────────────────────

    /** 未来3天预报列表 */
    private List<DailyForecast> forecast;

    // ── 内部类 ────────────────────────────────────────────────

    /**
     * 单日天气预报
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyForecast {

        /** 预报日期（yyyy-MM-dd） */
        private String date;

        /** 最高温度（℃） */
        private String tempMax;

        /** 最低温度（℃） */
        private String tempMin;

        /** 白天图标代码 */
        private String icon;

        /** 白天天气描述 */
        private String text;
    }
}
