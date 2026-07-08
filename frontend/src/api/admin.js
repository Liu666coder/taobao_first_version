/**
 * 后台管理相关 API
 * 包含管理员登录/信息、商品/分类/用户/订单管理及 AI 生成简介等接口
 */
import request from './request'

// 管理员登录
export function adminLogin(data) {
  return request.post('/admin/login', data)
}

// 获取管理员信息
export function getAdminInfo() {
  return request.get('/admin/info')
}

// 获取管理员列表
export function getAdminList(params) {
  return request.get('/admin/list', { params })
}

// 添加管理员
export function addAdmin(data) {
  return request.post('/admin', data)
}

// 修改管理员
export function updateAdmin(id, data) {
  return request.put(`/admin/${id}`, data)
}

// 删除管理员
export function deleteAdmin(id) {
  return request.delete(`/admin/${id}`)
}

// 启用/禁用管理员
export function updateAdminStatus(id, status) {
  return request.put(`/admin/${id}/status`, null, { params: { status } })
}

// 重置密码
export function resetPassword(id, data) {
  return request.put(`/admin/${id}/password`, data)
}

// ========== 商品管理 ==========
// 获取商品列表(后台)
export function getAdminProducts(params) {
  return request.get('/admin/products', { params })
}

// 添加商品
export function addProduct(data) {
  return request.post('/admin/products', data)
}

// 修改商品
export function updateProduct(id, data) {
  return request.put(`/admin/products/${id}`, data)
}

// 删除商品
export function deleteProduct(id) {
  return request.delete(`/admin/products/${id}`)
}

// 上下架商品
export function updateProductStatus(id, status) {
  return request.put(`/admin/products/${id}/status`, null, { params: { status } })
}

// ========== 分类管理 ==========
// 获取分类列表(后台)
export function getAdminCategories() {
  return request.get('/admin/categories')
}

// 添加分类
export function addCategory(data) {
  return request.post('/admin/categories', data)
}

// 修改分类
export function updateCategory(id, data) {
  return request.put(`/admin/categories/${id}`, data)
}

// 删除分类
export function deleteCategory(id) {
  return request.delete(`/admin/categories/${id}`)
}

// 启用/禁用分类
export function updateCategoryStatus(id, status) {
  return request.put(`/admin/categories/${id}/status`, null, { params: { status } })
}

// ========== 用户管理 ==========
// 获取用户列表
export function getUserList(params) {
  return request.get('/user/list', { params })
}

// 更新用户信息
export function updateUser(id, data) {
  return request.put(`/user/${id}`, data)
}

// 启用/禁用用户
export function updateUserStatus(id, status) {
  return request.put(`/user/${id}/status`, null, { params: { status } })
}

// 删除用户
export function deleteUser(id) {
  return request.delete(`/user/${id}`)
}

// ========== 订单管理 ==========
// 获取订单列表(后台)
export function getAdminOrders(params) {
  return request.get('/orders/admin', { params })
}

// 获取订单详情
export function getOrderDetail(id) {
  return request.get(`/orders/${id}`)
}

// 更新订单状态
export function updateOrderStatus(id, status) {
  return request.put(`/orders/admin/${id}/status`, null, { params: { status } })
}

// ========== AI生成 ==========
// AI一键生成商品简介
export function generateDescription(data) {
  return request.post('/admin/ai/generate-description', data)
}

// ========== 操作日志 ==========
// 获取操作日志列表
export function getAdminLogs(params) {
  return request.get('/admin/logs', { params })
}

// 获取最近操作日志
export function getRecentLogs(params) {
  return request.get('/admin/logs/recent', { params })
}

// 获取操作类型统计
export function getActionStats() {
  return request.get('/admin/logs/stats/actions')
}

// 获取管理员操作排行
export function getAdminLogStats() {
  return request.get('/admin/logs/stats/admins')
}

// 扫描异常操作
export function scanAbnormalOperations() {
  return request.post('/admin/logs/scan')
}

// 清理旧日志
export function cleanOldLogs(days) {
  return request.delete('/admin/logs/clean', { params: { days } })
}

// ========== 登录安全扫描 ==========
// 获取登录日志列表
export function getLoginLogs(params) {
  return request.get('/admin/security/logs', { params })
}

// 获取最近登录日志
export function getRecentLoginLogs(params) {
  return request.get('/admin/security/logs/recent', { params })
}

// 获取周登录统计
export function getWeekLoginSummary() {
  return request.get('/admin/security/stats/week')
}

// 获取每日登录统计
export function getDailyLoginStats() {
  return request.get('/admin/security/stats/daily')
}

// 扫描频繁失败登录
export function scanFailedLogins(params) {
  return request.post('/admin/security/scan/failed', null, { params })
}

// 扫描可疑IP
export function scanSuspiciousIps(params) {
  return request.post('/admin/security/scan/ips', null, { params })
}

// 综合安全扫描
export function fullSecurityScan() {
  return request.post('/admin/security/scan')
}

// 清理旧登录日志
export function cleanOldLoginLogs(days) {
  return request.delete('/admin/security/clean', { params: { days } })
}

