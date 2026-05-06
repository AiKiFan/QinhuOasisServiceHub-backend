-- 用户收藏表创建脚本
-- Migration: 20260506_create_user_favorites.sql
-- Author: AiKiFan

CREATE TABLE IF NOT EXISTS biz_user_favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '收藏ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_type VARCHAR(20) NOT NULL COMMENT '收藏对象类型：restaurant/interpreter/scenic',
    target_id BIGINT NOT NULL COMMENT '收藏对象ID',
    folder_id BIGINT COMMENT '收藏夹ID（可选）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    deleted INT DEFAULT 0 COMMENT '软删除标记：0-正常 1-已删除',
    UNIQUE KEY uk_user_target (user_id, target_type, target_id),
    INDEX idx_user_id (user_id),
    INDEX idx_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';