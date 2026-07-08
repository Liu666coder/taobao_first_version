/**
 * 路由配置模块
 * 定义前台（商城）和后台（管理）的所有路由，并通过全局守卫实现登录拦截
 */
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  // 进场页面
  {
    path: '/',
    name: 'Landing',
    component: () => import('@/views/front/LandingPage.vue'),
    meta: { title: '校园淘宝商城' }
  },
  // 前台路由
  {
    path: '/store',
    component: () => import('@/views/front/Layout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/front/Home.vue'),
        meta: { title: '商城主页' }
      },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        component: () => import('@/views/front/ProductDetail.vue'),
        meta: { title: '商品详情' }
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/front/Cart.vue'),
        meta: { title: '购物车', requireAuth: true }
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/front/Orders.vue'),
        meta: { title: '我的订单', requireAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/front/Profile.vue'),
        meta: { title: '会员中心', requireAuth: true }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/front/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/front/Register.vue'),
    meta: { title: '注册' }
  },
  // 后台路由
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/Login.vue'),
    meta: { title: '管理员登录' }
  },
  {
    path: '/admin',
    component: () => import('@/views/admin/Layout.vue'),
    meta: { requireAdmin: true },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '管理后台' }
      },
      {
        path: 'products',
        name: 'ProductManage',
        component: () => import('@/views/admin/ProductManage.vue'),
        meta: { title: '商品管理' }
      },
      {
        path: 'categories',
        name: 'CategoryManage',
        component: () => import('@/views/admin/CategoryManage.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'users',
        name: 'UserManage',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'admins',
        name: 'AdminManage',
        component: () => import('@/views/admin/AdminManage.vue'),
        meta: { title: '管理员管理' }
      },
      {
        path: 'orders',
        name: 'OrderManage',
        component: () => import('@/views/admin/OrderManage.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/admin/AdminProfile.vue'),
        meta: { title: '个人资料' }
      },
      {
        path: 'logs',
        name: 'AdminLogs',
        component: () => import('@/views/admin/AdminLogManage.vue'),
        meta: { title: '操作日志' }
      },
      {
        path: 'security',
        name: 'SecurityScan',
        component: () => import('@/views/admin/SecurityScan.vue'),
        meta: { title: '安全扫描' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局路由守卫：设置页面标题，未登录时拦截并跳转到登录页
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 校园淘宝` : '校园淘宝商城'

  const token = localStorage.getItem('token')
  const adminToken = localStorage.getItem('adminToken')

  // 前台需要登录的页面
  if (to.meta.requireAuth && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  // 后台需要管理员登录
  if (to.meta.requireAdmin && !adminToken) {
    next({ path: '/admin/login' })
    return
  }

  next()
})

export default router
