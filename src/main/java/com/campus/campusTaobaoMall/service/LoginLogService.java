package com.campus.campusTaobaoMall.service;

import com.campus.campusTaobaoMall.entity.LoginLog;
import com.campus.campusTaobaoMall.mapper.LoginLogMapper;
import com.campus.campusTaobaoMall.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoginLogService {

    @Autowired
    private LoginLogMapper loginLogMapper;

    /**
     * 记录登录日志
     */
    public void log(String userType, String username, String ip, String browser, String os, int status, String message) {
        LoginLog loginLog = new LoginLog();
        loginLog.setUserType(userType);
        loginLog.setUsername(username);
        loginLog.setLoginIp(ip);
        loginLog.setBrowser(browser);
        loginLog.setOs(os);
        loginLog.setStatus(status);
        loginLog.setMessage(message);
        loginLogMapper.insert(loginLog);
    }

    /**
     * 查询登录日志列表（分页）
     */
    public Result<?> getLogList(String userType, String username, Integer status, int page, int size) {
        int offset = (page - 1) * size;
        List<LoginLog> logs = loginLogMapper.search(userType, username, status, size, offset);
        int total = loginLogMapper.count(userType, username, status);

        Map<String, Object> result = new HashMap<>();
        result.put("list", logs);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return Result.success(result);
    }

    /**
     * 获取最近登录日志
     */
    public Result<?> getRecentLogs(int limit) {
        List<LoginLog> logs = loginLogMapper.findRecent(limit);
        return Result.success(logs);
    }

    /**
     * 获取登录统计概览（近7天）
     */
    public Result<?> getWeekSummary() {
        LoginLog summary = loginLogMapper.getWeekSummary();
        return Result.success(summary);
    }

    /**
     * 获取每日登录统计（近30天）
     */
    public Result<?> getDailyStats() {
        List<LoginLog> stats = loginLogMapper.getDailyStats();
        return Result.success(stats);
    }

    /**
     * 安全扫描 - 检测频繁登录失败的账号
     */
    public Result<?> scanFailedLogins(int threshold) {
        List<LoginLog> failedLogins = loginLogMapper.getFailedLogins(threshold);
        List<Map<String, Object>> warnings = new java.util.ArrayList<>();

        for (LoginLog log : failedLogins) {
            Map<String, Object> warning = new HashMap<>();
            warning.put("type", "BRUTE_FORCE");
            warning.put("username", log.getUsername());
            warning.put("count", log.getCount());
            warning.put("message", "账号「" + log.getUsername() + "」近7天登录失败" + log.getCount() + "次，疑似暴力破解");
            warnings.add(warning);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("warnings", warnings);
        result.put("warningCount", warnings.size());
        result.put("scanTime", java.time.LocalDateTime.now());
        return Result.success(result);
    }

    /**
     * 安全扫描 - 检测可疑IP
     */
    public Result<?> scanSuspiciousIps(int threshold) {
        List<LoginLog> suspiciousIps = loginLogMapper.getSuspiciousIps(threshold);
        List<Map<String, Object>> warnings = new java.util.ArrayList<>();

        for (LoginLog log : suspiciousIps) {
            Map<String, Object> warning = new HashMap<>();
            warning.put("type", "SUSPICIOUS_IP");
            warning.put("ip", log.getLoginIp());
            warning.put("count", log.getCount());
            warning.put("message", "IP「" + log.getLoginIp() + "」近24小时登录失败" + log.getCount() + "次，疑似恶意攻击");
            warnings.add(warning);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("warnings", warnings);
        result.put("warningCount", warnings.size());
        result.put("scanTime", java.time.LocalDateTime.now());
        return Result.success(result);
    }

    /**
     * 综合安全扫描
     */
    public Result<?> fullSecurityScan() {
        Map<String, Object> result = new HashMap<>();

        // 1. 检测频繁失败登录
        List<LoginLog> failedLogins = loginLogMapper.getFailedLogins(5);
        List<Map<String, Object>> allWarnings = new java.util.ArrayList<>();
        for (LoginLog log : failedLogins) {
            Map<String, Object> warning = new HashMap<>();
            warning.put("type", "BRUTE_FORCE");
            warning.put("username", log.getUsername());
            warning.put("count", log.getCount());
            warning.put("message", "账号「" + log.getUsername() + "」近7天登录失败" + log.getCount() + "次");
            allWarnings.add(warning);
        }

        // 2. 检测可疑IP
        List<LoginLog> suspiciousIps = loginLogMapper.getSuspiciousIps(10);
        for (LoginLog log : suspiciousIps) {
            Map<String, Object> warning = new HashMap<>();
            warning.put("type", "SUSPICIOUS_IP");
            warning.put("ip", log.getLoginIp());
            warning.put("count", log.getCount());
            warning.put("message", "IP「" + log.getLoginIp() + "」近24小时失败登录" + log.getCount() + "次");
            allWarnings.add(warning);
        }

        // 3. 周统计
        LoginLog weekSummary = loginLogMapper.getWeekSummary();

        result.put("warnings", allWarnings);
        result.put("warningCount", allWarnings.size());
        result.put("weekSummary", weekSummary);
        result.put("scanTime", java.time.LocalDateTime.now());

        return Result.success(result);
    }

    /**
     * 清理旧日志
     */
    public Result<?> cleanOldLogs(int days) {
        int deleted = loginLogMapper.deleteOldLogs(days);
        return Result.success("已清理" + deleted + "条" + days + "天前的登录日志");
    }
}
