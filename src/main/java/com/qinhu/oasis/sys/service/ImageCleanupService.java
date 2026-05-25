package com.qinhu.oasis.sys.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qinhu.oasis.interpreter.entity.InterpreterProfile;
import com.qinhu.oasis.interpreter.mapper.InterpreterProfileMapper;
import com.qinhu.oasis.restaurant.entity.Restaurant;
import com.qinhu.oasis.restaurant.mapper.RestaurantMapper;
import com.qinhu.oasis.sys.entity.SysUser;
import com.qinhu.oasis.sys.mapper.SysUserMapper;
import com.qinhu.oasis.tourism.entity.ScenicSpot;
import com.qinhu.oasis.tourism.mapper.ScenicSpotMapper;
import com.qinhu.oasis.ugc.entity.UgcPost;
import com.qinhu.oasis.ugc.mapper.UgcPostMapper;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MinIO 图片可用性检测与清理服务
 * <p>功能：扫描所有图片 URL，检查 MinIO 中是否存在对应对象，
 *        对不存在的图片路径清空，避免前端显示 NoSuchKey 错误。</p>
 *
 * <p>清理范围：
 * <ul>
 *   <li>sys_user.avatar - 用户头像</li>
 *   <li>interpreter_profile.cert_url - 译员证书</li>
 *   <li>biz_restaurant.cover_img, images - 餐厅封面和图片</li>
 *   <li>biz_scenic_spot.cover_img, images - 景点封面和图片</li>
 *   <li>ugc_post.cover_img, images - UGC 封面和图片</li>
 * </ul>
 * </p>
 *
 * @author AiKiFan
 * @date 2026-05-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageCleanupService {

    private final MinioClient minioClient;
    private final SysUserMapper sysUserMapper;
    private final InterpreterProfileMapper interpreterProfileMapper;
    private final RestaurantMapper restaurantMapper;
    private final ScenicSpotMapper scenicSpotMapper;
    private final UgcPostMapper ugcPostMapper;
    private final ObjectMapper objectMapper;

    @Value("${minio.endpoint}")
    private String minioEndpoint;

    /** 清理结果统计 */
    public static class CleanupResult {
        public int usersChecked = 0;
        public int usersCleaned = 0;
        public int interpretersChecked = 0;
        public int interpretersCleaned = 0;
        public int restaurantsChecked = 0;
        public int restaurantsCleaned = 0;
        public int scenicSpotsChecked = 0;
        public int scenicSpotsCleaned = 0;
        public int ugcPostsChecked = 0;
        public int ugcPostsCleaned = 0;
        public final List<String> missingObjects = new ArrayList<>();

        @Override
        public String toString() {
            return String.format(
                "清理完成：\n" +
                "用户头像：检查 %d 个，清理 %d 个\n" +
                "译员证书：检查 %d 个，清理 %d 个\n" +
                "餐厅图片：检查 %d 个，清理 %d 个\n" +
                "景点图片：检查 %d 个，清理 %d 个\n" +
                "UGC 图片：检查 %d 个，清理 %d 个\n" +
                "失效对象列表：%s",
                usersChecked, usersCleaned,
                interpretersChecked, interpretersCleaned,
                restaurantsChecked, restaurantsCleaned,
                scenicSpotsChecked, scenicSpotsCleaned,
                ugcPostsChecked, ugcPostsCleaned,
                missingObjects.size() > 10
                    ? missingObjects.subList(0, 10) + "...(共" + missingObjects.size() + "个)"
                    : missingObjects
            );
        }
    }

    /**
     * 执行全量图片清理
     */
    @Transactional
    public CleanupResult cleanupAll() {
        CleanupResult result = new CleanupResult();
        cleanupUsers(result);
        cleanupInterpreters(result);
        cleanupRestaurants(result);
        cleanupScenicSpots(result);
        cleanupUgcPosts(result);
        return result;
    }

    // ───────────── 用户头像清理 ─────────────

    private void cleanupUsers(CleanupResult result) {
        List<SysUser> allUsers = sysUserMapper.selectAllForCleanup();
        for (SysUser user : allUsers) {
            if (user.getAvatar() == null || user.getAvatar().isBlank()) continue;
            result.usersChecked++;
            if (!objectExists(user.getAvatar())) {
                log.warn("[ImageCleanup] 用户 {} 头像不存在: {}", user.getId(), user.getAvatar());
                result.missingObjects.add("用户头像[" + user.getId() + "]: " + truncateUrl(user.getAvatar()));
                user.setAvatar(null);
                sysUserMapper.updateById(user);
                result.usersCleaned++;
            }
        }
    }

    // ───────────── 译员证书清理 ─────────────

    private void cleanupInterpreters(CleanupResult result) {
        List<InterpreterProfile> profiles = interpreterProfileMapper.selectAllForCleanup();
        for (InterpreterProfile profile : profiles) {
            if (profile.getCertUrl() == null || profile.getCertUrl().isBlank()) continue;
            result.interpretersChecked++;
            if (!objectExists(profile.getCertUrl())) {
                log.warn("[ImageCleanup] 译员档案 {} 证书不存在: {}", profile.getId(), profile.getCertUrl());
                result.missingObjects.add("译员证书[" + profile.getId() + "]: " + truncateUrl(profile.getCertUrl()));
                profile.setCertUrl(null);
                interpreterProfileMapper.updateById(profile);
                result.interpretersCleaned++;
            }
        }
    }

    // ───────────── 餐厅图片清理 ─────────────

    private void cleanupRestaurants(CleanupResult result) {
        List<Restaurant> restaurants = restaurantMapper.selectAllForCleanup();
        for (Restaurant r : restaurants) {
            boolean changed = false;

            if (r.getCoverImg() != null && !r.getCoverImg().isBlank()) {
                result.restaurantsChecked++;
                if (!objectExists(r.getCoverImg())) {
                    log.warn("[ImageCleanup] 餐厅 {} 封面不存在: {}", r.getId(), r.getCoverImg());
                    result.missingObjects.add("餐厅封面[" + r.getId() + "]: " + truncateUrl(r.getCoverImg()));
                    r.setCoverImg(null);
                    changed = true;
                    result.restaurantsCleaned++;
                }
            }

            if (r.getImages() != null && !r.getImages().isBlank()) {
                try {
                    List<String> urls = parseImageUrls(r.getImages());
                    List<String> validUrls = new ArrayList<>();
                    boolean imagesChanged = false;
                    for (String url : urls) {
                        if (!objectExists(url)) {
                            log.warn("[ImageCleanup] 餐厅 {} 图片不存在: {}", r.getId(), url);
                            result.missingObjects.add("餐厅图片[" + r.getId() + "]: " + truncateUrl(url));
                            imagesChanged = true;
                        } else {
                            validUrls.add(url);
                        }
                    }
                    if (imagesChanged) {
                        r.setImages(objectMapper.writeValueAsString(validUrls));
                        changed = true;
                    }
                } catch (Exception e) {
                    log.error("[ImageCleanup] 餐厅 {} images JSON 解析失败: {}", r.getId(), e.getMessage());
                }
            }

            if (changed) {
                restaurantMapper.updateById(r);
            }
        }
    }

    // ───────────── 景点图片清理 ─────────────

    private void cleanupScenicSpots(CleanupResult result) {
        List<ScenicSpot> spots = scenicSpotMapper.selectAllForCleanup();
        for (ScenicSpot s : spots) {
            boolean changed = false;

            if (s.getCoverImg() != null && !s.getCoverImg().isBlank()) {
                result.scenicSpotsChecked++;
                if (!objectExists(s.getCoverImg())) {
                    log.warn("[ImageCleanup] 景点 {} 封面不存在: {}", s.getId(), s.getCoverImg());
                    result.missingObjects.add("景点封面[" + s.getId() + "]: " + truncateUrl(s.getCoverImg()));
                    s.setCoverImg(null);
                    changed = true;
                    result.scenicSpotsCleaned++;
                }
            }

            if (s.getImages() != null && !s.getImages().isBlank()) {
                try {
                    List<String> urls = parseImageUrls(s.getImages());
                    List<String> validUrls = new ArrayList<>();
                    boolean imagesChanged = false;
                    for (String url : urls) {
                        if (!objectExists(url)) {
                            log.warn("[ImageCleanup] 景点 {} 图片不存在: {}", s.getId(), url);
                            result.missingObjects.add("景点图片[" + s.getId() + "]: " + truncateUrl(url));
                            imagesChanged = true;
                        } else {
                            validUrls.add(url);
                        }
                    }
                    if (imagesChanged) {
                        s.setImages(objectMapper.writeValueAsString(validUrls));
                        changed = true;
                    }
                } catch (Exception e) {
                    log.error("[ImageCleanup] 景点 {} images JSON 解析失败: {}", s.getId(), e.getMessage());
                }
            }

            if (changed) {
                scenicSpotMapper.updateById(s);
            }
        }
    }

    // ───────────── UGC 图片清理 ─────────────

    private void cleanupUgcPosts(CleanupResult result) {
        List<UgcPost> posts = ugcPostMapper.selectAllForCleanup();
        for (UgcPost p : posts) {
            boolean changed = false;

            if (p.getCoverImg() != null && !p.getCoverImg().isBlank()) {
                result.ugcPostsChecked++;
                if (!objectExists(p.getCoverImg())) {
                    log.warn("[ImageCleanup] UGC {} 封面不存在: {}", p.getId(), p.getCoverImg());
                    result.missingObjects.add("UGC封面[" + p.getId() + "]: " + truncateUrl(p.getCoverImg()));
                    p.setCoverImg(null);
                    changed = true;
                    result.ugcPostsCleaned++;
                }
            }

            if (p.getImages() != null && !p.getImages().isBlank()) {
                try {
                    List<String> urls = parseImageUrls(p.getImages());
                    List<String> validUrls = new ArrayList<>();
                    boolean imagesChanged = false;
                    for (String url : urls) {
                        if (!objectExists(url)) {
                            log.warn("[ImageCleanup] UGC {} 图片不存在: {}", p.getId(), url);
                            result.missingObjects.add("UGC图片[" + p.getId() + "]: " + truncateUrl(url));
                            imagesChanged = true;
                        } else {
                            validUrls.add(url);
                        }
                    }
                    if (imagesChanged) {
                        p.setImages(objectMapper.writeValueAsString(validUrls));
                        changed = true;
                    }
                } catch (Exception e) {
                    log.error("[ImageCleanup] UGC {} images JSON 解析失败: {}", p.getId(), e.getMessage());
                }
            }

            if (changed) {
                ugcPostMapper.updateById(p);
            }
        }
    }

    // ───────────── MinIO 对象检测核心方法 ─────────────

    private boolean objectExists(String url) {
        if (url == null || url.isBlank()) return true;
        try {
            String objectKey = extractObjectKey(url);
            if (objectKey == null) {
                return true;
            }
            String bucket = extractBucket(url);
            minioClient.statObject(
                io.minio.StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build()
            );
            return true;
        } catch (io.minio.errors.ErrorResponseException e) {
            String code = e.errorResponse().code();
            if ("NoSuchKey".equals(code) || "NoSuchBucket".equals(code)) {
                return false;
            }
            log.warn("[ImageCleanup] MinIO statObject 出错（视为存在）: {} - {}", url, e.getMessage());
            return true;
        } catch (Exception e) {
            log.warn("[ImageCleanup] MinIO 连接异常（视为存在）: {} - {}", url, e.getMessage());
            return true;
        }
    }

    private String extractObjectKey(String urlStr) {
        try {
            URL url = new URL(urlStr);
            String path = url.getPath();
            if (path == null || path.isBlank() || path.equals("/")) {
                return null;
            }
            return path.substring(1);
        } catch (Exception e) {
            log.warn("[ImageCleanup] 无法解析 URL: {}", urlStr);
            return null;
        }
    }

    private String extractBucket(String urlStr) {
        String objectKey = extractObjectKey(urlStr);
        if (objectKey == null) return null;
        int slashIdx = objectKey.indexOf('/');
        if (slashIdx <= 0) return null;
        return objectKey.substring(0, slashIdx);
    }

    private String truncateUrl(String url) {
        if (url == null) return "null";
        return url.length() > 80 ? url.substring(0, 80) + "..." : url;
    }

    /** 刷新结果统计 */
    public static class RefreshResult {
        public int usersRefreshed = 0;
        public int interpretersRefreshed = 0;
        public int restaurantsRefreshed = 0;
        public int scenicSpotsRefreshed = 0;
        public int ugcPostsRefreshed = 0;

        @Override
        public String toString() {
            return String.format(
                "刷新完成：\n" +
                "用户头像：刷新 %d 个\n" +
                "译员证书：刷新 %d 个\n" +
                "餐厅图片：刷新 %d 个\n" +
                "景点图片：刷新 %d 个\n" +
                "UGC 图片：刷新 %d 个",
                usersRefreshed, interpretersRefreshed, restaurantsRefreshed, scenicSpotsRefreshed, ugcPostsRefreshed
            );
        }
    }

    /**
     * 刷新所有图片 URL：
     * 1. 去除预签名参数（?X-Amz-Signature=...）
     * 2. 将错误 IP（10.220.119.171、192.168.x.x 等）替换为 localhost
     */
    public RefreshResult refreshAllUrls() {
        RefreshResult result = new RefreshResult();
        refreshUsers(result);
        refreshInterpreters(result);
        refreshRestaurants(result);
        refreshScenicSpots(result);
        refreshUgcPosts(result);
        return result;
    }

    private void refreshUsers(RefreshResult result) {
        List<SysUser> allUsers = sysUserMapper.selectAllForCleanup();
        for (SysUser user : allUsers) {
            if (user.getAvatar() == null || user.getAvatar().isBlank()) continue;
            String fixed = fixImageUrl(user.getAvatar());
            if (!user.getAvatar().equals(fixed)) {
                log.info("[ImageRefresh] 用户 {} 头像 URL 刷新: {} → {}", user.getId(), truncateUrl(user.getAvatar()), truncateUrl(fixed));
                user.setAvatar(fixed);
                sysUserMapper.updateById(user);
                result.usersRefreshed++;
            }
        }
    }

    private void refreshInterpreters(RefreshResult result) {
        List<InterpreterProfile> profiles = interpreterProfileMapper.selectAllForCleanup();
        for (InterpreterProfile profile : profiles) {
            if (profile.getCertUrl() == null || profile.getCertUrl().isBlank()) continue;

            // 译员证书可能存了多张（逗号分隔），逐个处理
            String[] certUrls = profile.getCertUrl().split(",");
            StringBuilder sb = new StringBuilder();
            boolean anyChanged = false;
            for (int i = 0; i < certUrls.length; i++) {
                String cert = certUrls[i].trim();
                if (cert.isEmpty()) continue;
                String fixed = fixImageUrl(cert);
                if (!cert.equals(fixed)) anyChanged = true;
                if (i > 0) sb.append(",");
                sb.append(fixed);
            }

            if (anyChanged) {
                String fixed = sb.toString();
                log.info("[ImageRefresh] 译员 {} 证书 URL 刷新", profile.getId());
                profile.setCertUrl(fixed);
                interpreterProfileMapper.updateById(profile);
                result.interpretersRefreshed++;
            }
        }
    }

    private void refreshRestaurants(RefreshResult result) {
        List<Restaurant> restaurants = restaurantMapper.selectAllForCleanup();
        for (Restaurant r : restaurants) {
            boolean changed = false;

            if (r.getCoverImg() != null && !r.getCoverImg().isBlank()) {
                String fixed = fixImageUrl(r.getCoverImg());
                if (!r.getCoverImg().equals(fixed)) {
                    log.info("[ImageRefresh] 餐厅 {} 封面 URL 刷新: {} → {}", r.getId(), truncateUrl(r.getCoverImg()), truncateUrl(fixed));
                    r.setCoverImg(fixed);
                    changed = true;
                    result.restaurantsRefreshed++;
                }
            }

            if (r.getImages() != null && !r.getImages().isBlank()) {
                try {
                    List<String> urls = parseImageUrls(r.getImages());
                    List<String> fixedUrls = urls.stream().map(this::fixImageUrl).collect(Collectors.toList());
                    r.setImages(objectMapper.writeValueAsString(fixedUrls));
                    changed = true;
                } catch (Exception e) {
                    log.error("[ImageRefresh] 餐厅 {} images JSON 解析失败: {}", r.getId(), e.getMessage());
                }
            }

            if (changed) {
                restaurantMapper.updateById(r);
            }
        }
    }

    private void refreshScenicSpots(RefreshResult result) {
        List<ScenicSpot> spots = scenicSpotMapper.selectAllForCleanup();
        for (ScenicSpot s : spots) {
            boolean changed = false;

            if (s.getCoverImg() != null && !s.getCoverImg().isBlank()) {
                String fixed = fixImageUrl(s.getCoverImg());
                if (!s.getCoverImg().equals(fixed)) {
                    log.info("[ImageRefresh] 景点 {} 封面 URL 刷新: {} → {}", s.getId(), truncateUrl(s.getCoverImg()), truncateUrl(fixed));
                    s.setCoverImg(fixed);
                    changed = true;
                    result.scenicSpotsRefreshed++;
                }
            }

            if (s.getImages() != null && !s.getImages().isBlank()) {
                try {
                    List<String> urls = parseImageUrls(s.getImages());
                    List<String> fixedUrls = urls.stream().map(this::fixImageUrl).collect(Collectors.toList());
                    s.setImages(objectMapper.writeValueAsString(fixedUrls));
                    changed = true;
                } catch (Exception e) {
                    log.error("[ImageRefresh] 景点 {} images JSON 解析失败: {}", s.getId(), e.getMessage());
                }
            }

            if (changed) {
                scenicSpotMapper.updateById(s);
            }
        }
    }

    private void refreshUgcPosts(RefreshResult result) {
        List<UgcPost> posts = ugcPostMapper.selectAllForCleanup();
        for (UgcPost p : posts) {
            boolean changed = false;

            if (p.getCoverImg() != null && !p.getCoverImg().isBlank()) {
                String fixed = fixImageUrl(p.getCoverImg());
                if (!p.getCoverImg().equals(fixed)) {
                    log.info("[ImageRefresh] UGC {} 封面 URL 刷新: {} → {}", p.getId(), truncateUrl(p.getCoverImg()), truncateUrl(fixed));
                    p.setCoverImg(fixed);
                    changed = true;
                    result.ugcPostsRefreshed++;
                }
            }

            if (p.getImages() != null && !p.getImages().isBlank()) {
                try {
                    List<String> urls = parseImageUrls(p.getImages());
                    List<String> fixedUrls = urls.stream().map(this::fixImageUrl).collect(Collectors.toList());
                    p.setImages(objectMapper.writeValueAsString(fixedUrls));
                    changed = true;
                } catch (Exception e) {
                    log.error("[ImageRefresh] UGC {} images JSON 解析失败: {}", p.getId(), e.getMessage());
                }
            }

            if (changed) {
                ugcPostMapper.updateById(p);
            }
        }
    }

    /**
     * 修复图片 URL：
     * 1. 去除预签名参数（?X-Amz-Signature=...&X-Amz-Expires=...）
     * 2. 将非 localhost 的 MinIO IP 替换为 localhost
     */
    private String fixImageUrl(String url) {
        if (url == null || url.isBlank()) return url;

        // 只处理 MinIO URL（含 :9000 端口）
        if (!url.contains(":9000")) return url;

        // 去除预签名参数
        int queryIdx = url.indexOf('?');
        String baseUrl = queryIdx > 0 ? url.substring(0, queryIdx) : url;

        // 替换错误 IP 为 localhost
        try {
            java.net.URL u = new java.net.URL(baseUrl);
            String host = u.getHost();
            if (!"localhost".equals(host) && !"127.0.0.1".equals(host)) {
                return "http://localhost:9000" + u.getPath();
            }
        } catch (Exception e) {
            // 不是标准 URL 格式，原样返回
        }

        return baseUrl;
    }

    private List<String> parseImageUrls(String json) throws Exception {
        return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
    }
}