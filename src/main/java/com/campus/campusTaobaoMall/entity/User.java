package com.campus.campusTaobaoMall.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类 —— 对应数据库 user 表
 *
 * 【作用】封装用户信息，MyBatis查询结果自动映射到该对象
 *
 * 【表结构】
 * CREATE TABLE user (
 *   id          BIGINT PRIMARY KEY AUTO_INCREMENT,
 *   username    VARCHAR(50) UNIQUE NOT NULL,   -- 用户名（唯一）
 *   password    VARCHAR(255) NOT NULL,          -- 密码
 *   real_name   VARCHAR(50),                    -- 真实姓名
 *   email       VARCHAR(100),                   -- 邮箱
 *   phone       VARCHAR(20),                    -- 手机号
 *   avatar      VARCHAR(255),                   -- 头像URL
 *   status      INT DEFAULT 1,                  -- 状态：1正常 0禁用
 *   create_time DATETIME,                       -- 注册时间
 *   update_time DATETIME                        -- 更新时间
 * );
 *
 * 【与其他表的关系】
 * - 一个User可以发布多个Product（一对多）
 * - 一个User有一个Cart（一对一）
 * - 一个User可以有多个Orders（一对多）
 */
@Data
public class User {

    /** 用户ID，主键自增 */
    private Long id;

    /** 用户名，唯一标识，登录时使用 */
    private String username;

    /** 密码，存储加密后的密文 */
    private String password;

    /** 真实姓名，用于显示 */
    private String realName;

    /** 电子邮箱 */
    private String email;

    /** 手机号码，可用于手机号登录 */
    private String phone;

    /** 头像图片URL，如"/images/avatar_001.jpg" */
    private String avatar;

    /**
     * 账号状态
     * - 1：正常（默认）
     * - 0：禁用（管理员可禁用违规用户）
     */
    private Integer status;

    /** 注册时间，数据库自动填充 */
    private LocalDateTime createTime;

    /** 最后更新时间 */
    private LocalDateTime updateTime;
}
