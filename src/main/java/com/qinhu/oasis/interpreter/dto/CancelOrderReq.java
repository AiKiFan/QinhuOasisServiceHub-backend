package com.qinhu.oasis.interpreter.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 取消/拒绝订单请求参数
 *
 * @author AiKiFan
 * @date 2026-05-08
 */
@Data
public class CancelOrderReq {

    /** 取消/拒绝理由（可选，最多200字） */
    @Size(max = 200, message = "理由长度不能超过200字")
    private String reason;
}