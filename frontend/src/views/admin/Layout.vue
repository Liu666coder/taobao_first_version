<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="sidebar-header">
        <span v-if="!isCollapsed" class="logo-text">🛒 校园淘宝</span>
        <span v-else class="logo-icon">🛒</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        router
        background-color="transparent"
        text-color="rgba(255,255,255,0.55)"
        active-text-color="#ff5722"
      >
        <el-menu-item index="/admin">
          <el-icon><DataBoard /></el-icon>
          <span>控制面板</span>
        </el-menu-item>

        <el-menu-item index="/admin/products" v-if="hasProductPermission">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/categories" v-if="hasProductPermission">
          <el-icon><Grid /></el-icon>
          <span>分类管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/users" v-if="hasUserPermission">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/admins" v-if="isAdmin">
          <el-icon><UserFilled /></el-icon>
          <span>管理员管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/logs" v-if="isAdmin">
          <el-icon><Notebook /></el-icon>
          <span>操作日志</span>
        </el-menu-item>

        <el-menu-item index="/admin/orders" class="order-menu-item">
          <el-icon><Document /></el-icon>
          <span>订单管理</span>
          <span v-if="pendingShipmentCount > 0" class="order-count">{{ pendingShipmentCount > 99 ? '99+' : pendingShipmentCount }}</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <!-- 主内容区 -->
    <div class="main-area" :class="{ collapsed: isCollapsed }">
      <!-- 顶部栏 -->
      <header class="top-header">
        <div class="header-left">
          <el-icon class="toggle-btn" @click="isCollapsed = !isCollapsed">
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta.title !== '管理后台'">
              {{ $route.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-tag :type="roleTagType" size="small" class="role-tag">{{ roleText }}</el-tag>
          <el-dropdown @command="handleCommand">
            <span class="admin-name">
              <div class="avatar-small">
                <img v-if="adminInfo?.avatar" :src="adminInfo.avatar" class="avatar-img" />
                <el-icon v-else size="16"><User /></el-icon>
              </div>
              {{ adminInfo?.realName || adminInfo?.username || '管理员' }}
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人资料
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 内容区 -->
      <main class="content-area">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, provide } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getAdminInfo, getAdminOrders } from '@/api/admin'

const router = useRouter()
const route = useRoute()

const isCollapsed = ref(false)
const adminInfo = ref(null)
const pendingShipmentCount = ref(0)
let orderPollingTimer = null

const roleMap = {
  'PRODUCT_MANAGER': { text: '商品专员', tag: '' },
  'MARKETING_MANAGER': { text: '营销经理', tag: 'warning' },
  'SYSTEM_ADMIN': { text: '系统管理员', tag: 'danger' }
}

const roleText = computed(() => roleMap[adminInfo.value?.role]?.text || '管理员')
const roleTagType = computed(() => roleMap[adminInfo.value?.role]?.tag || '')
const activeMenu = computed(() => route.path)

const hasProductPermission = computed(() => {
  const role = adminInfo.value?.role
  return role === 'PRODUCT_MANAGER' || role === 'MARKETING_MANAGER' || role === 'SYSTEM_ADMIN'
})

const hasUserPermission = computed(() => {
  const role = adminInfo.value?.role
  return role === 'MARKETING_MANAGER' || role === 'SYSTEM_ADMIN'
})

const isAdmin = computed(() => adminInfo.value?.role === 'SYSTEM_ADMIN')

const fetchAdminInfo = async () => {
  try {
    const res = await getAdminInfo()
    if (res.code === 200) {
      adminInfo.value = res.data
    }
  } catch (e) {
    console.error(e)
  }
}

// 提供给子组件刷新管理员信息的方法
provide('refreshAdminInfo', fetchAdminInfo)

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/admin/profile')
  } else if (command === 'logout') {
    localStorage.removeItem('adminToken')
    router.push('/admin/login')
  }
}

// 检查待发货订单（状态1=已付款待发货）
const checkNewOrders = async () => {
  try {
    const res = await getAdminOrders({ status: 1 })
    if (res.code === 200 && res.data) {
      pendingShipmentCount.value = res.data.length
    } else {
      pendingShipmentCount.value = 0
    }
  } catch (e) {
    console.error(e)
  }
}

// 启动订单轮询
const startOrderPolling = () => {
  checkNewOrders() // 立即检查一次
  orderPollingTimer = setInterval(checkNewOrders, 30000) // 每30秒检查一次
}

// 停止轮询
const stopOrderPolling = () => {
  if (orderPollingTimer) {
    clearInterval(orderPollingTimer)
    orderPollingTimer = null
  }
}

// 监听路由变化，从个人资料页返回时刷新管理员信息
watch(() => route.path, (newPath, oldPath) => {
  if (oldPath === '/admin/profile' && newPath !== '/admin/profile') {
    fetchAdminInfo()
  }
})

onMounted(async () => {
  try {
    const res = await getAdminInfo()
    if (res.code === 200) {
      adminInfo.value = res.data
      // 登录成功后启动订单轮询
      startOrderPolling()
    } else {
      router.push('/admin/login')
    }
  } catch (e) {
    router.push('/admin/login')
  }
})

onUnmounted(() => {
  stopOrderPolling()
})
</script>

<style lang="scss" scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 220px;
  background: #151829;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  flex-shrink: 0;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;

  &.collapsed {
    width: 64px;
  }
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);

  .logo-text {
    font-size: 18px;
    font-weight: 700;
    color: #ff5722;
    letter-spacing: 0.5px;
  }

  .logo-icon {
    font-size: 24px;
  }
}

.el-menu {
  border-right: none;
  padding: 8px 6px;
}

.order-menu-item {
  position: relative;

  .order-count {
    position: absolute;
    top: 10px;
    right: 16px;
    min-width: 18px;
    height: 18px;
    padding: 0 5px;
    background: #ff4d4f;
    color: #fff;
    font-size: 11px;
    font-weight: 600;
    line-height: 18px;
    text-align: center;
    border-radius: 9px;
    box-shadow: 0 2px 6px rgba(255, 77, 79, 0.35);
  }
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f3f5f9;
  margin-left: 220px;
  transition: margin-left 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &.collapsed {
    margin-left: 64px;
  }
}

.top-header {
  height: 64px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  position: relative;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.toggle-btn {
  font-size: 20px;
  cursor: pointer;
  color: #666;
  transition: color 0.2s;
  padding: 4px;
  border-radius: 6px;

  &:hover {
    color: #ff5722;
    background: rgba(255, 87, 34, 0.06);
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.role-tag {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 6px;
  letter-spacing: 0.3px;
}

.admin-name {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  color: #333;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.2s;
  font-weight: 500;
  font-size: 14px;

  &:hover {
    background: #f5f7fa;
  }
}

.avatar-small {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #ff5722, #ff7043);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  overflow: hidden;

  .avatar-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.content-area {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}
</style>
