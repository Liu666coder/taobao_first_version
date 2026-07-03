package com.campus.campusTaobaoMall.controller;

import com.campus.campusTaobaoMall.dto.LoginRequest;
import com.campus.campusTaobaoMall.entity.Admin;
import com.campus.campusTaobaoMall.service.AdminService;
import com.campus.campusTaobaoMall.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginRequest request) {
        return adminService.login(request);
    }

    @GetMapping("/info")
    public Result<?> getAdminInfo(HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        return adminService.getAdminInfo(adminId);
    }

    @GetMapping("/list")
    public Result<?> getAdminList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        return adminService.getAdminList(keyword, role);
    }

    @PostMapping
    public Result<?> addAdmin(@RequestBody Admin admin) {
        return adminService.addAdmin(admin);
    }

    @PutMapping("/{id}")
    public Result<?> updateAdmin(@PathVariable Long id, @RequestBody Admin admin) {
        admin.setId(id);
        return adminService.updateAdmin(admin);
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteAdmin(@PathVariable Long id) {
        return adminService.deleteAdmin(id);
    }

    @PutMapping("/{id}/status")
    public Result<?> updateAdminStatus(@PathVariable Long id, @RequestParam Integer status) {
        return adminService.updateAdminStatus(id, status);
    }

    @PutMapping("/{id}/password")
    public Result<?> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String newPassword = params.get("newPassword");
        return adminService.updateAdminPassword(id, newPassword);
    }
}
