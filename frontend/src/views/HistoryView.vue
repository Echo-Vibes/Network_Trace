<script setup>
import { ref, shallowRef, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()
const userName = ref('')
const urlKeyword = ref('')
const loading = ref(false)
const rows = shallowRef([])
const scrollPos = ref(0)

const ROW_H = 36
const VISIBLE = 30
const BUF = 10

const slice = computed(() => {
  const len = rows.value.length
  if (len === 0) return { items: [], top: 0, bot: 0 }
  const start = Math.max(0, Math.floor(scrollPos.value / ROW_H) - BUF)
  const end = Math.min(len, start + VISIBLE + BUF * 2)
  return {
    items: rows.value.slice(start, end),
    top: start * ROW_H,
    bot: Math.max(0, (len - end) * ROW_H)
  }
})

function onScroll(e) {
  scrollPos.value = e.target.scrollTop
}

function formatUrl(url) {
  return url.length > 60 ? url.slice(0, 60) + '...' : url
}

async function query() {
  loading.value = true
  scrollPos.value = 0
  try {
    const params = new URLSearchParams()
    if (userName.value) params.append('userName', userName.value)
    if (urlKeyword.value) params.append('urlKeyword', urlKeyword.value)
    const res = await api.post('/history', params)
    rows.value = Array.isArray(res.data) ? res.data.map(Object.freeze) : []
  } catch {
    rows.value = []
  } finally {
    loading.value = false
  }
}

onMounted(query)
</script>

<template>
  <div class="page">
    <div class="container">
      <h1 class="ht">访问历史</h1>
      <h3 class="ht"><a @click="router.push('/')" style="cursor:pointer">返回主页</a></h3>

      <table class="tab">
        <tr class="ltr">
          <td style="width:100px;">用户名</td>
          <td><input v-model="userName" class="ipt" placeholder="输入用户名筛选..." @keyup.enter="query"></td>
          <td style="width:100px;">URL 关键字</td>
          <td><input v-model="urlKeyword" class="ipt" placeholder="输入URL关键字..." @keyup.enter="query"></td>
          <td class="buttonContainer" style="width:80px;">
            <button type="button" class="btn1" @click="query" :disabled="loading">查询</button>
          </td>
        </tr>
      </table>

      <br>

      <div v-if="loading" class="empty-msg">加载中...</div>
      <div v-else-if="rows.length === 0" class="empty-msg">无访问记录</div>
      <div v-else class="scroll-box" @scroll="onScroll">
        <table class="tab fix">
          <thead>
            <tr class="ltr">
              <th class="c1">ID</th>
              <th class="c2">用户名</th>
              <th class="c3">URL</th>
              <th class="c4">访问时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="slice.top" class="spacer" :style="{ height: slice.top + 'px' }"><td colspan="4"></td></tr>
            <tr v-for="row in slice.items" :key="row.id" class="ltr" :style="{ height: ROW_H + 'px' }">
              <td>{{ row.id }}</td>
              <td>{{ row.userName }}</td>
              <td class="url-cell">{{ formatUrl(row.url) }}</td>
              <td>{{ row.visitTime }}</td>
            </tr>
            <tr v-if="slice.bot" class="spacer" :style="{ height: slice.bot + 'px' }"><td colspan="4"></td></tr>
          </tbody>
        </table>
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
  max-width: 960px; margin: 0 auto;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 32px;
}
.ht { text-align: center; color: cadetblue; }
.tab { width: 100%; border: 3px solid cadetblue; margin: 0 auto; border-radius: 5px; border-collapse: collapse; }
.fix { table-layout: fixed; }
.ltr td { border: 1px solid powderblue; padding: 4px 8px; }
.ltr th { border: 1px solid powderblue; padding: 8px 12px; background-color: powderblue; color: #333; font-weight: bold; position: sticky; top: 0; z-index: 1; }
.ipt { border: 1px solid powderblue; width: 200px; padding: 4px 6px; }
.btn1 { border: 2px solid powderblue; border-radius: 4px; width: 80px; background-color: antiquewhite; cursor: pointer; padding: 4px 0; }
.buttonContainer { text-align: center; }
a { color: cadetblue; text-decoration: none; }
a:hover { text-decoration: underline; }
td.url-cell { max-width: 320px; word-break: break-all; overflow: hidden; }

.scroll-box {
  max-height: 500px;
  overflow-y: auto;
  border-radius: 5px;
}
.spacer td {
  padding: 0 !important;
  border: none !important;
  line-height: 0 !important;
}
.c1 { width: 80px; }
.c2 { width: 120px; }
.c3 { width: auto; }
.c4 { width: 150px; }
.empty-msg {
  text-align: center; color: #999; padding: 40px 0;
}
</style>