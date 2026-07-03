<template>
  <div class="order-manage">
    <div class="page-header">
      <h2>订单管理</h2>
    </div>

    <div class="search-bar">
      <el-form :inline="true">
        <el-form-item label="关键词">
          <el-input v-model="searchKeyword" placeholder="订单号/用户名" clearable @keyup.enter="fetchOrders" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchStatus" placeholder="全部" clearable>
            <el-option label="待付款" :value="0" />
            <el-option label="已付款" :value="1" />
            <el-option label="已发货" :value="2" />
            <el-option label="已完成" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchOrders">搜索</el-button>
          <el-button @click="searchKeyword = ''; searchStatus = null; fetchOrders()">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <el-table :data="orders" v-loading="loading" style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="username" label="用户" width="100" />
        <el-table-column label="金额" width="100">
          <template #default="{ row }">
            <span class="price">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" width="170">
          <template #default="{ row }">
            {{ row.createTime }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="viewDetail(row)">查看详情</el-button>
            <el-button v-if="row.status === 1" text type="warning" size="small" @click="handleShip(row)">发货</el-button>
            <el-button v-if="row.status === 2" text type="success" size="small" @click="handleComplete(row)">完成</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="detailVisible" title="订单详情" width="650px">
      <div v-if="currentOrder" class="detail-content">
        <div class="detail-info">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="用户">{{ currentOrder.username }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getStatusType(currentOrder.status)">
                {{ getStatusText(currentOrder.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="订单金额">
              <span class="price">¥{{ currentOrder.totalAmount }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="下单时间" :span="2">{{ currentOrder.createTime }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <h4 style="margin: 20px 0 12px;">订单商品</h4>
        <el-table v-if="currentOrder.items" :data="currentOrder.items" style="width: 100%" border>
          <el-table-column prop="productName" label="商品名称" />
          <el-table-column prop="price" label="单价" width="100">
            <template #default="{ row }">
              <span class="price">¥{{ row.price }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="小计" width="100">
            <template #default="{ row }">
              <span class="price">¥{{ (row.price * row.quantity).toFixed(2) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminOrders, updateOrderStatus, getOrderDetail } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const orders = ref([])
const searchKeyword = ref('')
const searchStatus = ref(null)
const detailVisible = ref(false)
const currentOrder = ref(null)

const statusMap = {
  0: { text: '待付款', type: 'warning' },
  1: { text: '已付款', type: 'success' },
  2: { text: '已发货', type: 'primary' },
  3: { text: '已完成', type: 'info' }
}

const getStatusText = (status) => statusMap[status]?.text || '未知'
const getStatusType = (status) => statusMap[status]?.type || 'info'

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await getAdminOrders({ keyword: searchKeyword.value, status: searchStatus.value })
    if (res.code === 200) {
      orders.value = res.data
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const viewDetail = async (row) => {
  try {
    const res = await getOrderDetail(row.id)
    if (res.code === 200) {
      currentOrder.value = res.data
      detailVisible.value = true
    }
  } catch (e) {
    ElMessage.error('获取详情失败')
  }
}

const handleShip = async (row) => {
  try {
    await ElMessageBox.confirm('确认发货吗？', '提示', { type: 'warning' })
    const res = await updateOrderStatus(row.id, 2)
    if (res.code === 200) {
      ElMessage.success('发货成功')
      fetchOrders()
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleComplete = async (row) => {
  try {
    await ElMessageBox.confirm('确认完成订单吗？', '提示', { type: 'warning' })
    const res = await updateOrderStatus(row.id, 3)
    if (res.code === 200) {
      ElMessage.success('订单已完成')
      fetchOrders()
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

onMounted(fetchOrders)
</script>

<style lang="scss" scoped>
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
}

.search-bar {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.table-card {
  background: #fff;
  border-radius: 8px;
  padding: 0;
  overflow: hidden;
}

.detail-content {
  .detail-info {
    margin-bottom: 16px;
  }
}
</style>
