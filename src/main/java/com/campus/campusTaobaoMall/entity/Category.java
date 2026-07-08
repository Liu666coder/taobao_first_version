package com.campus.campusTaobaoMall.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 商品分类实体类 —— 对应数据库 category 表
 *
 * 【作用】管理商品的分类信息，支持按分类筛选商品
 *
 * 【表结构】
 * CREATE TABLE category (
 *   id          BIGINT PRIMARY KEY AUTO_INCREMENT,
 *   name        VARCHAR(50) UNIQUE NOT NULL,  -- 分类名称（唯一）
 *   description VARCHAR(200),                 -- 分类描述
 *   status      INT DEFAULT 1,                -- 状态：1启用 0禁用
 *   create_time DATETIME                      -- 创建时间
 * );
 *
 * 【预设分类示例】
 * - 电子产品（手机、电脑、平板等）
 * - 图书教材（课本、小说、考试资料等）
 * - 生活用品（衣物、鞋帽、日用品等）
 * - 运动装备（球类、健身器材等）
 * - 其他
 *
 * 【与其他表的关系】
 * - 一个Category下可以有多个Product（一对多）
 */
@Data
public class Category {

    /** 分类ID，主键自增 */
    private Long id;

    /** 分类名称，如"电子产品"，唯一不可重复 */
    private String name;

    /** 分类描述，如"手机、电脑、平板等数码产品" */
    private String description;

    /**
     * 分类状态
     * - 1：启用（前台可见，默认值）
     * - 0：禁用（前台不可见，管理员可操作）
     */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;
}
