package com.campus.campusTaobaoMall.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItem {
    private Long id;
    private Long orderId;
    private Long userId;
    private String username;
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private String productImage;
}
