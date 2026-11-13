package com.licr.graduation.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class CheckinRewardService implements ApplicationRunner {
    private final JdbcTemplate db;

    public CheckinRewardService(JdbcTemplate db) { this.db = db; }

    public void award(long userId, Map<String,Object> rule, LocalDate date) {
        String awardKey = "DAILY".equals(rule.get("rule_type")) ? date.toString() : "ONCE";
        db.update("INSERT IGNORE INTO user_reward(user_id,rule_id,source_date,award_key,reward_points,badge_name) VALUES(?,?,?,?,?,?)",
            userId,rule.get("id"),date,awardKey,rule.get("reward_points"),rule.get("badge_name"));
        Long rewardId = db.queryForObject("SELECT id FROM user_reward WHERE user_id=? AND rule_id=? AND award_key=?",Long.class,userId,rule.get("id"),awardKey);
        db.update("INSERT IGNORE INTO point_ledger(user_id,points,reason,source_type,source_id) VALUES(?,?,?,?,?)",
            userId,rule.get("reward_points"),rule.get("name")+"奖励","SPORT_REWARD",rewardId);
    }

    @Transactional
    public void reconcileDailyRule(long ruleId) {
        List<Map<String,Object>> rules=db.queryForList("SELECT * FROM reward_rule WHERE id=? AND status=1 AND rule_type='DAILY' AND target_value=1",ruleId);
        if (rules.isEmpty()) return;
        Map<String,Object> rule=rules.get(0);
        int minimum=db.queryForObject("SELECT min_duration_minutes FROM reward_rule ORDER BY id LIMIT 1",Integer.class);
        List<Map<String,Object>> checkins=db.queryForList("SELECT user_id,checkin_date FROM sport_checkin WHERE checkin_date>=DATE(?) AND duration_minutes>=? ORDER BY id",
            rule.get("created_at"),minimum);
        for (Map<String,Object> checkin : checkins) {
            award(((Number)checkin.get("user_id")).longValue(),rule,((java.sql.Date)checkin.get("checkin_date")).toLocalDate());
        }
    }

    @Override
    public void run(ApplicationArguments args) {
        List<Long> ids=db.query("SELECT id FROM reward_rule WHERE status=1 AND rule_type='DAILY' AND target_value=1",(rs,row)->rs.getLong(1));
        for (Long id : ids) reconcileDailyRule(id);
    }
}
