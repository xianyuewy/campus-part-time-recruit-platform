<template>
  <el-dialog
    v-model="innerVisible"
    width="600px"
    destroy-on-close
    class="credit-dispute-dialog"
    align-center
    @closed="resetForm"
  >
    <template #header>
      <div class="dialog-header">
        <el-icon class="dialog-header-icon"><Warning /></el-icon>
        <div>
          <div class="dialog-header-title">发起信用争议</div>
          <div class="dialog-header-sub">请选择往来方与关联记录，并说明争议事由</div>
        </div>
      </div>
    </template>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      class="hint-alert"
      title="如何选择对方与关联项"
      description="请从「争议对象」中选择与您有过岗位投递/招聘往来的一方，无需输入用户数字 ID。若争议与某次申请、岗位或评价有关，请选择关联类型，并在下方选中对应记录。提交成功后，您仍可在「争议工单」中对「待处理」工单继续补充材料。"
    />

    <el-form ref="formRef" :model="form" label-position="top" class="dispute-form">
      <div class="form-section">
        <div class="section-label">争议对象 <span class="required">*</span></div>
        <el-select
          v-model="form.targetUserId"
          filterable
          placeholder="请选择往来方（来自历史投递/候选人）"
          style="width: 100%"
          :loading="loadingCp"
          @change="onTargetChange"
        >
          <el-option v-for="c in counterparties" :key="c.userId" :label="formatCounterparty(c)" :value="c.userId">
            <div class="opt-main">{{ c.displayName }}</div>
            <div class="opt-sub">@{{ c.username }} · {{ c.hint }}</div>
          </el-option>
        </el-select>
        <div v-if="!loadingCp && counterparties.length === 0" class="form-tip warn">
          暂无可选对象：请先完成至少一次岗位投递（学生）或收到投递（企业）后再发起争议。
        </div>
      </div>

      <div class="form-section">
        <div class="section-label">关联类型</div>
        <el-select v-model="form.relatedType" placeholder="请选择" style="width: 100%" @change="onRelTypeChange">
          <el-option v-for="t in CREDIT_RELATED_TYPES" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
        <div class="form-tip">{{ currentTypeDesc }}</div>
      </div>

      <div v-if="needsRelatedPick" class="form-section">
        <div class="section-label">关联记录 <span class="required">*</span></div>
        <el-select
          v-model="form.relatedId"
          filterable
          placeholder="请选择一条具体记录"
          style="width: 100%"
          :loading="loadingPicks"
          :disabled="!form.targetUserId"
        >
          <el-option v-for="p in relationPicks" :key="p.id" :label="p.label" :value="p.id" />
        </el-select>
        <div
          v-if="!loadingPicks && needsRelatedPick && form.targetUserId && relationPicks.length === 0"
          class="form-tip warn"
        >
          暂无此类记录，可改为「无关联记录」或完成互评/业务后再试。
        </div>
      </div>

      <div class="form-section">
        <div class="section-label">事由说明 <span class="required">*</span></div>
        <el-input
          v-model="form.reason"
          type="textarea"
          :rows="4"
          maxlength="1000"
          show-word-limit
          placeholder="请说明争议原因与诉求"
        />
      </div>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button round @click="close">取消</el-button>
        <el-button type="primary" round :loading="submitting" @click="submit">提交争议</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import { CREDIT_RELATED_TYPES } from '../constants/creditDispute'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  loadCounterparties: { type: Function, required: true },
  loadRelationPicks: { type: Function, required: true },
  submitDispute: { type: Function, required: true }
})

const emit = defineEmits(['update:modelValue', 'success'])

const innerVisible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const formRef = ref(null)
const counterparties = ref([])
const relationPicks = ref([])
const loadingCp = ref(false)
const loadingPicks = ref(false)
const submitting = ref(false)

const form = ref({
  targetUserId: null,
  relatedType: 'NONE',
  relatedId: null,
  reason: ''
})

const needsRelatedPick = computed(() => form.value.relatedType && form.value.relatedType !== 'NONE')

const currentTypeDesc = computed(() => {
  const t = CREDIT_RELATED_TYPES.find((x) => x.value === form.value.relatedType)
  return t ? t.shortDesc : ''
})

const formatCounterparty = (c) => `${c.displayName}（@${c.username}）`

const resetForm = () => {
  form.value = {
    targetUserId: null,
    relatedType: 'NONE',
    relatedId: null,
    reason: ''
  }
  relationPicks.value = []
}

const close = () => {
  innerVisible.value = false
}

const loadCp = async () => {
  loadingCp.value = true
  try {
    const res = await props.loadCounterparties()
    counterparties.value = res.success && Array.isArray(res.data) ? res.data : []
  } finally {
    loadingCp.value = false
  }
}

const loadPicks = async () => {
  if (!needsRelatedPick.value || !form.value.targetUserId) {
    relationPicks.value = []
    return
  }
  loadingPicks.value = true
  try {
    const res = await props.loadRelationPicks({
      targetUserId: form.value.targetUserId,
      relatedType: form.value.relatedType
    })
    relationPicks.value = res.success && Array.isArray(res.data) ? res.data : []
    if (relationPicks.value.length === 1 && form.value.relatedId == null) {
      form.value.relatedId = relationPicks.value[0].id
    }
  } finally {
    loadingPicks.value = false
  }
}

const onTargetChange = () => {
  form.value.relatedId = null
  loadPicks()
}

const onRelTypeChange = () => {
  form.value.relatedId = null
  loadPicks()
}

watch(
  () => props.modelValue,
  (v) => {
    if (v) {
      resetForm()
      loadCp()
    }
  }
)

const submit = async () => {
  if (!form.value.targetUserId) {
    ElMessage.warning('请选择争议对象')
    return
  }
  if (!form.value.reason?.trim()) {
    ElMessage.warning('请填写事由说明')
    return
  }
  if (needsRelatedPick.value && form.value.relatedId == null) {
    ElMessage.warning('请选择要关联的具体记录，或改为「无关联记录」')
    return
  }
  submitting.value = true
  try {
    const payload = {
      targetUserId: form.value.targetUserId,
      reason: form.value.reason.trim(),
      relatedType: form.value.relatedType === 'NONE' ? undefined : form.value.relatedType,
      relatedId: needsRelatedPick.value ? form.value.relatedId : undefined
    }
    await props.submitDispute(payload)
    ElMessage.success('已提交')
    emit('success')
    close()
  } catch (e) {
    ElMessage.error(e.message || '提交失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.dialog-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.dialog-header-icon {
  font-size: 28px;
  color: #e6a23c;
  margin-top: 2px;
}

.dialog-header-title {
  font-size: 17px;
  font-weight: 600;
  color: #303133;
}

.dialog-header-sub {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.hint-alert {
  margin-bottom: 18px;
  border-radius: 12px;
}

.dispute-form {
  max-height: 58vh;
  overflow-y: auto;
  padding-right: 4px;
}

.form-section {
  margin-bottom: 18px;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-label {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;

  .required {
    color: #f56c6c;
  }
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  border-radius: 10px;
}

.opt-main {
  font-weight: 500;
}

.opt-sub {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin-top: 6px;

  &.warn {
    color: #e6a23c;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>

<style lang="scss">
.credit-dispute-dialog {
  .el-dialog__header {
    padding: 20px 24px 12px;
    margin-right: 0;
  }
  .el-dialog__body {
    padding: 8px 24px 16px;
  }
  .el-dialog__footer {
    padding: 12px 24px 20px;
    border-top: 1px solid #f0f3f8;
  }
}
</style>
