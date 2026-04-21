import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { title: '首页' },
    },
    {
      path: '/app/chat/:id',
      name: 'appChat',
      component: () => import('../views/AppChatView.vue'),
      meta: { title: '应用生成' },
    },
    {
      path: '/app/edit/:id',
      name: 'appEdit',
      component: () => import('../views/AppEditView.vue'),
      meta: { title: '编辑应用' },
    },
    {
      path: '/admin/app/manage',
      name: 'appManage',
      component: () => import('../views/AppManageView.vue'),
      meta: { title: '应用管理', adminOnly: true },
    },
    {
      path: '/user/center',
      name: 'userCenter',
      component: () => import('../views/UserCenterView.vue'),
      meta: { title: '个人中心' },
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
      meta: { title: '关于我们' },
    },
  ],
})

router.beforeEach(async (to) => {
  const { useLoginUserStore } = await import('@/stores/loginUser')
  const loginUserStore = useLoginUserStore()

  if (!loginUserStore.loginUser && !loginUserStore.loading) {
    await loginUserStore.fetchLoginUser()
  }

  if (to.meta?.adminOnly && loginUserStore.loginUser?.userRole !== 'admin') {
    return '/'
  }

  return true
})

router.afterEach((to) => {
  document.title = `${String(to.meta?.title || 'Point AI Code Mother')} - Point AI Code Mother`
})

export default router
