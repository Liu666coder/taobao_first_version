<template>
  <div class="security-scan">
    <div class="page-header">
      <h2>安全扫描</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleFullScan" :loading="scanLoading">
          <el-icon><Monitor /></el-icon>全面安全扫描
        </el-button>
        <el-button type="danger" plain @click="handleClean">
          <el-icon><Delete /></el-icon>清理旧日志
        </el-button>
      </div>
    </div>

    <!-- 安全概览卡片 -->
    <div class="overview-section">
      <div class="overview-card">
        <div class="card-icon icon-total">
          <el-icon :size="24"><User /></el-icon>
        </div>
        <div class="card-info">
          <span class="card-num">{{ weekSummary.totalLogins || 0 }}</span>
          <span class="card-label">本周登录总次数</span>
        </div>
      </div>
      <div class="overview-card">
        <div class="card-icon icon-success">
          <el-icon :size="24"><CircleCheck /></el-icon>
        </div>
        <div class="card-info">
          <span class="card-num">{{ weekSummary.successCount || 0 }}</span>
          <span class="card-label">登录成功</span>
        </div>
      </div>
      <div class="overview-card">
        <div class="card-icon icon-fail">
          <el-icon :size="24"><CircleClose /></el-icon>
        </div>
        <div class="card-info">
          <span class="card-num">{{ weekSummary.failCount || 0 }}</span>
          <span class="card-label">登录失败</span>
        </div>
      </div>
      <div class="overview-card">
        <div class="card-icon icon-warn">
          <el-icon :size="24"><Warning /></el-icon>
        </div>
        <div class="card-info">
          <span class="card-num">{{ scanResult?.warningCount || 0 }}</span>
          <span class="card-label">安全警告</span>
        </div>
      </div>
    </div>

    <!-- 扫描结果 -->
    <div v-if="scanResult" class="scan-result-card">
      <div class="scan-header">
        <span class="scan-title">
          <el-icon :size="18"><WarningFilled /></el-icon>
          安全扫描报告
        </span>
        <div class="scan-meta">
          <span class="scan-time">扫描时间：{{ formatTime(scanResult.scanTime) }}</span>
          <el-button text @click="scanResult = null"><el-icon><Close /></el-icon></el-button>
        </div>
      </div>
      <div class="scan-body">
        <div v-if="scanResult.warnings?.length" class="scan-warnings">
          <div v-for="(w, i) in scanResult.warnings" :key="i" class="warning-item" :class="'warning-' + w.type.toLowerCase()">
            <div class="warning-icon">
              <el-icon v-if="w.type === 'BRUTE_FORCE'" color="#e6a23c"><Warning /></el-icon>
              <el-icon v-else color="#f56c6c"><CircleClose /></el-icon>
            </div>
            <div class="warning-content">
              <span class="warning-msg">{{ w.message }}</span>
              <span class="warning-type">{{ w.type === 'BRUTE_FORCE' ? '暴力破解风险' : '可疑IP' }}</span>
            </div>
          </div>
        </div>
        <div v-else class="scan-ok">
          <el-icon color="#67c23a" :size="24"><CircleCheck /></el-icon>
          <span>系统安全，未检测到异常登录行为</span>
        </div>
      </div>
    </div>

    <!-- 筛选区 -->
    <div class="filter-bar">
      <el-input v-model="filters.username" placeholder="搜索用户名" clearable
        style="width: 200px" @keyup.enter="fetchLogs">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="filters.status" placeholder="登录状态" clearable style="width: 140px" @change="fetchLogs">
        <el-option label="成功" :value="1" />
        <el-option label="失败" :value="0" />
      </el-select>
      <el-button type="primary" @click="fetchLogs">
        <el-icon><Search /></el-icon>查询
      </el-button>
    </div>

    <!-- 登录日志表格 -->
    <div class="table-card">
      <el-table :data="logs" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column label="用户类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.userType === 'ADMIN' ? 'danger' : ''">
              {{ row.userType === 'ADMIN' ? '管理员' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="loginIp" label="IP地址" width="140" />
        <el-table-column prop="browser" label="浏览器" width="100" />
        <el-table-column prop="os" label="操作系统" width="120" />
        <el-table-column prop="message" label="消息" min-width="150" show-overflow-tooltip />
        <el-table-column prop="loginTime" label="登录时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.loginTime) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchLogs"
          @current-change="fetchLogs"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getLoginLogs, getWeekLoginSummary, fullSecurityScan, cleanOldLoginLogs } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const scanLoading = ref(false)
const logs = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const scanResult = ref(null)
const weekSummary = ref({})

const filters = reactive({ username: '', status: null })

const formatTime = (t) => {
  if (!t) return ''
  if (Array.isArray(t)) {
    // Java LocalDateTime array format
    const [y, mo, d, h, mi, s] = t
    return `${y}-${String(mo).padStart(2,'0')}-${String(d).padStart(2,'0')} ${String(h).padStart(2,'0')}:${String(mi).padStart(2,'0')}:${String(s).padStart(2,'0')}`
  }
  return t.replace('T', ' ').substring(0, 19)
}

const fetchLogs = async () => {
  loading.value = true
  try {
    const res = await getLoginLogs({
      username: filters.username || undefined,
      status: filters.status !== null ? filters.status : undefined,
      page: page.value,
      size: pageSize.value
    })
    if (res.code === 200) {
      logs.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const fetchWeekSummary = async () => {
  try {
    const res = await getWeekLoginSummary()
    if (res.code === 200 && res.data) {
      weekSummary.value = {
        totalLogins: (res.data.count || 0),
        successCount: (res.data.status || 0),
        failCount: ((res.data.count || 0) - (res.data.status || 0))
      }
    }
  } catch (e) {
    console.error(e)
  }
}

const handleFullScan = async () => {
  scanLoading.value = true
  try {
    const res = await fullSecurityScan()
    if (res.code === 200) {
      scanResult.value = res.data
      ElMessage.success('安全扫描完成')
    }
  } catch (e) {
    ElMessage.error('扫描失败')
  } finally {
    scanLoading.value = false
  }
}

const handleClean = async () => {
  try {
    await ElMessageBox.confirm('确定要清理90天前的登录日志吗？此操作不可恢复。', '提示', { type: 'warning' })
    const res = await cleanOldLoginLogs(90)
    if (res.code === 200) {
      ElMessage.success(res.data)
      fetchLogs()
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('清理失败')
  }
}

onMounted(() => {
  fetchLogs()
  fetchWeekSummary()
})
</script>

<style lang="scss" scoped>
.security-scan {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h2 { font-size: 22px; color: #333; font-weight: bold; }
  .header-actions { display: flex; gap: 10px; }
}

/* 概览卡片 */
.overview-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
  margin-bottom: 20px;
}

.overview-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 8px rgba(0,0,0,0.04);
  transition: all 0.3s ease;

  &:hover { transform: translateY(-3px); box-shadow: 0 6px 20px rgba(0,0,0,0.08); }
}

.card-icon {
  width: 52px; height: 52px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; flex-shrink: 0;

  &.icon-total  { background: linear-gradient(135deg, #42a5f5, #1e88e5); }
  &.icon-success { background: linear-gradient(135deg, #66bb6a, #43a047); }
  &.icon-fail   { background: linear-gradient(135deg, #ef5350, #e53935); }
  &.icon-warn   { background: linear-gradient(135deg, #ffa726, #fb8c00); }
}

.card-info {
  display: flex; flex-direction: column; gap: 4px;
  .card-num { font-size: 28px; font-weight: 800; color: #1a1a2e; }
  .card-label { font-size: 13px; color: #999; }
}

/* 扫描结果 */
.scan-result-card {
  background: #fff; border-radius: 12px; margin-bottom: 20px;
  overflow: hidden; border: 1px solid #e8e8e8;
}

.scan-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #fff7e6, #fff1cc);
  border-bottom: 1px solid #f0d9a0;

  .scan-title {
    display: flex; align-items: center; gap: 8px;
    font-weight: 600; color: #e6a23c; font-size: 15px;
  }
  .scan-meta { display: flex; align-items: center; gap: 12px; }
  .scan-time { font-size: 13px; color: #999; }
}

.scan-body { padding: 20px; }

.scan-warnings { display: flex; flex-direction: column; gap: 10px; }

.warning-item {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 16px; border-radius: 8px; font-size: 14px;

  &.warning-brute_force { background: #fff7e6; }
  &.warning_suspicious_ip { background: #fef0f0; }

  .warning-icon { flex-shrink: 0; }
  .warning-content {
    display: flex; flex-direction: column; gap: 2px;
    .warning-msg { color: #333; }
    .warning-type { font-size: 12px; color: #999; }
  }
}

.scan-ok {
  display: flex; align-items: center; gap: 8px;
  padding: 16px; background: #f0f9eb; border-radius: 8px;
  font-size: 14px; color: #67c23a;
}

/* 筛选区 */
.filter-bar {
  display: flex; gap: 12px; align-items: center;
  margin-bottom: 16px; padding: 16px 20px;
  background: #fff; border-radius: 8px;
}

/* 表格 */
.table-card {
  background: #fff; border-radius: 8px; overflow: hidden;
}

.pagination-wrap {
  display: flex; justify-content: flex-end; padding: 16px 20px;
}

@media (max-width: 1200px) {
  .overview-section { grid-template-columns: repeat(2, 1fr); }
}
</style>
