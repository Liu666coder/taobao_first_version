<template>
  <div class="landing" :class="{ 'is-leaving': isLeaving }">
    <!-- 汇聚粒子层（购物车由此拼成） -->
    <canvas ref="assembleCanvas" class="assemble-canvas"></canvas>

    <!-- 背景星尘粒子层 -->
    <canvas ref="bgCanvas" class="bg-canvas"></canvas>

    <!-- 柔光粒子云（星云） -->
    <div class="nebula">
      <span class="cloud cloud-1"></span>
      <span class="cloud cloud-2"></span>
      <span class="cloud cloud-3"></span>
      <span class="cloud cloud-4"></span>
    </div>

    <!-- 星云光晕 -->
    <div class="aurora">
      <span class="aurora-blob blob-1"></span>
      <span class="aurora-blob blob-2"></span>
      <span class="aurora-blob blob-3"></span>
    </div>

    <!-- 星点扩散层 -->
    <div class="stars">
      <span v-for="s in starList" :key="s.id" class="star"
            :style="{ left: s.left, top: s.top, animationDelay: s.delay, animationDuration: s.dur }"></span>
    </div>

    <!-- 细腻噪点纹理 -->
    <div class="noise"></div>

    <!-- 极淡数字网格 -->
    <div class="grid-overlay"></div>

    <!-- 购物车主视觉容器（居中轴） -->
    <div class="hero" :class="{ 'hero-lit': cartLit }">
      <!-- 能量环 / 轨道 -->
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

      <!-- 发光玻璃购物车 SVG（点亮阶段淡入，与粒子轮廓重合） -->
      <svg class="cart-svg" viewBox="0 0 240 200" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <linearGradient id="strokeGrad" x1="0" y1="0" x2="240" y2="200" gradientUnits="userSpaceOnUse">
            <stop offset="0" stop-color="#FFE3B8" />
            <stop offset="0.45" stop-color="#FF9A3D" />
            <stop offset="1" stop-color="#FF4400" />
          </linearGradient>
          <linearGradient id="glassGrad" x1="0" y1="40" x2="0" y2="150" gradientUnits="userSpaceOnUse">
            <stop offset="0" stop-color="rgba(255,180,110,0.30)" />
            <stop offset="1" stop-color="rgba(255,90,20,0.05)" />
          </linearGradient>
          <radialGradient id="coreGlow" cx="0.5" cy="0.45" r="0.6">
            <stop offset="0" stop-color="rgba(255,140,40,0.75)" />
            <stop offset="1" stop-color="rgba(255,140,40,0)" />
          </radialGradient>
          <filter id="softGlow" x="-40%" y="-40%" width="180%" height="180%">
            <feGaussianBlur stdDeviation="3.2" result="b" />
            <feMerge>
              <feMergeNode in="b" />
              <feMergeNode in="SourceGraphic" />
            </feMerge>
          </filter>
        </defs>

        <!-- 发光核心 -->
        <ellipse cx="120" cy="95" rx="86" ry="70" fill="url(#coreGlow)" />

        <g filter="url(#softGlow)" stroke="url(#strokeGrad)" stroke-width="5"
           stroke-linecap="round" stroke-linejoin="round" fill="none">
          <!-- 提手 + 车斗外框 -->
          <path d="M28 40 H52 L74 132 H176 L196 66 H86"
                fill="url(#glassGrad)" />
          <!-- 车斗竖向格栅（玻璃感） -->
          <path d="M96 78 L102 120" stroke-width="3.5" opacity="0.7" />
          <path d="M118 78 L120 120" stroke-width="3.5" opacity="0.7" />
          <path d="M140 78 L138 120" stroke-width="3.5" opacity="0.7" />
          <path d="M162 78 L156 120" stroke-width="3.5" opacity="0.7" />
          <!-- 横向格栅 -->
          <path d="M84 92 H190" stroke-width="3" opacity="0.5" />
          <path d="M88 108 H182" stroke-width="3" opacity="0.5" />
          <!-- 轮子 -->
          <circle cx="92" cy="156" r="11" fill="rgba(255,120,30,0.18)" />
          <circle cx="164" cy="156" r="11" fill="rgba(255,120,30,0.18)" />
        </g>

        <!-- 高光反射 -->
        <path d="M92 66 L104 66 L96 92 L82 92 Z" fill="rgba(255,255,255,0.20)" />
      </svg>
    </div>

    <!-- 文字内容（购物车下方，共享中轴） -->
    <div class="content" :class="{ 'content-show': contentShow }">
      <p class="eyebrow">CAMPUS SELECTED MALL</p>
      <h1 class="title">校园淘宝商城</h1>
      <p class="tagline">一辆购物车，装下整个校园的期待</p>
      <p class="desc">
        从清晨补给到深夜慰藉，从学习日常到生活灵感，<br />
        精选好物正沿着星光轨迹，抵达你的大学时光。
      </p>

      <div class="tags">
        <span v-for="(tag, i) in tags" :key="tag" class="tag"
              :style="{ transitionDelay: `${i * 0.12}s` }">{{ tag }}</span>
      </div>

      <div class="enter-block">
        <button class="enter-btn" @click="enterStore">
          <span class="btn-label">进入商城</span>
          <span class="btn-arrow">→</span>
          <span class="btn-shine"></span>
        </button>
        <p class="enter-hint">轻触，让好物向你靠近</p>
      </div>
    </div>

    <footer class="footer">© 2026 校园淘宝商城 · 为大学生活精选每一份刚好</footer>

    <!-- 点击后向外扩散的橙色光波 -->
    <div v-if="isLeaving" class="burst"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const assembleCanvas = ref(null)
const bgCanvas = ref(null)

// 分阶段状态标志
const cartLit = ref(false)      // 购物车被橙光点亮
const ringsShow = ref(false)    // 能量环 / 轨道粒子出现
const contentShow = ref(false)  // 文案依次浮现
const isLeaving = ref(false)    // 退场

const tags = ['精选好物', '校园极速达', '安心售后', '专属优惠']

// 星点扩散：随机分布的闪烁星点（移动端减少）
const starCount = typeof window !== 'undefined' && window.innerWidth < 768 ? 22 : 42
const starList = Array.from({ length: starCount }, (_, i) => ({
  id: i,
  left: `${Math.random() * 100}%`,
  top: `${Math.random() * 100}%`,
  delay: `${(Math.random() * 6).toFixed(2)}s`,
  dur: `${(Math.random() * 4 + 3).toFixed(2)}s`
}))

/* ============ 背景星尘粒子 ============ */
let bgCtx = null, bgRaf = null, bgParticles = []
let bgW = 0, bgH = 0, dpr = 1

// 按景深插值颜色：远处偏冷蓝、暗；近处偏暖金、亮（不刺眼）
// depth: 0 = 最远, 1 = 最近
const depthColor = (depth) => {
  // 远景微光蓝 -> 近景暖金橙
  const far = [130, 175, 235]
  const near = [255, 200, 140]
  const r = Math.round(far[0] + (near[0] - far[0]) * depth)
  const g = Math.round(far[1] + (near[1] - far[1]) * depth)
  const b = Math.round(far[2] + (near[2] - far[2]) * depth)
  return `${r}, ${g}, ${b}`
}

const buildBgParticles = () => {
  const w = window.innerWidth
  const count = w < 480 ? 45 : w < 768 ? 80 : 150
  bgParticles = []
  for (let i = 0; i < count; i++) {
    // 连续景深：偏向远处更多（sqrt 让远景粒子占比更高，营造纵深）
    const depth = Math.pow(Math.random(), 1.4)
    bgParticles.push({
      x: Math.random() * bgW,
      y: Math.random() * bgH,
      depth,
      // 远处小(0.3px) -> 近处略大(2.4px)
      r: 0.3 + depth * 2.1,
      color: depthColor(depth),
      // 远处暗(0.12) -> 近处亮但封顶(0.68)，避免刺眼
      baseAlpha: 0.12 + depth * 0.56,
      // 近处漂移快，远处几乎静止，强化视差
      vx: (Math.random() - 0.5) * (0.05 + depth * 0.28),
      vy: (Math.random() - 0.5) * (0.05 + depth * 0.28),
      tw: Math.random() * Math.PI * 2,
      tws: Math.random() * 0.02 + 0.004
    })
  }
  // 远景先画、近景后画，保证近处粒子叠在上层
  bgParticles.sort((a, b) => a.depth - b.depth)
}

const drawBg = () => {
  bgCtx.clearRect(0, 0, bgW, bgH)
  for (const p of bgParticles) {
    p.x += p.vx; p.y += p.vy
    if (p.x < 0) p.x = bgW; if (p.x > bgW) p.x = 0
    if (p.y < 0) p.y = bgH; if (p.y > bgH) p.y = 0
    p.tw += p.tws
    // 闪烁：近处粒子闪烁幅度更明显，远处更稳定
    const flick = (Math.sin(p.tw) + 1) / 2
    const a = p.baseAlpha * (0.55 + flick * 0.45 * p.depth + 0.2 * (1 - p.depth) * flick)
    bgCtx.beginPath()
    bgCtx.arc(p.x, p.y, p.r, 0, Math.PI * 2)
    bgCtx.fillStyle = `rgba(${p.color}, ${a})`
    // 只有近处粒子带明显辉光，远处几乎无光晕
    bgCtx.shadowBlur = p.depth * p.r * 4
    bgCtx.shadowColor = `rgba(${p.color}, ${a * p.depth})`
    bgCtx.fill()
  }
  bgCtx.shadowBlur = 0
  bgRaf = requestAnimationFrame(drawBg)
}

/* ============ 汇聚粒子（拼成购物车轮廓） ============ */
let asCtx = null, asRaf = null, targets = []
let asW = 0, asH = 0
let phaseStart = 0
const ASSEMBLE_MS = 2000  // 0-2s 汇聚

// 用离屏 canvas 画购物车线条 → 扫描像素得到目标点
const buildTargetsFromCart = () => {
  const off = document.createElement('canvas')
  const size = 240
  off.width = size
  off.height = size
  const octx = off.getContext('2d')

  octx.strokeStyle = '#fff'
  octx.lineWidth = 4
  octx.lineCap = 'round'
  octx.lineJoin = 'round'

  // 购物车路径（与 SVG 车形一致，坐标缩放到 240x240 画布，居中偏上）
  const ox = 8, oy = 30
  octx.beginPath()
  // 提手 + 车斗外框
  octx.moveTo(28 + ox, 40 + oy)
  octx.lineTo(52 + ox, 40 + oy)
  octx.lineTo(74 + ox, 132 + oy)
  octx.lineTo(176 + ox, 132 + oy)
  octx.lineTo(196 + ox, 66 + oy)
  octx.lineTo(86 + ox, 66 + oy)
  octx.stroke()
  // 竖格栅
  const vlines = [[96, 78, 102, 120], [118, 78, 120, 120], [140, 78, 138, 120], [162, 78, 156, 120]]
  vlines.forEach(([x1, y1, x2, y2]) => {
    octx.beginPath(); octx.moveTo(x1 + ox, y1 + oy); octx.lineTo(x2 + ox, y2 + oy); octx.stroke()
  })
  // 横格栅
  octx.beginPath(); octx.moveTo(84 + ox, 92 + oy); octx.lineTo(190 + ox, 92 + oy); octx.stroke()
  octx.beginPath(); octx.moveTo(88 + ox, 108 + oy); octx.lineTo(182 + ox, 108 + oy); octx.stroke()
  // 轮子
  octx.beginPath(); octx.arc(92 + ox, 156 + oy, 11, 0, Math.PI * 2); octx.stroke()
  octx.beginPath(); octx.arc(164 + ox, 156 + oy, 11, 0, Math.PI * 2); octx.stroke()

  // 扫描像素采样
  const img = octx.getImageData(0, 0, size, size).data
  const pts = []
  const isMobile = window.innerWidth < 768
  const step = isMobile ? 4 : 3  // 采样密度：移动端稀疏些
  for (let y = 0; y < size; y += step) {
    for (let x = 0; x < size; x += step) {
      const alpha = img[(y * size + x) * 4 + 3]
      if (alpha > 128) pts.push({ x, y })
    }
  }
  return { pts, size }
}

// 计算购物车在屏幕上的呈现位置与缩放（与 .hero 视觉对齐）
const layoutTargets = () => {
  const { pts, size } = buildTargetsFromCart()
  const isMobile = window.innerWidth < 768
  const displaySize = isMobile ? 260 : 340   // 购物车在屏上的像素尺寸
  const scale = displaySize / size
  // 中心轴：水平居中，垂直略偏上
  const cx = asW / 2 - displaySize / 2
  const cy = asH * (isMobile ? 0.30 : 0.32) - displaySize / 2

  targets = pts.map(p => {
    const tx = cx + p.x * scale
    const ty = cy + p.y * scale
    // 起始点：从屏幕四周随机飞入
    const edge = Math.floor(Math.random() * 4)
    let sx, sy
    if (edge === 0) { sx = Math.random() * asW; sy = -20 }
    else if (edge === 1) { sx = asW + 20; sy = Math.random() * asH }
    else if (edge === 2) { sx = Math.random() * asW; sy = asH + 20 }
    else { sx = -20; sy = Math.random() * asH }
    return {
      sx, sy, tx, ty,
      x: sx, y: sy,
      r: Math.random() * 1.4 + 0.8,
      delay: Math.random() * 0.35,          // 错峰汇聚
      wobble: Math.random() * Math.PI * 2    // 成型后微颤
    }
  })
}

const easeOutCubic = t => 1 - Math.pow(1 - t, 3)

const drawAssemble = (now) => {
  if (!phaseStart) phaseStart = now
  const elapsed = now - phaseStart
  asCtx.clearRect(0, 0, asW, asH)

  for (const p of targets) {
    // 每颗粒子的进度（含错峰延迟）
    let t = (elapsed / ASSEMBLE_MS - p.delay) / (1 - p.delay)
    t = Math.max(0, Math.min(1, t))
    const e = easeOutCubic(t)
    p.x = p.sx + (p.tx - p.sx) * e
    p.y = p.sy + (p.ty - p.sy) * e

    // 成型后轻微颤动，保持"能量感"
    let dx = 0, dy = 0
    if (t >= 1) {
      p.wobble += 0.05
      dx = Math.cos(p.wobble) * 0.6
      dy = Math.sin(p.wobble * 1.3) * 0.6
    }

    // 越接近目标越亮
    const alpha = 0.35 + e * 0.6
    const glow = 2 + e * 4
    asCtx.beginPath()
    asCtx.arc(p.x + dx, p.y + dy, p.r, 0, Math.PI * 2)
    // 汇聚中偏金，成型点亮后偏橙
    const col = cartLit.value ? '255, 130, 40' : '255, 190, 120'
    asCtx.fillStyle = `rgba(${col}, ${alpha})`
    asCtx.shadowBlur = glow
    asCtx.shadowColor = `rgba(255, 150, 60, ${alpha})`
    asCtx.fill()
  }
  asCtx.shadowBlur = 0

  // 点亮后粒子逐渐淡出，把主视觉交给发光 SVG（保留少量环绕感）
  if (cartLit.value) {
    asCtx.globalAlpha = Math.max(0, asCtx.globalAlpha - 0.006)
    if (asCtx.globalAlpha <= 0.15) {
      asCtx.globalAlpha = 0.15
    }
  }

  asRaf = requestAnimationFrame(drawAssemble)
}

/* ============ 尺寸 ============ */
const resize = () => {
  dpr = Math.min(window.devicePixelRatio || 1, 2)
  // 背景层
  bgW = window.innerWidth; bgH = window.innerHeight
  bgCanvas.value.width = bgW * dpr; bgCanvas.value.height = bgH * dpr
  bgCanvas.value.style.width = bgW + 'px'; bgCanvas.value.style.height = bgH + 'px'
  bgCtx.setTransform(dpr, 0, 0, dpr, 0, 0)
  buildBgParticles()
  // 汇聚层
  asW = window.innerWidth; asH = window.innerHeight
  assembleCanvas.value.width = asW * dpr; assembleCanvas.value.height = asH * dpr
  assembleCanvas.value.style.width = asW + 'px'; assembleCanvas.value.style.height = asH + 'px'
  asCtx.setTransform(dpr, 0, 0, dpr, 0, 0)
  layoutTargets()
}

/* ============ 进入商城 ============ */
// 如无真实首页，可改为 window.location.href = 'home.html'
const enterStore = () => {
  if (isLeaving.value) return
  isLeaving.value = true
  setTimeout(() => {
    router.push('/store')
  }, 950)
}

/* ============ 时间轴编排 ============ */
let timers = []
const scheduleTimeline = () => {
  // 2-3s：购物车被橙光点亮
  timers.push(setTimeout(() => { cartLit.value = true }, 2000))
  // 3-4s：能量环 + 轨道粒子
  timers.push(setTimeout(() => { ringsShow.value = true }, 3000))
  // 4s 后：文案依次浮现
  timers.push(setTimeout(() => { contentShow.value = true }, 4000))
}

onMounted(() => {
  bgCtx = bgCanvas.value.getContext('2d')
  asCtx = assembleCanvas.value.getContext('2d')
  resize()
  window.addEventListener('resize', resize)

  drawBg()
  asRaf = requestAnimationFrame(drawAssemble)
  scheduleTimeline()
})

onBeforeUnmount(() => {
  if (bgRaf) cancelAnimationFrame(bgRaf)
  if (asRaf) cancelAnimationFrame(asRaf)
  window.removeEventListener('resize', resize)
  timers.forEach(clearTimeout)
})
</script>

<style lang="scss" scoped>
$deep-1: #04070f;
$deep-2: #081328;
$deep-3: #0c1f42;
$orange: #ff7a18;
$orange-hot: #ff4400;
$gold: #ffd28c;

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
  // 背景由深色渐显
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

.bg-canvas { position: absolute; inset: 0; z-index: 1; }
.assemble-canvas { position: absolute; inset: 0; z-index: 4; pointer-events: none; }

/* 星云 */
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
@keyframes drift {
  0%, 100% { transform: translate(0,0) scale(1); }
  50% { transform: translate(-4%,4%) scale(1.08); }
}

/* 柔光粒子云：大面积低透明度模糊光团，缓慢流动 */
.nebula { position: absolute; inset: 0; z-index: 0; overflow: hidden; pointer-events: none; }
.cloud {
  position: absolute;
  border-radius: 50%;
  filter: blur(70px);
  mix-blend-mode: screen;
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
@keyframes cloudFlow {
  0%, 100% { transform: translate(0,0) scale(1); opacity: 0.28; }
  50% { transform: translate(6%,-5%) scale(1.15); opacity: 0.45; }
}

/* 细腻噪点：SVG 分形噪声，极低透明度叠加，消除色带、增加质感 */
.noise {
  position: absolute; inset: 0; z-index: 2; pointer-events: none;
  opacity: 0.05;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='160' height='160'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='2' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
  background-size: 160px 160px;
  mix-blend-mode: overlay;
}

/* 星点扩散：微小星点由中心淡出并放大，营造星尘扩散 */
.stars { position: absolute; inset: 0; z-index: 2; pointer-events: none; }
.star {
  position: absolute;
  width: 2px; height: 2px;
  border-radius: 50%;
  background: rgba(255,255,255,0.9);
  box-shadow: 0 0 6px rgba(180,210,255,0.8);
  opacity: 0;
  animation: starPulse ease-in-out infinite;
}
@keyframes starPulse {
  0%, 100% { opacity: 0; transform: scale(0.4); }
  50% { opacity: 0.9; transform: scale(1.3); }
}

/* 数字网格 */
.grid-overlay {
  position: absolute; inset: 0; z-index: 1; pointer-events: none;
  background-image:
    linear-gradient(rgba(120,170,255,0.045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(120,170,255,0.045) 1px, transparent 1px);
  background-size: 64px 64px;
  mask-image: radial-gradient(ellipse 68% 58% at 50% 40%, black 28%, transparent 74%);
  -webkit-mask-image: radial-gradient(ellipse 68% 58% at 50% 40%, black 28%, transparent 74%);
}

/* 购物车主视觉：中心轴，垂直略偏上 */
.hero {
  position: absolute;
  top: 32%;
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

.cart-svg {
  width: 340px;
  height: 283px;
  opacity: 0;
  transform: scale(0.94);
  transition: opacity 1s ease, transform 1s ease, filter 0.8s ease;
  filter: drop-shadow(0 0 10px rgba(255,122,24,0.35));
}

// 点亮后 SVG 淡入
.hero-lit .cart-svg {
  opacity: 1;
  transform: scale(1);
  animation: cartFloat 5.5s ease-in-out 1s infinite;
}

@keyframes cartFloat {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-10px) scale(1); }
}

/* 能量环 / 轨道 */
.rings {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 1.2s ease;
}
.rings-show { opacity: 1; }

/* 玻璃能量核心：径向渐变 + 模糊光晕，半透明呼吸 */
.energy-core {
  position: absolute;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: radial-gradient(circle at 50% 45%,
    rgba(255,190,120,0.28) 0%,
    rgba(255,122,24,0.16) 32%,
    rgba(120,160,255,0.08) 58%,
    transparent 72%);
  filter: blur(6px);
  animation: coreBreathe 4.5s ease-in-out infinite;
}
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
    background: radial-gradient(circle,
      transparent 62%,
      rgba(255,180,120,0.06) 74%,
      rgba(255,255,255,0.10) 82%,
      transparent 90%);
    box-shadow:
      0 0 34px rgba(255,122,24,0.28),
      inset 0 0 26px rgba(255,190,130,0.22),
      inset 0 2px 14px rgba(255,255,255,0.18);
    backdrop-filter: blur(2px);
    animation: glassSpin 16s linear infinite, glassGlow 4.5s ease-in-out infinite;
  }

  &.ring-1 {
    width: 300px; height: 300px;
    border-top-color: rgba(255,122,24,0.85);
    border-right-color: rgba(255,122,24,0.25);
    box-shadow: 0 0 16px rgba(255,122,24,0.35), inset 0 0 12px rgba(255,140,50,0.12);
    animation: spin 7s linear infinite;
  }
  &.ring-2 {
    width: 350px; height: 350px;
    border-left-color: rgba(120,180,255,0.6);
    border-bottom-color: rgba(120,180,255,0.18);
    box-shadow: 0 0 16px rgba(90,150,255,0.28), inset 0 0 12px rgba(120,180,255,0.1);
    animation: spin 11s linear infinite reverse;
  }
  &.ring-3 {
    width: 260px; height: 260px;
    border-bottom-color: rgba(255,205,140,0.7);
    border-left-color: rgba(255,205,140,0.2);
    box-shadow: 0 0 14px rgba(255,205,140,0.3);
    animation: spin 9s linear infinite;
  }
}
@keyframes spin { to { transform: rotate(360deg); } }
@keyframes glassSpin { to { transform: rotate(-360deg); } }
@keyframes glassGlow {
  0%, 100% { box-shadow: 0 0 30px rgba(255,122,24,0.22), inset 0 0 24px rgba(255,190,130,0.18), inset 0 2px 14px rgba(255,255,255,0.16); }
  50% { box-shadow: 0 0 48px rgba(255,122,24,0.42), inset 0 0 32px rgba(255,190,130,0.3), inset 0 2px 16px rgba(255,255,255,0.24); }
}

/* 轨道粒子（绕车公转） */
.orbit-dot {
  position: absolute;
  width: 8px; height: 8px; border-radius: 50%;
  background: radial-gradient(circle, #ffd9a0, #ff7a18);
  box-shadow: 0 0 12px rgba(255,140,50,0.9);
}
.dot-a { animation: orbitA 7s linear infinite; }
.dot-b { animation: orbitB 11s linear infinite; }
.dot-c { width: 6px; height: 6px; animation: orbitC 9s linear infinite; }
@keyframes orbitA { from { transform: rotate(0) translateX(150px) rotate(0); } to { transform: rotate(360deg) translateX(150px) rotate(-360deg); } }
@keyframes orbitB { from { transform: rotate(120deg) translateX(175px) rotate(-120deg); } to { transform: rotate(480deg) translateX(175px) rotate(-480deg); } }
@keyframes orbitC { from { transform: rotate(240deg) translateX(130px) rotate(-240deg); } to { transform: rotate(600deg) translateX(130px) rotate(-600deg); } }

/* 悬停按钮时，购物车与光环同步增强 */
.landing:has(.enter-btn:hover) .cart-svg {
  filter: drop-shadow(0 0 22px rgba(255,122,24,0.85));
}
.landing:has(.enter-btn:hover) .ring-1 { border-top-color: rgba(255,122,24,1); }

/* 文字内容 */
.content {
  position: absolute;
  top: 58%;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 720px;
  padding: 0 24px;
  text-align: center;
  z-index: 6;
}

.eyebrow, .title, .tagline, .desc, .tags, .enter-block {
  opacity: 0;
  transform: translateY(24px);
  transition: opacity 0.9s ease, transform 0.9s cubic-bezier(0.22,1,0.36,1);
}
// 依次浮现
.content-show {
  .eyebrow { opacity: 1; transform: translateY(0); transition-delay: 0s; }
  .title { opacity: 1; transform: translateY(0); transition-delay: 0.15s; }
  .tagline { opacity: 1; transform: translateY(0); transition-delay: 0.35s; }
  .desc { opacity: 1; transform: translateY(0); transition-delay: 0.55s; }
  .tags { opacity: 1; transform: translateY(0); transition-delay: 0.75s; }
  .enter-block { opacity: 1; transform: translateY(0); transition-delay: 0.95s; }
  .tag { opacity: 1; transform: translateY(0); }
}

.eyebrow {
  font-size: 13px; letter-spacing: 6px; font-weight: 500;
  color: rgba(255,200,150,0.72); margin-bottom: 14px;
}
.title {
  font-size: 50px; font-weight: 800; letter-spacing: 4px; margin-bottom: 16px;
  background: linear-gradient(120deg, #fff 20%, $gold 55%, $orange 100%);
  -webkit-background-clip: text; background-clip: text; -webkit-text-fill-color: transparent;
  text-shadow: 0 4px 30px rgba(255,122,24,0.25);
}
.tagline {
  font-size: 20px; font-weight: 500; letter-spacing: 1px;
  color: rgba(255,255,255,0.92); margin-bottom: 16px;
}
.desc {
  font-size: 15px; line-height: 2; color: rgba(200,215,245,0.66);
  max-width: 540px; margin: 0 auto 32px;
}

.tags { display: flex; flex-wrap: wrap; justify-content: center; gap: 14px; margin-bottom: 42px; }
.tag {
  padding: 9px 22px; font-size: 14px; color: rgba(255,255,255,0.88);
  border-radius: 40px; background: rgba(255,255,255,0.06);
  border: 1px solid rgba(255,180,120,0.28); backdrop-filter: blur(8px);
  opacity: 0; transform: translateY(14px);
  transition: opacity 0.6s ease, transform 0.6s ease;
}

.enter-block { display: flex; flex-direction: column; align-items: center; }
.enter-btn {
  position: relative; display: inline-flex; align-items: center; gap: 12px;
  padding: 16px 54px; font-size: 17px; font-weight: 600; letter-spacing: 2px;
  color: #fff; border: none; border-radius: 50px; cursor: pointer; overflow: hidden;
  background: linear-gradient(120deg, $orange-hot, $orange 60%, #ff9a4d);
  box-shadow: 0 8px 30px rgba(255,100,20,0.45);
  animation: btnBreathe 3s ease-in-out infinite;
  transition: transform 0.35s cubic-bezier(0.22,1,0.36,1), box-shadow 0.35s ease;

  .btn-arrow { transition: transform 0.35s ease; }
  .btn-shine {
    position: absolute; top: 0; left: -120%; width: 80%; height: 100%;
    background: linear-gradient(100deg, transparent, rgba(255,255,255,0.5), transparent);
    transform: skewX(-20deg); transition: left 0.7s ease;
  }
  &:hover {
    transform: translateY(-4px) scale(1.03);
    box-shadow: 0 16px 46px rgba(255,100,20,0.65), 0 0 60px rgba(255,122,24,0.5);
    .btn-arrow { transform: translateX(6px); }
    .btn-shine { left: 120%; }
  }
  &:active { transform: translateY(-1px) scale(0.99); }
}
@keyframes btnBreathe {
  0%, 100% { box-shadow: 0 8px 30px rgba(255,100,20,0.4); }
  50% { box-shadow: 0 8px 40px rgba(255,122,24,0.75), 0 0 40px rgba(255,122,24,0.4); }
}

.enter-hint {
  margin-top: 20px; font-size: 13px; color: rgba(200,215,245,0.5);
  animation: hintPulse 2.6s ease-in-out infinite;
}
@keyframes hintPulse { 0%,100% { opacity: 0.5; } 50% { opacity: 0.85; } }

.footer {
  position: absolute; bottom: 24px; left: 0; right: 0; text-align: center; z-index: 6;
  font-size: 12px; letter-spacing: 1px; color: rgba(180,200,235,0.4);
}

/* 点击光波 */
.burst {
  position: absolute; top: 32%; left: 50%;
  width: 120px; height: 120px; border-radius: 50%;
  transform: translate(-50%, -50%); z-index: 7;
  background: radial-gradient(circle, rgba(255,122,24,0.65), transparent 70%);
  animation: burstOut 0.95s ease-out forwards;
}
@keyframes burstOut {
  0% { width: 120px; height: 120px; opacity: 0.9; }
  100% { width: 240vmax; height: 240vmax; opacity: 0; }
}

/* 响应式 */
@media (max-width: 768px) {
  .hero { top: 30%; width: 260px; height: 260px; }
  .cart-svg { width: 260px; height: 216px; }
  .energy-core { width: 230px; height: 230px; }
  .ring-glass { width: 248px; height: 248px; }
  .ring-1 { width: 230px; height: 230px; }
  .ring-2 { width: 270px; height: 270px; }
  .ring-3 { width: 200px; height: 200px; }
  @keyframes orbitA { from { transform: rotate(0) translateX(115px) rotate(0); } to { transform: rotate(360deg) translateX(115px) rotate(-360deg); } }
  @keyframes orbitB { from { transform: rotate(120deg) translateX(135px) rotate(-120deg); } to { transform: rotate(480deg) translateX(135px) rotate(-480deg); } }
  @keyframes orbitC { from { transform: rotate(240deg) translateX(100px) rotate(-240deg); } to { transform: rotate(600deg) translateX(100px) rotate(-600deg); } }
  .content { top: 55%; }
  .title { font-size: 34px; letter-spacing: 2px; }
  .tagline { font-size: 17px; }
  .desc { font-size: 13.5px; padding: 0 6px; }
  .eyebrow { font-size: 11px; letter-spacing: 4px; }
  .enter-btn { padding: 14px 46px; font-size: 16px; }
}

@media (max-width: 380px) {
  .title { font-size: 28px; }
  .tags { gap: 10px; }
  .tag { padding: 8px 16px; font-size: 13px; }
}

@media (prefers-reduced-motion: reduce) {
  .cart-svg, .ring, .energy-core, .orbit-dot, .enter-btn, .aurora-blob, .cloud, .star, .enter-hint {
    animation: none !important;
  }
}
</style>
