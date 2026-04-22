<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import { userLogin, userLogout, userRegister } from '@/api/userController'
import { useLoginUserStore } from '@/stores/loginUser'

interface MenuItem {
  key: string
  label: string
}

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const menuItems = computed<MenuItem[]>(() => {
  const items: MenuItem[] = [
    { key: '/', label: '首页' },
    { key: '/about', label: '关于' },
  ]

  if (loginUserStore.loginUser?.userRole === 'admin') {
    items.push({ key: '/admin/app/manage', label: '应用管理' })
    items.push({ key: '/admin/conversation/manage', label: '对话管理' })
  }

  return items
})

const authModalOpen = ref(false)
const authMode = ref<'login' | 'register'>('login')
const loginLoading = ref(false)
const registerLoading = ref(false)

const loginForm = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const registerForm = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const selectedKeys = computed(() => {
  const matched = menuItems.value.find(
    (item) => route.path === item.key || route.path.startsWith(`${item.key}/`),
  )
  return matched ? [matched.key] : []
})

const userDropdownItems = computed(() => {
  const items = [{ key: 'center', label: '个人中心' }]
  if (loginUserStore.loginUser?.userRole === 'admin') {
    items.push({ key: 'manage', label: '应用管理' })
    items.push({ key: 'conversationManage', label: '对话管理' })
  }
  items.push({ key: 'logout', label: '退出登录' })
  return items
})

const displayUserName = computed(() => {
  return loginUserStore.loginUser?.userName || loginUserStore.loginUser?.userAccount || '用户'
})

const avatarText = computed(() => displayUserName.value.slice(0, 1).toUpperCase())

const openAuthModal = (mode: 'login' | 'register' = 'login') => {
  authMode.value = mode
  authModalOpen.value = true
}

const resetForms = () => {
  loginForm.userAccount = ''
  loginForm.userPassword = ''
  registerForm.userAccount = ''
  registerForm.userPassword = ''
  registerForm.checkPassword = ''
}

const closeAuthModal = () => {
  authModalOpen.value = false
  authMode.value = 'login'
}

const handleMenuClick = ({ key }: { key: string }) => {
  router.push(key)
}

const handleLogin = async () => {
  if (!loginForm.userAccount || !loginForm.userPassword) {
    message.warning('请输入账号和密码')
    return
  }

  loginLoading.value = true
  try {
    const res = await userLogin(loginForm)
    if (res.data.code === 0 && res.data.data) {
      loginUserStore.setLoginUser(res.data.data)
      message.success('登录成功')
      closeAuthModal()
      resetForms()
      window.location.reload()
      return
    }
    message.error(res.data.message || '登录失败')
  } catch {
    message.error('登录失败，请稍后重试')
  } finally {
    loginLoading.value = false
  }
}

const handleRegister = async () => {
  if (!registerForm.userAccount || !registerForm.userPassword || !registerForm.checkPassword) {
    message.warning('请完整填写注册信息')
    return
  }

  registerLoading.value = true
  try {
    const res = await userRegister(registerForm)
    if (res.data.code === 0) {
      message.success('注册成功，请登录')
      loginForm.userAccount = registerForm.userAccount
      loginForm.userPassword = ''
      authMode.value = 'login'
      registerForm.userPassword = ''
      registerForm.checkPassword = ''
      return
    }
    message.error(res.data.message || '注册失败')
  } catch {
    message.error('注册失败，请稍后重试')
  } finally {
    registerLoading.value = false
  }
}

const handleUserAction = async ({ key }: { key: string }) => {
  if (key === 'center') {
    router.push('/user/center')
    return
  }

  if (key === 'manage') {
    router.push('/admin/app/manage')
    return
  }

  if (key === 'conversationManage') {
    router.push('/admin/conversation/manage')
    return
  }

  if (key === 'logout') {
    try {
      const res = await userLogout()
      if (res.data.code === 0) {
        loginUserStore.clearLoginUser()
        message.success('已退出登录')
        router.push('/')
        return
      }
      message.error(res.data.message || '退出登录失败')
    } catch {
      message.error('退出登录失败，请稍后重试')
    }
  }
}
</script>

<template>
  <div class="global-header">
    <div class="global-header__brand" @click="router.push('/')">
      <img alt="Point AI Code Mother" class="global-header__logo" src="@/assets/logo.png" />
      <span class="global-header__title">AI Coding</span>
    </div>

    <a-menu
      mode="horizontal"
      :items="menuItems"
      :selected-keys="selectedKeys"
      class="global-header__menu"
      @click="handleMenuClick"
    />

    <div class="global-header__actions">
      <template v-if="loginUserStore.isLogin">
        <a-dropdown>
          <div class="global-header__user">
            <a-avatar :src="loginUserStore.loginUser?.userAvatar" :size="40" class="global-header__avatar">
              {{ avatarText }}
            </a-avatar>
            <span class="global-header__nickname">{{ displayUserName }}</span>
          </div>
          <template #overlay>
            <a-menu :items="userDropdownItems" @click="handleUserAction" />
          </template>
        </a-dropdown>
      </template>
      <template v-else>
        <div class="global-header__guest-actions">
          <a-button type="primary" size="large" @click="openAuthModal('login')">登录</a-button>
        </div>
      </template>
    </div>

    <a-modal
      :open="authModalOpen"
      :footer="null"
      :centered="true"
      width="440px"
      destroy-on-close
      @cancel="closeAuthModal"
    >
      <div class="auth-modal">
        <div class="auth-modal__title">欢迎来到一句话 · 呈所想</div>
        <a-tabs v-model:active-key="authMode" centered>
          <a-tab-pane key="login" tab="登录">
            <a-form layout="vertical">
              <a-form-item label="账号">
                <a-input v-model:value="loginForm.userAccount" size="large" placeholder="请输入账号" />
              </a-form-item>
              <a-form-item label="密码">
                <a-input-password
                  v-model:value="loginForm.userPassword"
                  size="large"
                  placeholder="请输入密码"
                />
              </a-form-item>
              <a-button type="primary" block size="large" :loading="loginLoading" @click="handleLogin">
                登录
              </a-button>
            </a-form>
          </a-tab-pane>
          <a-tab-pane key="register" tab="注册">
            <a-form layout="vertical">
              <a-form-item label="账号">
                <a-input v-model:value="registerForm.userAccount" size="large" placeholder="请设置账号" />
              </a-form-item>
              <a-form-item label="密码">
                <a-input-password
                  v-model:value="registerForm.userPassword"
                  size="large"
                  placeholder="请设置密码"
                />
              </a-form-item>
              <a-form-item label="确认密码">
                <a-input-password
                  v-model:value="registerForm.checkPassword"
                  size="large"
                  placeholder="请再次输入密码"
                />
              </a-form-item>
              <a-button
                type="primary"
                block
                size="large"
                :loading="registerLoading"
                @click="handleRegister"
              >
                注册
              </a-button>
            </a-form>
          </a-tab-pane>
        </a-tabs>
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
.global-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.global-header__brand {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  cursor: pointer;
}

.global-header__logo {
  width: 36px;
  height: 36px;
  border-radius: 12px;
}

.global-header__title {
  color: #0f172a;
  font-size: 20px;
  font-weight: 700;
  white-space: nowrap;
}

.global-header__menu {
  flex: 1;
  min-width: 0;
  background: transparent;
  border-bottom: none;
}

.global-header__actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  min-width: fit-content;
}

.global-header__guest-actions,
.global-header__user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.global-header__user {
  padding: 6px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.global-header__user:hover {
  background: rgba(22, 119, 255, 0.08);
}

.global-header__nickname {
  max-width: 120px;
  overflow: hidden;
  color: #0f172a;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.auth-modal__title {
  margin-bottom: 20px;
  color: #0f172a;
  font-size: 24px;
  font-weight: 700;
  text-align: center;
}

@media (max-width: 900px) {
  .global-header {
    flex-wrap: wrap;
    justify-content: center;
    padding-block: 10px;
  }

  .global-header__menu {
    order: 3;
    width: 100%;
  }
}
</style>
