<template>
  <div>
    <h2>企业-岗位管理</h2>
    <el-card class="mb16">
      <el-button type="primary" @click="openPublish">发布兼职</el-button>
      <span class="tip">发布后需管理员审核通过，学生端方可浏览与投递。</span>
    </el-card>
    <el-card>
      <el-table :data="rows.records" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="160" />
        <el-table-column prop="title" label="标题" min-width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="120" show-overflow-tooltip />
        <el-table-column prop="contactWechat" label="微信说明" min-width="140" show-overflow-tooltip />
        <el-table-column prop="expireAt" label="到期时间" width="170" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">{{ jobStatusLabel(row.status) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="edit(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="offline(row)">下架</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="publishVisible" title="发布兼职" width="560px" destroy-on-close @closed="resetPublishForm">
      <el-form ref="publishFormRef" :model="publishForm" :rules="formRules" label-width="100px">
        <el-form-item label="岗位标题" prop="title">
          <el-input v-model="publishForm.title" maxlength="200" show-word-limit placeholder="简要概括岗位" />
        </el-form-item>
        <el-form-item label="岗位描述" prop="description">
          <el-input
            v-model="publishForm.description"
            type="textarea"
            :rows="5"
            maxlength="4000"
            show-word-limit
            placeholder="工作内容、时间要求、注意事项等"
          />
        </el-form-item>
        <el-form-item label="工作地点" prop="location">
          <el-input v-model="publishForm.location" maxlength="200" placeholder="如：XX区 XX路" />
        </el-form-item>
        <el-form-item label="岗位类型" prop="jobType">
          <el-input v-model="publishForm.jobType" maxlength="100" placeholder="如：促销 / 家教 / 客服" />
        </el-form-item>
        <el-form-item label="薪资文案" prop="salaryText">
          <el-input v-model="publishForm.salaryText" maxlength="100" placeholder="如：150元/天" />
        </el-form-item>
        <el-form-item label="薪资下限" prop="salaryMin">
          <el-input-number v-model="publishForm.salaryMin" :min="0" :max="999999" controls-position="right" class="w-full" />
        </el-form-item>
        <el-form-item label="薪资上限" prop="salaryMax">
          <el-input-number v-model="publishForm.salaryMax" :min="0" :max="999999" controls-position="right" class="w-full" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="publishForm.contactPhone" maxlength="32" placeholder="学生投递后用于联系您" />
        </el-form-item>
        <el-form-item label="微信/沟通" prop="contactWechat">
          <el-input
            v-model="publishForm.contactWechat"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="微信号、企业微信、添加备注说明等"
          />
        </el-form-item>
        <el-form-item label="到期时间" prop="expireAt">
          <el-date-picker
            v-model="publishForm.expireAt"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="不填默认30天后"
            style="width: 100%;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishVisible = false">取消</el-button>
        <el-button type="primary" :loading="publishLoading" @click="submitPublish">提交发布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editVisible" title="编辑岗位" width="560px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="formRules" label-width="100px">
        <el-form-item label="岗位标题" prop="title">
          <el-input v-model="editForm.title" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="岗位描述" prop="description">
          <el-input v-model="editForm.description" type="textarea" :rows="5" maxlength="4000" show-word-limit />
        </el-form-item>
        <el-form-item label="工作地点" prop="location">
          <el-input v-model="editForm.location" maxlength="200" />
        </el-form-item>
        <el-form-item label="岗位类型" prop="jobType">
          <el-input v-model="editForm.jobType" maxlength="100" />
        </el-form-item>
        <el-form-item label="薪资文案" prop="salaryText">
          <el-input v-model="editForm.salaryText" maxlength="100" />
        </el-form-item>
        <el-form-item label="薪资下限" prop="salaryMin">
          <el-input-number v-model="editForm.salaryMin" :min="0" :max="999999" controls-position="right" class="w-full" />
        </el-form-item>
        <el-form-item label="薪资上限" prop="salaryMax">
          <el-input-number v-model="editForm.salaryMax" :min="0" :max="999999" controls-position="right" class="w-full" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="editForm.contactPhone" maxlength="32" />
        </el-form-item>
        <el-form-item label="微信/沟通" prop="contactWechat">
          <el-input v-model="editForm.contactWechat" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="到期时间" prop="expireAt">
          <el-date-picker
            v-model="editForm.expireAt"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="不填默认30天后"
            style="width: 100%;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { publishCompanyJob, myCompanyJobs, offlineCompanyJob, updateCompanyJob } from '../api/company'
import { jobStatusLabel } from '../utils/enumLabel'

const emptyJobPayload = () => ({
  title: '',
  description: '',
  location: '',
  jobType: '',
  salaryText: '',
  salaryMin: null,
  salaryMax: null,
  contactPhone: '',
  contactWechat: '',
  expireAt: ''
})

const publishVisible = ref(false)
const publishLoading = ref(false)
const publishFormRef = ref(null)
const publishForm = reactive(emptyJobPayload())

const editVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref(null)
const editForm = reactive({ id: null, ...emptyJobPayload() })

const rows = reactive({ records: [], total: 0 })
const loading = ref(false)

const formRules = {
  title: [{ required: true, message: '请填写岗位标题', trigger: 'blur' }],
  description: [{ required: true, message: '请填写岗位描述', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请填写联系电话', trigger: 'blur' }],
  contactWechat: [{ required: true, message: '请填写微信或沟通说明', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await myCompanyJobs({ current: 1, size: 50 })
    if (res.success && res.data) Object.assign(rows, res.data)
  } finally {
    loading.value = false
  }
}

const resetPublishForm = () => {
  Object.assign(publishForm, emptyJobPayload())
}

const openPublish = () => {
  resetPublishForm()
  publishVisible.value = true
}

const submitPublish = async () => {
  const form = publishFormRef.value
  if (!form) return
  try {
    await form.validate()
  } catch {
    return
  }
  publishLoading.value = true
  try {
    await publishCompanyJob({ ...publishForm })
    ElMessage.success('发布成功，请等待审核')
    publishVisible.value = false
    await load()
  } catch (e) {
    ElMessage.error(e.message || '失败')
  } finally {
    publishLoading.value = false
  }
}

const edit = (row) => {
  Object.assign(editForm, {
    id: row.id,
    title: row.title || '',
    description: row.description || '',
    location: row.location || '',
    jobType: row.jobType || '',
    salaryText: row.salaryText || '',
    salaryMin: row.salaryMin ?? null,
    salaryMax: row.salaryMax ?? null,
    contactPhone: row.contactPhone || '',
    contactWechat: row.contactWechat || '',
    expireAt: row.expireAt || ''
  })
  editVisible.value = true
}

const saveEdit = async () => {
  const form = editFormRef.value
  if (!form) return
  try {
    await form.validate()
  } catch {
    return
  }
  editLoading.value = true
  try {
    const { id, ...payload } = editForm
    await updateCompanyJob(id, payload)
    ElMessage.success('已更新，修改后需重新审核')
    editVisible.value = false
    await load()
  } catch (e) {
    ElMessage.error(e.message || '失败')
  } finally {
    editLoading.value = false
  }
}

const offline = async (row) => {
  try {
    await offlineCompanyJob(row.id)
    ElMessage.success('已下架')
    await load()
  } catch (e) {
    ElMessage.error(e.message || '失败')
  }
}

onMounted(load)
</script>

<style scoped>
.mb16 {
  margin-bottom: 16px;
}
.tip {
  margin-left: 12px;
  font-size: 13px;
  color: #909399;
}
.w-full {
  width: 100%;
}
</style>
