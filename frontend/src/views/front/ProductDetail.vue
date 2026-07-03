<template>
  <div class="product-detail container">
    <div v-if="loading" class="loading">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <div v-else-if="product" class="detail-main">
      <!-- 面包屑 -->
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>{{ product.categoryName }}</el-breadcrumb-item>
        <el-breadcrumb-item>{{ product.name }}</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="detail-content">
        <!-- 商品图片 -->
        <div class="product-gallery">
          <div class="main-image">
            <img :src="product.image || getDefaultImage(product.categoryId)" :alt="product.name" />
          </div>
        </div>

        <!-- 商品信息 -->
        <div class="product-info">
          <h1 class="product-title">{{ product.name }}</h1>

          <div class="price-box">
            <div class="price-row">
              <span class="label">促销价</span>
              <span class="price">{{ product.price }}</span>
            </div>
          </div>

          <div class="info-list">
            <div class="info-item">
              <span class="label">分类</span>
              <span class="value">{{ product.categoryName }}</span>
            </div>
            <div class="info-item">
              <span class="label">库存</span>
              <span class="value">
                <el-tag :type="product.stock > 10 ? 'success' : 'danger'" size="small">
                  {{ product.stock > 10 ? '充足' : '仅剩' + product.stock + '件' }}
                </el-tag>
              </span>
            </div>
            <div class="info-item">
              <span class="label">配送</span>
              <span class="value">校园内免费配送</span>
            </div>
          </div>

          <div class="action-section">
            <div class="quantity-box">
              <span class="label">数量</span>
              <el-input-number
                v-model="quantity"
                :min="1"
                :max="product.stock"
                size="large"
              />
              <span class="stock-tip">库存{{ product.stock }}件</span>
            </div>

            <div class="action-buttons">
              <el-button
                type="primary"
                size="large"
                class="buy-btn"
                :disabled="product.stock === 0"
                @click="handleBuyNow"
              >
                <el-icon><ShoppingCart /></el-icon>
                立即购买
              </el-button>
              <el-button
                type="warning"
                size="large"
                class="cart-btn"
                :disabled="product.stock === 0"
                @click="handleAddCart"
              >
                <el-icon><ShoppingCart /></el-icon>
                加入购物车
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 商品详情 -->
      <div class="detail-tabs">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="商品详情" name="detail">
            <div class="detail-desc">
              <h3>商品描述</h3>
              <p>{{ product.description || '暂无详细描述' }}</p>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useCartStore } from '@/store/cart'
import { getProductDetail } from '@/api/product'
import { addToCart } from '@/api/cart'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const loading = ref(false)
const product = ref(null)
const quantity = ref(1)
const activeTab = ref('detail')

const getDefaultImage = (categoryId) => {
  const images = {
    1: '/images/iPhone17Pro.jpg',
    2: '/images/高等数学.jpg',
    3: '/images/宿舍台灯.jpg',
    4: '/images/优衣库短袖.jpg',
    5: '/images/篮球.jpg'
  }
  return images[categoryId] || '/images/高等数学.jpg'
}

const fetchProduct = async () => {
  loading.value = true
  try {
    const res = await getProductDetail(route.params.id)
    if (res.code === 200) {
      product.value = res.data
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleAddCart = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }

  try {
    const res = await addToCart({
      productId: product.value.id,
      quantity: quantity.value
    })
    if (res.code === 200) {
      ElMessage.success('已添加到购物车')
      cartStore.fetchCartCount()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    ElMessage.error('添加失败')
  }
}

const handleBuyNow = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }

  // 先加入购物车，然后跳转到购物车
  try {
    const res = await addToCart({
      productId: product.value.id,
      quantity: quantity.value
    })
    if (res.code === 200) {
      cartStore.fetchCartCount()
      router.push('/cart')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(fetchProduct)
</script>

<style lang="scss" scoped>
.product-detail {
  padding: 20px 15px;
}

.loading {
  text-align: center;
  padding: 100px 0;
  background: #fff;
  border-radius: 8px;

  p {
    margin-top: 12px;
    color: #999;
  }
}

.breadcrumb {
  margin-bottom: 16px;
}

.detail-main {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}

.detail-content {
  display: flex;
  gap: 40px;
}

// 商品图片
.product-gallery {
  width: 400px;
  flex-shrink: 0;
}

.main-image {
  width: 400px;
  height: 400px;
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

// 商品信息
.product-info {
  flex: 1;
}

.product-title {
  font-size: 20px;
  color: #333;
  margin-bottom: 16px;
  font-weight: bold;
  line-height: 1.4;
}

.price-box {
  background: linear-gradient(135deg, #fff1f0 0%, #fff 100%);
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 12px;

  .label {
    color: #999;
    font-size: 14px;
  }

  .price {
    font-size: 32px;
    color: #FF4400;
    font-weight: bold;
  }
}

.info-list {
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;

  .label {
    width: 70px;
    color: #999;
  }

  .value {
    color: #333;
  }
}

.action-section {
  border-top: 1px solid #eee;
  padding-top: 24px;
}

.quantity-box {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;

  .label {
    color: #666;
  }

  .stock-tip {
    color: #999;
    font-size: 12px;
  }
}

.action-buttons {
  display: flex;
  gap: 16px;

  .el-button {
    min-width: 180px;
    height: 50px;
    font-size: 16px;
  }

  .buy-btn {
    background: linear-gradient(135deg, #FF4400, #FF6633);
    border: none;

    &:hover {
      background: linear-gradient(135deg, #FF6633, #FF8866);
    }
  }

  .cart-btn {
    background: linear-gradient(135deg, #FFA500, #FFD700);
    border: none;
    color: #fff;

    &:hover {
      background: linear-gradient(135deg, #FFD700, #FFEC8B);
    }
  }
}

// 商品详情
.detail-tabs {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.detail-desc {
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;

  h3 {
    font-size: 16px;
    color: #333;
    margin-bottom: 12px;
  }

  p {
    color: #666;
    line-height: 1.8;
  }
}
</style>
