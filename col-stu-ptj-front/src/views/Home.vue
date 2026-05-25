<template>
  <div class="dash">
    <!-- 顶栏：欢迎 + 身份 + 主操作 -->
    <section class="dash-hero">
      <div class="dash-hero-bg" aria-hidden="true" />
      <div class="dash-hero-inner">
        <div class="dash-hero-left">
          <p class="dash-kicker">{{ greeting }}，{{ welcomeName || '用户' }}</p>
          <h1 class="dash-title">
            工作台
            <span class="dash-title-dot" />
          </h1>
          <p class="dash-meta">
            <span class="dash-date">{{ currentDate }}</span>
            <span class="role-chip" :class="'role-chip--' + roleKey">{{ roleLabel }}</span>
          </p>
          <div class="dash-hero-actions">
            <el-button type="primary" size="large" round class="dash-cta" @click="router.push(primaryCta.path)">
              <el-icon class="dash-cta-icon"><component :is="primaryCta.icon" /></el-icon>
              {{ primaryCta.text }}
            </el-button>
            <el-button size="large" round plain @click="router.push('/notifications')">
              <el-icon><Bell /></el-icon>
              消息中心
            </el-button>
          </div>
        </div>
        <div class="dash-hero-right">
          <div class="avatar-ring">
            <el-avatar :size="96" :src="avatarSrc" class="dash-avatar" />
          </div>
        </div>
      </div>
    </section>

    <div class="dash-body">
      <!-- 数据概览 -->
      <div class="stat-row">
        <div class="stat-card">
          <span class="stat-card-label">{{ homeSummary.labelA }}</span>
          <span class="stat-card-value">{{ homeSummary.metricA }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-card-label">{{ homeSummary.labelB }}</span>
          <span class="stat-card-value">{{ homeSummary.metricB }}</span>
        </div>
        <div class="stat-card stat-card--accent">
          <span class="stat-card-label">认证状态</span>
          <span class="stat-card-value stat-card-value--sm">{{ authStatusLabel }}</span>
        </div>
      </div>

      <!-- 轮播 -->
      <div v-if="bannerUrls.length" class="banner-shell">
        <div class="banner-label">
          <el-icon><Picture /></el-icon>
          平台推荐
        </div>
        <el-carousel height="200px" class="banner-carousel" indicator-position="outside" :interval="5000">
          <el-carousel-item v-for="(url, i) in bannerUrls" :key="i">
            <div class="banner-slide" :style="{ backgroundImage: `url(${url})` }" />
          </el-carousel-item>
        </el-carousel>
      </div>

      <!-- 公告 -->
      <div v-if="publicCfg.announcement" class="notice-strip">
        <el-icon class="notice-icon"><Promotion /></el-icon>
        <span class="notice-text">{{ publicCfg.announcement }}</span>
      </div>

      <!-- 信用规则 -->
      <el-collapse v-if="publicCfg.creditRules" class="rules-collapse" accordion>
        <el-collapse-item name="rules">
          <template #title>
            <span class="rules-title">
              <el-icon><Document /></el-icon>
              平台信用规则说明
            </span>
          </template>
          <pre class="rules-pre">{{ publicCfg.creditRules }}</pre>
        </el-collapse-item>
      </el-collapse>

      <!-- 常用功能 -->
      <section class="section">
        <header class="section-head">
          <h2 class="section-title">常用功能</h2>
          <p class="section-sub">点击进入对应模块，按角色展示可用入口</p>
        </header>
        <div class="feature-grid">
          <article
            v-for="feature in features"
            :key="feature.id"
            class="feature-tile"
            @click="handleFeatureClick(feature)"
          >
            <div class="feature-tile-icon">
              <div class="feature-tile-icon-inner" :style="{ background: feature.color }">
                <el-icon :size="22" color="#fff">
                  <component :is="feature.icon" />
                </el-icon>
              </div>
            </div>
            <div class="feature-tile-body">
              <h3>{{ feature.title }}</h3>
              <p>{{ feature.description }}</p>
            </div>
            <el-icon class="feature-tile-arrow"><ArrowRight /></el-icon>
          </article>
        </div>
      </section>

      <!-- 快捷操作 -->
      <section class="section section--tight">
        <header class="section-head">
          <h2 class="section-title">快捷操作</h2>
          <p class="section-sub">一键完成高频任务</p>
        </header>
        <div class="quick-bar">
          <el-button
            v-for="action in quickActions"
            :key="action.id"
            :type="action.type"
            :icon="action.icon"
            round
            class="quick-bar-btn"
            @click="handleQuickAction(action)"
          >
            {{ action.text }}
          </el-button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowRight,
  Bell,
  ChatDotSquare,
  Collection,
  DataAnalysis,
  Document,
  Medal,
  OfficeBuilding,
  Picture,
  Promotion,
  Star,
  Tickets,
  User,
  Wallet
} from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'
import { getPublicConfig } from '../api/public'
import { getHomeSummary } from '../api/auth'
import { API_BASE_URL } from '../utils/request'

const router = useRouter()
const userStore = useUserStore()

const publicCfg = ref({
  announcement: '',
  bannerImagesJson: '',
  creditRules: ''
})
const toAbsoluteMediaUrl = (u) => {
  if (!u || typeof u !== 'string') return ''
  const s = u.trim()
  if (!s) return ''
  if (s.startsWith('http://') || s.startsWith('https://')) return s
  const base = API_BASE_URL.replace(/\/$/, '')
  return s.startsWith('/') ? `${base}${s}` : `${base}/${s}`
}

const bannerUrls = computed(() => {
  const raw = publicCfg.value.bannerImagesJson
  if (!raw) return []
  try {
    const v = JSON.parse(raw)
    if (!Array.isArray(v)) return []
    return v
      .filter((u) => typeof u === 'string' && u.trim())
      .map((u) => toAbsoluteMediaUrl(u.trim()))
      .filter(Boolean)
  } catch {
    return []
  }
})

const defaultAvatar = '/avatars/default.png'

const userInfo = ref({
  username: '',
  displayName: '',
  role: '',
  authStatus: '',
  avatar: ''
})

const welcomeName = computed(() => userInfo.value.displayName || userInfo.value.username || '')

const homeSummary = ref({
  metricA: 0,
  labelA: '—',
  metricB: 0,
  labelB: '—'
})

const avatarSrc = computed(() => {
  const path = userInfo.value.avatar
  if (!path) return defaultAvatar
  if (path.startsWith('http')) return path
  return API_BASE_URL + (path.startsWith('/') ? path : '/' + path)
})

const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

const rawRole = computed(() => {
  const r = userInfo.value.role
  if (typeof r === 'string') return r
  if (r && typeof r === 'object') return r.name || r.code || ''
  return ''
})

const roleKey = computed(() => {
  const r = rawRole.value
  if (r === 'STUDENT') return 'student'
  if (r === 'COMPANY') return 'company'
  if (r === 'ADMIN') return 'admin'
  return 'default'
})

const roleLabel = computed(() => {
  const r = rawRole.value
  if (r === 'STUDENT') return '学生'
  if (r === 'COMPANY') return '企业'
  if (r === 'ADMIN') return '管理员'
  return '用户'
})

const authLabelMap = {
  UNAUTH: '未认证',
  PENDING: '审核中',
  APPROVED: '已认证',
  REJECTED: '认证未通过'
}

const authStatusLabel = computed(() => {
  const s = userInfo.value.authStatus
  return authLabelMap[s] || s || '—'
})

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const primaryCta = computed(() => {
  const r = rawRole.value
  if (r === 'COMPANY') {
    return { text: '管理我的岗位', path: '/company/jobs', icon: OfficeBuilding }
  }
  if (r === 'ADMIN') {
    return { text: '进入管理后台', path: '/admin/dashboard', icon: DataAnalysis }
  }
  return { text: '浏览在招岗位', path: '/jobs', icon: Document }
})

const features = ref([])

const quickActions = ref([
  {
    id: 1,
    text: '浏览兼职',
    icon: Document,
    type: 'primary',
    action: () => router.push('/jobs')
  },
  {
    id: 2,
    text: '完善资料',
    icon: User,
    type: 'success',
    action: () => {
      const r = rawRole.value
      if (r === 'COMPANY') router.push('/company/profile')
      else if (r === 'ADMIN') router.push('/admin/profile')
      else router.push('/profile')
    }
  },
  {
    id: 3,
    text: '账号与认证',
    icon: Collection,
    type: 'warning',
    action: () => {
      const r = rawRole.value
      if (r === 'COMPANY') router.push('/company/profile')
      else if (r === 'ADMIN') router.push('/admin/users')
      else router.push('/profile')
    }
  },
  {
    id: 4,
    text: '联系客服',
    icon: ChatDotSquare,
    type: 'info',
    action: () => window.open('https://support.example.com', '_blank')
  }
])

const handleFeatureClick = (feature) => {
  if (feature.role && feature.role !== rawRole.value) {
    ElMessage.warning('您没有权限访问此功能')
    return
  }
  router.push(feature.path)
}

const handleQuickAction = (action) => {
  action.action()
}

const loadUserInfo = async () => {
  try {
    const response = await userStore.getUserInfo()
    if (response.success && response.data) {
      userInfo.value = response.data
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

const featuresSeed = () => [
  {
    id: 1,
    title: '查看兼职',
    description: '浏览最新兼职岗位信息',
    icon: Document,
    color: '#409EFF',
    path: '/jobs'
  },
  {
    id: 2,
    title: '发布兼职',
    description: '为企业用户发布新的岗位',
    icon: OfficeBuilding,
    color: '#67C23A',
    path: '/company/jobs',
    role: 'COMPANY'
  },
  {
    id: 3,
    title: '我的申请',
    description: '查看申请记录和进度',
    icon: Wallet,
    color: '#E6A23C',
    path: '/applications',
    role: 'STUDENT'
  },
  {
    id: 11,
    title: '我的简历',
    description: '在线维护简历，企业投递后可查看',
    icon: Tickets,
    color: '#409EFF',
    path: '/resume',
    role: 'STUDENT'
  },
  {
    id: 12,
    title: '消息通知',
    description: '查看投递、面试与状态变更提醒',
    icon: Bell,
    color: '#F56C6C',
    path: '/notifications'
  },
  {
    id: 4,
    title: '我的评价',
    description: '查看发出和收到的评价记录',
    icon: Star,
    color: '#F56C6C',
    path: '/reviews',
    role: 'STUDENT'
  },
  {
    id: 8,
    title: '评价中心',
    description: '查看企业评价记录',
    icon: Star,
    color: '#FF8C00',
    path: '/company/reviews',
    role: 'COMPANY'
  },
  {
    id: 5,
    title: '候选人收件箱',
    description: '查看学生投递和处理进度',
    icon: ChatDotSquare,
    color: '#909399',
    path: '/company/applicants',
    role: 'COMPANY'
  },
  {
    id: 7,
    title: '我的收藏',
    description: '查看已收藏岗位',
    icon: Collection,
    color: '#1ABC9C',
    path: '/favorites',
    role: 'STUDENT'
  },
  {
    id: 9,
    title: '信用中心',
    description: '信用档案、调整记录与争议',
    icon: Medal,
    color: '#2ECC71',
    path: '/credit',
    role: 'STUDENT'
  },
  {
    id: 10,
    title: '信用中心',
    description: '企业信用档案与争议处理',
    icon: Medal,
    color: '#16A085',
    path: '/company/credit',
    role: 'COMPANY'
  },
  {
    id: 6,
    title: '数据统计',
    description: '查看平台数据和分析',
    icon: DataAnalysis,
    color: '#8E44AD',
    path: '/admin',
    role: 'ADMIN'
  }
]

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }

  await loadUserInfo()

  try {
    const sum = await getHomeSummary()
    if (sum.success && sum.data) {
      homeSummary.value = {
        metricA: sum.data.metricA ?? 0,
        labelA: sum.data.labelA || '—',
        metricB: sum.data.metricB ?? 0,
        labelB: sum.data.labelB || '—'
      }
    }
  } catch (e) {
    console.warn('加载首页统计失败', e)
  }

  try {
    const res = await getPublicConfig()
    if (res.success && res.data) {
      publicCfg.value = {
        announcement: res.data.announcement || '',
        bannerImagesJson: res.data.bannerImagesJson || '',
        creditRules: res.data.creditRules || ''
      }
    }
  } catch (e) {
    console.warn('加载公开配置失败', e)
  }

  const all = featuresSeed()
  const r = rawRole.value
  if (r) {
    features.value = all.filter((f) => !f.role || f.role === r)
  } else {
    features.value = all
  }
})
</script>

<style scoped lang="scss">
.dash {
  margin: -20px -20px 0;
  min-height: calc(100vh - 96px);
  background: linear-gradient(180deg, #f0f4fb 0%, #f5f7fa 45%, #fafbfc 100%);
}

.dash-hero {
  position: relative;
  padding: 28px 24px 32px;
  overflow: hidden;
}

.dash-hero-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(900px 380px at 8% -20%, rgba(99, 102, 241, 0.2), transparent 55%),
    radial-gradient(700px 300px at 92% 10%, rgba(14, 165, 233, 0.16), transparent 50%),
    linear-gradient(125deg, #0f172a 0%, #1e293b 38%, #334155 100%);
  &::after {
    content: '';
    position: absolute;
    inset: 0;
    opacity: 0.4;
    background-image: url("data:image/svg+xml,%3Csvg width='64' height='64' viewBox='0 0 64 64' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M0 32h64M32 0v64' stroke='%23ffffff' stroke-opacity='0.05' stroke-width='1'/%3E%3C/svg%3E");
  }
}

.dash-hero-inner {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
}

.dash-hero-left {
  flex: 1;
  min-width: 0;
}

.dash-kicker {
  margin: 0 0 8px;
  font-size: 15px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.88);
  letter-spacing: 0.02em;
}

.dash-title {
  margin: 0 0 12px;
  font-size: clamp(1.75rem, 4vw, 2.25rem);
  font-weight: 800;
  color: #fff;
  letter-spacing: -0.03em;
  display: flex;
  align-items: center;
  gap: 10px;
}

.dash-title-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, #38bdf8, #4ade80);
  box-shadow: 0 0 16px rgba(56, 189, 248, 0.7);
}

.dash-meta {
  margin: 0 0 22px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.dash-date {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.65);
}

.role-chip {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.04em;
  border: 1px solid rgba(255, 255, 255, 0.25);
  background: rgba(255, 255, 255, 0.1);
  color: #e2e8f0;
}

.role-chip--student {
  background: rgba(59, 130, 246, 0.25);
  border-color: rgba(96, 165, 250, 0.45);
  color: #bfdbfe;
}

.role-chip--company {
  background: rgba(34, 197, 94, 0.22);
  border-color: rgba(74, 222, 128, 0.4);
  color: #bbf7d0;
}

.role-chip--admin {
  background: rgba(168, 85, 247, 0.22);
  border-color: rgba(192, 132, 252, 0.45);
  color: #e9d5ff;
}

.dash-hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.dash-cta {
  padding: 0 28px;
  font-weight: 600;
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.45);
}

.dash-cta-icon {
  margin-right: 6px;
  vertical-align: text-bottom;
}

.dash-hero-right {
  flex-shrink: 0;
}

.avatar-ring {
  padding: 6px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(56, 189, 248, 0.9), rgba(74, 222, 128, 0.85));
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.25);
}

.dash-avatar {
  border: 3px solid rgba(15, 23, 42, 0.35);
}

/* body */
.dash-body {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 40px;
  position: relative;
  top: -20px;
}

.stat-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 14px;
  padding: 18px 20px;
  border: 1px solid #e8ecf2;
  box-shadow: 0 4px 20px rgba(15, 23, 42, 0.05);
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat-card-label {
  font-size: 12px;
  font-weight: 600;
  color: #94a3b8;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.stat-card-value {
  font-size: 26px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.03em;
  font-variant-numeric: tabular-nums;
}

.stat-card-value--sm {
  font-size: 18px;
  font-weight: 700;
  color: #2563eb;
}

.stat-card--accent {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
}

.banner-shell {
  background: #fff;
  border-radius: 16px;
  padding: 16px 16px 8px;
  margin-bottom: 16px;
  border: 1px solid #e8ecf2;
  box-shadow: 0 4px 24px rgba(15, 23, 42, 0.06);
}

.banner-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 700;
  color: #64748b;
  margin-bottom: 12px;
  .el-icon {
    color: #409eff;
  }
}

.banner-carousel {
  border-radius: 12px;
  overflow: hidden;
}

.banner-slide {
  height: 200px;
  border-radius: 12px;
  background-size: cover;
  background-position: center;
  background-color: #e2e8f0;
}

.notice-strip {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 18px;
  margin-bottom: 16px;
  border-radius: 12px;
  background: linear-gradient(90deg, rgba(59, 130, 246, 0.08), rgba(14, 165, 233, 0.06));
  border: 1px solid rgba(59, 130, 246, 0.18);
}

.notice-icon {
  flex-shrink: 0;
  margin-top: 2px;
  color: #3b82f6;
  font-size: 20px;
}

.notice-text {
  font-size: 14px;
  line-height: 1.6;
  color: #334155;
}

.rules-collapse {
  margin-bottom: 22px;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e8ecf2;
  :deep(.el-collapse-item__header) {
    font-weight: 600;
    padding: 4px 16px;
  }
  :deep(.el-collapse-item__wrap) {
    border: none;
  }
}

.rules-title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #334155;
}

.rules-pre {
  margin: 0;
  padding: 0 8px 12px;
  white-space: pre-wrap;
  font-family: ui-sans-serif, system-ui, sans-serif;
  font-size: 13px;
  color: #64748b;
  line-height: 1.65;
}

.section {
  margin-bottom: 28px;
}

.section--tight {
  margin-bottom: 0;
}

.section-head {
  margin-bottom: 16px;
}

.section-title {
  margin: 0 0 6px;
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.section-sub {
  margin: 0;
  font-size: 13px;
  color: #94a3b8;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.feature-tile {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 18px 40px 18px 18px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e8ecf2;
  cursor: pointer;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    border-color 0.2s ease;
  &:hover {
    transform: translateY(-3px);
    border-color: #c7d7f0;
    box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
    .feature-tile-arrow {
      opacity: 1;
      transform: translateX(0);
    }
  }
}

.feature-tile-icon-inner {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.feature-tile-body {
  flex: 1;
  min-width: 0;
  h3 {
    margin: 0 0 6px;
    font-size: 16px;
    font-weight: 700;
    color: #1e293b;
  }
  p {
    margin: 0;
    font-size: 13px;
    color: #64748b;
    line-height: 1.5;
  }
}

.feature-tile-arrow {
  position: absolute;
  right: 14px;
  top: 50%;
  margin-top: -10px;
  font-size: 18px;
  color: #94a3b8;
  opacity: 0.35;
  transform: translateX(-4px);
  transition: opacity 0.2s, transform 0.2s;
}

.quick-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 18px 20px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e8ecf2;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.04);
}

.quick-bar-btn {
  font-weight: 600;
}

@media (max-width: 900px) {
  .stat-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .dash {
    margin: -16px -16px 0;
  }
  .dash-hero {
    padding: 22px 16px 26px;
  }
  .dash-body {
    padding: 0 16px 32px;
    top: -14px;
  }
  .dash-hero-inner {
    flex-direction: column-reverse;
    align-items: flex-start;
  }
  .dash-hero-right {
    align-self: center;
  }
  .feature-grid {
    grid-template-columns: 1fr;
  }
}
</style>
