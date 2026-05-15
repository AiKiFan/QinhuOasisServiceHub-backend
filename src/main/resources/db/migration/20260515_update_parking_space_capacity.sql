-- 更新停车区域容量配置
-- A区100车位，B区100车位，残障区30车位，新能源区50车位

USE qinhu_oasis;

-- 更新区域容量和可用数
UPDATE parking_space
SET total_capacity = 100, available_count = 100
WHERE zone_code = 'ZONE_A';

UPDATE parking_space
SET total_capacity = 100, available_count = 100
WHERE zone_code = 'ZONE_B';

UPDATE parking_space
SET total_capacity = 30, available_count = 30
WHERE zone_code = 'ZONE_DIS';

UPDATE parking_space
SET total_capacity = 50, available_count = 50,
    location_desc = '景区南侧停车楼B1层，配备充电桩30套（15快充+15慢充）'
WHERE zone_code = 'ZONE_EV';
