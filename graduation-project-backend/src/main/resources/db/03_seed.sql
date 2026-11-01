USE lvyin_sport;
INSERT INTO `user` (username,password,nickname,role) SELECT 'admin', '$2a$10$.NC4Kg9UCmcXC4yVmBeNY.50vWR4XmPxPclpcdRdLiQThL0ZNq9WO', '系统管理员', 'ADMIN' WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE username='admin');
UPDATE `user` SET password='$2a$10$.NC4Kg9UCmcXC4yVmBeNY.50vWR4XmPxPclpcdRdLiQThL0ZNq9WO' WHERE username='admin' AND role='ADMIN';
INSERT INTO sport_venue (name,address,sport_type,description) SELECT '中心体育场','校园东区','足球','校园综合运动场地' WHERE NOT EXISTS (SELECT 1 FROM sport_venue WHERE name='中心体育场');
