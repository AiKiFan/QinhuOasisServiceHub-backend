package com.qinhu.oasis.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户追加回复请求 DTO（仅 PROCESSING 状态可追加）
 *
 * @author AiKiFan
 * @date 2026-05-08
 */
@Data
public class AppendReplyReq {
    @NotBlank
    @Size(max = 1000)
    private String replyContent;
}
