<template>
  <div>
    <h2>学生-信用中心</h2>
    <el-card v-loading="loadingProfile" class="mb16">
      <template v-if="credit">
        <el-descriptions title="我的信用档案" :column="2" border>
          <el-descriptions-item label="信用分">{{ credit.score ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="等级">{{ creditLevelLabel(credit.creditLevel) }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <el-empty v-else description="暂无信用档案" />
    </el-card>
    <el-card>
      <el-tabs v-model="tab" @tab-change="onTab">
        <el-tab-pane label="调整记录" name="logs" />
        <el-tab-pane label="争议工单" name="disputes" />
      </el-tabs>
      <div v-if="tab === 'disputes'" class="dispute-toolbar">
        <el-radio-group v-model="disputeScope" size="small" @change="onDisputeScopeChange">
          <el-radio-button label="INITIATED">我发起的</el-radio-button>
          <el-radio-button label="RECEIVED">针对我的</el-radio-button>
          <el-radio-button label="ALL">全部</el-radio-button>
        </el-radio-group>
        <span class="scope-hint">「针对我的」含企业/学生将您列为被诉方的工单</span>
      </div>
      <el-table v-if="tab === 'logs'" :data="rows.records" v-loading="loading" border>
        <el-table-column prop="delta" label="变动" width="90" />
        <el-table-column prop="reason" label="原因" min-width="200" />
        <el-table-column prop="adminUsername" label="操作人" width="120" />
        <el-table-column prop="createTime" label="时间" width="180" />
      </el-table>
      <el-table v-else :data="rows.records" v-loading="loading" border>
        <el-table-column prop="id" label="工单号" width="90" />
        <el-table-column label="申诉人" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.reporterDisplayName || row.reporterUsername || '—' }}</template>
        </el-table-column>
        <el-table-column label="被诉方" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.targetDisplayName || row.targetUsername || '—' }}</template>
        </el-table-column>
        <el-table-column label="关联类型" width="120">
          <template #default="{ row }">{{ creditRelatedTypeLabel(row.relatedType) }}</template>
        </el-table-column>
        <el-table-column prop="relatedId" label="关联ID" width="90" />
        <el-table-column prop="reason" label="事由" min-width="160" show-overflow-tooltip />
        <el-table-column label="补充" width="88">
          <template #default="{ row }">
            <el-tag v-if="row.supplementText || (row.evidenceUrls && row.evidenceUrls.length)" size="small" type="success">有</el-tag>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">{{ disputeStatusLabel(row.status) }}</template>
        </el-table-column>
        <el-table-column prop="adminRemark" label="处理说明" min-width="120" show-overflow-tooltip />
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="canSupplement(row)"
              type="primary"
              link
              size="small"
              @click="openSupplement(row)"
            >补充材料</el-button>
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
          @current-change="loadTab"
        />
      </div>
      <div class="mt16">
        <el-button type="primary" @click="dlg = true">发起争议</el-button>
      </div>
    </el-card>
    <CreditDisputeDialog
      v-model="dlg"
      :load-counterparties="getStudentDisputeCounterparties"
      :load-relation-picks="getStudentDisputeRelationPicks"
      :submit-dispute="submitStudentCreditDispute"
      @success="onDisputeSuccess"
    />

    <el-dialog v-model="suppDlg" title="补充争议材料" width="520px" destroy-on-close @closed="resetSupp">
      <p class="supp-tip">待管理员处理前可多次补充。支持图片或 PDF，单文件不超过 5MB。</p>
      <el-input v-model="suppText" type="textarea" rows="4" placeholder="补充说明（选填）" class="mb12" />
      <el-upload
        v-model:file-list="suppFileList"
        multiple
        :auto-upload="false"
        accept="image/*,.pdf"
        list-type="text"
      >
        <el-button type="default">选择证据文件</el-button>
      </el-upload>
      <template #footer>
        <el-button @click="suppDlg = false">取消</el-button>
        <el-button type="primary" :loading="suppLoading" @click="submitSupplement">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
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

<style scoped>
.mb16 {
  margin-bottom: 16px;
}
.mt16 {
  margin-top: 16px;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.dispute-toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
}
.scope-hint {
  font-size: 12px;
  color: #909399;
}
.mb12 {
  margin-bottom: 12px;
}
.supp-tip {
  font-size: 13px;
  color: #606266;
  margin: 0 0 12px;
  line-height: 1.5;
}
</style>
