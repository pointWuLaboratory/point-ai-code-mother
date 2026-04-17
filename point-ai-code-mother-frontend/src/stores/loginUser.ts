import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { getLoginUser } from '@/api/userController'

export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<API.LoginUserVO | null>(null)
  const loading = ref(false)

  const isLogin = computed(() => Boolean(loginUser.value?.id))

  async function fetchLoginUser() {
    loading.value = true
    try {
      const res = await getLoginUser()
      if (res.data.code === 0 && res.data.data) {
        loginUser.value = res.data.data
        return res.data.data
      }
      loginUser.value = null
      return null
    } catch {
      loginUser.value = null
      return null
    } finally {
      loading.value = false
    }
  }

  function setLoginUser(newLoginUser: API.LoginUserVO | null) {
    loginUser.value = newLoginUser
  }

  function clearLoginUser() {
    loginUser.value = null
  }

  return { loginUser, loading, isLogin, setLoginUser, clearLoginUser, fetchLoginUser }
})
