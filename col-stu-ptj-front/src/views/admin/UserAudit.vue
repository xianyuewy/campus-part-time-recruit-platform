<template>
  <div class="feature-page">
    <section class="page-hero page-hero--admin">
      <div class="page-hero-bg" aria-hidden="true" />
      <div class="page-hero-inner page-hero-inner--split">
        <div>
          <span class="page-hero-badge"><el-icon><User /></el-icon> 资质审核</span>
          <h1 class="page-hero-title">用户<span class="accent">审核</span></h1>
          <p class="page-hero-sub">审核学生/企业提交的认证材料，通过或驳回并填写意见。</p>
        </div>
        <span v-if="table.total" class="hero-stat">待审 {{ table.total }} 人</span>
      </div>
    </section>

    <div class="feature-body">
      <div class="table-card">
        <div class="table-card-head">
          <span>待审核用户</span>
          <span class="table-meta">共 {{ table.total }} 条</span>
        </div>
        <el-table :data="table.records" v-loading="loading" stripe class="data-table" empty-text="暂无待审用户">
          <el-table-column prop="username" label="用户名" width="120" show-overflow-tooltip />
          <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
          <el-table-column label="角色" width="88" align="center">
            <template #default="{ row }">
              <el-tag size="small" effect="light">{{ userRoleLabel(row.role) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="认证状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag type="warning" size="small" effect="light" round>{{ authStatusLabel(row.authStatus) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="认证材料" min-width="180">
            <template #default="{ row }">
              <div v-if="row.idCard" class="att-cell">
                <el-image
                  v-if="isImageAttachment(row.idCard)"
                  :src="fileUrl(row.idCard)"
                  :preview-src-list="[fileUrl(row.idCard)]"
                  fit="cover"
                  class="att-thumb"
                  preview-teleported
                />
                <el-link type="primary" :href="fileUrl(row.idCard)" target="_blank" rel="noopener">查看材料</el-link>
              </div>
              <span v-else class="text-muted">—</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="注册时间" width="168">
            <template #default="{ row }"><span class="time-cell">{{ formatTime(row.createTime) }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right" align="center">
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
import { User } from '@element-plus/icons-vue'
import { getPendingUsers, approveUser, rejectUser } from '../../api/admin'
import { API_BASE_URL } from '../../utils/request'
import { authStatusLabel, userRoleLabel } from '../../utils/enumLabel'

const fileUrl = (path) => {
  if (!path) return '#'
  const p = String(path).trim()
  if (p.startsWith('http://') || p.startsWith('https://')) return p
  return API_BASE_URL + (p.startsWith('/') ? p : `/${p}`)
}

const isImageAttachment = (path) => /\.(jpe?g|png|gif|webp|bmp|svg)$/i.test(String(path).split('?')[0])

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
@use '../../styles/feature-page.scss';

.hero-stat {
  font-size: 15px;
  font-weight: 600;
  padding: 10px 16px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.12);
}

.att-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.att-thumb {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}
</style>
