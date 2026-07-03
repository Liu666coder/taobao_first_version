<template>
  <div class="landing-page" @click="enterStore">
    <!-- 背景粒子效果 -->
    <div class="particles">
      <div v-for="i in 50" :key="i" class="particle" :style="getParticleStyle(i)"></div>
    </div>

    <!-- 浮动的装饰元素 -->
    <div class="floating-elements">
      <div class="float-item" v-for="i in 8" :key="i" :style="getFloatStyle(i)">
        {{ ['🛍️', '🎁', '🛒', '💳', '📱', '📚', '👟', '🎮'][i-1] }}
      </div>
    </div>

    <!-- 主内容 -->
    <div class="content" :class="{ 'content-visible': isVisible }">
      <!-- Logo动画 -->
      <div class="logo-wrapper">
        <div class="logo-circle">
          <span class="logo-icon">🛒</span>
        </div>
        <div class="logo-ring"></div>
        <div class="logo-ring ring-2"></div>
      </div>

      <!-- 标题 -->
      <h1 class="title">
        <span class="title-line line-1">校园</span>
        <span class="title-line line-2">淘宝商城</span>
      </h1>

      <!-- 副标题 -->
      <p class="subtitle">
        <span class="typing-text">{{ displayText }}</span>
        <span class="cursor">|</span>
      </p>

      <!-- 装饰线 -->
      <div class="decorative-line">
        <span></span>
        <span class="dot"></span>
        <span></span>
      </div>

      <!-- 特色标签 -->
      <div class="features">
        <div class="feature-tag" v-for="(feature, index) in features" :key="index"
             :style="{ animationDelay: `${index * 0.2 + 1.5}s` }">
          {{ feature }}
        </div>
      </div>

      <!-- 进入按钮 -->
      <div class="enter-section" :class="{ 'show': showButton }">
        <button class="enter-btn" @click.stop="enterStore">
          <span class="btn-text">进入商城</span>
          <span class="btn-icon">→</span>
          <div class="btn-shine"></div>
        </button>
        <p class="enter-hint">点击任意处进入</p>
      </div>
    </div>

    <!-- 底部版权 -->
    <div class="landing-footer">
      <p>© 2026 校园淘宝商城 · 专为大学生打造</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const isVisible = ref(false)
const showButton = ref(false)
const displayText = ref('')
const isAnimating = ref(true)

const features = ['🎁 新人优惠', '🚚 校园配送', '✅ 品质保证', '💬 在线客服']

const fullText = '让校园生活更精彩 | 你身边的购物天堂'

// 打字机效果
let typingTimer = null
const startTyping = () => {
  let index = 0
  typingTimer = setInterval(() => {
    if (index <= fullText.length) {
      displayText.value = fullText.substring(0, index)
      index++
    } else {
      clearInterval(typingTimer)
    }
  }, 80)
}

// 粒子样式
const getParticleStyle = (i) => {
  const size = Math.random() * 6 + 2
  return {
    width: `${size}px`,
    height: `${size}px`,
    left: `${Math.random() * 100}%`,
    top: `${Math.random() * 100}%`,
    animationDelay: `${Math.random() * 5}s`,
    animationDuration: `${Math.random() * 10 + 10}s`
  }
}

// 浮动元素样式
const getFloatStyle = (i) => {
  return {
    left: `${10 + (i - 1) * 12}%`,
    animationDelay: `${i * 0.5}s`,
    animationDuration: `${6 + Math.random() * 4}s`
  }
}

// 进入商城
const enterStore = () => {
  router.push('/store')
}

onMounted(() => {
  setTimeout(() => {
    isVisible.value = true
  }, 100)

  setTimeout(() => {
    startTyping()
  }, 800)

  setTimeout(() => {
    showButton.value = true
  }, 2000)
})

onUnmounted(() => {
  if (typingTimer) clearInterval(typingTimer)
})
</script>

<style lang="scss" scoped>
.landing-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  cursor: pointer;
}

// 粒子效果
.particles {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.particle {
  position: absolute;
  background: rgba(255, 68, 0, 0.6);
  border-radius: 50%;
  animation: particleFloat linear infinite;
}

@keyframes particleFloat {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
    opacity: 0.6;
  }
  50% {
    transform: translateY(-100px) rotate(180deg);
    opacity: 0.2;
  }
}

// 浮动装饰
.floating-elements {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.float-item {
  position: absolute;
  font-size: 40px;
  top: -60px;
  animation: floatDown linear infinite;
  opacity: 0.4;
}

@keyframes floatDown {
  0% {
    transform: translateY(-60px) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 0.4;
  }
  90% {
    opacity: 0.4;
  }
  100% {
    transform: translateY(100vh) rotate(360deg);
    opacity: 0;
  }
}

// 主内容
.content {
  text-align: center;
  z-index: 10;
  opacity: 0;
  transform: translateY(30px);
  transition: all 1s ease;
}

.content-visible {
  opacity: 1;
  transform: translateY(0);
}

// Logo
.logo-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
  margin: 0 auto 40px;
}

.logo-circle {
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, #FF4400, #FF6633);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 2;
  box-shadow: 0 10px 40px rgba(255, 68, 0, 0.4);
  animation: logoPulse 2s ease-in-out infinite;

  .logo-icon {
    font-size: 50px;
  }
}

.logo-ring {
  position: absolute;
  inset: -10px;
  border: 2px solid rgba(255, 68, 0, 0.3);
  border-radius: 50%;
  animation: ringPulse 2s ease-in-out infinite;
}

.ring-2 {
  inset: -25px;
  animation-delay: 0.5s;
  border-color: rgba(255, 68, 0, 0.2);
}

@keyframes logoPulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

@keyframes ringPulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.2;
  }
}

// 标题
.title {
  margin-bottom: 20px;
}

.title-line {
  display: block;
  font-weight: 900;
  letter-spacing: 8px;

  &.line-1 {
    font-size: 36px;
    color: rgba(255, 255, 255, 0.9);
    animation: slideInLeft 0.8s ease forwards;
    opacity: 0;
    transform: translateX(-30px);
  }

  &.line-2 {
    font-size: 56px;
    background: linear-gradient(135deg, #FF4400, #FFD700);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    animation: slideInRight 0.8s ease 0.3s forwards;
    opacity: 0;
    transform: translateX(30px);
  }
}

@keyframes slideInLeft {
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideInRight {
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

// 副标题
.subtitle {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 30px;
  min-height: 30px;
}

.cursor {
  animation: blink 1s infinite;
  margin-left: 2px;
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}

// 装饰线
.decorative-line {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 30px;

  span {
    width: 60px;
    height: 2px;
    background: linear-gradient(90deg, transparent, #FF4400, transparent);

    &.dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #FF4400;
    }
  }
}

// 特色标签
.features {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: 50px;
}

.feature-tag {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  padding: 10px 20px;
  border-radius: 30px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  opacity: 0;
  animation: fadeInUp 0.5s ease forwards;
}

@keyframes fadeInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
  from {
    opacity: 0;
    transform: translateY(20px);
  }
}

// 进入按钮
.enter-section {
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.8s ease;

  &.show {
    opacity: 1;
    transform: translateY(0);
  }
}

.enter-btn {
  position: relative;
  background: linear-gradient(135deg, #FF4400, #FF6633);
  color: white;
  border: none;
  padding: 16px 50px;
  font-size: 18px;
  font-weight: 600;
  border-radius: 50px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 8px 30px rgba(255, 68, 0, 0.4);

  &:hover {
    transform: translateY(-3px) scale(1.02);
    box-shadow: 0 15px 40px rgba(255, 68, 0, 0.5);

    .btn-icon {
      transform: translateX(5px);
    }

    .btn-shine {
      transform: translateX(100%);
    }
  }

  .btn-icon {
    transition: transform 0.3s ease;
  }

  .btn-shine {
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
    transition: transform 0.5s ease;
  }
}

.enter-hint {
  margin-top: 20px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 14px;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.5;
  }
  50% {
    opacity: 1;
  }
}

// 底部
.landing-footer {
  position: absolute;
  bottom: 30px;
  left: 0;
  right: 0;
  text-align: center;

  p {
    color: rgba(255, 255, 255, 0.4);
    font-size: 12px;
  }
}

// 响应式
@media (max-width: 768px) {
  .title-line {
    &.line-1 {
      font-size: 28px;
    }
    &.line-2 {
      font-size: 40px;
    }
  }

  .features {
    flex-direction: column;
    align-items: center;
  }

  .float-item {
    font-size: 30px;
  }
}
</style>
