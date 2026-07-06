package com.campus.campusTaobaoMall.mapper;

import com.campus.campusTaobaoMall.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Select("SELECT * FROM category WHERE id = #{id}")
    Category findById(@Param("id") Long id);

    @Select("SELECT * FROM category ORDER BY id ASC")
    List<Category> findAll();

    @Select("SELECT * FROM category WHERE status = 1 ORDER BY id ASC")
    List<Category> findEnabled();

    @Insert("INSERT INTO category(name, description, status) VALUES(#{name}, #{description}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

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

    @Delete("DELETE FROM category WHERE id=#{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM category")
    int count();
}
