/*
 Navicat Premium Data Transfer

 Source Server         : local
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : localhost:3306
 Source Schema         : qinhu_oasis

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 19/05/2026 13:26:01
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for biz_comment
-- ----------------------------
DROP TABLE IF EXISTS `biz_comment`;
CREATE TABLE `biz_comment`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '评论用户ID(sys_user.id)',
  `target_id` bigint UNSIGNED NOT NULL COMMENT '被评对象ID',
  `target_type` tinyint(1) NOT NULL COMMENT '目标类型: 1-餐厅 2-攻略 3-译员订单 4-车位订单',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `rating` tinyint(1) NULL DEFAULT NULL COMMENT '评分(1-5星,仅服务/餐厅评价有效)',
  `images` json NULL COMMENT '图片列表(Minio URL数组,最多3张)',
  `like_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数',
  `parent_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '父评论ID(NULL代表一级评论)',
  `reply_to_user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '回复目标用户ID',
  `order_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '关联订单ID(biz_order.id,服务评价时使用)',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态: 0-已屏蔽 1-正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '软删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_target`(`target_id` ASC, `target_type` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评价/评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_order
-- ----------------------------
DROP TABLE IF EXISTS `biz_order`;
CREATE TABLE `biz_order`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务订单号(雪花算法生成)',
  `order_type` tinyint(1) NOT NULL COMMENT '订单类型: 1-翻译服务 2-车位预约',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '下单用户ID(sys_user.id)',
  `interpreter_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '接单译员用户ID(翻译订单)',
  `service_type` tinyint(1) NULL DEFAULT NULL COMMENT '服务类型: 1-个人 2-团队(翻译订单)',
  `group_size` smallint UNSIGNED NULL DEFAULT 1 COMMENT '团队人数(翻译订单,默认1)',
  `parking_space_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '停车区域ID(车位订单)',
  `vehicle_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '车牌号(车位订单)',
  `start_time` datetime NOT NULL COMMENT '服务/预约开始时间',
  `end_time` datetime NOT NULL COMMENT '服务/预约结束时间',
  `total_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '订单应付金额(元)',
  `paid_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '实际支付金额(元)',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态: 0-待接单/待支付 1-已接单/已支付 2-进行中 3-已完成 4-已取消 5-退款中 6-已退款',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户备注',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '鑱旂郴鐢佃瘽锛堟父瀹㈤?绾︽椂濉?啓锛',
  `cancel_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '取消原因',
  `cancelled_by` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '取消方标识：user=游客取消 interpreter=译员取消',
  `is_commented` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已评价: 0-否 1-是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '软删除: 0-正常 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_interpreter_id`(`interpreter_id` ASC) USING BTREE,
  INDEX `idx_parking_space_id`(`parking_space_id` ASC) USING BTREE,
  INDEX `idx_order_type_status`(`order_type` ASC, `status` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 53 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '统一业务订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_restaurant
-- ----------------------------
DROP TABLE IF EXISTS `biz_restaurant`;
CREATE TABLE `biz_restaurant`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '餐厅ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '餐厅名称(中文)',
  `name_en` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '餐厅名称(英文)',
  `category` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '其他' COMMENT '分类: 中餐/西餐/小吃/快餐/甜品/其他',
  `cover_img` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图URL(Minio: ugc-images bucket)',
  `images` json NULL COMMENT '图片列表(Minio URL JSON数组)',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地址描述',
  `lat` decimal(10, 7) NULL DEFAULT NULL COMMENT '纬度(高德GCJ-02坐标)',
  `lng` decimal(10, 7) NULL DEFAULT NULL COMMENT '经度(高德GCJ-02坐标)',
  `avg_price` decimal(8, 2) NOT NULL DEFAULT 0.00 COMMENT '人均消费(元)',
  `phone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `business_hours` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '营业时间(如: 10:00-21:00)',
  `tags` json NULL COMMENT '标签列表JSON(如: [\"网红打卡\",\"外卖可送\"])',
  `rating` decimal(3, 2) NOT NULL DEFAULT 5.00 COMMENT '综合评分(1.00-5.00)',
  `review_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '评价总数',
  `sort_score` double NOT NULL DEFAULT 0 COMMENT '热度排行综合分(同步至Redis ZSet)',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态: 0-暂停营业 1-正常营业',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '软删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_rating`(`rating` ASC) USING BTREE,
  INDEX `idx_sort_score`(`sort_score` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_lat_lng`(`lat` ASC, `lng` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '餐厅表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_scenic_spot
-- ----------------------------
DROP TABLE IF EXISTS `biz_scenic_spot`;
CREATE TABLE `biz_scenic_spot`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鏅?偣ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鏅?偣鍚嶇О锛堜腑鏂囷級',
  `name_en` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鏅?偣鍚嶇О锛堣嫳鏂囷級',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '鏅?偣鎻忚堪锛堜腑鏂囷級',
  `description_en` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '鏅?偣鎻忚堪锛堣嫳鏂囷級',
  `cover_img` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '灏侀潰鍥綰RL锛圡inIO: ugc-images bucket锛',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '鍥剧墖鍒楄〃锛圝SON鏁扮粍锛',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鍦板潃鎻忚堪',
  `lat` decimal(10, 7) NULL DEFAULT NULL COMMENT '绾?害锛堥珮寰稧CJ-02鍧愭爣锛',
  `lng` decimal(10, 7) NULL DEFAULT NULL COMMENT '缁忓害锛堥珮寰稧CJ-02鍧愭爣锛',
  `opening_hours` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '寮?斁鏃堕棿',
  `ticket_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '闂ㄧエ浠锋牸',
  `rating` decimal(3, 2) NULL DEFAULT 5.00 COMMENT '璇勫垎锛?.00-5.00锛',
  `review_count` int NULL DEFAULT 0 COMMENT '璇勪环鎬绘暟',
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鏍囩?鍒楄〃锛圝SON鏁扮粍锛',
  `sort_score` double NULL DEFAULT 0 COMMENT '鐑?害鎺掕?缁煎悎鍒',
  `status` int NULL DEFAULT 1 COMMENT '鐘舵?锛?-鏆傚仠寮?斁 1-姝ｅ父寮?斁',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  `deleted` int NULL DEFAULT 0 COMMENT '杞?垹闄ゆ爣璁帮細0-姝ｅ父 1-宸插垹闄',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status_deleted`(`status` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_sort_score`(`sort_score` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鏅?偣淇℃伅琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for biz_user_favorite
-- ----------------------------
DROP TABLE IF EXISTS `biz_user_favorite`;
CREATE TABLE `biz_user_favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `target_id` bigint NOT NULL,
  `folder_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` int NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_target`(`user_id` ASC, `target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 45 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for interpreter_profile
-- ----------------------------
DROP TABLE IF EXISTS `interpreter_profile`;
CREATE TABLE `interpreter_profile`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '档案ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '关联用户ID(sys_user.id)',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '真实姓名',
  `student_id` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学号',
  `school` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所在院校',
  `english_level` tinyint(1) NOT NULL DEFAULT 0 COMMENT '英语水平: 0-CET4 1-CET6 2-TEM4 3-TEM8 4-其他',
  `cert_url` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `cert_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '证书编号',
  `introduction` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '中文自我介绍',
  `introduction_en` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '英文自我介绍',
  `service_types` tinyint(1) NOT NULL DEFAULT 3 COMMENT '服务类型(位运算): 1-仅个人 2-仅团队 3-均可',
  `hourly_rate` decimal(8, 2) NOT NULL DEFAULT 0.00 COMMENT '服务时薪(元/小时)',
  `rating` decimal(3, 2) NOT NULL DEFAULT 5.00 COMMENT '综合评分(1.00-5.00)',
  `total_orders` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '历史总接单数',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '审核状态: 0-待审核 1-已通过 2-已拒绝 3-暂停接单',
  `reject_reason` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核拒绝原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_english_level`(`english_level` ASC) USING BTREE,
  INDEX `idx_rating`(`rating` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '译员档案表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for parking_space
-- ----------------------------
DROP TABLE IF EXISTS `parking_space`;
CREATE TABLE `parking_space`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '区域ID',
  `zone_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区域名称(中文, 如:A区停车场)',
  `zone_name_en` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '区域名称(英文)',
  `zone_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区域编码(如:ZONE_A)',
  `space_type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '类型: 0-普通 1-残障专用 2-新能源充电',
  `total_capacity` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '总车位数量',
  `available_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '当前可用数(Redis镜像,仅展示用)',
  `location_desc` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '位置描述',
  `hourly_rate` decimal(6, 2) NOT NULL DEFAULT 5.00 COMMENT '停车费率(元/小时)',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态: 0-关闭 1-开放 2-维护中',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_zone_code`(`zone_code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_space_type`(`space_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '车位区域表(区域级库存)' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for parking_spot
-- ----------------------------
DROP TABLE IF EXISTS `parking_spot`;
CREATE TABLE `parking_spot`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '杞︿綅ID',
  `zone_id` bigint UNSIGNED NOT NULL COMMENT '鎵?睘鍖哄煙ID(parking_space.id)',
  `spot_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '杞︿綅缂栧彿',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '鐘舵?: 0-绌洪棽 1-宸插崰鐢?2-瓒呮椂',
  `charger_type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '充电桩类型: 0=普通 1=快充 2=慢充',
  `vehicle_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '杞︾墝鍙?鍗犵敤鏃?',
  `user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '棰勭害鐢ㄦ埛ID',
  `order_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '鍏宠仈璁㈠崟ID',
  `start_time` datetime NULL DEFAULT NULL COMMENT '鍏ュ満鏃堕棿',
  `planned_end_time` datetime NULL DEFAULT NULL COMMENT '棰勮?绂诲満鏃堕棿',
  `actual_end_time` datetime NULL DEFAULT NULL COMMENT '瀹為檯绂诲満鏃堕棿',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_zone_spot`(`zone_id` ASC, `spot_code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_zone_id`(`zone_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 452 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '车位表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_feedback
-- ----------------------------
DROP TABLE IF EXISTS `sys_feedback`;
CREATE TABLE `sys_feedback`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '提交用户ID(NULL代表匿名)',
  `feedback_type` tinyint(1) NOT NULL COMMENT '类型: 1-投诉 2-建议 3-咨询 4-其他',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '反馈主题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '详细描述',
  `images` json NULL COMMENT '图片附件(Minio URL数组)',
  `contact` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系方式(手机/邮箱,匿名可填)',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '处理状态: 0-待处理 1-处理中 2-已解决 3-已关闭',
  `reply_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '管理员回复内容',
  `reply_time` datetime NULL DEFAULT NULL COMMENT '回复时间',
  `handler_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '处理人管理员ID(sys_user.id)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_feedback_type`(`feedback_type` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '投诉建议表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码(BCrypt加密)',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL(Minio: user-avatars bucket)',
  `role` tinyint(1) NOT NULL DEFAULT 0 COMMENT '角色: 0-游客 1-学生译员 2-管理员',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '账号状态: 0-禁用 1-正常',
  `locale` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'zh_CN' COMMENT '语言偏好: zh_CN/en_US',
  `last_login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '软删除: 0-正常 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ugc_like
-- ----------------------------
DROP TABLE IF EXISTS `ugc_like`;
CREATE TABLE `ugc_like`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '点赞用户ID(sys_user.id)',
  `target_id` bigint UNSIGNED NOT NULL COMMENT '被点赞对象ID',
  `target_type` tinyint(1) NOT NULL COMMENT '目标类型: 1-攻略 2-评论',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_target`(`user_id` ASC, `target_id` ASC, `target_type` ASC) USING BTREE,
  INDEX `idx_target`(`target_id` ASC, `target_type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '点赞记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ugc_post
-- ----------------------------
DROP TABLE IF EXISTS `ugc_post`;
CREATE TABLE `ugc_post`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '作者用户ID(sys_user.id)',
  `post_type` tinyint(1) NOT NULL COMMENT '类型: 1-官方攻略 2-游客攻略 3-游客动态',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题(中文)',
  `title_en` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标题(英文)',
  `summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '摘要/副标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '正文(HTML富文本)',
  `cover_img` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图URL(Minio)',
  `images` json NULL COMMENT '附图列表(Minio URL数组,最多9张)',
  `view_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '浏览量',
  `like_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '评论数',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态: 0-草稿 1-已发布 2-审核中 3-已下架',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '软删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_post_type_status`(`post_type` ASC, `status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_like_count`(`like_count` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '攻略/动态表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
