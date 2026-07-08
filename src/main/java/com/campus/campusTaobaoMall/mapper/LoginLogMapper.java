package com.campus.campusTaobaoMall.mapper;

import com.campus.campusTaobaoMall.entity.LoginLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LoginLogMapper {

    @Insert("INSERT INTO login_log(user_type, username, login_ip, login_location, browser, os, status, message) " +
            "VALUES(#{userType}, #{username}, #{loginIp}, #{loginLocation}, #{browser}, #{os}, #{status}, #{message})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LoginLog loginLog);

    @Select("<script>" +
            "SELECT * FROM login_log WHERE 1=1 " +
            "<if test='userType != null and userType != \"\"'>" +
            "AND user_type = #{userType}" +
            "</if>" +
            "<if test='username != null and username != \"\"'>" +
            "AND username LIKE CONCAT('%',#{username},'%')" +
            "</if>" +
            "<if test='status != null'>" +
            "AND status = #{status}" +
            "</if>" +
            "ORDER BY login_time DESC " +
            "LIMIT #{limit} OFFSET #{offset}" +
            "</script>")
    List<LoginLog> search(@Param("userType") String userType,
                          @Param("username") String username,
                          @Param("status") Integer status,
                          @Param("limit") int limit,
                          @Param("offset") int offset);

    @Select("<script>" +
            "SELECT COUNT(*) FROM login_log WHERE 1=1 " +
            "<if test='userType != null and userType != \"\"'>" +
            "AND user_type = #{userType}" +
            "</if>" +
            "<if test='username != null and username != \"\"'>" +
            "AND username LIKE CONCAT('%',#{username},'%')" +
            "</if>" +
            "<if test='status != null'>" +
            "AND status = #{status}" +
            "</if>" +
            "</script>")
    int count(@Param("userType") String userType,
              @Param("username") String username,
              @Param("status") Integer status);

    @Select("SELECT * FROM login_log ORDER BY login_time DESC LIMIT #{limit}")
    List<LoginLog> findRecent(@Param("limit") int limit);

    @Select("SELECT username, COUNT(*) as count FROM login_log " +
            "WHERE status = 0 AND login_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
            "GROUP BY username HAVING count >= #{threshold} ORDER BY count DESC")
    List<LoginLog> getFailedLogins(@Param("threshold") int threshold);

    @Select("SELECT login_ip, COUNT(*) as count FROM login_log " +
            "WHERE status = 0 AND login_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR) " +
            "GROUP BY login_ip HAVING count >= #{threshold} ORDER BY count DESC")
    List<LoginLog> getSuspiciousIps(@Param("threshold") int threshold);

    @Select("SELECT DATE(login_time) as loginTime, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as count, " +
            "COUNT(*) as id FROM login_log " +
            "WHERE login_time >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
            "GROUP BY DATE(login_time) ORDER BY DATE(login_time)")
    List<LoginLog> getDailyStats();

    @Select("SELECT " +
            "COUNT(*) as id, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as count, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as status " +
            "FROM login_log WHERE login_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)")
    LoginLog getWeekSummary();

    @Delete("DELETE FROM login_log WHERE login_time < DATE_SUB(NOW(), INTERVAL #{days} DAY)")
    int deleteOldLogs(@Param("days") int days);
}
