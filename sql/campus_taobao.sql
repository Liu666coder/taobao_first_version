/*
 Navicat Premium Dump SQL

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : campus_taobao

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 08/07/2026 23:11:13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色: PRODUCT_MANAGER商品专员 MARKETING营销经理 SYSTEM_ADMIN系统管理员',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (1, 'admin', '123456', '主管1', '', '16626620269', '/images/admin_avatar_1_4d5511fb.jpg', 'SYSTEM_ADMIN', 1, '2026-06-11 17:39:55', '2026-07-06 09:53:13');
INSERT INTO `admin` VALUES (2, 'admin1', '123456', '刘admin', '', '16623020269', '/images/admin_avatar_2_725b031f.jpg', 'PRODUCT_MANAGER', 1, '2026-06-12 15:32:08', '2026-07-06 10:39:36');
INSERT INTO `admin` VALUES (5, 'admin2', '123456', '欧阳', NULL, NULL, '/images/admin_avatar_5_b856f461.jpg', 'MARKETING_MANAGER', 1, '2026-07-06 10:51:23', '2026-07-06 11:59:35');

-- ----------------------------
-- Table structure for cart
-- ----------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名（冗余字段）',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品名称（冗余字段）',
  `quantity` int NULL DEFAULT 1 COMMENT '数量',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `fk_cart_product`(`product_id` ASC) USING BTREE,
  CONSTRAINT `fk_cart_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 70 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '购物车表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of cart
-- ----------------------------
INSERT INTO `cart` VALUES (69, 1, '刘人源', 34, '自热火锅重庆风味', 1, '2026-07-08 14:18:59');

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (1, '电子产品', '手机、电脑、数码产品', 1, '2026-06-11 17:39:55');
INSERT INTO `category` VALUES (2, '图书教材', '各类教材、考试用书', 1, '2026-06-11 17:39:55');
INSERT INTO `category` VALUES (3, '生活用品', '日用品、宿舍用品', 1, '2026-06-11 17:39:55');
INSERT INTO `category` VALUES (4, '服饰鞋包', '服装、鞋子、包包', 1, '2026-06-11 17:39:55');
INSERT INTO `category` VALUES (5, '运动户外', '运动器材、户外用品', 1, '2026-06-11 17:39:55');

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名（冗余字段）',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品名称',
  `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '价格',
  `quantity` int NULL DEFAULT NULL COMMENT '数量',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `fk_item_product`(`product_id` ASC) USING BTREE,
  CONSTRAINT `fk_item_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_item_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 65 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单详情表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_item
-- ----------------------------
INSERT INTO `order_item` VALUES (1, 1, 1, '刘人源', 7, '篮球', 129.00, 1);
INSERT INTO `order_item` VALUES (2, 1, 1, '刘人源', 6, '优衣库T恤', 99.00, 1);
INSERT INTO `order_item` VALUES (3, 1, 1, '刘人源', 5, '宿舍台灯', 69.00, 1);
INSERT INTO `order_item` VALUES (4, 1, 1, '刘人源', 4, '线性代数教材', 35.00, 1);
INSERT INTO `order_item` VALUES (5, 1, 1, '刘人源', 3, '高等数学教材', 45.00, 2);
INSERT INTO `order_item` VALUES (6, 1, 1, '刘人源', 2, 'MacBook Pro 14寸', 14999.00, 1);
INSERT INTO `order_item` VALUES (7, 1, 1, '刘人源', 1, 'iPhone 17 Pro', 7999.00, 3);
INSERT INTO `order_item` VALUES (8, 2, 2, '欧阳马力', 3, '高等数学教材', 45.00, 1);
INSERT INTO `order_item` VALUES (9, 2, 2, '欧阳马力', 2, 'MacBook Pro 14寸', 14999.00, 1);
INSERT INTO `order_item` VALUES (10, 2, 2, '欧阳马力', 1, 'iPhone 17 Pro', 7999.00, 1);
INSERT INTO `order_item` VALUES (11, 3, 2, '欧阳马力', 4, '线性代数教材', 35.00, 1);
INSERT INTO `order_item` VALUES (14, 5, 2, '欧阳马力', 7, '篮球', 129.00, 1);
INSERT INTO `order_item` VALUES (15, 6, 2, '欧阳马力', 1, 'iPhone 17 Pro', 7999.00, 1);
INSERT INTO `order_item` VALUES (19, 10, 1, '刘人源', 1, 'iPhone 17 Pro', 7999.00, 1);
INSERT INTO `order_item` VALUES (20, 11, 1, '刘人源', 2, 'MacBook Pro 14寸', 14999.00, 1);
INSERT INTO `order_item` VALUES (21, 11, 1, '刘人源', 1, 'iPhone 17 Pro', 7999.00, 2);
INSERT INTO `order_item` VALUES (23, 13, 1, '刘人源', 2, 'MacBook Pro 14寸', 14999.00, 1);
INSERT INTO `order_item` VALUES (24, 13, 1, '刘人源', 1, 'iPhone 17 Pro', 7999.00, 1);
INSERT INTO `order_item` VALUES (25, 14, 1, '刘人源', 2, 'MacBook Pro 14寸', 14999.00, 1);
INSERT INTO `order_item` VALUES (26, 15, 1, '刘人源', 1, 'iPhone 17 Pro', 7999.00, 1);
INSERT INTO `order_item` VALUES (29, 18, 1, '刘人源', 2, 'MacBook Pro 14寸', 14999.00, 1);
INSERT INTO `order_item` VALUES (30, 19, 1, '刘人源', 2, 'MacBook Pro 14寸', 14999.00, 1);
INSERT INTO `order_item` VALUES (31, 19, 1, '刘人源', 1, 'iPhone 17 Pro', 7999.00, 1);
INSERT INTO `order_item` VALUES (32, 20, 1, '刘人源', 1, 'iPhone 17 Pro', 7999.00, 1);
INSERT INTO `order_item` VALUES (33, 21, 1, '刘人源', 1, 'iPhone 17 Pro', 7999.00, 1);
INSERT INTO `order_item` VALUES (34, 22, 1, '刘人源', 1, 'iPhone 17 Pro', 7999.00, 1);
INSERT INTO `order_item` VALUES (35, 23, 1, '刘人源', 1, 'iPhone 17 Pro', 7999.00, 1);
INSERT INTO `order_item` VALUES (36, 24, 1, '刘人源', 1, 'iPhone 17 Pro', 7999.00, 1);
INSERT INTO `order_item` VALUES (37, 25, 1, '刘人源', 1, 'iPhone 17 Pro', 7999.00, 1);
INSERT INTO `order_item` VALUES (38, 26, 1, '刘人源', 2, 'MacBook Pro 14寸', 14999.00, 1);
INSERT INTO `order_item` VALUES (39, 27, 1, '刘人源', 1, 'iPhone 17 Pro', 7999.00, 1);
INSERT INTO `order_item` VALUES (40, 28, 1, '刘人源', 1, 'iPhone 17 Pro', 7999.00, 1);
INSERT INTO `order_item` VALUES (41, 29, 1, '刘人源', 11, '李宁短袖', 99.00, 1);
INSERT INTO `order_item` VALUES (42, 30, 1, '刘人源', 1, 'iPhone 17 Pro', 7999.00, 1);
INSERT INTO `order_item` VALUES (43, 31, 1, '刘人源', 2, 'MacBook Pro 14寸', 14999.00, 1);
INSERT INTO `order_item` VALUES (44, 32, 1, '刘人源', 11, '李宁短袖', 99.00, 1);
INSERT INTO `order_item` VALUES (45, 33, 1, '刘人源', 7, '篮球', 129.00, 1);
INSERT INTO `order_item` VALUES (52, 40, 1, '刘人源', 13, '李宁利刃4篮球鞋', 399.00, 1);
INSERT INTO `order_item` VALUES (53, 40, 1, '刘人源', 27, '娃哈哈AD钙奶', 30.00, 1);
INSERT INTO `order_item` VALUES (54, 40, 1, '刘人源', 32, '言仓水豚噜噜联名雪糕马克杯陶瓷', 20.00, 2);
INSERT INTO `order_item` VALUES (55, 41, 1, '刘人源', 28, '透明多层抽屉式化妆品收纳盒', 30.00, 1);
INSERT INTO `order_item` VALUES (56, 41, 1, '刘人源', 30, '心相印抽纸加厚3层100抽卫生纸', 39.00, 1);
INSERT INTO `order_item` VALUES (57, 42, 1, '刘人源', 31, '锈钢泡面碗加餐具', 10.00, 1);
INSERT INTO `order_item` VALUES (58, 42, 1, '刘人源', 32, '言仓水豚噜噜联名雪糕马克杯陶瓷', 20.00, 1);
INSERT INTO `order_item` VALUES (60, 44, 1, '刘人源', 32, '言仓水豚噜噜联名雪糕马克杯陶瓷', 20.00, 2);
INSERT INTO `order_item` VALUES (61, 45, 1, '刘人源', 28, '透明多层抽屉式化妆品收纳盒', 30.00, 1);
INSERT INTO `order_item` VALUES (62, 45, 1, '刘人源', 32, '言仓水豚噜噜联名雪糕马克杯陶瓷', 20.00, 1);
INSERT INTO `order_item` VALUES (63, 45, 1, '刘人源', 23, '蒙牛鲜奶每日鲜语A2β酪蛋白鲜奶', 59.00, 1);
INSERT INTO `order_item` VALUES (64, 46, 1, '刘人源', 37, '李宁紧身衣短袖男健身速干T恤篮球', 23.00, 1);

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '总金额',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态 0待付款 1已付款 2已发货 3已完成',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_orders_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 47 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (1, 'ORD202606112337051BF3A63E', 1, 39418.00, 3, '2026-06-11 23:37:05');
INSERT INTO `orders` VALUES (2, 'ORD202606121415595C80D71E', 2, 23043.00, 3, '2026-06-12 14:15:59');
INSERT INTO `orders` VALUES (3, 'ORD20260612152118D0D45EC4', 2, 35.00, 2, '2026-06-12 15:21:18');
INSERT INTO `orders` VALUES (5, 'ORD202606121523151661CC32', 2, 129.00, 2, '2026-06-12 15:23:15');
INSERT INTO `orders` VALUES (6, 'ORD2026061215285813EAAA7A', 2, 7999.00, 2, '2026-06-12 15:28:58');
INSERT INTO `orders` VALUES (10, 'ORD20260703204841C6256F53', 1, 7999.00, 3, '2026-07-03 20:48:41');
INSERT INTO `orders` VALUES (11, 'ORD202607032051492F9748FC', 1, 30997.00, 3, '2026-07-03 20:51:49');
INSERT INTO `orders` VALUES (13, 'ORD2026070320592299A04E31', 1, 22998.00, 3, '2026-07-03 20:59:22');
INSERT INTO `orders` VALUES (14, 'ORD2026070320594803EEAD5B', 1, 14999.00, 3, '2026-07-03 20:59:48');
INSERT INTO `orders` VALUES (15, 'ORD202607060901567360AE9E', 1, 7999.00, 3, '2026-07-06 09:01:56');
INSERT INTO `orders` VALUES (16, 'ORD2026070609365538946B7E', 1, 100.00, 3, '2026-07-06 09:36:55');
INSERT INTO `orders` VALUES (17, 'ORD20260706110816212C21DE', 1, 1000.00, 3, '2026-07-06 11:08:16');
INSERT INTO `orders` VALUES (18, 'ORD2026070611094438D435C9', 1, 14999.00, 3, '2026-07-06 11:09:44');
INSERT INTO `orders` VALUES (19, 'ORD20260706111607D78CF8E2', 1, 22998.00, 3, '2026-07-06 11:16:07');
INSERT INTO `orders` VALUES (20, 'ORD20260706111725D9688DF2', 1, 7999.00, 3, '2026-07-06 11:17:25');
INSERT INTO `orders` VALUES (21, 'ORD20260706111733E21E05E2', 1, 7999.00, 3, '2026-07-06 11:17:33');
INSERT INTO `orders` VALUES (22, 'ORD202607061118297297FC52', 1, 7999.00, 3, '2026-07-06 11:18:29');
INSERT INTO `orders` VALUES (23, 'ORD20260706111837FECCA3DD', 1, 7999.00, 3, '2026-07-06 11:18:37');
INSERT INTO `orders` VALUES (24, 'ORD20260706112521C684E44F', 1, 7999.00, 3, '2026-07-06 11:25:21');
INSERT INTO `orders` VALUES (25, 'ORD2026070611255918622AC4', 1, 7999.00, 3, '2026-07-06 11:25:59');
INSERT INTO `orders` VALUES (26, 'ORD20260706112607AEEDCDBF', 1, 14999.00, 3, '2026-07-06 11:26:07');
INSERT INTO `orders` VALUES (27, 'ORD20260706113220FBFD991C', 1, 7999.00, 3, '2026-07-06 11:32:20');
INSERT INTO `orders` VALUES (28, 'ORD202607061140024F3D8B2F', 1, 7999.00, 3, '2026-07-06 11:40:02');
INSERT INTO `orders` VALUES (29, 'ORD2026070611464169D07393', 1, 99.00, 3, '2026-07-06 11:46:41');
INSERT INTO `orders` VALUES (30, 'ORD202607061150256857D8EF', 1, 7999.00, 3, '2026-07-06 11:50:25');
INSERT INTO `orders` VALUES (31, 'ORD2026070616470889C8B1FF', 1, 14999.00, 3, '2026-07-06 16:47:08');
INSERT INTO `orders` VALUES (32, 'ORD202607061710414E0BF784', 1, 99.00, 3, '2026-07-06 17:10:41');
INSERT INTO `orders` VALUES (33, 'ORD202607061918177FD829CC', 1, 129.00, 3, '2026-07-06 19:18:17');
INSERT INTO `orders` VALUES (40, 'ORD20260706230534C79A5895', 1, 469.00, 3, '2026-07-06 23:05:34');
INSERT INTO `orders` VALUES (41, 'ORD20260706231258C76E96E7', 1, 69.00, 3, '2026-07-06 23:12:58');
INSERT INTO `orders` VALUES (42, 'ORD202607071541376F746E29', 1, 30.00, 3, '2026-07-07 15:41:37');
INSERT INTO `orders` VALUES (44, 'ORD202607071545346C9CDE51', 1, 40.00, 3, '2026-07-07 15:45:34');
INSERT INTO `orders` VALUES (45, 'ORD20260707164756CBEAE4D4', 1, 109.00, 3, '2026-07-07 16:47:56');
INSERT INTO `orders` VALUES (46, 'ORD2026070716521664175B8E', 1, 23.00, 3, '2026-07-07 16:52:16');

-- ----------------------------
-- Table structure for product
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `category_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类名称（冗余字段）',
  `price` decimal(10, 2) NOT NULL COMMENT '价格',
  `stock` int NULL DEFAULT 0 COMMENT '库存',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品图片',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品描述',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态 1上架 0下架',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 40 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of product
-- ----------------------------
INSERT INTO `product` VALUES (1, 'iPhone 17 Pro', 1, '电子产品', 7999.00, 30, '/images/product_215f34e8.jpg?t=1783307203435', '📱 新学期装备党看过来！iPhone 17 Pro 轻松搞定学习+生活——A19芯片流畅运行各种学习App，4800万像素随手拍出社团活动大片📸，超长续航陪你从早八到晚自习🔋。7999元拿下“大学四年流畅战神”，性能颜值双在线，社团拍照/网课笔记/追剧游戏全hold住，这份科技投资绝对值！✨ （悄悄说：比官网价还友好哦~）', 1, '2026-06-11 17:39:55', '2026-07-06 17:05:34');
INSERT INTO `product` VALUES (2, 'MacBook Pro 14寸', 1, '电子产品', 14999.00, 21, '/images/MacBook Pro 14寸.jpg', '苹果笔记本电脑', 1, '2026-06-11 17:39:55', '2026-07-06 16:47:08');
INSERT INTO `product` VALUES (3, '高等数学教材', 2, '图书教材', 45.00, 197, '/images/高等数学.jpg', '同济大学高等数学', 1, '2026-06-11 17:39:55', '2026-06-12 14:15:59');
INSERT INTO `product` VALUES (4, '线性代数教材', 2, '图书教材', 35.00, 148, '/images/线性代数.jpg', '线性代数教材', 1, '2026-06-11 17:39:55', '2026-06-12 15:21:18');
INSERT INTO `product` VALUES (5, '宿舍台灯', 3, '生活用品', 69.00, 99, '/images/宿舍台灯.jpg', '护眼台灯', 1, '2026-06-11 17:39:55', '2026-07-03 20:53:40');
INSERT INTO `product` VALUES (6, '优衣库T恤', 4, '服饰鞋包', 99.00, 299, '/images/优衣库短袖.jpg', '纯棉T恤', 1, '2026-06-11 17:39:55', '2026-06-12 15:22:59');
INSERT INTO `product` VALUES (7, '篮球', 5, '运动户外', 129.00, 77, '/images/篮球.jpg', '斯伯丁篮球', 1, '2026-06-11 17:39:55', '2026-07-06 19:18:17');
INSERT INTO `product` VALUES (11, '李宁短袖', 3, '生活用品', 99.00, 8, '/images/product_5045fc05.png?t=1783309549391', '夏天就要到了，这件李宁短袖必须安排上😎 百元不到get国货之光，面料透气不闷汗，上课打球通勤都能穿！经典logo设计怎么搭都不出错，衣柜里的万能单品就是它✨ 学生党闭眼入，这个价格真的太香啦～快冲💨', 1, '2026-07-06 11:46:20', '2026-07-06 17:10:41');
INSERT INTO `product` VALUES (12, '蓝月亮洗衣液', 3, '生活用品', 40.00, 100, '/images/product_075609f5.png?t=1783328831913', '宿舍必备神器！蓝月亮洗衣液1大瓶才40元💰，宿舍合伙囤货人均不到10元！去污力超强还省用量，汗渍火锅味通通搞定～洗完衣服自带淡淡清香🌸，晒完被子阳光味道混合着花香，治愈一整天！关键是大容量装，从开学用到期末，省钱又省心，室友都夸你会过日子✨ 赶紧冲鸭！🛒', 1, '2026-07-06 17:07:53', '2026-07-06 17:09:13');
INSERT INTO `product` VALUES (13, '李宁利刃4篮球鞋', 5, '运动户外', 399.00, 49, '/images/product_2bab47fd.png?t=1783329388565', '球场MVP必备装备！李宁利刃4实战篮球鞋，缓震包裹双双在线，脚感超弹起步稳如磐石👟！耐磨防滑大底，水泥地也能轻松carry全场💥~ 期末解压就靠它，399的价格攒钱就能冲，性价比绝了！赶紧换上它，下个球场焦点就是你💡💪🎯', 1, '2026-07-06 17:17:30', '2026-07-06 23:05:34');
INSERT INTO `product` VALUES (14, '迪卡侬去氯洗发沐浴露洁面乳', 3, '生活用品', 99.00, 50, '/images/product_9e4bdfd9.png?t=1783329469230', '游完泳发质干涩？皮肤紧绷？迪卡侬去氯洗发沐浴露洁面乳，一瓶搞定泳后清洁！🧴 温和去除氯残留，洗头洗澡洗脸三合一，宿舍囤货超省心～✨ 囤货季必入，性价比绝绝子，泳池党闭眼入！🏊‍♀️💆‍♂️', 1, '2026-07-06 17:18:46', '2026-07-06 17:18:46');
INSERT INTO `product` VALUES (15, '高颜值大容量水杯女生冰霸吸管杯女学生夏天茶水分离杯子', 3, '生活用品', 10.00, 20, '/images/product_d18d216f.png?t=1783329587816', '夏天续命神器来啦！🔥这款冰霸杯不仅能装下1L冰水，还有茶水分离设计，泡茶泡果茶超方便～图书馆学习、操场运动随手带，清爽一整天✨ 高颜值ins风拍照也超出片！关键是现在0元试用，这羊毛不薅真的亏！🧋快@你的室友一起冲！', 1, '2026-07-06 17:20:58', '2026-07-06 17:23:13');
INSERT INTO `product` VALUES (16, '公牛插座usb插排插线板', 3, '生活用品', 30.00, 9, '/images/product_f879ce79.png?t=1783329710359', '宿舍神器来啦！公牛USB插排，30元承包你整个寝室的快乐~ 💡自带USB口，手机充电宝台灯齐上阵，告别充电头大战！孔位超多不打架，安全耐用性价比炸裂💥 奶茶价就能拯救电量焦虑，闭眼入不亏！', 1, '2026-07-06 17:23:01', '2026-07-06 17:24:01');
INSERT INTO `product` VALUES (17, '官方鉴别阿迪达斯adidas VL Court樱粉猫爪低帮板鞋', 5, '运动户外', 599.00, 11, '/images/product_cb0f5d97.png?t=1783329879292', '姐妹们快看这双樱花粉猫爪鞋🌸！阿迪达斯官方鉴别正品，软糯糯的粉色配色超百搭，配牛仔裤或小裙子都绝绝子！鞋底软乎乎像踩棉花，上课赶路or逛街暴走都不累脚🦶，599元get明星同款，学生党也能轻松hold住早八穿搭✨库存不多速冲！', 1, '2026-07-06 17:25:44', '2026-07-06 17:25:44');
INSERT INTO `product` VALUES (18, '海天实惠生抽', 3, '生活用品', 20.00, 50, '/images/product_78bae5c5.png?t=1783330148090', '食堂吃腻了？自己动手做美食吧！海天实惠生抽，只要20元，性价比超高，调味神器让宿舍菜肴瞬间升级。煮面、炒饭、拌沙拉，一滴鲜香，回味无穷！学生党必备，轻松变身厨神🍳💰 赶紧囤货，开启美味生活！😋', 1, '2026-07-06 17:29:36', '2026-07-06 17:29:36');
INSERT INTO `product` VALUES (19, '李宁羽毛球雷霆小钢炮', 5, '运动户外', 200.00, 11, '/images/product_128b34a7.png?t=1783330212461', '李宁雷霆小钢炮✨大学生球搭子看过来！日常训练、娱乐比赛都超顺手的耐打神器🏸轻便手感+暴扣快感，200元档位性价比王炸💥专业品牌闭眼入，宿舍楼下就能练起来！快来pick你的第一支战拍吧💪', 1, '2026-07-06 17:30:53', '2026-07-06 17:30:53');
INSERT INTO `product` VALUES (20, '李宁运动裤男款夏季黑色裤子', 3, '生活用品', 66.00, 20, '/images/product_d689eff1.png?t=1783330272681', '夏天必备の万能黑裤🔥 李宁正品运动裤，轻薄透气不闷汗！上课通勤、打球撸串一条搞定，学生价¥66到手 💰 百搭黑色怎么穿都不出错，侧边经典Logo超有型～ 室友已经悄悄入坑了！💪😎', 1, '2026-07-06 17:31:37', '2026-07-06 17:31:37');
INSERT INTO `product` VALUES (21, '立白组合 洗洁精', 3, '生活用品', 25.00, 6, '/images/product_ab1a9a6a.png?t=1783330415649', '宿舍火锅局后油腻腻的碗筷怎么办？🧴✨立白组合洗洁精来拯救！浓缩配方一滴去油快，温和不伤手还能轻松搞定果蔬清洗🍎。25元承包你一学期的洁净生活，性价比绝绝子，囤它准没错！💡', 1, '2026-07-06 17:33:57', '2026-07-06 17:33:57');
INSERT INTO `product` VALUES (22, '蒙牛特仑苏纯牛奶250mlx16盒', 3, '生活用品', 59.00, 40, '/images/product_cd90da97.png?t=1783330454686', '早餐搭档来啦！蒙牛特仑苏纯牛奶，每盒250ml刚刚好✨ 搭配面包或麦片，营养满分开启一天学习！整箱16盒装超划算，宿舍囤货优选，单盒不到4元💰 省时省力还能补充优质蛋白，熬夜写论文、早八赶课必备～健康生活从一瓶好牛奶开始🥛 需要的同学快冲鸭！', 1, '2026-07-06 17:34:41', '2026-07-06 17:34:41');
INSERT INTO `product` VALUES (23, '蒙牛鲜奶每日鲜语A2β酪蛋白鲜奶', 3, '生活用品', 59.00, 49, '/images/product_808c3eb1.png?t=1783330519482', '早八课、图书馆熬夜都需要补能量！蒙牛A2β酪蛋白鲜奶，亲和好吸收的“能量续命水”🥛✨ 现在0元尝鲜，宿舍冰箱常备它，随时补充优质蛋白和钙～ 比奶茶健康，比饮料实在，性价比之王就是它！💪', 1, '2026-07-06 17:35:40', '2026-07-07 16:47:56');
INSERT INTO `product` VALUES (24, '迷你小风扇', 3, '生活用品', 20.00, 30, '/images/product_05e64352.png?t=1783330569067', '嘿，同学们！夏天宿舍热到爆？这款迷你小风扇超便携💨，USB充电随处用，上课自习、运动健身都能随时降温❄️！仅需20元，轻松带走清凉，性价比杠杠的👍！快来入手，让这个夏天不再闷热吧～😄', 1, '2026-07-06 17:37:13', '2026-07-06 17:37:13');
INSERT INTO `product` VALUES (25, '男士单肩休闲大容量多层单肩斜跨包', 3, '生活用品', 40.00, 11, '/images/product_e8366f47.png?t=1783330976989', '上课泡图书馆总嫌包太小？这款单肩包大容量+多层分区，课本水杯充电宝轻松装下！40元超高性价比，学生党闭眼入✨背上去还超有范儿~💼', 1, '2026-07-06 17:43:32', '2026-07-06 17:43:32');
INSERT INTO `product` VALUES (26, '润本植物精油', 3, '生活用品', 20.00, 11, '/images/product_5e0f50f7.png?t=1783331109186', '寝室神器来啦！润本植物精油只要20元💰～熬夜复习滴一滴提神醒脑，蚊虫叮咬抹一抹快速舒缓，还能当香薰让宿舍秒变高级香氛房🏠✨ 纯天然配方温和不刺激，小支装随身带超方便！开学季囤货价，实用到哭😭精致生活其实不用贵～🌿', 1, '2026-07-06 17:47:05', '2026-07-06 17:47:05');
INSERT INTO `product` VALUES (27, '娃哈哈AD钙奶', 3, '生活用品', 30.00, 19, '/images/product_008ede08.png?t=1783331238857', '谁的童年回忆杀来啦！🥛 熬夜赶ddl、考试周续命必备～一瓶不到1块钱，宿舍囤一箱实现AD钙奶自由，好喝不贵还能补钙，课间来一瓶秒回血！宿舍拼单更划算，速冲！🏃♀️💨', 1, '2026-07-06 17:48:48', '2026-07-06 23:05:34');
INSERT INTO `product` VALUES (28, '透明多层抽屉式化妆品收纳盒', 3, '生活用品', 30.00, 18, '/images/product_6409016d.png?t=1783331348482', '开学囤货党必入！✨这款透明收纳盒太懂宿舍党了~ 三层抽屉轻松分类口红、面膜和小样，找东西一眼搞定，再也不用翻箱倒柜！防尘设计还能保持桌面清清爽爽，30元拯救杂乱化妆台，性价比绝了💄快来把桌面变整齐吧~', 1, '2026-07-06 17:49:33', '2026-07-07 16:47:56');
INSERT INTO `product` VALUES (29, '小米67W充电器套装', 1, '电子产品', 30.00, 8, '/images/product_d93c4b1f.png?t=1783331409095', '大学生们，手机电量告急？😟 小米67W充电器套装，67W疾速快充，半小时充一半电，多设备兼容，宿舍图书馆随身用超方便！😎 仅需¥30，性价比炸裂，快来解锁充电自由，告别电量焦虑！🔋⚡💰', 1, '2026-07-06 17:51:59', '2026-07-06 17:51:59');
INSERT INTO `product` VALUES (30, '心相印抽纸加厚3层100抽卫生纸', 3, '生活用品', 39.00, 19, '/images/product_4ec3cbab.png?t=1783336080877', '📚宿舍囤货必备！心相印加厚抽纸，3层厚实超耐用，擦手擦桌都安心～关键是100抽一包超大容量，39元到手一整箱，性价比绝了👍用完再也不怕纸荒，快来一起拼团冲冲冲！💖', 1, '2026-07-06 19:08:21', '2026-07-06 23:12:58');
INSERT INTO `product` VALUES (31, '锈钢泡面碗加餐具', 3, '生活用品', 10.00, 14, '/images/product_556f793f.png?t=1783336196849', '泡面神器驾到🍜！不锈钢材质健康又耐造，比塑料碗好洗一百倍～自带叉勺超贴心，宿舍/图书馆追剧写作业随时开动✨！10块还包邮，这性价比闭眼入不心疼🛒！', 1, '2026-07-06 19:10:23', '2026-07-07 15:44:56');
INSERT INTO `product` VALUES (32, '言仓水豚噜噜联名雪糕马克杯陶瓷', 3, '生活用品', 20.00, 17, '/images/product_858404f2.png?t=1783336243721', '水豚噜噜联名马克杯来啦！呆萌颜值直接承包你的课桌✨ 只要¥20就能get的快乐——夏日冰饮、冬日热可可统统hold住，萌系治愈还能提醒你多喝水💪 学生党闭眼入的性价比之王，快让这只水豚陪你写作业吧~ ❄️🔥❤️', 1, '2026-07-06 19:11:13', '2026-07-07 16:47:56');
INSERT INTO `product` VALUES (33, '雨伞uv太阳伞', 3, '生活用品', 20.00, 20, '/images/product_2b04a83b.png?t=1783336538405', '姐妹们！夏天出门谁还没把防晒伞啊？☂️ 这把UV太阳伞，UPF50+高效防晒，晴天挡紫外线，雨天当雨伞，一伞两用太划算！20块钱搞定整个夏天的太阳自由😎 轻便好收纳，放包里不占地方，上体育课、去食堂、逛街随身带～这个价格闭眼冲就对了！🌤️', 1, '2026-07-06 19:15:56', '2026-07-06 21:53:18');
INSERT INTO `product` VALUES (34, '自热火锅重庆风味', 3, '生活用品', 40.00, 11, '/images/product_a4b074b9.png?t=1783404052552', '宿舍追剧必备神器🔥不用电不用火，随时随地开吃！重庆正宗麻辣风味，肉菜粉条管够🍲 一盒才40块，比外卖便宜还能吃到撑，复习周囤几盒太香了！自己加点青菜更绝~', 1, '2026-07-07 14:02:36', '2026-07-07 15:40:56');
INSERT INTO `product` VALUES (37, '李宁紧身衣短袖男健身速干T恤篮球', 3, '生活用品', 23.00, 32, '/images/product_45598372.png?t=1783414280991', '打球缺件吸汗战袍？李宁这件速干T恤了解一下！🏃‍♂️ 健身、跑步、篮球场随便造，冰丝面料透气不闷汗，贴身剪裁显身材，关键是——只要23元！💰 轻松解锁运动新皮肤，冲就完了！💪🔥', 1, '2026-07-07 16:51:52', '2026-07-07 16:52:16');
INSERT INTO `product` VALUES (38, '正版泰国黄油小熊', 5, '运动户外', 50.00, 6, '/images/product_1135bdd5.png?t=1783494151278', '救命！这只软萌的正版泰国黄油小熊也太可爱了吧🐻✨ 做背包挂件或钥匙扣都超适合！上课、跑步、露营随身带，毛绒手感解压满分～价格才50元，正版白菜价，学生党闭眼入不心疼💸', 1, '2026-07-08 15:03:07', '2026-07-08 15:03:07');
INSERT INTO `product` VALUES (39, 'ad钙牛奶', 3, '生活用品', 60.00, 24, '/images/product_168b6726.png?t=1783496999133', '熬夜赶作业、早餐来不及？AD钙奶经典回归啦！✨ 怀旧配方，补钙又解馋，宿舍囤一箱能喝超久～早餐搭档/下午茶必备，60元直接承包你的快乐水预算！💰 快来带走童年回忆，做最懂省钱的崽～🥛', 1, '2026-07-08 15:54:31', '2026-07-08 15:54:31');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '刘人源', '123456', '刘人源', '468402880@qq.com', '16623020269', '/images/avatar_1_7cc95f0d.jpg', 1, '2026-06-11 21:36:28', '2026-07-06 11:59:59');
INSERT INTO `user` VALUES (2, '欧阳马力', '1234567', '欧阳马力', '', '18607959556', NULL, 1, '2026-06-12 09:37:29', '2026-06-12 14:08:36');

SET FOREIGN_KEY_CHECKS = 1;
