<template>
  <div class="page">
    <h2 class="page-title">我的收藏</h2>
    <el-card>
      <el-table :data="rows" v-loading="loading" border>
        <el-table-column prop="jobTitle" label="岗位名称" min-width="220" show-overflow-tooltip />
        <el-table-column prop="location" label="工作地点" min-width="140" show-overflow-tooltip />
        <el-table-column prop="salaryText" label="薪资" width="140" />
        <el-table-column prop="createTime" label="收藏时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="viewJob(row)">查看岗位</el-button>
            <el-button size="small" type="danger" @click="unfav(row)">取消收藏</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无收藏岗位" />
        </template>
      </el-table>
    </el-card>
  </div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { myFavorites, unfavoriteJob, getStudentJobDetail } from '../api/student'

const router = useRouter()
const rows = ref([])
const loading = ref(false)

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

const viewJob = (row) => {
  router.push(`/jobs/${row.jobId}`)
}

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

<style scoped>
.page-title {
  margin: 0 0 16px;
}
</style>