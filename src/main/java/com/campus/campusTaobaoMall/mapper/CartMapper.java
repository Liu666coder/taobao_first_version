package com.campus.campusTaobaoMall.mapper;

import com.campus.campusTaobaoMall.entity.Cart;
import org.apache.ibatis.annotations.*;  // MyBatis注解

import java.util.List;

/**
 * 购物车Mapper — 负责cart表的数据库操作
 */
@Mapper
public interface CartMapper {

    // 查询用户购物车（联表查询：同时获取用户名和商品信息）
    // LEFT JOIN user：关联用户表获取username
    // LEFT JOIN product：关联商品表获取价格、图片、库存
    @Select("SELECT c.*, u.username, p.name as product_name, p.price as product_price, " +
            "p.image as product_image, p.stock as product_stock " +
            "FROM cart c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "LEFT JOIN product p ON c.product_id = p.id " +
            "WHERE c.user_id = #{userId} ORDER BY c.create_time DESC")
    List<Cart> findByUserId(@Param("userId") Long userId);

    // 查询购物车中是否已有某商品（添加购物车时判断是否合并数量）
    @Select("SELECT * FROM cart WHERE user_id = #{userId} AND product_id = #{productId}")
    Cart findByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);

    // 根据购物车id查询
    @Select("SELECT * FROM cart WHERE id = #{id}")
    Cart findById(@Param("id") Long id);

    // 插入购物车记录
    @Insert("INSERT INTO cart(user_id, username, product_id, product_name, quantity) " +
            "VALUES(#{userId}, #{username}, #{productId}, #{productName}, #{quantity})")
    @Options(useGeneratedKeys = true, keyProperty = "id")  // 插入后自动把id设回对象
    int insert(Cart cart);

    // 更新购物车数量（修改数量时用）
    @Update("UPDATE cart SET quantity = #{quantity} WHERE id = #{id}")
    int updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

    // 删除购物车中的某条记录
    @Delete("DELETE FROM cart WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    // 删除用户的所有购物车记录（下单成功后清空购物车用）
    @Delete("DELETE FROM cart WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    // 删除所有包含某商品的购物车记录（商品下架时用）
    @Delete("DELETE FROM cart WHERE product_id = #{productId}")
    int deleteByProductId(@Param("productId") Long productId);

    // 统计用户购物车商品数量（购物车角标显示用）
    @Select("SELECT COUNT(*) FROM cart WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);
}
