package com.qinhu.oasis.sys.service;

import com.fasterxml.jackson.core.type.TypeReference;
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
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
        // 查询所有有头像的用户（avatar 非空）
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

            // 检查封面
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

            // 检查图片列表（JSON 数组）
            if (r.getImages() != null && !r.getImages().isBlank()) {
                List<String> validUrls = new ArrayList<>();
                boolean imagesChanged = false;
                try {
                    List<String> urls = objectMapper.readValue(r.getImages(), new TypeReference<List<String>>() {});
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
                List<String> validUrls = new ArrayList<>();
                boolean imagesChanged = false;
                try {
                    List<String> urls = objectMapper.readValue(s.getImages(), new TypeReference<List<String>>() {});
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
                List<String> validUrls = new ArrayList<>();
                boolean imagesChanged = false;
                try {
                    List<String> urls = objectMapper.readValue(p.getImages(), new TypeReference<List<String>>() {});
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

    /**
     * 检查 MinIO 中指定 URL 的对象是否存在
     */
    private boolean objectExists(String url) {
        if (url == null || url.isBlank()) return true;
        try {
            // 解析 URL，提取 bucket 和 objectName
            String objectKey = extractObjectKey(url);
            if (objectKey == null) {
                // 无法解析，说明不是 MinIO URL，跳过清理
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
            if ("NoSuchKey".equals(e.errorResponse().code()) ||
                "NoSuchBucket".equals(e.errorResponse().code())) {
                return false;
            }
            // 其他错误（如网络问题），保守处理：视为存在，不清空
            log.warn("[ImageCleanup] MinIO statObject 出错（视为存在）: {} - {}", url, e.getMessage());
            return true;
        } catch (Exception e) {
            // 网络超时等，保守处理
            log.warn("[ImageCleanup] MinIO 连接异常（视为存在）: {} - {}", url, e.getMessage());
            return true;
        }
    }

    /**
     * 从 MinIO 预签名 URL 中提取对象路径（object key）
     * 例如：http://localhost:9000/bucket/20260516/xxx.jpg?X-Amz-... → bucket/20260516/xxx.jpg
     */
    private String extractObjectKey(String urlStr) {
        try {
            URL url = new URL(urlStr);
            String path = url.getPath(); // /bucket/20260516/xxx.jpg
            if (path == null || path.isBlank() || path.equals("/")) return null;
            // 去掉前导 /
            return path.substring(1);
        } catch (Exception e) {
            log.warn("[ImageCleanup] 无法解析 URL: {}", urlStr);
            return null;
        }
    }

    /**
     * 从 MinIO URL 中提取 bucket 名称
     */
    private String extractBucket(String urlStr) {
        String objectKey = extractObjectKey(urlStr);
        if (objectKey == null) return null;
        int slashIdx = objectKey.indexOf('/');
        if (slashIdx <= 0) return null;
        return objectKey.substring(0, slashIdx);
    }

    /** 截断 URL 方便日志显示 */
    private String truncateUrl(String url) {
        if (url == null) return "null";
        return url.length() > 80 ? url.substring(0, 80) + "..." : url;
    }
}
