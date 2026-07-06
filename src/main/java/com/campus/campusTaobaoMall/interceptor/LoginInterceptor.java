package com.campus.campusTaobaoMall.interceptor;

import com.campus.campusTaobaoMall.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证token
        if (token == null || !JwtUtil.isTokenValid(token)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 401);
            result.put("message", "未登录或登录已过期");
            ObjectMapper objectMapper = new ObjectMapper();
            response.getWriter().write(objectMapper.writeValueAsString(result));
            return false;
        }

        // 将用户信息存入request
        String role = JwtUtil.getRole(token);
        request.setAttribute("userId", JwtUtil.getUserId(token));
        request.setAttribute("username", JwtUtil.getUsername(token));
        request.setAttribute("role", role);

        // ========== 角色权限校验 ==========
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 管理员专属接口校验
        if (uri.startsWith("/api/admin")) {
            // 排除登录接口（已在excludePathPatterns中排除，这里做双重保险）
            if (uri.equals("/api/admin/login")) {
                return true;
            }

            // 所有管理员接口都需要管理员角色
            if (!isAdminRole(role)) {
                sendForbidden(response, "需要管理员权限");
                return false;
            }

            // 系统管理员专属接口：管理员管理（增删改、状态、密码）
            if (uri.matches("/api/admin/(\\d+).*") || uri.equals("/api/admin/list")
                    || (uri.equals("/api/admin") && "POST".equals(method))) {
                if (!"SYSTEM_ADMIN".equals(role)) {
                    sendForbidden(response, "需要系统管理员权限");
                    return false;
                }
            }
        }

        // 用户管理接口（后台调用的用户接口）校验
        if (uri.matches("/api/user/\\d+.*") || uri.equals("/api/user/list")) {
            // 这些是后台管理用的用户接口，需要管理员角色
            if (!isAdminRole(role)) {
                sendForbidden(response, "需要管理员权限");
                return false;
            }
            // 用户管理（查询、修改、禁用、删除）需要营销经理或系统管理员权限
            if (!"MARKETING_MANAGER".equals(role) && !"SYSTEM_ADMIN".equals(role)) {
                sendForbidden(response, "需要营销经理或系统管理员权限");
                return false;
            }
        }

        // 后台订单管理接口校验
        if (uri.startsWith("/api/orders/admin")) {
            if (!isAdminRole(role)) {
                sendForbidden(response, "需要管理员权限");
                return false;
            }
        }

        // 前台用户接口校验（购物车、订单、个人信息等）- 仅限普通用户
        if (uri.startsWith("/api/cart") || uri.startsWith("/api/orders")) {
            if (!"USER".equals(role) && !isAdminRole(role)) {
                sendForbidden(response, "需要用户权限");
                return false;
            }
        }

        return true;
    }

    /**
     * 判断是否为管理员角色
     */
    private boolean isAdminRole(String role) {
        return "PRODUCT_MANAGER".equals(role)
                || "MARKETING_MANAGER".equals(role)
                || "SYSTEM_ADMIN".equals(role);
    }

    /**
     * 返回403权限不足响应
     */
    private void sendForbidden(HttpServletResponse response, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(403);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 403);
        result.put("message", message);
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
