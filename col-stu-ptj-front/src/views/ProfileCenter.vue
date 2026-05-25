<template>
  <div class="profile-page">
    <el-page-header v-if="!isAdminShell && !inMainLayout" @back="goBack" title="返回" />
    <h2 class="title">个人中心</h2>

    <el-tabs v-model="activeTab" type="border-card" class="tabs">
      <el-tab-pane label="基本信息" name="info">
        <el-form :model="form" label-width="100px" class="profile-form">
          <el-form-item label="用户名">
            <el-input v-model="form.username" disabled />
          </el-form-item>
          <el-form-item label="角色">
            <el-tag>{{ roleLabel }}</el-tag>
          </el-form-item>
          <el-form-item label="认证状态">
            <el-tag :type="authStatusTagType">{{ authStatusLabel }}</el-tag>
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="form.nickname" maxlength="64" show-word-limit placeholder="对外展示时优先于登录名" />
          </el-form-item>
          <el-form-item :label="realNameLabel">
            <el-input v-model="form.realName" maxlength="64" show-word-limit :placeholder="realNamePlaceholder" />
          </el-form-item>
          <p class="field-hint text-muted" v-if="userStore.isCompany">对外名称优先使用「企业资料」中的企业名称；昵称为空时将显示登录名。</p>
          <el-form-item label="邮箱">
            <el-input v-model="form.email" placeholder="常用邮箱" />
          </el-form-item>
          <el-form-item label="手机">
            <el-input v-model="form.phone" placeholder="选填" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="saveInfo">保存</el-button>
          </el-form-item>
        </el-form>
        <el-card v-if="userStore.isStudent" class="student-audit-card mt16" shadow="never">
          <template #header>
            <div class="audit-head">
              <span>学生资质认证</span>
              <el-tag :type="authStatusTagType">{{ authStatusLabel }}</el-tag>
            </div>
          </template>
          <p class="audit-tip">
            请上传学信网截图或学生证照片，提交后由管理员审核。上传新材料后需要重新提交审核。
          </p>
          <div class="audit-actions">
            <el-link v-if="studentQualification.idCard" :href="qualificationUrl" target="_blank" type="primary">
              查看已上传材料
            </el-link>
            <span v-else class="text-muted">暂未上传认证材料</span>
            <el-upload
              :show-file-list="false"
              accept="image/jpeg,image/png,image/gif,image/webp"
              :before-upload="beforeAvatarUpload"
              :http-request="doUploadStudentQualification"
            >
              <el-button type="primary" plain :loading="studentUploadLoading">上传认证材料</el-button>
            </el-upload>
            <el-button
              type="success"
              :loading="studentSubmitLoading"
              :disabled="!canSubmitStudentAudit"
              @click="submitStudentQualificationAudit"
            >
              提交审核
            </el-button>
          </div>
          <p v-if="studentQualification.authRemark" class="audit-remark">审核意见：{{ studentQualification.authRemark }}</p>
        </el-card>
        <el-alert
          v-if="userStore.isCompany"
          type="info"
          show-icon
          :closable="false"
          title="企业资料（执照、联系人等）请在企业资料页维护"
          class="mt16"
        />
        <el-button v-if="userStore.isCompany" type="primary" plain class="mt8" @click="router.push('/company/profile')">
          前往企业资料
        </el-button>
      </el-tab-pane>

      <el-tab-pane label="头像" name="avatar">
        <div class="avatar-block">
          <el-avatar :size="120" :src="avatarPreview" />
          <el-upload
            class="upload"
            :show-file-list="false"
            accept="image/jpeg,image/png,image/gif,image/webp"
            :before-upload="beforeAvatarUpload"
            :http-request="doUploadAvatar"
          >
            <el-button type="primary" :loading="uploading">上传新头像</el-button>
          </el-upload>
          <p class="hint">支持 jpg/png/gif/webp，单张最大 10MB。保存后立即在全局生效。</p>
        </div>
      </el-tab-pane>

      <el-tab-pane label="账户安全" name="security">
        <el-form :model="pwd" label-width="100px" style="max-width: 420px">
          <el-form-item label="当前密码">
            <el-input v-model="pwd.oldPassword" type="password" show-password autocomplete="off" />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="pwd.newPassword" type="password" show-password autocomplete="new-password" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="pwdLoading" @click="savePassword">修改密码</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
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
const inMainLayout = computed(() => route.matched.some((r) => r.meta && r.meta.mainLayout))

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

const realNameLabel = computed(() => {
  if (form.role === 'COMPANY') return '企业名称'
  return '真实姓名'
})
const realNamePlaceholder = computed(() => {
  if (form.role === 'COMPANY') return '可填与执照一致的企业名称，便于核对'
  if (form.role === 'STUDENT') return '与证件一致的真实姓名，选填'
  return '选填'
})

const pwd = reactive({
  oldPassword: '',
  newPassword: ''
})
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
    await changeUserPassword({
      oldPassword: pwd.oldPassword,
      newPassword: pwd.newPassword
    })
    ElMessage.success('密码已修改，所有登录端已失效，请使用新密码重新登录')
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

const goBack = () => {
  if (isAdminShell.value) router.push('/admin/dashboard')
  else router.push('/home')
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

onMounted(() => {
  load()
})
</script>

<style scoped>
.profile-page {
  max-width: 1080px;
  margin: 0 auto;
  padding: 20px 16px;
}
.title {
  margin: 12px 0 18px;
  font-size: 24px;
}
.tabs {
  border-radius: 12px;
}
.profile-form {
  max-width: 680px;
}
.avatar-block {
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
.mt8 {
  margin-top: 8px;
}
.mt16 {
  margin-top: 16px;
}
.student-audit-card {
  max-width: 820px;
}
.audit-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.audit-tip {
  margin: 0 0 12px;
  color: #606266;
}
.audit-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}
.audit-remark {
  margin: 12px 0 0;
  color: #e6a23c;
}
.text-muted {
  color: #909399;
}
.field-hint {
  margin: -6px 0 12px;
  font-size: 13px;
  max-width: 680px;
}
</style>
