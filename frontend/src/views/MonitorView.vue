<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()
const cpu = ref({ percent: -1 })
const memory = ref({ percent: -1, used: 0, total: 0 })
const network = ref([])
const statusMsg = ref('')
let timer = null

function cpuColor(pct) {
  if (pct < 50) return '#5cb85c'
  if (pct < 80) return '#f0ad4e'
  return '#d9534f'
}

function formatSize(bytes) {
  if (bytes >= 1073741824) return (bytes / 1073741824).toFixed(2) + ' GB'
  if (bytes >= 1048576) return (bytes / 1048576).toFixed(1) + ' MB'
  if (bytes >= 1024) return (bytes / 1024).toFixed(0) + ' KB'
  return bytes + ' B'
}

function formatBps(bps) {
  if (bps >= 1048576) return (bps / 1048576).toFixed(2) + ' MB/s'
  if (bps >= 1024) return (bps / 1024).toFixed(1) + ' KB/s'
  return bps + ' B/s'
}

async function fetchData() {
  try {
    const res = await api.get('/monitor')
    const data = res.data
    if (data.error) {
      statusMsg.value = data.error
      return
    }
    cpu.value = { percent: data.cpu?.percent ?? -1 }
    memory.value = {
      percent: data.memory?.percent ?? -1,
      used: data.memory?.used ?? 0,
      total: data.memory?.total ?? 0,
    }
    network.value = data.network || []
    statusMsg.value = ''
  } catch {
    statusMsg.value = '数据加载失败，正在重试...'
  }
}

onMounted(() => {
  fetchData()
  timer = setInterval(fetchData, 3000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <div class="page">
    <div class="container">
      <h1 class="ht">系统性能监控</h1>
      <h3 class="ht"><a @click="router.push('/')" style="cursor:pointer">返回主页</a></h3>

      <br>
      <table class="tab">
        <tr class="ltr"><th colspan="3">CPU & 内存</th></tr>
        <tr class="ltr">
          <td style="width:80px;"><span class="stat-label">CPU</span></td>
          <td>
            <span class="bar-bg">
              <span class="bar-fill" :style="{
                width: Math.min(cpu.percent, 100) + '%',
                background: cpu.percent >= 0 ? cpuColor(cpu.percent) : '#e0e0e0'
              }"></span>
            </span>
            <span class="stat-value">{{ cpu.percent >= 0 ? cpu.percent.toFixed(1) + ' %' : '-- %' }}</span>
          </td>
        </tr>
        <tr class="ltr">
          <td><span class="stat-label">内存</span></td>
          <td>
            <span class="bar-bg">
              <span class="bar-fill" :style="{
                width: Math.min(memory.percent, 100) + '%',
                background: memory.percent >= 0 ? cpuColor(memory.percent) : '#e0e0e0'
              }"></span>
            </span>
            <span class="stat-value">{{ memory.percent >= 0 ? memory.percent.toFixed(1) + ' %' : '-- %' }}</span>
          </td>
        </tr>
        <tr class="ltr">
          <td></td>
          <td style="font-size:12px;color:#666;">{{ formatSize(memory.used) }} / {{ formatSize(memory.total) }}</td>
        </tr>
      </table>

      <br>
      <table class="tab">
        <tr class="ltr">
          <th style="width:200px;">网络接口</th>
          <th style="width:150px;">下载速度</th>
          <th style="width:150px;">上传速度</th>
        </tr>
        <tr v-if="network.length === 0" class="ltr">
          <td colspan="3" class="nodata">未检测到活动网络接口</td>
        </tr>
        <tr v-for="net in network" :key="net.name" class="ltr if-row">
          <td class="if-name">{{ net.name }}</td>
          <td class="if-speed"><span class="if-arrow">↓</span> {{ formatBps(net.downloadBps) }}</td>
          <td class="if-speed"><span class="if-arrow">↑</span> {{ formatBps(net.uploadBps) }}</td>
        </tr>
      </table>

      <br>
      <div class="buttonContainer">
        <button class="btn1" @click="router.push('/')">返回主页</button>
      </div>
      <br>
      <h4 class="ht" style="color:#999;">{{ statusMsg }}</h4>
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
  max-width: 760px; margin: 0 auto;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 32px;
}
.ht { text-align: center; color: cadetblue; }
.tab { width: 100%; border: 3px solid cadetblue; margin: 0 auto; border-radius: 5px; border-collapse: collapse; }
.ltr td { border: 1px solid powderblue; padding: 8px 12px; }
.ltr th { border: 1px solid powderblue; padding: 8px 12px; background-color: powderblue; color: #333; font-weight: bold; }
.btn1 { border: 2px solid powderblue; border-radius: 4px; width: 120px; background-color: antiquewhite; cursor: pointer; padding: 4px 0; }
.buttonContainer { text-align: center; }
a { color: cadetblue; text-decoration: none; }
a:hover { text-decoration: underline; }
.bar-bg { background: #e0e0e0; border-radius: 4px; height: 20px; width: 200px; display: inline-block; vertical-align: middle; }
.bar-fill { display: inline-block; height: 20px; border-radius: 4px; transition: width 0.5s; }
.stat-label { width: 80px; display: inline-block; text-align: right; margin-right: 8px; font-weight: bold; }
.stat-value { display: inline-block; width: 70px; text-align: right; margin-left: 8px; font-family: Consolas, monospace; }
.if-row td { padding: 10px 12px; }
.if-name { font-weight: bold; font-size: 14px; }
.if-speed { font-family: Consolas, monospace; font-size: 13px; }
.if-arrow { color: cadetblue; }
.nodata { color: #999; text-align: center; }
</style>