package com.campus.campusTaobaoMall.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体类 —— 对应数据库 order_info 表
 *
 * 【作用】封装订单主信息（订单号、金额、状态等）
 *
 * 【表结构】
 * CREATE TABLE order_info (
 *   id           BIGINT PRIMARY KEY AUTO_INCREMENT,
 *   order_no     VARCHAR(50) UNIQUE NOT NULL,  -- 订单编号（唯一）
 *   user_id      BIGINT NOT NULL,              -- 下单用户ID
 *   total_amount DECIMAL(10,2) NOT NULL,       -- 订单总金额
 *   status       INT DEFAULT 0,                -- 订单状态
 *   create_time  DATETIME                      -- 下单时间
 * );
 *
 * 【订单状态流转】
 *   0（待付款）→ 付款 → 1（已付款/待发货）→ 发货 → 2（已发货）→ 确认收货 → 3（已完成）
 *   0（待付款）→ 取消 → 删除订单
 *
 * 【与其他表的关系】
 * - 一个Orders属于一个User（多对一）
 * - 一个Orders包含多个OrderItem（一对多）
 */
@Data
public class Orders {

    /** 订单ID，主键自增 */
    private Long id;

    /** 订单编号，格式："ORD" + 时间戳 + 随机8位，如"ORD20260701120000A1B2C3D4" */
    private String orderNo;

    /** 下单用户ID，关联user表 */
    private Long userId;

    /** 订单总金额，由所有订单明细的小计金额累加得出 */
    private BigDecimal totalAmount;

    /**
     * 订单状态
     * - 0：待付款（用户刚创建订单）
     * - 1：已付款/待发货（用户已付款，等待卖家发货）
     * - 2：已发货（卖家已发货，等待用户确认收货）
     * - 3：已完成（用户确认收货，交易完成）
     */
    private Integer status;

    /** 下单时间 */
    private LocalDateTime createTime;

    // ========== 以下字段不在order_info表中，通过联查order_item表获取 ==========

    /** 订单明细列表，包含该订单中所有商品的信息 */
    private List<OrderItem> items;

    /** 用户名，联查user表获取，用于后台订单管理展示 */
    private String username;
}
