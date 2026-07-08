package com.campus.campusTaobaoMall.mapper;

import com.campus.campusTaobaoMall.entity.User;
import org.apache.ibatis.annotations.*;  // MyBatis注解：@Select/@Insert/@Update/@Delete

import java.util.List;

/**
 * 用户Mapper — 负责user表的数据库操作
 */
@Mapper
public interface UserMapper {

    // 根据用户名查询（登录时用）
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    // 根据手机号查询（手机登录时用）
    @Select("SELECT * FROM user WHERE phone = #{phone}")
    User findByPhone(@Param("phone") String phone);

    // 根据id查询
    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(@Param("id") Long id);

    // 查询所有用户，按创建时间倒序
    @Select("SELECT * FROM user ORDER BY create_time DESC")
    List<User> findAll();

    // 后台搜索：支持按用户名、真实姓名、手机号模糊搜索
    @Select("<script>" +
            "SELECT * FROM user WHERE 1=1 " +                  // 1=1方便拼接AND
            "<if test='keyword != null and keyword != \"\"'>" + // keyword不为空才拼接
            "AND (username LIKE CONCAT('%',#{keyword},'%') " + // 匹配用户名
            "OR real_name LIKE CONCAT('%',#{keyword},'%') " +  // 或匹配真实姓名
            "OR phone LIKE CONCAT('%',#{keyword},'%'))" +      // 或匹配手机号
            "</if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    List<User> search(@Param("keyword") String keyword);

    // 插入用户（注册时用），返回影响行数
    @Insert("INSERT INTO user(username, password, real_name, email, phone, avatar, status) " +
            "VALUES(#{username}, #{password}, #{realName}, #{email}, #{phone}, #{avatar}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")  // 插入后自动把自增id设回对象
    int insert(User user);

    // 更新用户信息（动态SQL：只更新不为null的字段）
    @Update("<script>" +
            "UPDATE user SET " +
            "<if test='realName != null'>real_name=#{realName},</if>" +
            "<if test='email != null'>email=#{email},</if>" +
            "<if test='phone != null'>phone=#{phone},</if>" +
            "<if test='avatar != null'>avatar=#{avatar},</if>" +
            "<if test='status != null'>status=#{status},</if>" +
            "update_time=NOW() WHERE id=#{id}" +               // 同时更新修改时间
            "</script>")
    int update(User user);

    // 单独修改密码
    @Update("UPDATE user SET password=#{password} WHERE id=#{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    // 根据id删除用户
    @Delete("DELETE FROM user WHERE id=#{id}")
    int deleteById(@Param("id") Long id);
}
