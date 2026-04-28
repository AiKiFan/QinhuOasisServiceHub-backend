package com.qinhu.oasis.ugc.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 发表评价/评论请求参数
 * <p>targetType: 参见 {@link com.qinhu.oasis.common.constant.CommentTargetType}</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class CreateCommentReq {

    @NotNull(message = "目标 ID 不能为空")
    private Long targetId;

    /** 目标类型：1-餐厅 2-攻略 3-译员订单 4-车位订单 */
    @NotNull(message = "目标类型不能为空")
    private Integer targetType;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论最多1000个字符")
    private String content;

    /** 评分 1-5（对餐厅/服务评价时必填） */
    @Min(value = 1, message = "评分最低1星")
    @Max(value = 5, message = "评分最高5星")
    private Integer rating;

    /** 评论图片（最多3张 URL） */
    @Size(max = 3, message = "最多上传3张图片")
    private List<String> images;

    /** 父评论 ID（回复楼层时传入） */
    private Long parentId;

    /** 关联订单 ID（对服务/车位评价时必填） */
    private Long orderId;
}
