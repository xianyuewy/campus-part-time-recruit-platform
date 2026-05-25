<template>
  <div class="register-container">
    <!-- 背景装饰 -->
    <div class="register-bg">
      <div class="bg-shape bg-shape-1"></div>
      <div class="bg-shape bg-shape-2"></div>
    </div>

    <!-- 返回按钮 -->
    <el-button
        class="back-btn"
        type="primary"
        :icon="ArrowLeft"
        circle
        @click="goToLogin"
    />

    <!-- 注册表单 -->
    <div class="register-form-container">
      <div class="register-header">
        <h1 class="register-title">创建账户</h1>
        <p class="register-subtitle">加入大学生兼职招聘系统，开启你的兼职之旅</p>
      </div>

      <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          class="register-form"
          @keyup.enter="handleRegister"
      >
        <!-- 用户名 -->
        <el-form-item prop="username">
          <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名（3-20位字母、数字、下划线）"
              size="large"
              :prefix-icon="User"
              clearable
          />
        </el-form-item>

        <!-- 密码 -->
        <el-form-item prop="password">
          <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码（至少6位）"
              size="large"
              :prefix-icon="Lock"
              show-password
              clearable
          />
        </el-form-item>

        <!-- 确认密码 -->
        <el-form-item prop="confirmPassword">
          <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请确认密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              clearable
          />
        </el-form-item>

        <!-- 邮箱 -->
        <el-form-item prop="email">
          <el-input
              v-model="registerForm.email"
              placeholder="请输入邮箱"
              size="large"
              :prefix-icon="Message"
              clearable
          />
        </el-form-item>

        <!-- 手机号 -->
        <el-form-item prop="phone">
          <el-input
              v-model="registerForm.phone"
              placeholder="请输入手机号"
              size="large"
              :prefix-icon="Iphone"
              clearable
          >
            <template #prepend>+86</template>
          </el-input>
        </el-form-item>

        <!-- 用户角色 -->
        <el-form-item prop="role">
          <el-radio-group v-model="registerForm.role" class="role-radio-group">
            <el-radio-button label="STUDENT">
              <div class="role-option">
                <el-icon :size="20"><User /></el-icon>
                <span>我是学生</span>
              </div>
            </el-radio-button>
            <el-radio-button label="COMPANY">
              <div class="role-option">
                <el-icon :size="20"><OfficeBuilding /></el-icon>
                <span>我是企业</span>
              </div>
            </el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- 用户协议 -->
        <el-form-item>
          <el-checkbox v-model="registerForm.agreedToTerms">
            我已阅读并同意
            <el-link type="primary" :underline="false">《用户协议》</el-link>
            和
            <el-link type="primary" :underline="false">《隐私政策》</el-link>
          </el-checkbox>
        </el-form-item>

        <!-- 注册按钮 -->
        <el-form-item>
          <el-button
              type="primary"
              :loading="loading"
              size="large"
              class="register-btn"
              @click="handleRegister"
          >
            {{ loading ? '注册中...' : '立即注册' }}
          </el-button>
        </el-form-item>

        <!-- 登录链接 -->
        <div class="login-link">
          已有账户？
          <el-link type="primary" :underline="false" @click="goToLogin">
            立即登录
          </el-link>
        </div>
      </el-form>
    </div>

    <!-- 右侧信息面板 -->
    <div class="register-info-panel">
      <div class="register-info-content">
        <h2>注册后您可以享受</h2>
        <div class="benefit-list">
          <div class="benefit-item">
            <el-icon :size="24" color="#409EFF"><CircleCheck /></el-icon>
            <div>
              <h3>发布兼职</h3>
              <p>企业用户可以发布兼职岗位，招聘优秀人才</p>
            </div>
          </div>
          <div class="benefit-item">
            <el-icon :size="24" color="#409EFF"><CircleCheck /></el-icon>
            <div>
              <h3>申请工作</h3>
              <p>学生用户可以申请心仪的兼职岗位</p>
            </div>
          </div>
          <div class="benefit-item">
            <el-icon :size="24" color="#409EFF"><CircleCheck /></el-icon>
            <div>
              <h3>信用评价</h3>
              <p>建立个人/企业信用档案，提高信任度</p>
            </div>
          </div>
          <div class="benefit-item">
            <el-icon :size="24" color="#409EFF"><CircleCheck /></el-icon>
            <div>
              <h3>消息通知</h3>
              <p>实时接收申请进度和系统通知</p>
            </div>
          </div>
        </div>

        <!-- 统计数据 -->
        <div class="stats">
          <div class="stat-item">
            <div class="stat-number">5000+</div>
            <div class="stat-label">注册用户</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">1000+</div>
            <div class="stat-label">合作企业</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">98%</div>
            <div class="stat-label">满意度</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  Lock,
  Message,
  Iphone,
  OfficeBuilding,
  CircleCheck,
  ArrowLeft
} from '@element-plus/icons-vue'
import { register as registerApi } from '../api/auth'

const router = useRouter()

// 表单引用
const registerFormRef = ref()

// 表单数据
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  role: 'STUDENT',
  agreedToTerms: false
})

// 加载状态
const loading = ref(false)

// 自定义验证规则
const validateUsername = (rule, value, callback) => {
  const usernameRegex = /^[a-zA-Z0-9_]+$/
  if (!usernameRegex.test(value)) {
    callback(new Error('用户名只能包含字母、数字和下划线'))
  } else if (value.length < 3 || value.length > 20) {
    callback(new Error('用户名长度必须在3-20个字符之间'))
  } else {
    callback()
  }
}

const validatePassword = (rule, value, callback) => {
  if (value.length < 6) {
    callback(new Error('密码长度不能少于6位'))
  } else {
    if (registerForm.confirmPassword) {
      registerFormRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  const phoneRegex = /^1[3-9]\d{9}$/
  if (value && !phoneRegex.test(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(value)) {
    callback(new Error('邮箱格式不正确'))
  } else {
    callback()
  }
}

// 表单验证规则
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { validator: validateUsername, trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { validator: validateEmail, trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { validator: validatePhone, trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return

  // 验证用户协议
  if (!registerForm.agreedToTerms) {
    ElMessage({
      message: '请阅读并同意用户协议和隐私政策',
      type: 'warning',
      duration: 3000
    })
    return
  }

  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const response = await registerApi({
          username: registerForm.username,
          password: registerForm.password,
          email: registerForm.email,
          phone: registerForm.phone,
          role: registerForm.role
        })

        if (response.success) {
          // 注册成功，显示成功消息
          await ElMessageBox.alert(
              '注册成功！请登录您的账户。',
              '注册成功',
              {
                confirmButtonText: '立即登录',
                type: 'success',
                callback: () => {
                  router.push('/login')
                }
              }
          )
        }
      } catch (error) {
        ElMessage({
          message: error.message || '注册失败',
          type: 'error',
          duration: 3000
        })
      } finally {
        loading.value = false
      }
    }
  })
}

// 跳转到登录页面
const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped lang="scss">
.register-container {
  display: flex;
  height: 100vh;
  background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
  overflow: hidden;
  position: relative;
}

.register-bg {
  position: absolute;
  width: 100%;
  height: 100%;

  .bg-shape {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);

    &-1 {
      width: 400px;
      height: 400px;
      top: -200px;
      left: -200px;
    }

    &-2 {
      width: 300px;
      height: 300px;
      bottom: -150px;
      right: 20%;
    }
  }
}

.back-btn {
  position: absolute;
  top: 20px;
  left: 20px;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.2);
  border: none;

  &:hover {
    background: rgba(255, 255, 255, 0.3);
  }
}

.register-form-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  z-index: 1;
  overflow-y: auto;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
  width: 100%;
  max-width: 500px;

  .register-title {
    font-size: 32px;
    font-weight: 600;
    color: #2c3e50;
    margin-bottom: 8px;
    background: linear-gradient(45deg, #6a11cb, #2575fc);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }

  .register-subtitle {
    font-size: 16px;
    color: #666;
  }
}

.register-form {
  width: 100%;
  max-width: 500px;

  .el-form-item {
    margin-bottom: 20px;
  }

  .role-radio-group {
    width: 100%;

    .el-radio-button {
      flex: 1;

      :deep(.el-radio-button__inner) {
        width: 100%;
        padding: 20px;
        border-radius: 8px;
        border: 1px solid #e0e0e0;
        background: #f8f9fa;
        color: #666;

        &:hover {
          background: #e6f7ff;
          border-color: #409EFF;
        }
      }

      &.is-active {
        .el-radio-button__inner {
          background: #409EFF;
          border-color: #409EFF;
          color: white;
          box-shadow: none;
        }
      }
    }

    .role-option {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;

      span {
        font-size: 14px;
      }
    }
  }

  .register-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    margin-top: 10px;
  }
}

.login-link {
  text-align: center;
  margin: 20px 0;
  color: #666;

  .el-link {
    font-weight: 500;
  }
}

.register-info-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: white;
  z-index: 1;
}

.register-info-content {
  max-width: 500px;

  h2 {
    font-size: 36px;
    font-weight: 600;
    margin-bottom: 30px;
  }

  .benefit-list {
    .benefit-item {
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

  .stats {
    display: flex;
    justify-content: space-between;
    margin-top: 40px;
    padding-top: 40px;
    border-top: 1px solid rgba(255, 255, 255, 0.2);

    .stat-item {
      text-align: center;

      .stat-number {
        font-size: 32px;
        font-weight: 700;
        margin-bottom: 5px;
      }

      .stat-label {
        font-size: 14px;
        opacity: 0.8;
      }
    }
  }
}

// 响应式设计
@media (max-width: 1024px) {
  .register-info-panel {
    display: none;
  }
}

@media (max-width: 768px) {
  .register-form-container {
    padding: 20px;
  }

  .register-form {
    .role-radio-group {
      flex-direction: column;
      gap: 10px;
    }
  }
}
</style>