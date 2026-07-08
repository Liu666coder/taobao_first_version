package com.campus.campusTaobaoMall.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类 —— 对应数据库 product 表
 *
 * 【作用】封装商品信息，MyBatis查询结果自动映射到该对象
 *
 * 【表结构】
 * CREATE TABLE product (
 *   id            BIGINT PRIMARY KEY AUTO_INCREMENT,
 *   name          VARCHAR(100) NOT NULL,         -- 商品名称
 *   category_id   BIGINT,                        -- 分类ID，关联category表
 *   category_name VARCHAR(50),                   -- 分类名称（冗余存储，避免联表查询）
 *   price         DECIMAL(10,2) NOT NULL,        -- 价格（Decimal避免浮点精度问题）
 *   stock         INT DEFAULT 0,                 -- 库存数量
 *   image         VARCHAR(255),                  -- 商品图片URL
 *   description   TEXT,                          -- 商品描述（可AI生成）
 *   status        INT DEFAULT 1,                 -- 状态：1上架 0下架
 *   create_time   DATETIME,                      -- 发布时间
 *   update_time   DATETIME                       -- 更新时间
 * );
 *
 * 【与其他表的关系】
 * - 多个Product属于一个Category（多对一）
 * - 一个Product可以被多个User加入Cart（多对多，通过cart表）
 * - 一个Product可以出现在多个OrderItem中（多对多，通过order_item表）
 */
@Data
public class Product {

    /** 商品ID，主键自增 */
    private Long id;

    /** 商品名称，如"二手笔记本电脑" */
    private String name;

    /** 分类ID，关联category表的id字段 */
    private Long categoryId;

    /** 分类名称，冗余存储，如"电子产品"，避免每次联表查询category表 */
    private String categoryName;

    /** 商品价格，使用BigDecimal确保金额计算精度 */
    private BigDecimal price;

    /** 库存数量，加入购物车和创建订单时需要校验 */
    private Integer stock;

    /** 商品主图URL，如"/images/product_a1b2c3d4.jpg" */
    private String image;

    /** 商品描述文案，可通过AI一键生成 */
    private String description;

    /**
     * 商品状态
     * - 1：上架（前台可见，默认值）
     * - 0：下架（前台不可见，管理员可操作）
     */
    private Integer status;

    /** 发布时间 */
    private LocalDateTime createTime;

    /** 最后更新时间 */
    private LocalDateTime updateTime;
}
