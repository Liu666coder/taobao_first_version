<template>
  <div class="home container">
    <!-- 轮播图 -->
    <div class="banner-section">
      <el-carousel height="400px" :interval="5000" pause-on-hover>
        <el-carousel-item v-for="item in banners" :key="item.id">
          <div class="banner-item">
            <img :src="item.image" alt="banner" />
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 商品列表 -->
    <div class="section">
      <div class="section-header">
        <div class="section-title">
          <span class="title-icon">🔥</span>
          <h2>{{ currentCategory ? '分类商品' : '热门商品' }}</h2>
        </div>
        <el-link type="primary" :underline="false">查看更多 ></el-link>
      </div>

      <div v-if="loading" class="loading">
        <el-icon class="is-loading" :size="40"><Loading /></el-icon>
        <p>加载中...</p>
      </div>

      <div v-else-if="products.length === 0" class="empty">
        <el-empty description="暂无商品">
          <el-button type="primary" @click="filterByCategory(null)">查看全部</el-button>
        </el-empty>
      </div>

      <div v-else class="product-grid">
        <div
          v-for="product in products"
          :key="product.id"
          class="product-card"
          @click="goDetail(product.id)"
        >
          <div class="product-image">
            <img :src="product.image || getDefaultImage(product.categoryId)" :alt="product.name" />
            <div class="product-tag" v-if="product.stock < 10">仅剩{{ product.stock }}件</div>
          </div>
          <div class="product-info">
            <h3 class="product-name">{{ product.name }}</h3>
            <div class="product-bottom">
              <span class="price">{{ product.price }}</span>
              <span class="sales">已售{{ Math.floor(Math.random() * 1000) }}+</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, inject, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getProducts } from '@/api/product'

const router = useRouter()
const route = useRoute()
const currentCategory = inject('currentCategory')

const loading = ref(false)
const products = ref([])

const banners = ref([
  { id: 1, image: '/images/开学季大促图片.png' },
  { id: 2, image: '/images/二手好物图片.jpg' },
  { id: 3, image: '/images/数码专区图片.png' }
])

// 根据分类ID返回默认图片
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

const fetchProducts = async () => {
  loading.value = true
  try {
    const params = {
      keyword: route.query.keyword || '',
      categoryId: route.query.categoryId || currentCategory?.value || null
    }
    const res = await getProducts(params)
    if (res.code === 200) {
      products.value = res.data
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const goDetail = (id) => {
  router.push(`/store/product/${id}`)
}

const filterByCategory = (categoryId) => {
  currentCategory.value = categoryId
  router.push({ path: '/store', query: { categoryId } })
}

onMounted(fetchProducts)

watch(
  () => [route.query, currentCategory?.value],
  fetchProducts,
  { deep: true }
)
</script>

<style lang="scss" scoped>
.home {
  padding: 20px 15px;
}

// 轮播图
.banner-section {
  margin-bottom: 24px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,.1);
}

.banner-item {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    object-position: center top;
  }
}

// 商品区块
.section {
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #FF4400;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;

  .title-icon {
    font-size: 24px;
  }

  h2 {
    font-size: 22px;
    color: #333;
    font-weight: bold;
  }
}

.loading {
  padding: 80px 0;
  text-align: center;
  background: #fff;
  border-radius: 8px;

  p {
    margin-top: 12px;
    color: #999;
  }
}

.empty {
  padding: 80px 0;
  background: #fff;
  border-radius: 8px;
}

// 商品网格
.product-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

.product-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all .3s;
  box-shadow: 0 2px 8px rgba(0,0,0,.06);

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 12px 24px rgba(0,0,0,.15);

    .product-image img {
      transform: scale(1.05);
    }
  }
}

.product-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: #f5f5f5;
  position: relative;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform .3s;
  }
}

.product-tag {
  position: absolute;
  top: 10px;
  left: 10px;
  background: #FF4400;
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.product-info {
  padding: 12px;
}

.product-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  font-weight: normal;
  height: 40px;
  line-height: 20px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-bottom {
  display: flex;
  align-items: baseline;
  justify-content: space-between;

  .price {
    font-size: 20px;
    color: #FF4400;
    font-weight: bold;
  }

  .sales {
    font-size: 12px;
    color: #999;
  }
}
</style>
