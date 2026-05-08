package com.qinhu.oasis.interpreter.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预约译员请求参数
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class BookInterpreterReq {

    /** 译员档案 ID（interpreter_profile.id） */
    @NotNull
    private Long profileId;

    /** 服务类型：1=个人 2=团队 */
    @NotNull
    @Min(1)
    @Max(2)
    private Integer serviceType;

    /** 团队人数（团队服务时必填，默认 1） */
    @Min(1)
    private Integer groupSize;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    @Size(max = 500)
    private String remark;

    /** 联系电话（可选） */
    @Size(max = 20)
    private String phone;
}
