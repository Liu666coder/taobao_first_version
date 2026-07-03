package com.campus.campusTaobaoMall.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Category {
    private Long id;
    private String name;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
}
