package com.qinhu.oasis.feedback.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 投诉建议展示 VO
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class FeedbackVO {

    private Long id;
    private Long userId;
    /** 提交者昵称（来自 sys_user JOIN，匿名时为 null） */
    private String userNickname;
    /** 类型：1=投诉 2=建议 3=咨询 4=其他 */
    private Integer feedbackType;
    private String title;
    private String content;
    /** 图片附件 JSON 数组，@JsonRawValue 直接序列化为 JSON 数组 */
    @JsonRawValue
    private String images;
    private String contact;
    private Integer status;
    private String replyContent;
    private LocalDateTime replyTime;
    private Long handlerId;
    /** 处理人昵称（来自 sys_user JOIN） */
    private String handlerNickname;
    private LocalDateTime createTime;
}
