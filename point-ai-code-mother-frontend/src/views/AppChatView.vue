<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js/lib/core'
import xml from 'highlight.js/lib/languages/xml'
import css from 'highlight.js/lib/languages/css'
import javascript from 'highlight.js/lib/languages/javascript'
import 'highlight.js/styles/github-dark.css'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import { deployApp, getAppVoById, getAppVoByIdByAdmin } from '@/api/appController'
import { listAppChatHistory } from '@/api/chatHistoryController'
import aiAvatar from '@/assets/aiAvatar.png'
import { useLoginUserStore } from '@/stores/loginUser'

interface ChatMessage {
  id: string
  role: 'user' | 'ai'
  content: string
  createTime?: string
}

const HISTORY_PAGE_SIZE = 10
const API_BASE_URL = 'http://localhost:8123/api'

hljs.registerLanguage('html', xml)
hljs.registerLanguage('xml', xml)
hljs.registerLanguage('css', css)
hljs.registerLanguage('javascript', javascript)
hljs.registerLanguage('js', javascript)

const markdown = new MarkdownIt({
  html: true,
  breaks: true,
  linkify: true,
  highlight(code: string, language: string) {
    const normalizedLanguage = language?.toLowerCase()
    const aliasLanguageMap: Record<string, string> = {
      vue: 'html',
      ts: 'javascript',
      typescript: 'javascript',
      jsx: 'javascript',
      tsx: 'javascript',
    }
    const finalLanguage = aliasLanguageMap[normalizedLanguage] || normalizedLanguage
    if (finalLanguage && hljs.getLanguage(finalLanguage)) {
      return `<pre class="hljs"><code>${hljs.highlight(code, { language: finalLanguage }).value}</code></pre>`
    }
    return `<pre class="hljs"><code>${markdown.utils.escapeHtml(code)}</code></pre>`
  },
})

const renderMarkdown = (content: string) => markdown.render(content)
const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()
const appId = computed(() => {
  const id = route.params.id
  return Array.isArray(id) ? String(id[0] || '') : String(id || '')
})
const appInfo = ref<API.AppVO>()
const loading = ref(false)
const sending = ref(false)
const deploying = ref(false)
const deployUrl = ref('')
const previewReady = ref(false)
const previewLoading = ref(false)
const previewLoadFailed = ref(false)
const previewFrameKey = ref(0)
const historyLoadingMore = ref(false)
const hasMoreHistory = ref(false)
const totalHistoryCount = ref(0)
const historyLoadedCount = ref(0)
const oldestHistoryCreateTime = ref('')
const inputMessage = ref('')
const messages = ref<ChatMessage[]>([])
const messageContainerRef = ref<HTMLElement | null>(null)
let eventSource: EventSource | null = null

const isAdmin = computed(() => loginUserStore.loginUser?.userRole === 'admin')
const isOwner = computed(() => {
  const currentUserId = loginUserStore.loginUser?.id
  if (!appInfo.value?.userId || currentUserId == null) return false
  return String(currentUserId) === String(appInfo.value.userId)
})
const canEditChat = computed(() => isAdmin.value || isOwner.value)
const userAvatar = computed(() => loginUserStore.loginUser?.userAvatar || '')
const userAvatarText = computed(() => {
  const name = loginUserStore.loginUser?.userName || loginUserStore.loginUser?.userAccount || 'U'
  return name.slice(0, 1).toUpperCase()
})
const aiAvatarText = computed(() => 'AI')
const chatInputTooltip = computed(() => (canEditChat.value ? undefined : '无法在别人的作品下对话哦~'))
const deployPreviewUrl = computed(() => (appInfo.value?.deployKey ? `http://localhost/${appInfo.value.deployKey}/` : ''))
const previewUrl = computed(() => {
  if (!appInfo.value?.id || !appInfo.value?.codeGenType) return ''
  return `http://localhost:8123/api/static/${appInfo.value.codeGenType}_${appInfo.value.id}/`
})
const shouldDisplayPreviewPanel = computed(() => totalHistoryCount.value >= 2)

const buildChatGenCodeSSEUrl = (params: { appId: string; message: string }) => {
  const searchParams = new URLSearchParams({
    appId: params.appId,
    message: params.message,
  })
  return `${API_BASE_URL}/app/chat/gen/code?${searchParams.toString()}`
}
const getMessageTime = (value?: string) => {
  const time = value ? new Date(value).getTime() : 0
  return Number.isNaN(time) ? 0 : time
}
const sortHistory = (list: API.ChatHistory[] = []) => [...list].sort((a, b) => getMessageTime(a.createTime) - getMessageTime(b.createTime))
const normalizeMessageRole = (record: API.ChatHistory): 'user' | 'ai' => {
  const messageType = String(record.messageType || '').toLowerCase()
  return ['user', 'human', 'question', 'input'].some((keyword) => messageType.includes(keyword)) ? 'user' : 'ai'
}
const mapHistoryToMessage = (record: API.ChatHistory): ChatMessage => ({
  id: record.id != null ? String(record.id) : `${record.createTime || Date.now()}_${record.message || ''}`,
  role: normalizeMessageRole(record),
  content: record.message || '',
  createTime: record.createTime,
})
const mergeMessages = (incoming: ChatMessage[], existing: ChatMessage[]) => {
  const map = new Map<string, ChatMessage>()
  ;[...incoming, ...existing].forEach((item) => map.set(item.id, item))
  return [...map.values()].sort((a, b) => getMessageTime(a.createTime) - getMessageTime(b.createTime))
}
const resetPreviewState = () => {
  previewReady.value = false
  previewLoading.value = false
  previewLoadFailed.value = false
}
const checkPreviewAvailability = async () => {
  if (!previewUrl.value || !shouldDisplayPreviewPanel.value) {
    resetPreviewState()
    return
  }
  previewLoading.value = true
  previewLoadFailed.value = false
  try {
    const response = await fetch(previewUrl.value, { method: 'GET', credentials: 'include' })
    previewReady.value = response.ok
    previewLoadFailed.value = !response.ok
    if (response.ok) previewFrameKey.value += 1
  } catch {
    previewReady.value = false
    previewLoadFailed.value = true
  } finally {
    previewLoading.value = false
  }
}
const scrollToBottom = async () => {
  await nextTick()
  const el = messageContainerRef.value
  if (el) el.scrollTop = el.scrollHeight
}
const addMessage = (role: 'user' | 'ai', content: string) => {
  messages.value.push({ id: `${Date.now()}_${Math.random()}`, role, content, createTime: new Date().toISOString() })
  void scrollToBottom()
}
const loadAppDetail = async () => {
  if (!appId.value) {
    message.error('应用 id 不合法')
    router.push('/')
    return false
  }
  loading.value = true
  try {
    const request = isAdmin.value ? getAppVoByIdByAdmin : getAppVoById
    const res = await request({ id: Number(appId.value) })
    if (res.data.code === 0 && res.data.data) {
      appInfo.value = res.data.data
      return true
    }
    message.error(res.data.message || '获取应用详情失败')
  } catch {
    message.error('获取应用详情失败，请稍后重试')
  } finally {
    loading.value = false
  }
  return false
}
const loadChatHistory = async (loadMore = false) => {
  if (!appId.value || (loadMore && (!hasMoreHistory.value || historyLoadingMore.value))) return
  const container = messageContainerRef.value
  const previousScrollHeight = container?.scrollHeight || 0
  if (loadMore) historyLoadingMore.value = true
  else loading.value = true
  try {
    const res = await listAppChatHistory({
      appId: Number(appId.value),
      pageSize: HISTORY_PAGE_SIZE,
      lastCreateTime: loadMore ? oldestHistoryCreateTime.value || undefined : undefined,
    })
    if (res.data.code === 0 && res.data.data) {
      const historyMessages = sortHistory(res.data.data.records || []).map(mapHistoryToMessage)
      if (loadMore) {
        messages.value = mergeMessages(historyMessages, messages.value)
        historyLoadedCount.value += historyMessages.length
      } else {
        messages.value = historyMessages
        historyLoadedCount.value = historyMessages.length
      }
      totalHistoryCount.value = Number(res.data.data.totalRow || 0)
      oldestHistoryCreateTime.value = messages.value[0]?.createTime || ''
      hasMoreHistory.value = historyLoadedCount.value < totalHistoryCount.value && historyMessages.length > 0
      if (loadMore && container) {
        await nextTick()
        container.scrollTop = container.scrollHeight - previousScrollHeight
      } else {
        await scrollToBottom()
      }
      await checkPreviewAvailability()
      return
    }
    message.error(res.data.message || '获取对话历史失败')
  } catch {
    message.error(loadMore ? '加载更多历史消息失败，请稍后重试' : '获取对话历史失败，请稍后重试')
  } finally {
    if (loadMore) historyLoadingMore.value = false
    else loading.value = false
  }
}
const closeSSE = () => {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
}
const finishSSE = async () => {
  sending.value = false
  closeSSE()
  await loadAppDetail()
  await loadChatHistory()
}
const appendAiChunk = async (targetMessageId: string, rawData: string) => {
  if (!rawData) return
  let chunk = rawData
  try {
    const parsed = JSON.parse(rawData)
    if (typeof parsed?.d === 'string') chunk = parsed.d
  } catch {}
  const targetMessage = messages.value.find((item) => item.id === targetMessageId)
  if (!targetMessage) return
  targetMessage.content += chunk
  await scrollToBottom()
}
const sendMessage = async (content?: string) => {
  const text = (content ?? inputMessage.value).trim()
  if (!canEditChat.value) {
    message.warning(chatInputTooltip.value)
    return
  }
  if (!text || !appId.value) return
  closeSSE()
  sending.value = true
  previewReady.value = false
  previewLoadFailed.value = false
  addMessage('user', text)
  inputMessage.value = ''
  const aiMessage: ChatMessage = { id: `${Date.now()}_ai`, role: 'ai', content: '', createTime: new Date().toISOString() }
  messages.value.push(aiMessage)
  await scrollToBottom()
  eventSource = new EventSource(buildChatGenCodeSSEUrl({ appId: appId.value, message: text }), { withCredentials: true })
  eventSource.onmessage = async (event) => appendAiChunk(aiMessage.id, event.data ?? '')
  eventSource.addEventListener('done', () => void finishSSE())
  eventSource.onerror = () => {
    if (eventSource?.readyState === EventSource.CLOSED) {
      void finishSSE()
      return
    }
    sending.value = false
    closeSSE()
    message.error('代码生成失败，请稍后重试')
  }
}
const autoStartIfNeeded = async () => {
  const initPrompt = String(appInfo.value?.initPrompt || '').trim()
  if (!isOwner.value || totalHistoryCount.value > 0 || !initPrompt || sending.value) {
    return
  }
  await sendMessage(initPrompt)
}
const handleDeploy = async () => {
  if (!appId.value) return
  deploying.value = true
  try {
    const res = await deployApp({ appId: Number(appId.value) })
    if (res.data.code === 0 && res.data.data) {
      deployUrl.value = res.data.data
      message.success('部署成功')
      return
    }
    message.error(res.data.message || '部署失败')
  } catch {
    message.error('部署失败，请稍后重试')
  } finally {
    deploying.value = false
  }
}
onMounted(async () => {
  if (!loginUserStore.loginUser) await loginUserStore.fetchLoginUser()
  const detailLoaded = await loadAppDetail()
  if (!detailLoaded) return
  await loadChatHistory()
  await autoStartIfNeeded()
})
onBeforeUnmount(() => closeSSE())
</script>

<template>
  <section class="chat-view">
    <div class="chat-view__topbar">
      <div class="chat-view__title">{{ appInfo?.appName || `应用 #${appId}` }}</div>
      <div class="chat-view__actions">
        <a-button :disabled="!canEditChat" @click="router.push(`/app/edit/${appId}`)">编辑信息</a-button>
        <a-button type="primary" :loading="deploying" :disabled="!canEditChat" @click="handleDeploy">部署</a-button>
        <a v-if="deployPreviewUrl" :href="deployPreviewUrl" target="_blank" rel="noreferrer">
          <a-button>查看作品</a-button>
        </a>
      </div>
    </div>

    <div class="chat-view__main" :class="{ 'chat-view__main--with-preview': shouldDisplayPreviewPanel }">
      <div class="chat-panel">
        <div ref="messageContainerRef" class="chat-panel__messages">
          <div v-if="hasMoreHistory" class="chat-panel__load-more">
            <a-button type="link" :loading="historyLoadingMore" @click="loadChatHistory(true)">加载更多</a-button>
          </div>

          <a-spin :spinning="loading">
            <div v-if="messages.length === 0" class="chat-panel__empty">
              请输入需求，AI 会开始为你生成网站应用。
            </div>
            <div
              v-for="item in messages"
              :key="item.id"
              class="chat-message"
              :class="item.role === 'user' ? 'chat-message--user' : 'chat-message--ai'"
            >
              <a-avatar
                class="chat-message__avatar"
                :src="item.role === 'user' ? userAvatar : aiAvatar"
                :size="40"
              >
                {{ item.role === 'user' ? userAvatarText : aiAvatarText }}
              </a-avatar>
              <div
                v-if="item.role === 'ai'"
                class="chat-message__bubble chat-message__bubble--markdown markdown-body"
                v-html="renderMarkdown(item.content || '正在思考中…')"
              ></div>
              <div v-else class="chat-message__bubble">{{ item.content }}</div>
            </div>
          </a-spin>
        </div>

        <div class="chat-panel__input">
          <a-tooltip :title="chatInputTooltip" :open="!canEditChat ? undefined : false">
            <div class="chat-panel__input-wrap" :class="{ 'chat-panel__input-wrap--disabled': !canEditChat }">
              <a-textarea
                v-model:value="inputMessage"
                :auto-size="{ minRows: 3, maxRows: 5 }"
                :disabled="!canEditChat"
                :placeholder="canEditChat ? '请描述你想生成的网站，越详细效果越好哦' : '无法在别人的作品下对话哦~'"
                @pressEnter.prevent="sendMessage()"
              />
            </div>
          </a-tooltip>
          <div class="chat-panel__submit">
            <a-button type="primary" :loading="sending" :disabled="!canEditChat" @click="sendMessage()">发送</a-button>
          </div>
        </div>
      </div>

      <div v-if="shouldDisplayPreviewPanel" class="preview-panel">
        <div class="preview-panel__header">
          <span>生成后的网页展示</span>
          <a v-if="deployUrl" :href="deployUrl" target="_blank" rel="noreferrer">访问部署地址</a>
        </div>
        <div class="preview-panel__body">
          <a-spin :spinning="previewLoading">
            <iframe
              v-if="previewReady && previewUrl"
              :key="previewFrameKey"
              :src="previewUrl"
              class="preview-panel__iframe"
            />
            <a-empty
              v-else-if="previewLoadFailed"
              description="网页文件暂未生成成功，请重新生成或检查后端保存结果"
            />
            <a-empty v-else description="网站文件生成完成后将在这里展示效果" />
          </a-spin>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.chat-view {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: calc(100vh - 72px - 56px - 24px - 24px);
  min-height: 0;
}

.chat-view__topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 18px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 18px;
}

.chat-view__title {
  color: #111827;
  font-size: 22px;
  font-weight: 700;
}

.chat-view__actions {
  display: flex;
  gap: 12px;
}

.chat-view__main {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 12px;
  min-height: 0;
  flex: 1;
}

.chat-view__main--with-preview {
  grid-template-columns: minmax(0, 2fr) minmax(0, 3fr);
}

.chat-panel,
.preview-panel {
  display: flex;
  flex-direction: column;
  min-height: 0;
  height: 100%;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 20px;
  overflow: hidden;
}

.chat-panel__messages {
  flex: 1;
  padding: 14px;
  overflow: auto;
}

.chat-panel__load-more {
  display: flex;
  justify-content: center;
  margin-bottom: 8px;
}

.chat-panel__empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 220px;
  color: #94a3b8;
}

.chat-message {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 14px;
}

.chat-message--user {
  justify-content: flex-end;
}

.chat-message--user .chat-message__avatar {
  order: 2;
}

.chat-message--user .chat-message__bubble {
  order: 1;
}

.chat-message__avatar {
  flex-shrink: 0;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.12);
}

.chat-message__bubble {
  max-width: calc(88% - 52px);
  padding: 12px 16px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
  border-radius: 18px;
}

.chat-message__bubble--markdown {
  white-space: normal;
  overflow: hidden;
}

.chat-message--user .chat-message__bubble {
  color: #fff;
  background: linear-gradient(135deg, #1677ff, #36a3ff);
  border-bottom-right-radius: 6px;
}

.chat-message--ai .chat-message__bubble {
  color: #1f2937;
  background: #f5f7fb;
  border-bottom-left-radius: 6px;
}

.markdown-body :deep(*) {
  box-sizing: border-box;
}

.markdown-body :deep(p),
.markdown-body :deep(ul),
.markdown-body :deep(ol),
.markdown-body :deep(blockquote),
.markdown-body :deep(pre),
.markdown-body :deep(table) {
  margin: 0 0 12px;
}

.markdown-body :deep(*:last-child) {
  margin-bottom: 0;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4),
.markdown-body :deep(h5),
.markdown-body :deep(h6) {
  margin: 0 0 12px;
  color: #111827;
  font-weight: 700;
  line-height: 1.45;
}

.markdown-body :deep(h1) {
  font-size: 24px;
}

.markdown-body :deep(h2) {
  font-size: 20px;
}

.markdown-body :deep(h3) {
  font-size: 17px;
}

.markdown-body :deep(p),
.markdown-body :deep(li) {
  color: #374151;
  line-height: 1.8;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  padding-left: 22px;
}

.markdown-body :deep(blockquote) {
  padding: 10px 14px;
  color: #475569;
  background: rgba(148, 163, 184, 0.12);
  border-left: 4px solid #60a5fa;
  border-radius: 10px;
}

.markdown-body :deep(a) {
  color: #1677ff;
  text-decoration: none;
}

.markdown-body :deep(a:hover) {
  text-decoration: underline;
}

.markdown-body :deep(code) {
  padding: 0.15em 0.4em;
  color: #0f172a;
  font-family: 'SFMono-Regular', 'Consolas', 'Liberation Mono', monospace;
  font-size: 0.92em;
  background: rgba(15, 23, 42, 0.08);
  border-radius: 6px;
}

.markdown-body :deep(pre) {
  overflow-x: auto;
  background: #0d1117;
  border-radius: 14px;
}

.markdown-body :deep(pre code) {
  display: block;
  padding: 16px 18px;
  color: inherit;
  background: transparent;
  border-radius: 0;
}

.markdown-body :deep(table) {
  width: 100%;
  overflow: hidden;
  border-collapse: collapse;
  border-radius: 12px;
}

.markdown-body :deep(th),
.markdown-body :deep(td) {
  padding: 10px 12px;
  text-align: left;
  border: 1px solid rgba(148, 163, 184, 0.3);
}

.markdown-body :deep(th) {
  color: #111827;
  background: rgba(226, 232, 240, 0.7);
}

.markdown-body :deep(img) {
  max-width: 100%;
  border-radius: 12px;
}

.chat-panel__input {
  padding: 12px 14px 14px;
  border-top: 1px solid rgba(15, 23, 42, 0.06);
}

.chat-panel__input-wrap {
  border-radius: 14px;
}

.chat-panel__input-wrap--disabled {
  cursor: not-allowed;
}

.chat-panel__input-wrap--disabled :deep(textarea) {
  cursor: not-allowed;
}

.chat-panel__submit {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 12px;
}

.preview-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  color: #111827;
  font-weight: 700;
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
}

.preview-panel__body {
  display: flex;
  flex: 1;
  min-height: 0;
  align-items: center;
  justify-content: center;
  background: #fff;
}

.preview-panel__body :deep(.ant-spin-nested-loading),
.preview-panel__body :deep(.ant-spin-container) {
  width: 100%;
  height: 100%;
}

.preview-panel__body :deep(.ant-spin-container) {
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-panel__iframe {
  width: 100%;
  height: 100%;
  min-height: 0;
  border: 0;
}

@media (max-width: 1100px) {
  .chat-view {
    height: auto;
    min-height: 0;
  }

  .chat-view__main {
    grid-template-columns: 1fr;
  }

  .chat-panel,
  .preview-panel {
    min-height: 420px;
  }

  .chat-panel__messages,
  .preview-panel__body {
    min-height: 0;
  }
}
</style>
