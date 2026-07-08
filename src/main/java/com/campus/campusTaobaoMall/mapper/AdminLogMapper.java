package com.campus.campusTaobaoMall.mapper;

import com.campus.campusTaobaoMall.entity.AdminLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AdminLogMapper {

    @Insert("INSERT INTO admin_log(admin_id, admin_name, action, target, detail, ip) " +
            "VALUES(#{adminId}, #{adminName}, #{action}, #{target}, #{detail}, #{ip})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AdminLog log);

    @Select("<script>" +
            "SELECT * FROM admin_log WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (admin_name LIKE CONCAT('%',#{keyword},'%') OR action LIKE CONCAT('%',#{keyword},'%') OR target LIKE CONCAT('%',#{keyword},'%') OR detail LIKE CONCAT('%',#{keyword},'%'))" +
            "</if>" +
            "<if test='adminId != null'>" +
            "AND admin_id = #{adminId}" +
            "</if>" +
            "<if test='action != null and action != \"\"'>" +
            "AND action = #{action}" +
            "</if>" +
            "ORDER BY create_time DESC" +
            " LIMIT #{limit} OFFSET #{offset}" +
            "</script>")
    List<AdminLog> search(@Param("keyword") String keyword,
                          @Param("adminId") Long adminId,
                          @Param("action") String action,
                          @Param("limit") int limit,
                          @Param("offset") int offset);

    @Select("<script>" +
            "SELECT COUNT(*) FROM admin_log WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (admin_name LIKE CONCAT('%',#{keyword},'%') OR action LIKE CONCAT('%',#{keyword},'%') OR target LIKE CONCAT('%',#{keyword},'%') OR detail LIKE CONCAT('%',#{keyword},'%'))" +
            "</if>" +
            "<if test='adminId != null'>" +
            "AND admin_id = #{adminId}" +
            "</if>" +
            "<if test='action != null and action != \"\"'>" +
            "AND action = #{action}" +
            "</if>" +
            "</script>")
    int count(@Param("keyword") String keyword,
              @Param("adminId") Long adminId,
              @Param("action") String action);

    @Select("SELECT * FROM admin_log ORDER BY create_time DESC LIMIT #{limit}")
    List<AdminLog> findRecent(@Param("limit") int limit);

    @Select("SELECT action, COUNT(*) as count FROM admin_log " +
            "WHERE create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
            "GROUP BY action ORDER BY count DESC")
    List<AdminLog> getActionStats();

    @Select("SELECT admin_name, COUNT(*) as count FROM admin_log " +
            "WHERE create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
            "GROUP BY admin_name ORDER BY count DESC")
    List<AdminLog> getAdminStats();

    @Delete("DELETE FROM admin_log WHERE create_time < DATE_SUB(NOW(), INTERVAL #{days} DAY)")
    int deleteOldLogs(@Param("days") int days);
}
