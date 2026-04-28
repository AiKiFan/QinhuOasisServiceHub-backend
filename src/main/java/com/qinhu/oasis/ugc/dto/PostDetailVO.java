package com.qinhu.oasis.ugc.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 攻略/动态详情视图对象（继承列表VO，补充正文和图片列表）
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostDetailVO extends PostListVO {

    /** HTML 富文本正文 */
    private String content;
    /**
     * 图片列表（JSON 数组字符串，@JsonRawValue 使其在响应中直接输出为 JSON 数组）
     * 示例：["http://localhost:9000/qosh-ugc-images/20260428/xxx.jpg"]
     */
    @JsonRawValue
    private String images;
}
