<script setup lang="ts">
import { RouterView } from 'vue-router'
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
</script>

<template>
  <a-layout class="basic-layout">
    <a-layout-header class="basic-layout__header">
      <GlobalHeader />
    </a-layout-header>

    <a-layout-content class="basic-layout__content">
      <div class="basic-layout__content-inner">
        <RouterView />
      </div>
    </a-layout-content>

    <a-layout-footer class="basic-layout__footer">
      <GlobalFooter />
    </a-layout-footer>
  </a-layout>
</template>

<style scoped>
.basic-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background:
    radial-gradient(circle at top, rgba(22, 119, 255, 0.16), transparent 30%),
    linear-gradient(180deg, #f8fbff 0%, #eef5ff 48%, #ebf3ff 100%);
}

.basic-layout__header {
  position: sticky;
  top: 0;
  z-index: 100;
  flex-shrink: 0;
  height: 72px;
  padding-inline: clamp(16px, 4vw, 40px);
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(18px);
  border-bottom: 1px solid rgba(22, 119, 255, 0.12);
}

.basic-layout__content {
  flex: 1;
  width: 100%;
  padding: 28px 16px 40px;
}

.basic-layout__content-inner {
  width: 100%;
  max-width: 1280px;
  margin: 0 auto;
}

.basic-layout__footer {
  flex-shrink: 0;
  padding: 0;
  margin-top: auto;
  background: transparent;
}

@media (max-width: 768px) {
  .basic-layout__header {
    height: auto;
    padding-block: 12px;
  }
}
</style>
