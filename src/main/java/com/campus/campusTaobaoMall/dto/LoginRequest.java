package com.campus.campusTaobaoMall.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
    private String phone;           // 手机号
    private String code;            // 验证码
    private String loginType;       // 登录类型：password-密码登录，sms-短信验证码登录
}
