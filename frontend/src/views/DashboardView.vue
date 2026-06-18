<script setup>
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()

function logout() {
  api.get('/login', { params: { action: 'logout' } })
  localStorage.removeItem('loggedIn')
  router.push('/login')
}
</script>

<template>
  <div class="dashboard">
    <div class="container">
      <div class="status-bar">
        <div class="proxy-status">
          <span class="status-dot"></span>
          <span>代理端口: 8080 | 运行中</span>
        </div>
      </div>

      <div class="page-header">
        <h1 class="page-title">WebTrackDB</h1>
        <p class="page-subtitle">网络监控系统 · 选择功能模块</p>
      </div>

      <div class="cards-grid">
        <div class="feature-card" @click="router.push('/history')">
          <div class="card-icon">&#x1F4CB;</div>
          <h3 class="card-title">访问历史</h3>
          <p class="card-desc">查看所有用户的浏览记录</p>
        </div>
        <div class="feature-card" @click="router.push('/blacklist')">
          <div class="card-icon">&#x1F6AB;</div>
          <h3 class="card-title">黑名单管理</h3>
          <p class="card-desc">添加/删除拦截规则</p>
        </div>
        <div class="feature-card" @click="router.push('/monitor')">
          <div class="card-icon">&#x1F4CA;</div>
          <h3 class="card-title">性能监控</h3>
          <p class="card-desc">CPU/内存/网络实时状态</p>
        </div>
        <div class="feature-card" @click="router.push('/settings')">
          <div class="card-icon">&#x2699;&#xFE0F;</div>
          <h3 class="card-title">账号设置</h3>
          <p class="card-desc">修改用户名/密码</p>
        </div>
      </div>

      <button class="logout-btn" @click="logout">&#x1F6AA; 退出登录</button>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 24px;
}
.container { max-width: 1200px; margin: 0 auto; }
.status-bar {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 14px 24px;
  margin-bottom: 40px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}
.proxy-status {
  display: flex; align-items: center; gap: 8px;
  font-size: 13px; color: #4b5563;
  background: #f3f4f6; padding: 6px 14px; border-radius: 30px;
}
.status-dot {
  width: 8px; height: 8px; background: #10b981;
  border-radius: 50%; animation: pulse 1.5s infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}
.page-header { text-align: center; margin-bottom: 48px; }
.page-title {
  font-size: 48px; font-weight: 700; color: #ffffff;
  margin-bottom: 12px; text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}
.page-subtitle { color: rgba(255, 255, 255, 0.85); font-size: 16px; }
.cards-grid {
  display: grid; grid-template-columns: repeat(2, 1fr);
  gap: 30px; margin-bottom: 40px;
}
.feature-card {
  background: white; border-radius: 24px; padding: 40px 24px;
  text-align: center; cursor: pointer; transition: all 0.3s ease;
  box-shadow: 0 20px 25px -12px rgba(0, 0, 0, 0.15);
}
.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 25px 35px -12px rgba(0, 0, 0, 0.25);
}
.card-icon { font-size: 56px; margin-bottom: 20px; }
.card-title { font-size: 22px; font-weight: 600; color: #1f2937; margin-bottom: 12px; }
.card-desc { font-size: 14px; color: #6b7280; line-height: 1.5; }
.logout-btn {
  display: block; margin: 0 auto;
  background: rgba(255, 255, 255, 0.95);
  border: none; padding: 12px 32px; border-radius: 40px;
  font-size: 15px; font-weight: 500; cursor: pointer;
  transition: all 0.2s; color: #dc2626; width: fit-content;
}
.logout-btn:hover {
  background: white; transform: translateY(-2px);
  box-shadow: 0 10px 20px -5px rgba(0, 0, 0, 0.15);
}
@media (max-width: 768px) {
  .cards-grid { grid-template-columns: 1fr; gap: 20px; }
  .page-title { font-size: 28px; }
  .feature-card { padding: 28px 20px; }
}
</style>