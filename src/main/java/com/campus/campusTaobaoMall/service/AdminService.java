package com.campus.campusTaobaoMall.service;

import com.campus.campusTaobaoMall.dto.LoginRequest;
import com.campus.campusTaobaoMall.entity.Admin;
import com.campus.campusTaobaoMall.mapper.AdminMapper;
import com.campus.campusTaobaoMall.util.JwtUtil;
import com.campus.campusTaobaoMall.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    @Autowired
    private AdminMapper adminMapper;

    public Result<?> login(LoginRequest request) {
        Admin admin = adminMapper.findByUsername(request.getUsername());
        if (admin == null) {
            return Result.error("管理员不存在");
        }
        if (!admin.getPassword().equals(request.getPassword())) {
            return Result.error("密码错误");
        }
        if (admin.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }
        String token = JwtUtil.generateToken(admin.getId(), admin.getUsername(), admin.getRole());
        return Result.success(token);
    }

    public Result<?> getAdminList(String keyword, String role) {
        List<Admin> admins = adminMapper.search(keyword, role);
        admins.forEach(a -> a.setPassword(null));
        return Result.success(admins);
    }

    public Result<?> addAdmin(Admin admin) {
        // 检查用户名是否已存在
        Admin existing = adminMapper.findByUsername(admin.getUsername());
        if (existing != null) {
            return Result.error("用户名已存在");
        }
        admin.setStatus(1);
        adminMapper.insert(admin);
        return Result.success("添加成功");
    }

    public Result<?> updateAdmin(Admin admin) {
        adminMapper.update(admin);
        return Result.success("更新成功");
    }

    public Result<?> deleteAdmin(Long adminId) {
        adminMapper.deleteById(adminId);
        return Result.success("删除成功");
    }

    public Result<?> updateAdminStatus(Long adminId, Integer status) {
        Admin admin = new Admin();
        admin.setId(adminId);
        admin.setStatus(status);
        adminMapper.update(admin);
        return Result.success("操作成功");
    }

    public Result<?> updateAdminPassword(Long adminId, String newPassword) {
        adminMapper.updatePassword(adminId, newPassword);
        return Result.success("密码重置成功");
    }

    public Result<?> getAdminInfo(Long adminId) {
        Admin admin = adminMapper.findById(adminId);
        if (admin == null) {
            return Result.error("管理员不存在");
        }
        admin.setPassword(null);
        return Result.success(admin);
    }
}
