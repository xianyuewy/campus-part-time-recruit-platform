<template>
  <div class="feature-page">
    <section class="page-hero page-hero--admin">
      <div class="page-hero-bg" aria-hidden="true" />
      <div class="page-hero-inner">
        <span class="page-hero-badge"><el-icon><Files /></el-icon> 全平台岗位</span>
        <h1 class="page-hero-title">岗位<span class="accent">管理</span></h1>
        <p class="page-hero-sub">查看并管理全平台兼职岗位，支持按状态、关键词筛选与强制下架。</p>
      </div>
    </section>

    <div class="feature-body">
      <div class="toolbar-card">
        <div class="toolbar-head">
          <el-icon class="toolbar-head-icon"><Filter /></el-icon>
          <span>筛选查询</span>
        </div>
        <form class="filter-bar" @submit.prevent="onSearch">
          <div class="filter-field">
            <label>状态</label>
            <el-select v-model="query.status" clearable placeholder="全部" style="width: 130px">
              <el-option label="待审核" value="PENDING" />
              <el-option label="已上架" value="APPROVED" />
              <el-option label="已驳回" value="REJECTED" />
              <el-option label="已下架" value="OFFLINE" />
            </el-select>
          </div>
          <div class="filter-field filter-field--kw">
            <label>关键词</label>
            <el-input v-model="query.keyword" clearable placeholder="标题/地点" :prefix-icon="Search" @keyup.enter="onSearch" />
          </div>
          <div class="filter-actions">
            <el-button type="primary" round :icon="Search" :loading="loading" native-type="submit">查询</el-button>
            <el-button round :disabled="loading" @click.prevent="onReset">重置</el-button>
          </div>
        </form>
      </div>

      <div class="table-card">
        <div class="table-card-head">
          <span>岗位列表</span>
          <span class="table-meta">共 {{ table.total }} 条</span>
        </div>
        <el-table :data="table.records" v-loading="loading" stripe class="data-table" empty-text="暂无岗位">
          <el-table-column prop="title" label="标题" min-width="140" show-overflow-tooltip>
            <template #default="{ row }"><span class="job-title">{{ row.title }}</span></template>
          </el-table-column>
          <el-table-column prop="location" label="地点" width="110" show-overflow-tooltip />
          <el-table-column label="状态" width="96" align="center">
            <template #default="{ row }">
              <el-tag :type="jobTagType(row.status)" size="small" effect="light" round>{{ jobStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="企业" min-width="120" show-overflow-tooltip>
            <template #default="{ row }">{{ row.publisherCompanyName || '—' }}</template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="168">
            <template #default="{ row }"><span class="time-cell">{{ formatTime(row.createTime) }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="90" fixed="right" align="center">
            <template #default="{ row }">
              <el-button type="warning" size="small" text :disabled="row.status === 'OFFLINE'" @click="onOffline(row)">下架</el-button>
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
import { Files, Filter, Search } from '@element-plus/icons-vue'
import { listJobs, offlineJob } from '../../api/admin'
import { jobStatusLabel } from '../../utils/enumLabel'

const formatTime = (v) => {
  if (!v) return '—'
  const s = String(v).replace('T', ' ')
  return s.length > 16 ? s.slice(0, 16) : s
}

const jobTagType = (s) => {
  const m = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', OFFLINE: 'info' }
  return m[s] || 'info'
}

const loading = ref(false)
const query = reactive({ current: 1, size: 10, status: '', keyword: '' })
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

const onSearch = () => {
  query.current = 1
  load()
}

const onReset = () => {
  query.status = ''
  query.keyword = ''
  query.current = 1
  load()
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
@use '../../styles/feature-page.scss';

.filter-field--kw {
  flex: 1 1 200px;
  min-width: 160px;
  max-width: 280px;
}

.job-title {
  font-weight: 600;
  color: #303133;
}
</style>
