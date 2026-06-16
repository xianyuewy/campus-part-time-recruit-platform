<template>
  <div class="credit-page credit-page--student">
    <section class="page-hero">
      <div class="page-hero-bg" aria-hidden="true" />
      <div class="page-hero-inner">
        <div class="page-hero-copy">
          <span class="page-hero-badge">
            <el-icon><Medal /></el-icon>
            信用体系
          </span>
          <h1 class="page-hero-title">信用<span class="accent">中心</span></h1>
          <p class="page-hero-sub">查看信用分与调整记录，对争议事项发起申诉并补充材料。</p>
        </div>
        <div v-loading="loadingProfile" class="score-panel">
          <template v-if="credit">
            <div class="score-num">{{ credit.score ?? '—' }}</div>
            <div class="score-label">信用分</div>
            <el-tag :type="levelTagType(credit.creditLevel)" effect="dark" round size="large" class="score-level">
              {{ creditLevelLabel(credit.creditLevel) }}
            </el-tag>
          </template>
          <el-empty v-else :image-size="56" description="暂无信用档案" />
        </div>
      </div>
    </section>

    <div class="page-body">
      <div class="table-card">
        <div class="tabs-bar">
          <el-tabs v-model="tab" class="styled-tabs" @tab-change="onTab">
            <el-tab-pane label="调整记录" name="logs" />
            <el-tab-pane label="争议工单" name="disputes" />
          </el-tabs>
          <div class="tabs-actions">
            <el-button v-if="tab === 'disputes'" type="primary" round :icon="Plus" @click="dlg = true">发起争议</el-button>
          </div>
        </div>

        <div v-if="tab === 'disputes'" class="dispute-toolbar">
          <el-radio-group v-model="disputeScope" size="default" @change="onDisputeScopeChange">
            <el-radio-button label="INITIATED">我发起的</el-radio-button>
            <el-radio-button label="RECEIVED">针对我的</el-radio-button>
            <el-radio-button label="ALL">全部</el-radio-button>
          </el-radio-group>
          <span class="scope-hint">
            <el-icon><InfoFilled /></el-icon>
            「针对我的」含企业/学生将您列为被诉方的工单
          </span>
        </div>

        <el-table
          v-if="tab === 'logs'"
          :data="rows.records"
          v-loading="loading"
          stripe
          class="data-table"
          empty-text="暂无调整记录"
        >
          <el-table-column label="变动" width="100" align="center">
            <template #default="{ row }">
              <span :class="['delta-cell', deltaClass(row.delta)]">{{ formatDelta(row.delta) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="原因" min-width="200" show-overflow-tooltip />
          <el-table-column prop="adminUsername" label="操作人" width="120" show-overflow-tooltip />
          <el-table-column prop="createTime" label="时间" width="168">
            <template #default="{ row }">
              <span class="time-cell">{{ formatTime(row.createTime) }}</span>
            </template>
          </el-table-column>
        </el-table>

        <el-table
          v-else
          :data="rows.records"
          v-loading="loading"
          stripe
          class="data-table"
          empty-text="暂无争议工单"
        >
          <el-table-column prop="id" label="工单号" width="88" />
          <el-table-column label="申诉人" width="120" show-overflow-tooltip>
            <template #default="{ row }">{{ row.reporterDisplayName || row.reporterUsername || '—' }}</template>
          </el-table-column>
          <el-table-column label="被诉方" width="120" show-overflow-tooltip>
            <template #default="{ row }">{{ row.targetDisplayName || row.targetUsername || '—' }}</template>
          </el-table-column>
          <el-table-column label="关联类型" width="120">
            <template #default="{ row }">
              <span class="type-pill">{{ creditRelatedTypeLabel(row.relatedType) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="事由" min-width="140" show-overflow-tooltip />
          <el-table-column label="补充" width="72" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.supplementText || (row.evidenceUrls && row.evidenceUrls.length)" size="small" type="success" effect="light" round>有</el-tag>
              <span v-else class="text-muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="disputeTagType(row.status)" size="small" effect="light" round>
                {{ disputeStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="adminRemark" label="处理说明" min-width="120" show-overflow-tooltip />
          <el-table-column prop="createTime" label="时间" width="168">
            <template #default="{ row }">
              <span class="time-cell">{{ formatTime(row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right" align="center">
            <template #default="{ row }">
              <el-button v-if="canSupplement(row)" type="primary" text size="small" @click="openSupplement(row)">
                补充材料
              </el-button>
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
            @size-change="loadTab"
            @current-change="loadTab"
          />
        </div>
      </div>
    </div>

    <CreditDisputeDialog
      v-model="dlg"
      :load-counterparties="getStudentDisputeCounterparties"
      :load-relation-picks="getStudentDisputeRelationPicks"
      :submit-dispute="submitStudentCreditDispute"
      @success="onDisputeSuccess"
    />

    <el-dialog
      v-model="suppDlg"
      width="540px"
      destroy-on-close
      class="supplement-dialog"
      align-center
      @closed="resetSupp"
    >
      <template #header>
        <div class="dialog-header">
          <el-icon class="dialog-header-icon"><DocumentAdd /></el-icon>
          <div>
            <div class="dialog-header-title">补充争议材料</div>
            <div class="dialog-header-sub">待管理员处理前可多次补充</div>
          </div>
        </div>
      </template>
      <el-alert type="warning" :closable="false" show-icon class="supp-alert" title="支持图片或 PDF，单文件不超过 5MB" />
      <el-form label-position="top" class="supp-form">
        <el-form-item label="补充说明">
          <el-input v-model="suppText" type="textarea" :rows="4" placeholder="选填，说明新增证据或情况变化" />
        </el-form-item>
        <el-form-item label="证据文件">
          <el-upload v-model:file-list="suppFileList" multiple :auto-upload="false" accept="image/*,.pdf" drag class="supp-upload">
            <el-icon class="upload-icon"><UploadFilled /></el-icon>
            <div class="upload-text">拖拽文件到此处，或<em>点击上传</em></div>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="suppDlg = false">取消</el-button>
        <el-button type="primary" round :loading="suppLoading" @click="submitSupplement">提交补充</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { DocumentAdd, InfoFilled, Medal, Plus, UploadFilled } from '@element-plus/icons-vue'
import CreditDisputeDialog from '../components/CreditDisputeDialog.vue'
import { useUserStore } from '../store/user'
import {
  getStudentCredit,
  getStudentCreditAdjustLogs,
  getStudentCreditDisputes,
  getStudentDisputeCounterparties,
  getStudentDisputeRelationPicks,
  submitStudentCreditDispute,
  submitStudentDisputeSupplement
} from '../api/student'
import { creditRelatedTypeLabel, creditLevelLabel, disputeStatusLabel } from '../utils/enumLabel'

const userStore = useUserStore()
const myUserId = computed(() => userStore.userInfo?.userId)

const credit = ref(null)
const loadingProfile = ref(false)
const tab = ref('logs')
const disputeScope = ref('INITIATED')
const q = reactive({ current: 1, size: 10 })
const rows = reactive({ records: [], total: 0 })
const loading = ref(false)
const dlg = ref(false)

const suppDlg = ref(false)
const suppRow = ref(null)
const suppText = ref('')
const suppFileList = ref([])
const suppLoading = ref(false)

const formatTime = (v) => {
  if (!v) return '—'
  const s = String(v).replace('T', ' ')
  return s.length > 16 ? s.slice(0, 16) : s
}

const formatDelta = (d) => {
  if (d == null || d === '') return '—'
  const n = Number(d)
  if (Number.isNaN(n)) return String(d)
  return n > 0 ? `+${n}` : String(n)
}

const deltaClass = (d) => {
  const n = Number(d)
  if (Number.isNaN(n) || n === 0) return 'delta-zero'
  return n > 0 ? 'delta-up' : 'delta-down'
}

const levelTagType = (level) => {
  const map = { EXCELLENT: 'success', GOOD: 'primary', NORMAL: 'warning', RISKY: 'danger' }
  return map[level] || 'info'
}

const disputeTagType = (status) => {
  const map = { PENDING: 'warning', RESOLVED: 'success', REJECTED: 'danger' }
  return map[status] || 'info'
}

const canSupplement = (row) => {
  if (row.status !== 'PENDING') return false
  if (!myUserId.value || row.reporterUserId !== myUserId.value) return false
  return disputeScope.value === 'INITIATED' || disputeScope.value === 'ALL'
}

const openSupplement = (row) => {
  suppRow.value = row
  suppText.value = row.supplementText || ''
  suppFileList.value = []
  suppDlg.value = true
}

const resetSupp = () => {
  suppRow.value = null
  suppText.value = ''
  suppFileList.value = []
}

const submitSupplement = async () => {
  if (!suppRow.value) return
  suppLoading.value = true
  try {
    const fd = new FormData()
    if (suppText.value?.trim()) fd.append('supplementText', suppText.value.trim())
    for (const item of suppFileList.value) {
      const raw = item.raw
      if (raw) fd.append('files', raw)
    }
    await submitStudentDisputeSupplement(suppRow.value.id, fd)
    ElMessage.success('已提交')
    suppDlg.value = false
    await loadTab()
  } catch (e) {
    ElMessage.error(e.message || '失败')
  } finally {
    suppLoading.value = false
  }
}

const loadProfile = async () => {
  loadingProfile.value = true
  try {
    const res = await getStudentCredit()
    if (res.success) credit.value = res.data
  } finally {
    loadingProfile.value = false
  }
}

const loadTab = async () => {
  loading.value = true
  try {
    if (tab.value === 'logs') {
      const res = await getStudentCreditAdjustLogs(q)
      if (res.success && res.data) Object.assign(rows, res.data)
    } else {
      const res = await getStudentCreditDisputes({ ...q, scope: disputeScope.value })
      if (res.success && res.data) Object.assign(rows, res.data)
    }
  } finally {
    loading.value = false
  }
}

const onTab = () => {
  q.current = 1
  loadTab()
}

const onDisputeScopeChange = () => {
  q.current = 1
  loadTab()
}

const onDisputeSuccess = () => {
  if (tab.value === 'disputes') loadTab()
}

onMounted(async () => {
  await loadProfile()
  await loadTab()
})
</script>

<style scoped lang="scss">
.credit-page {
  margin: -8px -4px 0;
  min-height: 100%;
}

.page-hero {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #2e5a6b 0%, #3d8a9a 45%, #5bb8c8 100%);
  color: #fff;
}

.page-hero-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 90% 12%, rgba(255, 255, 255, 0.14), transparent 42%),
    radial-gradient(circle at 6% 88%, rgba(180, 240, 230, 0.2), transparent 48%);
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
    color: #c8f5ef;
  }
}

.page-hero-sub {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
  max-width: 400px;
  line-height: 1.55;
}

.score-panel {
  flex-shrink: 0;
  min-width: 140px;
  text-align: center;
  padding: 16px 24px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(8px);

  :deep(.el-empty__description) {
    color: rgba(255, 255, 255, 0.85);
  }
}

.score-num {
  font-size: 42px;
  font-weight: 800;
  line-height: 1;
}

.score-label {
  font-size: 13px;
  opacity: 0.85;
  margin: 6px 0 10px;
}

.page-body {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.table-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04), 0 10px 36px rgba(15, 23, 42, 0.06);
  border: 1px solid #eef1f6;
  overflow: hidden;
}

.tabs-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 20px 0;
  border-bottom: 1px solid #f0f3f8;
  flex-wrap: wrap;
  gap: 10px;
}

.styled-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 0;
  }
  :deep(.el-tabs__item) {
    font-weight: 500;
  }
  :deep(.el-tabs__active-bar) {
    background: #3d8a9a;
  }
  :deep(.el-tabs__item.is-active) {
    color: #3d8a9a;
  }
}

.tabs-actions {
  padding-bottom: 8px;
}

.dispute-toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 14px;
  padding: 14px 20px;
  background: #fafbfc;
  border-bottom: 1px solid #f0f3f8;
}

.scope-hint {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.data-table {
  :deep(.el-table__header th) {
    background: #f8fafc !important;
    font-weight: 600;
  }
}

.delta-cell {
  font-weight: 700;
  font-size: 14px;

  &.delta-up {
    color: #67c23a;
  }
  &.delta-down {
    color: #f56c6c;
  }
  &.delta-zero {
    color: #909399;
  }
}

.type-pill {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 6px;
  background: #e8f6f4;
  color: #3d8a9a;
}

.text-muted {
  color: #c0c4cc;
}

.time-cell {
  font-size: 13px;
  color: #606266;
}

.pager {
  padding: 14px 20px 18px;
  display: flex;
  justify-content: flex-end;
}

.dialog-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.dialog-header-icon {
  font-size: 26px;
  color: #3d8a9a;
}

.dialog-header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.dialog-header-sub {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.supp-alert {
  margin-bottom: 16px;
  border-radius: 10px;
}

.supp-form {
  :deep(.el-form-item__label) {
    font-weight: 500;
  }
}

.supp-upload {
  width: 100%;

  :deep(.el-upload-dragger) {
    border-radius: 12px;
    padding: 24px;
  }
}

.upload-icon {
  font-size: 40px;
  color: #c0c4cc;
  margin-bottom: 8px;
}

.upload-text {
  font-size: 14px;
  color: #606266;

  em {
    color: #3d8a9a;
    font-style: normal;
  }
}

@media (max-width: 768px) {
  .page-hero-inner {
    flex-direction: column;
    align-items: flex-start;
    padding: 20px;
  }

  .score-panel {
    width: 100%;
  }
}
</style>

<style lang="scss">
.supplement-dialog {
  .el-dialog__header {
    padding: 18px 22px 10px;
  }
  .el-dialog__footer {
    border-top: 1px solid #f0f3f8;
    padding: 12px 22px 18px;
  }
}
</style>
