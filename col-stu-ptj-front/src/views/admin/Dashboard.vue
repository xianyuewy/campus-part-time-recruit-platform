<template>
  <div class="feature-page admin-dashboard">
    <section class="page-hero page-hero--admin">
      <div class="page-hero-bg" aria-hidden="true" />
      <div class="page-hero-inner">
        <span class="page-hero-badge"><el-icon><Odometer /></el-icon> 平台运营</span>
        <h1 class="page-hero-title">管理<span class="accent">首页</span></h1>
        <p class="page-hero-sub">以下为全部管理功能入口，点击卡片进入对应模块；下方为实时数据概览。</p>
      </div>
    </section>

    <div class="feature-body">
      <el-row :gutter="16" class="entry-row">
        <el-col v-for="item in entries" :key="item.path" :xs="24" :sm="12" :md="8" :lg="6">
          <div class="entry-card" @click="go(item.path)">
            <div class="entry-icon-wrap" :style="{ background: item.tint }">
              <el-icon class="entry-icon" :size="26">
                <component :is="item.icon" />
              </el-icon>
            </div>
            <div class="entry-text">
              <div class="entry-title">{{ item.title }}</div>
              <div class="entry-desc">{{ item.desc }}</div>
            </div>
            <el-icon class="entry-arrow" :size="18"><ArrowRight /></el-icon>
          </div>
        </el-col>
      </el-row>

      <h3 class="section-title">数据概览</h3>
      <el-row :gutter="16" class="metric-row">
        <el-col v-for="m in metrics" :key="m.label" :xs="12" :sm="8" :md="6" :lg="4">
          <div class="metric-card" :class="m.cls">
            <div class="metric-label">{{ m.label }}</div>
            <div class="metric-value">{{ m.value }}</div>
          </div>
        </el-col>
      </el-row>

      <div class="table-card">
        <div class="table-card-head">
          <span>近 7 日新增趋势</span>
        </div>
        <el-table :data="stats.last7Days" stripe class="data-table" size="default">
          <el-table-column prop="date" label="日期" width="140" />
          <el-table-column prop="newUsers" label="新增用户" />
          <el-table-column prop="newJobs" label="新增岗位" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowRight,
  User,
  Avatar,
  DocumentChecked,
  Files,
  Medal,
  Setting,
  Document,
  Postcard,
  Odometer
} from '@element-plus/icons-vue'
import { getDashboardStats } from '../../api/admin'

const router = useRouter()

const entries = [
  { path: '/jobs', title: '浏览岗位', desc: '查看已上架兼职（只读）', icon: Document, tint: 'linear-gradient(135deg, #409eff, #79bbff)' },
  { path: '/admin/profile', title: '个人中心', desc: '资料、头像与密码', icon: Postcard, tint: 'linear-gradient(135deg, #5a8fbd, #a0cfff)' },
  { path: '/admin/users', title: '用户审核', desc: '待认证账号审核', icon: User, tint: 'linear-gradient(135deg, #67c23a, #95d475)' },
  { path: '/admin/user-manage', title: '用户管理', desc: '启停与账号管理', icon: Avatar, tint: 'linear-gradient(135deg, #e6a23c, #f3d19e)' },
  { path: '/admin/jobs', title: '岗位审核', desc: '待上架岗位审核', icon: DocumentChecked, tint: 'linear-gradient(135deg, #909399, #b1b3b8)' },
  { path: '/admin/job-list', title: '岗位管理', desc: '全平台岗位列表', icon: Files, tint: 'linear-gradient(135deg, #606266, #909399)' },
  { path: '/admin/credit', title: '信用监管', desc: '信用分与争议工单', icon: Medal, tint: 'linear-gradient(135deg, #f56c6c, #fab6b6)' },
  { path: '/admin/config', title: '系统配置', desc: '公告、轮播与规则', icon: Setting, tint: 'linear-gradient(135deg, #9b59b6, #c39bd3)' }
]

const stats = reactive({
  totalUsers: 0,
  pendingUsers: 0,
  totalJobs: 0,
  pendingJobs: 0,
  disabledUsers: 0,
  pendingDisputes: 0,
  last7Days: []
})

const metrics = computed(() => [
  { label: '用户总数', value: stats.totalUsers, cls: '' },
  { label: '待审用户', value: stats.pendingUsers, cls: 'warn' },
  { label: '岗位总数', value: stats.totalJobs, cls: '' },
  { label: '待审岗位', value: stats.pendingJobs, cls: 'warn' },
  { label: '停用账号', value: stats.disabledUsers, cls: 'danger' },
  { label: '待处理争议', value: stats.pendingDisputes, cls: 'warn' }
])

const go = (path) => {
  if (path !== router.currentRoute.value.path) router.push(path)
}

const load = async () => {
  try {
    const res = await getDashboardStats()
    if (res.success && res.data) Object.assign(stats, res.data)
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
@use '../../styles/feature-page.scss';

.entry-row {
  margin-bottom: 8px;
}

.entry-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  margin-bottom: 16px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid #eef1f6;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 12px 32px rgba(15, 23, 42, 0.1);

    .entry-arrow {
      color: #409eff;
      transform: translateX(4px);
    }
  }
}

.entry-icon-wrap {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.entry-text {
  flex: 1;
  min-width: 0;
}

.entry-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.entry-desc {
  font-size: 12px;
  color: #909399;
}

.entry-arrow {
  color: #c0c4cc;
  transition: transform 0.2s, color 0.2s;
}

.section-title {
  margin: 8px 0 16px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.metric-row {
  margin-bottom: 20px;
}

.metric-card {
  background: #fff;
  border-radius: 14px;
  padding: 18px 16px;
  margin-bottom: 16px;
  border: 1px solid #eef1f6;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.04);

  &.warn .metric-value {
    color: #e6a23c;
  }
  &.danger .metric-value {
    color: #f56c6c;
  }
}

.metric-label {
  font-size: 13px;
  color: #909399;
}

.metric-value {
  font-size: 28px;
  font-weight: 800;
  margin-top: 8px;
  color: #303133;
}
</style>
