package com.campus.campusTaobaoMall.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 管理员实体类 —— 对应数据库 admin 表
 *
 * 【作用】封装管理员信息，与User分开存储，独立管理
 *
 * 【表结构】
 * CREATE TABLE admin (
 *   id          BIGINT PRIMARY KEY AUTO_INCREMENT,
 *   username    VARCHAR(50) UNIQUE NOT NULL,  -- 管理员用户名（唯一）
 *   password    VARCHAR(255) NOT NULL,         -- 密码
 *   real_name   VARCHAR(50),                   -- 真实姓名
 *   email       VARCHAR(100),                  -- 邮箱
 *   phone       VARCHAR(20),                   -- 手机号
 *   avatar      VARCHAR(255),                  -- 头像URL
 *   role        VARCHAR(50),                   -- 角色（见下方说明）
 *   status      INT DEFAULT 1,                 -- 状态：1正常 0禁用
 *   create_time DATETIME,                      -- 创建时间
 *   update_time DATETIME                       -- 更新时间
 * );
 *
 * 【角色权限体系】
 * - SYSTEM_ADMIN：系统管理员，拥有所有权限（增删改管理员、重置密码等）
 * - MARKETING_MANAGER：营销经理，可管理用户和订单
 * - PRODUCT_MANAGER：商品管理员，可管理商品和分类
 *
 * 【与User表的区别】
 * - User表存储普通用户（买家/卖家）
 * - Admin表存储管理员（后台操作人员）
 * - 两者独立存储，通过role字段区分权限级别
 */
@Data
public class Admin {

    /** 管理员ID，主键自增 */
    private Long id;

    /** 管理员用户名，唯一标识，登录时使用 */
    private String username;

    /** 密码，存储加密后的密文 */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 电子邮箱 */
    private String email;

    /** 手机号码 */
    private String phone;

    /** 头像图片URL */
    private String avatar;

    /**
     * 管理员角色，决定权限范围
     * - "SYSTEM_ADMIN"：系统管理员（最高权限）
     * - "MARKETING_MANAGER"：营销经理（管理用户和订单）
     * - "PRODUCT_MANAGER"：商品管理员（管理商品和分类）
     */
    private String role;

    /**
     * 账号状态
     * - 1：正常（默认）
     * - 0：禁用
     */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 最后更新时间 */
    private LocalDateTime updateTime;
}
