package com.campus.campusTaobaoMall.mapper;

import com.campus.campusTaobaoMall.entity.Admin;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AdminMapper {

    @Select("SELECT * FROM admin WHERE username = #{username}")
    Admin findByUsername(@Param("username") String username);

    @Select("SELECT * FROM admin WHERE id = #{id}")
    Admin findById(@Param("id") Long id);

    @Select("SELECT * FROM admin ORDER BY create_time DESC")
    List<Admin> findAll();

    @Select("<script>" +
            "SELECT * FROM admin WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (username LIKE CONCAT('%',#{keyword},'%') OR real_name LIKE CONCAT('%',#{keyword},'%'))" +
            "</if>" +
            "<if test='role != null and role != \"\"'>" +
            "AND role = #{role}" +
            "</if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    List<Admin> search(@Param("keyword") String keyword, @Param("role") String role);

    @Insert("INSERT INTO admin(username, password, real_name, role, status) " +
            "VALUES(#{username}, #{password}, #{realName}, #{role}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Admin admin);

    @Update("<script>" +
            "UPDATE admin SET " +
            "<if test='realName != null'>real_name=#{realName},</if>" +
            "<if test='role != null'>role=#{role},</if>" +
            "<if test='status != null'>status=#{status},</if>" +
            "update_time=NOW() WHERE id=#{id}" +
            "</script>")
    int update(Admin admin);

    @Update("UPDATE admin SET password=#{password} WHERE id=#{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Delete("DELETE FROM admin WHERE id=#{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM admin")
    int count();
}
