/**
 * 购物车状态管理（Pinia Store）
 * 功能：管理购物车列表、数量角标、商品选中状态、总价计算
 * 所有页面共享此数据，导航栏角标实时更新
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCartList, getCartCount } from '@/api/cart'

export const useCartStore = defineStore('cart', () => {
  const cartList = ref([])       // 购物车商品列表
  const cartCount = ref(0)       // 购物车数量（导航栏角标显示）
  const selectedIds = ref([])    // 已选中的商品 ID（用于结算）

  // 获取购物车列表（调用 /api/cart）
  const fetchCartList = async () => {
    try {
      const res = await getCartList()
      if (res.code === 200) {
        cartList.value = res.data
        // 清理选中状态：移除已不在购物车中的商品
        const validIds = new Set(cartList.value.map(item => Number(item.id)))
        selectedIds.value = selectedIds.value.filter(id => validIds.has(id))
      }
    } catch (e) {
      console.error(e)
    }
  }

  // 获取购物车数量（用于导航栏红色角标）
  const fetchCartCount = async () => {
    try {
      const res = await getCartCount()
      if (res.code === 200) {
        cartCount.value = res.data
      }
    } catch (e) {
      console.error(e)
    }
  }

  // 切换单个商品的选中状态（勾选/取消勾选）
  const toggleSelect = (id) => {
    const numId = Number(id)
    const index = selectedIds.value.indexOf(numId)
    if (index > -1) {
      selectedIds.value.splice(index, 1)  // 取消选中
    } else {
      selectedIds.value.push(numId)       // 选中
    }
  }

  // 全选 / 取消全选
  const selectAll = (checked = true) => {
    if (checked) {
      selectedIds.value = cartList.value.map(item => Number(item.id))  // 全选
    } else {
      selectedIds.value = []  // 取消全选
    }
  }

  // 清空选中状态
  const clearSelection = () => {
    selectedIds.value = []
  }

  // 计算已选中商品的总价
  const totalPrice = () => {
    return cartList.value
      .filter(item => selectedIds.value.includes(Number(item.id)))  // 只算选中的
      .reduce((sum, item) => sum + item.productPrice * item.quantity, 0)  // 单价 × 数量 累加
  }

  return {
    cartList, cartCount, selectedIds,
    fetchCartList, fetchCartCount,
    toggleSelect, selectAll, clearSelection, totalPrice
  }
})
