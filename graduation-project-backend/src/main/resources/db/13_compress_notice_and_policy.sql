-- One-time data preserving migration. Run after 12_compress_community_interaction.sql.
USE lvyin_sport;

ALTER TABLE community_post
  ADD COLUMN title VARCHAR(100) NULL AFTER post_type,
  ADD COLUMN post_status TINYINT NOT NULL DEFAULT 1 AFTER image_url;

ALTER TABLE reward_rule ADD COLUMN min_duration_minutes INT NOT NULL DEFAULT 20;

UPDATE reward_rule r
JOIN sport_checkin_policy p ON p.id=1
SET r.min_duration_minutes=p.min_duration_minutes;

INSERT INTO community_post(user_id,post_type,title,content,created_at,post_status)
SELECT admin_id,'NOTICE',title,content,created_at,status
FROM sys_notice;

DROP TABLE sport_checkin_policy;
DROP TABLE sys_notice;
