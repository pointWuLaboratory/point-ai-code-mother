<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import type { RouteLocationRaw } from 'vue-router'
import { useRouter } from 'vue-router'
import { addApp, listGoodAppVoByPage, listMyAppVoByPage } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const creating = ref(false)
const promptText = ref('')

const myLoading = ref(false)
const goodLoading = ref(false)
const myList = ref<API.AppVO[]>([])
const goodList = ref<API.AppVO[]>([])

const myQuery = reactive({
  pageNum: 1,
  pageSize: 6,
  appName: '',
})

const goodQuery = reactive({
  pageNum: 1,
  pageSize: 6,
  appName: '',
})

const myTotal = ref(0)
const goodTotal = ref(0)

const canCreate = computed(() => Boolean(promptText.value.trim()))

const promptCaseList = [
  {
    label: '波塞风电商页面',
    prompt:
      '创建一个高转化的电商首页，品牌名为波塞风。整体风格要高级、清爽、有科技感。设计一个完整的电商页面，包括顶部导航、主视觉 Banner、热门商品推荐、活动专区、品牌卖点、用户评价和底部页脚等部分。',
  },
  {
    label: '企业网站',
    prompt:
      '创建一个企业网站，风格要大气、商务、专业。设计一个完整的企业网站首页，包括导航栏、hero 区域、服务介绍、公司优势、客户评价、合作伙伴、联系我们和底部页脚等部分。',
  },
  {
    label: '电商运营后台',
    prompt:
      '创建一个电商运营后台管理系统，风格要简洁、专业、数据化。设计一个完整的后台首页，包括侧边导航、顶部工具栏、核心数据卡片、订单趋势图、商品销售排行、待办事项和快捷操作区域。',
  },
  {
    label: '暗黑话题社区',
    prompt:
      '创建一个暗黑风格的话题社区首页，整体视觉要酷、沉浸、有未来感。设计一个完整的社区页面，包括顶部导航、热门话题区、推荐帖子列表、活跃用户、分类标签、发帖入口和社区公告等部分。',
  },
] as const

const fillPromptByCase = (prompt: string) => {
  promptText.value = prompt
}

const buildPreviewUrl = (item: API.AppVO) => {
  if (!item.id || !item.codeGenType) {
    return ''
  }
  return `http://localhost:8123/api/static/${item.codeGenType}_${item.id}/`
}

const buildDeployUrl = (item: API.AppVO) => {
  if (!item.deployKey) {
    return ''
  }
  return `http://localhost/${item.deployKey}/`
}

const buildChatRoute = (item: API.AppVO): RouteLocationRaw => ({
  name: 'appChat',
  params: { id: String(item.id || '') },
  query: { view: '1' },
})

const fetchMyApps = async () => {
  if (!loginUserStore.isLogin) {
    myList.value = []
    myTotal.value = 0
    return
  }

  myLoading.value = true
  try {
    const res = await listMyAppVoByPage({
      pageNum: myQuery.pageNum,
      pageSize: myQuery.pageSize,
      appName: myQuery.appName || undefined,
      sortField: 'createTime',
      sortOrder: 'desc',
    })
    if (res.data.code === 0 && res.data.data) {
      myList.value = res.data.data.records || []
      myTotal.value = Number(res.data.data.totalRow || 0)
      return
    }
    message.error(res.data.message || '获取我的应用失败')
  } catch {
    message.error('获取我的应用失败，请稍后重试')
  } finally {
    myLoading.value = false
  }
}

const fetchGoodApps = async () => {
  goodLoading.value = true
  try {
    const res = await listGoodAppVoByPage({
      pageNum: goodQuery.pageNum,
      pageSize: goodQuery.pageSize,
      appName: goodQuery.appName || undefined,
      sortField: 'priority',
      sortOrder: 'desc',
    })
    if (res.data.code === 0 && res.data.data) {
      goodList.value = res.data.data.records || []
      goodTotal.value = Number(res.data.data.totalRow || 0)
      return
    }
    message.error(res.data.message || '获取精选应用失败')
  } catch {
    message.error('获取精选应用失败，请稍后重试')
  } finally {
    goodLoading.value = false
  }
}

const handleCreateApp = async () => {
  const initPrompt = promptText.value.trim()
  if (!initPrompt) {
    message.warning('请输入应用需求描述')
    return
  }
  if (!loginUserStore.isLogin) {
    message.warning('请先登录后再创建应用')
    return
  }

  creating.value = true
  try {
    const res = await addApp({ initPrompt })
    if (res.data.code === 0 && res.data.data) {
      message.success('应用创建成功，开始生成中')
      router.push({
        name: 'appChat',
        params: { id: String(res.data.data) },
        query: { autoStart: '1', initPrompt },
      })
      return
    }
    message.error(res.data.message || '创建应用失败')
  } catch {
    message.error('创建应用失败，请稍后重试')
  } finally {
    creating.value = false
  }
}

const goToChat = (item: API.AppVO) => {
  router.push(buildChatRoute(item))
}

const goToWork = (item: API.AppVO) => {
  const deployUrl = buildDeployUrl(item)
  if (!deployUrl) {
    return
  }
  window.open(deployUrl, '_blank', 'noopener,noreferrer')
}

const goToEdit = (item: API.AppVO) => {
  router.push(`/app/edit/${item.id}`)
}

onMounted(async () => {
  if (!loginUserStore.loginUser) {
    await loginUserStore.fetchLoginUser()
  }
  await Promise.all([fetchMyApps(), fetchGoodApps()])
})
</script>

<template>
  <section class="home-view">
    <div class="hero-section">
      <div class="hero-section__title-wrap">
        <h1>一句话 · 呈所想</h1>
        <p>与 AI 对话轻松创建应用和网站</p>
      </div>

      <div class="prompt-card">
        <a-textarea
          v-model:value="promptText"
          :auto-size="{ minRows: 4, maxRows: 6 }"
          placeholder="使用 NoCode 创建一个高效的小工具，帮我计算……"
          class="prompt-card__textarea"
        />
        <div class="prompt-card__footer">
          <div class="prompt-card__tags">
            <!-- <a-tag>上传</a-tag>
            <a-tag>优化</a-tag> -->
          </div>
          <a-button type="primary" shape="circle" :loading="creating" :disabled="!canCreate" @click="handleCreateApp">
            ↑
          </a-button>
        </div>
      </div>

      <div class="hero-section__cases">
        <a-tag
          v-for="item in promptCaseList"
          :key="item.label"
          class="hero-section__case-tag"
          @click="fillPromptByCase(item.prompt)"
        >
          {{ item.label }}
        </a-tag>
      </div>
    </div>

    <a-card class="section-card" :bordered="false">
      <template #title>
        <div class="section-card__header">
          <span>我的作品</span>
          <div class="section-card__actions">
            <a-input-search
              v-model:value="myQuery.appName"
              placeholder="按名称搜索"
              style="width: 220px"
              @search="fetchMyApps"
            />
          </div>
        </div>
      </template>

      <a-empty v-if="!loginUserStore.isLogin" description="登录后查看我的应用" />
      <template v-else>
        <a-row :gutter="[20, 20]">
          <a-col v-for="item in myList" :key="item.id" :xs="24" :sm="12" :xl="8">
            <a-card hoverable class="app-card" :loading="myLoading" @click="goToChat(item)">
              <div class="app-card__cover">
                <img v-if="item.cover" :src="item.cover" :alt="item.appName" />
                <iframe
                  v-else-if="buildPreviewUrl(item)"
                  :src="buildPreviewUrl(item)"
                  class="app-card__iframe"
                  loading="lazy"
                />
                <div v-else class="app-card__placeholder">应用预览</div>
              </div>
              <div class="app-card__body">
                <div class="app-card__title">{{ item.appName || '未命名应用' }}</div>
                <div class="app-card__meta">创建于 {{ item.createTime || '--' }}</div>
                <div class="app-card__toolbar" @click.stop>
                  <a-button type="link" @click="goToChat(item)">查看对话</a-button>
                  <a-button v-if="item.deployKey" type="link" @click="goToWork(item)">查看作品</a-button>
                  <a-button type="link" @click="goToEdit(item)">编辑名称</a-button>
                </div>
              </div>
            </a-card>
          </a-col>
        </a-row>
        <a-empty v-if="!myLoading && myList.length === 0" description="暂无应用，快去生成一个吧" />
        <div class="section-card__pagination">
          <a-pagination
            v-model:current="myQuery.pageNum"
            :page-size="myQuery.pageSize"
            :total="myTotal"
            simple
            @change="fetchMyApps"
          />
        </div>
      </template>
    </a-card>

    <a-card class="section-card" :bordered="false">
      <template #title>
        <div class="section-card__header">
          <span>精选案例</span>
          <div class="section-card__actions">
            <a-input-search
              v-model:value="goodQuery.appName"
              placeholder="搜索精选应用"
              style="width: 220px"
              @search="fetchGoodApps"
            />
          </div>
        </div>
      </template>

      <a-row :gutter="[20, 20]">
        <a-col v-for="item in goodList" :key="item.id" :xs="24" :sm="12" :xl="8">
          <a-card hoverable class="app-card" :loading="goodLoading" @click="goToChat(item)">
            <div class="app-card__cover">
              <img v-if="item.cover" :src="item.cover" :alt="item.appName" />
              <iframe
                v-else-if="buildPreviewUrl(item)"
                :src="buildPreviewUrl(item)"
                class="app-card__iframe"
                loading="lazy"
              />
              <div v-else class="app-card__placeholder">精选预览</div>
            </div>
            <div class="app-card__body">
              <div class="app-card__title">{{ item.appName || '未命名应用' }}</div>
              <div class="app-card__meta">{{ item.user?.userName || item.user?.userAccount || '官方推荐' }}</div>
              <div class="app-card__toolbar" @click.stop>
                <a-button type="link" @click="goToChat(item)">查看对话</a-button>
                <a-button v-if="item.deployKey" type="link" @click="goToWork(item)">查看作品</a-button>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
      <a-empty v-if="!goodLoading && goodList.length === 0" description="暂无精选案例" />
      <div class="section-card__pagination">
        <a-pagination
          v-model:current="goodQuery.pageNum"
          :page-size="goodQuery.pageSize"
          :total="goodTotal"
          simple
          @change="fetchGoodApps"
        />
      </div>
    </a-card>
  </section>
</template>

<style scoped>
.home-view {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.hero-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 18px;
  padding: 36px 20px 18px;
  text-align: center;
}

.hero-section__title-wrap h1 {
  margin: 0;
  color: #111827;
  font-size: clamp(38px, 6vw, 56px);
  font-weight: 800;
}

.hero-section__title-wrap p {
  margin: 12px 0 0;
  color: #6b7280;
  font-size: 16px;
}

.prompt-card {
  width: min(100%, 920px);
  padding: 16px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 28px;
  box-shadow: 0 20px 50px rgba(51, 65, 85, 0.08);
}

.prompt-card__textarea :deep(textarea) {
  background: transparent;
  border: none;
  box-shadow: none;
  font-size: 16px;
}

.prompt-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 14px;
}

.prompt-card__tags {
  display: flex;
  gap: 8px;
}

.hero-section__cases {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
}

.hero-section__case-tag {
  padding: 8px 12px;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.hero-section__case-tag:hover {
  color: #1677ff;
  border-color: #91caff;
  background: #e6f4ff;
}

.section-card {
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.84);
  box-shadow: 0 16px 44px rgba(15, 23, 42, 0.06);
}

.section-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.section-card__pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.app-card {
  overflow: hidden;
  border-radius: 22px;
}

.app-card__cover {
  height: 190px;
  overflow: hidden;
  background: linear-gradient(135deg, #f3f7ff, #eaf6ff);
  border-radius: 16px;
}

.app-card__cover img,
.app-card__iframe {
  width: 100%;
  height: 100%;
  border: 0;
  object-fit: cover;
}

.app-card__placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #94a3b8;
  font-size: 15px;
}

.app-card__body {
  padding-top: 16px;
}

.app-card__title {
  color: #111827;
  font-size: 18px;
  font-weight: 700;
}

.app-card__meta {
  margin-top: 8px;
  color: #6b7280;
}

.app-card__toolbar {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}

@media (max-width: 768px) {
  .section-card__header {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
