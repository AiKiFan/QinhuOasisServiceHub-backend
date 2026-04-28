package com.qinhu.oasis.interpreter.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 译员档案实体类，对应数据库表 interpreter_profile
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class InterpreterProfile {

    private Long id;
    /** 关联用户 ID（sys_user.id） */
    private Long userId;
    /** 真实姓名 */
    private String realName;
    /** 学号 */
    private String studentId;
    /** 所在院校 */
    private String school;
    /**
     * 英语水平：0=CET4 1=CET6 2=TEM4 3=TEM8 4=其他
     */
    private Integer englishLevel;
    /** 英语证书图片 URL（Minio: qosh-interpreter-certs） */
    private String certUrl;
    /** 证书编号 */
    private String certNo;
    /** 中文自我介绍 */
    private String introduction;
    /** 英文自我介绍 */
    private String introductionEn;
    /**
     * 服务类型（位运算）：1=仅个人 2=仅团队 3=均可
     */
    private Integer serviceTypes;
    /** 服务时薪（元/小时） */
    private BigDecimal hourlyRate;
    /** 综合评分（1.00-5.00） */
    private BigDecimal rating;
    /** 历史总接单数 */
    private Integer totalOrders;
    /**
     * 审核状态（参见 InterpreterStatus）：0=待审核 1=已通过 2=已拒绝 3=暂停接单
     */
    private Integer status;
    /** 审核拒绝原因 */
    private String rejectReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
