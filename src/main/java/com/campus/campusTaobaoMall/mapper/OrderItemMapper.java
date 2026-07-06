package com.campus.campusTaobaoMall.mapper;

import com.campus.campusTaobaoMall.entity.OrderItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderItemMapper {

    @Select("SELECT oi.*, p.image as product_image FROM order_item oi " +
            "LEFT JOIN product p ON oi.product_id = p.id " +
            "WHERE oi.order_id = #{orderId}")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

    @Insert("INSERT INTO order_item(order_id, user_id, username, product_id, product_name, price, quantity) " +
            "VALUES(#{orderId}, #{userId}, #{username}, #{productId}, #{productName}, #{price}, #{quantity})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderItem orderItem);

    @Delete("DELETE FROM order_item WHERE order_id = #{orderId}")
    int deleteByOrderId(@Param("orderId") Long orderId);

    @Delete("DELETE FROM order_item WHERE product_id = #{productId}")
    int deleteByProductId(@Param("productId") Long productId);

    @Delete("DELETE FROM order_item WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
