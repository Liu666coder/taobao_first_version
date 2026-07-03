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
        background-color="#1a1a2e"
        text-color="#a0aec0"
        active-text-color="#FF4400"
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

        <el-menu-item index="/admin/orders">
          <el-icon><Document /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <!-- 主内容区 -->
    <div class="main-area">
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
                <el-icon size="16"><User /></el-icon>
              </div>
              {{ adminInfo?.realName || adminInfo?.username || '管理员' }}
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
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
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getAdminInfo } from '@/api/admin'

const router = useRouter()
const route = useRoute()

const isCollapsed = ref(false)
const adminInfo = ref(null)

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

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('adminToken')
    router.push('/admin/login')
  }
}

onMounted(async () => {
  try {
    const res = await getAdminInfo()
    if (res.code === 200) {
      adminInfo.value = res.data
    } else {
      router.push('/admin/login')
    }
  } catch (e) {
    router.push('/admin/login')
  }
})
</script>

<style lang="scss" scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 220px;
  background: #1a1a2e;
  transition: width .3s;
  overflow: hidden;
  flex-shrink: 0;

  &.collapsed {
    width: 64px;
  }
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255,255,255,.1);

  .logo-text {
    font-size: 18px;
    font-weight: bold;
    color: #FF4400;
  }

  .logo-icon {
    font-size: 24px;
  }
}

.el-menu {
  border-right: none;
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
}

.top-header {
  height: 64px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,.08);
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

  &:hover {
    color: #FF4400;
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.role-tag {
  font-size: 12px;
}

.admin-name {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #333;
  padding: 6px 12px;
  border-radius: 4px;

  &:hover {
    background: #f5f5f5;
  }
}

.avatar-small {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #FF4400, #FF6633);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.content-area {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}
</style>
