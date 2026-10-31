-- 使用已有 MySQL 管理员账号执行。本脚本只负责创建应用数据库账号和数据库。
CREATE DATABASE IF NOT EXISTS lvyin_sport DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'lcr'@'localhost' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON lvyin_sport.* TO 'lcr'@'localhost';
FLUSH PRIVILEGES;
