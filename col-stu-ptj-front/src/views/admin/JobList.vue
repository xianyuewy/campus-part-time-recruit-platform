<template>
  <div>
    <h2 class="page-title">岗位管理</h2>
    <el-card class="mb16">
      <el-form :inline="true" @submit.prevent="load">
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 140px">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已上架" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
            <el-option label="已下架" value="OFFLINE" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" clearable placeholder="标题/地点" style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card>
      <el-table :data="table.records" v-loading="loading" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="160" />
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="location" label="地点" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">{{ jobStatusLabel(row.status) }}</template>
        </el-table-column>
        <el-table-column label="企业名称" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.publisherCompanyName || '—' }}</template>
        </el-table-column>
        <el-table-column label="发布者昵称" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.publisherNickname || '—' }}</template>
        </el-table-column>
        <el-table-column label="登录账号" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.publisherUsername || '—' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              type="warning"
              size="small"
              :disabled="row.status === 'OFFLINE'"
              @click="onOffline(row)"
            >下架</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          background
          layout="prev, pager, next, sizes, total"
          :total="table.total"
          v-model:current-page="query.current"
          v-model:page-size="query.size"
          :page-sizes="[10, 20, 50]"
          @current-change="load"
          @size-change="load"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listJobs, offlineJob } from '../../api/admin'
import { jobStatusLabel } from '../../utils/enumLabel'

const loading = ref(false)
const query = reactive({
  current: 1,
  size: 10,
  status: '',
  keyword: ''
})
const table = reactive({ records: [], total: 0 })

const load = async () => {
  loading.value = true
  try {
    const res = await listJobs({
      current: query.current,
      size: query.size,
      status: query.status || undefined,
      keyword: query.keyword || undefined
    })
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

const onOffline = async (row) => {
  try {
    await ElMessageBox.confirm(`确定下架「${row.title}」？`, '提示', { type: 'warning' })
    await offlineJob(row.id)
    ElMessage.success('已下架')
    await load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

onMounted(load)
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
</style>
