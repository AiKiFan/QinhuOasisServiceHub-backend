package com.qinhu.oasis.ugc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 发布攻略/动态请求参数
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class CreatePostReq {

    /** 类型：2-游客攻略 3-游客动态（1-官方攻略仅管理员可用） */
    @NotNull(message = "请选择发布类型")
    private Integer postType;

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题最多200个字符")
    private String title;

    /** 英文标题（可选） */
    @Size(max = 300, message = "英文标题最多300个字符")
    private String titleEn;

    /** 摘要（可选，列表页展示用） */
    @Size(max = 500, message = "摘要最多500个字符")
    private String summary;

    @NotBlank(message = "正文内容不能为空")
    private String content;

    /** 封面图 URL（可选，由先行调用上传接口获得） */
    private String coverImg;

    /** 图片列表（最多9张，URL 列表） */
    @Size(max = 9, message = "最多上传9张图片")
    private List<String> images;
}
