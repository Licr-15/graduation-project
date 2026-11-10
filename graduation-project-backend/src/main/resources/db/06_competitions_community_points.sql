USE lvyin_sport;

CREATE TABLE IF NOT EXISTS sport_competition (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  sport_type VARCHAR(50) NOT NULL,
  description VARCHAR(1000),
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  points_reward INT NOT NULL DEFAULT 10,
  status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
  manager_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_competition_start(start_time),
  CONSTRAINT fk_competition_manager FOREIGN KEY (manager_id) REFERENCES `user`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS competition_sign (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  competition_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ENROLLED',
  proof_image MEDIUMTEXT,
  checked_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_competition_user(competition_id,user_id),
  CONSTRAINT fk_competition_sign_competition FOREIGN KEY (competition_id) REFERENCES sport_competition(id) ON DELETE CASCADE,
  CONSTRAINT fk_competition_sign_user FOREIGN KEY (user_id) REFERENCES `user`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS point_ledger (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  points INT NOT NULL,
  reason VARCHAR(100) NOT NULL,
  source_type VARCHAR(30) NOT NULL,
  source_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_points_source(user_id,source_type,source_id),
  INDEX idx_points_user_created(user_id,created_at),
  CONSTRAINT fk_points_user FOREIGN KEY (user_id) REFERENCES `user`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS community_post (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  post_type VARCHAR(20) NOT NULL DEFAULT 'DYNAMIC',
  content VARCHAR(1000) NOT NULL,
  image_url MEDIUMTEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_community_created(created_at),
  CONSTRAINT fk_post_user FOREIGN KEY (user_id) REFERENCES `user`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS community_comment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  content VARCHAR(500) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_community_comment(post_id,created_at),
  CONSTRAINT fk_community_comment_post FOREIGN KEY (post_id) REFERENCES community_post(id) ON DELETE CASCADE,
  CONSTRAINT fk_community_comment_user FOREIGN KEY (user_id) REFERENCES `user`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
