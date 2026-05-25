<template>
  <div class="main-layout">
    <el-container class="shell">
      <el-aside :width="asideWidth" class="aside">
        <div class="brand" @click="router.push('/home')">
          <span class="brand-title">大学生兼职</span>
          <span class="brand-sub">招聘系统</span>
        </div>
        <el-scrollbar class="menu-scroll">
          <el-menu
            :default-active="activeMenu"
            router
            :collapse="collapsed"
            background-color="#1f2d3d"
            text-color="#bfcbd9"
            active-text-color="#409eff"
            class="side-menu"
          >
            <el-menu-item index="/home">
              <el-icon><Odometer /></el-icon>
              <span>工作台</span>
            </el-menu-item>

            <template v-if="userStore.isStudent">
              <el-menu-item index="/jobs">
                <el-icon><Document /></el-icon>
                <span>岗位浏览</span>
              </el-menu-item>
              <el-menu-item index="/applications">
                <el-icon><Wallet /></el-icon>
                <span>我的投递</span>
              </el-menu-item>
              <el-menu-item index="/favorites">
                <el-icon><Star /></el-icon>
                <span>我的收藏</span>
              </el-menu-item>
              <el-menu-item index="/resume">
                <el-icon><Tickets /></el-icon>
                <span>我的简历</span>
              </el-menu-item>
              <el-menu-item index="/reviews">
                <el-icon><ChatDotSquare /></el-icon>
                <span>我的评价</span>
              </el-menu-item>
              <el-menu-item index="/credit">
                <el-icon><Medal /></el-icon>
                <span>信用中心</span>
              </el-menu-item>
            </template>

            <template v-else-if="userStore.isCompany">
              <el-menu-item index="/jobs">
                <el-icon><Document /></el-icon>
                <span>岗位浏览</span>
              </el-menu-item>
              <el-menu-item index="/company/jobs">
                <el-icon><OfficeBuilding /></el-icon>
                <span>岗位管理</span>
              </el-menu-item>
              <el-menu-item index="/company/applicants">
                <el-icon><List /></el-icon>
                <span>候选人</span>
              </el-menu-item>
              <el-menu-item index="/company/profile">
                <el-icon><Setting /></el-icon>
                <span>企业资料</span>
              </el-menu-item>
              <el-menu-item index="/company/reviews">
                <el-icon><Star /></el-icon>
                <span>评价中心</span>
              </el-menu-item>
              <el-menu-item index="/company/credit">
                <el-icon><Medal /></el-icon>
                <span>信用中心</span>
              </el-menu-item>
            </template>

            <template v-else-if="userStore.isAdmin">
              <el-menu-item index="/jobs">
                <el-icon><Document /></el-icon>
                <span>岗位浏览</span>
              </el-menu-item>
              <el-menu-item index="/admin/dashboard">
                <el-icon><DataAnalysis /></el-icon>
                <span>管理后台</span>
              </el-menu-item>
            </template>

            <el-menu-item-group title="通用">
              <el-menu-item index="/notifications">
                <el-icon><Bell /></el-icon>
                <span>消息通知</span>
              </el-menu-item>
              <el-menu-item index="/profile">
                <el-icon><User /></el-icon>
                <span>个人中心</span>
              </el-menu-item>
            </el-menu-item-group>
          </el-menu>
        </el-scrollbar>

        <div class="aside-foot">
          <el-button class="collapse-btn" text type="primary" @click="collapsed = !collapsed">
            {{ collapsed ? '展开' : '收起' }}侧栏
          </el-button>
        </div>
      </el-aside>

      <el-container class="right">
        <el-header class="topbar" height="56px">
          <div class="topbar-left">
            <span class="page-title">{{ pageTitle }}</span>
          </div>
          <div class="topbar-right">
            <el-badge
              :value="notificationStore.unreadCount"
              :hidden="notificationStore.unreadCount <= 0"
              :max="99"
              class="notify-badge"
            >
              <el-button circle text class="icon-btn" title="消息通知" @click="router.push('/notifications')">
                <el-icon :size="20"><Bell /></el-icon>
              </el-button>
            </el-badge>
            <el-dropdown trigger="click" @command="onUserCommand">
              <div class="user-chip">
                <el-avatar :size="32" :src="avatarSrc" />
                <span class="user-name">{{ displayName }}</span>
                <el-icon class="caret"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="security">
                    <el-icon><Setting /></el-icon>
                    账户设置
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        <el-main class="content">
          <router-view v-slot="{ Component }">
            <transition name="fade-slide" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  ArrowDown,
  Bell,
  ChatDotSquare,
  DataAnalysis,
  Document,
  List,
  Medal,
  Odometer,
  OfficeBuilding,
  Setting,
  Star,
  SwitchButton,
  Tickets,
  User,
  Wallet
} from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'
import { useNotificationStore } from '../store/notification'
import { API_BASE_URL } from '../utils/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const notificationStore = useNotificationStore()

const collapsed = ref(false)
const asideWidth = computed(() => (collapsed.value ? '64px' : '220px'))

const activeMenu = computed(() => {
  const p = route.path
  if (p.startsWith('/admin')) return '/admin/dashboard'
  if (p.startsWith('/jobs/')) return '/jobs'
  return p
})

const pageTitle = computed(() => {
  const m = route.matched
    .slice()
    .reverse()
    .find((r) => r.meta && r.meta.title)
  return m?.meta?.title || '工作台'
})

const defaultAvatar = '/avatars/default.png'
const avatarSrc = computed(() => {
  const path = userStore.userInfo?.avatar
  if (!path) return defaultAvatar
  if (path.startsWith('http')) return path
  return API_BASE_URL + (path.startsWith('/') ? path : '/' + path)
})

const displayName = computed(
  () => userStore.userInfo?.displayName || userStore.userInfo?.nickname || userStore.userInfo?.username || '用户'
)

watch(
  () => route.path,
  () => {
    notificationStore.fetchUnread().catch(() => {})
  },
  { immediate: true }
)

const onUserCommand = async (cmd) => {
  if (cmd === 'profile') {
    router.push('/profile')
    return
  }
  if (cmd === 'security') {
    router.push({ path: '/profile', query: { tab: 'security' } })
    return
  }
  if (cmd === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await userStore.userLogout()
      ElMessage.success('已退出登录')
      router.push('/login')
    } catch {
      /* cancel */
    }
  }
}
</script>

<style scoped lang="scss">
.main-layout {
  height: 100%;
  min-height: 100vh;
  background: #f0f2f5;
}

.shell {
  min-height: 100vh;
}

.aside {
  background: #1f2d3d;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #0d1520;
  transition: width 0.2s ease;
}

.brand {
  flex-shrink: 0;
  padding: 16px 14px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  .brand-title {
    display: block;
    font-size: 15px;
    font-weight: 700;
    color: #fff;
    letter-spacing: 0.5px;
  }
  .brand-sub {
    display: block;
    font-size: 12px;
    color: #8fa4b8;
    margin-top: 2px;
  }
}

.menu-scroll {
  flex: 1;
  min-height: 0;
}

.side-menu {
  border-right: none !important;
  padding: 8px 0 16px;
}

.side-menu :deep(.el-menu-item-group__title) {
  padding: 12px 16px 6px;
  font-size: 12px;
  color: #6b7c8f;
}

.aside-foot {
  flex-shrink: 0;
  padding: 8px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  .collapse-btn {
    width: 100%;
    justify-content: center;
    color: #8fa4b8 !important;
    font-size: 12px;
  }
}

.right {
  flex-direction: column;
  min-width: 0;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #e8ecf0;
  box-shadow: 0 1px 0 rgba(0, 0, 0, 0.02);
}

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.icon-btn {
  color: #606266;
  &:hover {
    color: #409eff;
  }
}

.notify-badge :deep(.el-badge__content) {
  border: 2px solid #fff;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px 4px 4px;
  border-radius: 24px;
  cursor: pointer;
  transition: background 0.2s;
  &:hover {
    background: #f5f7fa;
  }
  .user-name {
    max-width: 120px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 14px;
    color: #303133;
  }
  .caret {
    color: #909399;
    font-size: 12px;
  }
}

.content {
  padding: 20px;
  overflow: auto;
  min-height: calc(100vh - 56px);
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(6px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
