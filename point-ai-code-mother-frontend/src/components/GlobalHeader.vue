<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

interface MenuItem {
  key: string
  label: string
}

const route = useRoute()
const router = useRouter()

const menuItems: MenuItem[] = [
  { key: '/', label: '首页' },
  { key: '/about', label: '关于我们' },
]

const selectedKeys = computed(() => [route.path])

const handleMenuClick = ({ key }: { key: string }) => {
  router.push(key)
}
</script>

<template>
  <div class="global-header">
    <div class="global-header__brand">
      <img alt="Point AI Code Mother" class="global-header__logo" src="@/assets/logo.svg" />
      <span class="global-header__title">Point AI Code Mother</span>
    </div>

    <a-menu
      mode="horizontal"
      :items="menuItems"
      :selected-keys="selectedKeys"
      class="global-header__menu"
      @click="handleMenuClick"
    />

    <div class="global-header__actions">
      <a-button type="primary" size="large">登录</a-button>
    </div>
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
  display: inline-flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.global-header__logo {
  width: 40px;
  height: 40px;
  padding: 6px;
  object-fit: contain;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(22, 119, 255, 0.16), rgba(114, 46, 209, 0.14));
  box-shadow: 0 10px 24px rgba(22, 119, 255, 0.14);
}

.global-header__title {
  color: #0f172a;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.02em;
  white-space: nowrap;
}

.global-header__menu {
  flex: 1;
  min-width: 0;
  justify-content: center;
  background: transparent;
  border-bottom: none;
}

:deep(.global-header__menu .ant-menu-overflow) {
  justify-content: center;
}

:deep(.global-header__menu.ant-menu-horizontal > .ant-menu-item),
:deep(.global-header__menu.ant-menu-horizontal > .ant-menu-submenu) {
  top: 0;
  height: 72px;
  line-height: 72px;
  padding-inline: 18px;
  font-weight: 500;
}

.global-header__actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

@media (max-width: 900px) {
  .global-header {
    flex-wrap: wrap;
    justify-content: center;
    gap: 12px;
  }

  .global-header__brand,
  .global-header__actions {
    width: 100%;
    justify-content: center;
  }

  .global-header__menu {
    order: 3;
    width: 100%;
  }

  :deep(.global-header__menu .ant-menu-overflow) {
    justify-content: center;
  }

  :deep(.global-header__menu.ant-menu-horizontal > .ant-menu-item),
  :deep(.global-header__menu.ant-menu-horizontal > .ant-menu-submenu) {
    height: 48px;
    line-height: 48px;
  }
}
</style>
