<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { api } from './api.js'
import { CalendarDays, Camera, Heart, MessageCircle, Plus, Send, ShieldCheck, Star, Trash2, Trophy, Flag, BarChart3, Users } from 'lucide-vue-next'

const props = defineProps({ page: String, user: Object, token: String })
const emit = defineEmits(['login'])
const rows = ref([])
const points = ref({ total: 0, records: [], enrollments: [] })
const users = ref([])
const leaderboard = ref([])
const stats = ref({ activityCount: 0, createdCount: 0, competitionCount: 0, checkinCount: 0, sportBreakdown: [] })
const recommendations = ref([])
const participants = ref({})
const replies = ref({})
const activePost = ref(null)
const replyText = ref('')
const busy = ref(false)
const error = ref('')
const notice = ref('')
const editId = ref(null)
const competition = reactive({ title: '', sportType: '羽毛球', description: '', startTime: '', endTime: '', pointsReward: 10 })
const post = reactive({ postType: 'DYNAMIC', content: '', imageUrl: '' })
const canManage = computed(() => ['ADMIN', 'EVENT_MANAGER'].includes(props.user?.role))
const enrolled = computed(() => new Map(points.value.enrollments.map(row => [Number(row.competitionId), row.status])))
function requireLogin() { if (props.token) return true; emit('login'); return false }
function date(value) { return value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '' }
function local(value) { return value ? String(value).replace(' ', 'T').slice(0, 16) : '' }
async function load() {
  error.value = ''
  try {
    if (props.page === 'competitions') {
      rows.value = await api.get('/competitions')
      if (props.token) points.value = await api.get('/points/mine')
    } else if (props.page === 'community') rows.value = await api.get('/community/posts')
    else if (props.page === 'points') {
      const result = await Promise.all([api.get('/points/mine'), api.get('/points/leaderboard'), api.get('/stats/mine'), api.get('/recommendations')])
      points.value = result[0]; leaderboard.value = result[1] || []; stats.value = result[2] || stats.value; recommendations.value = result[3] || []
    }
    else if (props.page === 'admin-users') users.value = await api.get('/admin/users')
  } catch (e) { error.value = e.message }
}
async function run(action, success = '操作成功') {
  if (busy.value) return
  busy.value = true; error.value = ''; notice.value = ''
  try { await action(); notice.value = success; await load() }
  catch (e) { error.value = e.message }
  finally { busy.value = false }
}
function resetCompetition() { editId.value = null; Object.assign(competition, { title: '', sportType: '羽毛球', description: '', startTime: '', endTime: '', pointsReward: 10 }) }
function editCompetition(row) { editId.value = row.id; Object.assign(competition, { title: row.title, sportType: row.sportType, description: row.description || '', startTime: local(row.startTime), endTime: local(row.endTime), pointsReward: row.pointsReward }); window.scrollTo({ top: 0, behavior: 'smooth' }) }
async function loadParticipants(row) { try { participants.value[row.id] = await api.get(`/competitions/${row.id}/participants`) } catch (e) { error.value = e.message } }
function approve(row, participant, status) { run(async () => { await api.put(`/competitions/${row.id}/participants/${participant.id}/approval?status=${status}`, {}) ; await loadParticipants(row) }, '审核状态已更新') }
function saveResult(row, participant, event) { const form = event.target; run(async () => { await api.put(`/competitions/${row.id}/participants/${participant.id}/result`, { rank: Number(form.rank.value), score: Number(form.score.value) }); await loadParticipants(row) }, '成绩已保存') }
function saveCompetition() {
  if (new Date(competition.endTime) <= new Date(competition.startTime)) { error.value = '结束时间必须晚于开始时间'; return }
  const data = { ...competition, startTime: local(competition.startTime), endTime: local(competition.endTime), pointsReward: Number(competition.pointsReward) }
  run(async () => { if (editId.value) await api.put(`/competitions/${editId.value}`, data); else await api.post('/competitions', data); resetCompetition() }, '赛事已保存')
}
function readImage(event, callback) {
  const file = event.target.files?.[0]
  if (!file) return
  if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type) || file.size > 2 * 1024 * 1024) { error.value = '请选择不超过 2MB 的 JPG、PNG 或 WebP 照片'; return }
  const reader = new FileReader()
  reader.onload = () => callback(String(reader.result))
  reader.readAsDataURL(file)
}
function checkin(event, row) { readImage(event, imageUrl => run(() => api.post(`/competitions/${row.id}/checkin`, { imageUrl }), '打卡成功，积分已入账')) }
function savePost() {
  if (!requireLogin() || !post.content.trim()) return
  run(async () => { await api.post('/community/posts', { ...post, content: post.content.trim() }); Object.assign(post, { postType: 'DYNAMIC', content: '', imageUrl: '' }) }, '发布成功')
}
async function openReplies(row) {
  activePost.value = activePost.value === row.id ? null : row.id
  if (activePost.value) { try { replies.value[row.id] = await api.get(`/community/posts/${row.id}/replies`) } catch (e) { error.value = e.message } }
}
function sendReply(row) {
  if (!requireLogin() || !replyText.value.trim()) return
  run(async () => { await api.post(`/community/posts/${row.id}/replies`, { content: replyText.value.trim() }); replyText.value = ''; replies.value[row.id] = await api.get(`/community/posts/${row.id}/replies`) }, '回复成功')
}
function togglePost(row, type) {
  if (!requireLogin()) return
  run(() => api.post(`/community/posts/${row.id}/${type}`, {}), type === 'like' ? '点赞状态已更新' : '收藏状态已更新')
}
function reportPost(row) {
  if (!requireLogin()) return
  const reason = window.prompt('请输入举报原因', '不友善或与运动无关')
  if (reason?.trim()) run(() => api.post(`/community/posts/${row.id}/report`, { reason: reason.trim() }), '举报已提交')
}
onMounted(load)
</script>

<template>
  <div class="page-head"><div><p class="eyebrow">CAMPUS SPORTS</p><h1>{{ { competitions: '校园赛事', community: '运动社区', points: '我的积分', 'admin-users': '角色管理' }[page] }}</h1></div></div>
  <p v-if="error" class="xp-alert">{{ error }}</p><p v-if="notice" class="xp-success">{{ notice }}</p>

  <template v-if="page === 'competitions'">
    <form v-if="canManage" class="xp-form" @submit.prevent="saveCompetition">
      <div class="xp-title"><Trophy :size="20" /><h2>{{ editId ? '编辑赛事' : '发布赛事' }}</h2></div>
      <div class="xp-fields"><label>赛事名称<input v-model="competition.title" required maxlength="100" /></label><label>运动类型<input v-model="competition.sportType" required maxlength="50" /></label><label>开始时间<input v-model="competition.startTime" type="datetime-local" required /></label><label>结束时间<input v-model="competition.endTime" type="datetime-local" required /></label><label>打卡积分<input v-model.number="competition.pointsReward" type="number" min="1" max="100" required /></label></div>
      <label>赛事说明<textarea v-model="competition.description" rows="2" maxlength="1000" /></label>
      <div class="xp-actions"><button class="primary-button" :disabled="busy"><Plus :size="16" />{{ editId ? '保存修改' : '发布赛事' }}</button><button v-if="editId" type="button" class="outline-button" @click="resetCompetition">取消编辑</button></div>
    </form>
    <div v-if="!rows.length" class="state-panel">暂无赛事</div>
    <div v-else class="xp-list"><article v-for="row in rows" :key="row.id" class="xp-item"><div class="xp-main"><div class="xp-meta"><span class="small-tag">{{ row.sportType }}</span><span class="small-tag muted-tag">{{ row.status === 'OPEN' ? '进行中' : '已取消' }}</span><span>+{{ row.pointsReward }} 积分</span></div><h2>{{ row.title }}</h2><p>{{ row.description }}</p><div class="xp-meta"><CalendarDays :size="15" />{{ date(row.startTime) }} 至 {{ date(row.endTime) }} · {{ row.managerName }} · 已报名 {{ row.enrolledCount }} 人</div><div v-if="canManage && (user?.role === 'ADMIN' || row.managerId === user?.id)" class="xp-actions"><button class="text-button" @click="loadParticipants(row)"><Users :size="16" />报名管理</button></div><div v-if="participants[row.id]" class="xp-participants"><div v-for="participant in participants[row.id]" :key="participant.id" class="xp-participant"><span><strong>{{ participant.nickname }}</strong> <small>{{ participant.approvalStatus }}</small></span><span class="xp-actions"><button class="text-button" @click="approve(row, participant, 'APPROVED')">通过</button><button class="text-button" @click="approve(row, participant, 'REJECTED')">拒绝</button><form @submit.prevent="saveResult(row, participant, $event)"><input name="rank" type="number" min="1" placeholder="名次" /><input name="score" type="number" min="0" step="0.01" placeholder="成绩" /><button class="text-button">保存成绩</button></form></span></div></div></div><div class="xp-actions"><button v-if="row.status === 'OPEN' && token && !enrolled.get(row.id)" class="primary-button" :disabled="busy" @click="run(() => api.post(`/competitions/${row.id}/enroll`, {}), '报名成功')">报名</button><button v-else-if="row.status === 'OPEN' && token && enrolled.get(row.id) === 'ENROLLED'" class="outline-button" :disabled="busy" @click="run(() => api.delete(`/competitions/${row.id}/enroll`), '已取消报名')">取消报名</button><button v-else-if="row.status === 'OPEN' && !token" class="outline-button" @click="emit('login')">登录报名</button><span v-if="enrolled.get(row.id) === 'CHECKED'" class="small-tag">已打卡</span><label v-if="enrolled.get(row.id) === 'ENROLLED' && row.status === 'OPEN'" class="xp-upload"><Camera :size="17" />拍照打卡<input type="file" accept="image/jpeg,image/png,image/webp" @change="checkin($event, row)" /></label><button v-if="row.status === 'OPEN' && (user?.role === 'ADMIN' || row.managerId === user?.id)" class="icon-button" title="编辑赛事" @click="editCompetition(row)"><ShieldCheck :size="17" /></button><button v-if="row.status === 'OPEN' && (user?.role === 'ADMIN' || row.managerId === user?.id)" class="icon-button danger-text" title="取消赛事" @click="run(() => api.delete(`/competitions/${row.id}`), '赛事已取消')"><Trash2 :size="17" /></button></div></article></div>
  </template>

  <template v-else-if="page === 'community'">
    <form v-if="token" class="xp-form" @submit.prevent="savePost"><div class="xp-title"><MessageCircle :size="20" /><h2>分享运动时刻</h2></div><div class="xp-actions"><label><input v-model="post.postType" type="radio" value="DYNAMIC" />运动动态</label><label><input v-model="post.postType" type="radio" value="TOPIC" />论坛话题</label></div><textarea v-model="post.content" required maxlength="1000" rows="3" placeholder="记录今天的运动，或发起一个话题" /><div class="xp-actions"><label class="xp-upload"><Camera :size="17" />添加照片<input type="file" accept="image/jpeg,image/png,image/webp" @change="readImage($event, value => post.imageUrl = value)" /></label><span v-if="post.imageUrl">已选照片</span><button class="primary-button" :disabled="busy"><Send :size="16" />发布</button></div><img v-if="post.imageUrl" class="xp-preview" :src="post.imageUrl" alt="照片预览" /></form>
    <button v-else class="outline-button" @click="emit('login')">登录后分享动态</button>
    <div v-if="!rows.length" class="state-panel">暂无社区内容</div><div v-else class="xp-list"><article v-for="row in rows" :key="row.id" class="xp-item"><div class="xp-main"><div class="xp-meta"><span class="small-tag">{{ row.postType === 'TOPIC' ? '论坛话题' : '运动动态' }}</span>{{ row.nickname }} · {{ date(row.createdAt) }}</div><p class="xp-content">{{ row.content }}</p><img v-if="row.imageUrl" class="xp-post-image" :src="row.imageUrl" alt="动态照片" /><div class="xp-actions"><button class="text-button" @click="togglePost(row, 'like')"><Heart :size="16" />{{ row.likeCount || 0 }}</button><button class="text-button" @click="togglePost(row, 'favorite')"><Star :size="16" />{{ row.favoriteCount || 0 }}</button><button class="text-button" @click="openReplies(row)"><MessageCircle :size="16" />{{ row.replyCount }} 条回复</button><button v-if="token && user?.id !== row.userId" class="icon-button" title="举报动态" @click="reportPost(row)"><Flag :size="16" /></button><button v-if="user?.id === row.userId || user?.role === 'ADMIN'" class="icon-button danger-text" title="删除动态" @click="run(() => api.delete(`/community/posts/${row.id}`), '动态已删除')"><Trash2 :size="16" /></button></div><div v-if="activePost === row.id" class="xp-replies"><div v-for="reply in replies[row.id] || []" :key="reply.id" class="xp-reply"><strong>{{ reply.nickname }}</strong> {{ reply.content }} <small>{{ date(reply.createdAt) }}</small><button v-if="user?.id === reply.userId || user?.role === 'ADMIN'" class="icon-button danger-text" title="删除回复" @click="run(async () => { await api.delete(`/community/replies/${reply.id}`); replies[row.id] = await api.get(`/api/community/posts/${row.id}/replies`) }, '回复已删除')"><Trash2 :size="14" /></button></div><form v-if="token" class="xp-actions" @submit.prevent="sendReply(row)"><input v-model="replyText" required maxlength="500" placeholder="输入回复" /><button class="primary-button" :disabled="busy"><Send :size="16" />回复</button></form><button v-else class="text-button" @click="emit('login')">登录后回复</button></div></div></article></div>
  </template>

  <template v-else-if="page === 'points'"><div class="xp-total"><Trophy :size="25" /><span>累计积分</span><strong>{{ points.total }}</strong></div><div class="xp-stat-grid"><div><BarChart3 :size="19" /><strong>{{ stats.activityCount }}</strong><span>参加活动</span></div><div><Trophy :size="19" /><strong>{{ stats.competitionCount }}</strong><span>赛事报名</span></div><div><Camera :size="19" /><strong>{{ stats.checkinCount }}</strong><span>照片打卡</span></div><div><Plus :size="19" /><strong>{{ stats.createdCount }}</strong><span>发布活动</span></div></div><div v-if="points.badges?.length" class="xp-badges"><h2 class="xp-section">我的勋章</h2><div class="xp-badge-grid"><div v-for="badge in points.badges" :key="badge.name"><span>{{ badge.icon }}</span><strong>{{ badge.name }}</strong><small>{{ badge.description }}</small></div></div></div><h2 class="xp-section">积分排行榜</h2><div class="xp-list"><div v-for="(member,index) in leaderboard" :key="member.id" class="xp-item xp-record"><div><strong>#{{ index + 1 }} {{ member.nickname }}</strong><small>{{ member.checkinCount }} 次打卡</small></div><b>{{ member.totalPoints }} 分</b></div></div><h2 class="xp-section">积分记录</h2><div v-if="!points.records.length" class="state-panel">参加赛事并拍照打卡后，积分会显示在这里</div><div v-else class="xp-list"><div v-for="record in points.records" :key="record.id" class="xp-item xp-record"><div><strong>{{ record.reason }}</strong><small>{{ date(record.createdAt) }}</small></div><b>+{{ record.points }}</b></div></div><h2 v-if="recommendations.length" class="xp-section">为你推荐的活动</h2><div v-if="recommendations.length" class="xp-list"><div v-for="item in recommendations" :key="item.id" class="xp-item xp-record"><div><strong>{{ item.title }}</strong><small>{{ item.sportType }} · {{ date(item.startTime) }}</small></div><a class="text-button" :href="`/activities/${item.id}`">查看</a></div></div></template>

  <template v-else-if="page === 'admin-users'"><div class="xp-list"><div v-for="member in users" :key="member.id" class="xp-item xp-record"><div><strong>{{ member.nickname }}</strong><small>{{ member.username }}</small></div><span v-if="member.role === 'ADMIN'">系统管理员</span><select v-else :value="member.role" :disabled="busy" @change="run(() => api.put(`/admin/users/${member.id}/role`, { role: $event.target.value }), '角色已更新')"><option value="STUDENT">普通用户</option><option value="EVENT_MANAGER">赛事管理员</option></select></div></div></template>
</template>
