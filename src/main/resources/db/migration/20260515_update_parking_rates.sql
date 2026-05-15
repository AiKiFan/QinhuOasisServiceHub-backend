-- 更新停车区域费率
-- A区免费，B区10元/时，残障区免费，新能源区5元/时

USE qinhu_oasis;

UPDATE parking_space SET hourly_rate = 0.00 WHERE zone_code = 'ZONE_A';
UPDATE parking_space SET hourly_rate = 10.00 WHERE zone_code = 'ZONE_B';
UPDATE parking_space SET hourly_rate = 0.00 WHERE zone_code = 'ZONE_DIS';
UPDATE parking_space SET hourly_rate = 5.00 WHERE zone_code = 'ZONE_EV';
