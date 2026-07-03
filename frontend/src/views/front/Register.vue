<template>
  <div class="login-page">
    <div class="login-header">
      <div class="container">
        <div class="header-inner">
          <div class="logo" @click="$router.push('/')">
            <span class="logo-text">校园淘宝</span>
          </div>
          <h1>用户注册</h1>
        </div>
      </div>
    </div>

    <div class="login-content">
      <div class="login-box">
        <div class="login-form">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large">
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入用户名（3-20个字符）"
                prefix-icon="User"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码（6位以上）"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input
                v-model="form.confirmPassword"
                type="password"
                placeholder="请确认密码"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-form-item prop="realName">
              <el-input
                v-model="form.realName"
                placeholder="请输入真实姓名"
                prefix-icon="UserFilled"
              />
            </el-form-item>

            <el-form-item prop="phone">
              <el-input
                v-model="form.phone"
                placeholder="请输入手机号"
                prefix-icon="Phone"
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                class="login-btn"
                :loading="loading"
                @click="handleRegister"
              >
                注 册
              </el-button>
            </el-form-item>
          </el-form>

          <div class="login-footer">
            <div class="register-link">
              <span>已有账号？</span>
              <router-link to="/login">立即登录</router-link>
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
import { useRouter } from 'vue-router'
import { register } from '@/api/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3到20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await register({
      username: form.username,
      password: form.password,
      realName: form.realName,
      phone: form.phone
    })
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    ElMessage.error('注册失败')
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

.login-form {
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
