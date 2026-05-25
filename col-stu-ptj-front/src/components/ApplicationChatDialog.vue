<template>
  <el-dialog
    :model-value="modelValue"
    title="在线沟通"
    width="520px"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
    @closed="onClosed"
  >
    <p class="tip">
      <template v-if="wsState === 'open'">已通过 WebSocket 连接，消息实时送达对方。</template>
      <template v-else-if="wsState === 'connecting'">正在建立实时连接…</template>
      <template v-else>实时连接不可用，发送将走接口；打开窗口时会自动重试连接。</template>
    </p>
    <div ref="scrollRef" class="msg-scroll">
      <div v-for="m in messages" :key="m.id" class="msg-row" :class="{ mine: m.senderUserId === myUserId }">
        <div class="meta">{{ m.senderDisplayName || m.senderUsername || '用户' }} · {{ m.createTime }}</div>
        <div class="bubble">{{ m.content }}</div>
      </div>
      <el-empty v-if="!loading && !messages.length" description="暂无消息，先发一条吧" />
    </div>
    <div class="send-row">
      <el-input
        v-model="draft"
        type="textarea"
        :rows="2"
        maxlength="2000"
        show-word-limit
        placeholder="输入内容后发送"
        :disabled="!canSend"
      />
      <el-button type="primary" class="mt8" :loading="sending" :disabled="!canSend || !draft.trim()" @click="send">
        发送
      </el-button>
    </div>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { getApplicationChat, sendApplicationChat } from '../api/chat'
import { applicationChatWebSocketUrl } from '../utils/request'
import { useUserStore } from '../store/user'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  applicationId: { type: [Number, String], default: null },
  /** 为 false 时仅可查看（如投递已取消） */
  canSend: { type: Boolean, default: true }
})

const emit = defineEmits(['update:modelValue'])

const userStore = useUserStore()
const myUserId = computed(() => userStore.userInfo?.userId)

const messages = ref([])
const loading = ref(false)
const sending = ref(false)
const draft = ref('')
const scrollRef = ref(null)
const wsState = ref('closed') // connecting | open | closed

let ws = null
let reconnectTimer = null
let reconnectAttempts = 0
const MAX_RECONNECT = 8

const appendMessage = (vo) => {
  if (!vo || vo.id == null) return
  if (messages.value.some((x) => x.id === vo.id)) return
  messages.value = [...messages.value, vo]
  nextTick(() => {
    if (scrollRef.value) scrollRef.value.scrollTop = scrollRef.value.scrollHeight
  })
}

const handleWsPayload = (text) => {
  let data
  try {
    data = JSON.parse(text)
  } catch {
    return
  }
  if (data.type === 'NEW_MESSAGE' && data.message) {
    appendMessage(data.message)
  } else if (data.type === 'ERROR') {
    ElMessage.error(data.message || '发送失败')
  }
}

const clearReconnect = () => {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
}

const closeWs = () => {
  clearReconnect()
  reconnectAttempts = 0
  if (ws) {
    try {
      ws.close()
    } catch {
      /* ignore */
    }
    ws = null
  }
  wsState.value = 'closed'
}

const scheduleReconnect = (applicationId) => {
  if (!props.modelValue || !applicationId) return
  if (reconnectAttempts >= MAX_RECONNECT) return
  clearReconnect()
  const delay = Math.min(30000, 1000 * Math.pow(2, reconnectAttempts))
  reconnectAttempts += 1
  reconnectTimer = setTimeout(() => connectWs(applicationId), delay)
}

const connectWs = (applicationId) => {
  closeWs()
  if (!applicationId || !props.modelValue) return
  const token = localStorage.getItem('accessToken')
  if (!token) {
    wsState.value = 'closed'
    return
  }
  wsState.value = 'connecting'
  const url = applicationChatWebSocketUrl(applicationId)
  try {
    ws = new WebSocket(url)
  } catch (e) {
    wsState.value = 'closed'
    scheduleReconnect(applicationId)
    return
  }
  ws.onopen = () => {
    wsState.value = 'open'
    reconnectAttempts = 0
  }
  ws.onmessage = (ev) => handleWsPayload(ev.data)
  ws.onerror = () => {
    /* onclose 会处理 */
  }
  ws.onclose = () => {
    wsState.value = 'closed'
    ws = null
    if (props.modelValue && props.applicationId === applicationId) {
      scheduleReconnect(applicationId)
    }
  }
}

const load = async () => {
  if (!props.applicationId) return
  loading.value = true
  try {
    const res = await getApplicationChat(props.applicationId, { current: 1, size: 100 })
    if (res.success && res.data?.records) {
      messages.value = res.data.records
      await nextTick()
      if (scrollRef.value) scrollRef.value.scrollTop = scrollRef.value.scrollHeight
    }
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const send = async () => {
  if (!props.applicationId || !draft.value.trim()) return
  const text = draft.value.trim()
  sending.value = true
  try {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ content: text }))
      draft.value = ''
    } else {
      await sendApplicationChat(props.applicationId, { content: text })
      draft.value = ''
      await load()
      ElMessage.success('已发送')
    }
  } catch (e) {
    ElMessage.error(e.message || '发送失败')
  } finally {
    sending.value = false
  }
}

const onClosed = () => {
  closeWs()
  messages.value = []
  draft.value = ''
}

watch(
  () => [props.modelValue, props.applicationId],
  ([open, id]) => {
    if (open && id) {
      load().then(() => connectWs(id))
    } else {
      closeWs()
    }
  }
)

onUnmounted(closeWs)
</script>

<style scoped>
.tip {
  font-size: 13px;
  color: #909399;
  margin: 0 0 12px;
  line-height: 1.5;
}
.msg-scroll {
  max-height: 320px;
  overflow-y: auto;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
  background: #fafafa;
}
.msg-row {
  margin-bottom: 12px;
}
.msg-row.mine .bubble {
  background: #ecf5ff;
  border-color: #d9ecff;
}
.meta {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}
.bubble {
  white-space: pre-wrap;
  word-break: break-word;
  padding: 8px 10px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #ebeef5;
  font-size: 14px;
  color: #303133;
  line-height: 1.5;
}
.send-row {
  margin-top: 12px;
}
.mt8 {
  margin-top: 8px;
}
</style>
