package com.campus.campusTaobaoMall.controller;

import com.campus.campusTaobaoMall.dto.LoginRequest;
import com.campus.campusTaobaoMall.entity.Admin;
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

    @Value("${upload.path:}")
    private String uploadPath;

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
}
