package com.qinhu.oasis.feedback.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 提交投诉建议请求参数（支持匿名，userId 由 service 层从登录上下文获取）
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class CreateFeedbackReq {

    /** 类型：1=投诉 2=建议 3=咨询 4=其他 */
    @NotNull
    @Min(1)
    @Max(4)
    private Integer feedbackType;

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = 5000)
    private String content;

    /** 可选：图片 URL 列表（由 /files/upload 接口返回） */
    private List<String> images;

    /** 可选：联系方式 */
    @Size(max = 100)
    private String contact;
}
