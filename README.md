# 校园运动结伴服务平台

Spring Boot + Vue 3 毕业设计项目。

当前阶段：后端基础工程。

## 目录

- `graduation-project-backend`：Spring Boot 后端
- `graduation-project-frontend`：后续创建的 Vue 3 前端
- `毕设操作详细日志.md`：项目操作记录

## 后端启动

1. 创建 MySQL 数据库 `graduation_project`。
2. 根据本机环境设置 `DB_USERNAME` 和 `DB_PASSWORD`。
3. 在后端目录执行 `mvn spring-boot:run`。
4. 访问 `GET http://localhost:8080/api/system/health`。
