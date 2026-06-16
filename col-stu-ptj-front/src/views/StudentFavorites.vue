<template>
  <div class="feature-page">
    <section class="page-hero page-hero--fav">
      <div class="page-hero-bg" aria-hidden="true" />
      <div class="page-hero-inner">
        <span class="page-hero-badge"><el-icon><Star /></el-icon> 收藏夹</span>
        <h1 class="page-hero-title">我的<span class="accent">收藏</span></h1>
        <p class="page-hero-sub">快速查看已收藏的兼职岗位，支持跳转详情或取消收藏。</p>
      </div>
    </section>

    <div class="feature-body">
      <div class="table-card">
        <div class="table-card-head">
          <span>收藏列表</span>
          <span v-if="rows.length" class="table-meta">共 {{ rows.length }} 条</span>
        </div>
        <el-table :data="rows" v-loading="loading" stripe class="data-table" empty-text="暂无收藏岗位">
          <el-table-column prop="jobTitle" label="岗位名称" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="job-title">{{ row.jobTitle }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="location" label="工作地点" min-width="120" show-overflow-tooltip />
          <el-table-column prop="salaryText" label="薪资" width="120" />
          <el-table-column prop="createTime" label="收藏时间" width="168">
            <template #default="{ row }">
              <span class="time-cell">{{ formatTime(row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right" align="center">
            <template #default="{ row }">
              <el-button size="small" text type="primary" @click="viewJob(row)">查看岗位</el-button>
              <el-button size="small" text type="danger" @click="unfav(row)">取消收藏</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star } from '@element-plus/icons-vue'
import { myFavorites, unfavoriteJob, getStudentJobDetail } from '../api/student'

const router = useRouter()
const rows = ref([])
const loading = ref(false)

const formatTime = (v) => {
  if (!v) return '—'
  const s = String(v).replace('T', ' ')
  return s.length > 16 ? s.slice(0, 16) : s
}

const load = async () => {
  loading.value = true
  try {
    const res = await myFavorites()
    if (!res.success || !Array.isArray(res.data)) {
      rows.value = []
      return
    }
    const list = await Promise.all(
      res.data.map(async (item) => {
        try {
          const d = await getStudentJobDetail(item.jobId)
          const job = d.success ? d.data : null
          return {
            ...item,
            jobTitle: job?.title || `岗位 #${item.jobId}`,
            location: job?.location || '—',
            salaryText: job?.salaryText || '—'
          }
        } catch {
          return {
            ...item,
            jobTitle: `岗位 #${item.jobId}`,
            location: '—',
            salaryText: '—'
          }
        }
      })
    )
    rows.value = list
  } finally {
    loading.value = false
  }
}

const viewJob = (row) => router.push(`/jobs/${row.jobId}`)

const unfav = async (row) => {
  try {
    await unfavoriteJob(row.jobId)
    ElMessage.success('已取消收藏')
    await load()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
@use '../styles/feature-page.scss';

.job-title {
  font-weight: 600;
  color: #303133;
}
</style>
