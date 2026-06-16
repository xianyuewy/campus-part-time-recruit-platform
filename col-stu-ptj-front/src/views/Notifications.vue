<template>
  <div class="feature-page">
    <section class="page-hero page-hero--notify">
      <div class="page-hero-bg" aria-hidden="true" />
      <div class="page-hero-inner page-hero-inner--split">
        <div>
          <span class="page-hero-badge"><el-icon><Bell /></el-icon> 站内信</span>
          <h1 class="page-hero-title">消息<span class="accent">通知</span></h1>
          <p class="page-hero-sub">投递状态、面试安排等业务消息将在此推送，可一键跳转相关业务。</p>
        </div>
        <div class="notify-stats">
          <el-tag type="warning" effect="dark" round size="large">未读 {{ unreadCount }}</el-tag>
        </div>
      </div>
    </section>

    <div class="feature-body">
      <div class="toolbar-card">
        <div class="toolbar-head">
          <el-icon class="toolbar-head-icon"><Operation /></el-icon>
          <span>消息列表</span>
        </div>
        <div class="notify-actions">
          <el-button round :icon="Refresh" :loading="loading" @click="load">刷新</el-button>
          <el-button round @click="markAllRead">全部已读</el-button>
        </div>
      </div>

      <div class="table-card">
        <el-table :data="rows.records" v-loading="loading" stripe class="data-table" empty-text="暂无消息">
          <el-table-column label="状态" width="88" align="center">
            <template #default="{ row }">
              <el-tag :type="row.read ? 'info' : 'danger'" size="small" effect="light" round>
                {{ row.read ? '已读' : '未读' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="标题" width="160" show-overflow-tooltip>
            <template #default="{ row }">
              <span :class="['msg-title', { 'msg-title--unread': !row.read }]">{{ row.title }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="内容" min-width="280" show-overflow-tooltip />
          <el-table-column prop="createTime" label="时间" width="168">
            <template #default="{ row }">
              <span class="time-cell">{{ formatTime(row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center">
            <template #default="{ row }">
              <el-button v-if="!row.read" size="small" text type="primary" @click="markRead(row)">标为已读</el-button>
              <el-button size="small" text type="primary" @click="goBiz(row)">查看业务</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pager">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next"
            :total="rows.total"
            v-model:current-page="q.current"
            v-model:page-size="q.size"
            :page-sizes="[10, 20, 50]"
            @size-change="load"
            @current-change="load"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Bell, Operation, Refresh } from '@element-plus/icons-vue'
import {
  pageNotifications,
  getUnreadNotificationCount,
  markNotificationRead,
  markAllNotificationsRead
} from '../api/notification'
import { useNotificationStore } from '../store/notification'
import { useUserStore } from '../store/user'

const router = useRouter()
const notifStore = useNotificationStore()
const userStore = useUserStore()

const q = reactive({ current: 1, size: 10 })
const rows = reactive({ records: [], total: 0 })
const loading = ref(false)
const unreadCount = ref(0)

const formatTime = (v) => {
  if (!v) return '—'
  const s = String(v).replace('T', ' ')
  return s.length > 16 ? s.slice(0, 16) : s
}

const loadUnread = async () => {
  try {
    const res = await getUnreadNotificationCount()
    if (res.success) {
      const c = res.data?.count || 0
      unreadCount.value = c
      notifStore.setUnread(c)
    }
  } catch {
    // ignore
  }
}

const load = async () => {
  loading.value = true
  try {
    const res = await pageNotifications(q)
    if (res.success && res.data) Object.assign(rows, res.data)
    await loadUnread()
  } finally {
    loading.value = false
  }
}

const markRead = async (row) => {
  try {
    await markNotificationRead(row.id)
    row.read = true
    await loadUnread()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const markAllRead = async () => {
  try {
    await markAllNotificationsRead()
    ElMessage.success('已全部标记')
    await load()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const goBiz = async (row) => {
  if (row.id && !row.read) {
    try {
      await markNotificationRead(row.id)
      row.read = true
      await loadUnread()
    } catch (e) {
      ElMessage.error(e.message || '标记已读失败')
      return
    }
  }
  const appId = row.bizId ? String(row.bizId) : ''
  if (!appId) {
    ElMessage.info('该通知暂无可跳转业务')
    return
  }
  if (row.bizType === 'APPLICATION_SUBMITTED' || row.bizType === 'APPLICATION_CANCELLED') {
    router.push({ path: '/company/applicants', query: { applicationId: appId } })
    return
  }
  if (row.bizType === 'APPLICATION_STATUS' || row.bizType === 'INTERVIEW_SCHEDULE') {
    router.push({ path: '/applications', query: { applicationId: appId } })
    return
  }
  if (userStore.isCompany) {
    router.push({ path: '/company/applicants', query: { applicationId: appId } })
    return
  }
  if (userStore.isStudent) {
    router.push({ path: '/applications', query: { applicationId: appId } })
    return
  }
  ElMessage.info('该通知类型暂不支持业务跳转')
}

const bootstrap = async () => {
  try {
    await markAllNotificationsRead()
    notifStore.setUnread(0)
  } catch {
    // ignore
  }
  await load()
}

onMounted(bootstrap)
</script>

<style scoped lang="scss">
@use '../styles/feature-page.scss';

.notify-stats {
  flex-shrink: 0;
}

.notify-actions {
  display: flex;
  gap: 10px;
  padding: 12px 20px 14px;
  border-bottom: 1px solid #f0f3f8;
}

.msg-title {
  color: #606266;

  &--unread {
    font-weight: 600;
    color: #303133;
  }
}
</style>
