<template>
  <div class="feature-page">
    <section class="page-hero page-hero--admin">
      <div class="page-hero-bg" aria-hidden="true" />
      <div class="page-hero-inner">
        <span class="page-hero-badge"><el-icon><Avatar /></el-icon> 账号运维</span>
        <h1 class="page-hero-title">用户<span class="accent">管理</span></h1>
        <p class="page-hero-sub">按角色、状态与关键词筛选全平台用户，支持启用或停用账号。</p>
      </div>
    </section>

    <div class="feature-body">
      <div class="toolbar-card">
        <div class="toolbar-head">
          <el-icon class="toolbar-head-icon"><Search /></el-icon>
          <span>筛选查询</span>
        </div>
        <form class="filter-bar" @submit.prevent="onSearch">
          <div class="filter-field">
            <label>角色</label>
            <el-select v-model="query.role" clearable placeholder="全部" style="width: 120px">
              <el-option label="学生" value="STUDENT" />
              <el-option label="企业" value="COMPANY" />
              <el-option label="管理员" value="ADMIN" />
            </el-select>
          </div>
          <div class="filter-field">
            <label>状态</label>
            <el-select v-model="query.accountEnabled" clearable placeholder="全部" style="width: 120px">
              <el-option label="启用" :value="true" />
              <el-option label="停用" :value="false" />
            </el-select>
          </div>
          <div class="filter-field filter-field--kw">
            <label>关键词</label>
            <el-input v-model="query.keyword" clearable placeholder="用户名/邮箱" :prefix-icon="Search" @keyup.enter="onSearch" />
          </div>
          <div class="filter-actions">
            <el-button type="primary" round :icon="Search" :loading="loading" native-type="submit">查询</el-button>
            <el-button round :disabled="loading" @click.prevent="onReset">重置</el-button>
          </div>
        </form>
      </div>

      <div class="table-card">
        <div class="table-card-head">
          <span>用户列表</span>
          <span class="table-meta">共 {{ table.total }} 条</span>
        </div>
        <el-table :data="table.records" v-loading="loading" stripe class="data-table" empty-text="暂无用户">
          <el-table-column prop="username" label="用户名" width="120" show-overflow-tooltip />
          <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
          <el-table-column label="角色" width="88" align="center">
            <template #default="{ row }"><el-tag size="small" effect="light">{{ userRoleLabel(row.role) }}</el-tag></template>
          </el-table-column>
          <el-table-column label="认证" width="96" align="center">
            <template #default="{ row }">
              <el-tag :type="authTagType(row.authStatus)" size="small" effect="light" round>{{ authStatusLabel(row.authStatus) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="账号" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.accountEnabled ? 'success' : 'danger'" size="small" effect="light" round>
                {{ row.accountEnabled ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="注册时间" width="168">
            <template #default="{ row }"><span class="time-cell">{{ formatTime(row.createTime) }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right" align="center">
            <template #default="{ row }">
              <el-button v-if="row.accountEnabled" type="warning" size="small" text :disabled="row.role === 'ADMIN'" @click="onDisable(row)">停用</el-button>
              <el-button v-else type="success" size="small" text @click="onEnable(row)">启用</el-button>
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
import { Avatar, Search } from '@element-plus/icons-vue'
import { listUsers, disableUser, enableUser } from '../../api/admin'
import { authStatusLabel, userRoleLabel } from '../../utils/enumLabel'

const formatTime = (v) => {
  if (!v) return '—'
  const s = String(v).replace('T', ' ')
  return s.length > 16 ? s.slice(0, 16) : s
}

const authTagType = (s) => {
  const m = { APPROVED: 'success', PENDING: 'warning', REJECTED: 'danger' }
  return m[s] || 'info'
}

const loading = ref(false)
const query = reactive({ current: 1, size: 10, role: '', keyword: '', accountEnabled: undefined })
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

const onSearch = () => {
  query.current = 1
  load()
}

const onReset = () => {
  query.role = ''
  query.keyword = ''
  query.accountEnabled = undefined
  query.current = 1
  load()
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
@use '../../styles/feature-page.scss';

.filter-field--kw {
  flex: 1 1 200px;
  min-width: 160px;
  max-width: 260px;
}
</style>
