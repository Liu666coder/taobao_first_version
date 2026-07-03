<template>
  <div class="profile-page container">
    <div class="profile-layout">
      <!-- 左侧菜单 -->
      <div class="profile-sidebar">
        <div class="user-card">
          <div class="avatar-wrapper" @click="triggerAvatarUpload">
            <div class="avatar">
              <img v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" alt="avatar" class="avatar-img" />
              <el-icon v-else size="40"><User /></el-icon>
            </div>
            <div class="avatar-overlay">
              <el-icon size="20"><Camera /></el-icon>
              <span>更换头像</span>
            </div>
          </div>
          <input type="file" ref="avatarInput" accept="image/*" style="display: none" @change="handleAvatarUpload" />
          <div class="user-info">
            <h3>{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</h3>
            <p>{{ userStore.userInfo?.phone }}</p>
          </div>
        </div>

        <el-menu :default-active="activeMenu" @select="handleMenuSelect">
          <el-menu-item index="info">
            <el-icon><User /></el-icon>
            <span>基本资料</span>
          </el-menu-item>
          <el-menu-item index="password">
            <el-icon><Lock /></el-icon>
            <span>修改密码</span>
          </el-menu-item>
          <el-menu-item index="orders">
            <el-icon><Document /></el-icon>
            <span>我的订单</span>
          </el-menu-item>
          <el-menu-item index="cart">
            <el-icon><ShoppingCart /></el-icon>
            <span>我的购物车</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 右侧内容 -->
      <div class="profile-content">
        <!-- 基本资料 -->
        <div v-if="activeMenu === 'info'" class="content-card">
          <h3>基本资料</h3>
          <el-form ref="infoFormRef" :model="infoForm" :rules="infoRules" label-width="100px" class="profile-form">
            <el-form-item label="用户名">
              <el-input :value="userStore.userInfo?.username" disabled />
            </el-form-item>
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="infoForm.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="infoForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="infoForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="save-btn" :loading="infoLoading" @click="handleUpdateInfo">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 修改密码 -->
        <div v-if="activeMenu === 'password'" class="content-card">
          <h3>修改密码</h3>
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px" class="profile-form" style="max-width: 400px;">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请确认新密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="save-btn" :loading="pwdLoading" @click="handleUpdatePassword">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 快捷入口 -->
        <div v-if="activeMenu === 'orders' || activeMenu === 'cart'" class="content-card">
          <el-result icon="info" :title="activeMenu === 'orders' ? '查看我的订单' : '查看我的购物车'">
            <template #sub-title>
              <p>点击下方按钮查看{{ activeMenu === 'orders' ? '订单' : '购物车' }}详情</p>
            </template>
            <template #extra>
              <el-button type="primary" @click="$router.push(activeMenu === 'orders' ? '/orders' : '/cart')">
                {{ activeMenu === 'orders' ? '我的订单' : '我的购物车' }}
              </el-button>
            </template>
          </el-result>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { updateUserInfo, updatePassword, uploadAvatar } from '@/api/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const activeMenu = ref('info')
const infoFormRef = ref(null)
const pwdFormRef = ref(null)
const avatarInput = ref(null)
const infoLoading = ref(false)
const pwdLoading = ref(false)

const infoForm = reactive({
  realName: '',
  email: '',
  phone: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const infoRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleMenuSelect = (index) => {
  activeMenu.value = index
}

const triggerAvatarUpload = () => {
  avatarInput.value.click()
}

const handleAvatarUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }

  // 验证文件大小（5MB）
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }

  try {
    const res = await uploadAvatar(file)
    if (res.code === 200) {
      ElMessage.success('头像上传成功')
      userStore.fetchUserInfo()
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (e) {
    ElMessage.error('上传失败')
  }

  // 清空input
  event.target.value = ''
}

const handleUpdateInfo = async () => {
  const valid = await infoFormRef.value.validate().catch(() => false)
  if (!valid) return

  infoLoading.value = true
  try {
    const res = await updateUserInfo(infoForm)
    if (res.code === 200) {
      ElMessage.success('修改成功')
      userStore.fetchUserInfo()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    ElMessage.error('修改失败')
  } finally {
    infoLoading.value = false
  }
}

const handleUpdatePassword = async () => {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return

  pwdLoading.value = true
  try {
    const res = await updatePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    if (res.code === 200) {
      ElMessage.success('密码修改成功')
      pwdForm.oldPassword = ''
      pwdForm.newPassword = ''
      pwdForm.confirmPassword = ''
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    ElMessage.error('修改失败')
  } finally {
    pwdLoading.value = false
  }
}

onMounted(async () => {
  await userStore.fetchUserInfo()
  if (userStore.userInfo) {
    infoForm.realName = userStore.userInfo.realName || ''
    infoForm.email = userStore.userInfo.email || ''
    infoForm.phone = userStore.userInfo.phone || ''
  }
})
</script>

<style lang="scss" scoped>
.profile-page {
  padding: 20px 15px;
}

.profile-layout {
  display: flex;
  gap: 20px;
}

.profile-sidebar {
  width: 220px;
  flex-shrink: 0;
}

.user-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  text-align: center;
  margin-bottom: 16px;
}

.avatar-wrapper {
  position: relative;
  width: 80px;
  height: 80px;
  margin: 0 auto 12px;
  cursor: pointer;

  &:hover .avatar-overlay {
    opacity: 1;
  }

  .avatar {
    width: 80px;
    height: 80px;
    background: linear-gradient(135deg, #FF4400, #FF6633);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    overflow: hidden;

    .avatar-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .avatar-overlay {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.5);
    border-radius: 50%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: white;
    opacity: 0;
    transition: opacity 0.2s;

    span {
      font-size: 10px;
      margin-top: 4px;
    }
  }
}

.user-info {
  h3 {
    font-size: 16px;
    color: #333;
    margin-bottom: 4px;
  }

  p {
    font-size: 13px;
    color: #999;
  }
}

.el-menu {
  background: #fff;
  border-radius: 8px;
  border: none;
}

.profile-content {
  flex: 1;
}

.content-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;

  h3 {
    font-size: 18px;
    color: #333;
    margin-bottom: 24px;
    padding-bottom: 12px;
    border-bottom: 1px solid #f5f5f5;
  }
}

.profile-form {
  max-width: 500px;
  margin-top: 20px;
}

.save-btn {
  min-width: 120px;
  background: linear-gradient(135deg, #FF4400, #FF6633);
  border: none;

  &:hover {
    background: linear-gradient(135deg, #FF6633, #FF8866);
  }
}
</style>
