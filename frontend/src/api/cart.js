/**
 * 前台购物车相关 API
 * 提供购物车的增删改查及清空操作接口
 */
import request from './request'

// 获取购物车列表
export function getCartList() {
  return request.get('/cart')
}

// 添加到购物车
export function addToCart(data) {
  return request.post('/cart', data)
}

// 修改购物车数量
export function updateCartQuantity(id, quantity) {
  return request.put(`/cart/${id}`, { quantity })
}

// 删除购物车项
export function deleteCartItem(id) {
  return request.delete(`/cart/${id}`)
}

// 清空购物车
export function clearCart() {
  return request.delete('/cart/clear')
}

// 获取购物车数量
export function getCartCount() {
  return request.get('/cart/count')
}
