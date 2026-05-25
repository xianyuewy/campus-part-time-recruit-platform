<template>
  <div>
    <h2>企业-评价中心</h2>
    <el-card>
      <el-tabs v-model="tab" @tab-change="load">
        <el-tab-pane label="我发出的评价" name="given" />
        <el-tab-pane label="我收到的评价" name="received" />
      </el-tabs>
      <el-table :data="rows.records" v-loading="loading" border>
        <el-table-column prop="applicationId" label="申请ID" width="150" />
        <el-table-column label="评价人" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.fromDisplayName || row.fromUsername || '—' }}</template>
        </el-table-column>
        <el-table-column label="被评价人" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.toDisplayName || row.toUsername || '—' }}</template>
        </el-table-column>
        <el-table-column prop="score" label="评分" width="90" />
        <el-table-column prop="content" label="评价内容" min-width="220" />
        <el-table-column prop="createTime" label="时间" width="180" />
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { companyGivenReviews, companyReceivedReviews } from '../api/company'

const tab = ref('given')
const q = reactive({ current: 1, size: 10 })
const rows = reactive({ records: [], total: 0 })
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    const api = tab.value === 'given' ? companyGivenReviews : companyReceivedReviews
    const res = await api(q)
    if (res.success && res.data) Object.assign(rows, res.data)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.pager { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
