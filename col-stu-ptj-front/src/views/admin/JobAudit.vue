<template>
  <div class="feature-page">
    <section class="page-hero page-hero--admin">
      <div class="page-hero-bg" aria-hidden="true" />
      <div class="page-hero-inner page-hero-inner--split">
        <div>
          <span class="page-hero-badge"><el-icon><DocumentChecked /></el-icon> 内容审核</span>
          <h1 class="page-hero-title">岗位<span class="accent">审核</span></h1>
          <p class="page-hero-sub">审核企业提交的兼职岗位，通过后学生端可见。</p>
        </div>
        <span v-if="table.total" class="hero-stat">待审 {{ table.total }} 个</span>
      </div>
    </section>

    <div class="feature-body">
      <div class="table-card">
        <div class="table-card-head">
          <span>待审核岗位</span>
          <span class="table-meta">共 {{ table.total }} 条</span>
        </div>
        <el-table :data="table.records" v-loading="loading" stripe class="data-table" empty-text="暂无待审岗位">
          <el-table-column prop="title" label="标题" min-width="140" show-overflow-tooltip>
            <template #default="{ row }"><span class="job-title">{{ row.title }}</span></template>
          </el-table-column>
          <el-table-column label="企业" min-width="120" show-overflow-tooltip>
            <template #default="{ row }">{{ row.publisherCompanyName || '—' }}</template>
          </el-table-column>
          <el-table-column prop="location" label="地点" width="110" show-overflow-tooltip />
          <el-table-column prop="salaryText" label="薪资" width="100" />
          <el-table-column label="状态" width="96" align="center">
            <template #default="{ row }">
              <el-tag type="warning" size="small" effect="light" round>{{ jobStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="描述" min-width="160">
            <template #default="{ row }"><el-text line-clamp="2">{{ row.description }}</el-text></template>
          </el-table-column>
          <el-table-column prop="createTime" label="提交时间" width="168">
            <template #default="{ row }"><span class="time-cell">{{ formatTime(row.createTime) }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right" align="center">
            <template #default="{ row }">
              <el-button type="success" size="small" text @click="onApprove(row)">通过</el-button>
              <el-button type="danger" size="small" text @click="onReject(row)">驳回</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pager">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next"
            :total="table.total"
            v-model:current-page="query.current"
            v-model:page-size="query.size"
            :page-sizes="[10, 20, 50]"
            @current-change="load"
            @size-change="load"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DocumentChecked } from '@element-plus/icons-vue'
import { getPendingJobs, approveJob, rejectJob } from '../../api/admin'
import { jobStatusLabel } from '../../utils/enumLabel'

const formatTime = (v) => {
  if (!v) return '—'
  const s = String(v).replace('T', ' ')
  return s.length > 16 ? s.slice(0, 16) : s
}

const loading = ref(false)
const query = reactive({ current: 1, size: 10 })
const table = reactive({ records: [], total: 0 })

const load = async () => {
  loading.value = true
  try {
    const res = await getPendingJobs({ current: query.current, size: query.size })
    if (res.success && res.data) {
      table.records = res.data.records || []
      table.total = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const onApprove = async (row) => {
  try {
    await ElMessageBox.confirm(`确认通过岗位「${row.title}」？`, '提示', { type: 'warning' })
    await approveJob(row.id)
    ElMessage.success('已审核通过')
    await load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

const onReject = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回岗位', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '原因不能为空'
    })
    await rejectJob(row.id, value)
    ElMessage.success('已驳回')
    await load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
@use '../../styles/feature-page.scss';

.hero-stat {
  font-size: 15px;
  font-weight: 600;
  padding: 10px 16px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.12);
}

.job-title {
  font-weight: 600;
  color: #303133;
}
</style>
