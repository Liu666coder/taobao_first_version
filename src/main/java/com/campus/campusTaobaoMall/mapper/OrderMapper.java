package com.campus.campusTaobaoMall.mapper;

import com.campus.campusTaobaoMall.entity.Orders;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderMapper {

    @Select("SELECT o.*, u.username FROM orders o LEFT JOIN user u ON o.user_id = u.id WHERE o.id = #{id}")
    Orders findById(@Param("id") Long id);

    @Select("SELECT * FROM orders WHERE order_no = #{orderNo}")
    Orders findByOrderNo(@Param("orderNo") String orderNo);

    @Select("SELECT * FROM orders WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Orders> findByUserId(@Param("userId") Long userId);

    @Select("<script>" +
            "SELECT o.*, u.username FROM orders o " +
            "LEFT JOIN user u ON o.user_id = u.id " +
            "WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (o.order_no LIKE CONCAT('%',#{keyword},'%') OR u.username LIKE CONCAT('%',#{keyword},'%'))" +
            "</if>" +
            "<if test='status != null'>" +
            "AND o.status = #{status}" +
            "</if>" +
            "ORDER BY o.create_time DESC" +
            "</script>")
    List<Orders> searchAdmin(@Param("keyword") String keyword, @Param("status") Integer status);

    @Insert("INSERT INTO orders(order_no, user_id, total_amount, status) " +
            "VALUES(#{orderNo}, #{userId}, #{totalAmount}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Orders orders);

    @Update("UPDATE orders SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Delete("DELETE FROM orders WHERE id=#{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM orders")
    int count();
}
