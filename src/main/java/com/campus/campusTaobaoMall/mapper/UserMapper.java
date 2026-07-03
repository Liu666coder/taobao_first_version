package com.campus.campusTaobaoMall.mapper;

import com.campus.campusTaobaoMall.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    @Select("SELECT * FROM user WHERE phone = #{phone}")
    User findByPhone(@Param("phone") String phone);

    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(@Param("id") Long id);

    @Select("SELECT * FROM user ORDER BY create_time DESC")
    List<User> findAll();

    @Select("<script>" +
            "SELECT * FROM user WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (username LIKE CONCAT('%',#{keyword},'%') OR real_name LIKE CONCAT('%',#{keyword},'%') OR phone LIKE CONCAT('%',#{keyword},'%'))" +
            "</if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    List<User> search(@Param("keyword") String keyword);

    @Insert("INSERT INTO user(username, password, real_name, email, phone, avatar, status) " +
            "VALUES(#{username}, #{password}, #{realName}, #{email}, #{phone}, #{avatar}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("<script>" +
            "UPDATE user SET " +
            "<if test='realName != null'>real_name=#{realName},</if>" +
            "<if test='email != null'>email=#{email},</if>" +
            "<if test='phone != null'>phone=#{phone},</if>" +
            "<if test='avatar != null'>avatar=#{avatar},</if>" +
            "<if test='status != null'>status=#{status},</if>" +
            "update_time=NOW() WHERE id=#{id}" +
            "</script>")
    int update(User user);

    @Update("UPDATE user SET password=#{password} WHERE id=#{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Delete("DELETE FROM user WHERE id=#{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM user")
    int count();
}
