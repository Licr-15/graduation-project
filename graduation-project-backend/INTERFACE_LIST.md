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
