<template>
  <div>
    <h2 class="page-title">用户管理</h2>
    <el-card class="mb16">
      <el-form :inline="true" @submit.prevent="load">
        <el-form-item label="角色">
          <el-select v-model="query.role" clearable placeholder="全部" style="width: 120px">
            <el-option label="学生" value="STUDENT" />
            <el-option label="企业" value="COMPANY" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.accountEnabled" clearable placeholder="全部" style="width: 120px">
            <el-option label="启用" :value="true" />
            <el-option label="停用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" clearable placeholder="用户名/邮箱" style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card>
      <el-table :data="table.records" v-loading="loading" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="160" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="160" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">{{ userRoleLabel(row.role) }}</template>
        </el-table-column>
        <el-table-column label="认证" width="100">
          <template #default="{ row }">{{ authStatusLabel(row.authStatus) }}</template>
        </el-table-column>
        <el-table-column label="账号" width="90">
          <template #default="{ row }">
            <el-tag :type="row.accountEnabled ? 'success' : 'danger'" size="small">
              {{ row.accountEnabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.accountEnabled"
              type="warning"
              size="small"
              :disabled="row.role === 'ADMIN'"
              @click="onDisable(row)"
            >停用</el-button>
            <el-button v-else type="success" size="small" @click="onEnable(row)">启用</el-button>
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
import { listUsers, disableUser, enableUser } from '../../api/admin'
import { authStatusLabel, userRoleLabel } from '../../utils/enumLabel'

const loading = ref(false)
const query = reactive({
  current: 1,
  size: 10,
  role: '',
  keyword: '',
  accountEnabled: undefined
})
const table = reactive({ records: [], total: 0 })

const load = async () => {
  loading.value = true
  try {
    const params = {
      current: query.current,
      size: query.size,
      keyword: query.keyword || undefined,
      role: query.role || undefined
    }
    if (query.accountEnabled === true || query.accountEnabled === false) {
      params.accountEnabled = query.accountEnabled
    }
    const res = await listUsers(params)
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

const onDisable = async (row) => {
  try {
    await ElMessageBox.confirm(`确定停用用户「${row.username}」？`, '提示', { type: 'warning' })
    await disableUser(row.id)
    ElMessage.success('已停用')
    await load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

const onEnable = async (row) => {
  try {
    await enableUser(row.id)
    ElMessage.success('已启用')
    await load()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
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
