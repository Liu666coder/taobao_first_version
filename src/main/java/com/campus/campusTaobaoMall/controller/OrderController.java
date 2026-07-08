package com.campus.campusTaobaoMall.controller;

import com.campus.campusTaobaoMall.service.OrderService;
import com.campus.campusTaobaoMall.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单管理控制器
 * 提供用户下单、支付、确认收货、取消订单等操作，以及后台订单查询和状态管理
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 根据购物车选中项创建订单
     */
    @PostMapping
    public Result<?> createOrder(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<Integer> cartIdInts = (List<Integer>) params.get("cartIds");
        if (cartIdInts == null || cartIdInts.isEmpty()) {
            return Result.error("请选择要结算的商品");
        }
        List<Long> cartIds = new java.util.ArrayList<>();
        for (Integer cartId : cartIdInts) {
            cartIds.add(cartId.longValue());
        }
        return orderService.createOrder(userId, cartIds);
    }

    /**
     * 获取当前用户的订单列表
     */
    @GetMapping
    public Result<?> getOrderList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.getOrderList(userId);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public Result<?> getOrderDetail(@PathVariable Long id) {
        return orderService.getOrderDetail(id);
    }

    /**
     * 用户支付订单
     */
    @PutMapping("/{id}/pay")
    public Result<?> payOrder(@PathVariable Long id) {
        return orderService.payOrder(id);
    }

    /**
     * 用户确认收货
     */
    @PutMapping("/{id}/confirm")
    public Result<?> confirmReceive(@PathVariable Long id) {
        return orderService.confirmReceive(id);
    }

    /**
     * 用户取消订单
     */
    @DeleteMapping("/{id}")
    public Result<?> cancelOrder(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }

    /**
     * 后台查询所有订单（支持关键字和状态筛选）
     */
    @GetMapping("/admin")
    public Result<?> getAllOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return orderService.getAllOrders(keyword, status);
    }

    /**
     * 后台管理员更新订单状态（如发货等）
     */
    @PutMapping("/admin/{id}/status")
    public Result<?> updateOrderStatus(@PathVariable Long id, @RequestParam Integer status) {
        return orderService.updateOrderStatus(id, status);
    }
}
