package com.campus.campusTaobaoMall.mapper;

import com.campus.campusTaobaoMall.entity.Category;
import org.apache.ibatis.annotations.*;  // MyBatis注解

import java.util.List;

/**
 * 分类Mapper — 负责category表的数据库操作
 */
@Mapper
public interface CategoryMapper {

    // 根据id查询分类
    @Select("SELECT * FROM category WHERE id = #{id}")
    Category findById(@Param("id") Long id);

    // 查询所有分类，按id升序（预设分类：电子产品、图书教材、生活用品等）
    @Select("SELECT * FROM category ORDER BY id ASC")
    List<Category> findAll();

    // 查询所有启用的分类（status=1），前台显示用
    @Select("SELECT * FROM category WHERE status = 1 ORDER BY id ASC")
    List<Category> findEnabled();

    // 插入分类
    @Insert("INSERT INTO category(name, description, status) VALUES(#{name}, #{description}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")  // 插入后自动把id设回对象
    int insert(Category category);

    // 更新分类（动态SQL：只更新不为null的字段）
    // <trim suffixOverrides=','> 去掉末尾多余的逗号
    @Update("<script>" +
            "UPDATE category SET " +
            "<trim suffixOverrides=','>" +
            "<if test='name != null'>name=#{name},</if>" +
            "<if test='description != null'>description=#{description},</if>" +
            "<if test='status != null'>status=#{status},</if>" +
            "</trim>" +
            "WHERE id=#{id}" +
            "</script>")
    int update(Category category);

    // 根据id删除分类
    @Delete("DELETE FROM category WHERE id=#{id}")
    int deleteById(@Param("id") Long id);
}
