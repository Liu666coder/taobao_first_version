package com.campus.campusTaobaoMall.entity;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 订单明细实体类 —— 对应数据库 order_item 表
 *
 * 【作用】存储订单中每个商品的详细信息
 *
 * 【表结构】
 * CREATE TABLE order_item (
 *   id            BIGINT PRIMARY KEY AUTO_INCREMENT,
 *   order_id      BIGINT NOT NULL,          -- 所属订单ID，关联order_info表
 *   user_id       BIGINT NOT NULL,          -- 用户ID（冗余存储）
 *   username      VARCHAR(50),              -- 用户名（冗余存储）
 *   product_id    BIGINT NOT NULL,          -- 商品ID，关联product表
 *   product_name  VARCHAR(100),             -- 商品名称（冗余存储，下单时快照）
 *   price         DECIMAL(10,2),            -- 下单时价格（快照，商品后续改价不影响历史订单）
 *   quantity      INT,                      -- 购买数量
 *   product_image VARCHAR(255)              -- 商品图片（冗余存储，下单时快照）
 * );
 *
 * 【为什么冗余存储？】
 * - product_name、price、product_image 在下单时快照存储
 * - 即使商品后来改名、改价、换图，历史订单仍然显示下单时的信息
 * - 避免联表查询，提升订单列表查询性能
 *
 * 【与其他表的关系】
 * - 一个OrderItem属于一个Orders（多对一）
 * - 一个OrderItem引用一个Product（多对一）
 */
@Data
public class OrderItem {

    /** 明细ID，主键自增 */
    private Long id;

    /** 所属订单ID，关联order_info表 */
    private Long orderId;

    /** 用户ID，冗余存储，方便查询 */
    private Long userId;

    /** 用户名，冗余存储，方便后台展示 */
    private String username;

    /** 商品ID，关联product表 */
    private Long productId;

    /** 商品名称，下单时快照存储，后续商品改名不影响历史订单 */
    private String productName;

    /** 下单时的商品价格，快照存储，后续商品改价不影响历史订单 */
    private BigDecimal price;

    /** 购买数量 */
    private Integer quantity;

    /** 商品图片URL，下单时快照存储 */
    private String productImage;
}
