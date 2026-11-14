-- 将原“赛事管理员”并入系统管理员，系统最终只保留 STUDENT 和 ADMIN 两类角色。
-- 赛事管理接口仍只允许 ADMIN 访问，赛事管理员不再作为独立角色存在。
UPDATE `user`
SET role='ADMIN', nickname=CASE WHEN nickname='赛事管理员' THEN '系统管理员' ELSE nickname END
WHERE role='EVENT_MANAGER';
