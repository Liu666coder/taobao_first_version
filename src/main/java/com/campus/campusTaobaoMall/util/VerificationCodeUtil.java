package com.campus.campusTaobaoMall.util;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证码工具类（内存存储，适合演示）
 * 生产环境建议使用Redis
 */
public class VerificationCodeUtil {

    // 存储验证码：key=手机号，value=验证码
    private static final ConcurrentHashMap<String, String> codeCache = new ConcurrentHashMap<>();

    // 验证码过期时间（毫秒）：5分钟
    private static final long EXPIRE_TIME = 5 * 60 * 1000;

    // 存储验证码生成时间
    private static final ConcurrentHashMap<String, Long> codeTimeCache = new ConcurrentHashMap<>();

    /**
     * 生成6位数字验证码
     */
    public static String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 存储验证码
     */
    public static void saveCode(String phone, String code) {
        codeCache.put(phone, code);
        codeTimeCache.put(phone, System.currentTimeMillis());
        System.out.println("验证码已生成 - 手机号: " + phone + ", 验证码: " + code);
    }

    /**
     * 验证验证码
     */
    public static boolean verifyCode(String phone, String code) {
        String cachedCode = codeCache.get(phone);
        Long createTime = codeTimeCache.get(phone);

        if (cachedCode == null || createTime == null) {
            return false;
        }

        // 检查是否过期
        if (System.currentTimeMillis() - createTime > EXPIRE_TIME) {
            codeCache.remove(phone);
            codeTimeCache.remove(phone);
            return false;
        }

        // 验证码匹配
        if (cachedCode.equals(code)) {
            // 验证成功后删除验证码
            codeCache.remove(phone);
            codeTimeCache.remove(phone);
            return true;
        }

        return false;
    }

    /**
     * 检查是否可以发送验证码（防止频繁发送）
     */
    public static boolean canSendCode(String phone) {
        Long lastSendTime = codeTimeCache.get(phone);
        if (lastSendTime == null) {
            return true;
        }
        // 60秒内不能重复发送
        return System.currentTimeMillis() - lastSendTime > 60000;
    }
}
