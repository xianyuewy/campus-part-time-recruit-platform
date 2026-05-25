<template>
  <div class="admin-layout">
    <el-container class="admin-shell">
      <el-aside width="220px" class="aside">
        <div class="brand">管理后台</div>
        <el-menu :default-active="active" router>
          <el-menu-item index="/admin/dashboard">
            <span>管理首页</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <span>用户审核</span>
          </el-menu-item>
          <el-menu-item index="/admin/user-manage">
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/jobs">
            <span>岗位审核</span>
          </el-menu-item>
          <el-menu-item index="/admin/job-list">
            <span>岗位管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/credit">
            <span>信用监管</span>
          </el-menu-item>
          <el-menu-item index="/admin/config">
            <span>系统配置</span>
          </el-menu-item>
          <el-menu-item index="/admin/profile">
            <span>个人中心</span>
          </el-menu-item>
        </el-menu>
        <div class="aside-foot">
          <el-button link type="primary" @click="goHome">返回前台首页</el-button>
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

const route = useRoute()
const router = useRouter()

const active = computed(() => route.path)

const goHome = () => router.push('/home')
</script>

<style scoped lang="scss">
.admin-layout {
  min-height: 100vh;
  background: #f5f7fa;
}
.admin-shell {
  min-height: 100vh;
}
.aside {
  background: #fff;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
}
.brand {
  padding: 16px;
  font-weight: 600;
  font-size: 16px;
  border-bottom: 1px solid #ebeef5;
}
.aside-foot {
  padding: 12px 16px;
  margin-top: auto;
}
.main {
  padding: 20px;
}
</style>
