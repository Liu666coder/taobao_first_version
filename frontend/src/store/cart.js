import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCartList, getCartCount } from '@/api/cart'

export const useCartStore = defineStore('cart', () => {
  const cartList = ref([])
  const cartCount = ref(0)
  const selectedIds = ref([])

  const fetchCartList = async () => {
    try {
      const res = await getCartList()
      if (res.code === 200) {
        cartList.value = res.data
      }
    } catch (e) {
      console.error(e)
    }
  }

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

  const toggleSelect = (id) => {
    const numId = Number(id)
    const index = selectedIds.value.indexOf(numId)
    if (index > -1) {
      selectedIds.value.splice(index, 1)
    } else {
      selectedIds.value.push(numId)
    }
  }

  const selectAll = (checked = true) => {
    if (checked) {
      selectedIds.value = cartList.value.map(item => Number(item.id))
    } else {
      selectedIds.value = []
    }
  }

  const clearSelection = () => {
    selectedIds.value = []
  }

  const totalPrice = () => {
    return cartList.value
      .filter(item => selectedIds.value.includes(Number(item.id)))
      .reduce((sum, item) => sum + item.productPrice * item.quantity, 0)
  }

  return {
    cartList, cartCount, selectedIds,
    fetchCartList, fetchCartCount,
    toggleSelect, selectAll, clearSelection, totalPrice
  }
})
