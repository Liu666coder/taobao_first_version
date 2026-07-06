<template>
  <div class="dashboard">
    <div class="welcome-banner">
      <div class="welcome-info">
        <h2>👋 欢迎回来，{{ adminInfo?.realName || '管理员' }}</h2>
        <p>{{ getGreeting() }}，祝您工作愉快！</p>
      </div>
      <div class="welcome-time">
        <el-icon><Clock /></el-icon>
        {{ currentTime }}
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card" v-for="stat in visibleStats" :key="stat.label">
        <div class="stat-icon" :style="{ background: stat.color }">
          <el-icon :size="28"><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <div class="quick-actions">
      <h3>快捷操作</h3>
      <div class="action-grid">
        <div class="action-item" @click="$router.push('/admin/products')" v-if="hasProductPermission">
          <el-icon size="24"><Goods /></el-icon>
          <span>商品管理</span>
        </div>
        <div class="action-item" @click="$router.push('/admin/orders')">
          <el-icon size="24"><Document /></el-icon>
          <span>订单管理</span>
        </div>
        <div class="action-item" @click="$router.push('/admin/users')" v-if="hasUserPermission">
          <el-icon size="24"><User /></el-icon>
          <span>用户管理</span>
        </div>
        <div class="action-item" @click="$router.push('/admin/categories')" v-if="hasProductPermission">
          <el-icon size="24"><Grid /></el-icon>
          <span>分类管理</span>
        </div>
        <div class="action-item" @click="$router.push('/admin/admins')" v-if="isAdmin">
          <el-icon size="24"><UserFilled /></el-icon>
          <span>管理员管理</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { getAdminInfo, getAdminProducts, getAdminCategories, getUserList, getAdminOrders } from '@/api/admin'

const adminInfo = ref(null)
let timerInterval = null
const currentTime = ref('')

const allStats = ref([
  { label: '注册用户', value: 0, icon: 'User', color: '#FF4400', key: 'users', roles: ['MARKETING_MANAGER', 'SYSTEM_ADMIN'] },
  { label: '商品总数', value: 0, icon: 'Goods', color: '#67C23A', key: 'products', roles: ['PRODUCT_MANAGER', 'MARKETING_MANAGER', 'SYSTEM_ADMIN'] },
  { label: '订单总数', value: 0, icon: 'Document', color: '#409EFF', key: 'orders', roles: ['PRODUCT_MANAGER', 'MARKETING_MANAGER', 'SYSTEM_ADMIN'] },
  { label: '商品分类', value: 0, icon: 'Grid', color: '#E6A23C', key: 'categories', roles: ['PRODUCT_MANAGER', 'MARKETING_MANAGER', 'SYSTEM_ADMIN'] }
])

const hasProductPermission = computed(() => {
  const role = adminInfo.value?.role
  return role === 'PRODUCT_MANAGER' || role === 'MARKETING_MANAGER' || role === 'SYSTEM_ADMIN'
})

const hasUserPermission = computed(() => {
  const role = adminInfo.value?.role
  return role === 'MARKETING_MANAGER' || role === 'SYSTEM_ADMIN'
})

const isAdmin = computed(() => adminInfo.value?.role === 'SYSTEM_ADMIN')

// 根据当前角色过滤显示的统计卡片
const visibleStats = computed(() => {
  const role = adminInfo.value?.role
  if (!role) return allStats.value
  return allStats.value.filter(stat => stat.roles.includes(role))
})

const getGreeting = () => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 17) return '下午好'
  if (hour < 19) return '傍晚好'
  return '晚上好'
}

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const fetchStats = async () => {
  try {
    // 先获取管理员信息确定角色
    const adminRes = await getAdminInfo()
    if (adminRes.code === 200) {
      adminInfo.value = adminRes.data
    } else {
      return
    }

    const role = adminInfo.value?.role

    // 根据角色并行请求有权限的数据
    const promises = []
    const promiseKeys = []

    // 所有角色都可以查看商品和分类
    promises.push(getAdminProducts())
    promiseKeys.push('products')

    promises.push(getAdminCategories())
    promiseKeys.push('categories')

    // 所有角色都可以查看订单
    promises.push(getAdminOrders())
    promiseKeys.push('orders')

    // 只有营销经理和系统管理员可以查看用户列表
    if (role === 'MARKETING_MANAGER' || role === 'SYSTEM_ADMIN') {
      promises.push(getUserList())
      promiseKeys.push('users')
    }

    const results = await Promise.all(promises)

    results.forEach((res, index) => {
      const key = promiseKeys[index]
      const statItem = allStats.value.find(s => s.key === key)
      if (statItem && res.code === 200 && Array.isArray(res.data)) {
        statItem.value = res.data.length
      }
    })
  } catch (e) {
    console.error('获取统计数据失败:', e)
  }
}

onMounted(() => {
  fetchStats()
  updateTime()
  timerInterval = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
})
</script>

<style lang="scss" scoped>
.dashboard {
  max-width: 1200px;
}

.welcome-banner {
  background: linear-gradient(135deg, #FF4400, #FF6633);
  border-radius: 12px;
  padding: 32px;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.welcome-info {
  h2 {
    font-size: 24px;
    margin-bottom: 8px;
  }

  p {
    font-size: 16px;
    opacity: .9;
  }
}

.welcome-time {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,.06);
  transition: transform .3s, box-shadow .3s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0,0,0,.12);
  }
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 4px;
}

.quick-actions {
  background: #fff;
  border-radius: 12px;
  padding: 24px;

  h3 {
    font-size: 18px;
    color: #333;
    margin-bottom: 20px;
  }
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 24px;
  background: #f9f9f9;
  border-radius: 8px;
  cursor: pointer;
  transition: all .3s;

  &:hover {
    background: #fff1f0;
    color: #FF4400;
    transform: translateY(-2px);
  }

  span {
    font-size: 14px;
    color: #666;
  }
}
</style>
