<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="login-bg">
      <div class="bg-shape bg-shape-1"></div>
      <div class="bg-shape bg-shape-2"></div>
      <div class="bg-shape bg-shape-3"></div>
    </div>

    <!-- 登录表单 -->
    <div class="login-form-container">
      <div class="login-header">
        <h1 class="login-title">大学生兼职招聘系统</h1>
        <p class="login-subtitle">登录您的账户，开启兼职之旅</p>
      </div>

      <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @keyup.enter="handleLogin"
      >
        <!-- 用户名输入 -->
        <el-form-item prop="username">
          <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
              clearable
          />
        </el-form-item>

        <!-- 密码输入 -->
        <el-form-item prop="password">
          <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              clearable
          />
        </el-form-item>

        <!-- 记住我 -->
        <el-form-item>
          <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
          <el-link type="primary" :underline="false" class="forgot-password">
            忘记密码？
          </el-link>
        </el-form-item>

        <!-- 登录按钮 -->
        <el-form-item>
          <el-button
              type="primary"
              :loading="loading"
              size="large"
              class="login-btn"
              @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>

        <!-- 注册链接 -->
        <div class="register-link">
          还没有账户？
          <el-link type="primary" :underline="false" @click="goToRegister">
            立即注册
          </el-link>
        </div>

        <!-- 其他登录方式 -->
        <div class="other-login">
          <div class="divider">
            <span>其他登录方式</span>
          </div>
          <div class="login-methods">
            <el-tooltip content="微信登录" placement="top">
              <div class="login-method wechat">
                <el-icon :size="24"><ChatDotRound /></el-icon>
              </div>
            </el-tooltip>
            <el-tooltip content="QQ登录" placement="top">
              <div class="login-method qq">
                <el-icon :size="24"><ChatLineRound /></el-icon>
              </div>
            </el-tooltip>
            <el-tooltip content="手机登录" placement="top">
              <div class="login-method phone">
                <el-icon :size="24"><Iphone /></el-icon>
              </div>
            </el-tooltip>
          </div>
        </div>
      </el-form>

      <!-- 底部信息 -->
      <div class="login-footer">
        <p>© 2026 大学生兼职招聘系统 版权所有</p>
        <p>
          <el-link :underline="false" type="info">用户协议</el-link>
          ·
          <el-link :underline="false" type="info">隐私政策</el-link>
        </p>
      </div>
    </div>

    <!-- 右侧信息面板 -->
    <div class="info-panel">
      <div class="info-content">
        <h2>为什么选择我们？</h2>
        <div class="feature-list">
          <div class="feature-item">
            <el-icon :size="24" color="#409EFF"><Check /></el-icon>
            <div>
              <h3>安全可靠</h3>
              <p>企业认证，信用评价，保障交易安全</p>
            </div>
          </div>
          <div class="feature-item">
            <el-icon :size="24" color="#409EFF"><Check /></el-icon>
            <div>
              <h3>职位丰富</h3>
              <p>海量兼职岗位，覆盖多个行业</p>
            </div>
          </div>
          <div class="feature-item">
            <el-icon :size="24" color="#409EFF"><Check /></el-icon>
            <div>
              <h3>灵活便捷</h3>
              <p>随时随地申请，线上线下一体化</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, ChatDotRound, ChatLineRound, Iphone, Check } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()

// 表单引用
const loginFormRef = ref()

// 表单数据
const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

// 加载状态
const loading = ref(false)

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.userLogin({
          username: loginForm.username,
          password: loginForm.password
        })

        ElMessage({
          message: '登录成功',
          type: 'success',
          duration: 2000
        })

        // 跳转到首页
        router.push('/home')
      } catch (error) {
        ElMessage({
          message: error.message || '登录失败',
          type: 'error',
          duration: 3000
        })
      } finally {
        loading.value = false
      }
    }
  })
}

// 跳转到注册页面
const goToRegister = () => {
  router.push('/register')
}

// 组件挂载时检查是否已登录
onMounted(() => {
  if (userStore.isLoggedIn) {
    router.push('/home')
  }

  // 如果有记住的账号，自动填充
  const savedUsername = localStorage.getItem('rememberedUsername')
  if (savedUsername) {
    loginForm.username = savedUsername
    loginForm.rememberMe = true
  }
})
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
  position: relative;
}

.login-bg {
  position: absolute;
  width: 100%;
  height: 100%;

  .bg-shape {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);

    &-1 {
      width: 300px;
      height: 300px;
      top: -150px;
      right: -150px;
    }

    &-2 {
      width: 200px;
      height: 200px;
      bottom: -100px;
      left: 20%;
    }

    &-3 {
      width: 150px;
      height: 150px;
      bottom: 20%;
      right: 10%;
    }
  }
}

.login-form-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;

  .login-title {
    font-size: 32px;
    font-weight: 600;
    color: #2c3e50;
    margin-bottom: 8px;
    background: linear-gradient(45deg, #409EFF, #67C23A);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }

  .login-subtitle {
    font-size: 16px;
    color: #666;
  }
}

.login-form {
  width: 100%;
  max-width: 400px;

  .forgot-password {
    float: right;
  }

  .login-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    margin-top: 10px;
  }
}

.register-link {
  text-align: center;
  margin: 20px 0;
  color: #666;

  .el-link {
    font-weight: 500;
  }
}

.other-login {
  margin-top: 30px;

  .divider {
    display: flex;
    align-items: center;
    margin: 20px 0;
    color: #999;

    &::before,
    &::after {
      content: '';
      flex: 1;
      height: 1px;
      background: #e0e0e0;
    }

    span {
      padding: 0 20px;
      font-size: 14px;
    }
  }

  .login-methods {
    display: flex;
    justify-content: center;
    gap: 20px;

    .login-method {
      width: 50px;
      height: 50px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.3s;
      background: #f5f7fa;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      &.wechat:hover {
        background: #07C160;
        color: white;
      }

      &.qq:hover {
        background: #12B7F5;
        color: white;
      }

      &.phone:hover {
        background: #409EFF;
        color: white;
      }
    }
  }
}

.login-footer {
  margin-top: 40px;
  text-align: center;
  color: #999;
  font-size: 14px;

  p {
    margin: 5px 0;
  }
}

.info-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: white;
  z-index: 1;
}

.info-content {
  max-width: 500px;

  h2 {
    font-size: 36px;
    font-weight: 600;
    margin-bottom: 30px;
  }

  .feature-list {
    .feature-item {
      display: flex;
      align-items: flex-start;
      gap: 15px;
      margin-bottom: 25px;

      h3 {
        font-size: 18px;
        font-weight: 600;
        margin-bottom: 5px;
      }

      p {
        font-size: 14px;
        opacity: 0.9;
        line-height: 1.6;
      }

      .el-icon {
        margin-top: 2px;
        flex-shrink: 0;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
  }

  .info-panel {
    display: none;
  }

  .login-form-container {
    padding: 20px;
  }
}
</style>