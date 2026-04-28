package com.qinhu.oasis.common.service;

import com.qinhu.oasis.common.exception.BizException;
import com.qinhu.oasis.common.i18n.I18nUtil;
import com.qinhu.oasis.common.result.ResultCode;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

/**
 * Minio 文件存储服务
 * <p>负责校验文件类型、生成存储路径并上传到指定 Bucket，返回可访问 URL</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final MinioClient minioClient;
    private final I18nUtil i18nUtil;

    @Value("${minio.endpoint}")
    private String endpoint;

    /**
     * 上传图片文件到指定 Bucket
     *
     * @param file   上传的文件（仅允许 JPEG/PNG/WebP/GIF）
     * @param bucket Minio Bucket 名称
     * @return 文件访问 URL（格式：{endpoint}/{bucket}/{yyyyMMdd}/{uuid}.{ext}）
     */
    public String uploadImage(MultipartFile file, String bucket) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new BizException(ResultCode.FILE_TYPE_NOT_ALLOWED,
                    i18nUtil.msg(ResultCode.FILE_TYPE_NOT_ALLOWED));
        }

        String ext = resolveExtension(file.getOriginalFilename(), contentType);
        String objectName = LocalDate.now().format(DATE_FORMATTER)
                + "/" + UUID.randomUUID().toString().replace("-", "") + "." + ext;

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            log.error("Minio upload failed: bucket={}, object={}", bucket, objectName, e);
            throw new BizException(ResultCode.FILE_UPLOAD_FAIL,
                    i18nUtil.msg(ResultCode.FILE_UPLOAD_FAIL));
        }

        return endpoint + "/" + bucket + "/" + objectName;
    }

    // ───────────── 私有辅助方法 ─────────────

    private String resolveExtension(String filename, String contentType) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        }
        // MIME 类型兜底
        return switch (contentType.toLowerCase()) {
            case "image/png"  -> "png";
            case "image/webp" -> "webp";
            case "image/gif"  -> "gif";
            default           -> "jpg";
        };
    }
}
