<template>
  <div>
    <h2>消息通知</h2>
    <el-card class="mb16">
      <el-space>
        <el-tag type="warning">未读 {{ unreadCount }}</el-tag>
        <el-button size="small" type="primary" plain @click="load">刷新</el-button>
        <el-button size="small" @click="markAllRead">全部已读</el-button>
      </el-space>
    </el-card>

    <el-card>
      <el-table :data="rows.records" v-loading="loading" border>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.read ? 'info' : 'danger'" size="small">{{ row.read ? '已读' : '未读' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" width="180" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="340" show-overflow-tooltip />
        <el-table-column prop="createTime" label="时间" width="180" />
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button v-if="!row.read" size="small" type="primary" @click="markRead(row)">标为已读</el-button>
            <el-button size="small" @click="goBiz(row)">查看业务</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :total="rows.total"
          v-model:current-page="q.current"
          v-model:page-size="q.size"
          @current-change="load"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
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
    // 未登录或网络失败时仍展示列表
  }
  await load()
}

onMounted(bootstrap)
</script>

<style scoped>
.mb16 { margin-bottom: 16px; }
.pager { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
