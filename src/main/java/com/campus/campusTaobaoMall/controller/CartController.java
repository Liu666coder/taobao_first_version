package com.campus.campusTaobaoMall.controller;

import com.campus.campusTaobaoMall.service.CartService;
import com.campus.campusTaobaoMall.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 购物车控制器
 * 提供购物车的增删改查、数量修改、清空及数量统计等接口
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 获取当前用户的购物车列表
     */
    @GetMapping
    public Result<?> getCartList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return cartService.getCartList(userId);
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping
    public Result<?> addToCart(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long productId = Long.valueOf(params.get("productId").toString());
        Integer quantity = params.get("quantity") != null ?
                Integer.valueOf(params.get("quantity").toString()) : 1;
        return cartService.addToCart(userId, productId, quantity);
    }

    /**
     * 修改购物车商品数量
     */
    @PutMapping("/{id}")
    public Result<?> updateQuantity(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Integer quantity = params.get("quantity");
        return cartService.updateQuantity(id, quantity);
    }

    /**
     * 删除单个购物车项
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteCartItem(@PathVariable Long id) {
        return cartService.deleteCartItem(id);
    }

    /**
     * 清空当前用户购物车
     */
    @DeleteMapping("/clear")
    public Result<?> clearCart(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return cartService.clearCart(userId);
    }

    /**
     * 获取购物车商品数量（用于导航栏角标）
     */
    @GetMapping("/count")
    public Result<?> getCartCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return cartService.getCartCount(userId);
    }
}
