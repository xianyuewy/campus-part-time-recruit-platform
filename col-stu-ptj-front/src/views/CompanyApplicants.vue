<template>
  <div class="company-applicants-page">
    <section class="page-hero">
      <div class="page-hero-bg" aria-hidden="true" />
      <div class="page-hero-inner">
        <div class="page-hero-copy">
          <span class="page-hero-badge">
            <el-icon><User /></el-icon>
            人才管理
          </span>
          <h1 class="page-hero-title">候选人<span class="accent">收件箱</span></h1>
          <p class="page-hero-sub">查看投递、沟通面试、处理录用；支持按岗位名称与投递状态筛选。</p>
          <div v-if="rows.total" class="page-hero-stat">
            <span class="stat-num">{{ rows.total }}</span>
            <span class="stat-label">条投递记录</span>
          </div>
        </div>
      </div>
    </section>

    <div class="page-body">
      <div class="toolbar-card">
        <div class="toolbar-head">
          <el-icon class="toolbar-head-icon"><Filter /></el-icon>
          <span>筛选查询</span>
        </div>
        <form class="filter-bar" @submit.prevent="onSearch">
          <div class="filter-field filter-field--title">
            <label>岗位名称</label>
            <el-input
              v-model="q.jobTitle"
              clearable
              placeholder="模糊搜索岗位标题"
              :prefix-icon="Briefcase"
              @keyup.enter="onSearch"
            />
          </div>
          <div class="filter-field filter-field--status">
            <label>状态</label>
            <el-select v-model="q.status" placeholder="全部" clearable>
              <el-option label="待企业处理" value="SUBMITTED" />
              <el-option label="企业已阅" value="VIEWED" />
              <el-option label="面试沟通中" value="INTERVIEW" />
              <el-option label="已录用" value="HIRED" />
              <el-option label="未录用" value="REJECTED" />
              <el-option label="已取消投递" value="CANCELLED" />
            </el-select>
          </div>
          <div class="filter-actions">
            <el-button type="primary" round :icon="Search" :loading="loading" native-type="submit">查询</el-button>
            <el-button round :icon="Refresh" :disabled="loading" @click.prevent="onReset">重置</el-button>
          </div>
        </form>
      </div>

      <div class="table-card">
        <div class="table-card-head">
          <span class="table-title">投递列表</span>
          <span v-if="rows.total" class="table-meta">共 {{ rows.total }} 条</span>
        </div>

        <el-table
          :data="rows.records"
          v-loading="loading"
          stripe
          class="applicants-table"
          :row-class-name="rowClassName"
          empty-text="暂无投递记录，可调整筛选条件"
        >
          <el-table-column prop="jobTitle" label="应聘岗位" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="job-cell">{{ row.jobTitle || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="学生" min-width="130" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="student-cell">
                <span class="student-name">{{ row.studentDisplayName || '—' }}</span>
                <span v-if="row.studentRealName" class="student-real">{{ row.studentRealName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="简历" width="72" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.hasResume" size="small" type="success" effect="light" round>有</el-tag>
              <span v-else class="text-muted">—</span>
            </template>
          </el-table-column>
          <el-table-column prop="studentPhone" label="手机" width="120" show-overflow-tooltip />
          <el-table-column label="状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="appStatusTagType(row.status)" size="small" effect="light" round>
                {{ row.statusLabel || statusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="interviewTime" label="面试时间" width="168" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="time-cell">{{ formatTime(row.interviewTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="intention" label="求职意向" min-width="120" show-overflow-tooltip />
          <el-table-column label="操作" width="320" fixed="right" align="center">
            <template #default="{ row }">
              <div class="action-group">
                <el-button size="small" text type="primary" @click="openResume(row)">简历</el-button>
                <el-button size="small" text type="primary" @click="openChat(row)">沟通</el-button>
                <el-button size="small" text type="primary" @click="openInterview(row)">邀约</el-button>
                <el-divider direction="vertical" />
                <el-button size="small" text type="success" @click="setStatus(row, 'HIRED')">录用</el-button>
                <el-button size="small" text type="danger" @click="setStatus(row, 'REJECTED')">拒绝</el-button>
                <el-button
                  size="small"
                  text
                  type="warning"
                  :disabled="!canReview(row.status)"
                  @click="openReview(row)"
                >
                  评价
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="pager">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next"
            :total="rows.total"
            v-model:current-page="q.current"
            v-model:page-size="q.size"
            :page-sizes="[10, 20, 50]"
            @size-change="load"
            @current-change="load"
          />
        </div>
      </div>
    </div>

    <el-dialog v-model="reviewVisible" title="评价学生" width="500px" class="applicant-dialog">
      <el-form label-width="90px">
        <el-form-item label="学生">
          <el-input :model-value="reviewForm.studentLabel" disabled />
        </el-form-item>
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.score" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="reviewForm.content" type="textarea" rows="3" placeholder="可选，最多200字" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="interviewVisible" title="安排面试" width="520px" class="applicant-dialog">
      <el-form label-width="90px">
        <el-form-item label="学生">
          <el-input :model-value="interviewForm.studentLabel" disabled />
        </el-form-item>
        <el-form-item label="面试时间">
          <el-date-picker
            v-model="interviewForm.interviewTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="选择日期时间"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="面试地点">
          <el-input v-model="interviewForm.interviewLocation" maxlength="200" placeholder="如：XX大厦 3楼会议室" />
        </el-form-item>
        <el-form-item label="补充说明">
          <el-input v-model="interviewForm.interviewNote" type="textarea" rows="3" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="interviewVisible = false">取消</el-button>
        <el-button type="primary" @click="submitInterview">发送安排</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resumeVisible" title="学生简历" width="640px" destroy-on-close class="applicant-dialog">
      <template v-if="resumeLoading"><el-skeleton :rows="6" animated /></template>
      <template v-else>
        <el-descriptions v-if="hasAnyResume" :column="1" border class="resume-desc">
          <el-descriptions-item v-if="resume.attachmentPath" label="简历附件">
            <el-link type="primary" :href="resumeAttachmentHref" target="_blank" rel="noopener">
              {{ resume.attachmentOriginalName || '下载 PDF' }}
            </el-link>
          </el-descriptions-item>
          <el-descriptions-item label="自我介绍">{{ resume.selfIntro || '—' }}</el-descriptions-item>
          <el-descriptions-item label="教育经历"><pre class="pre">{{ resume.education || '—' }}</pre></el-descriptions-item>
          <el-descriptions-item label="技能特长"><pre class="pre">{{ resume.skills || '—' }}</pre></el-descriptions-item>
          <el-descriptions-item label="工作/实践"><pre class="pre">{{ resume.workExperience || '—' }}</pre></el-descriptions-item>
        </el-descriptions>
        <el-empty v-else description="该学生尚未填写在线简历" />
      </template>
    </el-dialog>

    <ApplicationChatDialog
      v-model="chatVisible"
      :application-id="chatAppId"
      :can-send="chatCanSend"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Briefcase, Filter, Refresh, Search, User } from '@element-plus/icons-vue'
import { companyApplicationsInbox, reviewStudent, updateApplicantStatus, getApplicantResume, scheduleApplicantInterview } from '../api/company'
import { API_BASE_URL } from '../utils/request'
import ApplicationChatDialog from '../components/ApplicationChatDialog.vue'

const route = useRoute()

const STATUS_MAP = {
  SUBMITTED: '待企业处理',
  VIEWED: '企业已阅',
  INTERVIEW: '面试沟通中',
  HIRED: '已录用',
  REJECTED: '未录用',
  CANCELLED: '已取消投递'
}
const statusText = (code) => STATUS_MAP[code] || code || '—'

const appStatusTagType = (status) => {
  const map = {
    SUBMITTED: 'warning',
    VIEWED: 'info',
    INTERVIEW: 'primary',
    HIRED: 'success',
    REJECTED: 'danger',
    CANCELLED: 'info'
  }
  return map[status] || 'info'
}

const formatTime = (v) => {
  if (!v) return '—'
  const s = String(v).replace('T', ' ')
  return s.length > 16 ? s.slice(0, 16) : s
}

const q = reactive({ current: 1, size: 10, jobTitle: '', status: '' })
const rows = reactive({ records: [], total: 0 })
const loading = ref(false)

const reviewVisible = ref(false)
const reviewForm = reactive({ applicationId: null, studentLabel: '', score: 5, content: '' })
const interviewVisible = ref(false)
const interviewForm = reactive({
  applicationId: null,
  studentLabel: '',
  interviewTime: '',
  interviewLocation: '',
  interviewNote: ''
})

const resumeVisible = ref(false)
const resumeLoading = ref(false)
const resume = reactive({
  selfIntro: '',
  education: '',
  skills: '',
  workExperience: '',
  attachmentPath: '',
  attachmentOriginalName: ''
})
const hasAnyResume = computed(
  () =>
    !!(
      resume.selfIntro ||
      resume.education ||
      resume.skills ||
      resume.workExperience ||
      resume.attachmentPath
    )
)
const resumeAttachmentHref = computed(() => {
  const p = resume.attachmentPath
  if (!p) return '#'
  const base = API_BASE_URL.replace(/\/$/, '')
  return p.startsWith('/') ? `${base}${p}` : `${base}/${p}`
})

const chatVisible = ref(false)
const chatAppId = ref(null)
const chatCanSend = ref(true)
const focusedAppId = ref(null)

const load = async () => {
  loading.value = true
  try {
    const params = {
      current: q.current,
      size: q.size,
      status: q.status || undefined,
      jobTitle: q.jobTitle?.trim() || undefined
    }
    const res = await companyApplicationsInbox(params)
    if (res.success && res.data) Object.assign(rows, res.data)
    applyRouteFocus()
  } finally {
    loading.value = false
  }
}

const onSearch = () => {
  q.current = 1
  load()
}

const onReset = () => {
  q.jobTitle = ''
  q.status = ''
  q.current = 1
  load()
}

const applyRouteFocus = () => {
  const appId = Number(route.query.applicationId)
  if (!appId) {
    focusedAppId.value = null
    return
  }
  const target = (rows.records || []).find((r) => Number(r.applicationId) === appId)
  if (!target) {
    ElMessage.info('未在当前分页找到该申请记录，请翻页查看')
    focusedAppId.value = null
    return
  }
  focusedAppId.value = appId
}

const setStatus = async (row, status) => {
  try {
    await updateApplicantStatus(row.applicationId, { status })
    ElMessage.success('已更新')
    await load()
  } catch (e) {
    ElMessage.error(e.message || '失败')
  }
}

const canReview = (status) => ['HIRED', 'CANCELLED'].includes(status)

const studentRowLabel = (row) => {
  const nick = row.studentDisplayName || '—'
  if (row.studentRealName) return `${nick}（${row.studentRealName}）`
  return nick
}

const openReview = (row) => {
  reviewForm.applicationId = row.applicationId
  reviewForm.studentLabel = studentRowLabel(row)
  reviewForm.score = 5
  reviewForm.content = ''
  reviewVisible.value = true
}

const openChat = (row) => {
  chatAppId.value = row.applicationId
  chatCanSend.value = row.status !== 'CANCELLED'
  chatVisible.value = true
}

const openInterview = (row) => {
  interviewForm.applicationId = row.applicationId
  interviewForm.studentLabel = studentRowLabel(row)
  interviewForm.interviewTime = row.interviewTime || ''
  interviewForm.interviewLocation = row.interviewLocation || ''
  interviewForm.interviewNote = row.interviewNote || ''
  interviewVisible.value = true
}

const rowClassName = ({ row }) => {
  return focusedAppId.value && Number(row.applicationId) === Number(focusedAppId.value) ? 'focus-row' : ''
}

const openResume = async (row) => {
  resumeVisible.value = true
  resumeLoading.value = true
  Object.assign(resume, {
    selfIntro: '',
    education: '',
    skills: '',
    workExperience: '',
    attachmentPath: '',
    attachmentOriginalName: ''
  })
  try {
    const res = await getApplicantResume(row.applicationId)
    if (res.success && res.data) {
      Object.assign(resume, {
        selfIntro: res.data.selfIntro || '',
        education: res.data.education || '',
        skills: res.data.skills || '',
        workExperience: res.data.workExperience || '',
        attachmentPath: res.data.attachmentPath || '',
        attachmentOriginalName: res.data.attachmentOriginalName || ''
      })
    }
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    resumeLoading.value = false
  }
}

const submitReview = async () => {
  if (!reviewForm.score) {
    ElMessage.warning('请先选择评分')
    return
  }
  try {
    await reviewStudent({
      applicationId: reviewForm.applicationId,
      score: reviewForm.score,
      content: reviewForm.content
    })
    ElMessage.success('评价成功')
    reviewVisible.value = false
  } catch (e) {
    ElMessage.error(e.message || '评价失败')
  }
}

const submitInterview = async () => {
  if (!interviewForm.interviewTime || !interviewForm.interviewLocation?.trim()) {
    ElMessage.warning('请填写面试时间和地点')
    return
  }
  try {
    await scheduleApplicantInterview(interviewForm.applicationId, {
      interviewTime: interviewForm.interviewTime,
      interviewLocation: interviewForm.interviewLocation.trim(),
      interviewNote: interviewForm.interviewNote?.trim() || ''
    })
    ElMessage.success('面试安排已发送')
    interviewVisible.value = false
    await load()
  } catch (e) {
    ElMessage.error(e.message || '发送失败')
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.company-applicants-page {
  margin: -8px -4px 0;
  min-height: 100%;
}

.page-hero {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #2c4a3e 0%, #3d6b5a 42%, #4a8f72 100%);
  color: #fff;
}

.page-hero-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 88% 15%, rgba(255, 255, 255, 0.12), transparent 42%),
    radial-gradient(circle at 8% 75%, rgba(167, 243, 208, 0.18), transparent 48%);
  pointer-events: none;
}

.page-hero-inner {
  position: relative;
  padding: 28px 32px;
}

.page-hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  padding: 5px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.15);
  margin-bottom: 10px;
}

.page-hero-title {
  margin: 0 0 8px;
  font-size: 26px;
  font-weight: 700;

  .accent {
    color: #b8f0d8;
  }
}

.page-hero-sub {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
  max-width: 480px;
  line-height: 1.55;
}

.page-hero-stat {
  margin-top: 14px;
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.stat-num {
  font-size: 22px;
  font-weight: 700;
}

.stat-label {
  font-size: 13px;
  opacity: 0.85;
}

.page-body {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.toolbar-card,
.table-card {
  background: #fff;
  border-radius: 16px;
  box-shadow:
    0 1px 2px rgba(15, 23, 42, 0.04),
    0 10px 36px rgba(15, 23, 42, 0.06);
  border: 1px solid #eef1f6;
  overflow: hidden;
}

.toolbar-head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  background: linear-gradient(90deg, rgba(61, 107, 90, 0.07), transparent);
  border-bottom: 1px solid #f0f3f8;
}

.toolbar-head-icon {
  color: #3d6b5a;
  font-size: 18px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 20px;
  flex-wrap: nowrap;
}

.filter-field {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;

  label {
    flex-shrink: 0;
    font-size: 13px;
    font-weight: 500;
    color: #606266;
    white-space: nowrap;
  }

  :deep(.el-input),
  :deep(.el-select) {
    width: 100%;
  }

  :deep(.el-input__wrapper) {
    border-radius: 10px;
    box-shadow: 0 0 0 1px #e4e9f0 inset;
  }
}

.filter-field--title {
  flex: 1 1 280px;
  min-width: 200px;
  max-width: 360px;
}

.filter-field--status {
  width: 160px;
}

.filter-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  margin-left: auto;
}

.table-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  border-bottom: 1px solid #f0f3f8;
}

.table-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.table-meta {
  font-size: 13px;
  color: #909399;
}

.applicants-table {
  :deep(.el-table__header th) {
    background: #f8fafc !important;
    color: #606266;
    font-weight: 600;
  }
}

.job-cell {
  font-weight: 600;
  color: #303133;
}

.student-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
  line-height: 1.3;
}

.student-name {
  font-weight: 500;
  color: #303133;
}

.student-real {
  font-size: 12px;
  color: #909399;
}

.text-muted {
  color: #c0c4cc;
}

.time-cell {
  font-size: 13px;
  color: #606266;
}

.action-group {
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap;
  justify-content: center;
  gap: 2px;

  :deep(.el-divider--vertical) {
    margin: 0 4px;
    height: 14px;
  }
}

.pager {
  padding: 14px 20px 18px;
  display: flex;
  justify-content: flex-end;
}

:deep(.focus-row td) {
  background: #ecfdf5 !important;
}

.pre {
  margin: 0;
  white-space: pre-wrap;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.5;
}

@media (max-width: 900px) {
  .filter-bar {
    flex-wrap: wrap;
  }

  .filter-actions {
    margin-left: 0;
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
