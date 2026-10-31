# 校园运动结伴服务平台

Spring Boot + Vue 3 毕业设计项目。

当前阶段：Spring Boot 后端完整业务版本。

## 目录

- `graduation-project-backend`：Spring Boot 后端
- `graduation-project-frontend`：后续创建的 Vue 3 前端
- `毕设操作详细日志.md`：项目操作记录

## 后端启动

1. 使用已有 MySQL 管理员账号执行 `graduation-project-backend/src/main/resources/db/01_create_database_user.sql`。
2. 以 `lcr/123456` 连接 `lvyin_sport`，依次执行 `02_schema.sql` 和 `03_seed.sql`。
3. 在后端目录执行 `mvn spring-boot:run`。
4. 访问 `GET http://localhost:8080/api/system/health`。

## 默认管理员

初始化数据会创建系统管理员登录账号 `admin`，密码为 `123456`。该账号是业务登录账号；数据库连接账号是 `lcr`，两者完全独立。

## 核心接口

- `POST /api/auth/register` 注册学生
- `POST /api/auth/login` 登录并获得 JWT
- `GET/PUT /api/auth/profile` 查看或修改个人资料
- `GET /api/venues` 浏览场地
- `GET /api/activities` 搜索活动（支持 `keyword`、`sportType`、`page`、`size`）
- `POST /api/activities` 发布活动
- `PUT/DELETE /api/activities/{id}` 编辑或取消自己的活动
- `POST/DELETE /api/activities/{id}/join` 报名或取消报名
- `GET /api/activities/mine` 查看自己发布的活动
- `GET /api/comments/activity/{activityId}` 查看评论
- `POST /api/comments/activity/{activityId}` 发布评论
- `DELETE /api/comments/{id}` 删除自己的评论
- `POST/PUT/DELETE /api/admin/venues...` 管理场地
- `GET /api/notices` 查看公告
- `POST /api/admin/notices` 发布公告
