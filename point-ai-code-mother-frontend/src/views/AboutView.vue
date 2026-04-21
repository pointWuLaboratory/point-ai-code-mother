<template>
  <div class="about-view">
    <a-card class="about-view__card" :bordered="false">
      <h1>关于平台</h1>
      <p>
        一句话 · 呈所想 是一个通过 AI 对话快速生成网站应用的前端工作台，支持应用创建、持续对话生成、预览、部署和管理。
      </p>
    </a-card>
  </div>
</template>

<style scoped>
.about-view {
  padding-top: 16px;
}

.about-view__card {
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.86);
}

.about-view__card h1 {
  margin-top: 0;
}

.about-view__card p {
  margin-bottom: 0;
  color: #64748b;
  line-height: 1.9;
}
</style>
