package com.campus.campusTaobaoMall.mapper;

import com.campus.campusTaobaoMall.entity.Orders;
import org.apache.ibatis.annotations.*;  // MyBatis注解

import java.util.List;

/**
 * 订单Mapper — 负责orders表的数据库操作
 */
@Mapper
public interface OrderMapper {

    // 根据id查询订单（联表查询获取用户名）
    @Select("SELECT o.*, u.username FROM orders o LEFT JOIN user u ON o.user_id = u.id WHERE o.id = #{id}")
    Orders findById(@Param("id") Long id);

    // 查询用户的所有订单，按创建时间倒序
    @Select("SELECT * FROM orders WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Orders> findByUserId(@Param("userId") Long userId);

    // 后台搜索：支持按订单号或用户名模糊搜索 + 按状态筛选
    @Select("<script>" +
            "SELECT o.*, u.username FROM orders o " +
            "LEFT JOIN user u ON o.user_id = u.id " +
            "WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +  // keyword不为空才拼接
            "AND (o.order_no LIKE CONCAT('%',#{keyword},'%') " + // 匹配订单号
            "OR u.username LIKE CONCAT('%',#{keyword},'%'))" +   // 或匹配用户名
            "</if>" +
            "<if test='status != null'>" +                        // status不为空才拼接
            "AND o.status = #{status}" +                          // 按状态筛选
            "</if>" +
            "ORDER BY o.create_time DESC" +
            "</script>")
    List<Orders> searchAdmin(@Param("keyword") String keyword, @Param("status") Integer status);

    // 插入订单
    @Insert("INSERT INTO orders(order_no, user_id, total_amount, status) " +
            "VALUES(#{orderNo}, #{userId}, #{totalAmount}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")  // 插入后自动把id设回对象
    int insert(Orders orders);

    // 更新订单状态（0待付款→1已付款→2已发货→3已完成）
    @Update("UPDATE orders SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    // 根据id删除订单
    @Delete("DELETE FROM orders WHERE id=#{id}")
    int deleteById(@Param("id") Long id);

    // 删除用户的所有订单（删除用户时级联删除）
    @Delete("DELETE FROM orders WHERE user_id=#{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
