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
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 天气服务实现 - 升级为和风天气 EdDSA JWT 鉴权模式
 *
 * @author AiKiFan
 * @date 2026-05-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private static final String CACHE_KEY_PREFIX = "weather:now:";
    private static final long CACHE_TTL_MINUTES = 30;

    // ── 从 application.yml 读取新配置 ───────────────────────
    @Value("${third-party.hefeng-weather.base-url}")
    private String baseUrl;

    @Value("${third-party.hefeng-weather.project-id}")
    private String projectId; // 对应截图中的项目 ID: 4C88VDQTG9

    @Value("${third-party.hefeng-weather.key-id}")
    private String keyId; // 对应截图中的凭据 ID: T9PRE4WXUE

    @Value("${third-party.hefeng-weather.private-key}")
    private String privateKeyString;

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public WeatherVO getWeather(double lon, double lat) {
        String cacheKey = CACHE_KEY_PREFIX + lon + "," + lat;

        // 1. 尝试 Redis 缓存
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("[Weather] Redis cache hit: {}", cacheKey);
                return objectMapper.readValue(cached, WeatherVO.class);
            }
        } catch (Exception e) {
            log.warn("[Weather] Redis read failed: {}", e.getMessage());
        }

        // 2. 调用 API
        try {
            WeatherVO vo = fetchFromApi(lon, lat);
            // 写入 Redis
            try {
                String json = objectMapper.writeValueAsString(vo);
                stringRedisTemplate.opsForValue().set(cacheKey, json, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn("[Weather] Redis write failed: {}", e.getMessage());
            }
            return vo;
        } catch (Exception e) {
            log.error("[Weather] API Error for location={},{}, fallback to mock. Error: {}", lon, lat, e.getMessage());
            return buildMockWeather();
        }
    }

    /**
     * 核心：生成 EdDSA 算法的 JWT Token (官方标准实现)
     */
    private String generateQWeatherToken() throws Exception {
        // 1. 提取纯净的私钥 Base64 字符串
        String cleanKey = privateKeyString
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] privateKeyBytes = Base64.getDecoder().decode(cleanKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EdDSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        // 2. 构造 Header (alg=EdDSA, kid=凭据ID)
        String headerJson = "{\"alg\": \"EdDSA\", \"kid\": \"" + keyId + "\"}";

        // 3. 构造 Payload (sub=项目ID, iat=当前时间前30秒)
        long iat = ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond() - 30;
        long exp = iat + 900; // 15分钟有效
        String payloadJson = "{\"sub\": \"" + projectId + "\", \"iat\": " + iat + ", \"exp\": " + exp + "}";

        // 4. Base64URL 编码 (无填充)
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String headerEncoded = encoder.encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
        String payloadEncoded = encoder.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String dataToSign = headerEncoded + "." + payloadEncoded;

        // 5. 签名
        Signature signer = Signature.getInstance("EdDSA");
        signer.initSign(privateKey);
        signer.update(dataToSign.getBytes(StandardCharsets.UTF_8));
        byte[] signature = signer.sign();
        String signatureEncoded = encoder.encodeToString(signature);

        return dataToSign + "." + signatureEncoded;
    }

    private WeatherVO fetchFromApi(double lon, double lat) throws Exception {
        String location = lon + "," + lat;
        // 生成 Token
        String token = generateQWeatherToken();

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();

        // 注意：URL 绝对不能再带 &key=
        String nowUrl = baseUrl + "/weather/now?location=" + location;
        String forecastUrl = baseUrl + "/weather/3d?location=" + location;

        // 构建带 Authorization Header 的请求
        HttpRequest nowReq = HttpRequest.newBuilder()
                .uri(URI.create(nowUrl))
                .header("Authorization", "Bearer " + token)
                .timeout(Duration.ofSeconds(10))
                .GET().build();

        HttpRequest forecastReq = HttpRequest.newBuilder()
                .uri(URI.create(forecastUrl))
                .header("Authorization", "Bearer " + token)
                .timeout(Duration.ofSeconds(10))
                .GET().build();

        // 获取并解压响应
        String nowBody = decompressGzip(client.send(nowReq, HttpResponse.BodyHandlers.ofInputStream()));
        String forecastBody = decompressGzip(client.send(forecastReq, HttpResponse.BodyHandlers.ofInputStream()));

        log.debug("[Weather] API Success. Now: {}, Forecast: {}", nowBody, forecastBody);
        return parseResponse(nowBody, forecastBody);
    }

    private String decompressGzip(HttpResponse<java.io.InputStream> response) throws Exception {
        String contentEncoding = response.headers().firstValue("Content-Encoding").orElse("");
        try (java.io.InputStream is = response.body()) {
            if ("gzip".equalsIgnoreCase(contentEncoding)) {
                try (java.util.zip.GZIPInputStream gis = new java.util.zip.GZIPInputStream(is)) {
                    return new String(gis.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                }
            }
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    private WeatherVO parseResponse(String nowBody, String forecastBody) throws Exception {
        JsonNode nowRoot = objectMapper.readTree(nowBody);
        JsonNode forecastRoot = objectMapper.readTree(forecastBody);

        if (!"200".equals(nowRoot.path("code").asText())) {
            throw new RuntimeException("和风接口错误: " + nowBody);
        }

        JsonNode now = nowRoot.path("now");
        List<WeatherVO.DailyForecast> forecastList = new ArrayList<>();

        if ("200".equals(forecastRoot.path("code").asText())) {
            for (JsonNode day : forecastRoot.path("daily")) {
                forecastList.add(WeatherVO.DailyForecast.builder()
                        .date(day.path("fxDate").asText())
                        .tempMax(day.path("tempMax").asText())
                        .tempMin(day.path("tempMin").asText())
                        .icon(day.path("iconDay").asText())
                        .text(day.path("textDay").asText())
                        .build());
            }
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

    private WeatherVO buildMockWeather() {
        log.info("[Weather] Returning fallback mock data");
        List<WeatherVO.DailyForecast> mockForecast = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            mockForecast.add(WeatherVO.DailyForecast.builder()
                    .date(LocalDate.now().plusDays(i).toString())
                    .tempMax("28").tempMin("18").icon("100").text("晴")
                    .build());
        }
        return WeatherVO.builder()
                .temp("24").feelsLike("23").icon("100").text("晴")
                .windDir("南风").windScale("3").humidity("50")
                .obsTime(java.time.LocalDateTime.now().toString())
                .forecast(mockForecast)
                .build();
    }
}