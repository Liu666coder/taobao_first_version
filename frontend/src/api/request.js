import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    const adminToken = localStorage.getItem('adminToken')

    // 判断当前是否在后台管理页面
    const isAdminPage = window.location.pathname.startsWith('/admin')

    if (isAdminPage) {
      // 后台管理页面：优先使用adminToken
      if (adminToken) {
        config.headers['Authorization'] = `Bearer ${adminToken}`
      } else if (token) {
        config.headers['Authorization'] = `Bearer ${token}`
      }
    } else {
      // 前台页面：优先使用user token
      if (token) {
        config.headers['Authorization'] = `Bearer ${token}`
      } else if (adminToken) {
        config.headers['Authorization'] = `Bearer ${adminToken}`
      }
    }

    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response) {
      const { status, data } = error.response

      if (status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('adminToken')
        // 根据当前路径判断跳转到哪个登录页
        const currentPath = window.location.pathname
        if (currentPath.startsWith('/admin')) {
          router.push('/admin/login')
        } else {
          router.push('/login')
        }
        ElMessage.error('登录已过期，请重新登录')
      } else if (status === 403) {
        ElMessage.error(data?.message || '没有权限访问')
      } else {
        ElMessage.error(data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络异常，请稍后重试')
    }

    return Promise.reject(error)
  }
)

export default request
