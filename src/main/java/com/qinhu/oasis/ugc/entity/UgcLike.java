package com.qinhu.oasis.ugc.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 点赞记录实体，对应 ugc_like 表
 * <p>target_type：参见 {@link com.qinhu.oasis.common.constant.LikeTargetType}</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
public class UgcLike {

    private Long id;
    private Long userId;
    private Long targetId;
    /** 目标类型：1-攻略/动态 2-评论 */
    private Integer targetType;
    private LocalDateTime createTime;
}
