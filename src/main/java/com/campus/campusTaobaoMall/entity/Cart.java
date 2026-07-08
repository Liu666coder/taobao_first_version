package com.campus.campusTaobaoMall.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车实体类 —— 对应数据库 cart 表
 *
 * 【作用】封装用户的购物车记录
 *
 * 【表结构】
 * CREATE TABLE cart (
 *   id           BIGINT PRIMARY KEY AUTO_INCREMENT,
 *   user_id      BIGINT NOT NULL,          -- 用户ID，关联user表
 *   username     VARCHAR(50),              -- 用户名（冗余存储）
 *   product_id   BIGINT NOT NULL,          -- 商品ID，关联product表
 *   product_name VARCHAR(100),             -- 商品名称（冗余存储，避免联表查询）
 *   quantity     INT DEFAULT 1,            -- 购买数量
 *   create_time  DATETIME                  -- 添加时间
 * );
 *
 * 【与其他表的关系】
 * - 一个Cart记录属于一个User（多对一）
 * - 一个Cart记录对应一个Product（多对一）
 *
 * 【业务逻辑】
 * - 同一用户对同一商品只能有一条购物车记录
 * - 重复添加时累加数量而非新增记录
 */
@Data
public class Cart {

    /** 购物车记录ID，主键自增 */
    private Long id;

    /** 用户ID，关联user表 */
    private Long userId;

    /** 用户名，冗余存储，避免联表查询user表 */
    private String username;

    /** 商品ID，关联product表 */
    private Long productId;

    /** 商品名称，冗余存储，避免联表查询product表 */
    private String productName;

    /** 购买数量，默认1 */
    private Integer quantity;

    /** 添加到购物车的时间 */
    private LocalDateTime createTime;

    // ========== 以下字段通过联查product表获取，不存储在cart表中 ==========

    /** 商品单价，联查product表获取，用于显示小计金额 */
    private BigDecimal productPrice;

    /** 商品图片URL，联查product表获取，用于显示商品缩略图 */
    private String productImage;

    /** 商品库存，联查product表获取，用于校验库存是否充足 */
    private Integer productStock;
}
