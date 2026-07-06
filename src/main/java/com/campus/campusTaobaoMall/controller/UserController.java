package com.campus.campusTaobaoMall.controller;

import com.campus.campusTaobaoMall.dto.LoginRequest;
import com.campus.campusTaobaoMall.entity.User;
import com.campus.campusTaobaoMall.service.UserService;
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
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Value("${upload.path:}")
    private String uploadPath;

    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        return userService.register(user);
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @GetMapping("/info")
    public Result<?> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.getUserInfo(userId);
    }

    @PutMapping("/update")
    public Result<?> updateUser(@RequestBody User user, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        user.setId(userId);
        return userService.updateUser(user);
    }

    @PutMapping("/password")
    public Result<?> updatePassword(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        return userService.updatePassword(userId, oldPassword, newPassword);
    }

    /**
     * 上传用户头像
     */
    @PostMapping("/avatar")
    public Result<?> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只支持图片文件");
        }

        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String fileName = "avatar_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

            // 确保上传路径存在
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 保存文件
            String filePath = uploadPath + fileName;
            File dest = new File(filePath);
            file.transferTo(dest);

            // 更新用户头像路径
            String avatarUrl = "/images/" + fileName;
            User user = new User();
            user.setId(userId);
            user.setAvatar(avatarUrl);
            userService.updateUser(user);

            return Result.success(avatarUrl);
        } catch (IOException e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<?> getUserList(@RequestParam(required = false) String keyword) {
        return userService.getUserList(keyword);
    }

    @PutMapping("/{id}")
    public Result<?> updateUserAdmin(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return userService.updateUser(user);
    }

    @PutMapping("/{id}/status")
    public Result<?> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        return userService.updateUserStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }
}
