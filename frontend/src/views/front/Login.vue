<template>
  <div class="login-page">
    <div class="login-header">
      <div class="container">
        <div class="header-inner">
          <div class="logo" @click="$router.push('/store')">
            <span class="logo-text">校园淘宝</span>
          </div>
          <h1>用户登录</h1>
        </div>
      </div>
    </div>

    <div class="login-content">
      <div class="login-box">
        <div class="login-form">
          <!-- 登录方式切换 -->
          <div class="login-tabs">
            <div
              class="tab-item"
              :class="{ active: loginType === 'username' }"
              @click="switchLoginType('username')"
            >
              账号登录
            </div>
            <div
              class="tab-item"
              :class="{ active: loginType === 'phone' }"
              @click="switchLoginType('phone')"
            >
              手机号登录
            </div>
          </div>

          <!-- 账号登录表单 -->
          <el-form
            v-if="loginType === 'username'"
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="0"
            size="large"
          >
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入账号"
                prefix-icon="User"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>

            <el-form-item>
              <div class="form-options">
                <el-checkbox v-model="rememberMe">记住我</el-checkbox>
                <el-link type="primary" :underline="false">忘记密码</el-link>
              </div>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                class="login-btn"
                :loading="loading"
                @click="handleLogin"
              >
                登 录
              </el-button>
            </el-form-item>
          </el-form>

          <!-- 手机号登录表单 -->
          <el-form
            v-else
            ref="phoneFormRef"
            :model="phoneForm"
            :rules="phoneRules"
            label-width="0"
            size="large"
          >
            <el-form-item prop="phone">
              <el-input
                v-model="phoneForm.phone"
                placeholder="请输入手机号"
                prefix-icon="Iphone"
                maxlength="11"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="phoneForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                show-password
                @keyup.enter="handlePhoneLogin"
              />
            </el-form-item>

            <el-form-item>
              <div class="form-options">
                <el-checkbox v-model="rememberMe">记住我</el-checkbox>
                <el-link type="primary" :underline="false">忘记密码</el-link>
              </div>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                class="login-btn"
                :loading="loading"
                @click="handlePhoneLogin"
              >
                登 录
              </el-button>
            </el-form-item>
          </el-form>

          <div class="login-footer">
            <div class="register-link">
              <span>还没有账号？</span>
              <router-link to="/register">免费注册</router-link>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="login-footer-text">
      <p>© 2026 校园淘宝商城 - 专为大学生打造的购物平台</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { login } from '@/api/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref(null)
const phoneFormRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)
const loginType = ref('username') // username 或 phone

// 账号登录表单
const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 手机号登录表单
const phoneForm = reactive({
  phone: '',
  password: ''
})

const phoneRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 切换登录方式
const switchLoginType = (type) => {
  loginType.value = type
}

// 账号登录
const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await login({
      username: form.username,
      password: form.password,
      loginType: 'username'
    })

    if (res.code === 200) {
      localStorage.removeItem('token')
      localStorage.removeItem('adminToken')
      localStorage.setItem('token', res.data)
      ElMessage.success('登录成功')
      // 跳转到首页，加时间戳防止浏览器缓存
      window.location.href = '/store?_t=' + Date.now()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    ElMessage.error('登录失败')
  } finally {
    loading.value = false
  }
}

// 手机号登录
const handlePhoneLogin = async () => {
  const valid = await phoneFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await login({
      phone: phoneForm.phone,
      password: phoneForm.password,
      loginType: 'phone'
    })

    if (res.code === 200) {
      localStorage.removeItem('token')
      localStorage.removeItem('adminToken')
      localStorage.setItem('token', res.data)
      ElMessage.success('登录成功')
      window.location.href = '/store?_t=' + Date.now()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    ElMessage.error('登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.login-header {
  background: #fff;
  border-bottom: 2px solid #FF4400;
  padding: 20px 0;
}

.header-inner {
  display: flex;
  align-items: center;
}

.logo {
  cursor: pointer;
  margin-right: 40px;

  .logo-text {
    font-size: 36px;
    font-weight: 900;
    color: #FF4400;
    font-family: 'Arial Black', 'Helvetica Neue', Arial, sans-serif;
    letter-spacing: 2px;
    text-shadow: 1px 1px 2px rgba(255, 68, 0, 0.3);
  }
}

.login-header h1 {
  font-size: 24px;
  color: #333;
  font-weight: normal;
}

.login-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: url('https://img.alicdn.com/tfs/TB1Kt3Pb8HH8KJjy0FpXXcApXXa.jpg') center/cover no-repeat;
  padding: 60px 0;
}

.login-box {
  width: 400px;
  background: rgba(255,255,255,.95);
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0,0,0,.15);
  padding: 40px;
}

.login-tabs {
  display: flex;
  margin-bottom: 24px;
  border-bottom: 1px solid #eee;

  .tab-item {
    flex: 1;
    text-align: center;
    padding: 12px 0;
    cursor: pointer;
    font-size: 16px;
    color: #666;
    transition: all 0.3s;

    &.active {
      color: #FF4400;
      border-bottom: 2px solid #FF4400;
    }

    &:hover {
      color: #FF4400;
    }
  }
}

.login-form {
  .form-options {
    display: flex;
    justify-content: space-between;
    width: 100%;
  }

  .login-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    background: linear-gradient(135deg, #FF4400, #FF6633);
    border: none;

    &:hover {
      background: linear-gradient(135deg, #FF6633, #FF8866);
    }
  }
}

.login-footer {
  margin-top: 24px;
  text-align: center;
}

.register-link {
  font-size: 14px;
  color: #666;

  a {
    color: #FF4400;
    margin-left: 4px;
  }
}

.login-footer-text {
  padding: 20px 0;
  text-align: center;
  background: #f5f5f5;

  p {
    color: #999;
    font-size: 12px;
  }
}
</style>
