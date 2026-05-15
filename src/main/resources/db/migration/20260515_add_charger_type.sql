-- 新增充电桩类型字段，用于区分普通车位、快充、慢充
-- charger_type: 0=普通车位 1=快充桩 2=慢充桩

USE qinhu_oasis;

-- 新增charger_type字段
ALTER TABLE parking_spot
ADD COLUMN charger_type TINYINT(1) NOT NULL DEFAULT 0 COMMENT '充电桩类型: 0=普通 1=快充 2=慢充' AFTER status;

-- 更新新能源区(E区)的车位类型
-- E-01 ~ E-15 为快充桩
UPDATE parking_spot SET charger_type = 1 WHERE zone_id = 4 AND spot_code >= 'E-01' AND spot_code <= 'E-15';

-- E-16 ~ E-30 为慢充桩
UPDATE parking_spot SET charger_type = 2 WHERE zone_id = 4 AND spot_code >= 'E-16' AND spot_code <= 'E-30';

-- E-31 ~ E-50 为普通车位
UPDATE parking_spot SET charger_type = 0 WHERE zone_id = 4 AND spot_code >= 'E-31';

-- 验证更新结果
SELECT zone_id, charger_type, COUNT(*) as count
FROM parking_spot
WHERE zone_id = 4
GROUP BY zone_id, charger_type;
