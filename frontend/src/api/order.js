/**
 * 前台订单相关 API
 * 提供订单创建、查询、付款、确认收货、取消等操作接口
 */
import request from './request'

// 创建订单
export function createOrder(data) {
  return request.post('/orders', data)
}

// 获取订单列表
export function getOrderList() {
  return request.get('/orders')
}

// 获取订单详情
export function getOrderDetail(id) {
  return request.get(`/orders/${id}`)
}

// 付款
export function payOrder(id) {
  return request.put(`/orders/${id}/pay`)
}

// 确认收货
export function confirmReceive(id) {
  return request.put(`/orders/${id}/confirm`)
}

// 取消订单
export function cancelOrder(id) {
  return request.delete(`/orders/${id}`)
}
