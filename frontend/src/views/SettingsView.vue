<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()
const newUsername = ref('')
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const usernameMsg = ref('')
const usernameMsgType = ref('')
const pwdMsg = ref('')
const pwdMsgType = ref('')

async function submitUsername() {
  if (!newUsername.value.trim()) {
    usernameMsg.value = '用户名不能为空'
    usernameMsgType.value = 'error'
    return
  }
  try {
    const params = new URLSearchParams()
    params.append('type', 'username')
    params.append('newUsername', newUsername.value.trim())
    const res = await api.post('/settings', params)
    usernameMsg.value = res.data.msg || ''
    usernameMsgType.value = res.data.msg && res.data.msg.includes('成功') ? 'success' : 'error'
    if (usernameMsgType.value === 'success') newUsername.value = ''
  } catch {
    usernameMsg.value = '修改失败'
    usernameMsgType.value = 'error'
  }
}

async function submitPassword() {
  if (!oldPassword.value.trim()) {
    pwdMsg.value = '请输入旧密码'
    pwdMsgType.value = 'error'
    return
  }
  if (!newPassword.value.trim()) {
    pwdMsg.value = '新密码不能为空'
    pwdMsgType.value = 'error'
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    pwdMsg.value = '两次输入的新密码不一致'
    pwdMsgType.value = 'error'
    return
  }
  try {
    const params = new URLSearchParams()
    params.append('type', 'password')
    params.append('oldPassword', oldPassword.value)
    params.append('newPassword', newPassword.value)
    const res = await api.post('/settings', params)
    pwdMsg.value = res.data.msg || ''
    pwdMsgType.value = res.data.msg && res.data.msg.includes('成功') ? 'success' : 'error'
    if (pwdMsgType.value === 'success') {
      oldPassword.value = ''
      newPassword.value = ''
      confirmPassword.value = ''
    }
  } catch {
    pwdMsg.value = '修改失败'
    pwdMsgType.value = 'error'
  }
}
</script>

<template>
  <div class="page">
    <div class="container">
      <h1 class="ht">修改管理员账号</h1>
      <h3 class="ht"><a @click="router.push('/')" style="cursor:pointer">返回主页</a></h3>

      <!-- 修改用户名 -->
      <form @submit.prevent="submitUsername">
        <table class="tab">
          <tr class="ltr">
            <th colspan="2" style="background-color:powderblue;">修改用户名</th>
          </tr>
          <tr class="ltr">
            <td style="width:100px;">新用户名</td>
            <td><input v-model="newUsername" class="ipt" placeholder="请输入新用户名"></td>
          </tr>
          <tr class="ltr">
            <td colspan="2" class="buttonContainer">
              <button type="submit" class="btn1">修改用户名</button>
            </td>
          </tr>
          <tr class="ltr">
            <td colspan="2" style="text-align:center;">
              <span :class="usernameMsgType === 'error' ? 'msg' : 'ok'">{{ usernameMsg }}</span>
            </td>
          </tr>
        </table>
      </form>

      <br>

      <!-- 修改密码 -->
      <form @submit.prevent="submitPassword">
        <table class="tab">
          <tr class="ltr">
            <th colspan="2" style="background-color:powderblue;">修改密码</th>
          </tr>
          <tr class="ltr">
            <td style="width:100px;">旧密码</td>
            <td><input v-model="oldPassword" class="ipt" type="password" placeholder="请输入旧密码"></td>
          </tr>
          <tr class="ltr">
            <td>新密码</td>
            <td><input v-model="newPassword" class="ipt" type="password" placeholder="请输入新密码"></td>
          </tr>
          <tr class="ltr">
            <td>确认新密码</td>
            <td><input v-model="confirmPassword" class="ipt" type="password" placeholder="再次输入新密码"></td>
          </tr>
          <tr class="ltr">
            <td colspan="2" class="buttonContainer">
              <button type="submit" class="btn1">修改密码</button>
            </td>
          </tr>
          <tr class="ltr">
            <td colspan="2" style="text-align:center;">
              <span :class="pwdMsgType === 'error' ? 'msg' : 'ok'">{{ pwdMsg }}</span>
            </td>
          </tr>
        </table>
      </form>

      <br>
      <div class="buttonContainer">
        <button class="btn1" style="width:120px;" @click="router.push('/')">返回主页</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 24px;
}
.container {
  max-width: 560px; margin: 0 auto;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 32px;
}
.ht { text-align: center; color: cadetblue; }
.tab { width: 100%; border: 5px solid cadetblue; margin: 0 auto; border-radius: 5px; }
.ltr td { border: 1px solid powderblue; padding: 8px 12px; }
.ltr th { border: 1px solid powderblue; padding: 8px 12px; background-color: powderblue; color: #333; font-weight: bold; }
.ipt { border: 1px solid powderblue; width: 200px; padding: 4px 6px; }
.btn1 { border: 2px solid powderblue; border-radius: 4px; width: 100px; background-color: antiquewhite; cursor: pointer; padding: 4px 0; }
.buttonContainer { text-align: center; }
a { color: cadetblue; text-decoration: none; }
a:hover { text-decoration: underline; }
.msg { color: rgb(230,87,51); font-size: 13px; }
.ok { color: green; font-size: 13px; }
</style>