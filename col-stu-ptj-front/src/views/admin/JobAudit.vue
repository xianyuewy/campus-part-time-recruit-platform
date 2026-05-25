<template>
  <div>
    <h2 class="page-title">岗位审核</h2>
    <el-card>
      <el-table :data="table.records" v-loading="loading" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="160" />
        <el-table-column label="企业名称" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.publisherCompanyName || '—' }}</template>
        </el-table-column>
        <el-table-column label="发布者昵称" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.publisherNickname || '—' }}</template>
        </el-table-column>
        <el-table-column label="登录账号" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.publisherUsername || '—' }}</template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="location" label="地点" width="120" />
        <el-table-column prop="salaryText" label="薪资" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">{{ jobStatusLabel(row.status) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="描述" min-width="200">
          <template #default="{ row }">
            <el-text line-clamp="2">{{ row.description }}</el-text>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="onApprove(row)">通过</el-button>
            <el-button type="danger" size="small" @click="onReject(row)">驳回</el-button>
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
import { getPendingJobs, approveJob, rejectJob } from '../../api/admin'
import { jobStatusLabel } from '../../utils/enumLabel'

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
.page-title {
  margin: 0 0 16px;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
