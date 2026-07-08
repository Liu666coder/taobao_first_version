package com.campus.campusTaobaoMall.controller;

import com.campus.campusTaobaoMall.service.AdminLogService;
import com.campus.campusTaobaoMall.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/logs")
public class AdminLogController {

    @Autowired
    private AdminLogService adminLogService;

    @GetMapping
    public Result<?> getLogList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long adminId,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminLogService.getLogList(keyword, adminId, action, page, size);
    }

    @GetMapping("/recent")
    public Result<?> getRecentLogs(@RequestParam(defaultValue = "20") int limit) {
        return adminLogService.getRecentLogs(limit);
    }

    @GetMapping("/stats/actions")
    public Result<?> getActionStats() {
        return adminLogService.getActionStats();
    }

    @GetMapping("/stats/admins")
    public Result<?> getAdminStats() {
        return adminLogService.getAdminStats();
    }

    @PostMapping("/scan")
    public Result<?> scanAbnormalOperations() {
        return adminLogService.scanAbnormalOperations();
    }

    @DeleteMapping("/clean")
    public Result<?> cleanOldLogs(@RequestParam(defaultValue = "90") int days) {
        return adminLogService.cleanOldLogs(days);
    }
}
