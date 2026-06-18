<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()
const username = ref('')
const password = ref('')
const msg = ref('')
const msgType = ref('')

async function submitLogin() {
  if (!username.value.trim()) {
    msg.value = '请输入账号'
    msgType.value = 'error'
    return
  }
  if (!password.value.trim()) {
    msg.value = '请输入密码'
    msgType.value = 'error'
    return
  }
  try {
    const params = new URLSearchParams()
    params.append('username', username.value)
    params.append('password', password.value)
    const res = await api.post('/login', params)
    if (res.data.success) {
      localStorage.setItem('loggedIn', 'true')
      msg.value = '登录成功，正在跳转...'
      msgType.value = 'success'
      setTimeout(() => router.push('/'), 500)
    } else {
      msg.value = res.data.msg || '账号或密码错误'
      msgType.value = 'error'
    }
  } catch {
    msg.value = '登录失败，请重试'
    msgType.value = 'error'
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card">
        <div class="logo-area">
          <div class="logo-icon">&#x1F310;</div>
          <div class="logo-title">WebTrackDB</div>
          <div class="logo-subtitle">网络监控系统 · 管理员登录</div>
        </div>

        <form @submit.prevent="submitLogin">
          <div class="form-group">
            <label class="form-label">账号</label>
            <input v-model="username" class="form-input" placeholder="请输入账号" autocomplete="off">
          </div>
          <div class="form-group">
            <label class="form-label">密码</label>
            <input v-model="password" class="form-input" type="password" placeholder="请输入密码">
          </div>
          <button type="submit" class="login-btn">登 录</button>
          <div v-if="msg" :class="['message', msgType === 'error' ? 'message-error' : 'message-success']">
            {{ msg }}
          </div>
        </form>

        <div class="footer-links">WebTrackDB | 网络行为监控系统</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.login-container { width: 100%; max-width: 440px; }
.login-card {
  background: rgba(255, 255, 255, 0.98);
  border-radius: 24px;
  padding: 40px 32px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  backdrop-filter: blur(10px);
  transition: transform 0.3s ease;
}
.login-card:hover { transform: translateY(-5px); }
.logo-area { text-align: center; margin-bottom: 32px; }
.logo-icon {
  width: 70px; height: 70px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  display: inline-flex;
  align-items: center; justify-content: center;
  font-size: 36px;
  box-shadow: 0 10px 25px -5px rgba(102, 126, 234, 0.4);
}
.logo-title { font-size: 32px; font-weight: 700; margin-top: 16px; color: #1f2937; }
.logo-subtitle { font-size: 14px; color: #6b7280; margin-top: 6px; }
.form-group { margin-bottom: 24px; }
.form-label { display: block; font-size: 14px; font-weight: 500; color: #374151; margin-bottom: 8px; }
.form-input {
  width: 100%; padding: 12px 16px; font-size: 15px;
  border: 1px solid #e5e7eb; border-radius: 12px;
  transition: all 0.2s; outline: none; background: #f9fafb;
}
.form-input:focus {
  border-color: #667eea; background: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}
.login-btn {
  width: 100%; padding: 12px; font-size: 16px; font-weight: 600;
  color: white;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none; border-radius: 12px; cursor: pointer;
  transition: all 0.2s; margin-top: 8px;
}
.login-btn:hover { transform: translateY(-2px); box-shadow: 0 10px 25px -5px rgba(102, 126, 234, 0.4); }
.login-btn:active { transform: translateY(0); }
.message {
  margin-top: 20px; padding: 12px; border-radius: 12px;
  font-size: 13px; text-align: center;
}
.message-error { background: #fee2e2; color: #dc2626; border-left: 3px solid #dc2626; }
.message-success { background: #dcfce7; color: #16a34a; border-left: 3px solid #16a34a; }
.footer-links { text-align: center; margin-top: 24px; font-size: 13px; color: #9ca3af; }
@media (max-width: 480px) {
  .login-card { padding: 32px 24px; }
  .logo-title { font-size: 24px; }
}
</style>