package com.campus.campusTaobaoMall.service;

import com.campus.campusTaobaoMall.entity.Cart;
import com.campus.campusTaobaoMall.entity.Product;
import com.campus.campusTaobaoMall.entity.User;
import com.campus.campusTaobaoMall.mapper.CartMapper;
import com.campus.campusTaobaoMall.mapper.ProductMapper;
import com.campus.campusTaobaoMall.mapper.UserMapper;
import com.campus.campusTaobaoMall.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 购物车业务服务层
 * 处理购物车的添加（已存在则合并数量）、数量修改、删除、清空及数量统计
 */
@Service
public class CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 查询用户的购物车列表
     */
    public Result<?> getCartList(Long userId) {
        List<Cart> cartList = cartMapper.findByUserId(userId);
        return Result.success(cartList);
    }

    /**
     * 添加商品到购物车：已存在则累加数量，否则新增记录；同时校验库存
     */
    public Result<?> addToCart(Long userId, Long productId, Integer quantity) {
        // 检查商品是否存在
        Product product = productMapper.findById(productId);
        if (product == null) {
            return Result.error("商品不存在");
        }
        if (product.getStatus() == 0) {
            return Result.error("商品已下架");
        }
        if (product.getStock() < quantity) {
            return Result.error("库存不足");
        }

        // 获取用户信息
        User user = userMapper.findById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 检查购物车中是否已有该商品
        Cart existingCart = cartMapper.findByUserAndProduct(userId, productId);
        if (existingCart != null) {
            // 更新数量
            int newQuantity = existingCart.getQuantity() + quantity;
            if (newQuantity > product.getStock()) {
                return Result.error("库存不足");
            }
            cartMapper.updateQuantity(existingCart.getId(), newQuantity);
        } else {
            // 新增购物车项
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setUsername(user.getUsername());
            cart.setProductId(productId);
            cart.setProductName(product.getName());
            cart.setQuantity(quantity);
            cartMapper.insert(cart);
        }
        return Result.success("添加成功");
    }

    /**
     * 修改购物车商品数量，需校验库存是否充足
     */
    public Result<?> updateQuantity(Long cartId, Integer quantity) {
        Cart cart = cartMapper.findById(cartId);
        if (cart == null) {
            return Result.error("购物车项不存在");
        }
        Product product = productMapper.findById(cart.getProductId());
        if (product.getStock() < quantity) {
            return Result.error("库存不足");
        }
        cartMapper.updateQuantity(cartId, quantity);
        return Result.success("更新成功");
    }

    /**
     * 删除单个购物车项
     */
    public Result<?> deleteCartItem(Long cartId) {
        cartMapper.deleteById(cartId);
        return Result.success("删除成功");
    }

    /**
     * 清空用户购物车
     */
    public Result<?> clearCart(Long userId) {
        cartMapper.deleteByUserId(userId);
        return Result.success("清空成功");
    }

    /**
     * 统计用户购物车商品数量（用于导航栏角标显示）
     */
    public Result<?> getCartCount(Long userId) {
        int count = cartMapper.countByUserId(userId);
        return Result.success(count);
    }
}
