<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-bg">
        <span class="bg-blob b1"></span>
        <span class="bg-blob b2"></span>
        <span class="bg-blob b3"></span>
      </div>
      <div class="banner-content">
        <div class="banner-left">
          <span class="wave-emoji">👋</span>
          <div class="banner-text">
            <h2>欢迎回来，{{ adminInfo?.realName || '管理员' }}</h2>
            <p>{{ getGreeting() }}，祝您工作愉快！</p>
          </div>
        </div>
        <div class="banner-right">
          <div class="time-badge">
            <el-icon :size="15"><Clock /></el-icon>
            <span>{{ currentTime }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 数据概览 -->
    <div class="stats-section">
      <div
        class="stat-card"
        v-for="(stat, idx) in visibleStats"
        :key="stat.label"
      >
        <div class="stat-icon" :class="'icon-' + stat.key">
          <el-icon :size="24"><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-body">
          <span class="stat-num">{{ stat.value }}</span>
          <span class="stat-text">{{ stat.label }}</span>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-panel">
      <div class="panel-header">
        <span class="panel-title">快捷操作</span>
        <span class="panel-divider"></span>
      </div>
      <div class="panel-grid">
        <div
          class="grid-cell"
          v-for="action in actions"
          :key="action.label"
          @click="$router.push(action.path)"
        >
          <div class="cell-icon">
            <el-icon :size="20"><component :is="action.icon" /></el-icon>
          </div>
          <span class="cell-label">{{ action.label }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAdminInfo, getAdminProducts, getAdminCategories, getUserList, getAdminOrders } from '@/api/admin'

const router = useRouter()
const adminInfo = ref(null)
const currentTime = ref('')
let timer = null

// 数据卡片
const allStats = ref([
  { label: '注册用户', value: 0, icon: 'User', key: 'users', roles: ['MARKETING_MANAGER', 'SYSTEM_ADMIN'] },
  { label: '商品总数', value: 0, icon: 'Goods', key: 'products', roles: ['PRODUCT_MANAGER', 'MARKETING_MANAGER', 'SYSTEM_ADMIN'] },
  { label: '订单总数', value: 0, icon: 'Document', key: 'orders', roles: ['PRODUCT_MANAGER', 'MARKETING_MANAGER', 'SYSTEM_ADMIN'] },
  { label: '商品分类', value: 0, icon: 'Grid', key: 'categories', roles: ['PRODUCT_MANAGER', 'MARKETING_MANAGER', 'SYSTEM_ADMIN'] }
])

// 快捷操作
const allActions = [
  { label: '商品管理', icon: 'Goods', path: '/admin/products', perm: 'product' },
  { label: '订单管理', icon: 'Document', path: '/admin/orders', perm: 'all' },
  { label: '用户管理', icon: 'User', path: '/admin/users', perm: 'user' },
  { label: '分类管理', icon: 'Grid', path: '/admin/categories', perm: 'product' },
  { label: '管理员管理', icon: 'UserFilled', path: '/admin/admins', perm: 'admin' },
  { label: '操作日志', icon: 'Notebook', path: '/admin/logs', perm: 'admin' }
]

const hasProductPermission = computed(() => {
  const r = adminInfo.value?.role
  return r === 'PRODUCT_MANAGER' || r === 'MARKETING_MANAGER' || r === 'SYSTEM_ADMIN'
})
const hasUserPermission = computed(() => {
  const r = adminInfo.value?.role
  return r === 'MARKETING_MANAGER' || r === 'SYSTEM_ADMIN'
})
const isAdmin = computed(() => adminInfo.value?.role === 'SYSTEM_ADMIN')

const visibleStats = computed(() => {
  const role = adminInfo.value?.role
  if (!role) return allStats.value
  return allStats.value.filter(s => s.roles.includes(role))
})

const actions = computed(() => {
  return allActions.filter(a => {
    if (a.perm === 'all') return true
    if (a.perm === 'product') return hasProductPermission.value
    if (a.perm === 'user') return hasUserPermission.value
    if (a.perm === 'admin') return isAdmin.value
    return false
  })
})

const getGreeting = () => {
  const h = new Date().getHours()
  if (h < 6) return '凌晨好'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 17) return '下午好'
  if (h < 19) return '傍晚好'
  return '晚上好'
}

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit'
  })
}

const fetchStats = async () => {
  try {
    const adminRes = await getAdminInfo()
    if (adminRes.code !== 200) return
    adminInfo.value = adminRes.data

    const role = adminInfo.value?.role
    const tasks = [
      { key: 'products', fn: getAdminProducts() },
      { key: 'categories', fn: getAdminCategories() },
      { key: 'orders', fn: getAdminOrders() }
    ]
    if (role === 'MARKETING_MANAGER' || role === 'SYSTEM_ADMIN') {
      tasks.push({ key: 'users', fn: getUserList() })
    }

    const results = await Promise.all(tasks.map(t => t.fn))
    results.forEach((res, i) => {
      const stat = allStats.value.find(s => s.key === tasks[i].key)
      if (stat && res.code === 200 && Array.isArray(res.data)) {
        stat.value = res.data.length
      }
    })
  } catch (e) {
    console.error('获取统计数据失败:', e)
  }
}

onMounted(() => {
  fetchStats()
  updateTime()
  timer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timer) { clearInterval(timer); timer = null }
})
</script>

<style lang="scss" scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

/* ==================== 欢迎横幅 ==================== */
.welcome-banner {
  position: relative;
  border-radius: 14px;
  overflow: hidden;
  margin-bottom: 24px;
  background: linear-gradient(135deg, #ff5722 0%, #ff7043 50%, #ff8a65 100%);
  box-shadow: 0 6px 24px rgba(255, 87, 34, 0.22);
}

.banner-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;

  .bg-blob {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.07);

    &.b1 { width: 240px; height: 240px; top: -70px; right: -30px; }
    &.b2 { width: 140px; height: 140px; bottom: -50px; right: 180px; background: rgba(255,255,255,0.04); }
    &.b3 { width: 80px;  height: 80px;  top: 15px;   right: 260px; background: rgba(255,255,255,0.035); }
  }
}

.banner-content {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 36px 40px;
}

.banner-left {
  display: flex;
  align-items: flex-start;
  gap: 16px;

  .wave-emoji {
    font-size: 36px;
    line-height: 1;
    margin-top: 2px;
  }
}

.banner-text {
  h2 {
    font-size: 26px;
    font-weight: 700;
    color: #fff;
    letter-spacing: 0.5px;
    margin-bottom: 6px;
  }

  p {
    font-size: 15px;
    color: rgba(255, 255, 255, 0.85);
    letter-spacing: 0.3px;
  }
}

.time-badge {
  display: flex;
  align-items: center;
  gap: 7px;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(6px);
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  padding: 8px 18px;
  border-radius: 20px;
  letter-spacing: 0.3px;
}

/* ==================== 数据概览 ==================== */
.stats-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.03);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 28px rgba(0, 0, 0, 0.08);
  }
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;

  &.icon-users    { background: linear-gradient(135deg, #ff7043, #ff5722); }
  &.icon-products { background: linear-gradient(135deg, #66bb6a, #43a047); }
  &.icon-orders   { background: linear-gradient(135deg, #42a5f5, #1e88e5); }
  &.icon-categories { background: linear-gradient(135deg, #ffca28, #ffb300); }
}

.stat-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-num {
  font-size: 30px;
  font-weight: 800;
  color: #1a1a2e;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}

.stat-text {
  font-size: 13px;
  color: #999;
  letter-spacing: 0.5px;
}

/* ==================== 快捷操作 ==================== */
.quick-panel {
  background: #fff;
  border-radius: 12px;
  padding: 28px 32px 32px;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.03);
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;

  .panel-title {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a2e;
    white-space: nowrap;
  }

  .panel-divider {
    flex: 1;
    height: 1px;
    background: #f0f0f0;
  }
}

.panel-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
}

.grid-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 28px 12px 24px;
  border-radius: 10px;
  background: #f8f9fb;
  cursor: pointer;
  transition: all 0.25s ease;
  border: 1px solid transparent;

  .cell-icon {
    width: 44px;
    height: 44px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #eaedf3;
    color: #8c8c8c;
    transition: all 0.25s ease;
  }

  .cell-label {
    font-size: 13px;
    color: #666;
    font-weight: 500;
    transition: color 0.25s ease;
  }

  &:hover {
    background: #fff7f5;
    border-color: rgba(255, 87, 34, 0.1);
    transform: translateY(-3px);
    box-shadow: 0 6px 20px rgba(255, 87, 34, 0.1);

    .cell-icon {
      background: #ff5722;
      color: #fff;
      box-shadow: 0 4px 12px rgba(255, 87, 34, 0.3);
    }

    .cell-label {
      color: #ff5722;
      font-weight: 600;
    }
  }
}

/* ==================== 响应式 ==================== */
@media (max-width: 1200px) {
  .stats-section { grid-template-columns: repeat(2, 1fr); }
  .panel-grid { grid-template-columns: repeat(3, 1fr); }
}

@media (max-width: 768px) {
  .banner-content { flex-direction: column; gap: 16px; align-items: flex-start; }
  .stats-section { grid-template-columns: 1fr; }
  .panel-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
