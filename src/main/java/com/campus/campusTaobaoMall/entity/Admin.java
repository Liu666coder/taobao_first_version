package com.campus.campusTaobaoMall.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Admin {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    private String avatar;
    private String role;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
