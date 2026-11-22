# 后端接口清单

除注册、登录、健康检查、公开场地、公开活动、公开评论和公开公告外，其余接口需要请求头 `Authorization: Bearer <JWT>`。

| 模块 | 方法 | 路径 | 权限 |
|---|---|---|---|
|认证|POST|`/api/auth/register`|公开|
|认证|POST|`/api/auth/login`|公开|
|用户|GET/PUT|`/api/auth/profile`|登录|
|场地|GET|`/api/venues`|公开|
|场地|POST/PUT/DELETE|`/api/admin/venues`、`/api/admin/venues/{id}`|管理员|
|活动|GET|`/api/activities`|公开|
|活动|POST|`/api/activities`|登录|
|活动|PUT/DELETE|`/api/activities/{id}`|发布者|
|报名|POST/DELETE|`/api/activities/{id}/join`|登录|
|活动|GET|`/api/activities/mine`|登录|
|评论|GET|`/api/comments/activity/{activityId}`|公开|
|评论|POST|`/api/comments/activity/{activityId}`|登录|
|评论|DELETE|`/api/comments/{id}`|评论作者|
|公告|GET|`/api/notices`|公开|
|公告|POST|`/api/admin/notices`|管理员|
|系统|GET|`/api/system/health`|公开|

## 注册与联系方式

- `POST /api/auth/register` 接收 `username`、`password`、`nickname` 和 `contact`。`contact` 必须是中国大陆手机号或邮箱，系统会写入对应字段并检查唯一性。
- `POST /api/auth/login` 仍使用用户名和密码登录；手机号/邮箱只作为联系方式，不发送验证码，也不作为登录凭证。
- `GET/PUT /api/auth/profile` 返回并维护 `phone`、`email`。资料中两项都可选，填写时校验格式和唯一性；注册新账号时二者择一必填。
- 已有数据库执行 `src/main/resources/db/17_user_contact.sql`，新建数据库按编号顺序执行全部 SQL 脚本。
