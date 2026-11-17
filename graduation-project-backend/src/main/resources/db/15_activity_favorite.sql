USE lvyin_sport;

CREATE TABLE IF NOT EXISTS activity_favorite (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  activity_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_activity_favorite(activity_id,user_id),
  INDEX idx_favorite_user_created(user_id,created_at),
  CONSTRAINT fk_favorite_activity FOREIGN KEY(activity_id) REFERENCES activity(id) ON DELETE CASCADE,
  CONSTRAINT fk_favorite_user FOREIGN KEY(user_id) REFERENCES `user`(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
