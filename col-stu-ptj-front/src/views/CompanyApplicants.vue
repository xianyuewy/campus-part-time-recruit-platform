<template>
  <div>
    <h2>候选人收件箱</h2>
    <el-card class="mb16">
      <el-form :inline="true">
        <el-form-item label="岗位ID">
          <el-input v-model.number="q.jobId" placeholder="可选，不填则看全部投递" clearable/>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="q.status" placeholder="全部" clearable style="width: 160px">
            <el-option label="待企业处理" value="SUBMITTED"/>
            <el-option label="企业已阅" value="VIEWED"/>
            <el-option label="面试沟通中" value="INTERVIEW"/>
            <el-option label="已录用" value="HIRED"/>
            <el-option label="未录用" value="REJECTED"/>
            <el-option label="已取消投递" value="CANCELLED"/>
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="load">查询</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table :data="rows.records" v-loading="loading" border :row-class-name="rowClassName">
        <el-table-column prop="applicationId" label="申请ID" width="160"/>
        <el-table-column prop="jobTitle" label="岗位" min-width="140"/>
        <el-table-column prop="studentDisplayName" label="学生" min-width="120" show-overflow-tooltip/>
        <el-table-column prop="studentRealName" label="真实姓名" width="100" show-overflow-tooltip/>
        <el-table-column label="简历" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.hasResume" size="small" type="success">有</el-tag>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column prop="studentEmail" label="邮箱" min-width="160" show-overflow-tooltip/>
        <el-table-column prop="studentPhone" label="手机号" width="120"/>
        <el-table-column label="状态" width="130">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.statusLabel || statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="interviewTime" label="面试时间" width="170" />
        <el-table-column prop="interviewLocation" label="面试地点" min-width="140" show-overflow-tooltip/>
        <el-table-column prop="intention" label="意向" min-width="160" show-overflow-tooltip/>
        <el-table-column label="操作" width="460" fixed="right">
          <template #default="{row}">
            <el-button size="small" @click="openResume(row)">简历</el-button>
            <el-button size="small" @click="openChat(row)">沟通</el-button>
            <el-button size="small" @click="openInterview(row)">邀约</el-button>
            <el-button size="small" type="success" @click="setStatus(row,'HIRED')">录用</el-button>
            <el-button size="small" type="danger" @click="setStatus(row,'REJECTED')">拒绝</el-button>
            <el-button size="small" type="primary" :disabled="!canReview(row.status)" @click="openReview(row)">评价</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :total="rows.total"
          v-model:current-page="q.current"
          v-model:page-size="q.size"
          @current-change="load"/>
      </div>
    </el-card>

    <el-dialog v-model="reviewVisible" title="评价学生" width="500px">
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
        <el-button @click="reviewVisible=false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="interviewVisible" title="安排面试" width="520px">
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
        <el-button @click="interviewVisible=false">取消</el-button>
        <el-button type="primary" @click="submitInterview">发送安排</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resumeVisible" title="学生简历" width="640px" destroy-on-close>
      <template v-if="resumeLoading"><el-skeleton :rows="6" animated /></template>
      <template v-else>
        <el-descriptions v-if="hasAnyResume" :column="1" border>
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
const route = useRoute()
import { companyApplicationsInbox, jobApplicants, reviewStudent, updateApplicantStatus, getApplicantResume, scheduleApplicantInterview } from '../api/company'
import { API_BASE_URL } from '../utils/request'
import ApplicationChatDialog from '../components/ApplicationChatDialog.vue'

const STATUS_MAP = {
  SUBMITTED: '待企业处理',
  VIEWED: '企业已阅',
  INTERVIEW: '面试沟通中',
  HIRED: '已录用',
  REJECTED: '未录用',
  CANCELLED: '已取消投递'
}
const statusText = (code) => STATUS_MAP[code] || code || '—'

const q = reactive({ current: 1, size: 10, jobId: null, status: '' })
const rows = reactive({ records:[], total:0 })
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

const load = async()=>{
  loading.value=true
  try{
    const params = { current: q.current, size: q.size, status: q.status || undefined }
    const res = q.jobId
      ? await jobApplicants(q.jobId, params)
      : await companyApplicationsInbox(params)
    if(res.success&&res.data) Object.assign(rows,res.data)
    applyRouteFocus()
  } finally {
    loading.value=false
  }
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

const setStatus = async(row,status)=>{
  try{
    await updateApplicantStatus(row.applicationId,{status})
    ElMessage.success('已更新')
    await load()
  } catch(e){
    ElMessage.error(e.message||'失败')
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

<style scoped>
.mb16{margin-bottom:16px}
.pager { margin-top: 16px; display: flex; justify-content: flex-end; }
:deep(.focus-row td) { background: #f0f9eb !important; }
.pre {
  margin: 0;
  white-space: pre-wrap;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.5;
}
</style>
