-- ================================================================
-- 沁湖驿站云服务平台 (QinhuOasis Service Hub)
-- 数据库初始化脚本
-- 版本: V1.0.0
-- 创建时间: 2026-04-28
-- 字符集: utf8mb4 + utf8mb4_unicode_ci (支持中文/英文/Emoji)
-- MySQL版本要求: 8.0+
-- ================================================================

-- 强制客户端连接字符集为 utf8mb4，防止中文截断
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 创建并切换数据库
CREATE DATABASE IF NOT EXISTS qinhu_oasis
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE qinhu_oasis;

-- 设置外键检查关闭（方便顺序执行建表）
SET FOREIGN_KEY_CHECKS = 0;


-- ================================================================
-- 系统域 (sys_*)
-- ================================================================

-- 用户表
-- 角色: 0=游客(Tourist) 1=学生译员(Student) 2=管理员(Admin)
-- locale 字段用于后端 i18n 响应语言兜底，优先级低于请求 Accept-Language Header
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id              BIGINT UNSIGNED     NOT NULL AUTO_INCREMENT          COMMENT '用户ID',
    username        VARCHAR(50)         NOT NULL                         COMMENT '登录用户名',
    password        VARCHAR(100)        NOT NULL                         COMMENT '密码(BCrypt加密)',
    phone           VARCHAR(20)                                          COMMENT '手机号',
    email           VARCHAR(100)                                         COMMENT '邮箱',
    nickname        VARCHAR(50)                                          COMMENT '昵称',
    avatar          VARCHAR(500)                                         COMMENT '头像URL(Minio: user-avatars bucket)',
    role            TINYINT(1)          NOT NULL DEFAULT 0               COMMENT '角色: 0-游客 1-学生译员 2-管理员',
    status          TINYINT(1)          NOT NULL DEFAULT 1               COMMENT '账号状态: 0-禁用 1-正常',
    locale          VARCHAR(10)         NOT NULL DEFAULT 'zh_CN'         COMMENT '语言偏好: zh_CN/en_US',
    last_login_ip   VARCHAR(50)                                          COMMENT '最后登录IP',
    last_login_time DATETIME                                             COMMENT '最后登录时间',
    create_time     DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT(1)          NOT NULL DEFAULT 0               COMMENT '软删除: 0-正常 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_phone (phone),
    UNIQUE KEY uk_email (email),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';


-- 译员档案表
-- 关联 sys_user(role=1)，需管理员审核通过(status=1)后方可接单
-- cert_url: 英语证书存储于 Minio bucket: interpreter-certs
-- service_types 位运算: bit0=个人服务 bit1=团队服务 → 3=均可
-- rating: 1.00-5.00，由评价系统计算更新
DROP TABLE IF EXISTS interpreter_profile;
CREATE TABLE interpreter_profile (
    id              BIGINT UNSIGNED     NOT NULL AUTO_INCREMENT          COMMENT '档案ID',
    user_id         BIGINT UNSIGNED     NOT NULL                         COMMENT '关联用户ID(sys_user.id)',
    real_name       VARCHAR(50)         NOT NULL                         COMMENT '真实姓名',
    student_id      VARCHAR(30)         NOT NULL                         COMMENT '学号',
    school          VARCHAR(100)                                         COMMENT '所在院校',
    english_level   TINYINT(1)          NOT NULL DEFAULT 0               COMMENT '英语水平: 0-CET4 1-CET6 2-TEM4 3-TEM8 4-其他',
    cert_url        VARCHAR(500)                                         COMMENT '英语证书图片URL(Minio)',
    cert_no         VARCHAR(50)                                          COMMENT '证书编号',
    introduction    TEXT                                                 COMMENT '中文自我介绍',
    introduction_en TEXT                                                 COMMENT '英文自我介绍',
    service_types   TINYINT(1)          NOT NULL DEFAULT 3               COMMENT '服务类型(位运算): 1-仅个人 2-仅团队 3-均可',
    hourly_rate     DECIMAL(8,2)        NOT NULL DEFAULT 0.00            COMMENT '服务时薪(元/小时)',
    rating          DECIMAL(3,2)        NOT NULL DEFAULT 5.00            COMMENT '综合评分(1.00-5.00)',
    total_orders    INT UNSIGNED        NOT NULL DEFAULT 0               COMMENT '历史总接单数',
    status          TINYINT(1)          NOT NULL DEFAULT 0               COMMENT '审核状态: 0-待审核 1-已通过 2-已拒绝 3-暂停接单',
    reject_reason   VARCHAR(300)                                         COMMENT '审核拒绝原因',
    create_time     DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    update_time     DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_english_level (english_level),
    INDEX idx_rating (rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='译员档案表';


-- ================================================================
-- 智慧旅游域 (parking_space / biz_order)
-- ================================================================

-- 车位区域表 (区域级库存)
-- 设计: 每行代表一个停车区域，available_count 是 MySQL 持久化镜像
-- 真实可用库存以 Redis 为准，key 规则: parking:stock:{id}
-- 初始化时将 available_count 同步写入 Redis，预约用 DECR，释放用 INCR
-- Redis 扣减为0后拒绝预约（防止超卖），定时任务每5min对齐 MySQL
DROP TABLE IF EXISTS parking_space;
CREATE TABLE parking_space (
    id              BIGINT UNSIGNED     NOT NULL AUTO_INCREMENT          COMMENT '区域ID',
    zone_name       VARCHAR(50)         NOT NULL                         COMMENT '区域名称(中文, 如:A区停车场)',
    zone_name_en    VARCHAR(100)                                         COMMENT '区域名称(英文)',
    zone_code       VARCHAR(20)         NOT NULL                         COMMENT '区域编码(如:ZONE_A)',
    space_type      TINYINT(1)          NOT NULL DEFAULT 0               COMMENT '类型: 0-普通 1-残障专用 2-新能源充电',
    total_capacity  INT UNSIGNED        NOT NULL DEFAULT 0               COMMENT '总车位数量',
    available_count INT UNSIGNED        NOT NULL DEFAULT 0               COMMENT '当前可用数(Redis镜像,仅展示用)',
    location_desc   VARCHAR(200)                                         COMMENT '位置描述',
    hourly_rate     DECIMAL(6,2)        NOT NULL DEFAULT 5.00            COMMENT '停车费率(元/小时)',
    status          TINYINT(1)          NOT NULL DEFAULT 1               COMMENT '状态: 0-关闭 1-开放 2-维护中',
    create_time     DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_zone_code (zone_code),
    INDEX idx_status (status),
    INDEX idx_space_type (space_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='车位区域表(区域级库存)';


-- 统一业务订单表
-- order_type=1 翻译服务: 使用 interpreter_id, service_type, group_size
-- order_type=2 车位预约: 使用 parking_space_id, vehicle_no
-- order_no 由应用层雪花算法生成(18位数字字符串)
-- 订单状态机: 待接单/待支付(0) → 已接单/已支付(1) → 服务中/使用中(2)
--              → 已完成(3) / 已取消(4) / 退款中(5) → 已退款(6)
DROP TABLE IF EXISTS biz_order;
CREATE TABLE biz_order (
    id                  BIGINT UNSIGNED     NOT NULL AUTO_INCREMENT          COMMENT '订单ID',
    order_no            VARCHAR(30)         NOT NULL                         COMMENT '业务订单号(雪花算法生成)',
    order_type          TINYINT(1)          NOT NULL                         COMMENT '订单类型: 1-翻译服务 2-车位预约',
    user_id             BIGINT UNSIGNED     NOT NULL                         COMMENT '下单用户ID(sys_user.id)',
    -- 翻译服务专用字段
    interpreter_id      BIGINT UNSIGNED                                      COMMENT '接单译员用户ID(翻译订单)',
    service_type        TINYINT(1)                                           COMMENT '服务类型: 1-个人 2-团队(翻译订单)',
    group_size          SMALLINT UNSIGNED   DEFAULT 1                        COMMENT '团队人数(翻译订单,默认1)',
    -- 车位预约专用字段
    parking_space_id    BIGINT UNSIGNED                                      COMMENT '停车区域ID(车位订单)',
    vehicle_no          VARCHAR(20)                                          COMMENT '车牌号(车位订单)',
    -- 公共字段
    start_time          DATETIME            NOT NULL                         COMMENT '服务/预约开始时间',
    end_time            DATETIME            NOT NULL                         COMMENT '服务/预约结束时间',
    total_amount        DECIMAL(10,2)       NOT NULL DEFAULT 0.00            COMMENT '订单应付金额(元)',
    paid_amount         DECIMAL(10,2)       NOT NULL DEFAULT 0.00            COMMENT '实际支付金额(元)',
    status              TINYINT(1)          NOT NULL DEFAULT 0               COMMENT '状态: 0-待接单/待支付 1-已接单/已支付 2-进行中 3-已完成 4-已取消 5-退款中 6-已退款',
    remark              VARCHAR(500)                                         COMMENT '用户备注',
    cancel_reason       VARCHAR(200)                                         COMMENT '取消原因',
    is_commented        TINYINT(1)          NOT NULL DEFAULT 0               COMMENT '是否已评价: 0-否 1-是',
    create_time         DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    update_time         DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT(1)          NOT NULL DEFAULT 0               COMMENT '软删除: 0-正常 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_interpreter_id (interpreter_id),
    INDEX idx_parking_space_id (parking_space_id),
    INDEX idx_order_type_status (order_type, status),
    INDEX idx_start_time (start_time),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统一业务订单表';


-- ================================================================
-- 美食点评域 (biz_restaurant)
-- ================================================================

-- 餐厅表
-- sort_score: 热度排行综合分 = rating*20 + LOG10(review_count+1)*10
-- 每次新增评价后异步更新 sort_score，同步写入 Redis ZSet:
--   key: restaurant:rank  member: {id}  score: sort_score
-- lat/lng 使用高德坐标系(GCJ-02)，配合高德地图SDK展示
DROP TABLE IF EXISTS biz_restaurant;
CREATE TABLE biz_restaurant (
    id              BIGINT UNSIGNED     NOT NULL AUTO_INCREMENT          COMMENT '餐厅ID',
    name            VARCHAR(100)        NOT NULL                         COMMENT '餐厅名称(中文)',
    name_en         VARCHAR(150)                                         COMMENT '餐厅名称(英文)',
    category        VARCHAR(30)         NOT NULL DEFAULT '其他'          COMMENT '分类: 中餐/西餐/小吃/快餐/甜品/其他',
    cover_img       VARCHAR(500)                                         COMMENT '封面图URL(Minio: ugc-images bucket)',
    images          JSON                                                 COMMENT '图片列表(Minio URL JSON数组)',
    address         VARCHAR(200)                                         COMMENT '地址描述',
    lat             DECIMAL(10,7)                                        COMMENT '纬度(高德GCJ-02坐标)',
    lng             DECIMAL(10,7)                                        COMMENT '经度(高德GCJ-02坐标)',
    avg_price       DECIMAL(8,2)        NOT NULL DEFAULT 0.00            COMMENT '人均消费(元)',
    phone           VARCHAR(30)                                          COMMENT '联系电话',
    business_hours  VARCHAR(100)                                         COMMENT '营业时间(如: 10:00-21:00)',
    tags            JSON                                                 COMMENT '标签列表JSON(如: ["网红打卡","外卖可送"])',
    rating          DECIMAL(3,2)        NOT NULL DEFAULT 5.00            COMMENT '综合评分(1.00-5.00)',
    review_count    INT UNSIGNED        NOT NULL DEFAULT 0               COMMENT '评价总数',
    sort_score      DOUBLE              NOT NULL DEFAULT 0               COMMENT '热度排行综合分(同步至Redis ZSet)',
    status          TINYINT(1)          NOT NULL DEFAULT 1               COMMENT '状态: 0-暂停营业 1-正常营业',
    create_time     DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT(1)          NOT NULL DEFAULT 0               COMMENT '软删除',
    PRIMARY KEY (id),
    INDEX idx_category (category),
    INDEX idx_rating (rating),
    INDEX idx_sort_score (sort_score),
    INDEX idx_status (status),
    INDEX idx_lat_lng (lat, lng)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='餐厅表';


-- ================================================================
-- 内容交互域 (ugc_post / biz_comment / ugc_like)
-- ================================================================

-- 攻略/动态表
-- post_type: 1=官方攻略(管理员发布,CMS) 2=游客攻略(UGC审核后发布) 3=游客动态(短内容,无需审核)
-- images 存储 Minio 返回的对象访问URL，最多9张
-- content: HTML富文本，由前端富文本编辑器生成
DROP TABLE IF EXISTS ugc_post;
CREATE TABLE ugc_post (
    id              BIGINT UNSIGNED     NOT NULL AUTO_INCREMENT          COMMENT '帖子ID',
    user_id         BIGINT UNSIGNED     NOT NULL                         COMMENT '作者用户ID(sys_user.id)',
    post_type       TINYINT(1)          NOT NULL                         COMMENT '类型: 1-官方攻略 2-游客攻略 3-游客动态',
    title           VARCHAR(200)        NOT NULL                         COMMENT '标题(中文)',
    title_en        VARCHAR(300)                                         COMMENT '标题(英文)',
    summary         VARCHAR(500)                                         COMMENT '摘要/副标题',
    content         LONGTEXT            NOT NULL                         COMMENT '正文(HTML富文本)',
    cover_img       VARCHAR(500)                                         COMMENT '封面图URL(Minio)',
    images          JSON                                                 COMMENT '附图列表(Minio URL数组,最多9张)',
    view_count      INT UNSIGNED        NOT NULL DEFAULT 0               COMMENT '浏览量',
    like_count      INT UNSIGNED        NOT NULL DEFAULT 0               COMMENT '点赞数',
    comment_count   INT UNSIGNED        NOT NULL DEFAULT 0               COMMENT '评论数',
    status          TINYINT(1)          NOT NULL DEFAULT 1               COMMENT '状态: 0-草稿 1-已发布 2-审核中 3-已下架',
    create_time     DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    update_time     DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT(1)          NOT NULL DEFAULT 0               COMMENT '软删除',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_post_type_status (post_type, status),
    INDEX idx_create_time (create_time),
    INDEX idx_like_count (like_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='攻略/动态表';


-- 评价/评论表
-- 多态设计，target_type 区分被评价对象:
--   1=餐厅(biz_restaurant.id) 2=攻略(ugc_post.id)
--   3=译员订单(biz_order.id,order_type=1) 4=车位订单(biz_order.id,order_type=2)
-- parent_id 支持一级回复(楼中楼)，最多2层不递归
-- rating 仅 target_type=1/3/4 时有效（对餐厅/服务的评分）
-- order_id 关联订单确保"一单一评"
DROP TABLE IF EXISTS biz_comment;
CREATE TABLE biz_comment (
    id                  BIGINT UNSIGNED     NOT NULL AUTO_INCREMENT          COMMENT '评论ID',
    user_id             BIGINT UNSIGNED     NOT NULL                         COMMENT '评论用户ID(sys_user.id)',
    target_id           BIGINT UNSIGNED     NOT NULL                         COMMENT '被评对象ID',
    target_type         TINYINT(1)          NOT NULL                         COMMENT '目标类型: 1-餐厅 2-攻略 3-译员订单 4-车位订单',
    content             TEXT                NOT NULL                         COMMENT '评论内容',
    rating              TINYINT(1)                                           COMMENT '评分(1-5星,仅服务/餐厅评价有效)',
    images              JSON                                                 COMMENT '图片列表(Minio URL数组,最多3张)',
    like_count          INT UNSIGNED        NOT NULL DEFAULT 0               COMMENT '点赞数',
    parent_id           BIGINT UNSIGNED                                      COMMENT '父评论ID(NULL代表一级评论)',
    reply_to_user_id    BIGINT UNSIGNED                                      COMMENT '回复目标用户ID',
    order_id            BIGINT UNSIGNED                                      COMMENT '关联订单ID(biz_order.id,服务评价时使用)',
    status              TINYINT(1)          NOT NULL DEFAULT 1               COMMENT '状态: 0-已屏蔽 1-正常',
    create_time         DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    update_time         DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT(1)          NOT NULL DEFAULT 0               COMMENT '软删除',
    PRIMARY KEY (id),
    INDEX idx_target (target_id, target_type),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_order_id (order_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价/评论表';


-- 点赞记录表
-- UNIQUE KEY 防止重复点赞
-- target_type: 1=攻略(ugc_post) 2=评论(biz_comment)
-- 点赞后需同步 +1 对应表的 like_count 字段(建议用 UPDATE...+1 原子操作)
DROP TABLE IF EXISTS ugc_like;
CREATE TABLE ugc_like (
    id          BIGINT UNSIGNED     NOT NULL AUTO_INCREMENT          COMMENT '记录ID',
    user_id     BIGINT UNSIGNED     NOT NULL                         COMMENT '点赞用户ID(sys_user.id)',
    target_id   BIGINT UNSIGNED     NOT NULL                         COMMENT '被点赞对象ID',
    target_type TINYINT(1)          NOT NULL                         COMMENT '目标类型: 1-攻略 2-评论',
    create_time DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_target (user_id, target_id, target_type),
    INDEX idx_target (target_id, target_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞记录表';


-- ================================================================
-- 综合治理域 (sys_feedback)
-- ================================================================

-- 投诉建议表
-- 支持匿名提交（user_id 可为 NULL）
-- 处理状态流转: 待处理(0) → 处理中(1) → 已解决(2) | 已关闭(3)
-- handler_id 为处理该反馈的管理员用户ID
DROP TABLE IF EXISTS sys_feedback;
CREATE TABLE sys_feedback (
    id              BIGINT UNSIGNED     NOT NULL AUTO_INCREMENT          COMMENT '反馈ID',
    user_id         BIGINT UNSIGNED                                      COMMENT '提交用户ID(NULL代表匿名)',
    feedback_type   TINYINT(1)          NOT NULL                         COMMENT '类型: 1-投诉 2-建议 3-咨询 4-其他',
    title           VARCHAR(200)        NOT NULL                         COMMENT '反馈主题',
    content         TEXT                NOT NULL                         COMMENT '详细描述',
    images          JSON                                                 COMMENT '图片附件(Minio URL数组)',
    contact         VARCHAR(100)                                         COMMENT '联系方式(手机/邮箱,匿名可填)',
    status          TINYINT(1)          NOT NULL DEFAULT 0               COMMENT '处理状态: 0-待处理 1-处理中 2-已解决 3-已关闭',
    reply_content   TEXT                                                 COMMENT '管理员回复内容',
    reply_time      DATETIME                                             COMMENT '回复时间',
    handler_id      BIGINT UNSIGNED                                      COMMENT '处理人管理员ID(sys_user.id)',
    create_time     DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    update_time     DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_feedback_type (feedback_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投诉建议表';


-- ================================================================
-- 恢复外键检查
-- ================================================================
SET FOREIGN_KEY_CHECKS = 1;


-- ================================================================
-- 初始化数据
-- ================================================================

-- 初始管理员账号
-- 原始密码: Admin@123456
-- BCrypt加密后(12轮): $2a$12$gNTnKaQbKQLVBOOA3Y.Fgu.kX6g7hZLgqXl5a.mJWaP0l/LRzMrGi
INSERT INTO sys_user (username, password, nickname, role, status, locale)
VALUES ('admin', '$2a$12$gNTnKaQbKQLVBOOA3Y.Fgu.kX6g7hZLgqXl5a.mJWaP0l/LRzMrGi', '系统管理员', 2, 1, 'zh_CN');

-- 车位区域初始化数据
INSERT INTO parking_space (zone_name, zone_name_en, zone_code, space_type, total_capacity, available_count, location_desc, hourly_rate, status) VALUES
('A区停车场',   'Parking Zone A',     'ZONE_A',   0, 100, 100, '景区东门入口附近，步行至大门约2分钟', 5.00, 1),
('B区停车场',   'Parking Zone B',     'ZONE_B',   0, 80,  80,  '景区西门入口附近，步行至大门约3分钟', 5.00, 1),
('残障专用区',  'Disabled Parking',   'ZONE_DIS', 1, 20,  20,  '景区主入口旁，紧邻无障碍通道',         0.00, 1),
('新能源充电区', 'EV Charging Zone',  'ZONE_EV',  2, 30,  30,  '景区南侧停车楼B1层，配备充电桩30套',  5.00, 1);

-- 示例餐厅数据
INSERT INTO biz_restaurant (name, name_en, category, address, avg_price, business_hours, rating, review_count, sort_score, status) VALUES
('沁湖渔港',   'Qinhu Fishing Harbor', '中餐', '沁湖景区核心区A栋一楼',  88.00, '10:30-21:00', 4.80, 256, 96.00 + 10 * LOG10(257), 1),
('湖畔茶室',   'Lakeside Tea House',   '甜品', '沁湖景区观景台旁',       35.00, '09:00-20:00', 4.60, 128, 92.00 + 10 * LOG10(129), 1),
('驿站快餐',   'Station Fast Food',    '快餐', '景区南门服务中心一楼',   25.00, '08:00-19:00', 4.20, 89,  84.00 + 10 * LOG10(90),  1);
