<!-- src/App.vue -->
<template>
  <router-view />
  <div v-if="showBackToHome" class="back-home-wrap">
    <el-button type="primary" round size="default" class="back-home-btn" @click="goHome">
      <el-icon class="back-home-icon"><House /></el-icon>
      返回首页
    </el-button>

  </div>
  <AiChatService />
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { House } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

/** 已登录业务页：左侧导航已含工作台入口；管理后台另有返回前台 */
const showBackToHome = computed(() => {
  const p = route.path
  if (p === '/home') return false
  if (p === '/login' || p === '/register') return false
  if (p.startsWith('/admin')) return false
  if (route.matched.some((r) => r.meta && r.meta.mainLayout)) return false
  return route.matched.some((r) => r.meta && r.meta.requiresAuth)
})

const goHome = () => {
  router.push('/home')
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body,
#app {
  height: 100%;
  width: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu',
    'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.back-home-wrap {
  position: fixed;
  right: 20px;
  bottom: 24px;
  z-index: 3100;
  pointer-events: none;
}
.back-home-wrap .back-home-btn {
  pointer-events: auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}
.back-home-icon {
  margin-right: 4px;
  vertical-align: middle;
}
</style>
