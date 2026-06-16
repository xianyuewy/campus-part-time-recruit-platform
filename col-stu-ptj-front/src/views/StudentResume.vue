<template>
  <div class="feature-page">
    <section class="page-hero page-hero--resume">
      <div class="page-hero-bg" aria-hidden="true" />
      <div class="page-hero-inner">
        <span class="page-hero-badge"><el-icon><Document /></el-icon> 求职资料</span>
        <h1 class="page-hero-title">我的<span class="accent">简历</span></h1>
        <p class="page-hero-sub">完善简历后，企业在候选人收件箱中可查看，便于沟通与筛选；可额外上传 PDF 附件。</p>
      </div>
    </section>

    <div class="feature-body">
      <div v-loading="loading" class="form-card">
        <div class="form-section">
          <div class="section-head">
            <el-icon><Paperclip /></el-icon>
            <span>简历附件</span>
          </div>
          <div class="attach-row">
            <el-upload :show-file-list="false" accept=".pdf,application/pdf" :disabled="uploading" :http-request="onUploadResume">
              <el-button type="primary" round :loading="uploading">上传 PDF</el-button>
            </el-upload>
            <template v-if="form.attachmentPath">
              <el-link type="primary" :href="attachmentHref" target="_blank" rel="noopener">
                {{ form.attachmentOriginalName || '查看附件' }}
              </el-link>
              <span class="hint">企业端可下载</span>
            </template>
            <span v-else class="hint">未上传附件</span>
          </div>
        </div>

        <div class="form-section">
          <div class="section-head"><el-icon><EditPen /></el-icon><span>在线简历</span></div>
          <el-form label-position="top" class="resume-form">
            <el-form-item label="自我介绍">
              <el-input v-model="form.selfIntro" type="textarea" :rows="4" maxlength="5000" show-word-limit placeholder="简要介绍自己" />
            </el-form-item>
            <el-form-item label="教育经历">
              <el-input v-model="form.education" type="textarea" :rows="4" maxlength="5000" show-word-limit placeholder="学校、专业、在读情况等" />
            </el-form-item>
            <el-form-item label="技能特长">
              <el-input v-model="form.skills" type="textarea" :rows="3" maxlength="5000" show-word-limit placeholder="语言能力、证书、擅长工具等" />
            </el-form-item>
            <el-form-item label="工作/实践">
              <el-input v-model="form.workExperience" type="textarea" :rows="4" maxlength="5000" show-word-limit placeholder="兼职、实习、项目经历等" />
            </el-form-item>
          </el-form>
        </div>

        <div class="form-footer">
          <el-button type="primary" size="large" round :loading="saving" @click="save">保存简历</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, EditPen, Paperclip } from '@element-plus/icons-vue'
import { getMyResume, saveMyResume, uploadResumeAttachment } from '../api/student'
import { API_BASE_URL } from '../utils/request'

const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const form = reactive({
  selfIntro: '',
  education: '',
  skills: '',
  workExperience: '',
  attachmentPath: '',
  attachmentOriginalName: ''
})

const attachmentHref = computed(() => {
  const p = form.attachmentPath
  if (!p) return '#'
  const base = API_BASE_URL.replace(/\/$/, '')
  return p.startsWith('/') ? `${base}${p}` : `${base}/${p}`
})

const load = async () => {
  loading.value = true
  try {
    const res = await getMyResume()
    if (res.success && res.data) {
      form.selfIntro = res.data.selfIntro || ''
      form.education = res.data.education || ''
      form.skills = res.data.skills || ''
      form.workExperience = res.data.workExperience || ''
      form.attachmentPath = res.data.attachmentPath || ''
      form.attachmentOriginalName = res.data.attachmentOriginalName || ''
    }
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const save = async () => {
  saving.value = true
  try {
    await saveMyResume({ ...form })
    ElMessage.success('已保存')
    await load()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const onUploadResume = async ({ file }) => {
  if (!file) return
  const fd = new FormData()
  fd.append('file', file)
  uploading.value = true
  try {
    const res = await uploadResumeAttachment(fd)
    if (res.success && res.data) {
      form.attachmentPath = res.data.attachmentPath || ''
      form.attachmentOriginalName = res.data.attachmentOriginalName || ''
      ElMessage.success('附件已上传')
    }
  } catch (e) {
    ElMessage.error(e.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
@use '../styles/feature-page.scss';

.form-section {
  padding: 22px 28px;
  border-bottom: 1px solid #f0f3f8;

  &:last-of-type {
    border-bottom: none;
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
    color: #3d7a8f;
    font-size: 18px;
  }
}

.attach-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.hint {
  font-size: 13px;
  color: #909399;
}

.resume-form {
  max-width: 720px;

  :deep(.el-form-item__label) {
    font-weight: 500;
  }

  :deep(.el-textarea__inner),
  :deep(.el-input__wrapper) {
    border-radius: 10px;
  }
}

.form-footer {
  padding: 20px 28px 28px;
  background: linear-gradient(180deg, #fafbfc, #fff);
  border-top: 1px solid #f0f3f8;
}
</style>
