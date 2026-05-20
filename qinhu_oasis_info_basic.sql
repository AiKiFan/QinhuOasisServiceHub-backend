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

 Date: 19/05/2026 13:14:13
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
-- Records of biz_comment
-- ----------------------------
INSERT INTO `biz_comment` VALUES (2, 1, 6, 5, '非常好,打cs很厉害', 3, NULL, 0, NULL, NULL, NULL, 1, '2026-05-08 14:44:04', '2026-05-14 20:26:32', 1);
INSERT INTO `biz_comment` VALUES (3, 1, 6, 5, '哈哈哈', 5, NULL, 0, NULL, NULL, NULL, 1, '2026-05-08 14:44:15', '2026-05-14 20:26:32', 1);
INSERT INTO `biz_comment` VALUES (4, 1, 6, 5, '就是不给我发刀', 1, NULL, 0, NULL, NULL, NULL, 1, '2026-05-08 14:44:32', '2026-05-14 20:26:32', 1);
INSERT INTO `biz_comment` VALUES (10, 1, 4, 1, '不错,我喜欢', 1, NULL, 0, NULL, NULL, NULL, 1, '2026-05-09 08:16:59', '2026-05-14 20:19:31', 1);
INSERT INTO `biz_comment` VALUES (18, 16, 3, 1, '难吃要的私', 5, NULL, 0, NULL, NULL, NULL, 1, '2026-05-11 20:00:21', '2026-05-14 20:19:31', 1);
INSERT INTO `biz_comment` VALUES (20, 1, 2, 1, '?', 3, NULL, 0, NULL, NULL, NULL, 1, '2026-05-14 20:10:55', '2026-05-14 20:19:31', 1);
INSERT INTO `biz_comment` VALUES (21, 1, 3, 1, '测试', 5, NULL, 0, NULL, NULL, NULL, 1, '2026-05-14 20:13:39', '2026-05-14 20:19:31', 1);

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
-- Records of biz_order
-- ----------------------------
INSERT INTO `biz_order` VALUES (5, '2052641394442637312', 1, 1, 3, 1, 1, NULL, NULL, '2026-05-11 02:30:00', '2026-05-12 02:00:00', 11500.00, 0.00, 4, '测试', NULL, '管理员怎么来约我了,换个号再来', 'interpreter', 0, '2026-05-08 14:46:59', '2026-05-14 20:26:32', 1);
INSERT INTO `biz_order` VALUES (48, '2055190590483730432', 2, 1, NULL, NULL, 1, 2, NULL, '2026-05-15 15:36:35', '2026-05-15 15:36:35', 0.00, 80.00, 3, NULL, NULL, '超时未入场，系统自动取消', 'system', 0, '2026-05-15 15:36:35', '2026-05-16 22:33:52', 0);

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
-- Records of biz_restaurant
-- ----------------------------
INSERT INTO `biz_restaurant` VALUES (1, '茶油饭庄', 'Camellia Oil Restaurant', '中餐', NULL, NULL, '袁州区明月山国家级旅游度假区西北门西北140米', 27.6032620, 114.2613770, 38.00, '13170853819', '07:00-20:00', NULL, 4.20, 0, 156.09933123331294, 1, '2026-04-28 17:07:23', '2026-05-16 22:33:52', 0);
INSERT INTO `biz_restaurant` VALUES (2, '明月山·竹影咖啡', 'Mingyue Mountain · Bamboo Glow Café', '咖啡 甜品', NULL, NULL, '袁州区竹林月影南侧', 27.6028000, 114.2609000, 31.00, NULL, '08:30-17:30', NULL, 2.00, 0, 112.10589710299249, 1, '2026-04-28 17:07:23', '2026-05-16 22:33:52', 0);
INSERT INTO `biz_restaurant` VALUES (3, '门楼土菜馆(明月山店)', 'Menlou Local Cuisine Restaurant', '中餐', NULL, NULL, '袁州区宜春市明月山国家级风景名胜区内', 27.6037350, 114.2605920, 46.00, '15879537409', '09:00-21:00', NULL, 5.00, 0, 124.54242509439325, 1, '2026-04-28 17:07:23', '2026-05-16 22:33:52', 0);
INSERT INTO `biz_restaurant` VALUES (4, '瀚德·山月集coffee', 'Hande · Shanyueji Coffee', '咖啡', NULL, NULL, '宜春市袁州区瀚德山居', 27.6021890, 114.2618220, 28.00, NULL, '09:00-21:00', NULL, 4.40, 0, 186.47525252892726, 1, '2026-05-06 12:27:01', '2026-05-16 22:33:52', 0);

-- ----------------------------
-- Table structure for biz_scenic_spot
-- ----------------------------
DROP TABLE IF EXISTS `biz_scenic_spot`;
CREATE TABLE `biz_scenic_spot`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '景点ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '景点名称（中文）',
  `name_en` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '景点名称（英文）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '景点描述（中文）',
  `description_en` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '景点描述（英文）',
  `cover_img` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图URL（MinIO: ugc-images bucket）',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '图片列表（JSON数组）',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址描述',
  `lat` decimal(10, 7) NULL DEFAULT NULL COMMENT '纬度（高德GCJ-02坐标）',
  `lng` decimal(10, 7) NULL DEFAULT NULL COMMENT '经度（高德GCJ-02坐标）',
  `opening_hours` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '开放时间',
  `ticket_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '门票价格',
  `rating` decimal(3, 2) NULL DEFAULT 5.00 COMMENT '评分（1.00-5.00）',
  `review_count` int NULL DEFAULT 0 COMMENT '评价总数',
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签列表（JSON数组）',
  `sort_score` double NULL DEFAULT 0 COMMENT '热度排序综合分',
  `status` int NULL DEFAULT 1 COMMENT '状态：0-暂停开放 1-正常开放',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NULL DEFAULT 0 COMMENT '软删除标记：0-正常 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status_deleted`(`status` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_sort_score`(`sort_score` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '景点信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_scenic_spot
-- ----------------------------
INSERT INTO `biz_scenic_spot` VALUES (1, '宜春明月山国家级风景名胜区', 'Yichun Mingyue Mountain National Scenic Area', '明月山为国家 5A 级景区、国家级风景名胜区，由 12 座千米以上山峰组成，主峰太平山海拔 1736 米。景区融山、石、林、泉、瀑、湖、竹海为一体，集雄、奇、幽、险、秀于一身，以月亮文化、禅宗文化与富硒温泉为特色，是生态游览、休闲度假的胜地。', 'As a national 5A-level tourist attraction and national scenic area, Mingyue Mountain consists of 12 peaks over 1,000 meters, with the main peak Taiping Mountain at 1,736 meters. Integrating mountains, rocks, forests, springs, waterfalls, lakes and bamboo seas, it features moon culture, Zen culture and selenium-rich hot springs, serving as a resort for ecological tourism and leisure vacation.', NULL, NULL, '宜春市袁州区温汤镇潭下村', 27.6185000, 114.2893000, '07:30-16:30', 0.00, 4.70, 0, '[\"5A 景区\",\"山岳风光\",\"温泉\",\"月亮文化\",\"禅宗文化\",\"休闲度假\"]\n\n\n\n\n\n\n\n参考 11 篇资料\n', 0, 1, '2026-05-06 12:13:29', '2026-05-16 22:33:52', 0);
INSERT INTO `biz_scenic_spot` VALUES (2, '云姑沐月', 'Yungu Muyue Sculpture', '云姑沐月为明月山标志性雕塑景观，位于明月广场中心，以南宋孝宗皇后夏云姑（小名明月）为原型打造，雕塑呈现云姑沐浴月光的优美姿态，承载着明月山的月亮文化与历史传说，是景区人文打卡地标。', 'Yungu Muyue is a iconic sculpture landscape of Mingyue Mountain, located in the center of Mingyue Square. It is modeled after Xia Yungu (childhood name Mingyue), Empress Xiaocheng of Emperor Xiaozong of the Southern Song Dynasty. The sculpture shows Yungu\'s graceful posture bathing in moonlight, embodying the moon culture and historical legends of Mingyue Mountain, and is a cultural check-in landmark in the scenic area.', NULL, NULL, '宜春市明月山国家级风景名胜区内', 27.6215000, 114.2948000, '07:30-16:30', 0.00, 3.20, 0, '[\"雕塑\",\"人文景观\",\"月亮文化\",\"打卡\"]', 0, 1, '2026-05-06 12:13:29', '2026-05-16 22:33:52', 0);
INSERT INTO `biz_scenic_spot` VALUES (3, '云谷飞瀑', 'Yungu Waterfall', '云谷飞瀑是明月山标志性瀑布景观，为景区五级瀑布群之首，瀑布落差宏伟，山间林木葱郁，常年云雾缭绕，山水相融景致秀丽，是明月山极具代表性的自然观光打卡点。', 'Yungu Waterfall is a landmark waterfall landscape of Mingyue Mountain and the first of the five-level waterfall group in the scenic area. It has a magnificent drop, surrounded by lush forests and perennial mist. The integration of mountains and waters presents beautiful scenery, making it a representative natural sightseeing spot in Mingyue Mountain.', NULL, NULL, '宜春市明月山国家级风景名胜区内', 27.6241000, 114.2972000, '07:30-16:30', 0.00, 3.90, 0, '[\"瀑布\",\"自然景观\",\"登山\",\"摄影\",\"休闲打卡\"]', 0, 1, '2026-05-06 12:13:29', '2026-05-16 22:33:52', 0);
INSERT INTO `biz_scenic_spot` VALUES (4, '明月山索道下站', 'Mingyue Mountain Cable Car Lower Station', '明月山索道下站是明月山索道的起点站，位于景区山脚，为游客提供上山缆车服务。索道全长约 2.5 公里，途经中站直达山顶，可快速登顶并俯瞰峡谷竹海、云雾山峦等美景，是省力登山的关键交通节点携程。', 'Mingyue Mountain Cable Car Lower Station is the starting point of the Mingyue Mountain Cable Car, located at the foot of the scenic area, providing tourists with uphill cable car services. The cable car is about 2.5 kilometers long, passing through the middle station to the top of the mountain, allowing visitors to quickly reach the summit and enjoy beautiful scenery such as canyons, bamboo seas and misty mountains. It is a key transportation node for labor-saving mountain climbing携程。', NULL, NULL, '宜春市明月山国家级风景名胜区内', 27.6208000, 114.2915000, '08:00-17:30', NULL, 4.60, 0, '[\"索道站\",\"登山交通\",\"观光缆车\",\"峡谷景观\"]', 0, 1, '2026-05-09 21:12:50', '2026-05-09 21:40:58', 0);
INSERT INTO `biz_scenic_spot` VALUES (5, '明月山缆车中站', 'Mingyue Mountain Cable Car Middle Station', '明月山缆车中站位于索道中段，是衔接下站与上站的重要枢纽，周边紧邻云谷飞瀑、狮子峰等瀑布群景观。可在此站上下缆车，游览瀑布后凭当日缆车票二次乘坐，兼具交通换乘与观景功能。', 'Mingyue Mountain Cable Car Middle Station is located in the middle section of the cable car, serving as a key hub connecting the lower and upper stations. It is close to waterfall group landscapes such as Yungu Waterfall and Lion Peak. Visitors can get on and off the cable car here, and reboard with a same-day ticket after visiting the waterfalls, integrating transportation transfer and sightseeing.', NULL, NULL, '宜春市明月山国家级风景名胜区索道东南侧', 27.6263000, 114.2947000, '08:00-17:30', NULL, 4.50, 0, '[\"索道站\",\"交通枢纽\",\"瀑布景观\",\"二次乘坐\"]', 0, 1, '2026-05-09 21:13:02', '2026-05-09 21:44:09', 0);
INSERT INTO `biz_scenic_spot` VALUES (6, '明月山索道上站', 'Mingyue Mountain Cable Car Upper Station', '明月山索道上站位于明月山山顶区域，是索道的终点站，海拔约 1530 米。出站即达青云栈道、月亮湖等核心景点，可俯瞰群山云海、竹海峡谷，是登顶观光、打卡高山美景的重要节点携程。', 'Mingyue Mountain Cable Car Upper Station is located at the top of Mingyue Mountain, the terminal of the cable car at an altitude of about 1530 meters. Exiting the station leads directly to core attractions such as Qingyun Plank Road and Moon Lake, offering panoramic views of mountains, sea of clouds, bamboo seas and canyons. It is an important spot for summit sightseeing and enjoying alpine scenery携程.', NULL, NULL, '宜春市明月山国家级风景名胜区内', 27.6342000, 114.2987000, '08:00-17:30', NULL, 4.70, 0, '[\"索道站\",\"山顶观光\",\"云海景观\",\"青云栈道\"]', 0, 1, '2026-05-09 21:13:33', '2026-05-09 21:42:27', 0);
INSERT INTO `biz_scenic_spot` VALUES (7, '晃月桥', 'Huangyue Bridge', '晃月桥是明月山月亮情之旅标志性悬索桥，人行桥上月影随桥晃动而得名，全长 66.8 米，紧邻溪流与瀑布群，是赏月、观瀑、情侣打卡的浪漫节点。', 'Huangyue Bridge is an iconic suspension bridge on Mingyue Mountain’s Moon Love Tour. The moon shadow sways with the bridge when walking on it, hence the name. It is 66.8 meters long, adjacent to streams and waterfall groups, a romantic spot for moon watching, waterfall viewing and couple check-in.', NULL, NULL, '宜春明月山国家级风景名胜区 (东南角)', 27.6228000, 114.2961000, '07:30-16:30', NULL, 4.30, 0, '[\"悬索桥\",\"月亮文化\",\"自然风光\",\"摄影\",\"情侣打卡\"]', 0, 1, '2026-05-09 21:48:13', '2026-05-09 21:48:13', 0);
INSERT INTO `biz_scenic_spot` VALUES (8, '抱月亭', 'Baoyue Pavilion', '抱月亭为明月山爱情之旅第六站，悬空六角亭，可赏山泉流水、竹林风影，意境恬淡浪漫，寓意相恋相守，是休憩与观景的人文小景。', 'Baoyue Pavilion is the sixth stop of Mingyue Mountain’s Love Tour. It is a suspended hexagonal pavilion with mountain springs, flowing water and bamboo forest shadows, quiet and romantic, symbolizing love and companionship, a cultural spot for rest and sightseeing.', NULL, NULL, '宜春明月山国家级风景名胜区 (东南角)', 27.6231000, 114.2965000, '07:30-16:30', 0.00, 4.50, 0, '[\"观景亭\",\"月亮文化\",\"人文景观\",\"休闲\",\"爱情主题\"]', 0, 1, '2026-05-09 21:48:31', '2026-05-16 22:33:52', 0);

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
-- Records of biz_user_favorite
-- ----------------------------
INSERT INTO `biz_user_favorite` VALUES (22, 1, 'interpreter', 2, NULL, '2026-05-09 14:50:15', 1);
INSERT INTO `biz_user_favorite` VALUES (23, 1, 'restaurant', 4, NULL, '2026-05-09 14:50:23', 0);
INSERT INTO `biz_user_favorite` VALUES (24, 1, 'restaurant', 2, NULL, '2026-05-09 14:50:28', 0);
INSERT INTO `biz_user_favorite` VALUES (34, 1, 'interpreter', 4, NULL, '2026-05-09 16:34:54', 0);
INSERT INTO `biz_user_favorite` VALUES (42, 1, 'scenic', 3, NULL, '2026-05-10 21:12:09', 0);

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
-- Records of interpreter_profile
-- ----------------------------

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
-- Records of parking_space
-- ----------------------------
INSERT INTO `parking_space` VALUES (1, 'A区停车场', 'Parking Zone A', 'ZONE_A', 0, 100, 100, '景区东门入口附近，步行至大门约2分钟', 0.00, 1, '2026-04-28 17:07:23', '2026-05-14 20:58:08');
INSERT INTO `parking_space` VALUES (2, 'B区停车场', 'Parking Zone B', 'ZONE_B', 0, 200, 200, '景区西门入口附近，步行至大门约3分钟', 10.00, 1, '2026-04-28 17:07:23', '2026-05-15 12:38:44');
INSERT INTO `parking_space` VALUES (3, '残障专用区', 'Disabled Parking', 'ZONE_DIS', 1, 50, 50, '景区主入口旁，紧邻无障碍通道', 0.00, 1, '2026-04-28 17:07:23', '2026-05-15 12:38:44');
INSERT INTO `parking_space` VALUES (4, '新能源充电区', 'EV Charging Zone', 'ZONE_EV', 2, 100, 100, '景区南侧停车楼B1层，配备充电桩30套（15快充+15慢充）', 5.00, 1, '2026-04-28 17:07:23', '2026-05-15 12:38:51');

-- ----------------------------
-- Table structure for parking_spot
-- ----------------------------
DROP TABLE IF EXISTS `parking_spot`;
CREATE TABLE `parking_spot`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `zone_id` bigint UNSIGNED NOT NULL COMMENT '所属停车区域ID',
  `spot_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '车位编号',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态: 0-空闲 1-已占用 2-超时',
  `charger_type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '充电桩类型: 0-无 1-快充 2-慢充',
  `vehicle_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '车牌号',
  `user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '占用用户ID',
  `order_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '关联订单ID',
  `start_time` datetime NULL DEFAULT NULL COMMENT '入场时间',
  `planned_end_time` datetime NULL DEFAULT NULL COMMENT '计划离场时间',
  `actual_end_time` datetime NULL DEFAULT NULL COMMENT '实际离场时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_zone_spot`(`zone_id` ASC, `spot_code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_zone_id`(`zone_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 452 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of parking_spot
-- ----------------------------
INSERT INTO `parking_spot` VALUES (1, 1, 'A-01', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 15:22:15');
INSERT INTO `parking_spot` VALUES (2, 1, 'A-02', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-16 22:35:20');
INSERT INTO `parking_spot` VALUES (3, 1, 'A-03', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 15:22:15');
INSERT INTO `parking_spot` VALUES (4, 1, 'A-04', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (5, 1, 'A-05', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (6, 1, 'A-06', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (7, 1, 'A-07', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (8, 1, 'A-08', 0, 0, NULL, NULL, NULL, NULL, NULL, '2026-05-15 12:30:50', '2026-05-15 00:48:43', '2026-05-15 15:22:15');
INSERT INTO `parking_spot` VALUES (9, 1, 'A-09', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (10, 1, 'A-10', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (11, 1, 'A-11', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (12, 1, 'A-12', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (13, 1, 'A-13', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (14, 1, 'A-14', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (15, 1, 'A-15', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (16, 1, 'A-16', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (17, 1, 'A-17', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 15:22:15');
INSERT INTO `parking_spot` VALUES (18, 1, 'A-18', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (19, 1, 'A-19', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 15:22:15');
INSERT INTO `parking_spot` VALUES (20, 1, 'A-20', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (21, 1, 'A-21', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (22, 1, 'A-22', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (23, 1, 'A-23', 0, 0, NULL, NULL, NULL, NULL, NULL, '2026-05-15 14:23:19', '2026-05-15 00:48:43', '2026-05-15 14:23:19');
INSERT INTO `parking_spot` VALUES (24, 1, 'A-24', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 15:22:15');
INSERT INTO `parking_spot` VALUES (25, 1, 'A-25', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (26, 1, 'A-26', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (27, 1, 'A-27', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (28, 1, 'A-28', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (29, 1, 'A-29', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (30, 1, 'A-30', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (31, 1, 'A-31', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (32, 1, 'A-32', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (33, 1, 'A-33', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 15:22:15');
INSERT INTO `parking_spot` VALUES (34, 1, 'A-34', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (35, 1, 'A-35', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (36, 1, 'A-36', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (37, 1, 'A-37', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (38, 1, 'A-38', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (39, 1, 'A-39', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (40, 1, 'A-40', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (41, 1, 'A-41', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (42, 1, 'A-42', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (43, 1, 'A-43', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 15:22:15');
INSERT INTO `parking_spot` VALUES (44, 1, 'A-44', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (45, 1, 'A-45', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (46, 1, 'A-46', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (47, 1, 'A-47', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (48, 1, 'A-48', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (49, 1, 'A-49', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (50, 1, 'A-50', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (51, 1, 'A-51', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (52, 1, 'A-52', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (53, 1, 'A-53', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (54, 1, 'A-54', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (55, 1, 'A-55', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (56, 1, 'A-56', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (57, 1, 'A-57', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (58, 1, 'A-58', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (59, 1, 'A-59', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (60, 1, 'A-60', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (61, 1, 'A-61', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (62, 1, 'A-62', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (63, 1, 'A-63', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (64, 1, 'A-64', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (65, 1, 'A-65', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (66, 1, 'A-66', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (67, 1, 'A-67', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (68, 1, 'A-68', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (69, 1, 'A-69', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (70, 1, 'A-70', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (71, 1, 'A-71', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (72, 1, 'A-72', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (73, 1, 'A-73', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (74, 1, 'A-74', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (75, 1, 'A-75', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (76, 1, 'A-76', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (77, 1, 'A-77', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (78, 1, 'A-78', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (79, 1, 'A-79', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (80, 1, 'A-80', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (81, 1, 'A-81', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (82, 1, 'A-82', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (83, 1, 'A-83', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (84, 1, 'A-84', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (85, 1, 'A-85', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (86, 1, 'A-86', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (87, 1, 'A-87', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (88, 1, 'A-88', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (89, 1, 'A-89', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (90, 1, 'A-90', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (91, 1, 'A-91', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (92, 1, 'A-92', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (93, 1, 'A-93', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (94, 1, 'A-94', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (95, 1, 'A-95', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (96, 1, 'A-96', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (97, 1, 'A-97', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (98, 1, 'A-98', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (99, 1, 'A-99', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (100, 1, 'A-100', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:48:43', '2026-05-15 00:48:43');
INSERT INTO `parking_spot` VALUES (101, 2, 'B-001', 0, 0, NULL, NULL, NULL, NULL, NULL, '2026-05-15 17:53:32', '2026-05-15 00:56:52', '2026-05-15 17:53:32');
INSERT INTO `parking_spot` VALUES (102, 2, 'B-002', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (103, 2, 'B-003', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (104, 2, 'B-004', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (105, 2, 'B-005', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (106, 2, 'B-006', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (107, 2, 'B-007', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 15:22:15');
INSERT INTO `parking_spot` VALUES (108, 2, 'B-008', 0, 0, NULL, NULL, NULL, NULL, NULL, '2026-05-15 17:57:59', '2026-05-15 00:56:52', '2026-05-15 17:57:59');
INSERT INTO `parking_spot` VALUES (109, 2, 'B-009', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (110, 2, 'B-010', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (111, 2, 'B-011', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (112, 2, 'B-012', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (113, 2, 'B-013', 0, 0, NULL, NULL, NULL, NULL, NULL, '2026-05-15 17:59:14', '2026-05-15 00:56:52', '2026-05-15 17:59:14');
INSERT INTO `parking_spot` VALUES (114, 2, 'B-014', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 15:22:15');
INSERT INTO `parking_spot` VALUES (115, 2, 'B-015', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (116, 2, 'B-016', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (117, 2, 'B-017', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (118, 2, 'B-018', 0, 0, NULL, NULL, NULL, NULL, NULL, '2026-05-15 18:17:39', '2026-05-15 00:56:52', '2026-05-15 18:17:39');
INSERT INTO `parking_spot` VALUES (119, 2, 'B-019', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (120, 2, 'B-020', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (121, 2, 'B-021', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (122, 2, 'B-022', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (123, 2, 'B-023', 0, 0, NULL, NULL, NULL, NULL, NULL, '2026-05-15 23:16:19', '2026-05-15 00:56:52', '2026-05-15 23:16:19');
INSERT INTO `parking_spot` VALUES (124, 2, 'B-024', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (125, 2, 'B-025', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (126, 2, 'B-026', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (127, 2, 'B-027', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (128, 2, 'B-028', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (129, 2, 'B-029', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (130, 2, 'B-030', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (131, 2, 'B-031', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (132, 2, 'B-032', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (133, 2, 'B-033', 0, 0, NULL, NULL, NULL, NULL, NULL, '2026-05-15 10:08:30', '2026-05-15 00:56:52', '2026-05-15 15:22:15');
INSERT INTO `parking_spot` VALUES (134, 2, 'B-034', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (135, 2, 'B-035', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (136, 2, 'B-036', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (137, 2, 'B-037', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (138, 2, 'B-038', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (139, 2, 'B-039', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (140, 2, 'B-040', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (141, 2, 'B-041', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (142, 2, 'B-042', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (143, 2, 'B-043', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (144, 2, 'B-044', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (145, 2, 'B-045', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (146, 2, 'B-046', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (147, 2, 'B-047', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-16 22:35:20');
INSERT INTO `parking_spot` VALUES (148, 2, 'B-048', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (149, 2, 'B-049', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (150, 2, 'B-050', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (151, 2, 'B-051', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (152, 2, 'B-052', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (153, 2, 'B-053', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (154, 2, 'B-054', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (155, 2, 'B-055', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (156, 2, 'B-056', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (157, 2, 'B-057', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (158, 2, 'B-058', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 15:22:15');
INSERT INTO `parking_spot` VALUES (159, 2, 'B-059', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (160, 2, 'B-060', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (161, 2, 'B-061', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (162, 2, 'B-062', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (163, 2, 'B-063', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (164, 2, 'B-064', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (165, 2, 'B-065', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (166, 2, 'B-066', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (167, 2, 'B-067', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (168, 2, 'B-068', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (169, 2, 'B-069', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (170, 2, 'B-070', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (171, 2, 'B-071', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (172, 2, 'B-072', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (173, 2, 'B-073', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (174, 2, 'B-074', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (175, 2, 'B-075', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (176, 2, 'B-076', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (177, 2, 'B-077', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (178, 2, 'B-078', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (179, 2, 'B-079', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (180, 2, 'B-080', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (181, 2, 'B-081', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (182, 2, 'B-082', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (183, 2, 'B-083', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (184, 2, 'B-084', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (185, 2, 'B-085', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (186, 2, 'B-086', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (187, 2, 'B-087', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (188, 2, 'B-088', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (189, 2, 'B-089', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (190, 2, 'B-090', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (191, 2, 'B-091', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (192, 2, 'B-092', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (193, 2, 'B-093', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (194, 2, 'B-094', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (195, 2, 'B-095', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (196, 2, 'B-096', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (197, 2, 'B-097', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (198, 2, 'B-098', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (199, 2, 'B-099', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (200, 2, 'B-100', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (201, 2, 'B-101', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (202, 2, 'B-102', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (203, 2, 'B-103', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (204, 2, 'B-104', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (205, 2, 'B-105', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (206, 2, 'B-106', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (207, 2, 'B-107', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (208, 2, 'B-108', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (209, 2, 'B-109', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (210, 2, 'B-110', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (211, 2, 'B-111', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (212, 2, 'B-112', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (213, 2, 'B-113', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (214, 2, 'B-114', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (215, 2, 'B-115', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (216, 2, 'B-116', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (217, 2, 'B-117', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (218, 2, 'B-118', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (219, 2, 'B-119', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (220, 2, 'B-120', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (221, 2, 'B-121', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (222, 2, 'B-122', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (223, 2, 'B-123', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (224, 2, 'B-124', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (225, 2, 'B-125', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (226, 2, 'B-126', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (227, 2, 'B-127', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (228, 2, 'B-128', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (229, 2, 'B-129', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (230, 2, 'B-130', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (231, 2, 'B-131', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (232, 2, 'B-132', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (233, 2, 'B-133', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (234, 2, 'B-134', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (235, 2, 'B-135', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (236, 2, 'B-136', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (237, 2, 'B-137', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (238, 2, 'B-138', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (239, 2, 'B-139', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (240, 2, 'B-140', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (241, 2, 'B-141', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (242, 2, 'B-142', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (243, 2, 'B-143', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (244, 2, 'B-144', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (245, 2, 'B-145', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (246, 2, 'B-146', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (247, 2, 'B-147', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (248, 2, 'B-148', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (249, 2, 'B-149', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (250, 2, 'B-150', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (251, 2, 'B-151', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (252, 2, 'B-152', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (253, 2, 'B-153', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (254, 2, 'B-154', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (255, 2, 'B-155', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (256, 2, 'B-156', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (257, 2, 'B-157', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (258, 2, 'B-158', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (259, 2, 'B-159', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (260, 2, 'B-160', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (261, 2, 'B-161', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (262, 2, 'B-162', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (263, 2, 'B-163', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (264, 2, 'B-164', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (265, 2, 'B-165', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (266, 2, 'B-166', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (267, 2, 'B-167', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (268, 2, 'B-168', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (269, 2, 'B-169', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (270, 2, 'B-170', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (271, 2, 'B-171', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (272, 2, 'B-172', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (273, 2, 'B-173', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (274, 2, 'B-174', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (275, 2, 'B-175', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (276, 2, 'B-176', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (277, 2, 'B-177', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (278, 2, 'B-178', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (279, 2, 'B-179', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (280, 2, 'B-180', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (281, 2, 'B-181', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (282, 2, 'B-182', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (283, 2, 'B-183', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (284, 2, 'B-184', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (285, 2, 'B-185', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (286, 2, 'B-186', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (287, 2, 'B-187', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (288, 2, 'B-188', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (289, 2, 'B-189', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (290, 2, 'B-190', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (291, 2, 'B-191', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (292, 2, 'B-192', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (293, 2, 'B-193', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (294, 2, 'B-194', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (295, 2, 'B-195', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (296, 2, 'B-196', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (297, 2, 'B-197', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (298, 2, 'B-198', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (299, 2, 'B-199', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (300, 2, 'B-200', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (301, 3, 'D-01', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (302, 3, 'D-02', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (303, 3, 'D-03', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (304, 3, 'D-04', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (305, 3, 'D-05', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (306, 3, 'D-06', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (307, 3, 'D-07', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (308, 3, 'D-08', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (309, 3, 'D-09', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (310, 3, 'D-10', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (311, 3, 'D-11', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (312, 3, 'D-12', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (313, 3, 'D-13', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (314, 3, 'D-14', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (315, 3, 'D-15', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (316, 3, 'D-16', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (317, 3, 'D-17', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (318, 3, 'D-18', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (319, 3, 'D-19', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (320, 3, 'D-20', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (321, 3, 'D-21', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (322, 3, 'D-22', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (323, 3, 'D-23', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (324, 3, 'D-24', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (325, 3, 'D-25', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (326, 3, 'D-26', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (327, 3, 'D-27', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (328, 3, 'D-28', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (329, 3, 'D-29', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (330, 3, 'D-30', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (331, 3, 'D-31', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (332, 3, 'D-32', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (333, 3, 'D-33', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (334, 3, 'D-34', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (335, 3, 'D-35', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (336, 3, 'D-36', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (337, 3, 'D-37', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (338, 3, 'D-38', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (339, 3, 'D-39', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (340, 3, 'D-40', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (341, 3, 'D-41', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (342, 3, 'D-42', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (343, 3, 'D-43', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (344, 3, 'D-44', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (345, 3, 'D-45', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (346, 3, 'D-46', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (347, 3, 'D-47', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (348, 3, 'D-48', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (349, 3, 'D-49', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (350, 3, 'D-50', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (351, 4, 'E-01', 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (352, 4, 'E-02', 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (353, 4, 'E-03', 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (354, 4, 'E-04', 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (355, 4, 'E-05', 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (356, 4, 'E-06', 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (357, 4, 'E-07', 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (358, 4, 'E-08', 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (359, 4, 'E-09', 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (360, 4, 'E-10', 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (361, 4, 'E-11', 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (362, 4, 'E-12', 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (363, 4, 'E-13', 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (364, 4, 'E-14', 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (365, 4, 'E-15', 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (366, 4, 'E-16', 0, 2, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (367, 4, 'E-17', 0, 2, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (368, 4, 'E-18', 0, 2, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (369, 4, 'E-19', 0, 2, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (370, 4, 'E-20', 0, 2, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (371, 4, 'E-21', 0, 2, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (372, 4, 'E-22', 0, 2, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (373, 4, 'E-23', 0, 2, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (374, 4, 'E-24', 0, 2, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (375, 4, 'E-25', 0, 2, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (376, 4, 'E-26', 0, 2, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (377, 4, 'E-27', 0, 2, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (378, 4, 'E-28', 0, 2, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (379, 4, 'E-29', 0, 2, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (380, 4, 'E-30', 0, 2, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');
INSERT INTO `parking_spot` VALUES (381, 4, 'E-31', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (382, 4, 'E-32', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (383, 4, 'E-33', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (384, 4, 'E-34', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (385, 4, 'E-35', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (386, 4, 'E-36', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (387, 4, 'E-37', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (388, 4, 'E-38', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (389, 4, 'E-39', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (390, 4, 'E-40', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (391, 4, 'E-41', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (392, 4, 'E-42', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (393, 4, 'E-43', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (394, 4, 'E-44', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (395, 4, 'E-45', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (396, 4, 'E-46', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (397, 4, 'E-47', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (398, 4, 'E-48', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (399, 4, 'E-49', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (400, 4, 'E-50', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (401, 4, 'E-51', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (402, 4, 'E-52', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (403, 4, 'E-53', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (404, 4, 'E-54', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (405, 4, 'E-55', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (406, 4, 'E-56', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (407, 4, 'E-57', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (408, 4, 'E-58', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (409, 4, 'E-59', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (410, 4, 'E-60', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (411, 4, 'E-61', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (412, 4, 'E-62', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (413, 4, 'E-63', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (414, 4, 'E-64', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (415, 4, 'E-65', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (416, 4, 'E-66', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (417, 4, 'E-67', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (418, 4, 'E-68', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (419, 4, 'E-69', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (420, 4, 'E-70', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (421, 4, 'E-71', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (422, 4, 'E-72', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (423, 4, 'E-73', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (424, 4, 'E-74', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (425, 4, 'E-75', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (426, 4, 'E-76', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (427, 4, 'E-77', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (428, 4, 'E-78', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (429, 4, 'E-79', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (430, 4, 'E-80', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (431, 4, 'E-81', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (432, 4, 'E-82', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (433, 4, 'E-83', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (434, 4, 'E-84', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (435, 4, 'E-85', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (436, 4, 'E-86', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (437, 4, 'E-87', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (438, 4, 'E-88', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (439, 4, 'E-89', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (440, 4, 'E-90', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (441, 4, 'E-91', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (442, 4, 'E-92', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (443, 4, 'E-93', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (444, 4, 'E-94', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (445, 4, 'E-95', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (446, 4, 'E-96', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (447, 4, 'E-97', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (448, 4, 'E-98', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (449, 4, 'E-99', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 00:56:52');
INSERT INTO `parking_spot` VALUES (450, 4, 'E-100', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 00:56:52', '2026-05-15 12:39:35');

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
-- Records of sys_feedback
-- ----------------------------

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
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$12$ZKg0tkI5US2QOmpNeFcDQuAhjI6iX1DLv5eikxdysQ5oqIqrpTCs6', NULL, NULL, '系统管理员', NULL, 2, 1, 'zh_CN', '0:0:0:0:0:0:0:1', '2026-05-16 12:24:46', '2026-04-28 17:07:23', '2026-05-16 12:24:45', 0);

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
-- Records of ugc_like
-- ----------------------------

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

-- ----------------------------
-- Records of ugc_post
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
