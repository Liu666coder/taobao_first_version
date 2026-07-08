<template>
  <div class="admin-log-manage">
    <div class="page-header">
      <h2>操作日志</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleScan" :loading="scanLoading">
          <el-icon><Monitor /></el-icon>扫描异常操作
        </el-button>
        <el-button type="danger" plain @click="handleClean">
          <el-icon><Delete /></el-icon>清理旧日志
        </el-button>
      </div>
    </div>

    <!-- 扫描结果卡片 -->
    <div v-if="scanResult" class="scan-result-card">
      <div class="scan-header">
        <span class="scan-title">
          <el-icon :size="18"><WarningFilled /></el-icon>
          异常操作扫描结果
        </span>
        <el-button text @click="scanResult = null">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
      <div class="scan-body">
        <div class="scan-summary">
          <div class="scan-stat">
            <span class="scan-stat-num">{{ scanResult.warningCount }}</span>
            <span class="scan-stat-text">异常警告</span>
          </div>
          <div class="scan-stat">
            <span class="scan-stat-num">{{ scanResult.actionStats?.length || 0 }}</span>
            <span class="scan-stat-text">操作类型</span>
          </div>
          <div class="scan-stat">
            <span class="scan-stat-num">{{ scanResult.adminStats?.length || 0 }}</span>
            <span class="scan-stat-text">活跃管理员</span>
          </div>
        </div>
        <div v-if="scanResult.warnings?.length" class="scan-warnings">
          <div v-for="(w, i) in scanResult.warnings" :key="i" class="warning-item">
            <el-icon color="#e6a23c"><Warning /></el-icon>
            <span>{{ w.message }}</span>
          </div>
        </div>
        <div v-else class="scan-ok">
          <el-icon color="#67c23a" :size="20"><CircleCheck /></el-icon>
          <span>未检测到异常操作，一切正常</span>
        </div>
      </div>
    </div>

    <!-- 筛选区 -->
    <div class="filter-bar">
      <el-input v-model="filters.keyword" placeholder="搜索操作/目标/详情" clearable
        style="width: 240px" @keyup.enter="fetchLogs">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="filters.action" placeholder="操作类型" clearable style="width: 160px" @change="fetchLogs">
        <el-option label="登录系统" value="登录系统" />
        <el-option label="添加管理员" value="添加管理员" />
        <el-option label="修改管理员" value="修改管理员" />
        <el-option label="删除管理员" value="删除管理员" />
        <el-option label="启用管理员" value="启用管理员" />
        <el-option label="禁用管理员" value="禁用管理员" />
        <el-option label="重置密码" value="重置密码" />
      </el-select>
      <el-button type="primary" @click="fetchLogs">
        <el-icon><Search /></el-icon>查询
      </el-button>
    </div>

    <!-- 日志表格 -->
    <div class="table-card">
      <el-table :data="logs" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="adminName" label="操作人" width="120" />
        <el-table-column prop="action" label="操作类型" width="130">
          <template #default="{ row }">
            <el-tag size="small" :type="getActionTag(row.action)">{{ row.action }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="target" label="操作对象" width="140" />
        <el-table-column prop="detail" label="操作详情" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="createTime" label="操作时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
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
import { getAdminLogs, scanAbnormalOperations, cleanOldLogs } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const scanLoading = ref(false)
const logs = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const scanResult = ref(null)

const filters = reactive({ keyword: '', action: '' })

const actionTagMap = {
  '登录系统': '',
  '添加管理员': 'success',
  '修改管理员': 'warning',
  '删除管理员': 'danger',
  '启用管理员': 'success',
  '禁用管理员': 'info',
  '重置密码': 'warning'
}
const getActionTag = (action) => actionTagMap[action] || ''

const formatTime = (t) => {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 19)
}

const fetchLogs = async () => {
  loading.value = true
  try {
    const res = await getAdminLogs({
      keyword: filters.keyword || undefined,
      action: filters.action || undefined,
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

const handleScan = async () => {
  scanLoading.value = true
  try {
    const res = await scanAbnormalOperations()
    if (res.code === 200) {
      scanResult.value = res.data
      ElMessage.success('扫描完成')
    }
  } catch (e) {
    ElMessage.error('扫描失败')
  } finally {
    scanLoading.value = false
  }
}

const handleClean = async () => {
  try {
    await ElMessageBox.confirm('确定要清理90天前的旧日志吗？此操作不可恢复。', '提示', { type: 'warning' })
    const res = await cleanOldLogs(90)
    if (res.code === 200) {
      ElMessage.success(res.data)
      fetchLogs()
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('清理失败')
  }
}

onMounted(fetchLogs)
</script>

<style lang="scss" scoped>
.admin-log-manage {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h2 {
    font-size: 22px;
    color: #333;
    font-weight: bold;
  }

  .header-actions {
    display: flex;
    gap: 10px;
  }
}

/* 扫描结果卡片 */
.scan-result-card {
  background: #fff;
  border-radius: 12px;
  margin-bottom: 20px;
  overflow: hidden;
  border: 1px solid #e8e8e8;
}

.scan-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #fff7e6, #fff1cc);
  border-bottom: 1px solid #f0d9a0;

  .scan-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    color: #e6a23c;
    font-size: 15px;
  }
}

.scan-body {
  padding: 20px;
}

.scan-summary {
  display: flex;
  gap: 40px;
  margin-bottom: 16px;
}

.scan-stat {
  display: flex;
  flex-direction: column;
  gap: 4px;

  .scan-stat-num {
    font-size: 28px;
    font-weight: 800;
    color: #1a1a2e;
  }

  .scan-stat-text {
    font-size: 13px;
    color: #999;
  }
}

.scan-warnings {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.warning-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: #fff7e6;
  border-radius: 8px;
  font-size: 14px;
  color: #666;
}

.scan-ok {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px;
  background: #f0f9eb;
  border-radius: 8px;
  font-size: 14px;
  color: #67c23a;
}

/* 筛选区 */
.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 8px;
}

/* 表格 */
.table-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
}
</style>
