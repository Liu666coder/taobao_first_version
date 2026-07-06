package com.campus.campusTaobaoMall.mapper;

import com.campus.campusTaobaoMall.entity.Cart;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CartMapper {

    @Select("SELECT c.*, u.username, p.name as product_name, p.price as product_price, " +
            "p.image as product_image, p.stock as product_stock " +
            "FROM cart c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "LEFT JOIN product p ON c.product_id = p.id " +
            "WHERE c.user_id = #{userId} ORDER BY c.create_time DESC")
    List<Cart> findByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM cart WHERE user_id = #{userId} AND product_id = #{productId}")
    Cart findByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);

    @Select("SELECT * FROM cart WHERE id = #{id}")
    Cart findById(@Param("id") Long id);

    @Insert("INSERT INTO cart(user_id, username, product_id, product_name, quantity) " +
            "VALUES(#{userId}, #{username}, #{productId}, #{productName}, #{quantity})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Cart cart);

    @Update("UPDATE cart SET quantity = #{quantity} WHERE id = #{id}")
    int updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Delete("DELETE FROM cart WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Delete("DELETE FROM cart WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    @Delete("DELETE FROM cart WHERE product_id = #{productId}")
    int deleteByProductId(@Param("productId") Long productId);

    @Select("SELECT COUNT(*) FROM cart WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);
}
