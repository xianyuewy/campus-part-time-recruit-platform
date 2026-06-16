<template>
  <div class="feature-page profile-page">
    <section class="page-hero page-hero--profile" :class="{ 'page-hero--admin': isAdminShell }">
      <div class="page-hero-bg" aria-hidden="true" />
      <div class="page-hero-inner page-hero-inner--split">
        <div class="hero-left">
          <span class="page-hero-badge"><el-icon><User /></el-icon> 账户设置</span>
          <h1 class="page-hero-title">个人<span class="accent">中心</span></h1>
          <p class="page-hero-sub">管理基本信息、头像与密码；学生可在此提交资质认证。</p>
        </div>
        <div class="hero-avatar">
          <el-avatar :size="72" :src="avatarPreview" />
          <span class="hero-username">{{ form.nickname || form.username }}</span>
          <el-tag :type="authStatusTagType" size="small" effect="dark" round>{{ authStatusLabel }}</el-tag>
        </div>
      </div>
    </section>

    <div class="feature-body">
      <div class="profile-card">
        <el-tabs v-model="activeTab" class="styled-tabs profile-tabs">
          <el-tab-pane label="基本信息" name="info">
            <div class="tab-panel">
              <el-form :model="form" label-position="top" class="profile-form">
                <el-row :gutter="24">
                  <el-col :xs="24" :sm="12">
                    <el-form-item label="用户名">
                      <el-input v-model="form.username" disabled />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12">
                    <el-form-item label="角色">
                      <el-tag>{{ roleLabel }}</el-tag>
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12">
                    <el-form-item label="昵称">
                      <el-input v-model="form.nickname" maxlength="64" show-word-limit placeholder="对外展示时优先于登录名" />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12">
                    <el-form-item :label="realNameLabel">
                      <el-input v-model="form.realName" maxlength="64" show-word-limit :placeholder="realNamePlaceholder" />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12">
                    <el-form-item label="邮箱">
                      <el-input v-model="form.email" placeholder="常用邮箱" />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12">
                    <el-form-item label="手机">
                      <el-input v-model="form.phone" placeholder="选填" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <p v-if="userStore.isCompany" class="field-hint">对外名称优先使用「企业资料」中的企业名称；昵称为空时将显示登录名。</p>
                <el-button type="primary" round :loading="saving" @click="saveInfo">保存信息</el-button>
              </el-form>

              <div v-if="userStore.isStudent" class="audit-block">
                <div class="audit-block-head">
                  <span>学生资质认证</span>
                  <el-tag :type="authStatusTagType" effect="light" round>{{ authStatusLabel }}</el-tag>
                </div>
                <p class="audit-tip">请上传学信网截图或学生证照片，提交后由管理员审核。上传新材料后需重新提交。</p>
                <div class="audit-actions">
                  <el-link v-if="studentQualification.idCard" :href="qualificationUrl" target="_blank" type="primary">查看已上传材料</el-link>
                  <span v-else class="text-muted">暂未上传认证材料</span>
                  <el-upload
                    :show-file-list="false"
                    accept="image/jpeg,image/png,image/gif,image/webp"
                    :before-upload="beforeAvatarUpload"
                    :http-request="doUploadStudentQualification"
                  >
                    <el-button type="primary" plain round :loading="studentUploadLoading">上传认证材料</el-button>
                  </el-upload>
                  <el-button
                    type="success"
                    round
                    :loading="studentSubmitLoading"
                    :disabled="!canSubmitStudentAudit"
                    @click="submitStudentQualificationAudit"
                  >
                    提交审核
                  </el-button>
                </div>
                <p v-if="studentQualification.authRemark" class="audit-remark">审核意见：{{ studentQualification.authRemark }}</p>
              </div>

              <el-alert
                v-if="userStore.isCompany"
                type="info"
                show-icon
                :closable="false"
                class="company-alert"
                title="企业资料（执照、联系人等）请在企业资料页维护"
              />
              <el-button v-if="userStore.isCompany" type="primary" plain round @click="router.push('/company/profile')">
                前往企业资料
              </el-button>
            </div>
          </el-tab-pane>

          <el-tab-pane label="头像" name="avatar">
            <div class="tab-panel avatar-panel">
              <el-avatar :size="120" :src="avatarPreview" />
              <el-upload
                :show-file-list="false"
                accept="image/jpeg,image/png,image/gif,image/webp"
                :before-upload="beforeAvatarUpload"
                :http-request="doUploadAvatar"
              >
                <el-button type="primary" round :loading="uploading">上传新头像</el-button>
              </el-upload>
              <p class="hint">支持 jpg/png/gif/webp，单张最大 10MB，保存后全局生效。</p>
            </div>
          </el-tab-pane>

          <el-tab-pane label="账户安全" name="security">
            <div class="tab-panel">
              <el-form :model="pwd" label-position="top" class="profile-form security-form">
                <el-form-item label="当前密码">
                  <el-input v-model="pwd.oldPassword" type="password" show-password autocomplete="off" />
                </el-form-item>
                <el-form-item label="新密码">
                  <el-input v-model="pwd.newPassword" type="password" show-password autocomplete="new-password" />
                </el-form-item>
                <el-button type="primary" round :loading="pwdLoading" @click="savePassword">修改密码</el-button>
              </el-form>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'
import {
  updateUserProfile,
  changeUserPassword,
  uploadUserAvatar,
  getCurrentUser,
  getStudentQualification,
  uploadStudentQualification,
  submitStudentAudit
} from '../api/auth'
import { API_BASE_URL } from '../utils/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isAdminShell = computed(() => route.path.startsWith('/admin'))

const activeTab = ref('info')
const saving = ref(false)
const uploading = ref(false)
const pwdLoading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  realName: '',
  email: '',
  phone: '',
  authStatus: '',
  role: ''
})

const realNameLabel = computed(() => (form.role === 'COMPANY' ? '企业名称' : '真实姓名'))
const realNamePlaceholder = computed(() => {
  if (form.role === 'COMPANY') return '可填与执照一致的企业名称'
  if (form.role === 'STUDENT') return '与证件一致的真实姓名，选填'
  return '选填'
})

const pwd = reactive({ oldPassword: '', newPassword: '' })
const studentUploadLoading = ref(false)
const studentSubmitLoading = ref(false)
const studentQualification = reactive({
  idCard: '',
  authStatus: '',
  authRemark: '',
  authTime: ''
})

const roleLabel = computed(() => {
  const r = form.role
  if (r === 'STUDENT') return '学生'
  if (r === 'COMPANY') return '企业'
  if (r === 'ADMIN') return '管理员'
  return r || '—'
})

const authLabelMap = {
  UNAUTH: '未认证',
  PENDING: '审核中',
  APPROVED: '已认证',
  REJECTED: '认证失败'
}
const authStatusLabel = computed(() => authLabelMap[form.authStatus] || form.authStatus || '—')
const authStatusTagType = computed(() => {
  if (form.authStatus === 'APPROVED') return 'success'
  if (form.authStatus === 'PENDING') return 'warning'
  if (form.authStatus === 'REJECTED') return 'danger'
  return 'info'
})
const qualificationUrl = computed(() => resolveAvatarUrl(studentQualification.idCard))
const canSubmitStudentAudit = computed(() => {
  if (!userStore.isStudent) return false
  if (!studentQualification.idCard) return false
  return form.authStatus !== 'PENDING' && form.authStatus !== 'APPROVED'
})

function resolveAvatarUrl(path) {
  if (!path) return '/avatars/default.png'
  if (path.startsWith('http')) return path
  return API_BASE_URL + (path.startsWith('/') ? path : '/' + path)
}

const avatarPreview = computed(() => resolveAvatarUrl(userStore.userInfo?.avatar))

const load = async () => {
  try {
    const res = await getCurrentUser()
    if (res.success && res.data) {
      const u = res.data
      form.username = u.username || ''
      form.nickname = u.nickname || ''
      form.realName = u.realName || ''
      form.email = u.email || ''
      form.phone = u.phone || ''
      form.authStatus = u.authStatus || ''
      form.role = typeof u.role === 'string' ? u.role : u.role?.name || ''
      userStore.userInfo = res.data
      localStorage.setItem('userInfo', JSON.stringify(res.data))
    }
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  }
  await loadStudentQualification()
}

const loadStudentQualification = async () => {
  if (!userStore.isStudent) return
  try {
    const res = await getStudentQualification()
    if (res.success && res.data) {
      studentQualification.idCard = res.data.idCard || ''
      studentQualification.authStatus = res.data.authStatus || ''
      studentQualification.authRemark = res.data.authRemark || ''
      studentQualification.authTime = res.data.authTime || ''
    }
  } catch {
    // ignore
  }
}

const MAX_AVATAR_BYTES = 10 * 1024 * 1024

const beforeAvatarUpload = (rawFile) => {
  if (rawFile.size > MAX_AVATAR_BYTES) {
    ElMessage.error('头像大小不能超过 10MB')
    return false
  }
  return true
}

const saveInfo = async () => {
  saving.value = true
  try {
    await updateUserProfile({
      email: form.email || undefined,
      phone: form.phone,
      nickname: form.nickname,
      realName: form.realName
    })
    ElMessage.success('已保存')
    await load()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const doUploadAvatar = async (options) => {
  const raw = options.file?.raw ?? options.file
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', raw, raw.name || 'avatar.jpg')
    const res = await uploadUserAvatar(fd)
    if (res.success && res.data?.avatar) {
      ElMessage.success('头像已更新')
      await load()
    }
  } catch (e) {
    ElMessage.error(e.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

const doUploadStudentQualification = async (options) => {
  const raw = options.file?.raw ?? options.file
  studentUploadLoading.value = true
  try {
    const fd = new FormData()
    fd.append('file', raw, raw.name || 'qualification.jpg')
    await uploadStudentQualification(fd)
    ElMessage.success('认证材料已上传，请提交审核')
    await load()
  } catch (e) {
    ElMessage.error(e.message || '上传失败')
  } finally {
    studentUploadLoading.value = false
  }
}

const submitStudentQualificationAudit = async () => {
  studentSubmitLoading.value = true
  try {
    await submitStudentAudit()
    ElMessage.success('已提交审核，请耐心等待')
    await load()
  } catch (e) {
    ElMessage.error(e.message || '提交失败')
  } finally {
    studentSubmitLoading.value = false
  }
}

const savePassword = async () => {
  if (!pwd.oldPassword || !pwd.newPassword) {
    ElMessage.warning('请填写完整')
    return
  }
  if (pwd.newPassword.length < 6) {
    ElMessage.warning('新密码至少 6 位')
    return
  }
  pwdLoading.value = true
  try {
    await changeUserPassword({ oldPassword: pwd.oldPassword, newPassword: pwd.newPassword })
    ElMessage.success('密码已修改，请使用新密码重新登录')
    pwd.oldPassword = ''
    pwd.newPassword = ''
    userStore.resetToken()
    router.push('/login')
  } catch (e) {
    ElMessage.error(e.message || '修改失败')
  } finally {
    pwdLoading.value = false
  }
}

watch(
  () => route.query.tab,
  (t) => {
    if (t === 'security') activeTab.value = 'security'
    else if (t === 'avatar') activeTab.value = 'avatar'
    else if (t === 'info') activeTab.value = 'info'
  },
  { immediate: true }
)

onMounted(() => load())
</script>

<style scoped lang="scss">
@use '../styles/feature-page.scss';

.page-hero--admin {
  background: linear-gradient(135deg, #1a2332 0%, #2d3f5c 48%, #3d5a80 100%) !important;
}

.hero-left {
  flex: 1;
  min-width: 200px;
}

.hero-avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.12);
}

.hero-username {
  font-size: 14px;
  font-weight: 600;
}

.profile-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04), 0 10px 36px rgba(15, 23, 42, 0.06);
  border: 1px solid #eef1f6;
  overflow: hidden;
}

.profile-tabs {
  :deep(.el-tabs__nav-wrap) {
    padding: 0 20px;
  }
}

.tab-panel {
  padding: 24px 28px 28px;
  max-width: 820px;
}

.profile-form {
  :deep(.el-form-item__label) {
    font-weight: 500;
  }
  :deep(.el-input__wrapper) {
    border-radius: 10px;
  }
}

.security-form {
  max-width: 420px;
}

.field-hint {
  margin: -8px 0 16px;
  font-size: 13px;
  color: #909399;
}

.audit-block {
  margin-top: 28px;
  padding: 20px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #eef1f6;
}

.audit-block-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  margin-bottom: 10px;
}

.audit-tip {
  margin: 0 0 14px;
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.audit-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.audit-remark {
  margin: 14px 0 0;
  color: #e6a23c;
  font-size: 13px;
}

.company-alert {
  margin: 20px 0 12px;
  border-radius: 10px;
}

.avatar-panel {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 16px;
}

.hint {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

.text-muted {
  color: #909399;
}
</style>
