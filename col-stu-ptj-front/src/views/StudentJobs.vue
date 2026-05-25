<template>
  <div class="jobs-page">
    <!-- 顶区：氛围背景 + 标题 -->
    <section class="hero">
      <div class="hero-bg" aria-hidden="true" />
      <div class="hero-inner">
        <div class="hero-copy">
          <span class="hero-badge">
            <el-icon><Sunny /></el-icon>
            实时在招
          </span>
          <h1 class="hero-title">发现适合你的<span class="hero-title-accent">兼职机会</span></h1>
          <p class="hero-sub">已上架岗位经过平台审核，支持关键词、类型与地点筛选。</p>
          <div v-if="jobs.total" class="hero-stat">
            <span class="hero-stat-value">{{ jobs.total }}</span>
            <span class="hero-stat-label">个岗位开放申请</span>
          </div>
        </div>
      </div>
    </section>

    <div class="jobs-body">
      <el-alert
        v-if="!isStudent"
        type="info"
        show-icon
        :closable="false"
        class="role-alert"
        title="当前为浏览模式"
        description="仅可查看在招岗位；投递与收藏需使用学生账号登录。"
      />

      <!-- 快捷筛选 -->
      <div class="quick-tags">
        <span class="quick-tags-label">试试</span>
        <button
          v-for="tag in quickTags"
          :key="tag"
          type="button"
          class="quick-tag"
          :class="{ active: q.jobType === tag }"
          @click="toggleQuickTag(tag)"
        >
          {{ tag }}
        </button>
        <button v-if="q.jobType" type="button" class="quick-tag quick-tag-clear" @click="clearQuickTag">清除类型</button>
      </div>

      <!-- 筛选 -->
      <div class="filter-shell">
        <div class="filter-head">
          <el-icon class="filter-head-icon"><Filter /></el-icon>
          <span>筛选条件</span>
        </div>
        <el-form :model="q" class="filter-form" @submit.prevent="onSearch">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="8">
              <el-form-item label="关键词">
                <el-input
                  v-model="q.keyword"
                  clearable
                  placeholder="搜标题或描述"
                  :prefix-icon="Search"
                  class="filter-input"
                  @keyup.enter="onSearch"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="5">
              <el-form-item label="类型">
                <el-input
                  v-model="q.jobType"
                  clearable
                  placeholder="如 家教、推广"
                  :prefix-icon="Briefcase"
                  class="filter-input"
                  @keyup.enter="onSearch"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="5">
              <el-form-item label="地点">
                <el-input
                  v-model="q.location"
                  clearable
                  placeholder="城市或区域"
                  :prefix-icon="Location"
                  class="filter-input"
                  @keyup.enter="onSearch"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6" class="filter-actions">
              <el-button type="primary" round :icon="Search" :loading="loading" @click="onSearch">查询</el-button>
              <el-button round :disabled="loading" @click="onReset">重置</el-button>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <!-- 列表 -->
      <div class="list-wrap">
        <template v-if="loading">
          <el-row :gutter="20" class="job-grid">
            <el-col v-for="s in 6" :key="s" :xs="24" :sm="12" :lg="8">
              <el-card class="job-card job-card--skeleton" shadow="never">
                <el-skeleton animated :rows="4" />
              </el-card>
            </el-col>
          </el-row>
        </template>

        <template v-else-if="!jobs.records.length">
          <el-empty class="list-empty" description="暂无符合条件的岗位">
            <template #description>
              <p class="empty-desc">试试调整关键词或点击上方快捷标签</p>
            </template>
            <el-button type="primary" round @click="onReset">重置全部条件</el-button>
          </el-empty>
        </template>

        <template v-else>
          <el-row :gutter="22" class="job-grid">
            <el-col v-for="row in jobs.records" :key="row.id" :xs="24" :sm="12" :lg="8">
              <article
                class="job-card"
                :style="cardAccentStyle(row)"
                @click="goDetail(row)"
              >
                <div class="job-card-glow" aria-hidden="true" />
                <div class="job-card-body">
                  <header class="job-card-head">
                    <h3 class="job-title">{{ row.title }}</h3>
                    <div class="job-meta-row">
                      <span v-if="row.jobType" class="meta-pill meta-pill--type">
                        <el-icon><Briefcase /></el-icon>
                        {{ row.jobType }}
                      </span>
                      <span v-if="row.location" class="meta-pill meta-pill--loc">
                        <el-icon><Location /></el-icon>
                        {{ row.location }}
                      </span>
                    </div>
                  </header>
                  <p class="job-preview">{{ previewText(row.description) }}</p>
                  <footer class="job-card-foot">
                    <div class="salary-wrap">
                      <span class="salary-label">薪资</span>
                      <span class="salary">{{ row.salaryText || '面议' }}</span>
                    </div>
                    <div class="actions" @click.stop>
                      <el-button text type="primary" class="linkish" @click="goDetail(row)">详情</el-button>
                      <template v-if="isStudent">
                        <el-button type="primary" round size="small" @click="apply(row)">投递</el-button>
                        <el-button round size="small" plain @click="fav(row)">收藏</el-button>
                      </template>
                    </div>
                  </footer>
                </div>
              </article>
            </el-col>
          </el-row>

          <div class="pager-wrap">
            <el-pagination
              v-model:current-page="q.current"
              v-model:page-size="q.size"
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="jobs.total"
              :page-sizes="[6, 12, 24]"
              @size-change="load"
              @current-change="load"
            />
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Briefcase, Filter, Location, Search, Sunny } from '@element-plus/icons-vue'
import { pageStudentJobs, applyJob, favoriteJob } from '../api/student'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()
const isStudent = computed(() => userStore.isStudent)

const quickTags = ['家教', '推广', '远程', '门店', '活动']

const goDetail = (row) => router.push({ name: 'StudentJobDetail', params: { jobId: String(row.id) } })

const q = reactive({ current: 1, size: 12, keyword: '', jobType: '', location: '' })
const jobs = reactive({ records: [], total: 0 })
const loading = ref(false)

/** 卡片顶部强调色：由 id 推导，稳定不随机跳 */
const cardAccentStyle = (row) => {
  const id = Number(row.id) || 0
  const hue = ((id % 7) * 28 + 188) % 360
  return {
    '--accent-h': String(hue)
  }
}

const previewText = (raw) => {
  if (!raw) return '暂无岗位描述，点击查看详情。'
  const t = String(raw)
    .replace(/<[^>]+>/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
  if (t.length <= 100) return t
  return `${t.slice(0, 100)}…`
}

const load = async () => {
  loading.value = true
  try {
    const res = await pageStudentJobs({
      ...q,
      keyword: q.keyword || undefined,
      jobType: q.jobType || undefined,
      location: q.location || undefined
    })
    if (res.success && res.data) Object.assign(jobs, res.data)
  } finally {
    loading.value = false
  }
}

const onSearch = () => {
  q.current = 1
  load()
}

const onReset = () => {
  q.keyword = ''
  q.jobType = ''
  q.location = ''
  q.current = 1
  load()
}

const toggleQuickTag = (tag) => {
  q.jobType = q.jobType === tag ? '' : tag
  q.current = 1
  load()
}

const clearQuickTag = () => {
  q.jobType = ''
  q.current = 1
  load()
}

const apply = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入投递意向', `投递「${row.title}」`, {
      inputPattern: /.+/,
      inputErrorMessage: '不能为空',
      confirmButtonText: '投递',
      cancelButtonText: '取消'
    })
    await applyJob({ jobId: row.id, intention: value })
    ElMessage.success('投递成功')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '失败')
  }
}

const fav = async (row) => {
  try {
    await favoriteJob(row.id)
    ElMessage.success('已收藏')
  } catch (e) {
    ElMessage.error(e.message || '失败')
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.jobs-page {
  margin: -20px -20px 0;
  min-height: calc(100vh - 96px);
  background: linear-gradient(180deg, #f4f7fc 0%, #eef2f8 48%, #f6f8fb 100%);
}

/* ---------- Hero ---------- */
.hero {
  position: relative;
  padding: 28px 24px 36px;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(1200px 400px at 12% -10%, rgba(64, 158, 255, 0.18), transparent 55%),
    radial-gradient(900px 320px at 88% 0%, rgba(103, 194, 58, 0.12), transparent 50%),
    linear-gradient(135deg, #1a2744 0%, #243652 42%, #2c4a6e 100%);
  &::after {
    content: '';
    position: absolute;
    inset: 0;
    opacity: 0.35;
    background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.06'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  }
}

.hero-inner {
  position: relative;
  z-index: 1;
  max-width: 1280px;
  margin: 0 auto;
}

.hero-copy {
  max-width: 640px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.04em;
  color: #e8f4ff;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(8px);
}

.hero-title {
  margin: 18px 0 12px;
  font-size: clamp(1.5rem, 4vw, 2rem);
  font-weight: 800;
  line-height: 1.25;
  color: #fff;
  letter-spacing: -0.02em;
}

.hero-title-accent {
  background: linear-gradient(120deg, #7dd3fc 0%, #a7f3d0 45%, #fde68a 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  padding-right: 2px;
}

.hero-sub {
  margin: 0 0 20px;
  font-size: 14px;
  line-height: 1.65;
  color: rgba(255, 255, 255, 0.72);
}

.hero-stat {
  display: inline-flex;
  align-items: baseline;
  gap: 8px;
  padding: 10px 18px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.14);
  backdrop-filter: blur(10px);
}

.hero-stat-value {
  font-size: 32px;
  font-weight: 800;
  color: #fff;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.03em;
}

.hero-stat-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.75);
}

/* ---------- Body ---------- */
.jobs-body {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 20px 32px;
  position: relative;
  top: -18px;
}

.role-alert {
  margin-bottom: 14px;
  border-radius: 12px;
  border: none;
  :deep(.el-alert__title) {
    font-weight: 600;
  }
}

.quick-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.quick-tags-label {
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.quick-tag {
  cursor: pointer;
  border: 1px solid #e0e6ed;
  background: #fff;
  color: #606266;
  font-size: 13px;
  padding: 7px 16px;
  border-radius: 999px;
  transition:
    color 0.2s,
    border-color 0.2s,
    box-shadow 0.2s,
    transform 0.15s;
  &:hover {
    border-color: #c6e2ff;
    color: #409eff;
    box-shadow: 0 4px 14px rgba(64, 158, 255, 0.12);
  }
  &.active {
    color: #fff;
    background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
    border-color: transparent;
    box-shadow: 0 6px 18px rgba(64, 158, 255, 0.35);
  }
}

.quick-tag-clear {
  border-style: dashed;
  color: #909399;
  &:hover {
    color: #f56c6c;
    border-color: #fbc4c4;
  }
}

.filter-shell {
  background: #fff;
  border-radius: 16px;
  box-shadow:
    0 1px 2px rgba(15, 23, 42, 0.04),
    0 12px 40px rgba(15, 23, 42, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.8);
  margin-bottom: 22px;
  overflow: hidden;
}

.filter-head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 20px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  background: linear-gradient(90deg, rgba(64, 158, 255, 0.06), transparent);
  border-bottom: 1px solid #f0f3f8;
}

.filter-head-icon {
  color: #409eff;
  font-size: 18px;
}

.filter-form {
  padding: 8px 20px 4px;
  :deep(.el-form-item) {
    margin-bottom: 14px;
  }
  :deep(.el-form-item__label) {
    font-weight: 500;
    color: #606266;
  }
}

.filter-input {
  :deep(.el-input__wrapper) {
    border-radius: 10px;
    box-shadow: 0 0 0 1px #e4e9f0 inset;
    transition: box-shadow 0.2s;
    &:hover {
      box-shadow: 0 0 0 1px #c6d5e8 inset;
    }
  }
  :deep(.el-input__wrapper.is-focus) {
    box-shadow: 0 0 0 1px #409eff inset, 0 0 0 4px rgba(64, 158, 255, 0.12);
  }
}

.filter-actions {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.list-wrap {
  min-height: 220px;
}

.list-empty {
  padding: 48px 16px;
  background: #fff;
  border-radius: 16px;
  border: 1px dashed #e0e6ed;
}

.empty-desc {
  margin: 8px 0 16px;
  font-size: 14px;
  color: #909399;
}

.job-grid {
  margin-bottom: 4px;
}

/* ---------- Job card ---------- */
.job-card {
  position: relative;
  margin-bottom: 22px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid #e8ecf2;
  cursor: pointer;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.04);
  transition:
    transform 0.22s cubic-bezier(0.34, 1.2, 0.64, 1),
    box-shadow 0.22s ease,
    border-color 0.22s ease;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    right: 0;
    top: 0;
    height: 4px;
    background: linear-gradient(
      90deg,
      hsl(var(--accent-h, 210), 72%, 56%) 0%,
      hsl(calc(var(--accent-h, 210) + 32), 65%, 62%) 100%
    );
    opacity: 0.92;
  }

  &:hover {
    transform: translateY(-5px);
    border-color: #d4e8ff;
    box-shadow:
      0 16px 40px rgba(64, 158, 255, 0.14),
      0 4px 12px rgba(15, 23, 42, 0.06);
    .job-card-glow {
      opacity: 1;
    }
  }
}

.job-card-glow {
  pointer-events: none;
  position: absolute;
  inset: -40% -20%;
  opacity: 0;
  transition: opacity 0.35s ease;
  background: radial-gradient(
    circle at 80% 0%,
    hsla(var(--accent-h, 210), 80%, 70%, 0.18),
    transparent 55%
  );
}

.job-card-body {
  position: relative;
  z-index: 1;
  padding: 20px 20px 18px;
  display: flex;
  flex-direction: column;
  min-height: 220px;
}

.job-card--skeleton {
  cursor: default;
  &::before {
    display: none;
  }
  &:hover {
    transform: none;
    box-shadow: 0 2px 8px rgba(15, 23, 42, 0.04);
    border-color: #e8ecf2;
  }
  :deep(.el-card__body) {
    padding: 20px;
  }
}

.job-card-head {
  margin-bottom: 12px;
}

.job-title {
  margin: 0 0 12px;
  font-size: 17px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  letter-spacing: -0.01em;
}

.job-meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.meta-pill {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 8px;
  font-weight: 500;
  .el-icon {
    font-size: 14px;
  }
}

.meta-pill--type {
  color: #2563eb;
  background: rgba(37, 99, 235, 0.08);
}

.meta-pill--loc {
  color: #64748b;
  background: #f1f5f9;
}

.job-preview {
  flex: 1;
  margin: 0 0 16px;
  font-size: 13px;
  color: #64748b;
  line-height: 1.65;
  min-height: 64px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.job-card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-top: 14px;
  border-top: 1px solid #f1f5f9;
}

.salary-wrap {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.salary-label {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.06em;
  color: #94a3b8;
  text-transform: uppercase;
}

.salary {
  font-size: 19px;
  font-weight: 800;
  letter-spacing: -0.02em;
  background: linear-gradient(135deg, #f97316 0%, #ef4444 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  justify-content: flex-end;
}

.linkish {
  font-weight: 600;
  padding: 4px 8px;
}

.pager-wrap {
  display: flex;
  justify-content: center;
  padding: 20px 12px 8px;
  :deep(.el-pagination) {
    flex-wrap: wrap;
    justify-content: center;
    padding: 12px 20px;
    background: #fff;
    border-radius: 14px;
    border: 1px solid #e8ecf2;
    box-shadow: 0 4px 16px rgba(15, 23, 42, 0.04);
  }
}

@media (max-width: 768px) {
  .jobs-page {
    margin: -16px -16px 0;
  }
  .hero {
    padding: 22px 16px 28px;
  }
  .jobs-body {
    padding: 0 16px 24px;
    top: -12px;
  }
}
</style>
