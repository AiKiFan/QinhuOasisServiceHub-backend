package com.qinhu.oasis.common.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qinhu.oasis.common.dto.WeatherVO;
import com.qinhu.oasis.common.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 天气服务实现
 * <p>代理和风天气 API（免费版），Redis 缓存 30 分钟；外部接口异常时降级返回 Mock 数据</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    // ── Redis 缓存 Key 前缀 & TTL ───────────────────────────
    private static final String CACHE_KEY_PREFIX = "weather:now:";
    private static final long CACHE_TTL_MINUTES = 30;

    // ── 和风天气配置（从 application.yml 读取） ───────────────
    @Value("${third-party.hefeng-weather.key}")
    private String apiKey;

    @Value("${third-party.hefeng-weather.base-url}")
    private String baseUrl;

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 获取景区天气（实时 + 3日预报）
     * 流程：Redis 命中 → 反序列化返回；未命中 → 调用和风 API → 写入 Redis → 返回；
     * API 异常 → 降级 Mock 数据
     */
    @Override
    public WeatherVO getWeather(double lon, double lat) {
        String cacheKey = CACHE_KEY_PREFIX + lon + "," + lat;

        // 1. 尝试从 Redis 缓存读取
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("[Weather] Redis cache hit: {}", cacheKey);
                return objectMapper.readValue(cached, WeatherVO.class);
            }
        } catch (Exception e) {
            log.warn("[Weather] Redis read failed, proceeding to API call: {}", e.getMessage());
        }

        // 2. 调用和风天气 API
        try {
            WeatherVO vo = fetchFromApi(lon, lat);
            // 写入 Redis 缓存
            try {
                String json = objectMapper.writeValueAsString(vo);
                stringRedisTemplate.opsForValue().set(cacheKey, json, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
                log.debug("[Weather] Cached weather data, key={}, ttl={}min", cacheKey, CACHE_TTL_MINUTES);
            } catch (Exception e) {
                log.warn("[Weather] Redis write failed: {}", e.getMessage());
            }
            return vo;
        } catch (Exception e) {
            log.error("[Weather] API call failed for location={},{}, falling back to mock. Error: {}", lon, lat, e.getMessage());
            return buildMockWeather();
        }
    }

    // ── 私有方法 ────────────────────────────────────────────

    /**
     * 调用和风天气 API，同时获取实时天气与3日预报，合并为 WeatherVO
     */
    private WeatherVO fetchFromApi(double lon, double lat) throws Exception {
        String location = lon + "," + lat;
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();

        String nowUrl = baseUrl + "/weather/now?location=" + location + "&key=" + apiKey;
        String forecastUrl = baseUrl + "/weather/3d?location=" + location + "&key=" + apiKey;

        // 1. 发送请求并以 InputStream 形式接收响应（核心变化）
        HttpResponse<java.io.InputStream> nowResponse = client.send(
                HttpRequest.newBuilder().uri(URI.create(nowUrl)).timeout(Duration.ofSeconds(10)).GET().build(),
                HttpResponse.BodyHandlers.ofInputStream()
        );
        HttpResponse<java.io.InputStream> forecastResponse = client.send(
                HttpRequest.newBuilder().uri(URI.create(forecastUrl)).timeout(Duration.ofSeconds(10)).GET().build(),
                HttpResponse.BodyHandlers.ofInputStream()
        );

        // 2. 调用下面的解压工具方法转为字符串
        String nowBody = decompressGzip(nowResponse);
        String forecastBody = decompressGzip(forecastResponse);

        log.debug("[Weather] API now response: {}", nowBody);
        log.debug("[Weather] API forecast response: {}", forecastBody);

        return parseResponse(nowBody, forecastBody);
    }

    /**
     * 处理和风天气返回的 GZIP 压缩流
     */
    private String decompressGzip(HttpResponse<java.io.InputStream> response) throws Exception {
        // 检查响应头是否包含 gzip
        String contentEncoding = response.headers().firstValue("Content-Encoding").orElse("");

        try (java.io.InputStream is = response.body()) {
            if ("gzip".equalsIgnoreCase(contentEncoding)) {
                // 如果是 gzip 格式，使用 GZIPInputStream 解压
                try (java.util.zip.GZIPInputStream gis = new java.util.zip.GZIPInputStream(is)) {
                    return new String(gis.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                }
            } else {
                // 如果不是压缩格式，直接读取
                return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }
        }
    }

    /**
     * 解析和风天气 API 响应 JSON，构造 WeatherVO
     * 和风天气成功状态码为 "200"（字符串）
     */
    private WeatherVO parseResponse(String nowBody, String forecastBody) throws Exception {
        JsonNode nowRoot = objectMapper.readTree(nowBody);
        JsonNode forecastRoot = objectMapper.readTree(forecastBody);

        // 检查响应状态
        String nowCode = nowRoot.path("code").asText();
        if (!"200".equals(nowCode)) {
            throw new RuntimeException("和风天气实时接口返回错误码: " + nowCode);
        }

        JsonNode now = nowRoot.path("now");

        // 解析3日预报
        List<WeatherVO.DailyForecast> forecastList = new ArrayList<>();
        String forecastCode = forecastRoot.path("code").asText();
        if ("200".equals(forecastCode)) {
            for (JsonNode day : forecastRoot.path("daily")) {
                forecastList.add(WeatherVO.DailyForecast.builder()
                        .date(day.path("fxDate").asText())
                        .tempMax(day.path("tempMax").asText())
                        .tempMin(day.path("tempMin").asText())
                        .icon(day.path("iconDay").asText())
                        .text(day.path("textDay").asText())
                        .build());
            }
        } else {
            log.warn("[Weather] Forecast API returned code: {}", forecastCode);
        }

        return WeatherVO.builder()
                .temp(now.path("temp").asText())
                .feelsLike(now.path("feelsLike").asText())
                .icon(now.path("icon").asText())
                .text(now.path("text").asText())
                .windDir(now.path("windDir").asText())
                .windScale(now.path("windScale").asText())
                .humidity(now.path("humidity").asText())
                .obsTime(now.path("obsTime").asText())
                .forecast(forecastList)
                .build();
    }

    /**
     * 降级 Mock 数据：返回明月山（宜春）典型春季天气
     * 仅在和风 API 调用失败时使用，确保前端 WeatherCard 不显示"加载失败"
     */
    private WeatherVO buildMockWeather() {
        log.info("[Weather] Using mock weather data for fallback");
        List<WeatherVO.DailyForecast> mockForecast = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            String date = LocalDate.now().plusDays(i).toString();
            mockForecast.add(WeatherVO.DailyForecast.builder()
                    .date(date)
                    .tempMax("26")
                    .tempMin("17")
                    .icon("104")
                    .text("多云")
                    .build());
        }
        return WeatherVO.builder()
                .temp("22")
                .feelsLike("21")
                .icon("104")
                .text("多云")
                .windDir("南风")
                .windScale("2")
                .humidity("68")
                .obsTime(java.time.LocalDateTime.now().toString())
                .forecast(mockForecast)
                .build();
    }
}
