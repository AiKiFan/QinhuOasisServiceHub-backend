-- ================================================================
-- 沁湖驿站云服务平台 - 数据库迁移脚本
-- 功能: 为 biz_order 表添加 phone 字段（游客预约时填写联系电话）
-- 版本: 20260508_add_order_phone.sql
-- 创建时间: 2026-05-08
-- ================================================================

USE qinhu_oasis;

-- 添加 phone 字段到 biz_order 表（游客预约翻译服务时可填写联系电话）
ALTER TABLE biz_order
ADD COLUMN phone VARCHAR(20) COMMENT '联系电话（游客预约时填写）' AFTER remark;