-- 创建车位明细表并初始化280个车位数据
-- A区100个，B区100个，残障区30个，新能源区50个

USE qinhu_oasis;

-- 创建车位明细表
CREATE TABLE IF NOT EXISTS parking_spot (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '车位ID',
    zone_id         BIGINT UNSIGNED NOT NULL COMMENT '所属区域ID',
    spot_code       VARCHAR(20) NOT NULL COMMENT '车位编号(如:A-01)',
    status          TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态: 0=空闲 1=已占用',
    vehicle_no      VARCHAR(20) COMMENT '车牌号',
    user_id         BIGINT UNSIGNED COMMENT '预约用户ID',
    order_id        BIGINT UNSIGNED COMMENT '关联订单ID',
    start_time      DATETIME COMMENT '入场时间',
    planned_end_time DATETIME COMMENT '预计离场时间(废弃字段)',
    actual_end_time DATETIME COMMENT '实际离场时间',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_zone_id (zone_id),
    INDEX idx_status (status),
    INDEX idx_user_id (user_id),
    INDEX idx_spot_code (spot_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='车位明细表';

-- 初始化A区100个车位 (zone_id=1)
INSERT INTO parking_spot (zone_id, spot_code, status)
SELECT 1, CONCAT('A-', LPAD(seq, 2, '0')), 0
FROM (
    SELECT @rownum := @rownum + 1 AS seq
    FROM (SELECT @rownum := 0) r, sys_user LIMIT 100
) t;

-- 初始化B区100个车位 (zone_id=2)
INSERT INTO parking_spot (zone_id, spot_code, status)
SELECT 2, CONCAT('B-', LPAD(seq, 2, '0')), 0
FROM (
    SELECT @rownum := @rownum + 1 AS seq
    FROM (SELECT @rownum := 0) r, sys_user LIMIT 100
) t;

-- 初始化残障区30个车位 (zone_id=3)
INSERT INTO parking_spot (zone_id, spot_code, status)
SELECT 3, CONCAT('D-', LPAD(seq, 2, '0')), 0
FROM (
    SELECT @rownum := @rownum + 1 AS seq
    FROM (SELECT @rownum := 0) r, sys_user LIMIT 30
) t;

-- 初始化新能源区50个车位 (zone_id=4)
INSERT INTO parking_spot (zone_id, spot_code, status)
SELECT 4, CONCAT('E-', LPAD(seq, 2, '0')), 0
FROM (
    SELECT @rownum := @rownum + 1 AS seq
    FROM (SELECT @rownum := 0) r, sys_user LIMIT 50
) t;
