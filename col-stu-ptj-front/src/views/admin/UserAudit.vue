<template>
  <div>
    <h2 class="page-title">用户审核</h2>
    <el-card>
      <el-table :data="table.records" v-loading="loading" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="160" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="160" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">{{ userRoleLabel(row.role) }}</template>
        </el-table-column>
        <el-table-column label="认证状态" width="100">
          <template #default="{ row }">{{ authStatusLabel(row.authStatus) }}</template>
        </el-table-column>
        <el-table-column label="账号" width="90">
          <template #default="{ row }">
            <el-tag :type="row.accountEnabled ? 'success' : 'danger'" size="small">
              {{ row.accountEnabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="认证材料" min-width="200">
          <template #default="{ row }">
            <div v-if="row.idCard" class="att-cell">
              <el-image
                v-if="isImageAttachment(row.idCard)"
                :src="fileUrl(row.idCard)"
                :preview-src-list="[fileUrl(row.idCard)]"
                fit="cover"
                class="att-thumb"
                preview-teleported
                :hide-on-click-modal="true"
              />
              <el-link type="primary" :href="fileUrl(row.idCard)" target="_blank" rel="noopener">
                {{ isImageAttachment(row.idCard) ? '预览/下载' : '打开附件' }}
              </el-link>
            </div>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
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
import { getPendingUsers, approveUser, rejectUser } from '../../api/admin'
import { API_BASE_URL } from '../../utils/request'
import { authStatusLabel, userRoleLabel } from '../../utils/enumLabel'

const fileUrl = (path) => {
  if (!path) return '#'
  const p = String(path).trim()
  if (p.startsWith('http://') || p.startsWith('https://')) return p
  return API_BASE_URL + (p.startsWith('/') ? p : `/${p}`)
}

const isImageAttachment = (path) => {
  if (!path) return false
  return /\.(jpe?g|png|gif|webp|bmp|svg)$/i.test(String(path).split('?')[0])
}

const loading = ref(false)
const query = reactive({ current: 1, size: 10 })
const table = reactive({ records: [], total: 0 })

const load = async () => {
  loading.value = true
  try {
    const res = await getPendingUsers({ current: query.current, size: query.size })
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
    await ElMessageBox.confirm(`确认通过用户「${row.username}」的认证？`, '提示', { type: 'warning' })
    await approveUser(row.id)
    ElMessage.success('已审核通过')
    await load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

const onReject = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回认证', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '原因不能为空'
    })
    await rejectUser(row.id, value)
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
.text-muted {
  color: #909399;
}
.att-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.att-thumb {
  width: 48px;
  height: 48px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
  cursor: zoom-in;
}
</style>
