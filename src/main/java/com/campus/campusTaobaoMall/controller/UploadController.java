package com.campus.campusTaobaoMall.controller;

import com.campus.campusTaobaoMall.vo.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * =====================================================================
 * 文件上传控制器
 * =====================================================================
 *
 * 【功能说明】
 * 提供商品图片上传功能，支持图片类型校验和2MB大小限制
 *
 * 【技术原理】
 * 1. 接收前端上传的MultipartFile文件
 * 2. 验证文件类型（只允许图片）
 * 3. 验证文件大小（不超过2MB）
 * 4. 生成唯一文件名（防止冲突）
 * 5. 保存到服务器指定目录
 * 6. 返回图片访问URL
 *
 * 【接口信息】
 * 路径：POST /api/upload/image
 * 请求：multipart/form-data（包含文件）
 * 响应：{ code: 200, data: "/images/product_xxx.jpg" }
 *
 * 【安全措施】
 * - 三重验证：前端accept + 前端beforeUpload + 后端验证
 * - UUID文件名：防止文件名冲突和路径遍历攻击
 * - 大小限制：防止恶意上传大文件
 * =====================================================================
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    /**
     * 上传路径，从配置文件application.yml中读取
     * 例如：upload.path: /data/images/
     */
    @Value("${upload.path:}")
    private String uploadPath;

    /**
     * =====================================================================
     * 上传图片文件
     * =====================================================================
     *
     * 【功能】接收前端上传的图片，保存到服务器，返回访问URL
     *
     * 【请求方式】POST
     * 【请求路径】/api/upload/image
     * 【请求格式】multipart/form-data
     *
     * 【请求参数】
     * - file: 图片文件（MultipartFile类型）
     *
     * 【响应结果】
     * - 成功：{ code: 200, data: "/images/product_xxx.jpg" }
     * - 失败：{ code: 500, message: "错误信息" }
     *
     * 【执行流程】
     * 1. 验证文件是否为空
     * 2. 验证文件类型（只允许图片）
     * 3. 验证文件大小（不超过2MB）
     * 4. 生成唯一文件名
     * 5. 保存文件到服务器
     * 6. 返回图片访问URL
     * =====================================================================
     */
    @PostMapping("/image")
    public Result<?> uploadImage(@RequestParam("file") MultipartFile file) {
        // =====================================================================
        // 第一步：验证文件是否为空
        // =====================================================================
        // 用户可能没有选择文件就点击上传
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }

        // =====================================================================
        // 第二步：验证文件类型
        // =====================================================================
        // 只允许上传图片文件（jpg、png、gif等）
        // getContentType() 返回MIME类型，如 "image/jpeg"、"image/png"
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只支持图片文件");
        }

        // =====================================================================
        // 第三步：验证文件大小
        // =====================================================================
        // 限制最大2MB，防止恶意上传大文件
        // getSize() 返回文件大小（字节）
        // 2MB = 2 * 1024 * 1024 = 2097152 字节
        if (file.getSize() > 2 * 1024 * 1024) {
            return Result.error("图片大小不能超过2MB");
        }

        try {
            // =====================================================================
            // 第四步：生成唯一文件名
            // =====================================================================
            // 4.1 获取原文件名
            String originalFilename = file.getOriginalFilename();  // 例如："photo.jpg"

            // 4.2 提取扩展名
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                // 例如：".jpg"
            }

            // 4.3 生成UUID（通用唯一标识符）
            // UUID.randomUUID() 生成随机ID，如 "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
            // substring(0, 8) 截取前8位，如 "a1b2c3d4"
            // 最终文件名：product_a1b2c3d4.jpg
            String fileName = "product_" + UUID.randomUUID().toString().substring(0, 8) + extension;

            // =====================================================================
            // 第五步：保存文件到服务器
            // =====================================================================
            // 5.1 确保上传目录存在
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();  // 创建多级目录
            }

            // 5.2 拼接完整文件路径
            String filePath = uploadPath + fileName;  // 例如：/data/images/product_a1b2c3d4.jpg

            // 5.3 创建目标文件对象
            File dest = new File(filePath);

            // 5.4 保存文件
            // transferTo() 是Spring提供的方法，将上传的文件保存到指定位置
            file.transferTo(dest);

            // =====================================================================
            // 第六步：返回图片访问URL
            // =====================================================================
            // 返回的URL供前端显示预览图
            // 前端通过这个URL访问图片：http://localhost:8080/images/product_a1b2c3d4.jpg
            String imageUrl = "/images/" + fileName;
            return Result.success(imageUrl);

        } catch (IOException e) {
            // =====================================================================
            // 异常处理
            // =====================================================================
            // 文件保存失败：磁盘空间不足、权限问题等
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}
