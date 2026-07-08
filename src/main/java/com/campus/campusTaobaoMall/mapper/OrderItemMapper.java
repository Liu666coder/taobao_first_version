package com.campus.campusTaobaoMall.mapper;

import com.campus.campusTaobaoMall.entity.OrderItem;
import org.apache.ibatis.annotations.*;  // MyBatis注解

import java.util.List;

/**
 * 订单明细Mapper — 负责order_item表的数据库操作
 * 一个订单可以包含多个商品，每个商品就是一条order_item记录
 */
@Mapper
public interface OrderItemMapper {

    // 查询订单的所有商品明细（联表获取商品图片）
    @Select("SELECT oi.*, p.image as product_image FROM order_item oi " +
            "LEFT JOIN product p ON oi.product_id = p.id " +
            "WHERE oi.order_id = #{orderId}")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

    // 插入订单明细（下单时保存商品快照：名称、价格、数量）
    @Insert("INSERT INTO order_item(order_id, user_id, username, product_id, product_name, price, quantity) " +
            "VALUES(#{orderId}, #{userId}, #{username}, #{productId}, #{productName}, #{price}, #{quantity})")
    @Options(useGeneratedKeys = true, keyProperty = "id")  // 插入后自动把id设回对象
    int insert(OrderItem orderItem);

    // 删除订单的所有明细（删除订单时级联删除）
    @Delete("DELETE FROM order_item WHERE order_id = #{orderId}")
    int deleteByOrderId(@Param("orderId") Long orderId);

    // 删除包含某商品的所有明细（商品下架时用）
    @Delete("DELETE FROM order_item WHERE product_id = #{productId}")
    int deleteByProductId(@Param("productId") Long productId);

    // 删除用户的所有订单明细（删除用户时级联删除）
    @Delete("DELETE FROM order_item WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
