<template>
  <div class="job-detail">
    <nav class="detail-nav">
      <el-button text type="primary" class="back-btn" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回岗位列表
      </el-button>
      <span class="nav-crumb">岗位浏览 <span class="crumb-sep">/</span> 详情</span>
    </nav>

    <div v-if="loading" class="detail-skeleton" v-loading="loading">
      <el-skeleton animated :rows="8" />
    </div>

    <template v-else-if="job">
      <el-alert
        v-if="!isStudent"
        type="info"
        show-icon
        :closable="false"
        class="role-alert"
        title="浏览模式"
        description="企业/管理员账号仅可查看岗位信息，无法投递或收藏。"
      />

      <!-- 标题区 -->
      <header class="detail-hero">
        <div class="hero-accent" aria-hidden="true" />
        <div class="hero-inner">
          <div class="hero-main">
            <h1 class="hero-title">{{ job.title }}</h1>
            <div class="hero-tags">
              <span v-if="job.jobType" class="pill pill--type">
                <el-icon><Briefcase /></el-icon>
                {{ job.jobType }}
              </span>
              <span v-if="job.location" class="pill pill--loc">
                <el-icon><Location /></el-icon>
                {{ job.location }}
              </span>
              <span v-if="expireText" class="pill pill--time">
                <el-icon><Clock /></el-icon>
                {{ expireText }}
              </span>
            </div>
          </div>
          <div class="hero-side">
            <div class="salary-box">
              <span class="salary-label">薪资</span>
              <span class="salary-value">{{ job.salaryText || '面议' }}</span>
              <span v-if="salaryRangeText" class="salary-range">{{ salaryRangeText }}</span>
            </div>
            <div v-if="isStudent" class="hero-actions">
              <el-button type="primary" size="large" round class="btn-apply" @click="apply">
                <el-icon><Promotion /></el-icon>
                立即投递
              </el-button>
              <el-button size="large" round plain @click="fav">
                <el-icon><Star /></el-icon>
                收藏岗位
              </el-button>
            </div>
          </div>
        </div>
      </header>

      <div class="detail-layout">
        <main class="detail-main">
          <section class="panel">
            <h2 class="panel-title">
              <el-icon><Document /></el-icon>
              岗位描述
            </h2>
            <div class="panel-body prose">
              <p v-if="!job.description" class="muted">暂无详细描述。</p>
              <p v-else class="desc-text">{{ job.description }}</p>
            </div>
          </section>
        </main>

        <aside class="detail-aside">
          <section class="panel panel--sticky">
            <h2 class="panel-title">
              <el-icon><Phone /></el-icon>
              联系与沟通
            </h2>
            <div class="panel-body contact-stack">
              <div v-if="job.contactPhone" class="contact-card">
                <div class="contact-card-icon phone">
                  <el-icon><Phone /></el-icon>
                </div>
                <div>
                  <div class="contact-label">联系电话</div>
                  <a :href="'tel:' + job.contactPhone" class="contact-value link">{{ job.contactPhone }}</a>
                </div>
              </div>
              <div v-if="job.contactWechat" class="contact-card">
                <div class="contact-card-icon wechat">
                  <el-icon><ChatDotRound /></el-icon>
                </div>
                <div class="contact-minw">
                  <div class="contact-label">微信 / 沟通说明</div>
                  <pre class="contact-pre">{{ job.contactWechat }}</pre>
                </div>
              </div>
              <p v-if="!job.contactPhone && !job.contactWechat" class="muted small">发布方未填写联系方式，投递后可在「我的投递」中与企业沟通。</p>
            </div>
          </section>

          <section v-if="isStudent" class="panel panel--cta-mobile">
            <el-button type="primary" size="large" round block class="btn-apply" @click="apply">
              立即投递
            </el-button>
            <el-button size="large" round block plain @click="fav">收藏岗位</el-button>
          </section>
        </aside>
      </div>
    </template>

    <el-empty v-else class="detail-empty" description="岗位不存在或已下架">
      <el-button type="primary" round @click="goBack">返回列表</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Briefcase,
  ChatDotRound,
  Clock,
  Document,
  Location,
  Phone,
  Promotion,
  Star
} from '@element-plus/icons-vue'
import { getStudentJobDetail, applyJob, favoriteJob } from '../api/student'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isStudent = computed(() => userStore.isStudent)

const job = ref(null)
const loading = ref(false)

const goBack = () => router.push('/jobs')

const expireText = computed(() => {
  const j = job.value
  if (!j?.expireAt) return ''
  try {
    const d = new Date(j.expireAt)
    if (Number.isNaN(d.getTime())) return ''
    return `截止 ${d.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })}`
  } catch {
    return ''
  }
})

const salaryRangeText = computed(() => {
  const j = job.value
  if (j?.salaryMin != null && j?.salaryMax != null && j.salaryMin !== j.salaryMax) {
    return `约 ${j.salaryMin}～${j.salaryMax} 元（参考）`
  }
  if (j?.salaryMin != null && j?.salaryMax != null) {
    return `约 ${j.salaryMin} 元（参考）`
  }
  return ''
})

const load = async () => {
  loading.value = true
  try {
    const res = await getStudentJobDetail(route.params.jobId)
    if (res.success && res.data) job.value = res.data
    else job.value = null
  } catch {
    job.value = null
  } finally {
    loading.value = false
  }
}

const apply = async () => {
  if (!job.value) return
  try {
    const { value } = await ElMessageBox.prompt('请输入投递意向', `投递「${job.value.title}」`, {
      inputPattern: /.+/,
      inputErrorMessage: '不能为空',
      confirmButtonText: '投递',
      cancelButtonText: '取消'
    })
    await applyJob({ jobId: job.value.id, intention: value })
    ElMessage.success('投递成功')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '失败')
  }
}

const fav = async () => {
  if (!job.value) return
  try {
    await favoriteJob(job.value.id)
    ElMessage.success('已收藏')
  } catch (e) {
    ElMessage.error(e.message || '失败')
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.job-detail {
  max-width: 1100px;
  margin: 0 auto;
}

.detail-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 18px;
}

.back-btn {
  font-weight: 600;
  padding: 4px 0;
}

.nav-crumb {
  font-size: 13px;
  color: #94a3b8;
}

.crumb-sep {
  margin: 0 6px;
  opacity: 0.6;
}

.detail-skeleton {
  padding: 24px;
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e8ecf2;
}

.role-alert {
  margin-bottom: 16px;
  border-radius: 12px;
}

/* Hero */
.detail-hero {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 22px;
  background: linear-gradient(135deg, #0f172a 0%, #1e3a5f 55%, #1e40af 100%);
  box-shadow: 0 16px 48px rgba(15, 23, 42, 0.18);
}

.hero-accent {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  height: 4px;
  background: linear-gradient(90deg, #38bdf8, #4ade80, #fbbf24);
}

.hero-inner {
  position: relative;
  z-index: 1;
  padding: 28px 24px 24px;
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  align-items: flex-start;
  justify-content: space-between;
}

.hero-main {
  flex: 1;
  min-width: 0;
}

.hero-title {
  margin: 0 0 14px;
  font-size: clamp(1.35rem, 3.5vw, 1.85rem);
  font-weight: 800;
  color: #fff;
  letter-spacing: -0.02em;
  line-height: 1.3;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  border: 1px solid rgba(255, 255, 255, 0.2);
  .el-icon {
    font-size: 15px;
  }
}

.pill--type {
  background: rgba(59, 130, 246, 0.25);
  color: #bfdbfe;
}

.pill--loc {
  background: rgba(255, 255, 255, 0.1);
  color: #e2e8f0;
}

.pill--time {
  background: rgba(251, 191, 36, 0.15);
  color: #fde68a;
  border-color: rgba(251, 191, 36, 0.35);
}

.hero-side {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 16px;
  min-width: 220px;
}

.salary-box {
  padding: 16px 18px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(8px);
}

.salary-label {
  display: block;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: rgba(255, 255, 255, 0.55);
  text-transform: uppercase;
  margin-bottom: 4px;
}

.salary-value {
  display: block;
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -0.03em;
  background: linear-gradient(120deg, #fde68a, #fca5a5);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.salary-range {
  display: block;
  margin-top: 6px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.65);
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.btn-apply {
  font-weight: 700;
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.45);
  .el-icon {
    margin-right: 4px;
  }
}

/* Layout */
.detail-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 22px;
  align-items: start;
}

.detail-main {
  min-width: 0;
}

.detail-aside {
  min-width: 0;
}

.panel {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e8ecf2;
  box-shadow: 0 4px 20px rgba(15, 23, 42, 0.04);
  overflow: hidden;
  margin-bottom: 0;
}

.panel--sticky {
  position: sticky;
  top: 12px;
}

.panel-title {
  margin: 0;
  padding: 16px 18px;
  font-size: 15px;
  font-weight: 800;
  color: #0f172a;
  display: flex;
  align-items: center;
  gap: 8px;
  border-bottom: 1px solid #f1f5f9;
  background: linear-gradient(180deg, #fafbfc 0%, #fff 100%);
  .el-icon {
    color: #409eff;
  }
}

.panel-body {
  padding: 18px 20px 22px;
}

.prose .desc-text {
  margin: 0;
  white-space: pre-wrap;
  font-size: 15px;
  line-height: 1.75;
  color: #334155;
}

.muted {
  color: #94a3b8;
  margin: 0;
}

.muted.small {
  font-size: 13px;
  line-height: 1.6;
}

.contact-stack {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.contact-card {
  display: flex;
  gap: 14px;
  padding: 14px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #eef2f7;
}

.contact-card-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 20px;
  &.phone {
    background: rgba(59, 130, 246, 0.12);
    color: #2563eb;
  }
  &.wechat {
    background: rgba(34, 197, 94, 0.12);
    color: #16a34a;
  }
}

.contact-label {
  font-size: 12px;
  font-weight: 600;
  color: #94a3b8;
  margin-bottom: 4px;
}

.contact-value.link {
  font-size: 16px;
  font-weight: 700;
  color: #2563eb;
  text-decoration: none;
  &:hover {
    text-decoration: underline;
  }
}

.contact-minw {
  min-width: 0;
}

.contact-pre {
  margin: 0;
  white-space: pre-wrap;
  font-family: inherit;
  font-size: 14px;
  color: #334155;
  line-height: 1.55;
  word-break: break-word;
}

.panel--cta-mobile {
  margin-top: 16px;
  padding: 16px;
  display: none;
  flex-direction: column;
  gap: 10px;
}

.detail-empty {
  padding: 48px 16px;
  background: #fff;
  border-radius: 16px;
  border: 1px dashed #e2e8f0;
}

@media (max-width: 900px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }
  .panel--sticky {
    position: static;
  }
  .hero-actions {
    display: none;
  }
  .panel--cta-mobile {
    display: flex;
  }
}

@media (max-width: 600px) {
  .hero-inner {
    padding: 20px 16px 18px;
  }
  .hero-side {
    width: 100%;
  }
}
</style>
