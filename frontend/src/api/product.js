/**
 * 前台商品相关 API
 * 提供商品列表查询、详情获取、分类列表等接口
 */
import request from './request'

// 获取商品列表
export function getProducts(params) {
  return request.get('/products', { params })
}

// 获取商品详情
export function getProductDetail(id) {
  return request.get(`/products/${id}`)
}

// 获取分类列表
export function getCategories() {
  return request.get('/categories')
}
