<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import { getAppVoById, getAppVoByIdByAdmin, updateApp, updateAppByAdmin } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const appId = computed(() => String(route.params.id || ''))
const loading = ref(false)
const submitting = ref(false)
const isAdmin = computed(() => loginUserStore.loginUser?.userRole === 'admin')

const formState = reactive<API.AppAdminUpdateRequest>({
  id: undefined,
  appName: '',
  cover: '',
  priority: 0,
})

const loadDetail = async () => {
  if (!appId.value) {
    message.error('应用 id 不合法')
    router.push('/')
    return
  }

  loading.value = true
  try {
    const request = isAdmin.value ? getAppVoByIdByAdmin : getAppVoById
    const res = await request({ id: appId.value })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      if (!isAdmin.value && String(data.userId ?? '') !== String(loginUserStore.loginUser?.id ?? '')) {
        message.error('你只能编辑自己的应用')
        router.push('/')
        return
      }
      formState.id = data.id
      formState.appName = data.appName || ''
      formState.cover = data.cover || ''
      formState.priority = data.priority || 0
      return
    }
    message.error(res.data.message || '获取应用信息失败')
  } catch {
    message.error('获取应用信息失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!formState.id) {
    return
  }
  if (!formState.appName?.trim()) {
    message.warning('请输入应用名称')
    return
  }

  submitting.value = true
  try {
    const res = isAdmin.value
      ? await updateAppByAdmin({
          id: formState.id,
          appName: formState.appName.trim(),
          cover: formState.cover?.trim() || undefined,
          priority: formState.priority,
        })
      : await updateApp({
          id: formState.id,
          appName: formState.appName.trim(),
        })

    if (res.data.code === 0) {
      message.success('保存成功')
      router.push(`/app/chat/${formState.id}`)
      return
    }
    message.error(res.data.message || '保存失败')
  } catch {
    message.error('保存失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  if (!loginUserStore.loginUser) {
    await loginUserStore.fetchLoginUser()
  }
  await loadDetail()
})
</script>

<template>
  <section class="app-edit-view">
    <a-card class="app-edit-card" :bordered="false" :loading="loading">
      <template #title>应用信息修改</template>
      <a-form layout="vertical">
        <a-form-item label="应用名称" required>
          <a-input v-model:value="formState.appName" placeholder="请输入应用名称" />
        </a-form-item>

        <template v-if="isAdmin">
          <a-form-item label="应用封面">
            <a-input v-model:value="formState.cover" placeholder="请输入封面图片 URL" />
          </a-form-item>
          <a-form-item label="优先级">
            <a-input-number v-model:value="formState.priority" style="width: 100%" :min="0" />
          </a-form-item>
        </template>

        <div class="app-edit-view__actions">
          <a-button @click="router.back()">取消</a-button>
          <a-button type="primary" :loading="submitting" @click="handleSubmit">保存</a-button>
        </div>
      </a-form>
    </a-card>
  </section>
</template>

<style scoped>
.app-edit-view {
  display: flex;
  justify-content: center;
  padding-top: 24px;
}

.app-edit-card {
  width: min(100%, 760px);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 18px 44px rgba(15, 23, 42, 0.06);
}

.app-edit-view__actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
