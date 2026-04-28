package com.qinhu.oasis.ugc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 文件上传响应视图对象
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
@AllArgsConstructor
public class FileUploadVO {

    /** 文件访问 URL（Minio 对象完整路径） */
    private String url;
    /** 原始文件名 */
    private String originalName;
    /** 文件大小（字节） */
    private Long size;
}
