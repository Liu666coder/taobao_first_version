package com.campus.campusTaobaoMall.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 商品数据传输对象（DTO）
 *
 * 【作用】封装前端提交的商品添加/修改数据
 *
 * 【使用场景】
 * - 添加商品：POST /api/admin/products
 * - 修改商品：PUT /api/admin/products/{id}
 *
 * 【为什么不用Entity？】
 * Entity（Product）包含数据库字段（如createTime、updateTime），
 * 这些字段不应该由前端传入，所以用DTO只接收前端需要提交的字段
 *
 * 【前端发送的JSON示例】
 * {
 *   "name": "二手笔记本电脑",
 *   "categoryId": 1,
 *   "price": 2500.00,
 *   "stock": 1,
 *   "image": "/images/product_a1b2c3d4.jpg",
 *   "description": "九成新，无磕碰"
 * }
 *
 * 【转换流程】
 * 前端JSON → ProductDTO → Service层用BeanUtils.copyProperties转为Product Entity → 存入数据库
 */
@Data
public class ProductDTO {

    /** 商品ID，修改商品时必填，添加商品时为空（数据库自增） */
    private Long id;

    /** 商品名称，如"二手笔记本电脑" */
    private String name;

    /** 分类ID，关联category表，如1=电子产品 */
    private Long categoryId;

    /** 分类名称，如"电子产品"，由Service层根据categoryId自动补充 */
    private String categoryName;

    /** 商品价格，使用BigDecimal避免浮点精度问题 */
    private BigDecimal price;

    /** 库存数量 */
    private Integer stock;

    /** 商品图片URL，如"/images/product_a1b2c3d4.jpg" */
    private String image;

    /** 商品描述文案，可通过AI生成功能自动填充 */
    private String description;

    /** 商品状态：1=上架，0=下架，添加时默认为1（上架） */
    private Integer status;
}
