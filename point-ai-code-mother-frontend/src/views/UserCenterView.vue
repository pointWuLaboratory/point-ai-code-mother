<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { listUserVoByPage } from '@/api/userController'
import { useLoginUserStore } from '@/stores/loginUser'

const loginUserStore = useLoginUserStore()
const loading = ref(false)
const userList = ref<API.UserVO[]>([])
const total = ref(0)
const pagination = reactive({
  current: 1,
  pageSize: 10,
})

const loginUser = computed(() => loginUserStore.loginUser)

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80,
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
    key: 'userAccount',
  },
  {
    title: '昵称',
    dataIndex: 'userName',
    key: 'userName',
  },
  {
    title: '角色',
    dataIndex: 'userRole',
    key: 'userRole',
    width: 120,
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
    key: 'userProfile',
    ellipsis: true,
  },
  {
    title: '注册时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 200,
  },
]

const fetchUserList = async () => {
  loading.value = true
  try {
    const res = await listUserVoByPage({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
    })

    if (res.data.code === 0 && res.data.data) {
      userList.value = res.data.data.records || []
      total.value = Number(res.data.data.totalRow || 0)
      return
    }

    message.error(res.data.message || '获取用户列表失败')
  } catch {
    message.error('获取用户列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleTableChange = (page: { current?: number; pageSize?: number }) => {
  pagination.current = page.current || 1
  pagination.pageSize = page.pageSize || 10
  fetchUserList()
}

onMounted(async () => {
  if (!loginUserStore.isLogin) {
    await loginUserStore.fetchLoginUser()
  }
  await fetchUserList()
})
</script>

<template>
  <section class="user-center">
    <div class="user-center__hero">
      <div>
        <div class="user-center__eyebrow">PERSONAL CENTER</div>
        <h1>个人中心</h1>
        <p>查看当前登录用户信息，并浏览系统中的用户列表。</p>
      </div>
    </div>

    <a-row :gutter="[20, 20]">
      <a-col :xs="24" :lg="8">
        <a-card class="user-center__card" :bordered="false">
          <div class="profile-card">
            <a-avatar :src="loginUser?.userAvatar" :size="88">
              {{ (loginUser?.userName || loginUser?.userAccount || 'U').slice(0, 1) }}
            </a-avatar>
            <div class="profile-card__name">{{ loginUser?.userName || '未登录用户' }}</div>
            <div class="profile-card__account">账号：{{ loginUser?.userAccount || '--' }}</div>
            <div class="profile-card__meta">
              <span>角色：{{ loginUser?.userRole || '--' }}</span>
              <span>创建时间：{{ loginUser?.createTime || '--' }}</span>
            </div>
            <a-divider />
            <div class="profile-card__bio">{{ loginUser?.userProfile || '这个用户暂时还没有留下个性签名。' }}</div>
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="16">
        <a-card class="user-center__card" :bordered="false" title="用户列表">
          <a-table
            row-key="id"
            :columns="columns"
            :data-source="userList"
            :loading="loading"
            :pagination="{
              current: pagination.current,
              pageSize: pagination.pageSize,
              total,
              showSizeChanger: true,
              showTotal: (count: number) => `共 ${count} 条`,
            }"
            :scroll="{ x: 900 }"
            @change="handleTableChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'userName'">
                <div class="user-table__name-cell">
                  <a-avatar :src="record.userAvatar" size="small">
                    {{ (record.userName || record.userAccount || 'U').slice(0, 1) }}
                  </a-avatar>
                  <span>{{ record.userName || '未命名用户' }}</span>
                </div>
              </template>
              <template v-else-if="column.dataIndex === 'userRole'">
                <a-tag color="blue">{{ record.userRole || 'user' }}</a-tag>
              </template>
              <template v-else-if="column.dataIndex === 'userProfile'">
                <span>{{ record.userProfile || '--' }}</span>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>
    </a-row>
  </section>
</template>

<style scoped>
.user-center {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.user-center__hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: clamp(24px, 4vw, 36px);
  border: 1px solid rgba(22, 119, 255, 0.12);
  border-radius: 28px;
  background:
    radial-gradient(circle at top right, rgba(22, 119, 255, 0.18), transparent 28%),
    linear-gradient(140deg, rgba(255, 255, 255, 0.95), rgba(244, 248, 255, 0.92));
  box-shadow: 0 18px 44px rgba(46, 83, 143, 0.08);
}

.user-center__eyebrow {
  margin-bottom: 10px;
  color: #1677ff;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
}

.user-center__hero h1 {
  margin: 0 0 12px;
  color: #0f172a;
  font-size: clamp(28px, 4vw, 40px);
}

.user-center__hero p {
  margin: 0;
  color: #64748b;
  line-height: 1.8;
}

.user-center__card {
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.06);
}

.profile-card {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.profile-card__name {
  margin-top: 18px;
  color: #0f172a;
  font-size: 24px;
  font-weight: 700;
}

.profile-card__account {
  margin-top: 8px;
  color: #475569;
}

.profile-card__meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 18px;
  color: #64748b;
}

.profile-card__bio {
  color: #475569;
  line-height: 1.8;
}

.user-table__name-cell {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}
</style>
