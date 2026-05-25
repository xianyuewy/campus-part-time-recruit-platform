<template>
  <div class="page">
    <el-page-header @back="router.push('/home')" title="返回" />
    <h2 class="title">我的简历</h2>
    <p class="desc">完善简历后，企业在「候选人收件箱」中可查看，便于沟通与筛选。可额外上传 PDF 简历附件。</p>
    <el-card v-loading="loading">
      <el-form label-width="100px" style="max-width: 720px">
        <el-form-item label="简历附件">
          <div class="attach-row">
            <el-upload
              :show-file-list="false"
              accept=".pdf,application/pdf"
              :disabled="uploading"
              :http-request="onUploadResume"
            >
              <el-button type="primary" plain :loading="uploading">上传 PDF</el-button>
            </el-upload>
            <template v-if="form.attachmentPath">
              <el-link type="primary" :href="attachmentHref" target="_blank" rel="noopener">
                {{ form.attachmentOriginalName || '查看附件' }}
              </el-link>
              <span class="hint">（企业端可下载）</span>
            </template>
            <span v-else class="hint">未上传附件</span>
          </div>
        </el-form-item>
        <el-form-item label="自我介绍">
          <el-input v-model="form.selfIntro" type="textarea" :rows="4" maxlength="5000" show-word-limit placeholder="简要介绍自己" />
        </el-form-item>
        <el-form-item label="教育经历">
          <el-input v-model="form.education" type="textarea" :rows="4" maxlength="5000" show-word-limit placeholder="学校、专业、在读情况等" />
        </el-form-item>
        <el-form-item label="技能特长">
          <el-input v-model="form.skills" type="textarea" :rows="3" maxlength="5000" show-word-limit placeholder="如语言能力、证书、擅长工具等" />
        </el-form-item>
        <el-form-item label="工作/实践">
          <el-input v-model="form.workExperience" type="textarea" :rows="4" maxlength="5000" show-word-limit placeholder="兼职、实习、项目经历等" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="save">保存简历</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyResume, saveMyResume, uploadResumeAttachment } from '../api/student'
import { API_BASE_URL } from '../utils/request'

const router = useRouter()
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

<style scoped>
.page {
  padding: 16px 24px 32px;
}
.title {
  margin: 12px 0 8px;
}
.desc {
  color: #909399;
  font-size: 14px;
  margin: 0 0 16px;
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
</style>
