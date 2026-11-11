USE lvyin_sport;
CREATE TABLE IF NOT EXISTS sport_checkin_policy (
 id TINYINT PRIMARY KEY,
 min_duration_minutes INT NOT NULL,
 updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT IGNORE INTO sport_checkin_policy(id,min_duration_minutes) VALUES(1,20);
INSERT INTO reward_rule(name,rule_type,target_value,reward_points,badge_name,created_by)
SELECT tiers.name,tiers.rule_type,tiers.target_value,tiers.reward_points,tiers.badge_name,admin.id
FROM (
 SELECT '连续运动 3 天' AS name,'STREAK' AS rule_type,3 AS target_value,10 AS reward_points,'初见坚持' AS badge_name
 UNION ALL SELECT '连续运动 7 天','STREAK',7,25,'一周坚持'
 UNION ALL SELECT '累计运动 5 次','TOTAL',5,15,'运动新星'
 UNION ALL SELECT '累计运动 15 次','TOTAL',15,40,'运动达人'
) AS tiers
JOIN `user` admin ON admin.username='admin'
WHERE NOT EXISTS (SELECT 1 FROM reward_rule existing WHERE existing.status=1 AND existing.rule_type=tiers.rule_type AND existing.target_value=tiers.target_value);
