<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { listAllChatHistoryByPageForAdmin } from '@/api/chatHistoryController'

const router = useRouter()
const loading = ref(false)
const chatHistoryList = ref<API.ChatHistory[]>([])
const total = ref(0)

const query = reactive<API.ChatHistoryQueryRequest>({
  pageNum: 1,
  pageSize: 10,
  appId: undefined,
  userId: undefined,
  messageType: undefined,
  message: undefined,
  lastCreateTime: undefined,
})

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 90 },
  { title: '应用 ID', dataIndex: 'appId', key: 'appId', width: 120 },
  { title: '用户 ID', dataIndex: 'userId', key: 'userId', width: 120 },
  { title: '消息类型', dataIndex: 'messageType', key: 'messageType', width: 140 },
  { title: '消息内容', dataIndex: 'message', key: 'message', ellipsis: true },
  { title: '父消息 ID', dataIndex: 'parentId', key: 'parentId', width: 120 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 190 },
  { title: '操作', key: 'action', fixed: 'right', width: 180 },
]

const fetchList = async () => {
  loading.value = true
  try {
    const res = await listAllChatHistoryByPageForAdmin({ ...query })
    if (res.data.code === 0 && res.data.data) {
      chatHistoryList.value = res.data.data.records || []
      total.value = Number(res.data.data.totalRow || 0)
      return
    }
    message.error(res.data.message || '获取对话记录失败')
  } catch {
    message.error('获取对话记录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const goToAppChat = (record: API.ChatHistory) => {
  if (!record.appId) {
    message.warning('该记录缺少应用信息，无法查看')
    return
  }
  router.push(`/app/chat/${record.appId}`)
}

onMounted(fetchList)
</script>

<template>
  <section class="conversation-manage-view">
    <a-card class="conversation-manage-card" :bordered="false" title="对话管理">
      <div class="conversation-manage-view__search">
        <a-input-number v-model:value="query.appId" placeholder="应用 ID" style="width: 160px" />
        <a-input-number v-model:value="query.userId" placeholder="用户 ID" style="width: 160px" />
        <a-input v-model:value="query.messageType" placeholder="消息类型" style="width: 180px" />
        <a-input-search
          v-model:value="query.message"
          placeholder="按消息内容搜索"
          style="width: 240px"
          @search="fetchList"
        />
        <a-input v-model:value="query.lastCreateTime" placeholder="游标时间" style="width: 220px" />
        <a-button type="primary" @click="fetchList">查询</a-button>
      </div>

      <a-table
        row-key="id"
        :columns="columns"
        :data-source="chatHistoryList"
        :loading="loading"
        :pagination="{
          current: query.pageNum,
          pageSize: query.pageSize,
          total,
          showSizeChanger: true,
          showTotal: (count: number) => `共 ${count} 条`,
          onChange: (page: number, pageSize: number) => {
            query.pageNum = page
            query.pageSize = pageSize
            fetchList()
          },
        }"
        :scroll="{ x: 1300 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'message'">
            <div class="conversation-manage-view__message">{{ record.message || '--' }}</div>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" @click="goToAppChat(record)">查看对话</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </section>
</template>

<style scoped>
.conversation-manage-view {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.conversation-manage-card {
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 18px 44px rgba(15, 23, 42, 0.06);
}

.conversation-manage-view__search {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.conversation-manage-view__message {
  max-width: 420px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
