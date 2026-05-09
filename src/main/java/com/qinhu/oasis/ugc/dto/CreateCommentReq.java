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

    @NotNull(message = "{validation.targetId.notNull}")
    private Long targetId;

    /** 目标类型：1-餐厅 2-攻略 3-译员订单 4-车位订单 */
    @NotNull(message = "{validation.targetType.notNull}")
    private Integer targetType;

    @NotBlank(message = "{validation.content.notBlank}")
    @Size(max = 1000, message = "{validation.content.size}")
    private String content;

    /** 评分 1-5（对餐厅/服务评价时必填） */
    @Min(value = 1, message = "{validation.rating.min}")
    @Max(value = 5, message = "{validation.rating.max}")
    private Integer rating;

    /** 评论图片（最多3张 URL） */
    @Size(max = 3, message = "{validation.images.size}")
    private List<String> images;

    /** 父评论 ID（回复楼层时传入） */
    private Long parentId;

    /** 关联订单 ID（对服务/车位评价时必填） */
    private Long orderId;
}
