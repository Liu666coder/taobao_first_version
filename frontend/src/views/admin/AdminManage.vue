<template>
  <div class="admin-manage">
    <div class="page-header">
      <h2>管理员管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>添加管理员
      </el-button>
    </div>

    <div class="table-card">
      <el-table :data="admins" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="getRoleTag(row.role)">{{ getRoleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button text type="warning" size="small" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button text :type="row.status === 1 ? 'warning' : 'success'" size="small" @click="handleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑管理员' : '添加管理员'" width="500px">
      <el-form ref="formRef" :model="form" :rules="getRules()" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%;">
            <el-option label="商品专员" value="PRODUCT_MANAGER" />
            <el-option label="营销经理" value="MARKETING_MANAGER" />
            <el-option label="系统管理员" value="SYSTEM_ADMIN" />
          </el-select>
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
import { getAdminList, addAdmin, updateAdmin, deleteAdmin, updateAdminStatus, resetPassword } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const admins = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const roleMap = {
  'PRODUCT_MANAGER': { text: '商品专员', tag: '' },
  'MARKETING_MANAGER': { text: '营销经理', tag: 'warning' },
  'SYSTEM_ADMIN': { text: '系统管理员', tag: 'danger' }
}

const getRoleText = (role) => roleMap[role]?.text || '未知'
const getRoleTag = (role) => roleMap[role]?.tag || ''

const form = reactive({ id: null, username: '', password: '', realName: '', role: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

// 动态验证规则：添加时密码必填，编辑时密码可选
const getRules = () => {
  const baseRules = { ...rules }
  if (!isEdit.value) {
    baseRules.password = [{ required: true, message: '请输入密码', trigger: 'blur' }]
  }
  return baseRules
}

const fetchAdmins = async () => {
  loading.value = true
  try {
    const res = await getAdminList()
    if (res.code === 200) {
      admins.value = res.data
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: null, username: '', password: '', realName: '', role: '' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, { ...row, password: '' })
  dialogVisible.value = true
}

const handleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定${action}管理员"${row.username}"吗？`, '提示', { type: 'warning' })
    const res = await updateAdminStatus(row.id, newStatus)
    if (res.code === 200) {
      ElMessage.success(`${action}成功`)
      fetchAdmins()
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除管理员"${row.username}"吗？`, '提示', { type: 'warning' })
    const res = await deleteAdmin(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchAdmins()
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleResetPwd = async (row) => {
  try {
    await ElMessageBox.confirm(`确定重置"${row.username}"的密码为 123456 吗？`, '提示', { type: 'warning' })
    const res = await resetPassword(row.id, { newPassword: '123456' })
    if (res.code === 200) {
      ElMessage.success('密码已重置为 123456')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('重置失败')
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const data = { username: form.username, realName: form.realName, role: form.role }
    if (!isEdit.value) data.password = form.password

    let res
    if (isEdit.value) {
      res = await updateAdmin(form.id, data)
    } else {
      res = await addAdmin(data)
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
      dialogVisible.value = false
      fetchAdmins()
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(fetchAdmins)
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
</style>
