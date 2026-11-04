USE lvyin_sport;

-- Local demonstration data. Student demo accounts use password 123456.
INSERT INTO `user` (username, password, nickname, role)
SELECT 'demo_chen', '$2a$10$.NC4Kg9UCmcXC4yVmBeNY.50vWR4XmPxPclpcdRdLiQThL0ZNq9WO', '陈同学', 'STUDENT'
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE username = 'demo_chen');
INSERT INTO `user` (username, password, nickname, role)
SELECT 'demo_lin', '$2a$10$.NC4Kg9UCmcXC4yVmBeNY.50vWR4XmPxPclpcdRdLiQThL0ZNq9WO', '林同学', 'STUDENT'
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE username = 'demo_lin');

INSERT INTO sport_venue (name, address, sport_type, description)
SELECT '综合体育馆', '校园北区体育馆一层', '羽毛球', '室内羽毛球场地，适合双打和日常练习'
WHERE NOT EXISTS (SELECT 1 FROM sport_venue WHERE name = '综合体育馆');
INSERT INTO sport_venue (name, address, sport_type, description)
SELECT '中心篮球场', '校园东区体育场旁', '篮球', '室外篮球场，适合半场交流赛'
WHERE NOT EXISTS (SELECT 1 FROM sport_venue WHERE name = '中心篮球场');
INSERT INTO sport_venue (name, address, sport_type, description)
SELECT '东区操场', '校园东区教学楼旁', '跑步', '环形跑道，适合晨跑和轻松跑'
WHERE NOT EXISTS (SELECT 1 FROM sport_venue WHERE name = '东区操场');

INSERT INTO activity (publisher_id, venue_id, title, sport_type, description, start_time, end_time, max_people)
SELECT u.id, v.id, '周三晚一起打羽毛球', '羽毛球', '轻松双打，欢迎新手加入。提前十分钟到场热身。',
       DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 2 DAY), INTERVAL 19 HOUR),
       DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 2 DAY), INTERVAL 21 HOUR), 4
FROM `user` u JOIN sport_venue v ON v.name = '综合体育馆' WHERE u.username = 'demo_chen'
AND NOT EXISTS (SELECT 1 FROM activity WHERE title = '周三晚一起打羽毛球');
INSERT INTO activity (publisher_id, venue_id, title, sport_type, description, start_time, end_time, max_people)
SELECT u.id, v.id, '篮球半场，差两位队友', '篮球', '水平不限，打球交流为主。带好运动鞋和饮水。',
       DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 3 DAY), INTERVAL 16 HOUR),
       DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 3 DAY), INTERVAL 18 HOUR), 8
FROM `user` u JOIN sport_venue v ON v.name = '中心篮球场' WHERE u.username = 'demo_lin'
AND NOT EXISTS (SELECT 1 FROM activity WHERE title = '篮球半场，差两位队友');
INSERT INTO activity (publisher_id, venue_id, title, sport_type, description, start_time, end_time, max_people)
SELECT u.id, v.id, '周末操场轻松跑 5 公里', '跑步', '慢速跑，边跑边聊，任何配速都欢迎。',
       DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 4 DAY), INTERVAL 7 HOUR),
       DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 4 DAY), INTERVAL 8 HOUR), 10
FROM `user` u JOIN sport_venue v ON v.name = '东区操场' WHERE u.username = 'demo_chen'
AND NOT EXISTS (SELECT 1 FROM activity WHERE title = '周末操场轻松跑 5 公里');

INSERT INTO activity_sign (activity_id, user_id, status)
SELECT a.id, u.id, 'JOINED' FROM activity a JOIN `user` u ON u.username = 'demo_lin'
WHERE a.title = '周三晚一起打羽毛球'
AND NOT EXISTS (SELECT 1 FROM activity_sign s WHERE s.activity_id = a.id AND s.user_id = u.id);
INSERT INTO `comment` (activity_id, user_id, content)
SELECT a.id, u.id, '周三见，第一次参加也可以一起打！' FROM activity a JOIN `user` u ON u.username = 'demo_lin'
WHERE a.title = '周三晚一起打羽毛球'
AND NOT EXISTS (SELECT 1 FROM `comment` c WHERE c.activity_id = a.id AND c.user_id = u.id AND c.content = '周三见，第一次参加也可以一起打！');
INSERT INTO sys_notice (title, content, admin_id)
SELECT '欢迎来到绿茵结伴', '发布一场运动，或加入同学的活动，一起去球场和操场。', u.id
FROM `user` u WHERE u.username = 'admin'
AND NOT EXISTS (SELECT 1 FROM sys_notice WHERE title = '欢迎来到绿茵结伴');
