<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()
const url = ref('')
const list = ref([])
const msg = ref('')
const msgType = ref('')

async function loadData() {
  try {
    const res = await api.get('/blacklist')
    list.value = res.data.list || []
  } catch {
    list.value = []
  }
}

async function addUrl() {
  if (!url.value.trim()) {
    msg.value = 'URL 不能为空'
    msgType.value = 'error'
    return
  }
  if (!url.value.includes('.')) {
    msg.value = 'URL 格式无效，须包含 .'
    msgType.value = 'error'
    return
  }
  try {
    const params = new URLSearchParams()
    params.append('url', url.value.trim())
    const res = await api.post('/blacklist', params)
    list.value = res.data.list || []
    msg.value = res.data.msg || '添加成功'
    msgType.value = res.data.msg && res.data.msg.includes('成功') ? 'success' : 'error'
    url.value = ''
  } catch {
    msg.value = '添加失败'
    msgType.value = 'error'
  }
}

async function deleteItem(id) {
  if (!confirm('确定要删除此黑名单项？')) return
  try {
    const res = await api.get('/blacklist', { params: { delete: id } })
    list.value = res.data.list || []
    msg.value = res.data.msg || '删除成功'
    msgType.value = 'success'
  } catch {
    msg.value = '删除失败'
    msgType.value = 'error'
  }
}

function formatUrl(url) {
  return url.length > 60 ? url.slice(0, 60) + '...' : url
}

onMounted(loadData)
</script>

<template>
  <div class="page">
    <div class="container">
      <h1 class="ht">黑名单管理</h1>
      <h3 class="ht"><a @click="router.push('/')" style="cursor:pointer">返回主页</a></h3>

      <table class="tab">
        <tr class="ltr">
          <td style="width:80px;">添加 URL</td>
          <td>
            <input v-model="url" class="ipt" placeholder="输入要拦截的 URL..." @keyup.enter="addUrl">
            <span :class="msgType === 'error' ? 'msg' : 'ok'">{{ msg }}</span>
          </td>
          <td class="buttonContainer" style="width:80px;">
            <button class="btn1" @click="addUrl">添加</button>
          </td>
        </tr>
      </table>

      <br>

      <table class="tab">
        <tr class="ltr">
          <th>ID</th><th>URL</th><th>添加时间</th><th>操作</th>
        </tr>
        <tr v-if="list.length === 0" class="ltr">
          <td colspan="4" style="text-align:center; color:#999;">暂无黑名单记录</td>
        </tr>
        <tr v-for="row in list" :key="row.id" class="ltr">
          <td>{{ row.id }}</td>
          <td>{{ formatUrl(row.url) }}</td>
          <td>{{ row.addTime }}</td>
          <td><a @click="deleteItem(row.id)" style="cursor:pointer; color: #dc2626;">删除</a></td>
        </tr>
      </table>
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
  max-width: 900px; margin: 0 auto;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 32px;
}
.ht { text-align: center; color: cadetblue; }
.tab { width: 100%; border: 3px solid cadetblue; margin: 0 auto; border-radius: 5px; border-collapse: collapse; }
.ltr td { border: 1px solid powderblue; padding: 8px 12px; }
.ltr th { border: 1px solid powderblue; padding: 8px 12px; background-color: powderblue; color: #333; font-weight: bold; }
.ipt { border: 1px solid powderblue; width: 300px; padding: 4px 6px; }
.btn1 { border: 2px solid powderblue; border-radius: 4px; width: 80px; background-color: antiquewhite; cursor: pointer; padding: 4px 0; }
.buttonContainer { text-align: center; }
a { color: cadetblue; text-decoration: none; }
a:hover { text-decoration: underline; }
.msg { color: rgb(230,87,51); font-size: 13px; }
.ok { color: green; font-size: 13px; }
</style>