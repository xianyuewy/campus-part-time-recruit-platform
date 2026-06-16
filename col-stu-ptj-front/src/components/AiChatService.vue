<template>
  <!-- 全局悬浮客服按钮 固定右下角 -->
  <Teleport to="body">
    <div class="ai-float-btn" @click="openChat">
      <el-icon size="22"><ChatDotRound /></el-icon>
      <span>AI客服</span>
    </div>
  </Teleport>

  <!-- 客服弹窗 draggable 开启拖拽 -->
  <el-dialog
      v-model="chatVisible"
      title="平台智能AI客服"
      width="480px"
      destroy-on-close
      @closed="clearInput"
      class="ai-chat-dialog"
      draggable
  >
    <!-- 聊天记录滚动容器 -->
    <div class="chat-wrap" ref="scrollRef">
      <!-- 空白欢迎页 + 快捷提问 -->
      <div v-if="chatList.length === 0" class="chat-empty">
        <el-icon size="42" color="#94a3b8"><ChatLineRound /></el-icon>
        <p>您好，我是平台AI智能客服，有兼职相关问题都可以咨询我</p>
        <div class="quick-btn-group">
          <el-button text size="small" @click="quickAsk('岗位审核多久出结果？')">岗位审核多久出结果？</el-button>
          <el-button text size="small" @click="quickAsk('怎么下架/关闭岗位？')">怎么下架/关闭岗位？</el-button>
          <el-button text size="small" @click="quickAsk('发布岗位有什么禁止内容？')">发布岗位有什么禁止内容？</el-button>
          <el-button text size="small" @click="quickAsk('学生怎么投递兼职？')">学生怎么投递兼职？</el-button>
        </div>
      </div>

      <!-- 对话列表 -->
      <div v-for="item in chatList" :key="item.id" class="chat-row">
        <!-- 用户消息 -->
        <div v-if="item.role === 'user'" class="msg user">
          <div class="msg-content">{{ item.text }}</div>
        </div>
        <!-- AI回复 -->
        <div v-else class="msg ai">
          <el-icon class="ai-icon" size="20"><Service /></el-icon>
          <div class="msg-content">{{ item.text }}</div>
        </div>
      </div>

      <!-- AI加载中 -->
      <div v-if="loading" class="loading-tip">AI正在思考，请稍等...</div>
    </div>

    <!-- 底部输入栏 -->
    <div class="chat-input-bar">
      <el-input
          v-model="queryText"
          placeholder="输入你的问题，回车快速发送"
          clearable
          @keyup.enter="sendMessage"
          class="input-box"
      />
      <el-button type="primary" :loading="loading" @click="sendMessage">发送</el-button>
      <el-button @click="resetAllChat" icon="Delete">清空</el-button>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, nextTick, Teleport } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, ChatLineRound, Service, Delete } from '@element-plus/icons-vue'
import axios from 'axios'

const chatVisible = ref(false)
const loading = ref(false)
const queryText = ref('')
const scrollRef = ref(null)
let msgId = 0
const chatList = ref([])


const openChat = () => {
  let targetUserId = ''
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr)
      targetUserId = userInfo.userId
      console.log('实时读取用户ID：', targetUserId)
    } catch (err) {
      console.error('解析失败', err)
    }
  }

  // 校验是否存在有效用户ID
  if (!targetUserId) {
    ElMessage.warning('请先登录后再咨询客服')
    return
  }
  // 把实时拿到的id传给发送函数
  chatVisible.value = true
  // 挂载到全局供sendMessage使用
  window.currentAiUserId = targetUserId
}

// 快捷提问
const quickAsk = (text) => {
  queryText.value = text
  sendMessage()
}

// 发送消息，从全局取实时userId
const sendMessage = async () => {
  const text = queryText.value.trim()
  if (!text) {
    ElMessage.warning('请输入咨询问题')
    return
  }
  const targetUserId = window.currentAiUserId
  if (!targetUserId) return

  chatList.value.push({
    id: ++msgId,
    role: 'user',
    text
  })
  queryText.value = ''
  loading.value = true
  await nextTick()
  scrollRef.value.scrollTop = scrollRef.value.scrollHeight

  try {
    const res = await axios.get('/api/ai/chat', {
      params: {
        userId: targetUserId,
        question: text
      }
    })
    chatList.value.push({
      id: ++msgId,
      role: 'ai',
      text: res.data
    })
  } catch (err) {
    ElMessage.error('AI客服服务异常，请稍后重试')
    console.error('AI接口错误：', err)
  } finally {
    loading.value = false
    nextTick(() => {
      scrollRef.value.scrollTop = scrollRef.value.scrollHeight
    })
  }
}

const clearInput = () => {
  queryText.value = ''
}

const resetAllChat = () => {
  chatList.value = []
  queryText.value = ''
}
</script>

<style scoped lang="scss">
/* 右下角悬浮按钮 全局固定层级最高 */
.ai-float-btn {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 9999;
  background: linear-gradient(135deg, #1e3a5f, #3d7ab5);
  color: #fff;
  padding: 10px 16px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(45, 90, 135, 0.28);
  transition: 0.2s all;
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 16px rgba(45, 90, 135, 0.35);
  }
}

/* 弹窗容器 */
.ai-chat-dialog {
  :deep(.el-dialog__header) {
    cursor: move; /* 鼠标移入标题显示拖拽光标 */
    user-select: none; /* 拖动时不选中文本 */
  }
  :deep(.el-dialog__body) {
    padding: 0;
  }
}

.chat-wrap {
  height: 400px;
  overflow-y: auto;
  padding: 16px;
  background-color: #f7f8fa;
}

/* 空白欢迎区域 */
.chat-empty {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #606266;
  gap: 14px;
  p {
    margin: 0;
    font-size: 14px;
  }
  .quick-btn-group {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    justify-content: center;
  }
}

/* 单条对话 */
.chat-row {
  margin-bottom: 16px;
  display: flex;
}
.user {
  justify-content: flex-end;
  .msg-content {
    background: #2d5a87;
    color: #fff;
    border-radius: 12px 2px 12px 12px;
  }
}
.ai {
  justify-content: flex-start;
  gap: 8px;
  align-items: flex-start;
  .ai-icon {
    margin-top: 4px;
    color: #2d5a87;
    flex-shrink: 0;
  }
  .msg-content {
    background: #ffffff;
    border: 1px solid #e4e7ed;
    color: #303133;
    border-radius: 2px 12px 12px 12px;
  }
}

.msg-content {
  max-width: 310px;
  padding: 9px 13px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-all;
}

.loading-tip {
  margin-left: 30px;
  font-size: 13px;
  color: #909399;
}

/* 底部输入栏 */
.chat-input-bar {
  display: flex;
  gap: 10px;
  padding: 14px 16px;
  border-top: 1px solid #eef1f6;
  align-items: center;
  .input-box {
    flex: 1;
  }
}
</style>