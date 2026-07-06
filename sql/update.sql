-- 安全更新数据库结构（保留现有数据）
-- 请在Navicat中执行此脚本

USE campus_taobao;

-- 1. 为商品表添加category_name字段
ALTER TABLE `product`
ADD COLUMN `category_name` VARCHAR(100) DEFAULT NULL COMMENT '分类名称（冗余字段）' AFTER `category_id`;

-- 2. 为购物车表添加username和product_name字段
ALTER TABLE `cart`
ADD COLUMN `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名（冗余字段）' AFTER `user_id`,
ADD COLUMN `product_name` VARCHAR(200) DEFAULT NULL COMMENT '商品名称（冗余字段）' AFTER `product_id`;

-- 3. 更新商品表中的category_name（根据category_id从category表获取）
UPDATE `product` p
INNER JOIN `category` c ON p.category_id = c.id
SET p.category_name = c.name;

-- 4. 更新购物车表中的username（根据user_id从user表获取）
UPDATE `cart` ca
INNER JOIN `user` u ON ca.user_id = u.id
SET ca.username = u.username;

-- 5. 更新购物车表中的product_name（根据product_id从product表获取）
UPDATE `cart` ca
INNER JOIN `product` p ON ca.product_id = p.id
SET ca.product_name = p.name;

-- 验证修改结果
SELECT '商品表category_name字段更新完成' AS message, COUNT(*) AS total FROM product WHERE category_name IS NOT NULL;
SELECT '购物车表username字段更新完成' AS message, COUNT(*) AS total FROM cart WHERE username IS NOT NULL;
SELECT '购物车表product_name字段更新完成' AS message, COUNT(*) AS total FROM cart WHERE product_name IS NOT NULL;

-- 6. 插入测试用户（如果不存在）
INSERT IGNORE INTO `user` (`username`, `password`, `real_name`, `phone`, `status`) VALUES
('zhangsan', '123456', '张三', '13800138001', 1),
('lisi', '123456', '李四', '13800138002', 1),
('wangwu', '123456', '王五', '13800138003', 1);

-- 7. 为订单详情表添加user_id和username字段
ALTER TABLE `order_item`
ADD COLUMN `user_id` BIGINT DEFAULT NULL COMMENT '用户ID' AFTER `order_id`,
ADD COLUMN `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名（冗余字段）' AFTER `user_id`;

-- 8. 更新订单详情表中的user_id和username（根据订单的user_id获取）
UPDATE `order_item` oi
INNER JOIN `orders` o ON oi.order_id = o.id
INNER JOIN `user` u ON o.user_id = u.id
SET oi.user_id = o.user_id, oi.username = u.username;
