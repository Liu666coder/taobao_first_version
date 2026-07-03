package com.campus.campusTaobaoMall.controller;

import com.campus.campusTaobaoMall.service.CartService;
import com.campus.campusTaobaoMall.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public Result<?> getCartList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return cartService.getCartList(userId);
    }

    @PostMapping
    public Result<?> addToCart(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long productId = Long.valueOf(params.get("productId").toString());
        Integer quantity = params.get("quantity") != null ?
                Integer.valueOf(params.get("quantity").toString()) : 1;
        return cartService.addToCart(userId, productId, quantity);
    }

    @PutMapping("/{id}")
    public Result<?> updateQuantity(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Integer quantity = params.get("quantity");
        return cartService.updateQuantity(id, quantity);
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteCartItem(@PathVariable Long id) {
        return cartService.deleteCartItem(id);
    }

    @DeleteMapping("/clear")
    public Result<?> clearCart(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return cartService.clearCart(userId);
    }

    @GetMapping("/count")
    public Result<?> getCartCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return cartService.getCartCount(userId);
    }
}
