package com.campus.campusTaobaoMall.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderDTO {
    private Long userId;
    private List<CartItemDTO> items;

    @Data
    public static class CartItemDTO {
        private Long productId;
        private Integer quantity;
    }
}
