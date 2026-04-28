package com.qinhu.oasis.interpreter.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 申请成为译员请求参数
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class ApplyInterpreterReq {

    @NotBlank
    @Size(max = 50)
    private String realName;

    @NotBlank
    @Size(max = 30)
    private String studentId;

    @Size(max = 100)
    private String school;

    /** 英语水平：0=CET4 1=CET6 2=TEM4 3=TEM8 4=其他 */
    @NotNull
    @Min(0)
    @Max(4)
    private Integer englishLevel;

    /** 英语证书图片 URL（由 cert-upload 接口返回） */
    private String certUrl;

    @Size(max = 50)
    private String certNo;

    @Size(max = 1000)
    private String introduction;

    @Size(max = 1000)
    private String introductionEn;

    /** 服务类型（位运算）：1=仅个人 2=仅团队 3=均可 */
    @NotNull
    @Min(1)
    @Max(3)
    private Integer serviceTypes;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal hourlyRate;
}
