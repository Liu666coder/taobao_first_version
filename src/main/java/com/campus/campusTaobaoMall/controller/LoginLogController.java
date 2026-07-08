package com.campus.campusTaobaoMall.controller;

import com.campus.campusTaobaoMall.service.LoginLogService;
import com.campus.campusTaobaoMall.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/security")
public class LoginLogController {

    @Autowired
    private LoginLogService loginLogService;

    @GetMapping("/logs")
    public Result<?> getLogList(
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return loginLogService.getLogList(userType, username, status, page, size);
    }

    @GetMapping("/logs/recent")
    public Result<?> getRecentLogs(@RequestParam(defaultValue = "20") int limit) {
        return loginLogService.getRecentLogs(limit);
    }

    @GetMapping("/stats/week")
    public Result<?> getWeekSummary() {
        return loginLogService.getWeekSummary();
    }

    @GetMapping("/stats/daily")
    public Result<?> getDailyStats() {
        return loginLogService.getDailyStats();
    }

    @PostMapping("/scan/failed")
    public Result<?> scanFailedLogins(@RequestParam(defaultValue = "5") int threshold) {
        return loginLogService.scanFailedLogins(threshold);
    }

    @PostMapping("/scan/ips")
    public Result<?> scanSuspiciousIps(@RequestParam(defaultValue = "10") int threshold) {
        return loginLogService.scanSuspiciousIps(threshold);
    }

    @PostMapping("/scan")
    public Result<?> fullSecurityScan() {
        return loginLogService.fullSecurityScan();
    }

    @DeleteMapping("/clean")
    public Result<?> cleanOldLogs(@RequestParam(defaultValue = "90") int days) {
        return loginLogService.cleanOldLogs(days);
    }
}
