-- One-time data preserving migration for databases created before this file.
-- Run after 06_competitions_community_points.sql and 08_rank_stats_interaction.sql.
USE lvyin_sport;

CREATE TABLE IF NOT EXISTS community_interaction (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  interaction_type VARCHAR(20) NOT NULL,
  reason VARCHAR(255),
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_community_interaction(post_id,user_id,interaction_type),
  INDEX idx_community_interaction_type(interaction_type,created_at),
  CONSTRAINT fk_community_interaction_post FOREIGN KEY (post_id) REFERENCES community_post(id) ON DELETE CASCADE,
  CONSTRAINT fk_community_interaction_user FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO community_interaction(post_id,user_id,interaction_type,created_at)
SELECT post_id,user_id,'LIKE',created_at FROM community_post_like;

INSERT IGNORE INTO community_interaction(post_id,user_id,interaction_type,created_at)
SELECT post_id,user_id,'FAVORITE',created_at FROM community_post_favorite;

INSERT IGNORE INTO community_interaction(post_id,user_id,interaction_type,reason,status,created_at)
SELECT post_id,user_id,'REPORT',reason,status,created_at FROM community_report;

DROP TABLE community_post_like;
DROP TABLE community_post_favorite;
DROP TABLE community_report;
