<template>
  <div class="cart-page container">
    <div class="cart-header">
      <h2>我的购物车</h2>
      <span class="cart-count">共 <strong>{{ cartStore.cartList.length }}</strong> 件商品</span>
    </div>

    <div v-if="cartStore.cartList.length === 0" class="empty-cart">
      <el-empty description="购物车空空如也">
        <el-button type="primary" @click="$router.push('/store')">去逛逛</el-button>
      </el-empty>
    </div>

    <div v-else class="cart-content">
      <!-- 购物车列表 -->
      <div class="cart-list">
        <div class="cart-list-header">
          <el-checkbox :model-value="isAllSelected" @change="handleSelectAll">全选</el-checkbox>
          <span class="col-product">商品信息</span>
          <span class="col-price">单价</span>
          <span class="col-quantity">数量</span>
          <span class="col-total">小计</span>
          <span class="col-action">操作</span>
        </div>

        <div v-for="item in cartStore.cartList" :key="item.id" class="cart-item">
          <el-checkbox
            :model-value="cartStore.selectedIds.includes(Number(item.id))"
            @change="cartStore.toggleSelect(item.id)"
          />

          <div class="col-product">
            <img :src="item.productImage || '/placeholder.png'" class="product-img" />
            <div class="product-info">
              <div class="product-name" @click="$router.push(`/store/product/${item.productId}`)">
                {{ item.productName }}
              </div>
            </div>
          </div>

          <div class="col-price">
            <span class="price">{{ item.productPrice }}</span>
          </div>

          <div class="col-quantity">
            <el-input-number
              v-model="item.quantity"
              :min="1"
              :max="item.productStock"
              size="small"
              @change="handleQuantityChange(item)"
            />
          </div>

          <div class="col-total">
            <span class="price">{{ (item.productPrice * item.quantity).toFixed(2) }}</span>
          </div>

          <div class="col-action">
            <el-button type="danger" text size="small" @click="handleDelete(item)">
              删除
            </el-button>
          </div>
        </div>
      </div>

      <!-- 结算栏 -->
      <div class="cart-footer">
        <div class="footer-left">
          <el-checkbox :model-value="isAllSelected" @change="handleSelectAll">全选</el-checkbox>
          <el-button text type="danger" @click="handleClearSelection">取消选择</el-button>
          <el-button text type="danger" @click="handleClearCart">清空购物车</el-button>
        </div>

        <div class="footer-right">
          <div class="total-info">
            <span>已选择 <strong>{{ cartStore.selectedIds.length }}</strong> 件</span>
            <div class="total-price">
              合计：<span class="price">{{ cartStore.totalPrice().toFixed(2) }}</span>
            </div>
          </div>
          <el-button
            type="primary"
            size="large"
            class="checkout-btn"
            :disabled="cartStore.selectedIds.length === 0"
            @click="handleCheckout"
          >
            结 算
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/store/cart'
import { updateCartQuantity, deleteCartItem, clearCart as clearCartApi } from '@/api/cart'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const cartStore = useCartStore()

// 计算是否全选
const isAllSelected = computed(() => {
  if (cartStore.cartList.length === 0) return false
  return cartStore.selectedIds.length === cartStore.cartList.length
})

// 全选/取消全选
const handleSelectAll = (val) => {
  cartStore.selectAll(val)
}

const handleClearSelection = () => {
  cartStore.clearSelection()
}

const handleQuantityChange = async (item) => {
  try {
    const res = await updateCartQuantity(item.id, item.quantity)
    if (res.code !== 200) {
      ElMessage.error(res.message)
      cartStore.fetchCartList()
    }
  } catch (e) {
    ElMessage.error('更新失败')
    cartStore.fetchCartList()
  }
}

const handleDelete = async (item) => {
  try {
    await ElMessageBox.confirm('确定删除该商品吗？', '提示', { type: 'warning' })
    const res = await deleteCartItem(item.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      // 从selectedIds中移除已删除的项
      const index = cartStore.selectedIds.indexOf(item.id)
      if (index > -1) {
        cartStore.selectedIds.splice(index, 1)
      }
      cartStore.fetchCartList()
      cartStore.fetchCartCount()
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleClearCart = async () => {
  try {
    await ElMessageBox.confirm('确定清空购物车吗？', '提示', { type: 'warning' })
    const res = await clearCartApi()
    if (res.code === 200) {
      ElMessage.success('清空成功')
      cartStore.clearSelection()
      cartStore.fetchCartList()
      cartStore.fetchCartCount()
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('清空失败')
  }
}

const handleCheckout = () => {
  if (cartStore.selectedIds.length === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }
  router.push({ path: '/store/orders', query: { cartIds: cartStore.selectedIds.join(',') } })
}

onMounted(() => {
  cartStore.fetchCartList()
})
</script>

<style lang="scss" scoped>
.cart-page {
  padding: 20px 15px;
}

.cart-header {
  display: flex;
  align-items: baseline;
  gap: 16px;
  margin-bottom: 20px;

  h2 {
    font-size: 24px;
    color: #333;
    font-weight: bold;
  }

  .cart-count {
    color: #999;
    font-size: 14px;

    strong {
      color: #FF4400;
    }
  }
}

.empty-cart {
  background: #fff;
  padding: 80px 0;
  border-radius: 8px;
}

.cart-content {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.cart-list-header {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 2px solid #FF4400;
  font-weight: bold;
  color: #666;

  .col-product { flex: 1; margin-left: 12px; }
  .col-price { width: 120px; text-align: center; }
  .col-quantity { width: 140px; text-align: center; }
  .col-total { width: 100px; text-align: center; }
  .col-action { width: 80px; text-align: center; }
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #f5f5f5;

  &:hover {
    background: #fafafa;
  }
}

.col-product {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: 12px;
}

.product-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #eee;
  cursor: pointer;
}

.product-name {
  color: #333;
  font-size: 14px;
  cursor: pointer;

  &:hover {
    color: #FF4400;
  }
}

.col-price, .col-quantity, .col-total, .col-action {
  text-align: center;
}

.cart-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.total-info {
  text-align: right;

  span {
    color: #666;
    font-size: 14px;

    strong {
      color: #FF4400;
    }
  }

  .total-price {
    margin-top: 4px;
    font-size: 14px;

    .price {
      font-size: 24px;
      color: #FF4400;
      font-weight: bold;
    }
  }
}

.checkout-btn {
  min-width: 150px;
  height: 50px;
  font-size: 18px;
  background: linear-gradient(135deg, #FF4400, #FF6633);
  border: none;

  &:hover {
    background: linear-gradient(135deg, #FF6633, #FF8866);
  }
}
</style>
