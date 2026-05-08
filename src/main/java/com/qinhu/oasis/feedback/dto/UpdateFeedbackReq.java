package com.qinhu.oasis.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 用户更新投诉建议请求 DTO（仅 PENDING 状态可改）
 *
 * @author AiKiFan
 * @date 2026-05-08
 */
@Data
public class UpdateFeedbackReq {
    @NotBlank
    @Size(max = 100)
    private String title;

    @NotBlank
    @Size(max = 2000)
    private String content;

    private List<String> images;

    @Size(max = 100)
    private String contact;
}
