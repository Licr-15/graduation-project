USE lvyin_sport;

-- Local demo only. The event manager account uses password 123456.
INSERT INTO `user` (username,password,nickname,role)
SELECT 'demo_event','$2a$10$.NC4Kg9UCmcXC4yVmBeNY.50vWR4XmPxPclpcdRdLiQThL0ZNq9WO','赛事管理员','EVENT_MANAGER'
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE username='demo_event');

INSERT INTO sport_competition(title,sport_type,description,start_time,end_time,points_reward,manager_id)
SELECT '校园羽毛球交流赛','羽毛球','双打交流赛，报名后在赛事当天上传现场照片打卡，获得 15 积分。',
  DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 2 DAY), INTERVAL 18 HOUR),
  DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 2 DAY), INTERVAL 20 HOUR),15,u.id
FROM `user` u WHERE u.username='demo_event'
AND NOT EXISTS (SELECT 1 FROM sport_competition WHERE title='校园羽毛球交流赛');

INSERT INTO sport_competition(title,sport_type,description,start_time,end_time,points_reward,manager_id)
SELECT '周末校园跑步挑战','跑步','在操场完成轻松跑，一起拍照留念并打卡。',
  DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 4 DAY), INTERVAL 8 HOUR),
  DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 4 DAY), INTERVAL 10 HOUR),10,u.id
FROM `user` u WHERE u.username='demo_event'
AND NOT EXISTS (SELECT 1 FROM sport_competition WHERE title='周末校园跑步挑战');

INSERT INTO community_post(user_id,post_type,content)
SELECT u.id,'TOPIC','本周想约一场羽毛球交流赛，大家更喜欢单打还是双打？'
FROM `user` u WHERE u.username='demo_event'
AND NOT EXISTS (SELECT 1 FROM community_post WHERE content='本周想约一场羽毛球交流赛，大家更喜欢单打还是双打？');

INSERT INTO community_post(user_id,post_type,content)
SELECT u.id,'DYNAMIC','今天跑了 5 公里，遇到了同样喜欢晨跑的同学。'
FROM `user` u WHERE u.username='demo_chen'
AND NOT EXISTS (SELECT 1 FROM community_post WHERE content='今天跑了 5 公里，遇到了同样喜欢晨跑的同学。');
