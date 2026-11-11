USE lvyin_sport;
CREATE TABLE IF NOT EXISTS sport_checkin (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id BIGINT NOT NULL, sport_type VARCHAR(50) NOT NULL,
 checkin_date DATE NOT NULL, duration_minutes INT NOT NULL DEFAULT 0, distance_km DECIMAL(8,2) NOT NULL DEFAULT 0,
 image_url MEDIUMTEXT, note VARCHAR(500), created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE KEY uk_user_checkin_date(user_id,checkin_date), INDEX idx_checkin_user_date(user_id,checkin_date),
 CONSTRAINT fk_checkin_user FOREIGN KEY(user_id) REFERENCES `user`(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS reward_rule (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(100) NOT NULL, rule_type VARCHAR(30) NOT NULL,
 target_value INT NOT NULL, reward_points INT NOT NULL DEFAULT 0, badge_name VARCHAR(100),
 status TINYINT NOT NULL DEFAULT 1, created_by BIGINT NOT NULL, created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT fk_reward_rule_admin FOREIGN KEY(created_by) REFERENCES `user`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS user_reward (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id BIGINT NOT NULL, rule_id BIGINT NOT NULL,
 source_date DATE, award_key VARCHAR(20) NOT NULL, reward_points INT NOT NULL DEFAULT 0, badge_name VARCHAR(100), created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE KEY uk_user_rule_award(user_id,rule_id,award_key), CONSTRAINT fk_user_reward_user FOREIGN KEY(user_id) REFERENCES `user`(id) ON DELETE CASCADE,
 CONSTRAINT fk_user_reward_rule FOREIGN KEY(rule_id) REFERENCES reward_rule(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
SET @has_award_key := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='user_reward' AND column_name='award_key');
SET @award_sql := IF(@has_award_key=0, 'ALTER TABLE user_reward ADD COLUMN award_key VARCHAR(20) NOT NULL DEFAULT ''ONCE''', 'SELECT 1');
PREPARE award_stmt FROM @award_sql;
EXECUTE award_stmt;
DEALLOCATE PREPARE award_stmt;
SET @old_key := (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='user_reward' AND index_name='uk_user_rule');
SET @new_key := (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='user_reward' AND index_name='uk_user_rule_award');
SET @new_key_sql := IF(@new_key=0, 'ALTER TABLE user_reward ADD UNIQUE KEY uk_user_rule_award(user_id,rule_id,award_key)', 'SELECT 1');
PREPARE new_key_stmt FROM @new_key_sql;
EXECUTE new_key_stmt;
DEALLOCATE PREPARE new_key_stmt;
SET @key_sql := IF(@old_key>0, 'ALTER TABLE user_reward DROP INDEX uk_user_rule', 'SELECT 1');
PREPARE key_stmt FROM @key_sql;
EXECUTE key_stmt;
DEALLOCATE PREPARE key_stmt;
