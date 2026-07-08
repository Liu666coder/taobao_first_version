<template>
  <!--
    =====================================================================
    LandingPage - 校园淘宝商城开场欢迎页
    =====================================================================

    【整体架构 - 7层视觉叠加】
    本页面采用多层视觉叠加技术，从底层到顶层依次为：
    1. 背景层 (.landing) - 多层径向渐变，营造深邃星空感
    2. 星云层 (.aurora + .nebula) - CSS模糊光团，模拟星云流动
    3. 背景粒子层 (bgCanvas) - Canvas绘制的星尘粒子，带景深视差
    4. 噪点+网格层 (.noise + .grid-overlay) - SVG噪点纹理+极淡网格，增加质感
    5. 汇聚粒子层 (assembleCanvas) - 从四周飞入并拼成购物车轮廓的粒子
    6. 主视觉层 (.hero) - SVG玻璃购物车 + 能量环轨道
    7. 内容层 (.content) - 文案和按钮，依次浮现

    【核心算法】
    - 像素采样：离屏Canvas绘制购物车→扫描像素→获取目标点坐标
    - 粒子运动：easeOutCubic缓动 + 错峰延迟 + 成型后微颤
    - 景深模拟：连续景深值控制粒子大小/速度/亮度/颜色

    【动画时间轴】
    0-2s: 粒子从四周汇聚成购物车轮廓
    2-3s: 购物车被橙光点亮，SVG淡入
    3-4s: 能量环+轨道粒子出现
    4s+:  文案依次浮现
    =====================================================================
  -->
  <div class="landing" :class="{ 'is-leaving': isLeaving }">
    <!--
      汇聚粒子层（Canvas）
      【作用】绘制从屏幕四周飞入的粒子，最终拼成购物车轮廓
      【算法】像素采样 → 获取目标点 → easeOutCubic缓动 → 错峰汇聚
    -->
    <canvas ref="assembleCanvas" class="assemble-canvas"></canvas>

    <!--
      背景星尘粒子层（Canvas）
      【作用】绘制带景深视差的星尘粒子，营造深邃星空感
      【算法】连续景深值控制粒子的：大小(0.3→2.4px)、速度、亮度、颜色(冷蓝→暖金)
    -->
    <canvas ref="bgCanvas" class="bg-canvas"></canvas>

    <!--
      柔光粒子云（CSS）
      【作用】大面积低透明度模糊光团，模拟星云流动效果
      【技术】border-radius:50% + filter:blur(70px) + mix-blend-mode:screen
    -->
    <div class="nebula">
      <span class="cloud cloud-1"></span>
      <span class="cloud cloud-2"></span>
      <span class="cloud cloud-3"></span>
      <span class="cloud cloud-4"></span>
    </div>

    <!--
      星云光晕（CSS）
      【作用】更大范围的模糊光团，与柔光云叠加增强星云效果
      【技术】filter:blur(100px) + 慢速漂移动画(15-22s周期)
    -->
    <div class="aurora">
      <span class="aurora-blob blob-1"></span>
      <span class="aurora-blob blob-2"></span>
      <span class="aurora-blob blob-3"></span>
    </div>

    <!--
      星点扩散层（CSS）
      【作用】随机分布的微小星点，由中心淡出并放大，营造星尘扩散感
      【技术】2px白色圆点 + box-shadow辉光 + 缩放+透明度脉冲动画
      【优化】移动端(<768px)减少到22个，桌面端42个
    -->
    <div class="stars">
      <span v-for="s in starList" :key="s.id" class="star"
            :style="{ left: s.left, top: s.top, animationDelay: s.delay, animationDuration: s.dur }"></span>
    </div>

    <!--
      细腻噪点纹理（SVG）
      【作用】极低透明度叠加，消除渐变色带、增加画面质感
      【技术】内联SVG的feTurbulence分形噪声 + mix-blend-mode:overlay
    -->
    <div class="noise"></div>

    <!--
      极淡数字网格（CSS）
      【作用】营造科技感的网格背景，径向渐变遮罩使其中心可见、边缘消失
      【技术】background-image线性渐变 + mask-image径向渐变遮罩
    -->
    <div class="grid-overlay"></div>

    <!--
      购物车主视觉容器
      【作用】居中放置SVG购物车和能量环，是页面的视觉焦点
      【定位】top:32% 垂直略偏上，符合视觉重心偏上的设计原则
    -->
    <div class="hero" :class="{ 'hero-lit': cartLit }">
      <!--
        能量环 / 轨道
        【作用】围绕购物车旋转的光环和轨道粒子，增强科技感和能量感
        【组成】玻璃能量核心 + 3层旋转环 + 3个轨道粒子
        【动画】各环不同速度/方向旋转，轨道粒子绕车公转
      -->
      <div class="rings" :class="{ 'rings-show': ringsShow }">
        <span class="energy-core"></span>
        <span class="ring ring-glass"></span>
        <span class="ring ring-1"></span>
        <span class="ring ring-2"></span>
        <span class="ring ring-3"></span>
        <span class="orbit-dot dot-a"></span>
        <span class="orbit-dot dot-b"></span>
        <span class="orbit-dot dot-c"></span>
      </div>

      <!--
        发光玻璃购物车 SVG
        【作用】购物车的主体图形，点亮阶段淡入，与汇聚粒子轮廓重合
        【技术】SVG渐变(linearGradient/radialGradient) + 模糊滤镜(feGaussianBlur)
        【结构】发光核心 + 提手车斗外框 + 竖/横格栅 + 轮子 + 高光反射
      -->
      <svg class="cart-svg" viewBox="0 0 240 200" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <!-- 轮廓渐变：左上浅金 → 右下深橙 -->
          <linearGradient id="strokeGrad" x1="0" y1="0" x2="240" y2="200" gradientUnits="userSpaceOnUse">
            <stop offset="0" stop-color="#FFE3B8" />
            <stop offset="0.45" stop-color="#FF9A3D" />
            <stop offset="1" stop-color="#FF4400" />
          </linearGradient>
          <!-- 玻璃填充渐变：顶部半透明橙 → 底部近乎透明 -->
          <linearGradient id="glassGrad" x1="0" y1="40" x2="0" y2="150" gradientUnits="userSpaceOnUse">
            <stop offset="0" stop-color="rgba(255,180,110,0.30)" />
            <stop offset="1" stop-color="rgba(255,90,20,0.05)" />
          </linearGradient>
          <!-- 发光核心渐变：中心亮橙 → 边缘透明 -->
          <radialGradient id="coreGlow" cx="0.5" cy="0.45" r="0.6">
            <stop offset="0" stop-color="rgba(255,140,40,0.75)" />
            <stop offset="1" stop-color="rgba(255,140,40,0)" />
          </radialGradient>
          <!-- 柔和发光滤镜：高斯模糊+原图叠加 -->
          <filter id="softGlow" x="-40%" y="-40%" width="180%" height="180%">
            <feGaussianBlur stdDeviation="3.2" result="b" />
            <feMerge>
              <feMergeNode in="b" />
              <feMergeNode in="SourceGraphic" />
            </feMerge>
          </filter>
        </defs>

        <!-- 发光核心：径向渐变椭圆，营造内部发光效果 -->
        <ellipse cx="120" cy="95" rx="86" ry="70" fill="url(#coreGlow)" />

        <!-- 购物车主体：带发光滤镜的路径 -->
        <g filter="url(#softGlow)" stroke="url(#strokeGrad)" stroke-width="5"
           stroke-linecap="round" stroke-linejoin="round" fill="none">
          <!-- 提手 + 车斗外框：单条连续路径描绘购物车轮廓 -->
          <path d="M28 40 H52 L74 132 H176 L196 66 H86"
                fill="url(#glassGrad)" />
          <!-- 车斗竖向格栅（玻璃感）：4条竖线模拟玻璃质感 -->
          <path d="M96 78 L102 120" stroke-width="3.5" opacity="0.7" />
          <path d="M118 78 L120 120" stroke-width="3.5" opacity="0.7" />
          <path d="M140 78 L138 120" stroke-width="3.5" opacity="0.7" />
          <path d="M162 78 L156 120" stroke-width="3.5" opacity="0.7" />
          <!-- 横向格栅：2条横线增强结构感 -->
          <path d="M84 92 H190" stroke-width="3" opacity="0.5" />
          <path d="M88 108 H182" stroke-width="3" opacity="0.5" />
          <!-- 轮子：2个半透明橙色圆 -->
          <circle cx="92" cy="156" r="11" fill="rgba(255,120,30,0.18)" />
          <circle cx="164" cy="156" r="11" fill="rgba(255,120,30,0.18)" />
        </g>

        <!-- 高光反射：白色斜角矩形，模拟玻璃反光 -->
        <path d="M92 66 L104 66 L96 92 L82 92 Z" fill="rgba(255,255,255,0.20)" />
      </svg>
    </div>

    <!--
      文字内容区
      【作用】品牌标语和进入按钮，购物车下方共享中轴
      【动画】content-show类触发依次浮现：eyebrow→title→tagline→desc→enter-block
      【时序】每项间隔0.15-0.2s，总时长约1s完成全部淡入
    -->
    <div class="content" :class="{ 'content-show': contentShow }">
      <p class="eyebrow">CAMPUS SELECTED MALL</p>
      <h1 class="title">校园淘宝商城</h1>
      <p class="tagline">一辆购物车，装下整个校园的期待</p>
      <p class="desc">
        从清晨补给到深夜慰藉，从学习日常到生活灵感，<br />
        精选好物正沿着星光轨迹，抵达你的大学时光。
      </p>

      <div class="enter-block">
        <!--
          进入商城按钮
          【交互】hover时：上移+放大+光晕增强+箭头右移+光泽扫过
          【呼吸动画】box-shadow周期性脉冲，引导用户点击
        -->
        <button class="enter-btn" @click="enterStore">
          <span class="btn-label">进入商城</span>
          <span class="btn-arrow">→</span>
          <span class="btn-shine"></span>
        </button>
        <p class="enter-hint">轻触，让好物向你靠近</p>
      </div>
    </div>

    <footer class="footer">© 2026 校园淘宝商城 · 为大学生活精选每一份刚好</footer>

    <!--
      点击光波效果
      【作用】点击"进入商城"后，从购物车位置向外扩散橙色光波
      【技术】radial-gradient圆形 + burstOut动画(0.95s) + 240vmax最终尺寸
    -->
    <div v-if="isLeaving" class="burst"></div>
  </div>
</template>

<script setup>
/**
 * LandingPage - 校园淘宝商城开场欢迎页
 *
 * 【核心功能】
 * 使用 Canvas 粒子动画拼成购物车轮廓，配合星空背景、能量环等视觉效果，
 * 点击"进入商城"按钮后播放退场动画并跳转到首页。
 *
 * 【技术栈】
 * - Vue 3 Composition API (ref, onMounted, onBeforeUnmount)
 * - Canvas 2D API (粒子系统、像素采样、requestAnimationFrame)
 * - SVG (渐变、滤镜、路径绘制)
 * - CSS动画 (@keyframes, transitions, transforms)
 *
 * 【模块划分】
 * 1. 背景星尘粒子系统 - 带景深视差的星空效果
 * 2. 汇聚粒子系统 - 从四周飞入拼成购物车轮廓
 * 3. 时间轴编排 - 控制各阶段动画的启动时机
 * 4. 交互处理 - 进入商城按钮点击事件
 */
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// ============ DOM引用 ============
const assembleCanvas = ref(null)  // 汇聚粒子Canvas
const bgCanvas = ref(null)        // 背景星尘Canvas

// ============ 分阶段状态标志 ============
// 这些ref控制动画时间轴的各个阶段，通过CSS类名切换触发动画
const cartLit = ref(false)      // 2s后：购物车被橙光点亮，SVG淡入
const ringsShow = ref(false)    // 3s后：能量环/轨道粒子出现
const contentShow = ref(false)  // 4s后：文案依次浮现
const isLeaving = ref(false)    // 点击按钮后：退场动画

// ============ 星点扩散配置 ============
// 随机生成的闪烁星点，移动端减少数量以优化性能
const starCount = typeof window !== 'undefined' && window.innerWidth < 768 ? 22 : 42
const starList = Array.from({ length: starCount }, (_, i) => ({
  id: i,
  left: `${Math.random() * 100}%`,    // 随机水平位置
  top: `${Math.random() * 100}%`,     // 随机垂直位置
  delay: `${(Math.random() * 6).toFixed(2)}s`,  // 随机延迟(0-6s)
  dur: `${(Math.random() * 4 + 3).toFixed(2)}s` // 随机周期(3-7s)
}))

/* ====================================================================
   模块1：背景星尘粒子系统
   ====================================================================
   【作用】绘制带景深视差的星尘粒子，营造深邃星空感

   【核心算法】
   1. 景深值(depth): 0=最远, 1=最近，使用 Math.pow(random, 1.4) 生成
      - 指数1.4 > 1 使分布偏向远景(小值)，远景粒子更多，增强纵深感
   2. 颜色插值: 远景冷蓝(130,175,235) → 近景暖金(255,200,140)
   3. 大小: 0.3px(远) → 2.4px(近)
   4. 速度: 0.05px/f(远，几乎静止) → 0.33px/f(近，漂移明显)
   5. 亮度: 0.12(远，暗) → 0.68(近，亮但封顶避免刺眼)

   【性能优化】
   - 移动端45个粒子，平板80个，桌面150个
   - 远景粒子无辉光(shadowBlur=0)，只有近处粒子带辉光
   - 按景深排序后绘制：远景先画、近景后画，保证正确的遮挡关系
   ==================================================================== */
let bgCtx = null, bgRaf = null, bgParticles = []
let bgW = 0, bgH = 0, dpr = 1

/**
 * 景深颜色插值函数
 * @param {number} depth - 景深值，0=最远(冷蓝), 1=最近(暖金)
 * @returns {string} RGB颜色字符串，如 "180, 190, 200"
 */
const depthColor = (depth) => {
  const far = [130, 175, 235]   // 远景：微光蓝
  const near = [255, 200, 140]  // 近景：暖金橙
  const r = Math.round(far[0] + (near[0] - far[0]) * depth)
  const g = Math.round(far[1] + (near[1] - far[1]) * depth)
  const b = Math.round(far[2] + (near[2] - far[2]) * depth)
  return `${r}, ${g}, ${b}`
}

/**
 * 构建背景粒子数组
 * 根据屏幕宽度决定粒子数量，每个粒子包含位置、景深、大小、颜色、速度等属性
 */
const buildBgParticles = () => {
  const w = window.innerWidth
  const count = w < 480 ? 45 : w < 768 ? 80 : 150  // 响应式粒子数量
  bgParticles = []
  for (let i = 0; i < count; i++) {
    // 使用 pow(random, 1.4) 生成偏向远景的景深分布
    const depth = Math.pow(Math.random(), 1.4)
    bgParticles.push({
      x: Math.random() * bgW,
      y: Math.random() * bgH,
      depth,
      r: 0.3 + depth * 2.1,           // 远处0.3px → 近处2.4px
      color: depthColor(depth),
      baseAlpha: 0.12 + depth * 0.56,  // 远处暗 → 近处亮(封顶0.68)
      vx: (Math.random() - 0.5) * (0.05 + depth * 0.28),  // 近处漂移快
      vy: (Math.random() - 0.5) * (0.05 + depth * 0.28),
      tw: Math.random() * Math.PI * 2,      // 闪烁相位
      tws: Math.random() * 0.02 + 0.004     // 闪烁速度
    })
  }
  bgParticles.sort((a, b) => a.depth - b.depth)  // 远景先画、近景后画
}

/**
 * 背景粒子动画主循环
 * 每帧清除画布 → 更新粒子位置 → 计算闪烁 → 绘制粒子
 */
const drawBg = () => {
  bgCtx.clearRect(0, 0, bgW, bgH)
  for (const p of bgParticles) {
    // 更新位置（环绕边界）
    p.x += p.vx; p.y += p.vy
    if (p.x < 0) p.x = bgW; if (p.x > bgW) p.x = 0
    if (p.y < 0) p.y = bgH; if (p.y > bgH) p.y = 0
    p.tw += p.tws

    // 闪烁计算：近处粒子闪烁幅度更明显，远处更稳定
    const flick = (Math.sin(p.tw) + 1) / 2
    const a = p.baseAlpha * (0.55 + flick * 0.45 * p.depth + 0.2 * (1 - p.depth) * flick)

    // 绘制粒子
    bgCtx.beginPath()
    bgCtx.arc(p.x, p.y, p.r, 0, Math.PI * 2)
    bgCtx.fillStyle = `rgba(${p.color}, ${a})`
    // 只有近处粒子带明显辉光，远处几乎无光晕（性能优化）
    bgCtx.shadowBlur = p.depth * p.r * 4
    bgCtx.shadowColor = `rgba(${p.color}, ${a * p.depth})`
    bgCtx.fill()
  }
  bgCtx.shadowBlur = 0
  bgRaf = requestAnimationFrame(drawBg)
}

/* ====================================================================
   模块2：汇聚粒子系统（拼成购物车轮廓）
   ====================================================================
   【核心算法】像素采样法

   步骤1: 离屏Canvas绘制购物车白色轮廓
   步骤2: 扫描Canvas像素，提取alpha > 128的坐标点
   步骤3: 将这些点作为粒子的目标位置
   步骤4: 粒子从屏幕四周随机位置出发，沿缓动曲线飞向目标

   【缓动函数】easeOutCubic: t => 1 - (1-t)³
   - 先快后慢，符合自然运动规律
   - 起始速度最快，接近目标时减速，避免突兀停止

   【错峰汇聚】每个粒子有随机延迟(0-0.35s)
   - 避免所有粒子同时到达，形成自然的"聚集"效果

   【成型后微颤】粒子到达目标后轻微颤动
   - 使用正弦函数模拟呼吸感，保持"能量感"
   ==================================================================== */
let asCtx = null, asRaf = null, targets = []
let asW = 0, asH = 0
let phaseStart = 0
const ASSEMBLE_MS = 2000  // 汇聚动画总时长：2秒

/**
 * 像素采样：从离屏Canvas获取购物车轮廓的目标点坐标
 * @returns {Object} { pts: 目标点数组, size: 画布尺寸 }
 */
const buildTargetsFromCart = () => {
  // 创建离屏Canvas（不显示，仅用于采样）
  const off = document.createElement('canvas')
  const size = 240
  off.width = size
  off.height = size
  const octx = off.getContext('2d')

  // 设置画笔样式
  octx.strokeStyle = '#fff'
  octx.lineWidth = 4
  octx.lineCap = 'round'
  octx.lineJoin = 'round'

  // 绘制购物车路径（与SVG车形一致，坐标偏移使居中）
  const ox = 8, oy = 30
  octx.beginPath()
  // 提手 + 车斗外框：单条连续路径
  octx.moveTo(28 + ox, 40 + oy)
  octx.lineTo(52 + ox, 40 + oy)
  octx.lineTo(74 + ox, 132 + oy)
  octx.lineTo(176 + ox, 132 + oy)
  octx.lineTo(196 + ox, 66 + oy)
  octx.lineTo(86 + ox, 66 + oy)
  octx.stroke()
  // 竖格栅：4条竖线
  const vlines = [[96, 78, 102, 120], [118, 78, 120, 120], [140, 78, 138, 120], [162, 78, 156, 120]]
  vlines.forEach(([x1, y1, x2, y2]) => {
    octx.beginPath(); octx.moveTo(x1 + ox, y1 + oy); octx.lineTo(x2 + ox, y2 + oy); octx.stroke()
  })
  // 横格栅：2条横线
  octx.beginPath(); octx.moveTo(84 + ox, 92 + oy); octx.lineTo(190 + ox, 92 + oy); octx.stroke()
  octx.beginPath(); octx.moveTo(88 + ox, 108 + oy); octx.lineTo(182 + ox, 108 + oy); octx.stroke()
  // 轮子：2个圆
  octx.beginPath(); octx.arc(92 + ox, 156 + oy, 11, 0, Math.PI * 2); octx.stroke()
  octx.beginPath(); octx.arc(164 + ox, 156 + oy, 11, 0, Math.PI * 2); octx.stroke()

  // 像素采样：扫描所有像素，提取alpha > 128的坐标
  const img = octx.getImageData(0, 0, size, size).data
  const pts = []
  const isMobile = window.innerWidth < 768
  const step = isMobile ? 4 : 3  // 采样步长：移动端稀疏些(4px)，桌面密集(3px)
  for (let y = 0; y < size; y += step) {
    for (let x = 0; x < size; x += step) {
      const alpha = img[(y * size + x) * 4 + 3]  // 获取alpha通道
      if (alpha > 128) pts.push({ x, y })        // alpha > 128 表示有像素
    }
  }
  return { pts, size }
}

/**
 * 计算购物车在屏幕上的呈现位置与缩放
 * 将采样点从240x240画布映射到实际屏幕尺寸
 */
const layoutTargets = () => {
  const { pts, size } = buildTargetsFromCart()
  const isMobile = window.innerWidth < 768
  const displaySize = isMobile ? 260 : 340   // 购物车在屏上的像素尺寸
  const scale = displaySize / size            // 缩放比例
  // 中心轴：水平居中，垂直略偏上(30%-32%)
  const cx = asW / 2 - displaySize / 2
  const cy = asH * (isMobile ? 0.30 : 0.32) - displaySize / 2

  targets = pts.map(p => {
    // 目标位置：缩放后的坐标
    const tx = cx + p.x * scale
    const ty = cy + p.y * scale
    // 起始位置：从屏幕四边随机飞入（上/右/下/左）
    const edge = Math.floor(Math.random() * 4)
    let sx, sy
    if (edge === 0) { sx = Math.random() * asW; sy = -20 }      // 上边
    else if (edge === 1) { sx = asW + 20; sy = Math.random() * asH }  // 右边
    else if (edge === 2) { sx = Math.random() * asW; sy = asH + 20 }  // 下边
    else { sx = -20; sy = Math.random() * asH }                  // 左边
    return {
      sx, sy,           // 起始位置
      tx, ty,           // 目标位置
      x: sx, y: sy,     // 当前位置（动画中更新）
      r: Math.random() * 1.4 + 0.8,          // 粒子半径(0.8-2.2px)
      delay: Math.random() * 0.35,            // 错峰延迟(0-0.35s)
      wobble: Math.random() * Math.PI * 2     // 成型后微颤相位
    }
  })
}

/**
 * 缓动函数：easeOutCubic
 * 先快后慢，符合自然运动规律
 * @param {number} t - 进度(0-1)
 * @returns {number} 缓动后的进度(0-1)
 */
const easeOutCubic = t => 1 - Math.pow(1 - t, 3)

/**
 * 汇聚粒子动画主循环
 * 每帧更新所有粒子位置，绘制到Canvas
 * @param {number} now - 当前时间戳(由requestAnimationFrame提供)
 */
const drawAssemble = (now) => {
  if (!phaseStart) phaseStart = now
  const elapsed = now - phaseStart
  asCtx.clearRect(0, 0, asW, asH)

  for (const p of targets) {
    // 计算每颗粒子的进度（含错峰延迟）
    let t = (elapsed / ASSEMBLE_MS - p.delay) / (1 - p.delay)
    t = Math.max(0, Math.min(1, t))  // 限制在0-1范围
    const e = easeOutCubic(t)        // 应用缓动

    // 插值计算当前位置
    p.x = p.sx + (p.tx - p.sx) * e
    p.y = p.sy + (p.ty - p.sy) * e

    // 成型后轻微颤动（保持"能量感"）
    let dx = 0, dy = 0
    if (t >= 1) {
      p.wobble += 0.05
      dx = Math.cos(p.wobble) * 0.6
      dy = Math.sin(p.wobble * 1.3) * 0.6
    }

    // 越接近目标越亮（alpha从0.35增长到0.95）
    const alpha = 0.35 + e * 0.6
    const glow = 2 + e * 4  // 辉光从2px增长到6px

    // 绘制粒子
    asCtx.beginPath()
    asCtx.arc(p.x + dx, p.y + dy, p.r, 0, Math.PI * 2)
    // 颜色变化：汇聚中偏金，点亮后偏橙
    const col = cartLit.value ? '255, 130, 40' : '255, 190, 120'
    asCtx.fillStyle = `rgba(${col}, ${alpha})`
    asCtx.shadowBlur = glow
    asCtx.shadowColor = `rgba(255, 150, 60, ${alpha})`
    asCtx.fill()
  }
  asCtx.shadowBlur = 0

  // 点亮后粒子逐渐淡出，把主视觉交给发光SVG（保留少量环绕感）
  if (cartLit.value) {
    asCtx.globalAlpha = Math.max(0, asCtx.globalAlpha - 0.006)
    if (asCtx.globalAlpha <= 0.15) {
      asCtx.globalAlpha = 0.15  // 最低保留15%透明度
    }
  }

  asRaf = requestAnimationFrame(drawAssemble)
}

/* ====================================================================
   模块3：尺寸适配与响应式
   ====================================================================
   【DPR处理】devicePixelRatio适配高分辨率屏幕
   - Canvas实际尺寸 = CSS尺寸 × DPR
   - 通过setTransform缩放绘制坐标，避免模糊

   【响应式】
   - 窗口大小变化时重新计算Canvas尺寸和粒子目标位置
   - 移动端/桌面端使用不同的粒子数量和采样密度
   ==================================================================== */
const resize = () => {
  dpr = Math.min(window.devicePixelRatio || 1, 2)  // 限制最大DPR为2，避免性能问题

  // 背景层尺寸设置
  bgW = window.innerWidth; bgH = window.innerHeight
  bgCanvas.value.width = bgW * dpr; bgCanvas.value.height = bgH * dpr
  bgCanvas.value.style.width = bgW + 'px'; bgCanvas.value.style.height = bgH + 'px'
  bgCtx.setTransform(dpr, 0, 0, dpr, 0, 0)  // 缩放绘制坐标
  buildBgParticles()  // 重新生成粒子

  // 汇聚层尺寸设置
  asW = window.innerWidth; asH = window.innerHeight
  assembleCanvas.value.width = asW * dpr; assembleCanvas.value.height = asH * dpr
  assembleCanvas.value.style.width = asW + 'px'; assembleCanvas.value.style.height = asH + 'px'
  asCtx.setTransform(dpr, 0, 0, dpr, 0, 0)
  layoutTargets()  // 重新计算目标位置
}

/* ====================================================================
   模块4：交互处理
   ==================================================================== */
const enterStore = () => {
  if (isLeaving.value) return  // 防止重复点击
  isLeaving.value = true       // 触发退场动画（CSS transition: opacity + blur）
  setTimeout(() => {
    router.push('/store')      // 950ms后跳转到商城首页
  }, 950)
}

/* ====================================================================
   模块5：时间轴编排
   ====================================================================
   【动画时间轴】
   0-2s:   粒子从四周汇聚成购物车轮廓
   2-3s:   购物车被橙光点亮，SVG淡入
   3-4s:   能量环+轨道粒子出现
   4s+:    文案依次浮现(eyebrow→title→tagline→desc→enter-block)

   【状态控制】
   - cartLit: 控制SVG淡入和粒子颜色变化
   - ringsShow: 控制能量环显隐
   - contentShow: 控制文案区依次浮现
   - isLeaving: 控制退场动画
   ==================================================================== */
let timers = []
const scheduleTimeline = () => {
  timers.push(setTimeout(() => { cartLit.value = true }, 2000))    // 2s: 点亮
  timers.push(setTimeout(() => { ringsShow.value = true }, 3000))  // 3s: 能量环
  timers.push(setTimeout(() => { contentShow.value = true }, 4000)) // 4s: 文案
}

/* ====================================================================
   生命周期
   ==================================================================== */
onMounted(() => {
  // 获取Canvas 2D上下文
  bgCtx = bgCanvas.value.getContext('2d')
  asCtx = assembleCanvas.value.getContext('2d')

  // 初始化尺寸和粒子
  resize()
  window.addEventListener('resize', resize)

  // 启动动画
  drawBg()                        // 背景粒子动画
  asRaf = requestAnimationFrame(drawAssemble)  // 汇聚粒子动画
  scheduleTimeline()              // 启动时间轴
})

onBeforeUnmount(() => {
  // 清理：取消动画帧、移除事件监听、清除定时器
  if (bgRaf) cancelAnimationFrame(bgRaf)
  if (asRaf) cancelAnimationFrame(asRaf)
  window.removeEventListener('resize', resize)
  timers.forEach(clearTimeout)
})
</script>

<style lang="scss" scoped>
/* ====================================================================
   SCSS变量定义
   ==================================================================== */
$deep-1: #04070f;      // 最深色：曜石黑
$deep-2: #081328;      // 深蓝色：夜空
$deep-3: #0c1f42;      // 中蓝色：深海
$orange: #ff7a18;      // 主题橙色
$orange-hot: #ff4400;   // 热力橙红
$gold: #ffd28c;        // 金色高光

/* ====================================================================
   根容器 .landing
   ====================================================================
   【作用】全屏固定定位的根容器，包含所有视觉层
   【背景】4层渐变叠加：
     1. 中心暖光晕(橙色径向渐变) - 营造光源感
     2. 顶部冷蓝(蓝色径向渐变) - 营造天空感
     3. 深色主体(三色径向渐变) - 营造纵深
     4. 底部线性渐变 - 过渡到纯黑

   【退场动画】is-leaving类触发：
     - opacity: 0 (淡出)
     - filter: blur(8px) (模糊)
     - transition: 0.95s ease (平滑过渡)
   ==================================================================== */
.landing {
  position: fixed;
  inset: 0;
  overflow: hidden;
  // 多层深色渐变：中心暖光晕 + 顶部冷蓝 + 底部曜石黑，叠出纵深
  background:
    radial-gradient(ellipse 90% 60% at 50% 32%, rgba(255,122,24,0.10) 0%, transparent 55%),
    radial-gradient(ellipse 120% 80% at 50% 0%, rgba(40,90,180,0.18) 0%, transparent 60%),
    radial-gradient(ellipse 130% 90% at 50% 30%, $deep-3 0%, $deep-2 48%, $deep-1 100%),
    linear-gradient(180deg, #06101f 0%, #030711 100%);
  font-family: -apple-system, "PingFang SC", "Microsoft YaHei", "Segoe UI", sans-serif;
  transition: opacity 0.95s ease, filter 0.95s ease;
  // 背景由深色渐显（初始opacity:0.4 → 1）
  animation: bgFadeIn 1.2s ease forwards;

  &.is-leaving {
    opacity: 0;
    filter: blur(8px);
  }
}

@keyframes bgFadeIn {
  from { opacity: 0.4; }
  to { opacity: 1; }
}

/* ====================================================================
   Canvas层定位
   ====================================================================
   【z-index层级】
   - bg-canvas(z-index:1): 背景星尘粒子
   - assemble-canvas(z-index:4): 汇聚粒子（购物车轮廓）
   - pointer-events:none: 穿透点击事件到下层
   ==================================================================== */
.bg-canvas { position: absolute; inset: 0; z-index: 1; }
.assemble-canvas { position: absolute; inset: 0; z-index: 4; pointer-events: none; }

/* ====================================================================
   星云层 (.aurora)
   ====================================================================
   【作用】更大范围的模糊光团，与柔光云叠加增强星云效果
   【技术】filter:blur(100px) + 慢速漂移动画(15-22s周期)
   【颜色】橙色(blob-1) + 蓝色(blob-2) + 浅蓝(blob-3)
   ==================================================================== */
.aurora { position: absolute; inset: 0; z-index: 0; overflow: hidden; pointer-events: none; }
.aurora-blob {
  position: absolute; border-radius: 50%; filter: blur(100px); opacity: 0.5;
  &.blob-1 {
    width: 46vw; height: 46vw; top: 4%; left: 50%; transform: translateX(-50%);
    background: radial-gradient(circle, rgba(255,122,24,0.34), transparent 70%);
    animation: drift 15s ease-in-out infinite;
  }
  &.blob-2 {
    width: 42vw; height: 42vw; bottom: -10%; left: 10%;
    background: radial-gradient(circle, rgba(28,84,190,0.42), transparent 70%);
    animation: drift 19s ease-in-out infinite reverse;
  }
  &.blob-3 {
    width: 34vw; height: 34vw; top: 18%; right: 4%;
    background: radial-gradient(circle, rgba(70,130,255,0.28), transparent 70%);
    animation: drift 22s ease-in-out infinite;
  }
}
/* 漂移动画：缓慢位移+缩放，周期15-22秒 */
@keyframes drift {
  0%, 100% { transform: translate(0,0) scale(1); }
  50% { transform: translate(-4%,4%) scale(1.08); }
}

/* ====================================================================
   柔光粒子云 (.nebula)
   ====================================================================
   【作用】大面积低透明度模糊光团，缓慢流动
   【技术】border-radius:50% + filter:blur(70px) + mix-blend-mode:screen
   【混合模式】screen(滤色)：提亮效果，叠加后更亮
   ==================================================================== */
.nebula { position: absolute; inset: 0; z-index: 0; overflow: hidden; pointer-events: none; }
.cloud {
  position: absolute;
  border-radius: 50%;
  filter: blur(70px);
  mix-blend-mode: screen;  // 滤色混合，提亮效果
  opacity: 0.35;
  &.cloud-1 {
    width: 38vw; height: 38vw; top: 12%; left: 8%;
    background: radial-gradient(circle, rgba(255,150,70,0.35), transparent 68%);
    animation: cloudFlow 26s ease-in-out infinite;
  }
  &.cloud-2 {
    width: 32vw; height: 32vw; top: 45%; right: 6%;
    background: radial-gradient(circle, rgba(90,150,255,0.32), transparent 68%);
    animation: cloudFlow 32s ease-in-out infinite reverse;
  }
  &.cloud-3 {
    width: 28vw; height: 28vw; bottom: 8%; left: 30%;
    background: radial-gradient(circle, rgba(160,120,255,0.22), transparent 70%);
    animation: cloudFlow 30s ease-in-out infinite;
  }
  &.cloud-4 {
    width: 24vw; height: 24vw; top: 6%; right: 22%;
    background: radial-gradient(circle, rgba(255,200,130,0.2), transparent 70%);
    animation: cloudFlow 24s ease-in-out infinite reverse;
  }
}
/* 柔光云流动动画：位移+缩放+透明度变化，周期24-32秒 */
@keyframes cloudFlow {
  0%, 100% { transform: translate(0,0) scale(1); opacity: 0.28; }
  50% { transform: translate(6%,-5%) scale(1.15); opacity: 0.45; }
}

/* ====================================================================
   噪点纹理 (.noise)
   ====================================================================
   【作用】极低透明度叠加，消除渐变色带、增加画面质感
   【技术】内联SVG的feTurbulence分形噪声
   【参数】baseFrequency:0.85(高频细节), numOctaves:2(2层叠加)
   【混合模式】overlay(叠加)：增强对比度
   ==================================================================== */
.noise {
  position: absolute; inset: 0; z-index: 2; pointer-events: none;
  opacity: 0.05;  // 极低透明度，几乎不可见但能消除色带
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='160' height='160'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='2' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
  background-size: 160px 160px;  // 平铺尺寸
  mix-blend-mode: overlay;  // 叠加混合，增强对比
}

/* ====================================================================
   星点扩散层 (.stars)
   ====================================================================
   【作用】随机分布的微小星点，由中心淡出并放大，营造星尘扩散感
   【技术】2px白色圆点 + box-shadow辉光 + 缩放+透明度脉冲动画
   【动画】starPulse：opacity 0→0.9→0, scale 0.4→1.3→0.4
   ==================================================================== */
.stars { position: absolute; inset: 0; z-index: 2; pointer-events: none; }
.star {
  position: absolute;
  width: 2px; height: 2px;
  border-radius: 50%;
  background: rgba(255,255,255,0.9);
  box-shadow: 0 0 6px rgba(180,210,255,0.8);  // 蓝白色辉光
  opacity: 0;
  animation: starPulse ease-in-out infinite;
}
@keyframes starPulse {
  0%, 100% { opacity: 0; transform: scale(0.4); }   // 淡出+缩小
  50% { opacity: 0.9; transform: scale(1.3); }       // 淡入+放大
}

/* ====================================================================
   数字网格 (.grid-overlay)
   ====================================================================
   【作用】营造科技感的网格背景
   【技术】background-image线性渐变绘制网格线
         + mask-image径向渐变遮罩(中心可见，边缘消失)
   【参数】64px网格间距，极低透明度(0.045)
   ==================================================================== */
.grid-overlay {
  position: absolute; inset: 0; z-index: 1; pointer-events: none;
  background-image:
    linear-gradient(rgba(120,170,255,0.045) 1px, transparent 1px),  // 水平线
    linear-gradient(90deg, rgba(120,170,255,0.045) 1px, transparent 1px);  // 垂直线
  background-size: 64px 64px;  // 网格间距
  // 径向渐变遮罩：中心可见(68%×58%)，边缘透明
  mask-image: radial-gradient(ellipse 68% 58% at 50% 40%, black 28%, transparent 74%);
  //-webkit-mask-image: radial-gradient(ellipse 68% 58% at 50% 40%, black 28%, transparent 74%);
}

/* ====================================================================
   购物车主视觉 (.hero)
   ====================================================================
   【作用】居中放置SVG购物车和能量环，是页面的视觉焦点
   【定位】top:32% 垂直略偏上，符合视觉重心偏上的设计原则
   【尺寸】340px×340px(桌面)，260px×260px(移动端)
   ==================================================================== */
.hero {
  position: absolute;
  top: 32%;  // 垂直略偏上，视觉重心
  left: 50%;
  transform: translate(-50%, -50%);
  width: 340px;
  height: 340px;
  z-index: 5;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

/* ====================================================================
   SVG购物车 (.cart-svg)
   ====================================================================
   【初始状态】opacity:0, scale(0.94) - 隐藏且略小
   【点亮后】hero-lit类触发：
     - opacity:1 (淡入)
     - scale(1) (恢复原始大小)
     - cartFloat动画(5.5s周期悬浮)
   ==================================================================== */
.cart-svg {
  width: 340px;
  height: 283px;
  opacity: 0;  // 初始隐藏
  transform: scale(0.94);  // 初始略小
  transition: opacity 1s ease, transform 1s ease, filter 0.8s ease;
  filter: drop-shadow(0 0 10px rgba(255,122,24,0.35));  // 橙色外发光
}

// 点亮后 SVG 淡入 + 悬浮动画
.hero-lit .cart-svg {
  opacity: 1;
  transform: scale(1);
  animation: cartFloat 5.5s ease-in-out 1s infinite;  // 延迟1s后开始悬浮
}

/* 悬浮动画：上下10px摆动，周期5.5秒 */
@keyframes cartFloat {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-10px) scale(1); }
}

/* ====================================================================
   能量环 / 轨道 (.rings)
   ====================================================================
   【组成】
   - energy-core: 玻璃能量核心(径向渐变+模糊光晕)
   - ring-glass: 玻璃质感环(半透明+内外光晕+反光)
   - ring-1/2/3: 三层旋转环(不同颜色/速度/方向)
   - orbit-dot: 轨道粒子(绕车公转)

   【动画】
   - coreBreathe: 核心呼吸(4.5s周期，scale 1→1.12→1)
   - glassSpin: 玻璃环旋转(16s周期，逆时针)
   - glassGlow: 玻璃环光晕脉冲(4.5s周期)
   - spin: 普通环旋转(7-11s周期，顺/逆时针)
   - orbitA/B/C: 轨道粒子公转(7-11s周期)
   ==================================================================== */
.rings {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;  // 初始隐藏
  transition: opacity 1.2s ease;
}
.rings-show { opacity: 1; }  // 3s后显示

/* 玻璃能量核心 */
.energy-core {
  position: absolute;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  // 径向渐变：中心暖橙 → 中间橙 → 边缘蓝 → 透明
  background: radial-gradient(circle at 50% 45%,
    rgba(255,190,120,0.28) 0%,
    rgba(255,122,24,0.16) 32%,
    rgba(120,160,255,0.08) 58%,
    transparent 72%);
  filter: blur(6px);  // 柔化边缘
  animation: coreBreathe 4.5s ease-in-out infinite;
}
/* 呼吸动画：scale和opacity周期性变化 */
@keyframes coreBreathe {
  0%, 100% { transform: scale(1); opacity: 0.7; }
  50% { transform: scale(1.12); opacity: 1; }
}

.ring {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(255,170,90,0.22);

  /* 玻璃质感环：半透明厚边框 + 内外双层光晕 + 微弱径向反光 */
  &.ring-glass {
    width: 322px;
    height: 322px;
    border: 2px solid rgba(255,255,255,0.14);
    // 径向渐变：中心透明 → 边缘橙色反光 → 白色高光 → 透明
    background: radial-gradient(circle,
      transparent 62%,
      rgba(255,180,120,0.06) 74%,
      rgba(255,255,255,0.10) 82%,
      transparent 90%);
    // 多层阴影：外发光 + 内发光 + 顶部高光
    box-shadow:
      0 0 34px rgba(255,122,24,0.28),      // 外发光
      inset 0 0 26px rgba(255,190,130,0.22), // 内发光
      inset 0 2px 14px rgba(255,255,255,0.18); // 顶部高光
    backdrop-filter: blur(2px);  // 背景模糊
    animation: glassSpin 16s linear infinite, glassGlow 4.5s ease-in-out infinite;
  }

  &.ring-1 {
    width: 300px; height: 300px;
    border-top-color: rgba(255,122,24,0.85);    // 顶部亮橙
    border-right-color: rgba(255,122,24,0.25);  // 右侧暗橙
    box-shadow: 0 0 16px rgba(255,122,24,0.35), inset 0 0 12px rgba(255,140,50,0.12);
    animation: spin 7s linear infinite;  // 7秒一圈，顺时针
  }
  &.ring-2 {
    width: 350px; height: 350px;
    border-left-color: rgba(120,180,255,0.6);   // 左侧亮蓝
    border-bottom-color: rgba(120,180,255,0.18); // 底部暗蓝
    box-shadow: 0 0 16px rgba(90,150,255,0.28), inset 0 0 12px rgba(120,180,255,0.1);
    animation: spin 11s linear infinite reverse;  // 11秒一圈，逆时针
  }
  &.ring-3 {
    width: 260px; height: 260px;
    border-bottom-color: rgba(255,205,140,0.7);  // 底部亮金
    border-left-color: rgba(255,205,140,0.2);    // 左侧暗金
    box-shadow: 0 0 14px rgba(255,205,140,0.3);
    animation: spin 9s linear infinite;  // 9秒一圈，顺时针
  }
}
/* 旋转动画 */
@keyframes spin { to { transform: rotate(360deg); } }
@keyframes glassSpin { to { transform: rotate(-360deg); } }  // 逆时针
/* 玻璃环光晕脉冲 */
@keyframes glassGlow {
  0%, 100% { box-shadow: 0 0 30px rgba(255,122,24,0.22), inset 0 0 24px rgba(255,190,130,0.18), inset 0 2px 14px rgba(255,255,255,0.16); }
  50% { box-shadow: 0 0 48px rgba(255,122,24,0.42), inset 0 0 32px rgba(255,190,130,0.3), inset 0 2px 16px rgba(255,255,255,0.24); }
}

/* ====================================================================
   轨道粒子 (.orbit-dot)
   ====================================================================
   【作用】绕购物车公转的小光点，增强科技感
   【技术】transform: rotate() translateX() rotate() 实现绕中心公转
         第二个rotate(-angle)保持粒子自身不旋转
   【轨道半径】dot-a:150px, dot-b:175px, dot-c:130px
   ==================================================================== */
.orbit-dot {
  position: absolute;
  width: 8px; height: 8px; border-radius: 50%;
  background: radial-gradient(circle, #ffd9a0, #ff7a18);  // 金到橙渐变
  box-shadow: 0 0 12px rgba(255,140,50,0.9);  // 橙色辉光
}
.dot-a { animation: orbitA 7s linear infinite; }   // 7秒一圈
.dot-b { animation: orbitB 11s linear infinite; }  // 11秒一圈
.dot-c { width: 6px; height: 6px; animation: orbitC 9s linear infinite; }  // 9秒一圈，更小
/* 公转动画：rotate + translateX + counter-rotate */
@keyframes orbitA { from { transform: rotate(0) translateX(150px) rotate(0); } to { transform: rotate(360deg) translateX(150px) rotate(-360deg); } }
@keyframes orbitB { from { transform: rotate(120deg) translateX(175px) rotate(-120deg); } to { transform: rotate(480deg) translateX(175px) rotate(-480deg); } }
@keyframes orbitC { from { transform: rotate(240deg) translateX(130px) rotate(-240deg); } to { transform: rotate(600deg) translateX(130px) rotate(-600deg); } }

/* ====================================================================
   交互增强：悬停按钮时购物车与光环同步增强
   ====================================================================
   【技术】:has()选择器 - 父元素包含某子元素时应用样式
   【效果】购物车外发光增强，能量环颜色变亮
   ==================================================================== */
.landing:has(.enter-btn:hover) .cart-svg {
  filter: drop-shadow(0 0 22px rgba(255,122,24,0.85));  // 外发光增强
}
.landing:has(.enter-btn:hover) .ring-1 { border-top-color: rgba(255,122,24,1); }  // 环变亮

/* ====================================================================
   文字内容区 (.content)
   ====================================================================
   【作用】品牌标语和进入按钮，购物车下方共享中轴
   【动画】content-show类触发依次浮现：
     - eyebrow: 0s延迟
     - title: 0.15s延迟
     - tagline: 0.35s延迟
     - desc: 0.55s延迟
     - enter-block: 0.75s延迟
   【技术】opacity + translateY(24px→0) + transition-delay
   ==================================================================== */
.content {
  position: absolute;
  top: 54%;  // 购物车下方
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 720px;
  padding: 0 24px;
  text-align: center;
  z-index: 6;
}

/* 文字元素初始状态：隐藏+下移 */
.eyebrow, .title, .tagline, .desc, .tags, .enter-block {
  opacity: 0;
  transform: translateY(24px);  // 初始下移24px
  transition: opacity 0.9s ease, transform 0.9s cubic-bezier(0.22,1,0.36,1);
}
// 依次浮现：添加content-show类后，各元素按延迟依次淡入上移
.content-show {
  .eyebrow { opacity: 1; transform: translateY(0); transition-delay: 0s; }        // 0s
  .title { opacity: 1; transform: translateY(0); transition-delay: 0.15s; }       // 0.15s
  .tagline { opacity: 1; transform: translateY(0); transition-delay: 0.35s; }     // 0.35s
  .desc { opacity: 1; transform: translateY(0); transition-delay: 0.55s; }        // 0.55s
  .enter-block { opacity: 1; transform: translateY(0); transition-delay: 0.75s; } // 0.75s
}

/* 副标题：小字、宽字距、暖金色 */
.eyebrow {
  font-size: 13px; letter-spacing: 6px; font-weight: 500;
  color: rgba(255,200,150,0.72); margin-bottom: 14px;
}
/* 主标题：大字、渐变色(白→金→橙) */
.title {
  font-size: 50px; font-weight: 800; letter-spacing: 4px; margin-bottom: 16px;
  // 文字渐变：左上白 → 中间金 → 右下橙
  background: linear-gradient(120deg, #fff 20%, $gold 55%, $orange 100%);
  -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent;
  text-shadow: 0 4px 30px rgba(255,122,24,0.25);  // 橙色投影
}
/* 标语 */
.tagline {
  font-size: 20px; font-weight: 500; letter-spacing: 1px;
  color: rgba(255,255,255,0.92); margin-bottom: 16px;
}
/* 描述文字 */
.desc {
  font-size: 15px; line-height: 2; color: rgba(200,215,245,0.66);
  max-width: 540px; margin: 0 auto 32px;
}

/* ====================================================================
   进入商城按钮 (.enter-btn)
   ====================================================================
   【样式】圆角胶囊按钮(50px)，橙红渐变背景
   【交互效果】
     - hover: 上移4px + 放大1.03 + 光晕增强 + 箭头右移 + 光泽扫过
     - active: 轻微下沉(模拟按下)
   【呼吸动画】btnBreathe：box-shadow周期性脉冲，引导用户点击
   【光泽效果】btn-shine：白色斜角条从左到右扫过(skewX(-20deg))
   ==================================================================== */
.enter-block { display: flex; flex-direction: column; align-items: center; }
.enter-btn {
  position: relative; display: inline-flex; align-items: center; gap: 12px;
  padding: 16px 54px; font-size: 17px; font-weight: 600; letter-spacing: 2px;
  color: #fff; border: none; border-radius: 50px; cursor: pointer; overflow: hidden;
  // 橙红渐变背景
  background: linear-gradient(120deg, $orange-hot, $orange 60%, #ff9a4d);
  box-shadow: 0 8px 30px rgba(255,100,20,0.45);
  animation: btnBreathe 3s ease-in-out infinite;  // 呼吸动画
  transition: transform 0.35s cubic-bezier(0.22,1,0.36,1), box-shadow 0.35s ease;

  .btn-arrow { transition: transform 0.35s ease; }  // 箭头过渡
  .btn-shine {
    // 光泽条：白色渐变，倾斜20度
    position: absolute; top: 0; left: -120%; width: 80%; height: 100%;
    background: linear-gradient(100deg, transparent, rgba(255,255,255,0.5), transparent);
    transform: skewX(-20deg); transition: left 0.7s ease;
  }
  &:hover {
    transform: translateY(-4px) scale(1.03);  // 上移+放大
    box-shadow: 0 16px 46px rgba(255,100,20,0.65), 0 0 60px rgba(255,122,24,0.5);  // 光晕增强
    .btn-arrow { transform: translateX(6px); }  // 箭头右移
    .btn-shine { left: 120%; }  // 光泽扫过
  }
  &:active { transform: translateY(-1px) scale(0.99); }  // 按下效果
}
/* 按钮呼吸动画：box-shadow脉冲 */
@keyframes btnBreathe {
  0%, 100% { box-shadow: 0 8px 30px rgba(255,100,20,0.4); }
  50% { box-shadow: 0 8px 40px rgba(255,122,24,0.75), 0 0 40px rgba(255,122,24,0.4); }
}

/* 按钮提示文字 */
.enter-hint {
  margin-top: 20px; font-size: 13px; color: rgba(200,215,245,0.5);
  animation: hintPulse 2.6s ease-in-out infinite;  // 透明度脉冲
}
@keyframes hintPulse { 0%,100% { opacity: 0.5; } 50% { opacity: 0.85; } }

/* 页脚 */
.footer {
  position: absolute; bottom: 24px; left: 0; right: 0; text-align: center; z-index: 6;
  font-size: 12px; letter-spacing: 1px; color: rgba(180,200,235,0.4);
}

/* ====================================================================
   点击光波效果 (.burst)
   ====================================================================
   【作用】点击"进入商城"后，从购物车位置向外扩散橙色光波
   【技术】radial-gradient圆形 + burstOut动画(0.95s)
   【尺寸】初始120px → 最终240vmax(覆盖全屏)
   ==================================================================== */
.burst {
  position: absolute; top: 32%; left: 50%;  // 与购物车位置一致
  width: 120px; height: 120px; border-radius: 50%;
  transform: translate(-50%, -50%); z-index: 7;
  background: radial-gradient(circle, rgba(255,122,24,0.65), transparent 70%);
  animation: burstOut 0.95s ease-out forwards;  // 0.95秒后完成
}
/* 光波扩散动画 */
@keyframes burstOut {
  0% { width: 120px; height: 120px; opacity: 0.9; }     // 初始：小而亮
  100% { width: 240vmax; height: 240vmax; opacity: 0; } // 最终：巨大而透明
}

/* ====================================================================
   响应式设计
   ==================================================================== */
@media (max-width: 768px) {
  // 平板/手机适配
  .hero { top: 30%; width: 260px; height: 260px; }
  .cart-svg { width: 260px; height: 216px; }
  .energy-core { width: 230px; height: 230px; }
  .ring-glass { width: 248px; height: 248px; }
  .ring-1 { width: 230px; height: 230px; }
  .ring-2 { width: 270px; height: 270px; }
  .ring-3 { width: 200px; height: 200px; }
  // 轨道半径缩小
  @keyframes orbitA { from { transform: rotate(0) translateX(115px) rotate(0); } to { transform: rotate(360deg) translateX(115px) rotate(-360deg); } }
  @keyframes orbitB { from { transform: rotate(120deg) translateX(135px) rotate(-120deg); } to { transform: rotate(480deg) translateX(135px) rotate(-480deg); } }
  @keyframes orbitC { from { transform: rotate(240deg) translateX(100px) rotate(-240deg); } to { transform: rotate(600deg) translateX(100px) rotate(-600deg); } }
  // 文字缩小
  .content { top: 52%; }
  .title { font-size: 34px; letter-spacing: 2px; }
  .tagline { font-size: 17px; }
  .desc { font-size: 13.5px; padding: 0 6px; }
  .eyebrow { font-size: 11px; letter-spacing: 4px; }
  .enter-btn { padding: 14px 46px; font-size: 16px; }
}

@media (max-width: 380px) {
  // 小屏手机进一步缩小
  .title { font-size: 28px; }
}

/* ====================================================================
   无障碍：减少动画偏好
   ====================================================================
   【作用】尊重用户系统设置的"减少动画"偏好
   【技术】prefers-reduced-motion: reduce 媒体查询
   【效果】禁用所有动画，仅保留静态视觉
   ==================================================================== */
@media (prefers-reduced-motion: reduce) {
  .cart-svg, .ring, .energy-core, .orbit-dot, .enter-btn, .aurora-blob, .cloud, .star, .enter-hint {
    animation: none !important;
  }
}
</style>
