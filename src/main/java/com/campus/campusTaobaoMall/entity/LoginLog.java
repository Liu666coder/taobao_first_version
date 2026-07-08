package com.campus.campusTaobaoMall.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LoginLog {
    private Long id;
    private String userType;
    private String username;
    private String loginIp;
    private String loginLocation;
    private String browser;
    private String os;
    private Integer status;
    private String message;
    private LocalDateTime loginTime;
    private Integer count; // 用于统计查询
}
