<template>
  <div class="applications-page">
    <header class="page-head">
      <div>
        <h1 class="page-title">我的投递</h1>
        <p class="page-desc">跟踪投递进度、查看面试安排并与企业沟通</p>
      </div>
      <div v-if="rows.total" class="page-stat">
        <span class="stat-num">{{ rows.total }}</span>
        <span class="stat-label">条记录</span>
      </div>
    </header>

    <div v-loading="loading" class="list-shell">
      <template v-if="loading">
        <div class="skeleton-grid">
          <el-card v-for="n in 4" :key="n" class="sk-card" shadow="never">
            <el-skeleton animated :rows="4" />
          </el-card>
        </div>
      </template>

      <el-empty
        v-else-if="!rows.records.length"
        class="list-empty"
        description="暂无投递记录"
      >
        <p class="empty-hint">去岗位浏览页投递心仪兼职吧</p>
        <el-button type="primary" round @click="router.push('/jobs')">浏览岗位</el-button>
      </el-empty>

      <template v-else>
        <div class="app-cards">
          <article
            v-for="row in rows.records"
            :key="row.id"
            class="app-card"
            :class="{ 'app-card--focus': isFocused(row) }"
          >
            <div class="app-card-top">
              <div class="app-card-title-row">
                <h2 class="job-title">{{ row.jobTitle || '岗位' }}</h2>
                <el-tag :type="statusTagType(row.status)" effect="light" round class="status-tag">
                  {{ row.statusLabel || statusText(row.status) }}
                </el-tag>
              </div>
              <div class="company-line">
                <el-icon><OfficeBuilding /></el-icon>
                <span>{{ row.companyDisplayName || row.companyUsername || '企业' }}</span>
              </div>
            </div>

            <div class="app-meta">
              <div class="meta-cell">
                <span class="meta-k">投递时间</span>
                <span class="meta-v">{{ formatTime(row.createTime) }}</span>
              </div>
              <div class="meta-cell meta-cell--wide">
                <span class="meta-k">投递意向</span>
                <span class="meta-v intention">{{ row.intention || '—' }}</span>
              </div>
            </div>

            <div v-if="hasInterviewSummary(row)" class="interview-strip">
              <el-icon><Calendar /></el-icon>
              <span>{{ formatTime(row.interviewTime) }}</span>
              <span v-if="row.interviewLocation" class="sep">·</span>
              <span v-if="row.interviewLocation">{{ row.interviewLocation }}</span>
            </div>

            <footer class="app-card-actions">
              <el-button type="primary" plain round size="small" @click="openChat(row)">
                <el-icon><ChatDotRound /></el-icon>
                沟通
              </el-button>
              <el-button
                round
                size="small"
                :disabled="!hasInterviewDetail(row)"
                @click="openInterviewDetail(row)"
              >
                <el-icon><Document /></el-icon>
                面试详情
              </el-button>
              <el-button
                round
                size="small"
                type="warning"
                plain
                :disabled="!canCancel(row.status)"
                @click="cancel(row.id)"
              >
                取消投递
              </el-button>
              <el-button
                round
                size="small"
                type="success"
                :disabled="!canReview(row.status)"
                @click="openReview(row)"
              >
                评价企业
              </el-button>
            </footer>
          </article>
        </div>

        <div class="pager-wrap">
          <el-pagination
            v-model:current-page="q.current"
            v-model:page-size="q.size"
            background
            layout="total, prev, pager, next, jumper"
            :total="rows.total"
            @current-change="load"
          />
        </div>
      </template>
    </div>

    <el-dialog v-model="reviewVisible" title="评价企业" width="500px" destroy-on-close>
      <el-form label-width="90px">
        <el-form-item label="岗位">
          <el-input :model-value="reviewForm.jobTitle" disabled />
        </el-form-item>
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.score" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="reviewForm.content" type="textarea" rows="3" maxlength="200" show-word-limit placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" round @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>

    <ApplicationChatDialog v-model="chatVisible" :application-id="chatAppId" :can-send="chatCanSend" />

    <el-dialog v-model="interviewVisible" title="面试安排详情" width="520px" destroy-on-close>
      <el-descriptions v-if="interviewDetail" :column="1" border class="interview-desc">
        <el-descriptions-item label="申请 ID">{{ interviewDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="岗位">{{ interviewDetail.jobTitle || '—' }}</el-descriptions-item>
        <el-descriptions-item label="企业">
          {{ interviewDetail.companyDisplayName || interviewDetail.companyUsername || '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="面试时间">{{ interviewDetail.interviewTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="面试地点">{{ interviewDetail.interviewLocation || '—' }}</el-descriptions-item>
        <el-descriptions-item label="面试说明">{{ interviewDetail.interviewNote || '—' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" round @click="interviewVisible = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Calendar, ChatDotRound, Document, OfficeBuilding } from '@element-plus/icons-vue'
import { myApplications, cancelApplication, reviewCompany } from '../api/student'
import ApplicationChatDialog from '../components/ApplicationChatDialog.vue'

const route = useRoute()
const router = useRouter()

const STATUS_MAP = {
  SUBMITTED: '待企业处理',
  VIEWED: '企业已阅',
  INTERVIEW: '面试沟通中',
  HIRED: '已录用',
  REJECTED: '未录用',
  CANCELLED: '已取消投递'
}

const statusText = (code) => STATUS_MAP[code] || code || '—'

const statusTagType = (status) => {
  const m = {
    SUBMITTED: 'warning',
    VIEWED: 'info',
    INTERVIEW: 'primary',
    HIRED: 'success',
    REJECTED: 'danger',
    CANCELLED: 'info'
  }
  return m[status] || 'info'
}

const formatTime = (val) => {
  if (!val) return '—'
  try {
    const d = new Date(val)
    if (Number.isNaN(d.getTime())) return String(val)
    return d.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return String(val)
  }
}

const q = reactive({ current: 1, size: 10 })
const rows = reactive({ records: [], total: 0 })
const loading = ref(false)

const reviewVisible = ref(false)
const reviewForm = reactive({ applicationId: null, jobTitle: '', score: 5, content: '' })

const chatVisible = ref(false)
const chatAppId = ref(null)
const chatCanSend = ref(true)
const interviewVisible = ref(false)
const interviewDetail = ref(null)
const focusedAppId = ref(null)

const isFocused = (row) => focusedAppId.value && Number(row.id) === Number(focusedAppId.value)

const load = async () => {
  loading.value = true
  try {
    const res = await myApplications(q)
    if (res.success && res.data) Object.assign(rows, res.data)
    applyRouteFocus()
  } finally {
    loading.value = false
  }
}

const applyRouteFocus = () => {
  const appId = Number(route.query.applicationId)
  if (!appId) {
    focusedAppId.value = null
    return
  }
  const target = (rows.records || []).find((r) => Number(r.id) === appId)
  if (!target) {
    ElMessage.info('未在当前分页找到该申请记录，请翻页查看')
    focusedAppId.value = null
    return
  }
  focusedAppId.value = appId
  if (hasInterviewDetail(target)) {
    interviewDetail.value = target
    interviewVisible.value = true
  }
}

const canCancel = (status) => ['SUBMITTED', 'VIEWED', 'INTERVIEW'].includes(status)
const canReview = (status) => ['HIRED', 'CANCELLED'].includes(status)

const openChat = (row) => {
  chatAppId.value = row.id
  chatCanSend.value = row.status !== 'CANCELLED'
  chatVisible.value = true
}

const hasInterviewDetail = (row) => {
  return Boolean(row?.interviewTime || row?.interviewLocation || row?.interviewNote)
}

const hasInterviewSummary = (row) => {
  return Boolean(row?.interviewTime || row?.interviewLocation)
}

const openInterviewDetail = (row) => {
  if (!hasInterviewDetail(row)) {
    ElMessage.info('暂未收到面试安排详情')
    return
  }
  interviewDetail.value = row
  interviewVisible.value = true
}

const cancel = async (appId) => {
  try {
    await cancelApplication(appId)
    ElMessage.success('已取消投递')
    await load()
  } catch (e) {
    ElMessage.error(e.message || '取消失败')
  }
}

const openReview = (row) => {
  reviewForm.applicationId = row.id
  reviewForm.jobTitle = row.jobTitle || ''
  reviewForm.score = 5
  reviewForm.content = ''
  reviewVisible.value = true
}

const submitReview = async () => {
  if (!reviewForm.score) {
    ElMessage.warning('请先选择评分')
    return
  }
  try {
    await reviewCompany({
      applicationId: reviewForm.applicationId,
      score: reviewForm.score,
      content: reviewForm.content
    })
    ElMessage.success('评价成功')
    reviewVisible.value = false
    await load()
  } catch (e) {
    ElMessage.error(e.message || '评价失败')
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.applications-page {
  max-width: 900px;
  margin: 0 auto;
}

.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e8ecf0;
}

.page-title {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.page-desc {
  margin: 0;
  font-size: 14px;
  color: #94a3b8;
}

.page-stat {
  text-align: right;
}

.stat-num {
  font-size: 28px;
  font-weight: 800;
  color: #409eff;
  margin-right: 4px;
}

.stat-label {
  font-size: 13px;
  color: #94a3b8;
}

.list-shell {
  min-height: 200px;
}

.skeleton-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sk-card {
  border-radius: 14px;
  border: 1px solid #eef2f7;
}

.list-empty {
  padding: 48px 20px;
  background: #fff;
  border-radius: 16px;
  border: 1px dashed #e2e8f0;
}

.empty-hint {
  margin: 0 0 16px;
  font-size: 14px;
  color: #94a3b8;
}

.app-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.app-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e8ecf2;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.04);
  padding: 20px 22px;
  transition:
    box-shadow 0.2s ease,
    border-color 0.2s ease,
    transform 0.2s ease;
  &:hover {
    border-color: #d0e3f7;
    box-shadow: 0 10px 28px rgba(64, 158, 255, 0.1);
  }
}

.app-card--focus {
  border-color: #95d475;
  box-shadow: 0 0 0 1px rgba(149, 212, 117, 0.5), 0 12px 32px rgba(103, 194, 58, 0.12);
}

.app-card-top {
  margin-bottom: 14px;
}

.app-card-title-row {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.job-title {
  margin: 0;
  flex: 1;
  min-width: 0;
  font-size: 18px;
  font-weight: 800;
  color: #1e293b;
  line-height: 1.35;
  letter-spacing: -0.02em;
}

.status-tag {
  flex-shrink: 0;
  font-weight: 600;
}

.company-line {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #64748b;
  .el-icon {
    color: #94a3b8;
  }
}

.app-meta {
  display: grid;
  grid-template-columns: 1fr 1.4fr;
  gap: 12px 20px;
  padding: 14px 0;
  border-top: 1px dashed #f1f5f9;
  border-bottom: 1px dashed #f1f5f9;
}

.meta-cell {
  min-width: 0;
}

.meta-k {
  display: block;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.06em;
  color: #94a3b8;
  text-transform: uppercase;
  margin-bottom: 4px;
}

.meta-v {
  font-size: 14px;
  color: #334155;
  font-weight: 500;
}

.intention {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}

.interview-strip {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin-top: 14px;
  padding: 10px 14px;
  border-radius: 10px;
  background: linear-gradient(90deg, rgba(59, 130, 246, 0.08), rgba(14, 165, 233, 0.05));
  border: 1px solid rgba(59, 130, 246, 0.15);
  font-size: 13px;
  color: #334155;
  .el-icon {
    color: #3b82f6;
    font-size: 16px;
  }
  .sep {
    opacity: 0.5;
  }
}

.app-card-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
  .el-button {
    font-weight: 600;
  }
  .el-icon {
    margin-right: 2px;
    vertical-align: text-bottom;
  }
}

.pager-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding: 4px;
  :deep(.el-pagination) {
    flex-wrap: wrap;
    justify-content: center;
    padding: 12px 18px;
    background: #fff;
    border-radius: 14px;
    border: 1px solid #e8ecf2;
    box-shadow: 0 4px 16px rgba(15, 23, 42, 0.04);
  }
}

.interview-desc {
  border-radius: 10px;
  overflow: hidden;
}

@media (max-width: 640px) {
  .app-meta {
    grid-template-columns: 1fr;
  }
  .meta-cell--wide {
    grid-column: 1;
  }
  .page-head {
    flex-direction: column;
    align-items: flex-start;
  }
  .page-stat {
    text-align: left;
  }
}
</style>
