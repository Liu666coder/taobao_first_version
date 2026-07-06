package com.campus.campusTaobaoMall.controller;

import com.campus.campusTaobaoMall.service.OrderService;
import com.campus.campusTaobaoMall.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

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

    @GetMapping
    public Result<?> getOrderList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.getOrderList(userId);
    }

    @GetMapping("/{id}")
    public Result<?> getOrderDetail(@PathVariable Long id) {
        return orderService.getOrderDetail(id);
    }

    @PutMapping("/{id}/pay")
    public Result<?> payOrder(@PathVariable Long id) {
        return orderService.payOrder(id);
    }

    @PutMapping("/{id}/confirm")
    public Result<?> confirmReceive(@PathVariable Long id) {
        return orderService.confirmReceive(id);
    }

    @DeleteMapping("/{id}")
    public Result<?> cancelOrder(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }

    // 后台管理
    @GetMapping("/admin")
    public Result<?> getAllOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return orderService.getAllOrders(keyword, status);
    }

    @PutMapping("/admin/{id}/status")
    public Result<?> updateOrderStatus(@PathVariable Long id, @RequestParam Integer status) {
        return orderService.updateOrderStatus(id, status);
    }
}
