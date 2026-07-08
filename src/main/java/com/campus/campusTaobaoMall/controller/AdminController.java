package com.campus.campusTaobaoMall.controller;

import com.campus.campusTaobaoMall.dto.LoginRequest;
import com.campus.campusTaobaoMall.entity.Admin;
import com.campus.campusTaobaoMall.service.AdminLogService;
import com.campus.campusTaobaoMall.service.AdminService;
import com.campus.campusTaobaoMall.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AdminLogService adminLogService;

    @Value("${upload.path:}")
    private String uploadPath;

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        Result<?> result = adminService.login(request);
        if (result.getCode() == 200) {
            String ip = getClientIp(httpRequest);
            adminLogService.log(null, request.getUsername(), "登录系统", "管理员账号", request.getUsername() + " 登录成功", ip);
        }
        return result;
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
    public Result<?> addAdmin(@RequestBody Admin admin, HttpServletRequest httpRequest) {
        Result<?> result = adminService.addAdmin(admin);
        if (result.getCode() == 200) {
            Long adminId = (Long) httpRequest.getAttribute("userId");
            String adminName = (String) httpRequest.getAttribute("username");
            String ip = getClientIp(httpRequest);
            adminLogService.log(adminId, adminName, "添加管理员", "管理员账号", "添加管理员「" + admin.getUsername() + "」，角色：" + admin.getRole(), ip);
        }
        return result;
    }

    @PutMapping("/{id}")
    public Result<?> updateAdmin(@PathVariable Long id, @RequestBody Admin admin, HttpServletRequest httpRequest) {
        admin.setId(id);
        Result<?> result = adminService.updateAdmin(admin);
        if (result.getCode() == 200) {
            Long adminId = (Long) httpRequest.getAttribute("userId");
            String adminName = (String) httpRequest.getAttribute("username");
            String ip = getClientIp(httpRequest);
            adminLogService.log(adminId, adminName, "修改管理员", "管理员ID:" + id, "修改管理员信息", ip);
        }
        return result;
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteAdmin(@PathVariable Long id, HttpServletRequest httpRequest) {
        Result<?> result = adminService.deleteAdmin(id);
        if (result.getCode() == 200) {
            Long adminId = (Long) httpRequest.getAttribute("userId");
            String adminName = (String) httpRequest.getAttribute("username");
            String ip = getClientIp(httpRequest);
            adminLogService.log(adminId, adminName, "删除管理员", "管理员ID:" + id, "删除管理员账号", ip);
        }
        return result;
    }

    @PutMapping("/{id}/status")
    public Result<?> updateAdminStatus(@PathVariable Long id, @RequestParam Integer status, HttpServletRequest httpRequest) {
        Result<?> result = adminService.updateAdminStatus(id, status);
        if (result.getCode() == 200) {
            Long adminId = (Long) httpRequest.getAttribute("userId");
            String adminName = (String) httpRequest.getAttribute("username");
            String ip = getClientIp(httpRequest);
            String statusText = status == 1 ? "启用" : "禁用";
            adminLogService.log(adminId, adminName, statusText + "管理员", "管理员ID:" + id, statusText + "管理员账号", ip);
        }
        return result;
    }

    @PutMapping("/{id}/password")
    public Result<?> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> params, HttpServletRequest httpRequest) {
        String newPassword = params.get("newPassword");
        Result<?> result = adminService.updateAdminPassword(id, newPassword);
        if (result.getCode() == 200) {
            Long adminId = (Long) httpRequest.getAttribute("userId");
            String adminName = (String) httpRequest.getAttribute("username");
            String ip = getClientIp(httpRequest);
            adminLogService.log(adminId, adminName, "重置密码", "管理员ID:" + id, "重置管理员密码", ip);
        }
        return result;
    }

    // ========== 个人资料管理 ==========

    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody Admin admin, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        admin.setId(adminId);
        return adminService.updateAdmin(admin);
    }

    @PostMapping("/avatar")
    public Result<?> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");

        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只支持图片文件");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String fileName = "admin_avatar_" + adminId + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String filePath = uploadPath + fileName;
            File dest = new File(filePath);
            file.transferTo(dest);

            String avatarUrl = "/images/" + fileName;
            Admin admin = new Admin();
            admin.setId(adminId);
            admin.setAvatar(avatarUrl);
            adminService.updateAdmin(admin);

            return Result.success(avatarUrl);
        } catch (IOException e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    @PutMapping("/password")
    public Result<?> updateMyPassword(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        return adminService.updateMyPassword(adminId, oldPassword, newPassword);
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        return ip;
    }
}
