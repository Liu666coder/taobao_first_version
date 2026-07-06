package com.campus.campusTaobaoMall.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Cart {
    private Long id;
    private Long userId;
    private String username;
    private Long productId;
    private String productName;
    private Integer quantity;
    private LocalDateTime createTime;

    // 关联查询字段
    private BigDecimal productPrice;
    private String productImage;
    private Integer productStock;
}
