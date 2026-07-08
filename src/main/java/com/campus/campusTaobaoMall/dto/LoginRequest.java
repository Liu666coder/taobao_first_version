package com.campus.campusTaobaoMall.dto;

import lombok.Data;

/**
 * 登录请求数据传输对象（DTO）
 *
 * 【作用】封装前端登录表单提交的数据，作为Controller接口的参数
 *
 * 【使用场景】
 * 前端调用 POST /api/user/login 时，请求体中的JSON会被自动反序列化为该对象
 *
 * 【前端发送的JSON示例】
 * {
 *   "username": "zhangsan",
 *   "password": "123456",
 *   "loginType": "password"
 * }
 *
 * 【字段说明】
 * - username/password：密码登录时使用
 * - phone/code：手机号登录时使用（预留功能）
 * - loginType：区分登录方式，"password"=密码登录，"phone"=手机号登录
 */
@Data
public class LoginRequest {

    /** 用户名，密码登录时必填 */
    private String username;

    /** 密码，两种登录方式都必填 */
    private String password;

    /** 手机号，手机号登录时必填 */
    private String phone;

    /** 验证码，手机号登录时使用（预留功能，当前未实现） */
    private String code;

    /**
     * 登录类型，决定使用哪种登录方式
     * - "password"：用户名+密码登录（默认）
     * - "phone"：手机号+密码登录
     */
    private String loginType;
}
