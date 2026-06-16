<template>
  <div class="feature-page">
    <section class="page-hero page-hero--admin">
      <div class="page-hero-bg" aria-hidden="true" />
      <div class="page-hero-inner">
        <span class="page-hero-badge"><el-icon><Medal /></el-icon> 信用治理</span>
        <h1 class="page-hero-title">信用<span class="accent">监管</span></h1>
        <p class="page-hero-sub">管理用户信用档案、人工调分，并处理平台争议工单。</p>
      </div>
    </section>

    <div class="feature-body">
      <div class="table-card">
        <div class="tabs-bar">
          <el-tabs v-model="activeTab" class="styled-tabs" @tab-change="onTabChange">
            <el-tab-pane label="信用档案" name="profiles" />
            <el-tab-pane label="争议工单" name="disputes" />
          </el-tabs>
        </div>

        <div v-if="activeTab === 'profiles'" class="tab-toolbar">
          <el-input
            v-model="profileKw"
            clearable
            placeholder="按用户名搜索"
            :prefix-icon="Search"
            class="kw-input"
            @keyup.enter="loadProfiles"
            @clear="loadProfiles"
          />
          <el-button type="primary" round :icon="Search" :loading="pLoading" @click="loadProfiles">查询</el-button>
        </div>

        <div v-else class="tab-toolbar">
          <el-select v-model="dQuery.status" clearable placeholder="全部状态" style="width: 160px" @change="loadDisputes">
            <el-option label="待处理" value="PENDING" />
            <el-option label="已处理" value="RESOLVED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </div>

        <el-table
          v-if="activeTab === 'profiles'"
          :data="profiles.records"
          v-loading="pLoading"
          stripe
          class="data-table credit-profiles-table"
        >
          <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip />
          <el-table-column prop="score" label="信用分" width="88" align="center" />
          <el-table-column label="等级" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="levelTagType(row.creditLevel)" size="small" effect="light" round>
                {{ creditLevelLabel(row.creditLevel) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="88" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" text @click="openAdjust(row)">调分</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-table v-else :data="disputes.records" v-loading="dLoading" stripe class="data-table">
          <el-table-column prop="id" label="工单号" width="80" />
          <el-table-column label="申诉人" width="110" show-overflow-tooltip>
            <template #default="{ row }">{{ row.reporterDisplayName || row.reporterUsername || '—' }}</template>
          </el-table-column>
          <el-table-column label="被诉方" width="110" show-overflow-tooltip>
            <template #default="{ row }">{{ row.targetDisplayName || row.targetUsername || '—' }}</template>
          </el-table-column>
          <el-table-column label="关联" width="110">
            <template #default="{ row }">
              <span class="type-pill">{{ creditRelatedTypeLabel(row.relatedType) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="事由" min-width="140" show-overflow-tooltip />
          <el-table-column label="状态" width="96" align="center">
            <template #default="{ row }">
              <el-tag :type="disputeTagType(row.status)" size="small" effect="light" round>
                {{ disputeStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" width="168">
            <template #default="{ row }"><span class="time-cell">{{ formatTime(row.createTime) }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right" align="center">
            <template #default="{ row }">
              <template v-if="row.status === 'PENDING'">
                <el-button type="success" size="small" text @click="resolveRow(row, 'RESOLVED')">处理</el-button>
                <el-button type="danger" size="small" text @click="resolveRow(row, 'REJECTED')">驳回</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>

        <div class="pager">
          <el-pagination
            v-if="activeTab === 'profiles'"
            background
            layout="total, sizes, prev, pager, next"
            :total="profiles.total"
            v-model:current-page="pQuery.current"
            v-model:page-size="pQuery.size"
            :page-sizes="[10, 20, 50]"
            @current-change="loadProfiles"
            @size-change="loadProfiles"
          />
          <el-pagination
            v-else
            background
            layout="total, sizes, prev, pager, next"
            :total="disputes.total"
            v-model:current-page="dQuery.current"
            v-model:page-size="dQuery.size"
            :page-sizes="[10, 20, 50]"
            @current-change="loadDisputes"
            @size-change="loadDisputes"
          />
        </div>
      </div>
    </div>

    <el-dialog v-model="adjustVisible" width="460px" destroy-on-close class="admin-dialog" align-center @close="resetAdjust">
      <template #header>
        <div class="dlg-head">
          <el-icon class="dlg-icon"><Edit /></el-icon>
          <div>
            <div class="dlg-title">人工调整信用分</div>
            <div class="dlg-sub">用户：{{ adjustRow?.username }}</div>
          </div>
        </div>
      </template>
      <el-form label-position="top">
        <el-form-item label="变化分">
          <el-input-number v-model="adjustForm.delta" :min="-100" :max="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="调整原因" required>
          <el-input v-model="adjustForm.reason" type="textarea" :rows="3" placeholder="请填写调分原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="adjustVisible = false">取消</el-button>
        <el-button type="primary" round @click="submitAdjust">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Medal, Search } from '@element-plus/icons-vue'
import { listCreditProfiles, adjustCredit, listCreditDisputes, resolveCreditDispute } from '../../api/admin'
import { creditRelatedTypeLabel, creditLevelLabel, disputeStatusLabel } from '../../utils/enumLabel'

const formatTime = (v) => {
  if (!v) return '—'
  const s = String(v).replace('T', ' ')
  return s.length > 16 ? s.slice(0, 16) : s
}

const levelTagType = (l) => {
  const m = { EXCELLENT: 'success', GOOD: 'primary', NORMAL: 'warning', RISKY: 'danger' }
  return m[l] || 'info'
}

const disputeTagType = (s) => {
  const m = { PENDING: 'warning', RESOLVED: 'success', REJECTED: 'danger' }
  return m[s] || 'info'
}

const activeTab = ref('profiles')
const profileKw = ref('')
const pLoading = ref(false)
const pQuery = reactive({ current: 1, size: 10 })
const profiles = reactive({ records: [], total: 0 })

const dLoading = ref(false)
const dQuery = reactive({ current: 1, size: 10, status: '' })
const disputes = reactive({ records: [], total: 0 })

const adjustVisible = ref(false)
const adjustRow = ref(null)
const adjustForm = reactive({ delta: 0, reason: '' })

const onTabChange = () => {
  if (activeTab.value === 'profiles') loadProfiles()
  else loadDisputes()
}

const loadProfiles = async () => {
  pLoading.value = true
  try {
    const res = await listCreditProfiles({
      current: pQuery.current,
      size: pQuery.size,
      keyword: profileKw.value || undefined
    })
    if (res.success && res.data) {
      profiles.records = res.data.records || []
      profiles.total = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    pLoading.value = false
  }
}

const loadDisputes = async () => {
  dLoading.value = true
  try {
    const res = await listCreditDisputes({
      current: dQuery.current,
      size: dQuery.size,
      status: dQuery.status || undefined
    })
    if (res.success && res.data) {
      disputes.records = res.data.records || []
      disputes.total = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    dLoading.value = false
  }
}

const openAdjust = (row) => {
  adjustRow.value = row
  adjustForm.delta = 0
  adjustForm.reason = ''
  adjustVisible.value = true
}

const resetAdjust = () => {
  adjustRow.value = null
}

const submitAdjust = async () => {
  if (!adjustForm.reason?.trim()) {
    ElMessage.warning('请填写原因')
    return
  }
  try {
    await adjustCredit({
      userId: adjustRow.value.userId,
      delta: adjustForm.delta,
      reason: adjustForm.reason
    })
    ElMessage.success('已保存')
    adjustVisible.value = false
    await loadProfiles()
  } catch (e) {
    ElMessage.error(e.message || '失败')
  }
}

const resolveRow = async (row, action) => {
  try {
    const { value } = await ElMessageBox.prompt(
      action === 'RESOLVED' ? '处理说明' : '驳回说明',
      '填写备注',
      { confirmButtonText: '确定', cancelButtonText: '取消', inputPattern: /.+/ }
    )
    await resolveCreditDispute(row.id, { action, adminRemark: value })
    ElMessage.success('已提交')
    await loadDisputes()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '失败')
  }
}

onMounted(() => {
  loadProfiles()
  loadDisputes()
})
</script>

<style scoped lang="scss">
@use '../../styles/feature-page.scss';

.tab-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  background: #fafbfc;
  border-bottom: 1px solid #f0f3f8;
}

.kw-input {
  max-width: 280px;
}

.type-pill {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 6px;
  background: #eef2f8;
  color: #3d5a80;
}

.dlg-head {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.dlg-icon {
  font-size: 26px;
  color: #3d5a80;
}

.dlg-title {
  font-size: 16px;
  font-weight: 600;
}

.dlg-sub {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

:deep(.credit-profiles-table .el-table__cell) {
  word-break: break-all;
}
</style>

<style lang="scss">
.admin-dialog .el-dialog__footer {
  border-top: 1px solid #f0f3f8;
}
</style>
