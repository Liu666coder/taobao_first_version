<template>
  <div class="category-manage">
    <div class="page-header">
      <h2>分类管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>添加分类
      </el-button>
    </div>

    <div class="table-card">
      <el-table
        :data="categories"
        v-loading="loading"
        style="width: 100%"
        row-key="id"
        :expand-row-keys="expandedRows"
        @expand-change="handleExpandChange"
      >
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expand-products">
              <div v-if="row._productsLoading" class="products-loading">加载中...</div>
              <div v-else-if="!row._products || row._products.length === 0" class="products-empty">该分类下暂无商品</div>
              <template v-else>
                <div class="products-summary">
                  共 <strong>{{ row._products.length }}</strong> 件上架商品
                </div>
                <table class="products-table">
                  <thead>
                    <tr>
                      <th>商品名称</th>
                      <th>库存</th>
                      <th>价格</th>
                      <th>状态</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="p in row._products" :key="p.id">
                      <td>{{ p.name }}</td>
                      <td>{{ p.stock }}</td>
                      <td class="price">¥{{ p.price }}</td>
                      <td>
                        <el-tag :type="p.status === 1 ? 'success' : 'info'" size="small">
                          {{ p.status === 1 ? '在售' : '下架' }}
                        </el-tag>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </template>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button text :type="row.status === 1 ? 'warning' : 'success'" size="small" @click="handleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '添加分类'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
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
import { ref, reactive, onMounted } from 'vue'
import { getAdminCategories, addCategory, updateCategory, deleteCategory, updateCategoryStatus, getAdminProducts } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const categories = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const expandedRows = ref([])

const form = reactive({ id: null, name: '', description: '' })
const rules = { name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }] }

const fetchCategories = async () => {
  loading.value = true
  try {
    const res = await getAdminCategories()
    if (res.code === 200) {
      categories.value = res.data
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleExpandChange = (row, expanded) => {
  // 展开时加载商品
  if (expanded.some(r => r.id === row.id)) {
    fetchCategoryProducts(row)
  }
}

const fetchCategoryProducts = async (row) => {
  // 已加载过则跳过
  if (row._products !== undefined) return
  row._productsLoading = true
  row._products = []
  try {
    const res = await getAdminProducts({ categoryId: row.id, status: 1 })
    if (res.code === 200) {
      row._products = Array.isArray(res.data) ? res.data : []
    }
  } catch (e) {
    console.error(e)
  } finally {
    row._productsLoading = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: null, name: '', description: '' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    const res = await updateCategoryStatus(row.id, newStatus)
    if (res.code === 200) {
      ElMessage.success('操作成功')
      fetchCategories()
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除分类"${row.name}"吗？`, '提示', { type: 'warning' })
    const res = await deleteCategory(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchCategories()
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.response?.data?.message || '删除失败')
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const data = { name: form.name, description: form.description }
    let res
    if (isEdit.value) {
      res = await updateCategory(form.id, data)
    } else {
      res = await addCategory(data)
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
      dialogVisible.value = false
      fetchCategories()
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(fetchCategories)
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

.table-card {
  background: #fff;
  border-radius: 8px;
  padding: 0;
  overflow: hidden;
}

.expand-products {
  padding: 12px 20px 12px 60px;
  background: #fafafa;

  .products-loading,
  .products-empty {
    color: #999;
    font-size: 13px;
    text-align: center;
    padding: 12px 0;
  }

  .products-summary {
    font-size: 13px;
    color: #666;
    margin-bottom: 10px;

    strong {
      color: #ff4400;
      font-size: 15px;
    }
  }

  .products-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 13px;

    th {
      text-align: left;
      padding: 8px 12px;
      color: #999;
      font-weight: normal;
      border-bottom: 1px solid #eee;
    }

    td {
      padding: 8px 12px;
      color: #333;
      border-bottom: 1px solid #f0f0f0;
    }

    .price {
      color: #ff4400;
      font-weight: 500;
    }
  }
}
</style>
