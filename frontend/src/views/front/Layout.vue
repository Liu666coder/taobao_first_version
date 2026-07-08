<template>
  <div class="front-layout">
    <!-- 顶部工具栏 -->
    <div class="top-bar">
      <div class="container top-bar-inner">
        <div class="top-left">
          <span>Hi，欢迎来到校园淘宝！</span>
          <template v-if="!userStore.token">
            <router-link to="/login" class="top-link">请登录</router-link>
            <router-link to="/register" class="top-link">免费注册</router-link>
          </template>
          <template v-else>
            <span class="top-link user-greeting">你好，{{ userStore.userInfo?.realName || userStore.userInfo?.username || '' }}</span>
          </template>
        </div>
        <div class="top-right">
          <router-link to="/admin/login" class="top-link">管理员入口</router-link>
          <el-divider direction="vertical" />
          <span class="top-link order-link" @click="requireLogin('/store/orders')">
            我的订单
            <el-badge :value="pendingOrderCount" :hidden="pendingOrderCount === 0" class="order-badge" />
          </span>
          <el-divider direction="vertical" />
          <span class="top-link" @click="requireLogin('/store/cart')">
            <el-icon><ShoppingCart /></el-icon> 购物车
            <el-badge :value="cartStore.cartCount" :hidden="cartStore.cartCount === 0" class="cart-badge" />
          </span>
        </div>
      </div>
    </div>

    <!-- 主导航栏 -->
    <header class="header">
      <div class="container header-inner">
        <div class="logo" @click="$router.push('/store')">
          <span class="logo-icon">🛒</span>
          <span class="logo-text">校园淘宝</span>
        </div>

        <span class="nav-link" @click="$router.push('/')">
          <span class="landing-icon">✦</span> 回到开始页面
        </span>

        <span class="nav-link" @click="$router.push('/store')">首页</span>

        <div class="search-box">
          <el-input
            v-model="keyword"
            placeholder="搜索商品"
            size="large"
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button type="primary" @click="handleSearch">
                搜索
              </el-button>
            </template>
          </el-input>
        </div>

        <div class="header-right">
          <el-dropdown @command="handleCommand" v-if="userStore.token">
            <span class="user-menu">
              <div class="user-avatar-nav">
                <img v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" alt="avatar" class="nav-avatar-img" />
                <span v-else class="nav-avatar-text">{{ (userStore.userInfo?.username || '用').charAt(0).toUpperCase() }}</span>
              </div>
              {{ userStore.userInfo?.realName || userStore.userInfo?.username || '我的账户' }}
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>会员中心
                </el-dropdown-item>
                <el-dropdown-item command="orders">
                  <el-icon><Document /></el-icon>我的订单
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <router-link v-else to="/login" class="login-link">
            <el-icon><User /></el-icon> 请登录
          </router-link>
        </div>
      </div>
    </header>

    <!-- 分类导航 -->
    <div class="category-nav">
      <div class="container">
        <div class="category-list">
          <span
            class="category-item all"
            :class="{ active: currentCategory === null }"
            @click="filterByCategory(null)"
          >
            全部分类
          </span>
          <span
            v-for="cat in categories"
            :key="cat.id"
            class="category-item"
            :class="{ active: currentCategory === cat.id }"
            @click="filterByCategory(cat.id)"
          >
            {{ cat.name }}
          </span>
        </div>
      </div>
    </div>

    <!-- 内容区域 -->
    <main class="main-content">
      <router-view :key="routerViewKey" />
    </main>

    <!-- 底部版权 -->
    <footer class="footer">
      <div class="container">
        <p>© 2026 校园淘宝商城 - 专为大学生打造的购物平台</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
/**
 * Layout - 前台商城整体布局
 * 包含顶部工具栏、导航栏、搜索框、分类导航和页脚，
 * 管理用户登录状态、购物车角标、待处理订单数等全局信息
 */
import { ref, onMounted, provide, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useCartStore } from '@/store/cart'
import { getCategories } from '@/api/product'
import { getOrderList } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()

// 监听路由变化，每次切换页面都刷新购物车数量
watch(() => route.path, () => {
  if (userStore.token) {
    cartStore.fetchCartCount()
    fetchPendingOrders()
  }
})

const keyword = ref('')
const categories = ref([])
const currentCategory = ref(null)
const pendingOrderCount = ref(0)

const fetchPendingOrders = async () => {
  try {
    const res = await getOrderList()
    if (res.code === 200 && Array.isArray(res.data)) {
      // 状态0=待付款，状态1=待发货，状态2=已发货待收货
      pendingOrderCount.value = res.data.filter(o => o.status === 0 || o.status === 1 || o.status === 2).length
    }
  } catch (e) {
    console.error(e)
  }
}

provide('currentCategory', currentCategory)
provide('categories', categories)

const handleSearch = () => {
  router.push({ path: '/store', query: { keyword: keyword.value, categoryId: currentCategory.value } })
}

const filterByCategory = (categoryId) => {
  currentCategory.value = categoryId
  router.push({ path: '/store', query: { keyword: keyword.value, categoryId } })
}

const requireLogin = (path) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录再操作')
    return
  }
  router.push(path)
}

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/store/profile')
  } else if (command === 'orders') {
    router.push('/store/orders')
  } else if (command === 'logout') {
    userStore.logout()
    cartStore.cartCount = 0
    // 强制刷新页面
    window.location.href = '/store'
  }
}

onMounted(async () => {
  try {
    const res = await getCategories()
    if (res.code === 200) {
      categories.value = res.data
    }
  } catch (e) {
    console.error(e)
  }

  if (userStore.token) {
    await userStore.fetchUserInfo()
    // 检查用户是否被禁用
    if (userStore.userInfo && userStore.userInfo.status === 0) {
      ElMessageBox.alert('您的账号已被禁用，请联系管理员', '账号已被禁用', {
        confirmButtonText: '确定',
        type: 'warning',
        showClose: false,
        closeOnClickModal: false,
        closeOnPressEscape: false
      }).then(() => {
        userStore.logout()
        cartStore.cartCount = 0
        window.location.href = '/login'
      })
      return
    }
    cartStore.fetchCartCount()
    fetchPendingOrders()
  }
})
</script>

<style lang="scss" scoped>
.front-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.top-bar {
  background: #f5f5f5;
  border-bottom: 1px solid #e5e5e5;
  height: 35px;
  line-height: 35px;
  font-size: 12px;
  color: #666;
}

.top-bar-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.top-left, .top-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.top-link {
  color: #666;
  cursor: pointer;

  &:hover {
    color: #FF4400;
  }
}

.user-greeting {
  color: #FF4400;
  font-weight: 500;
}

.header {
  background: #fff;
  padding: 20px 0;
}

.header-inner {
  display: flex;
  align-items: center;
  gap: 40px;
}

.logo {
  display: flex;
  align-items: center;
  cursor: pointer;
  flex-shrink: 0;

  .logo-icon {
    font-size: 32px;
    margin-right: 8px;
  }

  .logo-text {
    font-size: 28px;
    font-weight: bold;
    color: #FF4400;
    background: linear-gradient(135deg, #FF4400, #FF6633);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
}

.nav-link {
  margin-left: 12px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  .landing-icon {
    font-size: 11px;
    animation: twinkle 1.5s ease-in-out infinite;
  }
}

@keyframes twinkle {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}

.search-box {
  flex: 1;
  max-width: 600px;

  :deep(.el-input-group__append) {
    background: #FF4400;
    border-color: #FF4400;
    color: #fff;
    padding: 0 30px;
    font-size: 16px;

    .el-button {
      color: #fff;
      border: none;
    }
  }
}

.header-right {
  flex-shrink: 0;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #333;
  padding: 6px 12px;
  border: 1px solid #eee;
  border-radius: 25px;
  transition: all 0.2s;

  &:hover {
    color: #FF4400;
    border-color: #FF4400;
    box-shadow: 0 2px 8px rgba(255, 68, 0, 0.15);
  }

  .user-avatar-nav {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    overflow: hidden;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;

    .nav-avatar-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .nav-avatar-text {
      color: white;
      font-size: 14px;
      font-weight: 600;
    }
  }
}

.login-link {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #333;
  padding: 8px 12px;
  border: 1px solid #eee;
  border-radius: 4px;
  text-decoration: none;

  &:hover {
    color: #FF4400;
    border-color: #FF4400;
  }
}

.category-nav {
  background: #fff;
  border-top: 2px solid #FF4400;
  box-shadow: 0 2px 4px rgba(0,0,0,.06);
}

.category-list {
  display: flex;
  align-items: center;
  height: 40px;
  gap: 0;
}

.category-item {
  cursor: pointer;
  font-size: 14px;
  color: #333;
  padding: 8px 20px;
  border-bottom: 2px solid transparent;
  transition: all .3s;

  &.all {
    background: #FF4400;
    color: #fff;
    font-weight: bold;
  }

  &:hover, &.active {
    color: #FF4400;
    background: #fff1f0;
  }
}

.main-content {
  flex: 1;
  padding: 20px 0;
}

.footer {
  padding: 20px 0;
  text-align: center;
  background: #f5f5f5;
  border-top: 1px solid #e5e5e5;

  p {
    color: #999;
    font-size: 12px;
  }
}

.cart-badge {
  :deep(.el-badge__content) {
    background: #FF4400;
    position: absolute;
    top: -8px;
    right: -8px;
  }
}

.order-link {
  position: relative;
}

.order-badge {
  :deep(.el-badge__content) {
    background: #ff4d4f;
    position: absolute;
    top: -10px;
    right: -16px;
    font-size: 10px;
    height: 16px;
    line-height: 16px;
    padding: 0 4px;
    border-radius: 8px;
  }
}
</style>
