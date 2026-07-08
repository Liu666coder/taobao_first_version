package com.campus.campusTaobaoMall.mapper;

import com.campus.campusTaobaoMall.entity.Product;
import org.apache.ibatis.annotations.*;  // MyBatis注解：@Select/@Insert/@Update/@Delete

import java.util.List;

/**
 * 商品Mapper — 负责product表的数据库操作
 * @Mapper让Spring自动实现这个接口，你只需要写SQL
 */
@Mapper
public interface ProductMapper {

    // 根据id查询单个商品
    @Select("SELECT * FROM product WHERE id = #{id}")
    Product findById(@Param("id") Long id);

    // 查询所有上架商品（status=1表示上架），按创建时间倒序
    @Select("SELECT * FROM product WHERE status = 1 ORDER BY create_time DESC")
    List<Product> findEnabled();

    // 前台搜索：支持按关键词模糊搜索 + 按分类筛选（联表查询）
    @Select("<script>" +
            "SELECT p.* FROM product p " +
            "LEFT JOIN category c ON p.category_id = c.id " +
            "WHERE p.status = 1 " +
            "AND (c.id IS NULL OR c.status = 1) " +
            "<if test='keyword != null and keyword != \"\"'>" +  // 动态SQL：keyword不为空才拼接
            "AND p.name LIKE CONCAT('%',#{keyword},'%')" +     // 模糊匹配商品名
            "</if>" +
            "<if test='categoryId != null'>" +                  // categoryId不为空才拼接
            "AND p.category_id = #{categoryId}" +              // 按分类筛选
            "</if>" +
            "ORDER BY p.create_time DESC" +
            "</script>")
    List<Product> search(@Param("keyword") String keyword, @Param("categoryId") Long categoryId);

    // 后台搜索：支持关键词 + 分类 + 状态筛选（比前台多一个status参数）
    @Select("<script>" +
            "SELECT * FROM product WHERE 1=1 " +                // 1=1方便后面拼接AND
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND name LIKE CONCAT('%',#{keyword},'%')" +
            "</if>" +
            "<if test='categoryId != null'>" +
            "AND category_id = #{categoryId}" +
            "</if>" +
            "<if test='status != null'>" +                      // 后台可以按状态筛选
            "AND status = #{status}" +                          // 0=下架 1=上架
            "</if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    List<Product> searchAdmin(@Param("keyword") String keyword,
                              @Param("categoryId") Long categoryId,
                              @Param("status") Integer status);

    // 插入商品，返回影响行数（1=成功）
    @Insert("INSERT INTO product(name, category_id, category_name, price, stock, image, description, status) " +
            "VALUES(#{name}, #{categoryId}, #{categoryName}, #{price}, #{stock}, #{image}, #{description}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")  // 插入后自动把自增id设回对象
    int insert(Product product);

    // 更新商品（动态SQL：只更新不为null的字段）
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
            "update_time=NOW() WHERE id=#{id}" +               // 同时更新修改时间
            "</script>")
    int update(Product product);

    // 根据id删除商品
    @Delete("DELETE FROM product WHERE id=#{id}")
    int deleteById(@Param("id") Long id);

    // 统计某分类下的商品数量
    @Select("SELECT COUNT(*) FROM product WHERE category_id = #{categoryId}")
    int countByCategoryId(@Param("categoryId") Long categoryId);
}
