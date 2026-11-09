import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "./stores/auth.js";

const RouteShell = { render: () => null };
const routes = [
  { path: "/", redirect: "/activities" },
  { path: "/activities", name: "activities", component: RouteShell },
  { path: "/activities/:id", name: "detail", component: RouteShell },
  { path: "/mine", name: "mine", component: RouteShell, meta: { auth: true } },
  { path: "/venues", name: "venues", component: RouteShell },
  { path: "/notices", name: "notices", component: RouteShell },
  {
    path: "/profile",
    name: "profile",
    component: RouteShell,
    meta: { auth: true },
  },
  {
    path: "/admin/venues",
    name: "admin-venues",
    component: RouteShell,
    meta: { admin: true },
  },
  {
    path: "/admin/activities",
    name: "admin-activities",
    component: RouteShell,
    meta: { admin: true },
  },
  {
    path: "/admin/notices",
    name: "admin-notices",
    component: RouteShell,
    meta: { admin: true },
  },
  { path: "/:pathMatch(.*)*", redirect: "/activities" },
];

const router = createRouter({ history: createWebHistory(), routes });
router.beforeEach((to) => {
  const auth = useAuthStore();
  if (to.meta.auth && !auth.token)
    return { name: "activities", query: { login: "1" } };
  if (to.meta.admin && auth.user?.role !== "ADMIN")
    return { name: "activities" };
});
export default router;
