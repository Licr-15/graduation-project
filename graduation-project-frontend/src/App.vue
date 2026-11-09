<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "./stores/auth.js";
import { api } from "./api.js";
import {
  dateTime,
  isPast,
  isoLocal,
  localInput,
  sportImage,
  sports,
} from "./format.js";
import {
  Activity,
  ArrowLeft,
  CalendarDays,
  Check,
  ChevronRight,
  Clock3,
  Compass,
  ImagePlus,
  LogOut,
  MapPin,
  Megaphone,
  Menu,
  MessageCircle,
  Pencil,
  Plus,
  Search,
  ShieldCheck,
  Shuffle,
  Trash2,
  UserRound,
  Users,
  X,
} from "lucide-vue-next";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const page = computed(() => route.name);
const isAdmin = computed(() => auth.user?.role === "ADMIN");
const activities = ref([]);
const created = ref([]);
const joined = ref([]);
const adminActivities = ref([]);
const venues = ref([]);
const notices = ref([]);
const detail = ref(null);
const comments = ref([]);
const loading = ref(false);
const saving = ref(false);
const toast = ref("");
const error = ref("");
const sidebarOpen = ref(false);
const authOpen = ref(false);
const authMode = ref("login");
const activityOpen = ref(false);
const venueOpen = ref(false);
const noticeOpen = ref(false);
const mineTab = ref("created");
const filter = reactive({ keyword: "", sportType: "全部", page: 1, size: 9 });
const hasMore = ref(false);
const picking = ref(false);
const lastPickedId = ref(null);
const authForm = reactive({ username: "", password: "", nickname: "" });
const profileForm = reactive({ nickname: "", phone: "", avatar: "" });
const activityForm = reactive({
  id: null,
  title: "",
  sportType: "羽毛球",
  venueId: "",
  description: "",
  imageUrl: "",
  startTime: "",
  endTime: "",
  maxPeople: 4,
});
const venueForm = reactive({
  id: null,
  name: "",
  address: "",
  sportType: "羽毛球",
  description: "",
});
const noticeForm = reactive({ title: "", content: "" });
const commentText = ref("");
let toastTimer;

const joinedIds = computed(() => new Set(joined.value.map((item) => item.id)));
const displayedMine = computed(() =>
  mineTab.value === "created" ? created.value : joined.value,
);
const title = computed(
  () =>
    ({
      activities: "发现运动搭子",
      detail: "活动详情",
      mine: "我的活动",
      venues: "运动场地",
      notices: "校园公告",
      profile: "个人资料",
      "admin-activities": "活动管理",
      "admin-venues": "场地管理",
      "admin-notices": "公告管理",
    })[page.value] || "绿茵结伴",
);
function message(text) {
  toast.value = text;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => {
    toast.value = "";
  }, 3500);
}
function fail(err) {
  error.value = err.message || "操作失败";
  message(error.value);
}
function requireLogin() {
  if (!auth.token) {
    authMode.value = "login";
    authOpen.value = true;
    return false;
  }
  return true;
}
function clearError() {
  error.value = "";
}
const imageChoice = ref("default");
function setImageChoice(value) {
  imageChoice.value = value;
  if (value === "default") activityForm.imageUrl = "";
  else if (value !== "custom") activityForm.imageUrl = value;
}
function onImageFile(event) {
  const file = event.target.files?.[0];
  if (!file) return;
  if (!file.type.startsWith("image/")) return message("请选择图片文件");
  if (file.size > 2 * 1024 * 1024) return message("本地图片不能超过 2MB");
  const reader = new FileReader();
  reader.onload = () => {
    activityForm.imageUrl = String(reader.result);
    imageChoice.value = "custom";
  };
  reader.readAsDataURL(file);
}
const activityPreview = computed(() =>
  sportImage(activityForm.sportType, activityForm.imageUrl),
);
function activityStatus(item) {
  if (isPast(item.startTime)) return "已开始";
  return item.joinedCount >= item.maxPeople ? "已满员" : "正在招募";
}
async function pickActivity() {
  if (picking.value) return;
  picking.value = true;
  try {
    const rows = await api.get("/activities", {
      keyword: filter.keyword.trim() || undefined,
      sportType: filter.sportType === "全部" ? undefined : filter.sportType,
      page: 1,
      size: 100,
    });
    const available = (rows || []).filter(
      (item) =>
        item.status === "OPEN" &&
        !isPast(item.startTime) &&
        item.joinedCount < item.maxPeople &&
        item.publisherId !== auth.user?.id,
    );
    if (!available.length) return message("暂时没有可加入的活动，换个分类看看");
    const choices =
      available.length > 1
        ? available.filter((item) => item.id !== lastPickedId.value)
        : available;
    const selected = choices[Math.floor(Math.random() * choices.length)];
    lastPickedId.value = selected.id;
    await router.push(`/activities/${selected.id}`);
  } catch (err) {
    fail(err);
  } finally {
    picking.value = false;
  }
}
async function loadActivities() {
  loading.value = true;
  clearError();
  try {
    const rows = await api.get("/activities", {
      keyword: filter.keyword.trim() || undefined,
      sportType: filter.sportType === "全部" ? undefined : filter.sportType,
      page: filter.page,
      size: filter.size,
    });
    activities.value = rows || [];
    hasMore.value = activities.value.length === filter.size;
  } catch (err) {
    fail(err);
  } finally {
    loading.value = false;
  }
}
async function loadMine() {
  if (!auth.token) return;
  loading.value = true;
  clearError();
  try {
    [created.value, joined.value] = await Promise.all([
      api.get("/activities/mine"),
      api.get("/activities/mine/joined"),
    ]);
  } catch (err) {
    fail(err);
  } finally {
    loading.value = false;
  }
}
async function loadAdminActivities() {
  loading.value = true;
  clearError();
  try {
    adminActivities.value = (await api.get("/admin/activities")) || [];
  } catch (err) {
    fail(err);
  } finally {
    loading.value = false;
  }
}
async function loadDetail() {
  loading.value = true;
  clearError();
  detail.value = null;
  try {
    const id = route.params.id;
    const [item, discussion] = await Promise.all([
      api.get(`/activities/${id}`),
      api.get(`/comments/activity/${id}`),
    ]);
    detail.value = item;
    comments.value = discussion || [];
    if (auth.token) joined.value = await api.get("/activities/mine/joined");
  } catch (err) {
    fail(err);
  } finally {
    loading.value = false;
  }
}
async function loadVenues() {
  loading.value = true;
  clearError();
  try {
    venues.value = (await api.get("/venues")) || [];
  } catch (err) {
    fail(err);
  } finally {
    loading.value = false;
  }
}
async function loadNotices() {
  loading.value = true;
  clearError();
  try {
    notices.value = (await api.get("/notices")) || [];
  } catch (err) {
    fail(err);
  } finally {
    loading.value = false;
  }
}
async function loadPage() {
  sidebarOpen.value = false;
  if (page.value === "activities") await loadActivities();
  else if (page.value === "detail") await loadDetail();
  else if (page.value === "mine") await loadMine();
  else if (page.value === "admin-activities") await loadAdminActivities();
  else if (page.value === "venues" || page.value === "admin-venues")
    await loadVenues();
  else if (page.value === "notices" || page.value === "admin-notices")
    await loadNotices();
  else if (page.value === "profile")
    Object.assign(profileForm, {
      nickname: auth.user?.nickname || "",
      phone: auth.user?.phone || "",
      avatar: auth.user?.avatar || "",
    });
}
function searchActivities() {
  filter.page = 1;
  loadActivities();
}
function setSport(value) {
  filter.sportType = value;
  searchActivities();
}
function openActivity(item = null) {
  if (!requireLogin()) return;
  Object.assign(
    activityForm,
    item
      ? {
          id: item.id,
          title: item.title,
          sportType: item.sportType,
          venueId: item.venueId ?? "",
          description: item.description || "",
          imageUrl: item.imageUrl || "",
          startTime: localInput(item.startTime),
          endTime: localInput(item.endTime),
          maxPeople: item.maxPeople,
        }
      : {
          id: null,
          title: "",
          sportType: "羽毛球",
          venueId: "",
          description: "",
          imageUrl: "",
          startTime: "",
          endTime: "",
          maxPeople: 4,
        },
  );
  imageChoice.value = item?.imageUrl ? "custom" : "default";
  activityOpen.value = true;
  loadVenuesForForm();
}
async function loadVenuesForForm() {
  try {
    venues.value = (await api.get("/venues")) || [];
  } catch (err) {
    fail(err);
  }
}
async function saveActivity() {
  if (saving.value) return;
  if (new Date(activityForm.startTime) <= new Date())
    return message("开始时间必须晚于现在");
  if (new Date(activityForm.endTime) <= new Date(activityForm.startTime))
    return message("结束时间必须晚于开始时间");
  saving.value = true;
  try {
    const data = {
      title: activityForm.title.trim(),
      sportType: activityForm.sportType.trim(),
      venueId: activityForm.venueId ? Number(activityForm.venueId) : null,
      description: activityForm.description.trim() || null,
      imageUrl: activityForm.imageUrl || null,
      startTime: isoLocal(activityForm.startTime),
      endTime: isoLocal(activityForm.endTime),
      maxPeople: Number(activityForm.maxPeople),
    };
    const adminEdit = isAdmin.value && page.value === "admin-activities";
    if (activityForm.id) {
      await api.put(
        `${adminEdit ? "/admin/activities" : "/activities"}/${activityForm.id}`,
        data,
      );
      message("活动已更新");
      activityOpen.value = false;
      await loadPage();
    } else {
      const id = await api.post("/activities", data);
      message("活动发布成功");
      activityOpen.value = false;
      await router.push(`/activities/${id}`);
    }
  } catch (err) {
    fail(err);
  } finally {
    saving.value = false;
  }
}
async function cancelActivity(item) {
  if (!confirm(`确定取消“${item.title}”吗？`)) return;
  try {
    await api.delete(
      `${isAdmin.value && page.value === "admin-activities" ? "/admin/activities" : "/activities"}/${item.id}`,
    );
    message("活动已取消");
    await loadPage();
  } catch (err) {
    fail(err);
  }
}
async function toggleJoin(item) {
  if (!requireLogin()) return;
  saving.value = true;
  try {
    if (joinedIds.value.has(item.id)) {
      await api.delete(`/activities/${item.id}/join`);
      message("已取消报名");
    } else {
      await api.post(`/activities/${item.id}/join`, {});
      message("报名成功");
    }
    if (page.value === "detail") await loadDetail();
    else if (page.value === "mine") await loadMine();
    else {
      joined.value = await api.get("/activities/mine/joined");
      await loadActivities();
    }
  } catch (err) {
    fail(err);
  } finally {
    saving.value = false;
  }
}
async function submitComment() {
  if (!requireLogin() || !commentText.value.trim()) return;
  saving.value = true;
  try {
    await api.post(`/comments/activity/${route.params.id}`, {
      content: commentText.value.trim(),
    });
    commentText.value = "";
    comments.value = await api.get(`/comments/activity/${route.params.id}`);
    message("评论已发布");
  } catch (err) {
    fail(err);
  } finally {
    saving.value = false;
  }
}
async function deleteComment(item) {
  if (!confirm("确定删除这条评论吗？")) return;
  try {
    await api.delete(`/comments/${item.id}`);
    comments.value = await api.get(`/comments/activity/${route.params.id}`);
    message("评论已删除");
  } catch (err) {
    fail(err);
  }
}
async function submitAuth() {
  saving.value = true;
  try {
    const data =
      authMode.value === "login"
        ? { username: authForm.username.trim(), password: authForm.password }
        : {
            username: authForm.username.trim(),
            password: authForm.password,
            nickname: authForm.nickname.trim(),
          };
    auth.setSession(
      await api.post(
        authMode.value === "login" ? "/auth/login" : "/auth/register",
        data,
      ),
    );
    authOpen.value = false;
    authForm.password = "";
    message(authMode.value === "login" ? "登录成功" : "注册成功");
    if (route.query.login) router.replace({ path: route.path });
    await loadPage();
  } catch (err) {
    fail(err);
  } finally {
    saving.value = false;
  }
}
function logout() {
  auth.logout();
  created.value = [];
  joined.value = [];
  router.push("/activities");
  message("已退出登录");
}
async function saveProfile() {
  saving.value = true;
  try {
    auth.user = await api.put("/auth/profile", {
      nickname: profileForm.nickname.trim(),
      phone: profileForm.phone.trim() || null,
      avatar: profileForm.avatar.trim() || null,
    });
    localStorage.setItem("lvyin_user", JSON.stringify(auth.user));
    message("资料已保存");
  } catch (err) {
    fail(err);
  } finally {
    saving.value = false;
  }
}
function openVenue(item = null) {
  Object.assign(
    venueForm,
    item
      ? { ...item }
      : {
          id: null,
          name: "",
          address: "",
          sportType: "羽毛球",
          description: "",
        },
  );
  venueOpen.value = true;
}
async function saveVenue() {
  saving.value = true;
  try {
    const data = {
      name: venueForm.name.trim(),
      address: venueForm.address.trim(),
      sportType: venueForm.sportType.trim(),
      description: venueForm.description.trim() || null,
    };
    if (venueForm.id) await api.put(`/admin/venues/${venueForm.id}`, data);
    else await api.post("/admin/venues", data);
    venueOpen.value = false;
    message("场地已保存");
    await loadVenues();
  } catch (err) {
    fail(err);
  } finally {
    saving.value = false;
  }
}
async function disableVenue(item) {
  if (!confirm(`确定停用“${item.name}”吗？停用后不会显示在公开场地列表。`))
    return;
  try {
    await api.delete(`/admin/venues/${item.id}`);
    message("场地已停用");
    await loadVenues();
  } catch (err) {
    fail(err);
  }
}
async function saveNotice() {
  saving.value = true;
  try {
    await api.post("/admin/notices", {
      title: noticeForm.title.trim(),
      content: noticeForm.content.trim(),
    });
    noticeOpen.value = false;
    Object.assign(noticeForm, { title: "", content: "" });
    message("公告已发布");
    await loadNotices();
  } catch (err) {
    fail(err);
  } finally {
    saving.value = false;
  }
}
function unauthorized() {
  auth.logout();
  message("登录已失效，请重新登录");
  authOpen.value = true;
}
watch(
  () => route.fullPath,
  () => {
    if (route.query.login) authOpen.value = true;
    loadPage();
  },
  { immediate: true },
);
onMounted(() => {
  auth.refresh();
  window.addEventListener("lvyin-unauthorized", unauthorized);
});
onUnmounted(() => {
  window.removeEventListener("lvyin-unauthorized", unauthorized);
  clearTimeout(toastTimer);
});
</script>

<template>
  <div class="app-shell">
    <div
      v-if="sidebarOpen"
      class="mobile-shade"
      @click="sidebarOpen = false"
    ></div>
    <aside class="sidebar" :class="{ open: sidebarOpen }">
      <RouterLink to="/activities" class="brand"
        ><span class="brand-mark"><Activity :size="19" /></span
        ><span>绿茵结伴</span></RouterLink
      >
      <div class="side-label">校园运动</div>
      <RouterLink
        to="/activities"
        class="side-link"
        :class="{ current: page === 'activities' || page === 'detail' }"
        ><Compass :size="18" />发现活动</RouterLink
      >
      <RouterLink
        to="/mine"
        class="side-link"
        :class="{ current: page === 'mine' }"
        ><CalendarDays :size="18" />我的活动</RouterLink
      >
      <RouterLink
        to="/venues"
        class="side-link"
        :class="{ current: page === 'venues' }"
        ><MapPin :size="18" />运动场地</RouterLink
      >
      <RouterLink
        to="/notices"
        class="side-link"
        :class="{ current: page === 'notices' }"
        ><Megaphone :size="18" />校园公告</RouterLink
      >
      <div v-if="isAdmin" class="side-label section-gap">管理员</div>
      <RouterLink
        v-if="isAdmin"
        to="/admin/venues"
        class="side-link"
        :class="{ current: page === 'admin-venues' }"
        ><ShieldCheck :size="18" />场地管理</RouterLink
      >
      <RouterLink
        v-if="isAdmin"
        to="/admin/activities"
        class="side-link"
        :class="{ current: page === 'admin-activities' }"
        ><Activity :size="18" />活动管理</RouterLink
      >
      <RouterLink
        v-if="isAdmin"
        to="/admin/notices"
        class="side-link"
        :class="{ current: page === 'admin-notices' }"
        ><Megaphone :size="18" />公告管理</RouterLink
      >
      <div class="side-bottom">
        <span class="side-dot"></span> 和同学一起，动起来
      </div>
    </aside>
    <div class="workspace">
      <header class="topbar">
        <div class="top-left">
          <button
            class="icon-button mobile-menu"
            title="打开导航"
            @click="sidebarOpen = true"
          >
            <Menu :size="20" /></button
          ><span class="breadcrumb">绿茵结伴</span
          ><ChevronRight :size="15" class="crumb-arrow" /><strong>{{
            title
          }}</strong>
        </div>
        <div class="top-actions">
          <span class="top-date">校园运动结伴服务平台</span
          ><button
            v-if="auth.token"
            class="user-button"
            @click="router.push('/profile')"
          >
            <span class="user-avatar">{{
              auth.user?.nickname?.slice(0, 1) || "同"
            }}</span
            ><span>{{ auth.user?.nickname || "同学" }}</span></button
          ><button v-else class="outline-button" @click="authOpen = true">
            登录 / 注册
          </button>
        </div>
      </header>
      <main class="content">
        <template v-if="page === 'activities'">
          <div class="page-head">
            <div>
              <p class="eyebrow">SPORTS TOGETHER</p>
              <h1>发现运动搭子</h1>
              <p>找一场合适的运动，和同学一起出发。</p>
            </div>
            <button class="primary-button" @click="openActivity()">
              <Plus :size="17" />发布活动
            </button>
          </div>
          <div class="feature-strip">
            <div>
              <span class="strip-kicker">绿茵结伴 · 校园运动社区</span>
              <h2>下一场运动，一起去</h2>
              <p>球场、跑道与操场，总有人和你时间相合。</p>
            </div>
            <button
              class="surprise-button"
              :disabled="picking"
              @click="pickActivity"
            >
              <Shuffle :size="17" />{{ picking ? "寻找中…" : "随机挑一场" }}
            </button>
          </div>
          <div class="toolbar">
            <div class="sport-tabs">
              <button
                v-for="sport in sports"
                :key="sport"
                :class="{ selected: filter.sportType === sport }"
                @click="setSport(sport)"
              >
                {{ sport }}
              </button>
            </div>
            <form class="search-box" @submit.prevent="searchActivities">
              <Search :size="17" /><input
                v-model="filter.keyword"
                placeholder="搜索活动或地点"
                aria-label="搜索活动或地点"
              /><button title="搜索" type="submit">
                <ChevronRight :size="18" />
              </button>
            </form>
          </div>
          <div class="section-heading">
            <h2>近期活动</h2>
            <span>按开始时间排序</span>
          </div>
          <div v-if="loading" class="state-panel">正在加载活动…</div>
          <div v-else-if="error" class="state-panel">
            <p>{{ error }}</p>
            <button class="text-button" @click="loadActivities">重试</button>
          </div>
          <div v-else-if="!activities.length" class="state-panel">
            <Activity :size="30" />
            <h3>暂无符合条件的活动</h3>
            <p>试试其他运动类型，或发布一场新的结伴活动。</p>
            <button class="primary-button" @click="openActivity()">
              <Plus :size="16" />发布活动
            </button>
          </div>
          <div v-else class="activity-grid">
            <article
              v-for="item in activities"
              :key="item.id"
              class="activity-card"
              @click="router.push(`/activities/${item.id}`)"
            >
              <div
                class="card-image"
                :style="{
                  backgroundImage: `url(${sportImage(item.sportType, item.imageUrl)})`,
                }"
              >
                <span
                  class="status-label"
                  :class="{
                    full: activityStatus(item) === '已满员',
                    past: activityStatus(item) === '已开始',
                  }"
                  >{{ activityStatus(item) }}</span
                ><span class="sport-label">{{ item.sportType }}</span>
              </div>
              <div class="card-content">
                <h3>{{ item.title }}</h3>
                <p>
                  <Clock3 :size="15" />{{ dateTime(item.startTime) }} -
                  {{ dateTime(item.endTime).split(" ").pop() }}
                </p>
                <p>
                  <MapPin :size="15" />{{
                    item.venueName || "地点由发起人告知"
                  }}
                </p>
                <div class="card-footer">
                  <span class="host-initial">{{
                    item.publisherName?.slice(0, 1)
                  }}</span
                  ><span class="host-name">{{ item.publisherName }}</span
                  ><span class="spacer"></span
                  ><span>{{ item.joinedCount }} / {{ item.maxPeople }} 人</span
                  ><ChevronRight :size="16" />
                </div>
              </div>
            </article>
          </div>
          <div v-if="activities.length" class="pagination">
            <button
              class="outline-button"
              :disabled="filter.page === 1"
              @click="
                filter.page--;
                loadActivities();
              "
            >
              上一页</button
            ><span>第 {{ filter.page }} 页</span
            ><button
              class="outline-button"
              :disabled="!hasMore"
              @click="
                filter.page++;
                loadActivities();
              "
            >
              下一页
            </button>
          </div>
        </template>
        <template v-else-if="page === 'detail'"
          ><button class="back-link" @click="router.push('/activities')">
            <ArrowLeft :size="17" />返回活动列表
          </button>
          <div v-if="loading" class="state-panel">正在加载活动…</div>
          <div v-else-if="error" class="state-panel">
            <h3>{{ error }}</h3>
            <button class="text-button" @click="loadDetail">重试</button>
          </div>
          <div v-else-if="detail" class="detail-layout">
            <div class="detail-main">
              <div
                class="detail-cover"
                :style="{
                  backgroundImage: `url(${sportImage(detail.sportType, detail.imageUrl)})`,
                }"
              ></div>
              <div class="detail-title-row">
                <div>
                  <div class="detail-tags">
                    <span>{{ detail.sportType }}</span
                    ><span :class="{ dim: detail.status !== 'OPEN' }">{{
                      detail.status === "OPEN" ? "正在招募" : "已取消"
                    }}</span>
                  </div>
                  <h1>{{ detail.title }}</h1>
                  <p>由 {{ detail.publisherName }} 发布</p>
                </div>
                <div
                  v-if="
                    auth.user?.id === detail.publisherId &&
                    detail.status === 'OPEN'
                  "
                  class="detail-actions"
                >
                  <button class="outline-button" @click="openActivity(detail)">
                    <Pencil :size="15" />编辑</button
                  ><button
                    class="outline-button danger-text"
                    @click="cancelActivity(detail)"
                  >
                    <Trash2 :size="15" />取消活动
                  </button>
                </div>
              </div>
              <section class="plain-section">
                <h2>活动介绍</h2>
                <p class="description">
                  {{ detail.description || "发起人暂未填写活动介绍。" }}
                </p>
              </section>
              <section class="plain-section">
                <div class="section-heading">
                  <h2>活动讨论</h2>
                  <span>{{ comments.length }} 条评论</span>
                </div>
                <div v-if="comments.length" class="comment-list">
                  <div
                    v-for="comment in comments"
                    :key="comment.id"
                    class="comment"
                  >
                    <div class="comment-avatar">
                      {{ comment.nickname?.slice(0, 1) }}
                    </div>
                    <div>
                      <div class="comment-top">
                        <strong>{{ comment.nickname }}</strong
                        ><span>{{ dateTime(comment.createdAt) }}</span
                        ><button
                          v-if="auth.user?.id === comment.userId"
                          title="删除评论"
                          @click="deleteComment(comment)"
                        >
                          <Trash2 :size="15" />
                        </button>
                      </div>
                      <p>{{ comment.content }}</p>
                    </div>
                  </div>
                </div>
                <p v-else class="empty-inline">还没有评论。</p>
                <form class="comment-form" @submit.prevent="submitComment">
                  <textarea
                    v-model="commentText"
                    maxlength="500"
                    placeholder="说说你的想法…"
                    aria-label="评论内容"
                    required
                  ></textarea
                  ><button
                    class="primary-button"
                    :disabled="saving || !commentText.trim()"
                  >
                    <MessageCircle :size="16" />发布评论
                  </button>
                </form>
              </section>
            </div>
            <aside class="detail-side">
              <h2>活动信息</h2>
              <div class="info-line">
                <span><CalendarDays :size="16" />活动时间</span
                ><strong
                  >{{ dateTime(detail.startTime) }} -
                  {{ dateTime(detail.endTime).split(" ").pop() }}</strong
                >
              </div>
              <div class="info-line">
                <span><MapPin :size="16" />集合地点</span
                ><strong>{{ detail.venueName || "地点由发起人告知" }}</strong>
              </div>
              <div class="info-line">
                <span><UserRound :size="16" />发起人</span
                ><strong>{{ detail.publisherName }}</strong>
              </div>
              <div class="info-line">
                <span><Users :size="16" />报名人数</span
                ><strong
                  >{{ detail.joinedCount }} / {{ detail.maxPeople }} 人</strong
                >
                <div class="capacity">
                  <i
                    :style="{
                      width: `${Math.min(100, (detail.joinedCount / detail.maxPeople) * 100)}%`,
                    }"
                  ></i>
                </div>
                <small
                  >剩余
                  {{
                    Math.max(0, detail.maxPeople - detail.joinedCount)
                  }}
                  个名额</small
                >
              </div>
              <button
                v-if="
                  detail.status === 'OPEN' &&
                  !isPast(detail.startTime) &&
                  auth.user?.id !== detail.publisherId
                "
                class="primary-button wide"
                :class="{ secondary: joinedIds.has(detail.id) }"
                :disabled="
                  saving ||
                  (!joinedIds.has(detail.id) &&
                    detail.joinedCount >= detail.maxPeople)
                "
                @click="toggleJoin(detail)"
              >
                <Check v-if="joinedIds.has(detail.id)" :size="17" /><Plus
                  v-else
                  :size="17"
                />{{
                  joinedIds.has(detail.id)
                    ? "取消报名"
                    : detail.joinedCount >= detail.maxPeople
                      ? "人数已满"
                      : "加入这场运动"
                }}
              </button>
              <p v-else class="side-note">
                {{
                  auth.user?.id === detail.publisherId
                    ? "这是你发布的活动"
                    : detail.status !== "OPEN"
                      ? "活动已取消"
                      : "活动已开始"
                }}
              </p>
            </aside>
          </div></template
        >
        <template v-else-if="page === 'mine'"
          ><div class="page-head">
            <div>
              <p class="eyebrow">MY ACTIVITIES</p>
              <h1>我的活动</h1>
              <p>管理自己发布的运动，也能查看已报名的场次。</p>
            </div>
            <button class="primary-button" @click="openActivity()">
              <Plus :size="17" />发布活动
            </button>
          </div>
          <div class="subtabs">
            <button
              :class="{ active: mineTab === 'created' }"
              @click="mineTab = 'created'"
            >
              我发布的 <span>{{ created.length }}</span></button
            ><button
              :class="{ active: mineTab === 'joined' }"
              @click="mineTab = 'joined'"
            >
              我报名的 <span>{{ joined.length }}</span>
            </button>
          </div>
          <div v-if="loading" class="state-panel">正在加载活动…</div>
          <div v-else-if="error" class="state-panel">{{ error }}</div>
          <div v-else-if="!displayedMine.length" class="state-panel">
            <CalendarDays :size="30" />
            <h3>
              {{ mineTab === "created" ? "还没有发布活动" : "还没有报名活动" }}
            </h3>
            <button
              class="primary-button"
              @click="
                mineTab === 'created'
                  ? openActivity()
                  : router.push('/activities')
              "
            >
              {{ mineTab === "created" ? "发布活动" : "去发现活动" }}
            </button>
          </div>
          <div v-else class="mine-list">
            <article
              v-for="item in displayedMine"
              :key="item.id"
              class="mine-item"
            >
              <img :src="sportImage(item.sportType, item.imageUrl)" :alt="item.sportType" />
              <div class="mine-content">
                <div class="mine-top">
                  <span class="small-tag">{{ item.sportType }}</span
                  ><span class="small-tag muted-tag">{{
                    item.status === "OPEN"
                      ? isPast(item.startTime)
                        ? "已开始"
                        : "招募中"
                      : "已取消"
                  }}</span>
                </div>
                <h3>{{ item.title }}</h3>
                <p>
                  {{ dateTime(item.startTime) }} ·
                  {{ item.venueName || "地点由发起人告知" }} ·
                  {{ item.joinedCount }}/{{ item.maxPeople }} 人
                </p>
              </div>
              <div class="mine-actions">
                <button
                  class="text-button"
                  @click="router.push(`/activities/${item.id}`)"
                >
                  查看详情</button
                ><button
                  v-if="mineTab === 'created' && item.status === 'OPEN'"
                  class="icon-button"
                  title="编辑活动"
                  @click="openActivity(item)"
                >
                  <Pencil :size="17" /></button
                ><button
                  v-if="mineTab === 'created' && item.status === 'OPEN'"
                  class="icon-button danger-text"
                  title="取消活动"
                  @click="cancelActivity(item)"
                >
                  <Trash2 :size="17" /></button
                ><button
                  v-if="mineTab === 'joined' && item.status === 'OPEN'"
                  class="outline-button"
                  @click="toggleJoin(item)"
                >
                  取消报名
                </button>
              </div>
            </article>
          </div></template
        >
        <template v-else-if="page === 'admin-activities'">
          <div class="page-head"><div><p class="eyebrow">ALL ACTIVITIES</p><h1>活动管理</h1><p>管理员可以查看、编辑和取消所有用户发布的活动。</p></div></div>
          <div v-if="loading" class="state-panel">正在加载活动…</div>
          <div v-else-if="error" class="state-panel">{{ error }}</div>
          <div v-else-if="!adminActivities.length" class="state-panel"><Activity :size="30" /><h3>暂无活动</h3></div>
          <div v-else class="admin-activity-list">
            <article v-for="item in adminActivities" :key="item.id" class="admin-activity-item">
              <img :src="sportImage(item.sportType, item.imageUrl)" :alt="item.sportType" />
              <div class="admin-activity-content"><div class="mine-top"><span class="small-tag">{{ item.sportType }}</span><span class="small-tag muted-tag">{{ item.status === 'OPEN' ? '开放中' : '已取消' }}</span></div><h3>{{ item.title }}</h3><p>发布者：{{ item.publisherName }} · {{ dateTime(item.startTime) }} · {{ item.joinedCount }}/{{ item.maxPeople }} 人</p></div>
              <div class="mine-actions"><button class="text-button" @click="router.push(`/activities/${item.id}`)">查看</button><button class="icon-button" title="编辑活动" @click="openActivity(item)"><Pencil :size="17" /></button><button v-if="item.status === 'OPEN'" class="icon-button danger-text" title="取消活动" @click="cancelActivity(item)"><Trash2 :size="17" /></button></div>
            </article>
          </div>
        </template>
        <template v-else-if="page === 'venues' || page === 'admin-venues'"
          ><div class="page-head">
            <div>
              <p class="eyebrow">CAMPUS VENUES</p>
              <h1>{{ page === "admin-venues" ? "场地管理" : "运动场地" }}</h1>
              <p>
                {{
                  page === "admin-venues"
                    ? "维护平台展示的校园运动场地。"
                    : "查看校园内可以开展运动的场地。"
                }}
              </p>
            </div>
            <button
              v-if="page === 'admin-venues'"
              class="primary-button"
              @click="openVenue()"
            >
              <Plus :size="17" />新增场地
            </button>
          </div>
          <div v-if="loading" class="state-panel">正在加载场地…</div>
          <div v-else-if="error" class="state-panel">{{ error }}</div>
          <div v-else-if="!venues.length" class="state-panel">
            <MapPin :size="30" />
            <h3>暂无场地</h3>
          </div>
          <div v-else class="venue-grid">
            <article v-for="venue in venues" :key="venue.id" class="venue-item">
              <div class="venue-symbol"><MapPin :size="22" /></div>
              <div class="venue-content">
                <div class="venue-title">
                  <h3>{{ venue.name }}</h3>
                  <span class="small-tag">{{ venue.sportType }}</span>
                </div>
                <p><MapPin :size="15" />{{ venue.address }}</p>
                <p class="venue-description">
                  {{ venue.description || "适合开展校园运动活动" }}
                </p>
              </div>
              <div v-if="page === 'admin-venues'" class="venue-actions">
                <button
                  class="icon-button"
                  title="编辑场地"
                  @click="openVenue(venue)"
                >
                  <Pencil :size="17" /></button
                ><button
                  class="icon-button danger-text"
                  title="停用场地"
                  @click="disableVenue(venue)"
                >
                  <Trash2 :size="17" />
                </button>
              </div>
            </article></div
        ></template>
        <template v-else-if="page === 'notices' || page === 'admin-notices'"
          ><div class="page-head">
            <div>
              <p class="eyebrow">CAMPUS NEWS</p>
              <h1>{{ page === "admin-notices" ? "公告管理" : "校园公告" }}</h1>
              <p>及时了解平台和校园运动相关消息。</p>
            </div>
            <button
              v-if="page === 'admin-notices'"
              class="primary-button"
              @click="noticeOpen = true"
            >
              <Plus :size="17" />发布公告
            </button>
          </div>
          <div v-if="loading" class="state-panel">正在加载公告…</div>
          <div v-else-if="error" class="state-panel">{{ error }}</div>
          <div v-else-if="!notices.length" class="state-panel">
            <Megaphone :size="30" />
            <h3>暂无公告</h3>
          </div>
          <div v-else class="notice-list">
            <article
              v-for="notice in notices"
              :key="notice.id"
              class="notice-item"
            >
              <div class="notice-icon"><Megaphone :size="20" /></div>
              <div>
                <div class="notice-meta">
                  {{ dateTime(notice.createdAt) }} · {{ notice.adminName }}
                </div>
                <h2>{{ notice.title }}</h2>
                <p>{{ notice.content }}</p>
              </div>
            </article>
          </div></template
        >
        <template v-else-if="page === 'profile'"
          ><div class="page-head">
            <div>
              <p class="eyebrow">MY ACCOUNT</p>
              <h1>个人资料</h1>
              <p>管理你在绿茵结伴展示的基本信息。</p>
            </div>
          </div>
          <div class="profile-layout">
            <div class="profile-summary">
              <div class="profile-avatar">
                {{ auth.user?.nickname?.slice(0, 1) || "同" }}
              </div>
              <h2>{{ auth.user?.nickname }}</h2>
              <p>@{{ auth.user?.username }}</p>
              <span class="small-tag">{{ isAdmin ? "管理员" : "学生" }}</span>
            </div>
            <form class="profile-form" @submit.prevent="saveProfile">
              <h2>基本资料</h2>
              <label
                >用户名<input :value="auth.user?.username" disabled /></label
              ><label
                >昵称<input
                  v-model="profileForm.nickname"
                  required
                  maxlength="50" /></label
              ><label
                >联系电话<input
                  v-model="profileForm.phone"
                  maxlength="20"
                  placeholder="可选" /></label
              ><label
                >头像地址<input
                  v-model="profileForm.avatar"
                  maxlength="255"
                  placeholder="可选"
              /></label>
              <div class="profile-footer">
                <button class="primary-button" :disabled="saving">
                  <Check :size="17" />保存资料</button
                ><button
                  type="button"
                  class="outline-button danger-text"
                  @click="logout"
                >
                  <LogOut :size="17" />退出登录
                </button>
              </div>
            </form>
          </div></template
        >
      </main>
    </div>
    <div v-if="toast" class="toast" role="status">{{ toast }}</div>
    <div v-if="authOpen" class="modal-backdrop" @click.self="authOpen = false">
      <div class="modal small-modal">
        <div class="modal-head">
          <div>
            <h2>{{ authMode === "login" ? "欢迎回来" : "加入绿茵结伴" }}</h2>
            <p>
              {{
                authMode === "login"
                  ? "登录后加入活动、发布结伴。"
                  : "创建账号，开始寻找运动搭子。"
              }}
            </p>
          </div>
          <button class="icon-button" title="关闭" @click="authOpen = false">
            <X :size="20" />
          </button>
        </div>
        <div class="subtabs auth-tabs">
          <button
            :class="{ active: authMode === 'login' }"
            @click="authMode = 'login'"
          >
            登录</button
          ><button
            :class="{ active: authMode === 'register' }"
            @click="authMode = 'register'"
          >
            注册
          </button>
        </div>
        <form class="stack-form" @submit.prevent="submitAuth">
          <label
            >用户名<input
              v-model="authForm.username"
              required
              minlength="3"
              maxlength="50"
              autocomplete="username" /></label
          ><label v-if="authMode === 'register'"
            >昵称<input
              v-model="authForm.nickname"
              required
              maxlength="50" /></label
          ><label
            >密码<input
              v-model="authForm.password"
              required
              type="password"
              :minlength="authMode === 'register' ? 6 : 1"
              maxlength="72"
              :autocomplete="
                authMode === 'login' ? 'current-password' : 'new-password'
              " /></label
          ><button class="primary-button wide" :disabled="saving">
            {{
              saving ? "请稍候…" : authMode === "login" ? "登录" : "创建账号"
            }}
          </button>
        </form>
      </div>
    </div>
    <div
      v-if="activityOpen"
      class="modal-backdrop"
      @click.self="activityOpen = false"
    >
      <div class="modal">
        <div class="modal-head">
          <div>
            <h2>{{ activityForm.id ? "编辑结伴活动" : "发布结伴活动" }}</h2>
          </div>
          <button
            class="icon-button"
            title="关闭"
            @click="activityOpen = false"
          >
            <X :size="20" />
          </button>
        </div>
        <form class="stack-form" @submit.prevent="saveActivity">
          <label
            >活动标题<input
              v-model="activityForm.title"
              required
              maxlength="100"
              placeholder="例如：周三晚一起打羽毛球"
          /></label>
          <div class="form-grid">
            <label
              >运动类型<select v-model="activityForm.sportType" required>
                <option v-for="sport in sports.slice(1)" :key="sport">
                  {{ sport }}
                </option>
              </select></label
            ><label
              >人数上限<input
                v-model.number="activityForm.maxPeople"
                type="number"
                min="2"
                max="100"
                required
            /></label>
          </div>
          <label
            >运动场地<select v-model="activityForm.venueId">
              <option value="">暂不指定场地</option>
              <option v-for="venue in venues" :key="venue.id" :value="venue.id">
                {{ venue.name }} · {{ venue.address }}
              </option>
            </select></label
          >
          <div class="form-grid">
            <label
              >开始时间<input
                v-model="activityForm.startTime"
                type="datetime-local"
                required /></label
            ><label
              >结束时间<input
                v-model="activityForm.endTime"
                type="datetime-local"
                required
            /></label>
          </div>
          <label
            >活动说明<textarea
              v-model="activityForm.description"
              maxlength="1000"
              rows="4"
              placeholder="告诉同学们运动强度、集合方式等"
            ></textarea>
          </label>
          <div class="image-picker">
            <div class="image-picker-head"><strong>活动封面</strong><span>可使用系统图片、图片地址或本地图片</span></div>
            <div class="image-options">
              <button type="button" :class="{ selected: imageChoice === 'default' }" @click="setImageChoice('default')"><img :src="sportImage(activityForm.sportType)" alt="系统默认封面" /><span>按运动类型</span></button>
              <button v-for="option in ['/sports/football.jpg','/sports/badminton.jpg','/sports/basketball.jpg','/sports/running.jpg']" :key="option" type="button" :class="{ selected: imageChoice === option }" @click="setImageChoice(option)"><img :src="option" alt="系统封面" /><span>系统图片</span></button>
            </div>
            <div class="image-input-row"><label class="image-url-field">图片地址<input v-model="activityForm.imageUrl" placeholder="https://... 或 /sports/football.jpg" @focus="imageChoice = 'custom'" /></label><label class="file-button">选择本地图片<input type="file" accept="image/*" @change="onImageFile" /></label></div>
            <img class="image-preview" :src="activityPreview" alt="活动封面预览" />
          </div>
          <div class="form-actions">
            <button
              type="button"
              class="outline-button"
              @click="activityOpen = false"
            >
              取消</button
            ><button class="primary-button" :disabled="saving">
              {{
                saving ? "提交中…" : activityForm.id ? "保存修改" : "发布活动"
              }}
            </button>
          </div>
        </form>
      </div>
    </div>
    <div
      v-if="venueOpen"
      class="modal-backdrop"
      @click.self="venueOpen = false"
    >
      <div class="modal">
        <div class="modal-head">
          <h2>{{ venueForm.id ? "编辑场地" : "新增场地" }}</h2>
          <button class="icon-button" title="关闭" @click="venueOpen = false">
            <X :size="20" />
          </button>
        </div>
        <form class="stack-form" @submit.prevent="saveVenue">
          <label
            >场地名称<input
              v-model="venueForm.name"
              required
              maxlength="100" /></label
          ><label
            >场地地址<input
              v-model="venueForm.address"
              required
              maxlength="255" /></label
          ><label
            >适用运动<input
              v-model="venueForm.sportType"
              required
              maxlength="50"
              list="sport-options" /><datalist id="sport-options">
              <option
                v-for="sport in sports.slice(1)"
                :key="sport"
                :value="sport"
              ></option></datalist></label
          ><label
            >场地说明<textarea
              v-model="venueForm.description"
              maxlength="500"
              rows="3"
            ></textarea>
          </label>
          <div class="form-actions">
            <button
              type="button"
              class="outline-button"
              @click="venueOpen = false"
            >
              取消</button
            ><button class="primary-button" :disabled="saving">保存场地</button>
          </div>
        </form>
      </div>
    </div>
    <div
      v-if="noticeOpen"
      class="modal-backdrop"
      @click.self="noticeOpen = false"
    >
      <div class="modal">
        <div class="modal-head">
          <h2>发布校园公告</h2>
          <button class="icon-button" title="关闭" @click="noticeOpen = false">
            <X :size="20" />
          </button>
        </div>
        <form class="stack-form" @submit.prevent="saveNotice">
          <label
            >公告标题<input
              v-model="noticeForm.title"
              required
              maxlength="100" /></label
          ><label
            >公告内容<textarea
              v-model="noticeForm.content"
              required
              rows="7"
            ></textarea>
          </label>
          <div class="form-actions">
            <button
              type="button"
              class="outline-button"
              @click="noticeOpen = false"
            >
              取消</button
            ><button class="primary-button" :disabled="saving">发布公告</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
