package com.campus.campusTaobaoMall.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminLog {
    private Long id;
    private Long adminId;
    private String adminName;
    private String action;
    private String target;
    private String detail;
    private String ip;
    private LocalDateTime createTime;
    private Integer count; // 用于统计查询
}
