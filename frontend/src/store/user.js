/**
 * 用户状态管理（Pinia Store）
 * 功能：管理用户登录状态、token、用户信息
 * 所有页面共享此数据，避免重复请求
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  // 登录凭证，从 localStorage 读取（刷新页面不丢失）
  const token = ref(localStorage.getItem('token') || '')
  // 用户信息（姓名、头像等）
  const userInfo = ref(null)

  // 保存 token：同时存到内存和 localStorage
  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  // 登出：清空所有用户数据
  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  // 从后端获取最新用户信息（调用 /api/user/info）
  const fetchUserInfo = async () => {
    if (!token.value) {
      userInfo.value = null
      return
    }
    try {
      const res = await getUserInfo()
      if (res.code === 200) {
        userInfo.value = res.data
      } else {
        logout()  // token 无效，自动登出
      }
    } catch (e) {
      logout()  // 请求失败，自动登出
    }
  }

  return { token, userInfo, setToken, logout, fetchUserInfo }
})
