package com.qinhu.oasis.common.constant;

/**
 * 攻略/动态状态常量
 * <p>0-草稿 1-已发布 2-审核中 3-已下架</p>
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public final class PostStatus {

    public static final int DRAFT      = 0;
    public static final int PUBLISHED  = 1;
    public static final int REVIEWING  = 2;
    public static final int TAKEN_DOWN = 3;

    private PostStatus() {
    }
}
