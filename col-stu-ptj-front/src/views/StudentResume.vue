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
        <!-- 原有：简历附件 完全不动 -->
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

        <!-- ========== 新增：AI 智能解析区块 ========== -->
        <div class="form-section">
          <div class="section-head">
            <el-icon><MagicStick /></el-icon>
            <span>AI 智能解析</span>
          </div>
          <div class="attach-row">
            <el-upload
                :show-file-list="false"
                accept=".pdf,.docx"
                :disabled="parsing"
                :http-request="onParseResume"
            >
              <el-button type="success" round :loading="parsing">
                上传简历自动填充
              </el-button>
            </el-upload>
            <span class="hint">支持 PDF / DOCX 格式，解析后自动填入下方表单，请核对后保存</span>
          </div>
        </div>
        <!-- ========== 新增结束 ========== -->

        <!-- 原有：在线简历 完全不动 -->
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
import { computed, onMounted, onBeforeUnmount, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, EditPen, Paperclip, MagicStick } from '@element-plus/icons-vue'
// 替换引入：去掉原来的 parseResume，换成两个异步接口
import { getMyResume, saveMyResume, uploadResumeAttachment, submitParseTask, getParseResult } from '../api/student'
import { API_BASE_URL } from '../utils/request'

const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const parsing = ref(false)
// 轮询定时器，用于组件销毁时清理
let pollTimer = null

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

// ========== AI 简历解析（异步轮询版·修复后） ==========
const onParseResume = async ({ file }) => {
  // 1. 文件格式校验
  const fileName = file.name.toLowerCase()
  const isLegal = fileName.endsWith('.pdf') || fileName.endsWith('.docx')
  if (!isLegal) {
    ElMessage.error('仅支持 PDF 和 DOCX 格式的简历文件')
    return
  }
  if (file.size / 1024 / 1024 > 10) {
    ElMessage.error('文件大小不能超过 10MB')
    return
  }

  parsing.value = true
  // 清空上一次的定时器
  if (pollTimer) clearInterval(pollTimer)

  try {
    const fd = new FormData()
    fd.append('file', file)

    // 2. 提交解析任务
    const submitRes = await submitParseTask(fd)
    // 对齐项目原有接口写法：直接用 res.success 判断
    if (!submitRes.success) {
      ElMessage.error(submitRes.message || '提交解析失败')
      parsing.value = false
      return
    }
    const taskId = submitRes.data

    // 3. 启动轮询查询结果
    let count = 0
    const maxCount = 45

    pollTimer = setInterval(async () => {
      count++
      try {
        const res = await getParseResult(taskId)

        // 解析完成：有数据就回填
        if (res.success && res.data) {
          clearInterval(pollTimer)
          pollTimer = null
          const data = res.data
          form.education = data.school || form.education
          form.skills = data.skills?.join('、') || form.skills
          form.workExperience = data.experience || form.workExperience
          form.selfIntro = data.selfEvaluation || form.selfIntro
          ElMessage.success('解析完成，请核对信息后手动保存')
          parsing.value = false
        }

        // 超时处理
        if (count >= maxCount) {
          clearInterval(pollTimer)
          pollTimer = null
          ElMessage.error('解析超时，请重试')
          parsing.value = false
        }
      } catch (err) {
        clearInterval(pollTimer)
        pollTimer = null
        ElMessage.error('查询解析结果失败')
        parsing.value = false
      }
    }, 1000)

  } catch (e) {
    console.error('提交解析异常详情：', e) // 报错时看控制台
    ElMessage.error('解析提交失败，请检查网络')
    parsing.value = false
  }
}

// 组件销毁时清理定时器，防止内存泄漏
onBeforeUnmount(() => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
})

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