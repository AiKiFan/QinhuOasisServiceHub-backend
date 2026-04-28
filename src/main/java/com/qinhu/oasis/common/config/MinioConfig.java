package com.qinhu.oasis.common.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Minio 文件存储配置类，注入 {@link MinioClient} Bean
 * <p>对应 application.yml 中的 minio.* 配置项</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    /**
     * 构建 MinioClient，供文件上传/下载服务使用
     *
     * @return MinioClient 实例
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
