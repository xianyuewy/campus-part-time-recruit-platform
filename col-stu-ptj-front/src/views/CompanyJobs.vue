<template>
  <div class="company-jobs-page">
    <section class="page-hero">
      <div class="page-hero-bg" aria-hidden="true" />
      <div class="page-hero-inner">
        <div class="page-hero-copy">
          <span class="page-hero-badge">
            <el-icon><OfficeBuilding /></el-icon>
            企业工作台
          </span>
          <h1 class="page-hero-title">岗位<span class="accent">管理</span></h1>
          <p class="page-hero-sub">发布、编辑与下架兼职岗位；支持按关键词、类型与状态快速检索。</p>
          <div v-if="rows.total" class="page-hero-stat">
            <span class="stat-num">{{ rows.total }}</span>
            <span class="stat-label">条岗位记录</span>
          </div>
        </div>
        <div class="page-hero-action">
          <el-button type="primary" size="large" round :icon="Plus" @click="openPublish">发布兼职</el-button>
          <p class="hero-tip">提交后需管理员审核，通过后学生端可见</p>
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
          <div class="filter-field filter-field--keyword">
            <label>关键词</label>
            <el-input
              v-model="q.keyword"
              clearable
              placeholder="标题或描述"
              :prefix-icon="Search"
              @keyup.enter="onSearch"
            />
          </div>
          <div class="filter-field filter-field--type">
            <label>类型</label>
            <el-input
              v-model="q.jobType"
              clearable
              placeholder="岗位类型"
              :prefix-icon="Briefcase"
              @keyup.enter="onSearch"
            />
          </div>
          <div class="filter-field filter-field--status">
            <label>状态</label>
            <el-select v-model="q.status" placeholder="全部" clearable>
              <el-option label="待审核" value="PENDING" />
              <el-option label="已上架" value="APPROVED" />
              <el-option label="已驳回" value="REJECTED" />
              <el-option label="已下架" value="OFFLINE" />
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
          <span class="table-title">岗位列表</span>
          <span v-if="rows.total" class="table-meta">共 {{ rows.total }} 条</span>
        </div>

        <el-table
          :data="rows.records"
          v-loading="loading"
          stripe
          class="jobs-table"
          empty-text="暂无岗位，可调整筛选或点击发布兼职"
        >
          <el-table-column prop="title" label="岗位标题" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="job-title-cell">{{ row.title }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="jobType" label="类型" width="100" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-if="row.jobType" class="type-pill">{{ row.jobType }}</span>
              <span v-else class="text-muted">—</span>
            </template>
          </el-table-column>
          <el-table-column prop="salaryText" label="薪资" width="110" show-overflow-tooltip />
          <el-table-column prop="location" label="地点" min-width="100" show-overflow-tooltip />
          <el-table-column prop="contactPhone" label="联系电话" width="120" show-overflow-tooltip />
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" size="small" effect="light" round>
                {{ jobStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="expireAt" label="到期时间" width="168" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="expire-cell">{{ formatExpire(row.expireAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="168" fixed="right" align="center">
            <template #default="{ row }">
              <el-button size="small" text type="primary" @click="edit(row)">编辑</el-button>
              <el-button
                size="small"
                text
                type="warning"
                :disabled="row.status === 'OFFLINE'"
                @click="offline(row)"
              >
                下架
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pager">
          <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
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

    <el-dialog v-model="publishVisible" title="发布兼职" width="560px" destroy-on-close @closed="resetPublishForm" class="job-dialog">
      <el-form ref="publishFormRef" :model="publishForm" :rules="formRules" label-width="100px">
        <el-form-item label="岗位标题" prop="title">
          <el-input v-model="publishForm.title" maxlength="200" show-word-limit placeholder="简要概括岗位" />
        </el-form-item>
        <el-form-item label="岗位描述" prop="description">
          <el-input
            v-model="publishForm.description"
            type="textarea"
            :rows="5"
            maxlength="4000"
            show-word-limit
            placeholder="工作内容、时间要求、注意事项等"
          />
        </el-form-item>
        <el-form-item label="工作地点" prop="location">
          <el-input v-model="publishForm.location" maxlength="200" placeholder="如：XX区 XX路" />
        </el-form-item>
        <el-form-item label="岗位类型" prop="jobType">
          <el-input v-model="publishForm.jobType" maxlength="100" placeholder="如：促销 / 家教 / 客服" />
        </el-form-item>
        <el-form-item label="薪资文案" prop="salaryText">
          <el-input v-model="publishForm.salaryText" maxlength="100" placeholder="如：150元/天" />
        </el-form-item>
        <el-form-item label="薪资下限" prop="salaryMin">
          <el-input-number v-model="publishForm.salaryMin" :min="0" :max="999999" controls-position="right" class="w-full" />
        </el-form-item>
        <el-form-item label="薪资上限" prop="salaryMax">
          <el-input-number v-model="publishForm.salaryMax" :min="0" :max="999999" controls-position="right" class="w-full" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="publishForm.contactPhone" maxlength="32" placeholder="学生投递后用于联系您" />
        </el-form-item>
        <el-form-item label="微信/沟通" prop="contactWechat">
          <el-input
            v-model="publishForm.contactWechat"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="微信号、企业微信、添加备注说明等"
          />
        </el-form-item>
        <el-form-item label="到期时间" prop="expireAt">
          <el-date-picker
            v-model="publishForm.expireAt"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="不填默认30天后"
            style="width: 100%;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishVisible = false">取消</el-button>
        <el-button type="primary" :loading="publishLoading" @click="submitPublish">提交发布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editVisible" title="编辑岗位" width="560px" destroy-on-close class="job-dialog">
      <el-form ref="editFormRef" :model="editForm" :rules="formRules" label-width="100px">
        <el-form-item label="岗位标题" prop="title">
          <el-input v-model="editForm.title" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="岗位描述" prop="description">
          <el-input v-model="editForm.description" type="textarea" :rows="5" maxlength="4000" show-word-limit />
        </el-form-item>
        <el-form-item label="工作地点" prop="location">
          <el-input v-model="editForm.location" maxlength="200" />
        </el-form-item>
        <el-form-item label="岗位类型" prop="jobType">
          <el-input v-model="editForm.jobType" maxlength="100" />
        </el-form-item>
        <el-form-item label="薪资文案" prop="salaryText">
          <el-input v-model="editForm.salaryText" maxlength="100" />
        </el-form-item>
        <el-form-item label="薪资下限" prop="salaryMin">
          <el-input-number v-model="editForm.salaryMin" :min="0" :max="999999" controls-position="right" class="w-full" />
        </el-form-item>
        <el-form-item label="薪资上限" prop="salaryMax">
          <el-input-number v-model="editForm.salaryMax" :min="0" :max="999999" controls-position="right" class="w-full" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="editForm.contactPhone" maxlength="32" />
        </el-form-item>
        <el-form-item label="微信/沟通" prop="contactWechat">
          <el-input v-model="editForm.contactWechat" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="到期时间" prop="expireAt">
          <el-date-picker
            v-model="editForm.expireAt"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="不填默认30天后"
            style="width: 100%;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Briefcase, Filter, OfficeBuilding, Plus, Refresh, Search } from '@element-plus/icons-vue'
import { publishCompanyJob, myCompanyJobs, offlineCompanyJob, updateCompanyJob } from '../api/company'
import { jobStatusLabel } from '../utils/enumLabel'

const emptyJobPayload = () => ({
  title: '',
  description: '',
  location: '',
  jobType: '',
  salaryText: '',
  salaryMin: null,
  salaryMax: null,
  contactPhone: '',
  contactWechat: '',
  expireAt: ''
})

const publishVisible = ref(false)
const publishLoading = ref(false)
const publishFormRef = ref(null)
const publishForm = reactive(emptyJobPayload())

const editVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref(null)
const editForm = reactive({ id: null, ...emptyJobPayload() })

const q = reactive({ current: 1, size: 10, keyword: '', jobType: '', status: '' })
const rows = reactive({ records: [], total: 0 })
const loading = ref(false)

const formRules = {
  title: [{ required: true, message: '请填写岗位标题', trigger: 'blur' }],
  description: [{ required: true, message: '请填写岗位描述', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请填写联系电话', trigger: 'blur' }],
  contactWechat: [{ required: true, message: '请填写微信或沟通说明', trigger: 'blur' }]
}

const statusTagType = (status) => {
  const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', OFFLINE: 'info' }
  return map[status] || 'info'
}

const formatExpire = (v) => {
  if (!v) return '—'
  const s = String(v).replace('T', ' ')
  return s.length > 16 ? s.slice(0, 16) : s
}

const load = async () => {
  loading.value = true
  try {
    const res = await myCompanyJobs({
      current: q.current,
      size: q.size,
      keyword: q.keyword || undefined,
      jobType: q.jobType || undefined,
      status: q.status || undefined
    })
    if (res.success && res.data) {
      // 单独赋值，杜绝合并失效问题
      rows.records = res.data.records ?? []
      rows.total = Number(res.data.total) ?? 0
      rows.current = res.data.current ?? 1
      rows.size = res.data.size ?? 10
      rows.pages = res.data.pages ?? 0
    } else {
      rows.records = []
      rows.total = 0
    }
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
  q.status = ''
  q.current = 1
  load()
}

const resetPublishForm = () => {
  Object.assign(publishForm, emptyJobPayload())
}

const openPublish = () => {
  resetPublishForm()
  publishVisible.value = true
}

const submitPublish = async () => {
  const form = publishFormRef.value
  if (!form) return
  try {
    await form.validate()
  } catch {
    return
  }
  publishLoading.value = true
  try {
    await publishCompanyJob({ ...publishForm })
    ElMessage.success('发布成功，请等待审核')
    publishVisible.value = false
    await load()
  } catch (e) {
    ElMessage.error(e.message || '失败')
  } finally {
    publishLoading.value = false
  }
}

const edit = (row) => {
  Object.assign(editForm, {
    id: row.id,
    title: row.title || '',
    description: row.description || '',
    location: row.location || '',
    jobType: row.jobType || '',
    salaryText: row.salaryText || '',
    salaryMin: row.salaryMin ?? null,
    salaryMax: row.salaryMax ?? null,
    contactPhone: row.contactPhone || '',
    contactWechat: row.contactWechat || '',
    expireAt: row.expireAt || ''
  })
  editVisible.value = true
}

const saveEdit = async () => {
  const form = editFormRef.value
  if (!form) return
  try {
    await form.validate()
  } catch {
    return
  }
  editLoading.value = true
  try {
    const { id, ...payload } = editForm
    await updateCompanyJob(id, payload)
    ElMessage.success('已更新，修改后需重新审核')
    editVisible.value = false
    await load()
  } catch (e) {
    ElMessage.error(e.message || '失败')
  } finally {
    editLoading.value = false
  }
}

const offline = async (row) => {
  try {
    await offlineCompanyJob(row.id)
    ElMessage.success('已下架')
    await load()
  } catch (e) {
    ElMessage.error(e.message || '失败')
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.company-jobs-page {
  margin: -8px -4px 0;
  min-height: 100%;
}

.page-hero {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #1e3a5f 0%, #2d5a87 42%, #3d7ab5 100%);
  color: #fff;
}

.page-hero-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 85% 20%, rgba(255, 255, 255, 0.14), transparent 45%),
    radial-gradient(circle at 10% 80%, rgba(100, 181, 246, 0.2), transparent 50%);
  pointer-events: none;
}

.page-hero-inner {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 28px 32px;
  flex-wrap: wrap;
}

.page-hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.04em;
  padding: 5px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(6px);
  margin-bottom: 10px;
}

.page-hero-title {
  margin: 0 0 8px;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: -0.02em;

  .accent {
    color: #a8d4ff;
  }
}

.page-hero-sub {
  margin: 0;
  font-size: 14px;
  opacity: 0.88;
  max-width: 420px;
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

.page-hero-action {
  text-align: center;
  flex-shrink: 0;
}

.hero-tip {
  margin: 10px 0 0;
  font-size: 12px;
  opacity: 0.75;
  max-width: 200px;
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
  background: linear-gradient(90deg, rgba(45, 90, 135, 0.06), transparent);
  border-bottom: 1px solid #f0f3f8;
}

.toolbar-head-icon {
  color: #2d5a87;
  font-size: 18px;
}

/* 单行筛选 */
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

.filter-field--keyword {
  flex: 1 1 200px;
  min-width: 160px;
  max-width: 240px;
}

.filter-field--type {
  width: 150px;
}

.filter-field--status {
  width: 130px;
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

.jobs-table {
  :deep(.el-table__header th) {
    background: #f8fafc !important;
    color: #606266;
    font-weight: 600;
  }

  :deep(.el-table__row) {
    transition: background 0.15s;
  }
}

.job-title-cell {
  font-weight: 600;
  color: #303133;
}

.type-pill {
  display: inline-block;
  padding: 2px 10px;
  font-size: 12px;
  border-radius: 999px;
  background: #f0f5fa;
  color: #2d5a87;
}

.text-muted {
  color: #c0c4cc;
}

.expire-cell {
  font-size: 13px;
  color: #606266;
}

.pager {
  padding: 14px 20px 18px;
  display: flex;
  justify-content: flex-end;
}

.w-full {
  width: 100%;
}

@media (max-width: 1100px) {
  .filter-bar {
    flex-wrap: wrap;
  }

  .filter-actions {
    margin-left: 0;
    width: 100%;
    justify-content: flex-end;
  }
}

@media (max-width: 768px) {
  .page-hero-inner {
    flex-direction: column;
    align-items: flex-start;
    padding: 20px;
  }

  .filter-field--keyword,
  .filter-field--type,
  .filter-field--status {
    width: 100%;
    max-width: none;
    flex: 1 1 100%;
  }
}
</style>
