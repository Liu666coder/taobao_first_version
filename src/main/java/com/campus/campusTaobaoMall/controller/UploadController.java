package com.campus.campusTaobaoMall.controller;

import com.campus.campusTaobaoMall.vo.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${upload.path:}")
    private String uploadPath;

    @PostMapping("/image")
    public Result<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只支持图片文件");
        }

        // 验证文件大小（2MB限制）
        if (file.getSize() > 2 * 1024 * 1024) {
            return Result.error("图片大小不能超过2MB");
        }

        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = "product_" + UUID.randomUUID().toString().substring(0, 8) + extension;

            // 确保上传路径存在
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 保存文件
            String filePath = uploadPath + fileName;
            File dest = new File(filePath);
            file.transferTo(dest);

            // 返回图片访问路径
            String imageUrl = "/images/" + fileName;
            return Result.success(imageUrl);
        } catch (IOException e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}
