# 校园运动结伴服务平台

Spring Boot + Vue 3 毕业设计项目。

当前阶段：Spring Boot 后端与 Vue 3 前端已联调，可在本机运行。

## 目录

- `graduation-project-backend`：Spring Boot 后端
- `graduation-project-frontend`：Vue 3 + Vite 前端
- `design-preview`：前端设计预览及截图
- `毕设操作详细日志.md`：项目操作记录
- `毕设系统自测操作手册.md`：本机启动、网页操作与 Navicat 对照步骤

## 后端启动

1. 使用已有 MySQL 管理员账号执行 `graduation-project-backend/src/main/resources/db/01_create_database_user.sql`。
2. 以 `lcr/123456` 连接 `lvyin_sport`，依次执行 `02_schema.sql` 和 `03_seed.sql`。需要演示数据时再执行 `04_demo.sql`，该脚本可重复执行。
3. 在后端目录执行 `mvn spring-boot:run`。
4. 访问 `GET http://localhost:8080/api/system/health`。

## 前端启动

1. 在 `graduation-project-frontend` 目录执行 `npm install`。
2. 执行 `npm run dev`。
3. 浏览器打开 [http://localhost:5173](http://localhost:5173)。Vite 会把 `/api` 请求转发到本机 8080 端口的后端。

前端包含活动发现与搜索、活动详情、发布/编辑/取消、报名/取消报名、评论、我的活动、场地、公告、个人资料，以及管理员场地和公告管理。没有创建前端专用数据库表。

本机演示账号：学生 `demo_chen` / `123456`、`demo_lin` / `123456`。部署到公开环境前应更换所有演示账号密码，或不执行演示数据脚本。

前端构建：在前端目录执行 `npm run build`。已有演示数据时可执行 `npm run smoke` 做浏览器流程验证；脚本会清理它创建的临时数据。

运行效果：[活动发现（桌面）](graduation-project-frontend/screenshots/discover-desktop.png)、[活动发现（手机）](graduation-project-frontend/screenshots/discover-mobile.png)、[活动详情](graduation-project-frontend/screenshots/activity-detail.png)、[管理员场地管理](graduation-project-frontend/screenshots/admin-venues.png)。

## 默认管理员

初始化数据会创建系统管理员登录账号 `admin`，密码为 `123456`。该账号是业务登录账号；数据库连接账号是 `lcr`，两者完全独立。

## 核心接口

- `POST /api/auth/register` 注册学生
- `POST /api/auth/login` 登录并获得 JWT
- `GET/PUT /api/auth/profile` 查看或修改个人资料
- `GET /api/venues` 浏览场地
- `GET /api/activities` 搜索活动（支持 `keyword`、`sportType`、`page`、`size`）
- `GET /api/activities/{id}` 查看活动详情
- `POST /api/activities` 发布活动
- `PUT/DELETE /api/activities/{id}` 编辑或取消自己的活动
- `POST/DELETE /api/activities/{id}/join` 报名或取消报名
- `GET /api/activities/mine` 查看自己发布的活动
- `GET /api/activities/mine/joined` 查看自己已报名的活动
- `GET /api/comments/activity/{activityId}` 查看评论
- `POST /api/comments/activity/{activityId}` 发布评论
- `DELETE /api/comments/{id}` 删除自己的评论
- `POST/PUT/DELETE /api/admin/venues...` 管理场地
- `GET /api/notices` 查看公告
- `POST /api/admin/notices` 发布公告

## 页面图片

前端使用本地静态图片；羽毛球和乒乓球图片分别来自 Wikimedia Commons 的 [Olympics 2012 Mixed Doubles Final](https://commons.wikimedia.org/wiki/File:Olympics_2012_Mixed_Doubles_Final.jpg) 与 [Table-tennis-match-karlskrona-idrottshall](https://commons.wikimedia.org/wiki/File:Table-tennis-match-karlskrona-idrottshall.jpg)。篮球、足球、跑步图片来自 Unsplash。
