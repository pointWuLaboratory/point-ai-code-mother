<script setup lang="ts">
const highlights = [
  {
    title: '统一用户入口',
    description: '登录、注册、个人信息与用户管理被整合到一致的全局体验中。',
  },
  {
    title: '响应式页面布局',
    description: '顶部导航、内容区和底部版权在桌面与移动端都能保持清晰层次。',
  },
  {
    title: '基于接口联调',
    description: '已接入 UserController 的登录、注册、获取登录用户与分页列表接口。',
  },
]
</script>

<template>
  <section class="home-view">
    <div class="home-view__hero">
      <div class="home-view__badge">POINT AI CODE MOTHER</div>
      <h1>面向用户系统的一体化前端工作台</h1>
      <p>
        现在你可以通过右上角完成登录与注册，登录后进入个人中心查看自己的资料，并浏览分页用户列表。
      </p>
    </div>

    <div class="home-view__grid">
      <a-card v-for="item in highlights" :key="item.title" class="home-view__card" :bordered="false">
        <h3>{{ item.title }}</h3>
        <p>{{ item.description }}</p>
      </a-card>
    </div>
  </section>
</template>

<style scoped>
.home-view {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding-block: 12px;
}

.home-view__hero {
  padding: clamp(28px, 5vw, 48px);
  border: 1px solid rgba(22, 119, 255, 0.12);
  border-radius: 28px;
  background:
    radial-gradient(circle at right top, rgba(22, 119, 255, 0.18), transparent 28%),
    linear-gradient(145deg, rgba(255, 255, 255, 0.94), rgba(244, 248, 255, 0.92));
  box-shadow: 0 18px 50px rgba(46, 83, 143, 0.08);
}

.home-view__badge {
  display: inline-flex;
  padding: 6px 12px;
  color: #1677ff;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  background: rgba(22, 119, 255, 0.08);
  border-radius: 999px;
}

.home-view__hero h1 {
  margin: 18px 0 14px;
  color: #0f172a;
  font-size: clamp(32px, 5vw, 52px);
  line-height: 1.1;
}

.home-view__hero p {
  max-width: 720px;
  color: #475569;
  font-size: 16px;
  line-height: 1.8;
}

.home-view__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}

.home-view__card {
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.06);
}

.home-view__card :deep(.ant-card-body) {
  padding: 24px;
}

.home-view__card h3 {
  margin-bottom: 10px;
  color: #0f172a;
  font-size: 18px;
}

.home-view__card p {
  margin: 0;
  color: #64748b;
  line-height: 1.8;
}

@media (max-width: 900px) {
  .home-view__grid {
    grid-template-columns: 1fr;
  }
}
</style>
