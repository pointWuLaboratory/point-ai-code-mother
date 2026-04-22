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
    label: '企业官网',
    prompt:
      '帮我创建一个面向科技公司的企业官网，整体风格现代、专业、有未来感。首页需要包含顶部导航、品牌主视觉、核心产品能力介绍、解决方案模块、客户案例展示、合作伙伴 Logo、团队介绍、联系我们和页脚。要求适配移动端，按钮和配色要有明显科技感，文字内容先用中文示例填充。',
  },
  {
    label: '个人博客',
    prompt:
      '帮我创建一个个人博客网站，适合展示技术文章和成长记录。页面需要有首页 Banner、个人介绍、文章列表、热门标签、精选推荐、时间线、关于我和联系方式等模块。整体风格简洁但有设计感，阅读体验要舒服，支持深色系科技风视觉，并预留文章详情页和分类筛选入口。',
  },
  {
    label: '电商首页',
    prompt:
      '帮我创建一个高转化的电商首页，主打数码产品销售。需要包含顶部搜索区、分类导航、促销 Banner、爆款商品推荐、限时活动、品牌专区、用户评价、常见问题和底部服务承诺。整体视觉要高级、年轻、科技感强，卡片样式统一，突出价格、优惠信息和立即购买按钮。',
  },
  {
    label: '活动落地页',
    prompt:
      '帮我创建一个新品发布活动落地页，适合做营销推广和线索收集。页面需要有震撼的首屏视觉、产品亮点介绍、功能对比、用户收益、使用场景、报名表单、常见问答和底部 CTA。整体风格要炫酷、未来感、带渐变光效，重点突出转化按钮，并兼顾手机端浏览体验。',
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
    <div class="hero-shell">
      <div class="hero-section">
        <div class="hero-section__title-wrap">
          <h1>AI 应用生成平台</h1>
          <p>一句话轻松创建网站应用</p>
        </div>

        <div class="prompt-card">
          <a-textarea
            v-model:value="promptText"
            :auto-size="{ minRows: 4, maxRows: 6 }"
            placeholder="帮我创建个人博客网站"
            class="prompt-card__textarea"
          />
          <div class="prompt-card__footer">
            <div class="prompt-card__hint">输入你的网站需求，AI 会自动帮你生成页面原型与代码</div>
            <a-button
              type="primary"
              shape="circle"
              size="large"
              :loading="creating"
              :disabled="!canCreate"
              @click="handleCreateApp"
            >
              ↑
            </a-button>
          </div>
        </div>

        <div class="hero-section__cases">
          <button
            v-for="item in promptCaseList"
            :key="item.label"
            type="button"
            class="case-card"
            @click="fillPromptByCase(item.prompt)"
          >
            <span class="case-card__label">{{ item.label }}</span>
            <span class="case-card__text">{{ item.prompt }}</span>
          </button>
        </div>
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
  gap: 24px;
}

.hero-shell {
  position: relative;
  left: 50%;
  width: 100vw;
  margin-left: -50vw;
  padding: 24px 0 36px;
  overflow: hidden;
  background:
    radial-gradient(circle at 20% 20%, rgba(89, 110, 255, 0.38), transparent 24%),
    radial-gradient(circle at 80% 18%, rgba(0, 229, 255, 0.32), transparent 22%),
    radial-gradient(circle at 50% 75%, rgba(160, 84, 255, 0.2), transparent 30%),
    linear-gradient(135deg, #060b1f 0%, #0d1633 32%, #121c47 65%, #071126 100%);
}

.hero-shell::before {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(rgba(255, 255, 255, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.06) 1px, transparent 1px);
  background-size: 44px 44px;
  opacity: 0.18;
  content: '';
  pointer-events: none;
}

.hero-section {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  width: 100%;
  max-width: 1280px;
  padding: 28px 20px 0;
  margin: 0 auto;
  text-align: center;
}

.hero-section__title-wrap h1 {
  margin: 0;
  color: #f8fbff;
  font-size: clamp(40px, 6vw, 64px);
  font-weight: 800;
  line-height: 1.08;
  letter-spacing: 0.02em;
  text-shadow: 0 10px 30px rgba(0, 0, 0, 0.28);
}

.hero-section__title-wrap p {
  margin: 14px 0 0;
  color: rgba(226, 232, 240, 0.92);
  font-size: 18px;
  letter-spacing: 0.04em;
}

.prompt-card {
  width: min(100%, 980px);
  padding: 18px 18px 16px;
  background: rgba(7, 14, 38, 0.62);
  border: 1px solid rgba(125, 211, 252, 0.18);
  border-radius: 28px;
  box-shadow:
    0 24px 70px rgba(3, 7, 18, 0.38),
    inset 0 1px 0 rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(16px);
}

.prompt-card__textarea {
  border-radius: 22px;
}

.prompt-card__textarea :deep(.ant-input) {
  padding: 8px 2px;
  color: #f8fafc;
  font-size: 17px;
  line-height: 1.8;
  background: transparent;
  border: none;
  box-shadow: none;
}

.prompt-card__textarea :deep(.ant-input::placeholder) {
  color: rgba(226, 232, 240, 0.52);
}

.prompt-card__textarea :deep(.ant-input:focus) {
  box-shadow: none;
}

.prompt-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 10px;
}

.prompt-card__hint {
  color: rgba(191, 219, 254, 0.82);
  font-size: 14px;
  text-align: left;
}

.hero-section__cases {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  width: min(100%, 1100px);
}

.case-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 18px 20px;
  color: #dbeafe;
  text-align: left;
  background: rgba(12, 21, 50, 0.56);
  border: 1px solid rgba(125, 211, 252, 0.16);
  border-radius: 22px;
  box-shadow: 0 18px 40px rgba(2, 6, 23, 0.24);
  cursor: pointer;
  transition:
    transform 0.2s ease,
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    background 0.2s ease;
}

.case-card:hover {
  background: rgba(17, 28, 68, 0.88);
  border-color: rgba(125, 211, 252, 0.42);
  box-shadow: 0 24px 60px rgba(2, 6, 23, 0.36);
  transform: translateY(-2px);
}

.case-card__label {
  color: #ffffff;
  font-size: 18px;
  font-weight: 700;
}

.case-card__text {
  color: rgba(219, 234, 254, 0.84);
  line-height: 1.8;
}

.section-card {
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.88);
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

@media (max-width: 900px) {
  .hero-section__cases {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .home-view {
    gap: 18px;
  }

  .hero-shell {
    padding-top: 12px;
    padding-bottom: 24px;
  }

  .hero-section {
    gap: 18px;
    padding-inline: 16px;
  }

  .prompt-card__footer {
    flex-direction: column;
    align-items: stretch;
  }

  .section-card__header {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
