<template>
  <div class="admin-dashboard">
    <div class="head-block">
      <h2 class="page-title">管理后台首页</h2>
      <p class="head-desc">以下为全部管理功能入口，点击卡片即可进入对应模块。</p>
    </div>

    <el-row :gutter="16" class="entry-row">
      <el-col v-for="item in entries" :key="item.path" :xs="24" :sm="12" :md="8" :lg="6">
        <el-card class="entry-card" shadow="hover" @click="go(item.path)">
          <div class="entry-inner">
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
        </el-card>
      </el-col>
    </el-row>

    <h3 class="section-title">数据概览</h3>
    <el-row :gutter="16" class="mb16">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover"><div class="metric-label">用户总数</div><div class="metric-value">{{ stats.totalUsers }}</div></el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover"><div class="metric-label">待审用户</div><div class="metric-value warn">{{ stats.pendingUsers }}</div></el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover"><div class="metric-label">岗位总数</div><div class="metric-value">{{ stats.totalJobs }}</div></el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover"><div class="metric-label">待审岗位</div><div class="metric-value warn">{{ stats.pendingJobs }}</div></el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover"><div class="metric-label">停用账号</div><div class="metric-value danger">{{ stats.disabledUsers }}</div></el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover"><div class="metric-label">待处理争议</div><div class="metric-value warn">{{ stats.pendingDisputes }}</div></el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" header="近7日新增趋势">
      <el-table :data="stats.last7Days" size="small" style="width: 100%">
        <el-table-column prop="date" label="日期" width="140" />
        <el-table-column prop="newUsers" label="新增用户" />
        <el-table-column prop="newJobs" label="新增岗位" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
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
  Postcard
} from '@element-plus/icons-vue'
import { getDashboardStats } from '../../api/admin'

const router = useRouter()

const entries = [
  {
    path: '/jobs',
    title: '浏览岗位',
    desc: '查看已上架兼职（只读）',
    icon: Document,
    tint: 'linear-gradient(135deg, #409eff 0%, #79bbff 100%)'
  },
  {
    path: '/admin/profile',
    title: '个人中心',
    desc: '资料、头像与密码',
    icon: Postcard,
    tint: 'linear-gradient(135deg, #409eff 0%, #a0cfff 100%)'
  },
  {
    path: '/admin/users',
    title: '用户审核',
    desc: '待认证账号审核',
    icon: User,
    tint: 'linear-gradient(135deg, #67c23a 0%, #95d475 100%)'
  },
  {
    path: '/admin/user-manage',
    title: '用户管理',
    desc: '启停与账号管理',
    icon: Avatar,
    tint: 'linear-gradient(135deg, #e6a23c 0%, #f3d19e 100%)'
  },
  {
    path: '/admin/jobs',
    title: '岗位审核',
    desc: '待上架岗位审核',
    icon: DocumentChecked,
    tint: 'linear-gradient(135deg, #909399 0%, #b1b3b8 100%)'
  },
  {
    path: '/admin/job-list',
    title: '岗位管理',
    desc: '全平台岗位列表',
    icon: Files,
    tint: 'linear-gradient(135deg, #606266 0%, #909399 100%)'
  },
  {
    path: '/admin/credit',
    title: '信用监管',
    desc: '信用分与争议工单',
    icon: Medal,
    tint: 'linear-gradient(135deg, #f56c6c 0%, #fab6b6 100%)'
  },
  {
    path: '/admin/config',
    title: '系统配置',
    desc: '公告、轮播与规则',
    icon: Setting,
    tint: 'linear-gradient(135deg, #9b59b6 0%, #c39bd3 100%)'
  }
]

const go = (path) => {
  if (path === router.currentRoute.value.path) {
    return
  }
  router.push(path)
}

const stats = reactive({
  totalUsers: 0,
  pendingUsers: 0,
  totalJobs: 0,
  pendingJobs: 0,
  disabledUsers: 0,
  pendingDisputes: 0,
  last7Days: []
})

const load = async () => {
  try {
    const res = await getDashboardStats()
    if (res.success && res.data) {
      Object.assign(stats, res.data)
    }
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.admin-dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

.head-block {
  margin-bottom: 20px;
}

.page-title {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.head-desc {
  margin: 0;
  font-size: 14px;
  color: #909399;
  line-height: 1.5;
}

.section-title {
  margin: 28px 0 16px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.entry-row {
  margin-bottom: 8px;
}

.entry-card {
  cursor: pointer;
  margin-bottom: 16px;
  border-radius: 10px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }

  :deep(.el-card__body) {
    padding: 16px 14px;
  }
}

.entry-inner {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 56px;
}

.entry-icon-wrap {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: 10px;
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
  line-height: 1.4;
}

.entry-arrow {
  flex-shrink: 0;
  color: #c0c4cc;
  transition: transform 0.2s ease, color 0.2s ease;
}

.entry-card:hover .entry-arrow {
  color: #409eff;
  transform: translateX(3px);
}

.mb16 {
  margin-bottom: 16px;
}

.metric-label {
  color: #909399;
  font-size: 13px;
}
.metric-value {
  font-size: 26px;
  font-weight: 700;
  margin-top: 8px;
  color: #303133;
}
.metric-value.warn {
  color: #e6a23c;
}
.metric-value.danger {
  color: #f56c6c;
}
</style>
