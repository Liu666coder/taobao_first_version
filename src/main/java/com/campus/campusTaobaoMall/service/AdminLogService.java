package com.campus.campusTaobaoMall.service;

import com.campus.campusTaobaoMall.entity.AdminLog;
import com.campus.campusTaobaoMall.mapper.AdminLogMapper;
import com.campus.campusTaobaoMall.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminLogService {

    @Autowired
    private AdminLogMapper adminLogMapper;

    /**
     * 记录管理员操作日志
     */
    public void log(Long adminId, String adminName, String action, String target, String detail, String ip) {
        AdminLog adminLog = new AdminLog();
        adminLog.setAdminId(adminId);
        adminLog.setAdminName(adminName);
        adminLog.setAction(action);
        adminLog.setTarget(target);
        adminLog.setDetail(detail);
        adminLog.setIp(ip);
        adminLogMapper.insert(adminLog);
    }

    /**
     * 查询操作日志列表（分页）
     */
    public Result<?> getLogList(String keyword, Long adminId, String action, int page, int size) {
        int offset = (page - 1) * size;
        List<AdminLog> logs = adminLogMapper.search(keyword, adminId, action, size, offset);
        int total = adminLogMapper.count(keyword, adminId, action);

        Map<String, Object> result = new HashMap<>();
        result.put("list", logs);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return Result.success(result);
    }

    /**
     * 获取最近操作日志
     */
    public Result<?> getRecentLogs(int limit) {
        List<AdminLog> logs = adminLogMapper.findRecent(limit);
        return Result.success(logs);
    }

    /**
     * 获取操作统计（近7天）
     */
    public Result<?> getActionStats() {
        List<AdminLog> stats = adminLogMapper.getActionStats();
        return Result.success(stats);
    }

    /**
     * 获取管理员操作排行（近7天）
     */
    public Result<?> getAdminStats() {
        List<AdminLog> stats = adminLogMapper.getAdminStats();
        return Result.success(stats);
    }

    /**
     * 扫描异常操作（频繁操作检测）
     */
    public Result<?> scanAbnormalOperations() {
        List<AdminLog> actionStats = adminLogMapper.getActionStats();
        List<AdminLog> adminStats = adminLogMapper.getAdminStats();

        Map<String, Object> scanResult = new HashMap<>();
        scanResult.put("actionStats", actionStats);
        scanResult.put("adminStats", adminStats);
        scanResult.put("scanTime", java.time.LocalDateTime.now());

        // 检测异常：单个管理员7天内操作超过50次
        List<Map<String, Object>> warnings = new java.util.ArrayList<>();
        for (AdminLog stat : adminStats) {
            if (stat.getCount() != null && stat.getCount() > 50) {
                Map<String, Object> warning = new HashMap<>();
                warning.put("type", "HIGH_FREQUENCY");
                warning.put("adminName", stat.getAdminName());
                warning.put("count", stat.getCount());
                warning.put("message", "管理员「" + stat.getAdminName() + "」近7天操作" + stat.getCount() + "次，操作频率异常");
                warnings.add(warning);
            }
        }
        scanResult.put("warnings", warnings);
        scanResult.put("warningCount", warnings.size());

        return Result.success(scanResult);
    }

    /**
     * 清理旧日志
     */
    public Result<?> cleanOldLogs(int days) {
        int deleted = adminLogMapper.deleteOldLogs(days);
        return Result.success("已清理" + deleted + "条" + days + "天前的日志");
    }
}
