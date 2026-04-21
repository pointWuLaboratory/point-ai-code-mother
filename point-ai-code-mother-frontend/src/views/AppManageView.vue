<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import {
  deleteAppByAdmin,
  listAppVoByPageByAdmin,
  updateAppByAdmin,
} from '@/api/appController'

const router = useRouter()
const loading = ref(false)
const appList = ref<API.AppVO[]>([])
const total = ref(0)

const query = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 10,
  appName: undefined,
  codeGenType: undefined,
  userId: undefined,
  priority: undefined,
})

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 90 },
  { title: '应用名称', dataIndex: 'appName', key: 'appName', width: 220 },
  { title: '类型', dataIndex: 'codeGenType', key: 'codeGenType', width: 140 },
  { title: '创建者', dataIndex: ['user', 'userName'], key: 'userName', width: 180 },
  { title: '优先级', dataIndex: 'priority', key: 'priority', width: 120 },
  { title: '部署 Key', dataIndex: 'deployKey', key: 'deployKey', width: 120 ,ellipsis: true },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 190 },
  { title: '操作', key: 'action', fixed: 'right', width: 400 },
]

const fetchList = async () => {
  loading.value = true
  try {
    const res = await listAppVoByPageByAdmin({
      ...query,
    })
    if (res.data.code === 0 && res.data.data) {
      appList.value = res.data.data.records || []
      total.value = Number(res.data.data.totalRow || 0)
      return
    }
    message.error(res.data.message || '获取应用列表失败')
  } catch {
    message.error('获取应用列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleDelete = (record: API.AppVO) => {
  Modal.confirm({
    title: `确认删除 ${record.appName || '该应用'}？`,
    content: '删除后无法恢复，请谨慎操作。',
    async onOk() {
      const res = await deleteAppByAdmin({ id: record.id })
      if (res.data.code === 0) {
        message.success('删除成功')
        await fetchList()
        return
      }
      message.error(res.data.message || '删除失败')
    },
  })
}

const handleSetGood = async (record: API.AppVO) => {
  try {
    const res = await updateAppByAdmin({
      id: record.id,
      appName: record.appName,
      cover: record.cover,
      priority: 99,
    })
    if (res.data.code === 0) {
      message.success('已设为精选')
      await fetchList()
      return
    }
    message.error(res.data.message || '设置精选失败')
  } catch {
    message.error('设置精选失败，请稍后重试')
  }
}

const buildDeployUrl = (record: API.AppVO) => {
  if (!record.deployKey) {
    return ''
  }
  return `http://localhost/${record.deployKey}/`
}

const goToChat = (record: API.AppVO) => {
  router.push({
    name: 'appChat',
    params: { id: String(record.id || '') },
    query: { view: '1' },
  })
}

const goToWork = (record: API.AppVO) => {
  const deployUrl = buildDeployUrl(record)
  if (!deployUrl) {
    return
  }
  window.open(deployUrl, '_blank', 'noopener,noreferrer')
}

onMounted(fetchList)
</script>

<template>
  <section class="app-manage-view">
    <a-card class="app-manage-card" :bordered="false" title="应用管理">
      <div class="app-manage-view__search">
        <a-input-search
          v-model:value="query.appName"
          placeholder="按应用名称搜索"
          style="width: 220px"
          @search="fetchList"
        />
        <a-input
          v-model:value="query.codeGenType"
          placeholder="代码生成类型"
          style="width: 180px"
        />
        <a-input-number v-model:value="query.userId" placeholder="用户 ID" style="width: 160px" />
        <a-input-number v-model:value="query.priority" placeholder="优先级" style="width: 160px" />
        <a-button type="primary" @click="fetchList">查询</a-button>
      </div>

      <a-table
        row-key="id"
        :columns="columns"
        :data-source="appList"
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
          <template v-if="column.key === 'userName'">
            {{ record.user?.userName || record.user?.userAccount || '--' }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" @click="goToChat(record)">查看对话</a-button>
              <a-button v-if="record.deployKey" type="link" @click="goToWork(record)">查看作品</a-button>
              <a-button type="link" @click="router.push(`/app/edit/${record.id}`)">编辑</a-button>
              <a-button type="link" @click="handleSetGood(record)">精选</a-button>
              <a-button danger type="link" @click="handleDelete(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </section>
</template>

<style scoped>
.app-manage-view {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.app-manage-card {
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 18px 44px rgba(15, 23, 42, 0.06);
}

.app-manage-view__search {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}
</style>
