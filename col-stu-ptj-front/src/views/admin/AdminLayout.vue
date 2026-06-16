<template>
  <div class="admin-layout">
    <el-container class="admin-shell">
      <el-aside width="232px" class="aside">
        <div class="brand">
          <el-icon class="brand-icon"><Setting /></el-icon>
          <span>管理后台</span>
        </div>
        <el-menu :default-active="active" router class="admin-menu">
          <el-menu-item index="/admin/dashboard">
            <el-icon><Odometer /></el-icon>
            <span>管理首页</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户审核</span>
          </el-menu-item>
          <el-menu-item index="/admin/user-manage">
            <el-icon><Avatar /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/jobs">
            <el-icon><DocumentChecked /></el-icon>
            <span>岗位审核</span>
          </el-menu-item>
          <el-menu-item index="/admin/job-list">
            <el-icon><Files /></el-icon>
            <span>岗位管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/credit">
            <el-icon><Medal /></el-icon>
            <span>信用监管</span>
          </el-menu-item>
          <el-menu-item index="/admin/config">
            <el-icon><Tools /></el-icon>
            <span>系统配置</span>
          </el-menu-item>
          <el-menu-item index="/admin/profile">
            <el-icon><Postcard /></el-icon>
            <span>个人中心</span>
          </el-menu-item>
        </el-menu>
        <div class="aside-foot">
          <button type="button" class="aside-home-btn" @click="goHome">
            <el-icon><HomeFilled /></el-icon>
            <span>返回前台首页</span>
          </button>
        </div>
      </el-aside>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Setting,
  Odometer,
  User,
  Avatar,
  DocumentChecked,
  Files,
  Medal,
  Tools,
  Postcard,
  HomeFilled
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const active = computed(() => route.path)
const goHome = () => router.push('/home')
</script>

<style scoped lang="scss">
.admin-layout {
  min-height: 100vh;
  background: #eef1f6;
}

.admin-shell {
  min-height: 100vh;
}

.aside {
  background: linear-gradient(180deg, #1a2332 0%, #243044 100%);
  border-right: none;
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 24px rgba(15, 23, 42, 0.12);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 18px;
  font-weight: 700;
  font-size: 16px;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.brand-icon {
  font-size: 22px;
  color: #a8c5e8;
}

.admin-menu {
  flex: 1;
  border-right: none;
  background: transparent;
  padding: 12px 10px;

  :deep(.el-menu-item) {
    border-radius: 10px;
    margin-bottom: 4px;
    color: rgba(255, 255, 255, 0.75);
    height: 44px;

    &:hover {
      background: rgba(255, 255, 255, 0.08);
      color: #fff;
    }

    &.is-active {
      background: linear-gradient(90deg, rgba(64, 158, 255, 0.35), rgba(64, 158, 255, 0.12));
      color: #fff;
      font-weight: 600;
    }
  }
}

.aside-foot {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.aside-home-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 10px 14px;
  border: 1px solid rgba(255, 255, 255, 0.28);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition:
    background 0.2s,
    border-color 0.2s,
    color 0.2s;

  .el-icon {
    font-size: 16px;
    color: #a8c5e8;
  }

  &:hover {
    background: rgba(64, 158, 255, 0.22);
    border-color: rgba(120, 180, 255, 0.55);
    color: #fff;
  }

  &:active {
    background: rgba(64, 158, 255, 0.32);
  }
}

.main {
  padding: 20px 24px 32px;
  background: #eef1f6;
}
</style>
