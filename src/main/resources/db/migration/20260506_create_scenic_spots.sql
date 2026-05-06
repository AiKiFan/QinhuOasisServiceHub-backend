-- 景点表创建脚本
-- Migration: 20260506_create_scenic_spots.sql
-- Author: AiKiFan

CREATE TABLE IF NOT EXISTS biz_scenic_spot (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '景点ID',
    name VARCHAR(100) NOT NULL COMMENT '景点名称（中文）',
    name_en VARCHAR(100) COMMENT '景点名称（英文）',
    description TEXT COMMENT '景点描述（中文）',
    description_en TEXT COMMENT '景点描述（英文）',
    cover_img VARCHAR(500) COMMENT '封面图URL（MinIO: ugc-images bucket）',
    images TEXT COMMENT '图片列表（JSON数组）',
    address VARCHAR(200) COMMENT '地址描述',
    lat DECIMAL(10, 7) COMMENT '纬度（高德GCJ-02坐标）',
    lng DECIMAL(10, 7) COMMENT '经度（高德GCJ-02坐标）',
    opening_hours VARCHAR(100) COMMENT '开放时间',
    ticket_price DECIMAL(10, 2) COMMENT '门票价格',
    rating DECIMAL(3, 2) DEFAULT 5.00 COMMENT '评分（1.00-5.00）',
    review_count INT DEFAULT 0 COMMENT '评价总数',
    tags VARCHAR(500) COMMENT '标签列表（JSON数组）',
    sort_score DOUBLE DEFAULT 0 COMMENT '热度排行综合分',
    status INT DEFAULT 1 COMMENT '状态：0-暂停开放 1-正常开放',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '软删除标记：0-正常 1-已删除',
    INDEX idx_status_deleted (status, deleted),
    INDEX idx_sort_score (sort_score DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='景点信息表';

-- 示例景点数据
INSERT INTO biz_scenic_spot (name, name_en, description, description_en, cover_img, address, lat, lng, opening_hours, ticket_price, tags)
VALUES
('明月山主峰', 'Mingyue Peak', '明月山主峰海拔1650米，是宜春市最高峰，山顶常年云雾缭绕', 'Mingyue Peak stands at 1650m, the highest peak in Yichun.', '/api/files/minio/ugc-images/peak.jpg', '宜春市明月山风景区', 28.123456, 114.123456, '08:00-17:00', 120.00, '["登山","云海","日出"]'),
('月亮湖', 'Moon Lake', '月亮湖位于明月山脚下，湖水清澈，周围环境优美', 'Moon Lake at the foot of Mingyue Mountain with crystal clear waters.', '/api/files/minio/ugc-images/lake.jpg', '宜春市明月山风景区', 28.120000, 114.120000, '全天开放', 0.00, '["湖泊","休闲","摄影"]'),
('沁湖驿站', 'Qinhu Station', '沁湖驿站是明月山景区的服务中心', 'Qinhu Station serves as the service center for Mingyue Mountain.', '/api/files/minio/ugc-images/station.jpg', '宜春市明月山风景区', 28.118000, 114.118000, '06:00-22:00', 0.00, '["服务中心","餐饮","译员"]');