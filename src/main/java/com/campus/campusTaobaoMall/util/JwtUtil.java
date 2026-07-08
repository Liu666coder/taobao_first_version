package com.campus.campusTaobaoMall.util;

import io.jsonwebtoken.Claims;       // JWT中的声明集，存Payload里的键值对
import io.jsonwebtoken.Jwts;         // JWT工具类，创建和解析Token
import io.jsonwebtoken.SignatureAlgorithm; // 签名算法（如HS256）

import java.util.Date;               // 日期类，设置Token的签发和过期时间

/**
 * JWT工具类 — 负责Token的生成、解析和验证
 *
 * Token的三段式结构：
 * header（算法）.payload（用户信息）.signature（签名防伪）
 *
 * 密钥（SECRET）只有服务器知道，用于签名和验证
 */
public class JwtUtil {
    // 密钥，用于签名和验证。只有服务器知道，泄露会导致所有Token被伪造
    private static final String SECRET = "campusTaobaoMallSecretKey2024";
    // Token有效期：24小时（单位：毫秒）。24×60×60×1000 = 86400000
    private static final Long EXPIRATION = 24 * 60 * 60 * 1000L;

    /**
     * 生成Token — 登录成功后调用
     * 把用户信息用密钥签名，生成一串防伪字符串
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色（USER/ADMIN）
     * @return JWT Token字符串（三段式）
     */
    public static String generateToken(Long userId, String username, String role) {
        return Jwts.builder()                          // 创建JWT构建器
                .setSubject(String.valueOf(userId))    // 把userId放到Subject字段
                .claim("username", username)           // 添加自定义声明：用户名
                .claim("role", role)                   // 添加自定义声明：角色
                .setIssuedAt(new Date())               // 设置签发时间：现在
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION)) // 设置过期时间：24小时后
                .signWith(SignatureAlgorithm.HS256, SECRET) // 用HS256算法+密钥签名（防篡改）
                .compact();                            // 压缩成字符串返回
    }

    /**
     * 解析Token — 提取用户信息
     * 验证签名是否有效，有效则返回Payload内容
     *
     * @param token JWT Token字符串
     * @return Claims对象（包含userId、username、role等）
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()          // 创建JWT解析器
                .setSigningKey(SECRET) // 设置密钥（和签名时用同一个）
                .parseClaimsJws(token) // 解析Token并验证签名（签名不对会抛异常）
                .getBody();            // 获取Payload部分（用户信息）
    }

    /**
     * 从Token中获取用户ID
     * 先解析Token，再取出Subject（就是userId）
     */
    public static Long getUserId(String token) {
        Claims claims = parseToken(token);      // 解析Token
        return Long.parseLong(claims.getSubject()); // Subject是字符串，转成Long数字
    }

    /**
     * 从Token中获取用户名
     */
    public static String getUsername(String token) {
        Claims claims = parseToken(token);      // 解析Token
        return (String) claims.get("username"); // 取出username字段
    }

    /**
     * 从Token中获取角色
     */
    public static String getRole(String token) {
        Claims claims = parseToken(token);      // 解析Token
        return (String) claims.get("role");     // 取出role字段
    }

    /**
     * 验证Token是否有效
     * 有效 = 签名正确 + 没有过期
     *
     * @return true=有效，false=无效（被篡改或过期）
     */
    public static boolean isTokenValid(String token) {
        try {
            Claims claims = parseToken(token);   // 解析Token（签名不对会抛异常）
            // 判断过期时间是否在"现在"之后（是则未过期）
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            // 解析失败（签名无效/Token格式错误），返回false
            return false;
        }
    }
}
