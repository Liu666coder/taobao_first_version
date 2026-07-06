<template>
  <div class="admin-profile">
    <div class="profile-card">
      <!-- 左侧头像区域 -->
      <div class="profile-left">
        <div class="avatar-section">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :on-success="handleAvatarSuccess"
            :on-error="handleAvatarError"
            accept="image/*"
          >
            <div class="avatar-wrapper">
              <img v-if="adminInfo.avatar" :src="adminInfo.avatar" class="avatar-img" />
              <div v-else class="avatar-placeholder">
                <el-icon size="40"><User /></el-icon>
              </div>
              <div class="avatar-overlay">
                <el-icon size="20"><Camera /></el-icon>
                <span>更换头像</span>
              </div>
            </div>
          </el-upload>
          <div class="admin-name">{{ adminInfo.realName || adminInfo.username }}</div>
          <el-tag :type="roleTagType" size="small">{{ roleText }}</el-tag>
        </div>

        <!-- 菜单 -->
        <div class="menu-section">
          <div
            class="menu-item"
            :class="{ active: activeMenu === 'profile' }"
            @click="activeMenu = 'profile'"
          >
            <el-icon><User /></el-icon>
            <span>基本资料</span>
          </div>
          <div
            class="menu-item"
            :class="{ active: activeMenu === 'password' }"
            @click="activeMenu = 'password'"
          >
            <el-icon><Lock /></el-icon>
            <span>修改密码</span>
          </div>
        </div>
      </div>

      <!-- 右侧内容区 -->
      <div class="profile-right">
        <!-- 基本资料 -->
        <div v-if="activeMenu === 'profile'" class="form-section">
          <h3>基本资料</h3>
          <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="80px" style="max-width: 500px;">
            <el-form-item label="用户名">
              <el-input :value="adminInfo.username" disabled />
            </el-form-item>
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="profileLoading" @click="handleSaveProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 修改密码 -->
        <div v-if="activeMenu === 'password'" class="form-section">
          <h3>修改密码</h3>
          <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px" style="max-width: 500px;">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="passwordLoading" @click="handleSavePassword">保存修改</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, inject, onMounted } from 'vue'
import { getAdminInfo } from '@/api/admin'
import { ElMessage } from 'element-plus'

// 注入Layout提供的刷新管理员信息方法
const refreshAdminInfo = inject('refreshAdminInfo')

const uploadUrl = '/api/admin/avatar'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('adminToken') || ''}`
}))

const adminInfo = ref({})
const activeMenu = ref('profile')
const profileLoading = ref(false)
const passwordLoading = ref(false)
const profileFormRef = ref(null)
const passwordFormRef = ref(null)

const roleMap = {
  'PRODUCT_MANAGER': { text: '商品专员', tag: '' },
  'MARKETING_MANAGER': { text: '营销经理', tag: 'warning' },
  'SYSTEM_ADMIN': { text: '系统管理员', tag: 'danger' }
}

const roleText = computed(() => roleMap[adminInfo.value?.role]?.text || '管理员')
const roleTagType = computed(() => roleMap[adminInfo.value?.role]?.tag || '')

const profileForm = reactive({
  realName: '',
  email: '',
  phone: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const profileRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const fetchAdminInfo = async () => {
  try {
    const res = await getAdminInfo()
    if (res.code === 200) {
      adminInfo.value = res.data
      profileForm.realName = res.data.realName || ''
      profileForm.email = res.data.email || ''
      profileForm.phone = res.data.phone || ''
    }
  } catch (e) {
    console.error(e)
  }
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

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

const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    adminInfo.value.avatar = response.data
    ElMessage.success('头像更新成功')
    // 刷新Layout中的管理员信息
    if (refreshAdminInfo) refreshAdminInfo()
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleAvatarError = () => {
  ElMessage.error('头像上传失败')
}

const handleSaveProfile = async () => {
  const valid = await profileFormRef.value.validate().catch(() => false)
  if (!valid) return

  profileLoading.value = true
  try {
    const request = (await import('@/api/request')).default
    const res = await request.put('/admin/profile', {
      realName: profileForm.realName,
      email: profileForm.email,
      phone: profileForm.phone
    })
    if (res.code === 200) {
      ElMessage.success('保存成功')
      fetchAdminInfo()
      // 刷新Layout中的管理员信息
      if (refreshAdminInfo) refreshAdminInfo()
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    profileLoading.value = false
  }
}

const handleSavePassword = async () => {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  passwordLoading.value = true
  try {
    const request = (await import('@/api/request')).default
    const res = await request.put('/admin/password', {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    if (res.code === 200) {
      ElMessage.success('密码修改成功')
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    }
  } catch (e) {
    ElMessage.error('修改失败')
  } finally {
    passwordLoading.value = false
  }
}

onMounted(fetchAdminInfo)
</script>

<style lang="scss" scoped>
.admin-profile {
  max-width: 900px;
}

.profile-card {
  display: flex;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,.06);
}

.profile-left {
  width: 280px;
  background: #fafafa;
  padding: 30px 20px;
  border-right: 1px solid #eee;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-bottom: 24px;
  border-bottom: 1px solid #eee;
  margin-bottom: 16px;
}

.avatar-uploader {
  :deep(.el-upload) {
    cursor: pointer;
  }
}

.avatar-wrapper {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;

  &:hover .avatar-overlay {
    opacity: 1;
  }
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #FF4400, #FF6633);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity .3s;
  gap: 4px;

  span {
    font-size: 12px;
  }
}

.admin-name {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin: 16px 0 8px;
}

.menu-section {
  .menu-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 16px;
    border-radius: 8px;
    cursor: pointer;
    transition: all .3s;
    color: #666;

    &:hover {
      background: #fff;
      color: #FF4400;
    }

    &.active {
      background: #fff1f0;
      color: #FF4400;
      font-weight: 500;
    }
  }
}

.profile-right {
  flex: 1;
  padding: 30px 40px;
}

.form-section {
  h3 {
    font-size: 20px;
    color: #333;
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid #eee;
  }
}
</style>
