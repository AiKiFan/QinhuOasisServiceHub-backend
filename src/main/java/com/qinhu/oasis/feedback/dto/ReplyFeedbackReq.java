package com.qinhu.oasis.feedback.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员回复/处理投诉建议请求参数
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class ReplyFeedbackReq {

    @NotBlank
    @Size(max = 2000)
    private String replyContent;

    /** 处理后目标状态：1=处理中 2=已解决 3=已关闭 */
    @NotNull
    @Min(1)
    @Max(3)
    private Integer status;
}
