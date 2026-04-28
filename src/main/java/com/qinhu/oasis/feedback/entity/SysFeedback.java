package com.qinhu.oasis.feedback.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 投诉建议实体类，对应数据库表 sys_feedback
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class SysFeedback {

    private Long id;
    /** 提交用户 ID（NULL 代表匿名提交） */
    private Long userId;
    /**
     * 类型（参见 FeedbackType）：1=投诉 2=建议 3=咨询 4=其他
     */
    private Integer feedbackType;
    /** 反馈主题 */
    private String title;
    /** 详细描述 */
    private String content;
    /** 图片附件 JSON 数组（Minio URL），以字符串形式存储 */
    private String images;
    /** 联系方式（手机/邮箱，匿名时也可填写） */
    private String contact;
    /**
     * 处理状态（参见 FeedbackStatus）：0=待处理 1=处理中 2=已解决 3=已关闭
     */
    private Integer status;
    /** 管理员回复内容 */
    private String replyContent;
    /** 回复时间 */
    private LocalDateTime replyTime;
    /** 处理人管理员 ID */
    private Long handlerId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
