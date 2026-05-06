-- 餐厅表添加英文名称字段
-- Migration: 20260506_add_restaurant_name_en.sql
-- Author: AiKiFan

ALTER TABLE biz_restaurant
ADD COLUMN name_en VARCHAR(100) COMMENT '餐厅名称（英文，i18n）' AFTER name;

-- 示例数据：为现有餐厅添加英文名称
UPDATE biz_restaurant SET name_en = 'Chinese Restaurant' WHERE category = '中餐' AND name_en IS NULL;
UPDATE biz_restaurant SET name_en = 'Western Restaurant' WHERE category = '西餐' AND name_en IS NULL;
UPDATE biz_restaurant SET name_en = 'Snacks & Fast Food' WHERE category = '小吃' AND name_en IS NULL;
UPDATE biz_restaurant SET name_en = 'Coffee & Desserts' WHERE category = '甜品' AND name_en IS NULL;
UPDATE biz_restaurant SET name_en = 'Other Dining' WHERE category = '其他' AND name_en IS NULL;