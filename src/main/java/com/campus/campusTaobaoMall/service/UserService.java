package com.campus.campusTaobaoMall.service;

import com.campus.campusTaobaoMall.dto.LoginRequest;
import com.campus.campusTaobaoMall.entity.User;
import com.campus.campusTaobaoMall.mapper.CartMapper;
import com.campus.campusTaobaoMall.mapper.OrderItemMapper;
import com.campus.campusTaobaoMall.mapper.OrderMapper;
import com.campus.campusTaobaoMall.mapper.UserMapper;
import com.campus.campusTaobaoMall.util.JwtUtil;
import com.campus.campusTaobaoMall.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    public Result<?> register(User user) {
        // 检查用户名是否已存在
        User existing = userMapper.findByUsername(user.getUsername());
        if (existing != null) {
            return Result.error("用户名已存在");
        }
        user.setStatus(1);
        userMapper.insert(user);
        return Result.success("注册成功");
    }

    public Result<?> login(LoginRequest request) {
        // 根据登录类型进行验证
        if ("phone".equals(request.getLoginType())) {
            return loginByPhone(request);
        } else {
            return loginByPassword(request);
        }
    }

    private Result<?> loginByPassword(LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return Result.error("账号和密码不能为空");
        }

        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (!user.getPassword().equals(request.getPassword())) {
            return Result.error("密码错误");
        }
        if (user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), "USER");
        return Result.success(token);
    }

    private Result<?> loginByPhone(LoginRequest request) {
        if (request.getPhone() == null || request.getPassword() == null) {
            return Result.error("手机号和密码不能为空");
        }

        User user = userMapper.findByPhone(request.getPhone());
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (!user.getPassword().equals(request.getPassword())) {
            return Result.error("密码错误");
        }
        if (user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }

        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), "USER");
        return Result.success(token);
    }

    public Result<?> getUserInfo(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    public Result<?> updateUser(User user) {
        userMapper.update(user);
        return Result.success("更新成功");
    }

    public Result<?> updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.findById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (!user.getPassword().equals(oldPassword)) {
            return Result.error("原密码错误");
        }
        userMapper.updatePassword(userId, newPassword);
        return Result.success("密码修改成功");
    }

    public Result<?> getUserList(String keyword) {
        List<User> users = userMapper.search(keyword);
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }

    public Result<?> updateUserStatus(Long userId, Integer status) {
        User user = new User();
        user.setId(userId);
        user.setStatus(status);
        userMapper.update(user);
        return Result.success("操作成功");
    }

    public Result<?> deleteUser(Long userId) {
        // 先删除关联记录
        cartMapper.deleteByUserId(userId);
        orderItemMapper.deleteByUserId(userId);
        orderMapper.deleteByUserId(userId);
        // 再删除用户
        userMapper.deleteById(userId);
        return Result.success("删除成功");
    }
}
