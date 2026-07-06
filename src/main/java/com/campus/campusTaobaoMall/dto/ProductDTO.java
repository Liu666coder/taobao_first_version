package com.campus.campusTaobaoMall.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;
    private BigDecimal price;
    private Integer stock;
    private String image;
    private String description;
    private Integer status;
}
