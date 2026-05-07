package com.qinhu.oasis.common.init;

import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * MinIO Bucket 初始化器
 * <p>在 Spring Boot 启动时自动检查并创建 bucket，配置访问策略</p>
 *
 * <h3>权限策略表</h3>
 * <table border="1">
 *   <tr><th>Bucket</th><th>策略</th><th>存储内容</th></tr>
 *   <tr><td>qosh-ugc-images</td><td>public-read</td><td>游客攻略图、评论晒图</td></tr>
 *   <tr><td>qosh-interpreter-certs</td><td>私密</td><td>译员资质证书</td></tr>
 *   <tr><td>qosh-public-static</td><td>public-read</td><td>景区地图、官方攻略配图</td></tr>
 *   <tr><td>qosh-sys-assets</td><td>私密</td><td>报表导出、日志备份</td></tr>
 * </table>
 *
 * @author AiKiFan
 * @date 2026-05-05
 */
@Slf4j
@Component
public class MinioBucketInitializer implements CommandLineRunner {

    private final MinioClient minioClient;

    @Value("${minio.buckets.public-static}")
    private String publicStaticBucket;

    @Value("${minio.buckets.interpreter-certs}")
    private String interpreterCertsBucket;

    @Value("${minio.buckets.ugc-images}")
    private String ugcImagesBucket;

    @Value("${minio.buckets.sys-assets}")
    private String sysAssetsBucket;

    public MinioBucketInitializer(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void run(String... args) {
        log.info("========== MinIO Bucket 初始化开始 ==========");
        try {
            // 公开读取 bucket（游客图片、译员证书、静态资源）
            createBucketWithPublicRead(ugcImagesBucket, "游客攻略图、评论晒图");
            createBucketWithPublicRead(publicStaticBucket, "景区地图、官方攻略配图");
            createBucketWithPublicRead(interpreterCertsBucket, "译员资质证书");
            createPrivateBucket(sysAssetsBucket, "报表导出、日志备份");

            log.info("========== MinIO Bucket 初始化完成 ==========");
        } catch (Exception e) {
            log.error("MinIO Bucket 初始化失败", e);
        }
    }

    /**
     * 创建私密 bucket（默认策略）
     */
    private void createPrivateBucket(String bucketName, String description)
            throws ErrorResponseException, InsufficientDataException, InternalException,
                   InvalidResponseException, ServerException, XmlParserException,
                   IOException, NoSuchAlgorithmException, InvalidKeyException {
        if (!bucketExists(bucketName)) {
            minioClient.makeBucket(io.minio.MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("✓ 创建私密 Bucket: {} ({})", bucketName, description);
        } else {
            log.info("✓ Bucket 已存在: {} ({})", bucketName, description);
        }
    }

    /**
     * 创建公开读取 bucket 并设置匿名读策略
     */
    private void createBucketWithPublicRead(String bucketName, String description)
            throws ErrorResponseException, InsufficientDataException, InternalException,
                   InvalidResponseException, ServerException, XmlParserException,
                   IOException, NoSuchAlgorithmException, InvalidKeyException {
        if (!bucketExists(bucketName)) {
            minioClient.makeBucket(io.minio.MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("✓ 创建 Bucket: {} ({})", bucketName, description);
        } else {
            log.info("✓ Bucket 已存在: {} ({})", bucketName, description);
        }

        // 设置公开读取策略（允许匿名用户下载对象）
        String policy = buildPublicReadPolicy(bucketName);
        minioClient.setBucketPolicy(
                io.minio.SetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .config(policy)
                        .build()
        );
        log.info("  → 已设置 public-read 策略: {}", bucketName);
    }

    /**
     * 检查 bucket 是否存在
     */
    private boolean bucketExists(String bucketName)
            throws ErrorResponseException, InsufficientDataException, InternalException,
                   InvalidResponseException, ServerException, XmlParserException,
                   IOException, NoSuchAlgorithmException, InvalidKeyException {
        return minioClient.bucketExists(
                io.minio.BucketExistsArgs.builder().bucket(bucketName).build()
        );
    }

    /**
     * 构建 public-read 策略 JSON（允许匿名读取 bucket 中所有对象）
     */
    private String buildPublicReadPolicy(String bucketName) {
        return String.format("""
            {
              "Version": "2012-10-17",
              "Statement": [
                {
                  "Effect": "Allow",
                  "Principal": {
                    "AWS": ["*"]
                  },
                  "Action": ["s3:GetObject"],
                  "Resource": ["arn:aws:s3:::%s/*"]
                }
              ]
            }
            """, bucketName);
    }
}