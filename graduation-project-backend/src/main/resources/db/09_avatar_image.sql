USE lvyin_sport;
SET @avatar_type := (SELECT DATA_TYPE FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user' AND column_name = 'avatar');
SET @avatar_sql := IF(@avatar_type = 'mediumtext', 'SELECT 1', 'ALTER TABLE `user` MODIFY COLUMN avatar MEDIUMTEXT NULL');
PREPARE avatar_stmt FROM @avatar_sql;
EXECUTE avatar_stmt;
DEALLOCATE PREPARE avatar_stmt;
