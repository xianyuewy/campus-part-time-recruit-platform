<template>
  <div class="company-profile-page">
    <section class="page-hero">
      <div class="page-hero-bg" aria-hidden="true" />
      <div class="page-hero-inner">
        <div class="page-hero-copy">
          <span class="page-hero-badge">
            <el-icon><OfficeBuilding /></el-icon>
            企业中心
          </span>
          <h1 class="page-hero-title">企业<span class="accent">资料</span></h1>
          <p class="page-hero-sub">完善企业信息与资质，审核通过后可正常发布岗位并接收投递。</p>
        </div>
        <div class="status-panel">
          <span class="status-panel-label">资质状态</span>
          <el-tag :type="statusTagType" size="large" effect="dark" round>{{ statusText }}</el-tag>
          <p v-if="userStore.authStatus === 'REJECTED'" class="status-hint">请修改资料后重新提交审核</p>
          <p v-else-if="userStore.authStatus === 'PENDING'" class="status-hint">审核中，请耐心等待</p>
          <p v-else-if="userStore.authStatus !== 'APPROVED'" class="status-hint">填写完整信息后可提交资质审核</p>
        </div>
      </div>
    </section>

    <div class="page-body">
      <div class="form-card">
        <div class="form-section">
          <div class="section-head">
            <el-icon><Document /></el-icon>
            <span>基本信息</span>
          </div>
          <el-form label-position="top" class="profile-form">
            <el-row :gutter="24">
              <el-col :xs="24" :sm="12">
                <el-form-item label="企业名称" required>
                  <el-input v-model="form.companyName" placeholder="营业执照上的企业全称" maxlength="200" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="营业执照号" required>
                  <el-input v-model="form.licenseNo" placeholder="统一社会信用代码或注册号" maxlength="64" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>

        <div class="form-section">
          <div class="section-head">
            <el-icon><Phone /></el-icon>
            <span>联系方式</span>
          </div>
          <el-form label-position="top" class="profile-form">
            <el-row :gutter="24">
              <el-col :xs="24" :sm="12">
                <el-form-item label="联系人">
                  <el-input v-model="form.contactName" placeholder="对接招聘的负责人姓名" maxlength="64" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="联系电话">
                  <el-input v-model="form.contactPhone" placeholder="手机或座机" maxlength="32" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>

        <div class="form-section form-section--last">
          <div class="section-head">
            <el-icon><EditPen /></el-icon>
            <span>企业介绍</span>
          </div>
          <el-form label-position="top" class="profile-form">
            <el-form-item label="简介">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="5"
                maxlength="2000"
                show-word-limit
                placeholder="主营业务、规模、招聘优势等，便于学生了解企业"
              />
            </el-form-item>
          </el-form>
        </div>

        <div class="form-footer">
          <el-button type="primary" size="large" round :icon="Check" :loading="saving" @click="save">保存资料</el-button>
          <el-button
            type="success"
            size="large"
            round
            :icon="Upload"
            :disabled="userStore.authStatus === 'PENDING' || userStore.authStatus === 'APPROVED'"
            :loading="auditing"
            @click="submitAudit"
          >
            提交资质审核
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, Document, EditPen, OfficeBuilding, Phone, Upload } from '@element-plus/icons-vue'
import { getCompanyProfile, submitCompanyAudit, upsertCompanyProfile } from '../api/company'
import { useUserStore } from '../store/user'

const userStore = useUserStore()
const form = reactive({
  companyName: '',
  licenseNo: '',
  contactName: '',
  contactPhone: '',
  description: ''
})
const saving = ref(false)
const auditing = ref(false)

const load = async () => {
  const res = await getCompanyProfile()
  if (res.success && res.data) Object.assign(form, res.data)
}

const save = async () => {
  if (!form.companyName?.trim() || !form.licenseNo?.trim()) {
    ElMessage.warning('请填写企业名称与执照号')
    return
  }
  saving.value = true
  try {
    await upsertCompanyProfile(form)
    ElMessage.success('保存成功')
  } catch (e) {
    ElMessage.error(e.message || '失败')
  } finally {
    saving.value = false
  }
}

const submitAudit = async () => {
  if (!form.companyName?.trim() || !form.licenseNo?.trim()) {
    ElMessage.warning('请先完善企业名称与执照号并保存')
    return
  }
  auditing.value = true
  try {
    await submitCompanyAudit()
    await userStore.getUserInfo()
    ElMessage.success('已提交审核，请等待管理员处理')
  } catch (e) {
    ElMessage.error(e.message || '提交失败')
  } finally {
    auditing.value = false
  }
}

const statusText = computed(() => {
  const s = userStore.authStatus
  if (s === 'APPROVED') return '已认证'
  if (s === 'PENDING') return '审核中'
  if (s === 'REJECTED') return '审核未通过'
  return '未认证'
})

const statusTagType = computed(() => {
  const s = userStore.authStatus
  if (s === 'APPROVED') return 'success'
  if (s === 'PENDING') return 'warning'
  if (s === 'REJECTED') return 'danger'
  return 'info'
})

onMounted(async () => {
  await userStore.getUserInfo()
  await load()
})
</script>

<style scoped lang="scss">
.company-profile-page {
  margin: -8px -4px 0;
  min-height: 100%;
}

.page-hero {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #3d3a6b 0%, #5a4d8c 45%, #7b6bae 100%);
  color: #fff;
}

.page-hero-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 90% 10%, rgba(255, 255, 255, 0.14), transparent 40%),
    radial-gradient(circle at 5% 90%, rgba(200, 180, 255, 0.2), transparent 50%);
  pointer-events: none;
}

.page-hero-inner {
  position: relative;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  padding: 28px 32px;
  flex-wrap: wrap;
}

.page-hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  padding: 5px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.15);
  margin-bottom: 10px;
}

.page-hero-title {
  margin: 0 0 8px;
  font-size: 26px;
  font-weight: 700;

  .accent {
    color: #ddd6fe;
  }
}

.page-hero-sub {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
  max-width: 420px;
  line-height: 1.55;
}

.status-panel {
  flex-shrink: 0;
  text-align: right;
  padding: 12px 18px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(8px);
}

.status-panel-label {
  display: block;
  font-size: 12px;
  opacity: 0.85;
  margin-bottom: 8px;
}

.status-hint {
  margin: 10px 0 0;
  font-size: 12px;
  opacity: 0.8;
  max-width: 200px;
  margin-left: auto;
}

.form-card {
  background: #fff;
  border-radius: 16px;
  box-shadow:
    0 1px 2px rgba(15, 23, 42, 0.04),
    0 10px 36px rgba(15, 23, 42, 0.06);
  border: 1px solid #eef1f6;
  overflow: hidden;
}

.form-section {
  padding: 22px 28px 8px;
  border-bottom: 1px solid #f0f3f8;

  &--last {
    border-bottom: none;
    padding-bottom: 4px;
  }
}

.section-head {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;

  .el-icon {
    color: #5a4d8c;
    font-size: 18px;
  }
}

.profile-form {
  :deep(.el-form-item__label) {
    font-weight: 500;
    color: #606266;
    padding-bottom: 4px;
  }

  :deep(.el-input__wrapper),
  :deep(.el-textarea__inner) {
    border-radius: 10px;
  }
}

.form-footer {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 20px 28px 28px;
  background: linear-gradient(180deg, #fafbfc, #fff);
  border-top: 1px solid #f0f3f8;
}

@media (max-width: 768px) {
  .page-hero-inner {
    flex-direction: column;
    padding: 20px;
  }

  .status-panel {
    text-align: left;
    width: 100%;
  }

  .status-hint {
    margin-left: 0;
  }
}
</style>
