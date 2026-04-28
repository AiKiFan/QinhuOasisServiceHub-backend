package com.qinhu.oasis.common.constant;

/**
 * 攻略/动态类型常量
 * <p>1-官方攻略（管理员发布）2-游客攻略（需审核）3-游客动态（短内容，直接发布）</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public final class PostType {

    public static final int OFFICIAL   = 1;
    public static final int TOURIST    = 2;
    public static final int DYNAMIC    = 3;

    private PostType() {
    }
}
