package com.qinhu.oasis.common.constant;

/**
 * 停车区域状态常量，对应 parking_space.status 字段
 *
 * @author AiKiFan
 * @date 2026-04-28
 */
public final class SpaceStatus {

    private SpaceStatus() {}

    /** 关闭 */
    public static final int CLOSED = 0;
    /** 开放 */
    public static final int OPEN = 1;
    /** 维护中 */
    public static final int MAINTENANCE = 2;
}
