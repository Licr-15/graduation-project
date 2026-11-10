USE lvyin_sport;

ALTER TABLE competition_sign
  ADD COLUMN approval_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' AFTER status,
  ADD COLUMN result_rank INT AFTER approval_status,
  ADD COLUMN result_score DECIMAL(8,2) AFTER result_rank;

CREATE TABLE IF NOT EXISTS community_post_like (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_post_like(post_id,user_id),
  CONSTRAINT fk_post_like_post FOREIGN KEY (post_id) REFERENCES community_post(id) ON DELETE CASCADE,
  CONSTRAINT fk_post_like_user FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS community_post_favorite (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_post_favorite(post_id,user_id),
  CONSTRAINT fk_post_favorite_post FOREIGN KEY (post_id) REFERENCES community_post(id) ON DELETE CASCADE,
  CONSTRAINT fk_post_favorite_user FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS community_report (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  reason VARCHAR(255) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_post_report(post_id,user_id),
  CONSTRAINT fk_post_report_post FOREIGN KEY (post_id) REFERENCES community_post(id) ON DELETE CASCADE,
  CONSTRAINT fk_post_report_user FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
