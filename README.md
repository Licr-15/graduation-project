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
2. 以 `lcr/123456` 连接 `lvyin_sport`，依次执行 `02_schema.sql`、`03_seed.sql`、`05_activity_image.sql`、`06_competitions_community_points.sql`、`08_rank_stats_interaction.sql`、`09_avatar_image.sql`、`10_sport_checkin_rewards.sql`。需要演示数据时再执行 `04_demo.sql` 和 `07_demo_experience.sql`；演示脚本可重复执行。
3. 在后端目录执行 `mvn spring-boot:run`。
4. 访问 `GET http://localhost:8080/api/system/health`。

## 前端启动

1. 在 `graduation-project-frontend` 目录执行 `npm install`。
2. 执行 `npm run dev`。
3. 浏览器打开 [http://localhost:5173](http://localhost:5173)。Vite 会把 `/api` 请求转发到本机 8080 端口的后端。

前端包含活动发现与搜索、活动详情、发布/编辑/取消、报名/取消报名、评论、我的活动、场地、公告、个人资料，以及管理员场地、活动、公告管理。活动支持系统图片、图片地址和本地图片作为封面。

每日运动打卡：登录后在“每日打卡”填写运动类型、时长、距离，可选照片和感受；每人每天一次。系统管理员在“奖励规则”设置每日、连续天数或累计次数奖励，可停用和重新启用。奖励写入 `user_reward`，积分写入 `point_ledger`，在“我的积分”可查看。

赛事与社区功能：系统管理员在“角色管理”中授予赛事管理员角色；赛事管理员管理自己创建的赛事，系统管理员可管理所有赛事。学生报名赛事，并在赛事时间内上传照片打卡，积分进入排行榜。赛事管理员可以审核报名和录入名次/成绩。运动社区支持动态、话题、照片、回复、点赞、收藏和举报；系统管理员可处理举报。积分页面展示个人统计、勋章和按运动偏好推荐的活动。

新增赛事与社区：系统管理员在“角色管理”中将普通用户设为赛事管理员；赛事管理员登录后可发布、编辑及取消自己创建的赛事。学生报名赛事，赛事进行时上传现场照片打卡，每场只能打卡一次，积分进入“我的积分”。“运动社区”支持动态和论坛话题发帖、上传照片、回复及本人删除；系统管理员可删除帖子和回复。赛事时间未到时不可提前打卡。

活动页增加“随机挑一场”：按当前运动分类和搜索词，从未开始且仍有名额的活动中随机进入一场；活动卡片会区分招募中、已满员和已开始。

本机演示账号：学生 `demo_chen` / `123456`、`demo_lin` / `123456`；赛事管理员 `demo_event` / `123456`（执行 `07_demo_experience.sql` 后可用）。部署到公开环境前应更换所有演示账号密码，或不执行演示数据脚本。

前端构建：在前端目录执行 `npm run build`。已有演示数据时可执行 `npm run smoke` 做浏览器流程验证；执行 `node scripts/checkin-smoke.mjs` 验证打卡与奖励闭环；脚本会清理它创建的临时数据。

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
- `GET /api/points/leaderboard` 积分排行榜
- `GET /api/stats/mine` 个人运动统计
- `GET /api/recommendations` 运动偏好活动推荐
- `GET /api/competitions/{id}/participants`、报名审核及成绩管理接口
- `POST /api/community/posts/{id}/like`、`favorite`、`report` 社区互动接口

升级现有数据库时，依次运行尚未执行的迁移脚本 `05_activity_image.sql`、`06_competitions_community_points.sql`、`08_rank_stats_interaction.sql`、`09_avatar_image.sql` 和 `10_sport_checkin_rewards.sql`。赛事与社区演示数据可运行 `07_demo_experience.sql` 导入。
- `GET/POST /api/checkins/mine`、`/api/checkins` 查询与提交每日运动打卡
- `GET/POST/PUT/DELETE /api/admin/reward-rules...` 管理奖励规则；`PUT /api/admin/reward-rules/{id}/enable` 重新启用
- `GET/POST/PUT/DELETE /api/competitions...` 浏览与管理赛事
- `POST/DELETE /api/competitions/{id}/enroll` 报名与取消报名
- `POST /api/competitions/{id}/checkin` 上传照片打卡并累计积分
- `GET /api/points/mine` 查看积分流水和报名状态
- `GET/POST/DELETE /api/community/posts...` 社区动态与话题
- `GET/POST /api/community/posts/{id}/replies` 查看与发布回复
- `GET /api/admin/users`、`PUT /api/admin/users/{id}/role` 管理赛事管理员角色

## 页面图片

前端使用本地静态图片；羽毛球和乒乓球图片分别来自 Wikimedia Commons 的 [Olympics 2012 Mixed Doubles Final](https://commons.wikimedia.org/wiki/File:Olympics_2012_Mixed_Doubles_Final.jpg) 与 [Table-tennis-match-karlskrona-idrottshall](https://commons.wikimedia.org/wiki/File:Table-tennis-match-karlskrona-idrottshall.jpg)。篮球、足球、跑步图片来自 Unsplash。
