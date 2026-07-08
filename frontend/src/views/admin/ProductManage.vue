<template>
  <div class="product-manage">
    <div class="page-header">
      <h2>商品管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>添加商品
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true">
        <el-form-item label="关键词">
          <el-input v-model="searchKeyword" placeholder="商品名称" clearable @keyup.enter="fetchProducts" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchCategoryId" placeholder="全部分类" clearable style="width: 160px;">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchStatus" placeholder="全部" clearable style="width: 120px;">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchProducts">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 商品列表 -->
    <div class="table-card">
      <el-table :data="products" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="商品图片" width="100">
          <template #default="{ row }">
            <img :src="row.image || '/placeholder.png'" class="product-img" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column label="价格" width="100">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button text :type="row.status === 1 ? 'warning' : 'success'" size="small" @click="handleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 添加/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商品' : '添加商品'" width="650px" top="5vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%;">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="价格" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="form.stock" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- ===================================================================== -->
        <!-- 商品图片上传区域 -->
        <!-- ===================================================================== -->
        <el-form-item label="商品图片">
          <div class="image-upload-area">
            <!--
              el-upload 图片上传组件
              上传流程：选择文件 → 前端验证 → 自动上传 → 后端处理 → 返回URL → 显示预览

              属性说明：
              - action: 上传接口地址
              - headers: 请求头，携带Token身份验证
              - show-file-list: 是否显示文件列表
              - before-upload: 上传前验证函数
              - on-success: 上传成功回调
              - on-error: 上传失败回调
              - accept: 文件选择器接受的文件类型
            -->
            <el-upload
              class="image-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :before-upload="beforeUpload"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              accept="image/*"
            >
              <!-- 上传组件内容：有图片显示预览图，无图片显示上传占位符 -->
              <img v-if="form.image" :src="form.image" class="preview-image" @error="handleImageError" />
              <div v-else class="upload-placeholder">
                <el-icon size="40"><Upload /></el-icon>
                <span>点击上传图片</span>
              </div>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="商品描述">
          <div class="description-box">
            <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入商品描述" />
            <el-button
              type="primary"
              class="ai-btn"
              :loading="aiLoading"
              :disabled="!form.name"
              @click="handleAiGenerate"
            >
              <span v-if="!aiLoading">✨ AI一键生成简介</span>
              <span v-else>AI生成中...</span>
            </el-button>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
// 导入Vue核心函数
import { ref, reactive, computed, onMounted } from 'vue'
// 导入API接口函数
import { getAdminProducts, addProduct, updateProduct, deleteProduct, updateProductStatus, getAdminCategories, generateDescription } from '@/api/admin'
// 导入Element Plus消息提示组件
import { ElMessage, ElMessageBox } from 'element-plus'

// ==================== 响应式变量 ====================
const loading = ref(false)           // 表格加载状态
const submitLoading = ref(false)     // 提交按钮加载状态
const aiLoading = ref(false)         // AI生成加载状态
const products = ref([])             // 商品列表数据（从后端获取）
const categories = ref([])           // 分类列表数据（从后端获取）
const dialogVisible = ref(false)     // 对话框显示/隐藏
const isEdit = ref(false)            // 当前是添加(false)还是编辑(true)模式

// ==================== 图片上传配置 ====================
const uploadUrl = '/api/upload/image'  // 上传接口地址

// 上传请求头：携带管理员Token进行身份验证
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('adminToken') || ''}`
}))

// 上传前验证：检查文件类型和大小
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')  // 是否是图片
  const isLt2M = file.size / 1024 / 1024 < 2      // 是否小于2MB

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 上传成功回调：把返回的图片URL存入表单
const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    const imgUrl = response.data + '?t=' + Date.now()  // 添加时间戳防止缓存
    Object.assign(form, { image: imgUrl })  // 更新表单中的图片字段
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 上传失败回调
const handleUploadError = () => {
  ElMessage.error('图片上传失败')
}

// 图片加载失败时的处理
const handleImageError = (e) => {
  console.warn('图片加载失败:', e.target.src)
}

// ==================== 表单相关 ====================
const formRef = ref(null)  // 表单引用，用于验证

// 搜索条件
const searchKeyword = ref('')        // 搜索关键词
const searchCategoryId = ref(null)   // 搜索分类ID
const searchStatus = ref(null)       // 搜索状态

// 表单数据（添加/编辑商品时使用）
const form = reactive({
  id: null,           // 商品ID（编辑时有值）
  name: '',           // 商品名称
  categoryId: null,   // 分类ID
  price: 0,           // 价格
  stock: 0,           // 库存
  image: '',          // 图片URL
  description: ''     // 商品描述
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

// ==================== 数据获取 ====================
// 获取商品列表（支持搜索筛选）
const fetchProducts = async () => {
  loading.value = true  // 显示加载动画
  try {
    const res = await getAdminProducts({
      keyword: searchKeyword.value,
      categoryId: searchCategoryId.value,
      status: searchStatus.value
    })
    if (res.code === 200) {
      products.value = res.data  // 更新商品列表
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false  // 隐藏加载动画
  }
}

// 获取分类列表（用于下拉框选项）
const fetchCategories = async () => {
  try {
    const res = await getAdminCategories()
    if (res.code === 200) {
      categories.value = res.data  // 更新分类列表
    }
  } catch (e) {
    console.error(e)
  }
}

// 重置搜索条件
const resetSearch = () => {
  searchKeyword.value = ''
  searchCategoryId.value = null
  searchStatus.value = null
  fetchProducts()  // 重新获取数据
}

// ==================== 操作处理 ====================
// 点击"添加商品"按钮：打开对话框，重置表单
const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: null, name: '', categoryId: null, price: 0, stock: 0, image: '', description: '' })
  dialogVisible.value = true
}

// 点击"编辑"按钮：打开对话框，填入当前行数据
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)  // 把商品数据填入表单
  dialogVisible.value = true
}

// 上下架切换：0=下架，1=上架
const handleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1  // 切换状态
  try {
    const res = await updateProductStatus(row.id, newStatus)
    if (res.code === 200) {
      ElMessage.success('操作成功')
      fetchProducts()  // 刷新列表
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

// 删除商品：弹出确认框，确认后删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除商品"${row.name}"吗？`, '提示', { type: 'warning' })
    const res = await deleteProduct(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchProducts()  // 刷新列表
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')  // 取消不算错误
  }
}

// 提交表单（添加或编辑）
const handleSubmit = async () => {
  // 1. 验证表单
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return  // 验证失败则不提交

  submitLoading.value = true  // 显示加载动画
  try {
    // 2. 准备数据（删除不需要的字段）
    const data = { ...form }
    delete data.id            // 添加时不需要id
    delete data.categoryName  // 不需要分类名称

    // 3. 调用API（根据模式调用不同接口）
    let res
    if (isEdit.value) {
      res = await updateProduct(form.id, data)  // 编辑：PUT /api/admin/products/:id
    } else {
      res = await addProduct(data)  // 添加：POST /api/admin/products
    }

    // 4. 处理结果
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
      dialogVisible.value = false  // 关闭对话框
      fetchProducts()  // 刷新列表
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false  // 隐藏加载动画
  }
}

// ==================== 页面初始化 ====================
onMounted(() => {
  fetchProducts()    // 页面加载时获取商品列表
  fetchCategories()  // 页面加载时获取分类列表
})

// ==================== AI生成商品简介 ====================
const handleAiGenerate = async () => {
  // 1. 验证：必须先输入商品名称
  if (!form.name) {
    ElMessage.warning('请先输入商品名称')
    return
  }

  aiLoading.value = true  // 2. 显示加载动画
  try {
    // 3. 查找分类名称（从分类列表中根据categoryId找到name）
    const cat = categories.value.find(c => c.id === form.categoryId)
    const categoryName = cat ? cat.name : '未分类'

    // 4. 调用后端AI接口
    const res = await generateDescription({
      productName: form.name,       // 商品名称
      category: categoryName,      // 分类名称
      price: form.price            // 价格
    })

    // 5. 处理结果
    if (res.code == 200 && res.data) {
      const desc = String(res.data).trim()  // 转字符串并去空格
      form.description = desc  // 把AI文案填入描述输入框
      ElMessage.success('AI简介生成成功')
    } else {
      ElMessage.error('生成失败，请重试')
    }
  } catch (e) {
    console.error('AI生成失败:', e)
    ElMessage.error('AI服务调用失败')
  } finally {
    aiLoading.value = false  // 6. 隐藏加载动画
  }
}
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

.product-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #eee;
}

.price {
  color: #FF4400;
  font-weight: bold;
}

.image-upload-area {
  width: 100%;

  .image-uploader {
    :deep(.el-upload) {
      border: 1px dashed #d9d9d9;
      border-radius: 8px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: border-color 0.3s;
      width: 180px;
      height: 180px;

      &:hover {
        border-color: #FF4400;
      }
    }
  }

  .preview-image {
    width: 180px;
    height: 180px;
    display: block;
    object-fit: cover;
  }

  .image-uploader:hover .preview-image {
    opacity: 0.7;
  }

  .upload-placeholder {
    width: 180px;
    height: 180px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #999;
    gap: 8px;

    span {
      font-size: 13px;
    }
  }
}

.description-box {
  width: 100%;
  position: relative;

  .ai-btn {
    margin-top: 8px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    color: #fff;
    font-weight: 500;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }

    &:active {
      transform: translateY(0);
    }
  }
}
</style>
