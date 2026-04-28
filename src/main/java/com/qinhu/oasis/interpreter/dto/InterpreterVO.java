package com.qinhu.oasis.interpreter.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 译员信息展示 VO（列表与详情共用）
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class InterpreterVO {

    private Long id;
    private Long userId;
    /** 昵称（来自 sys_user JOIN） */
    private String nickname;
    /** 头像（来自 sys_user JOIN） */
    private String avatar;
    private String realName;
    private String studentId;
    private String school;
    private Integer englishLevel;
    private String certUrl;
    private String certNo;
    private String introduction;
    private String introductionEn;
    /** 国际化显示文本，由 service 根据 locale 设置 */
    private String displayIntroduction;
    private Integer serviceTypes;
    private BigDecimal hourlyRate;
    private BigDecimal rating;
    private Integer totalOrders;
    private Integer status;
    private String rejectReason;
    private LocalDateTime createTime;
}
