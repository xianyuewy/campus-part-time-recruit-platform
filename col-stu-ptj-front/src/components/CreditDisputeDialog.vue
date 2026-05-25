<template>
  <el-dialog
    v-model="innerVisible"
    title="发起信用争议"
    width="580px"
    destroy-on-close
    @closed="resetForm"
  >
    <el-alert
      type="info"
      :closable="false"
      show-icon
      class="hint-alert"
      title="如何选择对方与关联项"
      description="请从「争议对象」中选择与您有过岗位投递/招聘往来的一方，无需输入用户数字 ID。若争议与某次申请、岗位或评价有关，请选择关联类型，并在下方选中对应记录。提交成功后，您仍可在「争议工单」列表中对「待处理」工单继续补充文字说明与图片/PDF 证据，直至管理员结案。"
    />
    <el-form ref="formRef" :model="form" label-width="108px" class="form-body">
      <el-form-item label="争议对象" required>
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
      </el-form-item>
      <el-form-item label="关联类型">
        <el-select v-model="form.relatedType" placeholder="请选择" style="width: 100%" @change="onRelTypeChange">
          <el-option
            v-for="t in CREDIT_RELATED_TYPES"
            :key="t.value"
            :label="t.label"
            :value="t.value"
          />
        </el-select>
        <div class="form-tip">{{ currentTypeDesc }}</div>
      </el-form-item>
      <el-form-item v-if="needsRelatedPick" label="关联记录" required>
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
        <div v-if="!loadingPicks && needsRelatedPick && form.targetUserId && relationPicks.length === 0" class="form-tip warn">
          暂无此类记录，可改为「无关联记录」或完成互评/业务后再试。
        </div>
      </el-form-item>
      <el-form-item label="事由说明" required>
        <el-input v-model="form.reason" type="textarea" :rows="4" placeholder="请说明争议原因与诉求" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="close">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">提交</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
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

<style scoped>
.hint-alert {
  margin-bottom: 16px;
}
.form-body {
  max-height: 70vh;
  overflow-y: auto;
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
}
.form-tip.warn {
  color: #e6a23c;
}
</style>
