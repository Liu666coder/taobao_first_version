<template>
  <div class="orders-page container">
    <div class="page-header">
      <h2>我的订单</h2>
    </div>

    <!-- 从购物车结算过来 -->
    <div v-if="isCheckout" class="checkout-card">
      <div class="checkout-header">
        <el-icon size="24" color="#67C23A"><CircleCheck /></el-icon>
        <h3>订单提交成功！</h3>
      </div>
      <div v-if="orderDetail" class="order-info">
        <p>订单号：{{ orderDetail.orderNo }}</p>
        <p>订单金额：<span class="price">{{ orderDetail.totalAmount }}</span></p>
      </div>
      <div class="checkout-actions">
        <el-button @click="handleCancelOrder">取消订单</el-button>
        <el-button type="primary" class="pay-btn" @click="handlePayOrder">立即付款</el-button>
      </div>
    </div>

    <!-- 订单列表 -->
    <div v-else class="order-list">
      <!-- 订单筛选 -->
      <div class="order-tabs">
        <el-radio-group v-model="activeTab" @change="handleTabChange">
          <el-radio-button label="all">全部订单</el-radio-button>
          <el-radio-button label="0">待付款</el-radio-button>
          <el-radio-button label="1">已付款</el-radio-button>
          <el-radio-button label="2">已发货</el-radio-button>
          <el-radio-button label="3">已完成</el-radio-button>
        </el-radio-group>
      </div>

      <div v-if="loading" class="loading">
        <el-icon class="is-loading" :size="40"><Loading /></el-icon>
        <p>加载中...</p>
      </div>

      <div v-else-if="filteredOrders.length === 0" class="empty">
        <el-empty description="暂无订单">
          <el-button type="primary" @click="$router.push('/')">去购物</el-button>
        </el-empty>
      </div>

      <div v-else class="orders">
        <div v-for="order in filteredOrders" :key="order.id" class="order-card">
          <div class="order-header">
            <div class="order-info-left">
              <span class="order-time">{{ order.createTime }}</span>
              <span class="order-no">订单号：{{ order.orderNo }}</span>
            </div>
            <el-tag :type="getStatusType(order.status)" size="small">
              {{ getStatusText(order.status) }}
            </el-tag>
          </div>

          <div class="order-body">
            <div class="order-amount">
              <span class="label">订单金额：</span>
              <span class="price">{{ order.totalAmount }}</span>
            </div>
          </div>

          <div class="order-footer">
            <el-button text type="primary" @click="viewDetail(order)">查看详情</el-button>
            <el-button v-if="order.status === 0" type="primary" size="small" @click="payOrder(order)">
              立即付款
            </el-button>
            <el-button v-if="order.status === 0" size="small" @click="cancelOrder(order)">
              取消订单
            </el-button>
            <el-button v-if="order.status === 2" type="success" size="small" @click="confirmReceive(order)">
              确认收货
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <div v-if="currentOrder" class="detail-content">
        <div class="detail-info">
          <p><strong>订单号：</strong>{{ currentOrder.orderNo }}</p>
          <p><strong>订单状态：</strong>{{ getStatusText(currentOrder.status) }}</p>
          <p><strong>订单金额：</strong><span class="price">{{ currentOrder.totalAmount }}</span></p>
          <p><strong>下单时间：</strong>{{ currentOrder.createTime }}</p>
        </div>

        <el-table v-if="currentOrder.items" :data="currentOrder.items" style="width: 100%">
          <el-table-column prop="productName" label="商品名称" />
          <el-table-column prop="price" label="单价" width="100">
            <template #default="{ row }">
              <span class="price">{{ row.price }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="小计" width="100">
            <template #default="{ row }">
              <span class="price">{{ (row.price * row.quantity).toFixed(2) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderList, getOrderDetail, payOrder as payOrderApi, cancelOrder as cancelOrderApi, confirmReceive as confirmReceiveApi } from '@/api/order'
import { createOrder } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const orders = ref([])
const isCheckout = ref(false)
const orderDetail = ref(null)
const detailVisible = ref(false)
const currentOrder = ref(null)
const activeTab = ref('all')

const statusMap = {
  0: { text: '待付款', type: 'warning' },
  1: { text: '已付款', type: 'success' },
  2: { text: '已发货', type: 'primary' },
  3: { text: '已完成', type: 'info' }
}

const getStatusText = (status) => statusMap[status]?.text || '未知'
const getStatusType = (status) => statusMap[status]?.type || 'info'

const filteredOrders = computed(() => {
  if (activeTab.value === 'all') return orders.value
  return orders.value.filter(order => order.status === parseInt(activeTab.value))
})

const handleTabChange = () => {}

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await getOrderList()
    if (res.code === 200) {
      orders.value = res.data
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleCheckoutFromCart = async () => {
  const cartIds = route.query.cartIds
  if (!cartIds) return

  isCheckout.value = true
  const ids = cartIds.split(',').map(Number)

  try {
    const res = await createOrder({ cartIds: ids })
    if (res.code === 200) {
      orderDetail.value = res.data
      // 订单创建成功后清除URL参数，防止刷新重复创建
      window.history.replaceState({}, '', '/orders')
    } else {
      ElMessage.error(res.message)
      isCheckout.value = false
      // 创建失败也清除URL参数
      window.history.replaceState({}, '', '/orders')
    }
  } catch (e) {
    ElMessage.error('创建订单失败')
    isCheckout.value = false
    window.history.replaceState({}, '', '/orders')
  }
}

const handlePayOrder = async () => {
  try {
    const res = await payOrderApi(orderDetail.value.id)
    if (res.code === 200) {
      ElMessage.success('付款成功')
      isCheckout.value = false
      router.replace('/orders')
      fetchOrders()
    }
  } catch (e) {
    ElMessage.error('付款失败')
  }
}

const handleCancelOrder = async () => {
  try {
    await ElMessageBox.confirm('确定取消订单吗？', '提示', { type: 'warning' })
    const res = await cancelOrderApi(orderDetail.value.id)
    if (res.code === 200) {
      ElMessage.success('订单已取消')
      isCheckout.value = false
      router.replace('/orders')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('取消失败')
  }
}

const viewDetail = async (order) => {
  try {
    const res = await getOrderDetail(order.id)
    if (res.code === 200) {
      currentOrder.value = res.data
      detailVisible.value = true
    }
  } catch (e) {
    ElMessage.error('获取详情失败')
  }
}

const payOrder = async (order) => {
  try {
    await ElMessageBox.confirm(`确认付款 ¥${order.totalAmount} 吗？`, '确认付款', { type: 'info' })
    const res = await payOrderApi(order.id)
    if (res.code === 200) {
      ElMessage.success('付款成功')
      fetchOrders()
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('付款失败')
  }
}

const cancelOrder = async (order) => {
  try {
    await ElMessageBox.confirm('确定取消订单吗？', '提示', { type: 'warning' })
    const res = await cancelOrderApi(order.id)
    if (res.code === 200) {
      ElMessage.success('订单已取消')
      fetchOrders()
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('取消失败')
  }
}

const confirmReceive = async (order) => {
  try {
    await ElMessageBox.confirm('确认已收到商品吗？', '确认收货', { type: 'info' })
    const res = await confirmReceiveApi(order.id)
    if (res.code === 200) {
      ElMessage.success('确认收货成功')
      fetchOrders()
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

onMounted(() => {
  if (route.query.cartIds) {
    handleCheckoutFromCart()
  } else {
    fetchOrders()
  }
})
</script>

<style lang="scss" scoped>
.orders-page {
  padding: 20px 15px;
}

.page-header {
  margin-bottom: 20px;

  h2 {
    font-size: 24px;
    color: #333;
    font-weight: bold;
  }
}

// 结算成功
.checkout-card {
  background: #fff;
  border-radius: 8px;
  padding: 40px;
  text-align: center;
}

.checkout-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 24px;

  h3 {
    font-size: 20px;
    color: #67C23A;
  }
}

.order-info {
  margin-bottom: 24px;

  p {
    margin-bottom: 8px;
    color: #666;
    font-size: 16px;
  }
}

.checkout-actions {
  display: flex;
  justify-content: center;
  gap: 16px;

  .pay-btn {
    min-width: 150px;
    height: 48px;
    font-size: 16px;
    background: linear-gradient(135deg, #FF4400, #FF6633);
    border: none;
  }
}

// 订单列表
.order-tabs {
  background: #fff;
  padding: 16px 20px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.loading, .empty {
  background: #fff;
  padding: 80px 0;
  border-radius: 8px;
  text-align: center;
}

.order-card {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 16px;
  overflow: hidden;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f9f9f9;
  border-bottom: 1px solid #eee;
}

.order-info-left {
  display: flex;
  gap: 20px;

  .order-time {
    color: #999;
    font-size: 13px;
  }

  .order-no {
    color: #666;
    font-size: 13px;
  }
}

.order-body {
  padding: 20px;
}

.order-amount {
  .label {
    color: #666;
  }
}

.order-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 16px 20px;
  border-top: 1px solid #f5f5f5;
}

.detail-content {
  .detail-info {
    margin-bottom: 20px;

    p {
      margin-bottom: 8px;
      color: #666;
    }
  }
}
</style>
