<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { api } from './api.js'
import { ArrowUpRight, CalendarDays, Camera, Heart, MessageCircle, Plus, Send, ShieldCheck, Star, Trash2, Trophy, Flag, BarChart3, Users } from 'lucide-vue-next'

const props = defineProps({ page: String, user: Object, token: String })
const emit = defineEmits(['login'])
const rows = ref([])
const competitionFormOpen = ref(false)
const competitionFilter = ref('active')
const communityFilter = ref('all')
const now = ref(Date.now())
let clock
const points = ref({ total: 0, records: [], enrollments: [] })
const users = ref([])
const leaderboard = ref([])
const stats = ref({ activityCount: 0, createdCount: 0, competitionCount: 0, checkinCount: 0, sportBreakdown: [] })
const checkinData = ref({ today: [], records: [], streak: 0, total: 0, rewards: [], goals: [], minDurationMinutes: 20 })
const rules = ref([])
const checkinPolicy = reactive({ minDurationMinutes: 20 })
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
const checkinForm = reactive({ sportType: '羽毛球', durationMinutes: 30, distanceKm: 0, imageUrl: '', note: '' })
const rule = reactive({ id: null, name: '', ruleType: 'STREAK', targetValue: 3, rewardPoints: 10, badgeName: '' })
const canManage = computed(() => ['ADMIN', 'EVENT_MANAGER'].includes(props.user?.role))
const hasDistance = computed(() => /跑步|慢跑|散步|步行|健走|骑行|自行车|徒步/.test(checkinForm.sportType))
const enrolled = computed(() => new Map(points.value.enrollments.map(row => [Number(row.competitionId), row.status])))
const enrollmentApproval = computed(() => new Map(points.value.enrollments.map(row => [Number(row.competitionId), row.approvalStatus])))
function competitionState(row) {
  if (row.status !== 'OPEN') return 'cancelled'
  if (now.value < new Date(row.startTime).getTime()) return 'upcoming'
  if (now.value > new Date(row.endTime).getTime()) return 'ended'
  return 'live'
}
const visibleCompetitions = computed(() => rows.value.filter(row => {
  const state = competitionState(row)
  return competitionFilter.value === 'all' || (competitionFilter.value === 'active' ? state === 'upcoming' || state === 'live' : competitionFilter.value === state)
}).sort((a, b) => {
  const priority = { live: 0, upcoming: 1, ended: 2, cancelled: 3 }
  const difference = priority[competitionState(a)] - priority[competitionState(b)]
  return difference || (competitionState(a) === 'upcoming' ? new Date(a.startTime) - new Date(b.startTime) : new Date(b.startTime) - new Date(a.startTime))
}))
const visiblePosts = computed(() => rows.value.filter(row => communityFilter.value === 'all' || (communityFilter.value === 'mine' ? Number(row.userId) === Number(props.user?.id) : row.postType === communityFilter.value)))
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
    else if (props.page === 'checkins') checkinData.value = await api.get('/checkins/mine')
    else if (props.page === 'admin-reward-rules') {
      const [currentRules, policy] = await Promise.all([api.get('/admin/reward-rules'), api.get('/admin/checkin-policy')])
      rules.value = currentRules
      Object.assign(checkinPolicy, policy)
    }
  } catch (e) { error.value = e.message }
}
async function run(action, success = '操作成功') {
  if (busy.value) return
  busy.value = true; error.value = ''; notice.value = ''
  try { await action(); notice.value = success; await load() }
  catch (e) { error.value = e.message }
  finally { busy.value = false }
}
function resetCompetition() { editId.value = null; competitionFormOpen.value = false; Object.assign(competition, { title: '', sportType: '羽毛球', description: '', startTime: '', endTime: '', pointsReward: 10 }) }
function editCompetition(row) { editId.value = row.id; competitionFormOpen.value = true; Object.assign(competition, { title: row.title, sportType: row.sportType, description: row.description || '', startTime: local(row.startTime), endTime: local(row.endTime), pointsReward: row.pointsReward }); window.scrollTo({ top: 0, behavior: 'smooth' }) }
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
function saveCheckin() { run(() => api.post('/checkins', { ...checkinForm, durationMinutes: Number(checkinForm.durationMinutes), distanceKm: hasDistance.value ? Number(checkinForm.distanceKm) : 0 }), '今日运动打卡成功，奖励进度已更新') }
function chooseCheckinImage(event) { readImage(event, value => checkinForm.imageUrl = value) }
async function deleteReply(row, reply) {
  await run(() => api.delete(`/community/replies/${reply.id}`), '回复已删除')
  if (!error.value) replies.value[row.id] = await api.get(`/community/posts/${row.id}/replies`)
}
function saveCheckinPolicy() { run(() => api.put('/admin/checkin-policy', { minDurationMinutes: Number(checkinPolicy.minDurationMinutes) }), '有效打卡条件已保存') }
function ruleTypeChanged() { if (rule.ruleType === 'DAILY') rule.targetValue = 1 }
function saveRule() { const data = { ...rule, targetValue: Number(rule.targetValue), rewardPoints: Number(rule.rewardPoints) }; run(async () => { if (rule.id) await api.put(`/admin/reward-rules/${rule.id}`, data); else await api.post('/admin/reward-rules', data); Object.assign(rule, { id: null, name: '', ruleType: 'STREAK', targetValue: 3, rewardPoints: 10, badgeName: '' }) }, '奖励规则已保存') }
function resetRule() { Object.assign(rule, { id: null, name: '', ruleType: 'STREAK', targetValue: 3, rewardPoints: 10, badgeName: '' }) }
function editRule(item) { Object.assign(rule, item) }
function refreshOnFocus() { now.value = Date.now(); if (['points', 'checkins', 'competitions', 'community'].includes(props.page)) load() }
watch(() => props.user?.avatar, () => { if (props.page === 'community') load() })
onMounted(() => { load(); clock = window.setInterval(() => now.value = Date.now(), 30000); window.addEventListener('focus', refreshOnFocus) })
onUnmounted(() => { window.clearInterval(clock); window.removeEventListener('focus', refreshOnFocus) })
</script>

<template>
  <div class="page-head"><div><p class="eyebrow">CAMPUS SPORTS</p><h1>{{ { competitions: '校园赛事', community: '运动社区', points: '我的积分', checkins: '每日运动打卡', 'admin-reward-rules': '打卡奖励规则', 'admin-users': '角色管理' }[page] }}</h1></div></div>
  <p v-if="error" class="xp-alert">{{ error }}</p><p v-if="notice" class="xp-success">{{ notice }}</p>

  <template v-if="page === 'competitions'">
    <div class="xp-competition-toolbar">
      <div class="xp-competition-tabs" role="group" aria-label="赛事状态">
        <button v-for="option in [{ value: 'active', label: '可参与' }, { value: 'all', label: '全部' }, { value: 'upcoming', label: '未开始' }, { value: 'live', label: '进行中' }, { value: 'ended', label: '已结束' }]" :key="option.value" type="button" :class="{ selected: competitionFilter === option.value }" @click="competitionFilter = option.value">{{ option.label }}</button>
      </div>
      <button v-if="canManage" type="button" class="primary-button" @click="competitionFormOpen ? resetCompetition() : competitionFormOpen = true"><Plus :size="16" />{{ competitionFormOpen ? '收起表单' : '发布赛事' }}</button>
    </div>
    <form v-if="canManage && competitionFormOpen" class="xp-form" @submit.prevent="saveCompetition">
      <div class="xp-title"><Trophy :size="20" /><h2>{{ editId ? '编辑赛事' : '发布赛事' }}</h2></div>
      <div class="xp-fields"><label>赛事名称<input v-model="competition.title" required maxlength="100" /></label><label>运动类型<input v-model="competition.sportType" required maxlength="50" /></label><label>开始时间<input v-model="competition.startTime" type="datetime-local" required /></label><label>结束时间<input v-model="competition.endTime" type="datetime-local" required /></label><label>打卡积分<input v-model.number="competition.pointsReward" type="number" min="1" max="100" required /></label></div>
      <label>赛事说明<textarea v-model="competition.description" rows="2" maxlength="1000" /></label>
      <div class="xp-actions"><button class="primary-button" :disabled="busy"><Plus :size="16" />{{ editId ? '保存修改' : '发布赛事' }}</button><button v-if="editId" type="button" class="outline-button" @click="resetCompetition">取消编辑</button></div>
    </form>
    <div v-if="!visibleCompetitions.length" class="xp-competition-empty">{{ competitionFilter === 'active' ? '暂无可参与的赛事' : '当前分类暂无赛事' }}</div>
    <div v-else class="xp-competition-list">
      <article v-for="row in visibleCompetitions" :key="row.id" class="xp-competition-row">
        <div class="xp-competition-body">
          <div class="xp-competition-tags"><span class="small-tag">{{ row.sportType }}</span><span class="small-tag" :class="{ 'muted-tag': competitionState(row) === 'ended' || competitionState(row) === 'cancelled' }">{{ { upcoming: '未开始', live: '进行中', ended: '已结束', cancelled: '已取消' }[competitionState(row)] }}</span><span>+{{ row.pointsReward }} 积分</span></div>
          <h2>{{ row.title }}</h2><p v-if="row.description">{{ row.description }}</p>
          <div class="xp-competition-date"><CalendarDays :size="16" />{{ date(row.startTime) }} 至 {{ date(row.endTime) }}</div>
          <div class="xp-competition-detail">{{ row.managerName }} · 已报名 {{ row.enrolledCount }} 人</div>
          <div v-if="canManage && (user?.role === 'ADMIN' || row.managerId === user?.id)" class="xp-competition-manage"><button class="text-button" @click="loadParticipants(row)"><Users :size="16" />报名管理</button></div>
          <div v-if="participants[row.id]" class="xp-participants"><div v-for="participant in participants[row.id]" :key="participant.id" class="xp-participant"><span><strong>{{ participant.nickname }}</strong> <small>{{ participant.approvalStatus }}</small></span><span class="xp-actions"><button class="text-button" @click="approve(row, participant, 'APPROVED')">通过</button><button class="text-button" @click="approve(row, participant, 'REJECTED')">拒绝</button><form @submit.prevent="saveResult(row, participant, $event)"><input name="rank" type="number" min="1" placeholder="名次" /><input name="score" type="number" min="0" step="0.01" placeholder="成绩" /><button class="text-button">保存成绩</button></form></span></div></div>
        </div>
        <div class="xp-competition-controls">
          <button v-if="competitionState(row) === 'upcoming' && token && !enrolled.get(row.id)" class="primary-button" :disabled="busy" @click="run(() => api.post(`/competitions/${row.id}/enroll`, {}), '报名成功')">报名</button>
          <button v-else-if="competitionState(row) === 'upcoming' && token && enrolled.get(row.id) === 'ENROLLED'" class="outline-button" :disabled="busy" @click="run(() => api.delete(`/competitions/${row.id}/enroll`), '已取消报名')">取消报名</button>
          <button v-else-if="competitionState(row) === 'upcoming' && !token" class="outline-button" @click="emit('login')">登录报名</button>
          <span v-if="enrolled.get(row.id) === 'CHECKED'" class="small-tag">已打卡</span>
          <label v-else-if="competitionState(row) === 'live' && enrolled.get(row.id) === 'ENROLLED' && enrollmentApproval.get(row.id) !== 'REJECTED'" class="xp-upload"><Camera :size="17" />拍照打卡<input type="file" accept="image/jpeg,image/png,image/webp" @change="checkin($event, row)" /></label>
          <span v-if="competitionState(row) === 'ended' && enrolled.get(row.id) !== 'CHECKED'" class="xp-competition-hint">已结束，无法打卡</span>
          <span v-else-if="competitionState(row) === 'live' && enrollmentApproval.get(row.id) === 'REJECTED'" class="xp-competition-hint">报名未通过，无法打卡</span>
          <span v-else-if="competitionState(row) === 'live' && !enrolled.get(row.id)" class="xp-competition-hint">报名已截止</span>
          <button v-if="row.status === 'OPEN' && (user?.role === 'ADMIN' || row.managerId === user?.id)" class="icon-button" title="编辑赛事" @click="editCompetition(row)"><ShieldCheck :size="17" /></button>
          <button v-if="row.status === 'OPEN' && (user?.role === 'ADMIN' || row.managerId === user?.id)" class="icon-button danger-text" title="取消赛事" @click="run(() => api.delete(`/competitions/${row.id}`), '赛事已取消')"><Trash2 :size="17" /></button>
        </div>
      </article>
    </div>
  </template>

  <template v-else-if="page === 'community'">
    <form v-if="token" class="xp-community-composer" @submit.prevent="savePost">
      <div class="xp-community-author"><span class="xp-community-avatar"><img v-if="user?.avatar" :src="user.avatar" alt="" /><span v-else>{{ user?.nickname?.slice(0, 1) || '我' }}</span></span><strong>{{ user?.nickname || '我' }}</strong></div>
      <div class="xp-community-modes" role="group" aria-label="发布类型"><button type="button" :class="{ selected: post.postType === 'DYNAMIC' }" @click="post.postType = 'DYNAMIC'">运动动态</button><button type="button" :class="{ selected: post.postType === 'TOPIC' }" @click="post.postType = 'TOPIC'">论坛话题</button></div>
      <textarea v-model="post.content" required maxlength="1000" rows="3" :placeholder="post.postType === 'TOPIC' ? '你想和大家讨论什么运动话题？' : '今天有什么运动新鲜事？'" />
      <div v-if="post.imageUrl" class="xp-community-preview"><img :src="post.imageUrl" alt="待发布照片" /><button type="button" class="icon-button" title="移除照片" @click="post.imageUrl = ''"><Trash2 :size="16" /></button></div>
      <div class="xp-community-compose-actions"><label class="xp-upload"><Camera :size="17" />{{ post.imageUrl ? '更换照片' : '添加照片' }}<input type="file" accept="image/jpeg,image/png,image/webp" @change="readImage($event, value => post.imageUrl = value)" /></label><span>{{ post.content.length }}/1000</span><button class="primary-button" :disabled="busy || !post.content.trim()"><Send :size="16" />发布</button></div>
    </form>
    <button v-else class="outline-button" @click="emit('login')">登录后分享动态</button>
    <div class="xp-community-toolbar"><h2>社区动态</h2><div class="xp-community-tabs" role="group" aria-label="内容筛选"><button v-for="option in [{ value: 'all', label: '全部' }, { value: 'DYNAMIC', label: '运动动态' }, { value: 'TOPIC', label: '论坛话题' }, ...(token ? [{ value: 'mine', label: '我的' }] : [])]" :key="option.value" type="button" :class="{ selected: communityFilter === option.value }" @click="communityFilter = option.value">{{ option.label }}</button></div></div>
    <div v-if="!visiblePosts.length" class="xp-community-empty">{{ rows.length ? '这个分类还没有内容' : '暂无社区内容' }}</div>
    <div v-else class="xp-community-feed"><article v-for="row in visiblePosts" :key="row.id" class="xp-community-post">
      <div class="xp-community-head"><span class="xp-community-avatar"><img v-if="row.avatar" :src="row.avatar" alt="" /><span v-else>{{ row.nickname?.slice(0, 1) || '同' }}</span></span><div><strong>{{ row.nickname }}</strong><small>{{ date(row.createdAt) }}</small></div><span class="small-tag">{{ row.postType === 'TOPIC' ? '论坛话题' : '运动动态' }}</span></div>
      <p class="xp-community-content">{{ row.content }}</p><img v-if="row.imageUrl" class="xp-community-image" :src="row.imageUrl" alt="动态照片" />
      <div class="xp-community-interactions"><button type="button" @click="togglePost(row, 'like')"><Heart :size="17" />赞 {{ row.likeCount || 0 }}</button><button type="button" @click="openReplies(row)"><MessageCircle :size="17" />回复 {{ row.replyCount || 0 }}</button><button type="button" @click="togglePost(row, 'favorite')"><Star :size="17" />收藏 {{ row.favoriteCount || 0 }}</button><span></span><button v-if="token && user?.id !== row.userId" type="button" title="举报动态" aria-label="举报动态" @click="reportPost(row)"><Flag :size="16" /></button><button v-if="user?.id === row.userId || user?.role === 'ADMIN'" type="button" title="删除动态" aria-label="删除动态" @click="run(() => api.delete(`/community/posts/${row.id}`), '动态已删除')"><Trash2 :size="16" /></button></div>
      <div v-if="activePost === row.id" class="xp-community-replies"><div v-if="!(replies[row.id] || []).length" class="xp-community-no-replies">还没有回复</div><div v-for="reply in replies[row.id] || []" :key="reply.id" class="xp-community-reply"><span class="xp-community-avatar"><img v-if="reply.avatar" :src="reply.avatar" alt="" /><span v-else>{{ reply.nickname?.slice(0, 1) || '同' }}</span></span><div><strong>{{ reply.nickname }}</strong><p>{{ reply.content }}</p><small>{{ date(reply.createdAt) }}</small></div><button v-if="user?.id === reply.userId || user?.role === 'ADMIN'" type="button" class="icon-button danger-text" title="删除回复" @click="deleteReply(row, reply)"><Trash2 :size="14" /></button></div><form v-if="token" class="xp-community-reply-form" @submit.prevent="sendReply(row)"><input v-model="replyText" required maxlength="500" placeholder="写一条回复" /><button class="primary-button" :disabled="busy"><Send :size="16" />回复</button></form><button v-else class="text-button" @click="emit('login')">登录后回复</button></div>
    </article></div>
  </template>

  <template v-else-if="page === 'points'">
    <div class="xp-total"><Trophy :size="25" /><span>累计积分</span><strong>{{ points.total }}</strong></div>
    <div class="xp-stat-grid"><div><BarChart3 :size="19" /><strong>{{ stats.activityCount }}</strong><span>参加活动</span></div><div><Trophy :size="19" /><strong>{{ stats.competitionCount }}</strong><span>赛事报名</span></div><div><Camera :size="19" /><strong>{{ stats.checkinCount }}</strong><span>照片打卡</span></div><div><Plus :size="19" /><strong>{{ stats.createdCount }}</strong><span>发布活动</span></div></div>
    <div v-if="points.badges?.length" class="xp-badges"><h2 class="xp-section">我的勋章</h2><div class="xp-badge-grid"><div v-for="badge in points.badges" :key="badge.name"><span>{{ badge.icon }}</span><strong>{{ badge.name }}</strong><small>{{ badge.description }}</small></div></div></div>
    <section class="xp-points-section">
      <h2 class="xp-section">积分排行榜</h2>
      <div v-if="!leaderboard.length" class="xp-inline-empty">暂无排行数据</div>
      <div v-else class="xp-leaderboard">
        <div v-for="(member, index) in leaderboard" :key="member.id" class="xp-leader-row" :class="{ 'xp-leader-self': member.id === user?.id }">
          <span class="xp-rank" :class="{ 'xp-rank-top': index < 3 }">{{ String(index + 1).padStart(2, '0') }}</span>
          <span class="xp-leader-avatar">{{ member.nickname?.slice(0, 1) || '同' }}</span>
          <span class="xp-leader-info"><strong>{{ member.nickname }}<small v-if="member.id === user?.id" class="xp-self-label">我</small></strong><small>{{ member.checkinCount }} 条积分记录</small></span>
          <span class="xp-leader-score"><strong>{{ member.totalPoints }}</strong><small>积分</small></span>
        </div>
      </div>
    </section>
    <section class="xp-points-section">
      <h2 class="xp-section">积分记录</h2>
      <div v-if="!points.records.length" class="xp-points-empty"><span class="xp-empty-icon"><Trophy :size="19" /></span><div><strong>暂无积分记录</strong><p>参加运动打卡后，奖励会记录在这里。</p></div><a href="/checkins" class="xp-empty-action">去打卡 <ArrowUpRight :size="16" /></a></div>
      <div v-else class="xp-ledger"><div v-for="record in points.records" :key="record.id" class="xp-ledger-row"><span class="xp-ledger-icon"><Trophy :size="16" /></span><div><strong>{{ record.reason }}</strong><small>{{ date(record.createdAt) }}</small></div><b>+{{ record.points }}</b></div></div>
    </section>
    <section v-if="recommendations.length" class="xp-points-section">
      <h2 class="xp-section">为你推荐的活动</h2>
      <div class="xp-recommendations"><a v-for="item in recommendations" :key="item.id" class="xp-recommend-row" :href="`/activities/${item.id}`"><span class="xp-sport-icon"><CalendarDays :size="18" /></span><span class="xp-recommend-info"><strong>{{ item.title }}</strong><small>{{ item.sportType }} · {{ date(item.startTime) }}</small></span><ArrowUpRight :size="18" class="xp-recommend-arrow" /></a></div>
    </section>
  </template>

  <template v-else-if="page === 'checkins'">
    <div class="xp-total"><CalendarDays :size="25" /><span>连续打卡</span><strong>{{ checkinData.streak }} 天</strong><span>累计 {{ checkinData.total }} 次</span></div>
    <section class="xp-goals-section">
      <h2 class="xp-section">打卡目标与奖励</h2>
      <p class="xp-goal-note">每次运动至少 {{ checkinData.minDurationMinutes }} 分钟，每天可打卡一次；连续打卡中断后重新计算，累计次数保留。</p>
      <div v-if="!checkinData.goals.length" class="xp-inline-empty">当前暂无启用中的奖励目标</div>
      <div v-else class="xp-goal-list">
        <div v-for="goal in checkinData.goals" :key="goal.id" class="xp-goal-row">
          <div class="xp-goal-heading"><div><span class="small-tag">{{ { DAILY: '每日', STREAK: '连续', TOTAL: '累计' }[goal.ruleType] }}</span><strong>{{ goal.name }}</strong></div><span class="xp-goal-points">+{{ goal.rewardPoints }} 积分</span></div>
          <div class="xp-goal-meta"><span>{{ goal.ruleType === 'DAILY' ? '今日完成' : goal.ruleType === 'STREAK' ? '连续打卡' : '累计打卡' }} {{ Math.min(goal.progress, goal.targetValue) }}/{{ goal.targetValue }} {{ goal.unit }}</span><span>{{ goal.claimed ? '已领取' : `还差 ${Math.max(0, goal.targetValue - goal.progress)} ${goal.unit}` }}</span></div>
          <div class="xp-goal-track" role="progressbar" :aria-valuenow="Math.min(goal.progress, goal.targetValue)" :aria-valuemax="goal.targetValue" aria-valuemin="0"><span :style="{ width: `${Math.min(100, goal.progress / goal.targetValue * 100)}%` }"></span></div>
          <small v-if="goal.badgeName" class="xp-goal-badge">勋章：{{ goal.badgeName }}</small>
        </div>
      </div>
    </section>
    <form v-if="!checkinData.today?.length" class="xp-form" @submit.prevent="saveCheckin">
      <div class="xp-title"><Camera :size="20" /><h2>完成今日运动</h2></div>
      <div class="xp-fields">
        <label>运动类型<input v-model="checkinForm.sportType" list="checkin-sports" required maxlength="50" /><datalist id="checkin-sports"><option value="羽毛球" /><option value="乒乓球" /><option value="篮球" /><option value="足球" /><option value="排球" /><option value="跑步" /><option value="散步" /><option value="骑行" /><option value="健身" /></datalist></label>
        <label>运动时长（分钟）<input v-model.number="checkinForm.durationMinutes" type="number" :min="checkinData.minDurationMinutes" max="1440" required /></label>
        <label v-if="hasDistance">运动距离（公里，可选）<input v-model.number="checkinForm.distanceKm" type="number" min="0" step="0.1" /></label>
        <label>运动照片<span class="xp-upload">选择照片<input type="file" accept="image/jpeg,image/png,image/webp" @change="chooseCheckinImage" /></span></label>
      </div>
      <textarea v-model="checkinForm.note" rows="2" maxlength="500" placeholder="写下今天的运动感受"></textarea>
      <img v-if="checkinForm.imageUrl" class="xp-preview" :src="checkinForm.imageUrl" alt="运动照片预览" />
      <div class="xp-actions"><button class="primary-button" :disabled="busy"><Camera :size="16" />今日打卡</button></div>
    </form>
    <div v-else class="xp-success">今天已经完成运动打卡，明天再来继续坚持。</div>
    <h2 class="xp-section">我的打卡记录</h2>
    <div v-if="!checkinData.records.length" class="xp-inline-empty">还没有运动记录</div>
    <div v-else class="xp-list"><div v-for="item in checkinData.records" :key="item.id" class="xp-item xp-record"><div><strong>{{ item.sportType }} · {{ item.checkinDate }}</strong><small>{{ item.durationMinutes }} 分钟<span v-if="Number(item.distanceKm) > 0"> · {{ item.distanceKm }} 公里</span><span v-if="item.note"> · {{ item.note }}</span></small></div><span class="small-tag">已打卡</span></div></div>
    <h2 v-if="checkinData.rewards?.length" class="xp-section">获得的奖励</h2>
    <div class="xp-badge-grid"><div v-for="item in checkinData.rewards" :key="item.name + item.createdAt"><Trophy :size="20" /><strong>{{ item.name }}</strong><small>{{ item.sourceDate }} · +{{ item.rewardPoints }} 积分 {{ item.badgeName || '' }}</small></div></div>
  </template>

  <template v-else-if="page === 'admin-reward-rules'">
    <form class="xp-form xp-policy-form" @submit.prevent="saveCheckinPolicy"><div class="xp-title"><CalendarDays :size="20" /><h2>有效打卡条件</h2></div><div class="xp-policy-fields"><label>最低运动时长（分钟）<input v-model.number="checkinPolicy.minDurationMinutes" type="number" min="1" max="1440" required /></label><button class="outline-button" :disabled="busy">保存条件</button></div></form>
    <form class="xp-form" @submit.prevent="saveRule">
      <div class="xp-title"><Trophy :size="20" /><h2>{{ rule.id ? '编辑奖励规则' : '新增奖励规则' }}</h2></div>
      <div class="xp-fields">
        <label>规则名称<input v-model="rule.name" required maxlength="100" /></label>
        <label>规则类型<select v-model="rule.ruleType" @change="ruleTypeChanged"><option value="DAILY">每日打卡</option><option value="STREAK">连续打卡</option><option value="TOTAL">累计次数</option></select></label>
        <label>达成目标（{{ rule.ruleType === 'STREAK' ? '天' : '次' }}）<input v-model.number="rule.targetValue" type="number" min="1" :max="rule.ruleType === 'DAILY' ? 1 : 3650" :disabled="rule.ruleType === 'DAILY'" required /></label>
        <label>奖励积分<input v-model.number="rule.rewardPoints" type="number" min="0" max="10000" required /></label>
      </div>
      <label>奖励勋章名称<input v-model="rule.badgeName" maxlength="100" placeholder="可选，例如坚持运动" /></label>
      <div class="xp-actions"><button class="primary-button" :disabled="busy"><Plus :size="16" />保存规则</button><button v-if="rule.id" type="button" class="outline-button" @click="resetRule">取消编辑</button></div>
    </form>
    <div v-if="!rules.length" class="state-panel">暂无奖励规则</div>
    <div v-else class="xp-list xp-rule-list">
      <div v-for="item in rules" :key="item.id" class="xp-item xp-record xp-rule-item">
        <div><strong>{{ item.name }}</strong><small>{{ { DAILY: '每日打卡', STREAK: '连续打卡', TOTAL: '累计次数' }[item.ruleType] }} · 目标 {{ item.targetValue }} {{ item.ruleType === 'STREAK' ? '天' : '次' }} · +{{ item.rewardPoints }} 积分 {{ item.badgeName || '' }}</small></div>
        <span class="small-tag" :class="{ 'muted-tag': !item.status }">{{ item.status ? '启用中' : '已停用' }}</span>
        <button class="text-button" :disabled="busy" @click="editRule(item)">编辑</button>
        <button v-if="item.status" class="icon-button danger-text" title="停用规则" :disabled="busy" @click="run(() => api.delete(`/admin/reward-rules/${item.id}`), '规则已停用')"><Trash2 :size="16" /></button>
        <button v-else class="text-button" :disabled="busy" @click="run(() => api.put(`/admin/reward-rules/${item.id}/enable`, {}), '规则已启用')">启用</button>
      </div>
    </div>
  </template>

  <template v-else-if="page === 'admin-users'"><div class="xp-list"><div v-for="member in users" :key="member.id" class="xp-item xp-record"><div><strong>{{ member.nickname }}</strong><small>{{ member.username }}</small></div><span v-if="member.role === 'ADMIN'">系统管理员</span><select v-else :value="member.role" :disabled="busy" @change="run(() => api.put(`/admin/users/${member.id}/role`, { role: $event.target.value }), '角色已更新')"><option value="STUDENT">普通用户</option><option value="EVENT_MANAGER">赛事管理员</option></select></div></div></template>
</template>
