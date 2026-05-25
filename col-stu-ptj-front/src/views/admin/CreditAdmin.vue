<template>
  <div>
    <h2 class="page-title">信用监管</h2>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="信用档案" name="profiles">
        <el-card class="mb16">
          <el-input
            v-model="profileKw"
            clearable
            placeholder="按用户名搜索"
            style="width: 240px; margin-right: 12px"
            @clear="loadProfiles"
          />
          <el-button type="primary" @click="loadProfiles">查询</el-button>
        </el-card>
        <el-card>
          <el-table
            :data="profiles.records"
            v-loading="pLoading"
            border
            table-layout="fixed"
            class="credit-profiles-table"
            style="width: 100%"
          >
            <el-table-column prop="userId" label="用户ID" min-width="200" show-overflow-tooltip align="left" />
            <el-table-column prop="username" label="用户名" min-width="130" show-overflow-tooltip align="left" />
            <el-table-column prop="score" label="信用分" width="88" align="center" />
            <el-table-column label="等级" width="100" align="center">
              <template #default="{ row }">
                <el-tag size="small" type="info" effect="plain">{{ creditLevelLabel(row.creditLevel) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="96" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click="openAdjust(row)">调分</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pager">
            <el-pagination
              background
              layout="prev, pager, next, total"
              :total="profiles.total"
              v-model:current-page="pQuery.current"
              v-model:page-size="pQuery.size"
              @current-change="loadProfiles"
            />
          </div>
        </el-card>
      </el-tab-pane>
      <el-tab-pane label="争议工单" name="disputes">
        <el-card class="mb16">
          <el-select v-model="dQuery.status" clearable placeholder="全部状态" style="width: 160px" @change="loadDisputes">
            <el-option label="待处理" value="PENDING" />
            <el-option label="已处理" value="RESOLVED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-card>
        <el-card>
          <el-table
            :data="disputes.records"
            v-loading="dLoading"
            border
            table-layout="fixed"
            style="width: 100%"
          >
            <el-table-column prop="id" label="ID" width="160" />
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
            <el-table-column prop="reason" label="事由" min-width="180" show-overflow-tooltip />
            <el-table-column prop="supplementText" label="补充说明" min-width="160" show-overflow-tooltip />
            <el-table-column label="证据材料" min-width="200">
              <template #default="{ row }">
                <template v-if="row.evidenceUrls && row.evidenceUrls.length">
                  <div v-for="(u, i) in row.evidenceUrls" :key="i" class="ev-link">
                    <el-link :href="resolveUploadUrl(u)" target="_blank" type="primary">附件{{ i + 1 }}</el-link>
                  </div>
                </template>
                <span v-else>—</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">{{ disputeStatusLabel(row.status) }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 'PENDING'"
                  type="success"
                  size="small"
                  @click="resolveRow(row, 'RESOLVED')"
                >处理</el-button>
                <el-button
                  v-if="row.status === 'PENDING'"
                  type="danger"
                  size="small"
                  @click="resolveRow(row, 'REJECTED')"
                >驳回</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pager">
            <el-pagination
              background
              layout="prev, pager, next, total"
              :total="disputes.total"
              v-model:current-page="dQuery.current"
              v-model:page-size="dQuery.size"
              @current-change="loadDisputes"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="adjustVisible" title="人工调整信用分" width="420px" @close="resetAdjust">
      <el-form label-width="80px">
        <el-form-item label="用户"><span>{{ adjustRow?.username }}</span></el-form-item>
        <el-form-item label="变化分">
          <el-input-number v-model="adjustForm.delta" :min="-100" :max="100" />
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="adjustForm.reason" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdjust">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listCreditProfiles,
  adjustCredit,
  listCreditDisputes,
  resolveCreditDispute
} from '../../api/admin'
import { API_BASE_URL } from '../../utils/request'
import { creditRelatedTypeLabel, creditLevelLabel, disputeStatusLabel } from '../../utils/enumLabel'

const resolveUploadUrl = (u) => {
  if (!u) return '#'
  const s = String(u).trim()
  if (s.startsWith('http://') || s.startsWith('https://')) return s
  const base = API_BASE_URL.replace(/\/$/, '')
  const path = s.startsWith('/') ? s : `/${s}`
  return `${base}${path}`
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
.page-title {
  margin: 0 0 16px;
}
.mb16 {
  margin-bottom: 16px;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.ev-link {
  line-height: 1.6;
}
/* 长数字用户 ID 在固定布局下与表头对齐，避免错列感 */
:deep(.credit-profiles-table .el-table__cell) {
  word-break: break-all;
}
</style>
