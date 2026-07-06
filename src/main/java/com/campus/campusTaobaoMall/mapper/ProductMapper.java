package com.campus.campusTaobaoMall.mapper;

import com.campus.campusTaobaoMall.entity.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductMapper {

    @Select("SELECT * FROM product WHERE id = #{id}")
    Product findById(@Param("id") Long id);

    @Select("SELECT * FROM product WHERE status = 1 ORDER BY create_time DESC")
    List<Product> findEnabled();

    @Select("<script>" +
            "SELECT p.* FROM product p " +
            "LEFT JOIN category c ON p.category_id = c.id " +
            "WHERE p.status = 1 " +
            "AND (c.id IS NULL OR c.status = 1) " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND p.name LIKE CONCAT('%',#{keyword},'%')" +
            "</if>" +
            "<if test='categoryId != null'>" +
            "AND p.category_id = #{categoryId}" +
            "</if>" +
            "ORDER BY p.create_time DESC" +
            "</script>")
    List<Product> search(@Param("keyword") String keyword, @Param("categoryId") Long categoryId);

    @Select("<script>" +
            "SELECT * FROM product WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND name LIKE CONCAT('%',#{keyword},'%')" +
            "</if>" +
            "<if test='categoryId != null'>" +
            "AND category_id = #{categoryId}" +
            "</if>" +
            "<if test='status != null'>" +
            "AND status = #{status}" +
            "</if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    List<Product> searchAdmin(@Param("keyword") String keyword,
                              @Param("categoryId") Long categoryId,
                              @Param("status") Integer status);

    @Insert("INSERT INTO product(name, category_id, category_name, price, stock, image, description, status) " +
            "VALUES(#{name}, #{categoryId}, #{categoryName}, #{price}, #{stock}, #{image}, #{description}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    @Update("<script>" +
            "UPDATE product SET " +
            "<if test='name != null'>name=#{name},</if>" +
            "<if test='categoryId != null'>category_id=#{categoryId},</if>" +
            "<if test='categoryName != null'>category_name=#{categoryName},</if>" +
            "<if test='price != null'>price=#{price},</if>" +
            "<if test='stock != null'>stock=#{stock},</if>" +
            "<if test='image != null'>image=#{image},</if>" +
            "<if test='description != null'>description=#{description},</if>" +
            "<if test='status != null'>status=#{status},</if>" +
            "update_time=NOW() WHERE id=#{id}" +
            "</script>")
    int update(Product product);

    @Delete("DELETE FROM product WHERE id=#{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM product WHERE category_id = #{categoryId}")
    int countByCategoryId(@Param("categoryId") Long categoryId);
}
