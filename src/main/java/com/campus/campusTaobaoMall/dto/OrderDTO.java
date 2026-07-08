package com.campus.campusTaobaoMall.dto;

import lombok.Data;
import java.util.List;

/**
 * 订单数据传输对象（DTO）
 *
 * 【作用】封装前端提交的订单创建数据
 *
 * 【使用场景】
 * - 创建订单：POST /api/orders
 *
 * 【前端发送的JSON示例】
 * {
 *   "cartIds": [1, 2, 3]
 * }
 *
 * 【说明】
 * 当前项目中OrderController直接使用Map接收参数，该DTO作为备用方案定义
 * 实际创建订单时，前端传入购物车ID列表（cartIds），后端根据购物车信息创建订单
 */
@Data
public class OrderDTO {

    /** 用户ID，由后端从JWT Token中解析获取，前端不需要传 */
    private Long userId;

    /** 购物车项ID列表，指定要结算的购物车商品 */
    private List<Long> cartIds;

    /**
     * 内部类：购物车项信息（备用方案）
     *
     * 如果前端需要直接传商品信息而非购物车ID，可以使用此结构
     * 当前项目未使用此内部类，保留作为扩展预留
     */
    @Data
    public static class CartItemDTO {

        /** 商品ID */
        private Long productId;

        /** 购买数量 */
        private Integer quantity;
    }
}
