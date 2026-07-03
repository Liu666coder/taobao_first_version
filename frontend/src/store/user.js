import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

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
        logout()
      }
    } catch (e) {
      logout()
    }
  }

  return { token, userInfo, setToken, logout, fetchUserInfo }
})
