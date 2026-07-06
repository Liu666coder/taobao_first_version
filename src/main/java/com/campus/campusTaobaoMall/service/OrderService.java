package com.campus.campusTaobaoMall.service;

import com.campus.campusTaobaoMall.entity.Cart;
import com.campus.campusTaobaoMall.entity.OrderItem;
import com.campus.campusTaobaoMall.entity.Orders;
import com.campus.campusTaobaoMall.entity.Product;
import com.campus.campusTaobaoMall.entity.User;
import com.campus.campusTaobaoMall.mapper.CartMapper;
import com.campus.campusTaobaoMall.mapper.OrderItemMapper;
import com.campus.campusTaobaoMall.mapper.OrderMapper;
import com.campus.campusTaobaoMall.mapper.ProductMapper;
import com.campus.campusTaobaoMall.mapper.UserMapper;
import com.campus.campusTaobaoMall.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Transactional
    public Result<?> createOrder(Long userId, List<Long> cartIds) {
        // 获取用户信息
        User user = userMapper.findById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 获取购物车列表
        List<Cart> cartList = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Long cartId : cartIds) {
            Cart cart = cartMapper.findById(cartId);
            if (cart == null) {
                return Result.error("购物车项不存在");
            }
            Product product = productMapper.findById(cart.getProductId());
            if (product == null || product.getStatus() == 0) {
                return Result.error("商品不存在或已下架");
            }
            if (product.getStock() < cart.getQuantity()) {
                return Result.error(product.getName() + " 库存不足");
            }
            cart.setProductName(product.getName());
            cart.setProductPrice(product.getPrice());
            cartList.add(cart);
            totalAmount = totalAmount.add(product.getPrice().multiply(new BigDecimal(cart.getQuantity())));
        }

        // 生成订单号
        String orderNo = "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 创建订单
        Orders order = new Orders();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(0); // 待付款
        orderMapper.insert(order);

        // 创建订单详情并扣减库存
        for (Cart cart : cartList) {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setUserId(userId);
            item.setUsername(user.getUsername());
            item.setProductId(cart.getProductId());
            item.setProductName(cart.getProductName());
            item.setProductImage(cart.getProductImage());
            item.setPrice(cart.getProductPrice());
            item.setQuantity(cart.getQuantity());
            orderItemMapper.insert(item);

            // 扣减库存
            Product product = productMapper.findById(cart.getProductId());
            product.setStock(product.getStock() - cart.getQuantity());
            productMapper.update(product);
        }

        // 清空购物车
        for (Long cartId : cartIds) {
            cartMapper.deleteById(cartId);
        }

        return Result.success(order);
    }

    public Result<?> getOrderList(Long userId) {
        List<Orders> orders = orderMapper.findByUserId(userId);
        return Result.success(orders);
    }

    public Result<?> getOrderDetail(Long orderId) {
        Orders order = orderMapper.findById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        List<OrderItem> items = orderItemMapper.findByOrderId(orderId);
        order.setItems(items);
        return Result.success(order);
    }

    public Result<?> payOrder(Long orderId) {
        Orders order = orderMapper.findById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (order.getStatus() != 0) {
            return Result.error("订单状态错误");
        }
        orderMapper.updateStatus(orderId, 1); // 已付款
        return Result.success("付款成功");
    }

    @Transactional
    public Result<?> cancelOrder(Long orderId) {
        Orders order = orderMapper.findById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (order.getStatus() != 0) {
            return Result.error("只能取消待付款订单");
        }

        // 恢复库存
        List<OrderItem> items = orderItemMapper.findByOrderId(orderId);
        for (OrderItem item : items) {
            Product product = productMapper.findById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                productMapper.update(product);
            }
        }

        // 先删除订单明细
        orderItemMapper.deleteByOrderId(orderId);
        // 再删除订单
        orderMapper.deleteById(orderId);
        return Result.success("取消成功");
    }

    public Result<?> confirmReceive(Long orderId) {
        Orders order = orderMapper.findById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (order.getStatus() != 2) {
            return Result.error("只能确认已发货的订单");
        }
        orderMapper.updateStatus(orderId, 3); // 已完成
        return Result.success("确认收货成功");
    }

    // 后台管理
    public Result<?> getAllOrders(String keyword, Integer status) {
        List<Orders> orders = orderMapper.searchAdmin(keyword, status);
        return Result.success(orders);
    }

    public Result<?> updateOrderStatus(Long orderId, Integer status) {
        orderMapper.updateStatus(orderId, status);
        return Result.success("更新成功");
    }
}
