package com.campus.campusTaobaoMall.mapper;

import com.campus.campusTaobaoMall.entity.Admin;
import org.apache.ibatis.annotations.*;  // MyBatis注解

import java.util.List;

/**
 * 管理员Mapper — 负责admin表的数据库操作
 */
@Mapper
public interface AdminMapper {

    // 根据用户名查询（管理员登录时用）
    @Select("SELECT * FROM admin WHERE username = #{username}")
    Admin findByUsername(@Param("username") String username);

    // 根据id查询
    @Select("SELECT * FROM admin WHERE id = #{id}")
    Admin findById(@Param("id") Long id);

    // 后台搜索：支持按用户名、真实姓名模糊搜索 + 按角色筛选
    @Select("<script>" +
            "SELECT * FROM admin WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +  // keyword不为空才拼接
            "AND (username LIKE CONCAT('%',#{keyword},'%') " +  // 匹配用户名
            "OR real_name LIKE CONCAT('%',#{keyword},'%'))" +   // 或匹配真实姓名
            "</if>" +
            "<if test='role != null and role != \"\"'>" +        // role不为空才拼接
            "AND role = #{role}" +                               // 按角色筛选（SYSTEM_ADMIN/MARKETING_MANAGER/PRODUCT_MANAGER）
            "</if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    List<Admin> search(@Param("keyword") String keyword, @Param("role") String role);

    // 插入管理员
    @Insert("INSERT INTO admin(username, password, real_name, email, phone, avatar, role, status) " +
            "VALUES(#{username}, #{password}, #{realName}, #{email}, #{phone}, #{avatar}, #{role}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")  // 插入后自动把id设回对象
    int insert(Admin admin);

    // 更新管理员信息（动态SQL：只更新不为null的字段）
    @Update("<script>" +
            "UPDATE admin SET " +
            "<if test='realName != null'>real_name=#{realName},</if>" +
            "<if test='email != null'>email=#{email},</if>" +
            "<if test='phone != null'>phone=#{phone},</if>" +
            "<if test='avatar != null'>avatar=#{avatar},</if>" +
            "<if test='role != null'>role=#{role},</if>" +
            "<if test='status != null'>status=#{status},</if>" +
            "update_time=NOW() WHERE id=#{id}" +               // 同时更新修改时间
            "</script>")
    int update(Admin admin);

    // 单独修改密码
    @Update("UPDATE admin SET password=#{password} WHERE id=#{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    // 根据id删除管理员
    @Delete("DELETE FROM admin WHERE id=#{id}")
    int deleteById(@Param("id") Long id);
}
