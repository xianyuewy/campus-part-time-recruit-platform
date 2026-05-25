<template>
  <div>
    <h2>企业-资料管理</h2>
    <el-card>
      <div class="status-row">
        <span class="status-label">当前资质状态：</span>
        <el-tag :type="statusTagType">{{ statusText }}</el-tag>
      </div>
      <el-form label-width="100px">
        <el-form-item label="企业名称"><el-input v-model="form.companyName"/></el-form-item>
        <el-form-item label="执照号"><el-input v-model="form.licenseNo"/></el-form-item>
        <el-form-item label="联系人"><el-input v-model="form.contactName"/></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.contactPhone"/></el-form-item>
        <el-form-item label="企业介绍"><el-input v-model="form.description" type="textarea" rows="4"/></el-form-item>
        <el-form-item>
          <el-button type="primary" @click="save">保存</el-button>
          <el-button
            type="success"
            :disabled="userStore.authStatus === 'PENDING' || userStore.authStatus === 'APPROVED'"
            @click="submitAudit">
            提交资质审核
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
<script setup>
import { computed, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getCompanyProfile, submitCompanyAudit, upsertCompanyProfile } from '../api/company'
import { useUserStore } from '../store/user'

const userStore = useUserStore()
const form = reactive({ companyName:'', licenseNo:'', contactName:'', contactPhone:'', description:'' })
const load = async()=>{ const res=await getCompanyProfile(); if(res.success&&res.data) Object.assign(form,res.data)}
const save = async()=>{ try{ await upsertCompanyProfile(form); ElMessage.success('保存成功')}catch(e){ElMessage.error(e.message||'失败')}}
const submitAudit = async () => {
  try {
    await submitCompanyAudit()
    await userStore.getUserInfo()
    ElMessage.success('已提交审核，请等待管理员处理')
  } catch (e) {
    ElMessage.error(e.message || '提交失败')
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
<style scoped>
.status-row {
  margin-bottom: 16px;
}
.status-label {
  margin-right: 8px;
  color: #606266;
}
</style>