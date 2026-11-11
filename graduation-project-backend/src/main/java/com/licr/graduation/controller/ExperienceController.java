package com.licr.graduation.controller;

import com.licr.graduation.common.BusinessException;
import com.licr.graduation.common.Result;
import com.licr.graduation.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.support.GeneratedKeyHolder;

@RestController
@RequestMapping("/api")
public class ExperienceController {
    private final JdbcTemplate db;
    public ExperienceController(JdbcTemplate db) { this.db = db; }

    public record CompetitionInput(@NotBlank @Size(max=100) String title, @NotBlank @Size(max=50) String sportType,
        @Size(max=1000) String description, @NotNull LocalDateTime startTime, @NotNull LocalDateTime endTime,
        @Min(1) @Max(100) int pointsReward) {}
    public record Proof(@NotBlank String imageUrl) {}
    public record PostInput(@NotBlank @Size(max=1000) String content, @Pattern(regexp="DYNAMIC|TOPIC") String postType, String imageUrl) {}
    public record ReplyInput(@NotBlank @Size(max=500) String content) {}
    public record RoleInput(@Pattern(regexp="STUDENT|EVENT_MANAGER") String role) {}
    public record ResultInput(@Min(1) @Max(9999) int rank, @DecimalMin("0") @DecimalMax("10000") double score) {}
    public record ReportInput(@NotBlank @Size(max=255) String reason) {}
    public record CheckinInput(@NotBlank @Size(max=50) String sportType, @Min(0) @Max(1440) int durationMinutes, @DecimalMin("0") @DecimalMax("999") double distanceKm, String imageUrl, @Size(max=500) String note) {}
    public record RuleInput(@NotBlank @Size(max=100) String name, @NotNull @Pattern(regexp="DAILY|STREAK|TOTAL") String ruleType, @Min(1) @Max(3650) int targetValue, @Min(0) @Max(10000) int rewardPoints, @Size(max=100) String badgeName) {}

    private CurrentUser user(HttpServletRequest request) { return AuthController.user(request); }
    private void manager(CurrentUser user) {
        String role = db.queryForObject("SELECT role FROM `user` WHERE id=? AND status=1", String.class, user.id());
        if (!"ADMIN".equals(role) && !"EVENT_MANAGER".equals(role)) throw new BusinessException("需要赛事管理员权限");
    }
    private void ownCompetition(CurrentUser user, long id) {
        manager(user);
        if ("ADMIN".equals(user.role())) return;
        Integer count = db.queryForObject("SELECT COUNT(*) FROM sport_competition WHERE id=? AND manager_id=?", Integer.class, id, user.id());
        if (count == null || count == 0) throw new BusinessException("只能管理自己创建的赛事");
    }
    private void checkCompetition(CompetitionInput input) {
        if (!input.endTime().isAfter(input.startTime())) throw new BusinessException("结束时间必须晚于开始时间");
    }
    private LocalDateTime time(Object value) {
        return value instanceof LocalDateTime local ? local : ((java.sql.Timestamp) value).toLocalDateTime();
    }
    private long insert(String sql, Object... args) {
        GeneratedKeyHolder key = new GeneratedKeyHolder();
        db.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < args.length; i++) statement.setObject(i + 1, args[i]);
            return statement;
        }, key);
        return key.getKey().longValue();
    }
    private void admin(CurrentUser u) { if (!"ADMIN".equals(u.role())) throw new BusinessException("需要管理员权限"); }
    private int streak(long userId) {
        List<LocalDate> dates = db.query("SELECT checkin_date FROM sport_checkin WHERE user_id=? ORDER BY checkin_date DESC", (rs, row) -> rs.getDate(1).toLocalDate(), userId);
        LocalDate expected = LocalDate.now();
        if (!dates.isEmpty() && !dates.get(0).equals(expected)) expected = expected.minusDays(1);
        int count = 0;
        for (LocalDate date : dates) {
            if (!date.equals(expected)) break;
            count++;
            expected = expected.minusDays(1);
        }
        return count;
    }
    private void validateRule(RuleInput input) {
        if ("DAILY".equals(input.ruleType()) && input.targetValue() != 1) throw new BusinessException("每日打卡规则的达成目标必须为 1");
    }

    @GetMapping("/checkins/mine")
    public Result<?> checkins(HttpServletRequest request) {
        long id=user(request).id();
        return Result.ok(Map.of("today", db.queryForList("SELECT id,sport_type AS sportType,checkin_date AS checkinDate,duration_minutes AS durationMinutes,distance_km AS distanceKm,image_url AS imageUrl,note FROM sport_checkin WHERE user_id=? AND checkin_date=CURDATE()",id),
            "records",db.queryForList("SELECT id,sport_type AS sportType,checkin_date AS checkinDate,duration_minutes AS durationMinutes,distance_km AS distanceKm,note FROM sport_checkin WHERE user_id=? ORDER BY checkin_date DESC LIMIT 60",id),
            "streak",streak(id),
            "total",db.queryForObject("SELECT COUNT(*) FROM sport_checkin WHERE user_id=?",Integer.class,id),
            "rewards",db.queryForList("SELECT r.name,ur.reward_points AS rewardPoints,ur.badge_name AS badgeName,ur.created_at AS createdAt FROM user_reward ur JOIN reward_rule r ON r.id=ur.rule_id WHERE ur.user_id=? ORDER BY ur.created_at DESC",id)));
    }
    @PostMapping("/checkins")
    @Transactional
    public Result<?> createCheckin(HttpServletRequest request,@Valid @RequestBody CheckinInput input) {
        long id=user(request).id();
        if(input.imageUrl()!=null && !input.imageUrl().isBlank() && (!input.imageUrl().matches("(?s)^data:image/(jpeg|png|webp);base64,[A-Za-z0-9+/=]+$") || input.imageUrl().length()>2800000)) throw new BusinessException("请选择不超过 2MB 的 JPG、PNG 或 WebP 照片");
        try { db.update("INSERT INTO sport_checkin(user_id,sport_type,checkin_date,duration_minutes,distance_km,image_url,note) VALUES(?,?,CURDATE(),?,?,?,?)",id,input.sportType(),input.durationMinutes(),input.distanceKm(),input.imageUrl(),input.note()); }
        catch(DuplicateKeyException e) { throw new BusinessException("今天已经打卡，每天只能打卡一次"); }
        Integer total=db.queryForObject("SELECT COUNT(*) FROM sport_checkin WHERE user_id=?",Integer.class,id);
        int currentStreak=streak(id);
        List<Map<String,Object>> rules=db.queryForList("SELECT * FROM reward_rule WHERE status=1 AND ((rule_type='DAILY' AND target_value=1) OR (rule_type='STREAK' AND target_value<=?) OR (rule_type='TOTAL' AND target_value<=?))",currentStreak,total);
        for (Map<String,Object> r : rules) {
            String awardKey = "DAILY".equals(r.get("rule_type")) ? LocalDate.now().toString() : "ONCE";
            int inserted = db.update("INSERT IGNORE INTO user_reward(user_id,rule_id,source_date,award_key,reward_points,badge_name) VALUES(?,?,CURDATE(),?,?,?)", id,r.get("id"),awardKey,r.get("reward_points"),r.get("badge_name"));
            if (inserted == 1) {
                Long rewardId = db.queryForObject("SELECT id FROM user_reward WHERE user_id=? AND rule_id=? AND award_key=?",Long.class,id,r.get("id"),awardKey);
                db.update("INSERT INTO point_ledger(user_id,points,reason,source_type,source_id) VALUES(?,?,?,?,?)",id,r.get("reward_points"),r.get("name")+"奖励","SPORT_REWARD",rewardId);
            }
        }
        return Result.ok(null);
    }
    @GetMapping("/admin/reward-rules")
    public Result<?> rules(HttpServletRequest request) { admin(user(request)); return Result.ok(db.queryForList("SELECT id,name,rule_type AS ruleType,target_value AS targetValue,reward_points AS rewardPoints,badge_name AS badgeName,status FROM reward_rule ORDER BY target_value")); }
    @PostMapping("/admin/reward-rules")
    public Result<?> createRule(HttpServletRequest request,@Valid @RequestBody RuleInput input) { CurrentUser u=user(request); admin(u); validateRule(input); return Result.ok(insert("INSERT INTO reward_rule(name,rule_type,target_value,reward_points,badge_name,created_by) VALUES(?,?,?,?,?,?)",input.name(),input.ruleType(),input.targetValue(),input.rewardPoints(),input.badgeName(),u.id())); }
    @PutMapping("/admin/reward-rules/{id}")
    public Result<?> updateRule(HttpServletRequest request,@PathVariable long id,@Valid @RequestBody RuleInput input) { admin(user(request)); validateRule(input); db.update("UPDATE reward_rule SET name=?,rule_type=?,target_value=?,reward_points=?,badge_name=? WHERE id=?",input.name(),input.ruleType(),input.targetValue(),input.rewardPoints(),input.badgeName(),id); return Result.ok(null); }
    @DeleteMapping("/admin/reward-rules/{id}")
    public Result<?> deleteRule(HttpServletRequest request,@PathVariable long id) { admin(user(request)); db.update("UPDATE reward_rule SET status=0 WHERE id=?",id); return Result.ok(null); }
    @PutMapping("/admin/reward-rules/{id}/enable")
    public Result<?> enableRule(HttpServletRequest request,@PathVariable long id) { admin(user(request)); db.update("UPDATE reward_rule SET status=1 WHERE id=?",id); return Result.ok(null); }

    @GetMapping("/competitions")
    public Result<?> competitions() {
        return Result.ok(db.queryForList("""
            SELECT c.id,c.title,c.sport_type AS sportType,c.description,c.start_time AS startTime,
              c.end_time AS endTime,c.points_reward AS pointsReward,c.status,c.manager_id AS managerId,
              u.nickname AS managerName,(SELECT COUNT(*) FROM competition_sign s WHERE s.competition_id=c.id AND s.status IN ('ENROLLED','CHECKED') AND s.approval_status<>'REJECTED') AS enrolledCount
            FROM sport_competition c JOIN `user` u ON u.id=c.manager_id ORDER BY c.start_time DESC
            """));
    }
    @PostMapping("/competitions")
    public Result<?> createCompetition(HttpServletRequest request, @Valid @RequestBody CompetitionInput input) {
        CurrentUser u = user(request); manager(u); checkCompetition(input);
        return Result.ok(insert("INSERT INTO sport_competition(title,sport_type,description,start_time,end_time,points_reward,manager_id) VALUES(?,?,?,?,?,?,?)",
            input.title(),input.sportType(),input.description(),input.startTime(),input.endTime(),input.pointsReward(),u.id()));
    }
    @PutMapping("/competitions/{id}")
    public Result<?> updateCompetition(HttpServletRequest request,@PathVariable long id,@Valid @RequestBody CompetitionInput input) {
        ownCompetition(user(request),id); checkCompetition(input);
        if (db.update("UPDATE sport_competition SET title=?,sport_type=?,description=?,start_time=?,end_time=?,points_reward=? WHERE id=? AND status='OPEN'",
            input.title(),input.sportType(),input.description(),input.startTime(),input.endTime(),input.pointsReward(),id)==0) throw new BusinessException("赛事不存在或已取消");
        return Result.ok(null);
    }
    @DeleteMapping("/competitions/{id}")
    public Result<?> cancelCompetition(HttpServletRequest request,@PathVariable long id) {
        ownCompetition(user(request),id);
        db.update("UPDATE sport_competition SET status='CANCELLED' WHERE id=?",id);
        return Result.ok(null);
    }
    @PostMapping("/competitions/{id}/enroll")
    public Result<?> enroll(HttpServletRequest request,@PathVariable long id) {
        CurrentUser u=user(request);
        List<Map<String,Object>> rows=db.queryForList("SELECT status,start_time FROM sport_competition WHERE id=?",id);
        if (rows.isEmpty() || !"OPEN".equals(rows.get(0).get("status"))) throw new BusinessException("赛事不可报名");
        if (!time(rows.get(0).get("start_time")).isAfter(LocalDateTime.now())) throw new BusinessException("赛事已开始");
        try { db.update("INSERT INTO competition_sign(competition_id,user_id,approval_status) VALUES(?,?,'PENDING') ON DUPLICATE KEY UPDATE status=IF(status='CANCELLED','ENROLLED',status),approval_status=IF(status='CANCELLED','PENDING',approval_status)",id,u.id()); }
        catch (DuplicateKeyException e) { throw new BusinessException("已经报名"); }
        return Result.ok(null);
    }
    @DeleteMapping("/competitions/{id}/enroll")
    public Result<?> withdraw(HttpServletRequest request,@PathVariable long id) {
        db.update("UPDATE competition_sign SET status='CANCELLED' WHERE competition_id=? AND user_id=? AND status='ENROLLED'",id,user(request).id());
        return Result.ok(null);
    }
    @PostMapping("/competitions/{id}/checkin")
    @Transactional
    public Result<?> checkin(HttpServletRequest request,@PathVariable long id,@Valid @RequestBody Proof proof) {
        CurrentUser u=user(request);
        if (!proof.imageUrl().matches("(?s)^data:image/(jpeg|png|webp);base64,.+" ) || proof.imageUrl().length()>2800000)
            throw new BusinessException("请上传不超过 2MB 的 JPG、PNG 或 WebP 照片");
        List<Map<String,Object>> rows=db.queryForList("SELECT status,start_time,end_time,points_reward FROM sport_competition WHERE id=?",id);
        if(rows.isEmpty() || !"OPEN".equals(rows.get(0).get("status"))) throw new BusinessException("赛事不可打卡");
        LocalDateTime now=LocalDateTime.now();
        LocalDateTime start=time(rows.get(0).get("start_time"));
        LocalDateTime end=time(rows.get(0).get("end_time"));
        if(now.isBefore(start) || now.isAfter(end)) throw new BusinessException("请在赛事期间打卡");
        if(db.update("UPDATE competition_sign SET status='CHECKED',proof_image=?,checked_at=NOW() WHERE competition_id=? AND user_id=? AND status='ENROLLED' AND approval_status<>'REJECTED'",
            proof.imageUrl(),id,u.id())==0) throw new BusinessException("请先报名，且每场赛事只能打卡一次");
        db.update("INSERT INTO point_ledger(user_id,points,reason,source_type,source_id) VALUES(?,?,'赛事照片打卡','COMPETITION',?)",
            u.id(),rows.get(0).get("points_reward"),id);
        return Result.ok(null);
    }
    @GetMapping("/points/mine")
    public Result<?> points(HttpServletRequest request) {
        long id=user(request).id();
        return Result.ok(Map.of("total",db.queryForObject("SELECT COALESCE(SUM(points),0) FROM point_ledger WHERE user_id=?",Integer.class,id),
            "records",db.queryForList("SELECT id,points,reason,created_at AS createdAt FROM point_ledger WHERE user_id=? ORDER BY created_at DESC",id),
            "enrollments",db.queryForList("SELECT competition_id AS competitionId,status,approval_status AS approvalStatus,proof_image IS NOT NULL AS checked FROM competition_sign WHERE user_id=?",id),
            "badges",badges(id)));
    }
    private List<Map<String,Object>> badges(long userId) {
        Integer checked=db.queryForObject("SELECT COUNT(*) FROM competition_sign WHERE user_id=? AND status='CHECKED'",Integer.class,userId);
        Integer joined=db.queryForObject("SELECT COUNT(*) FROM activity_sign WHERE user_id=? AND status='JOINED'",Integer.class,userId);
        List<Map<String,Object>> result=new java.util.ArrayList<>();
        if(checked!=null && checked>=1) result.add(Map.of("name","首次打卡","icon","📸","description","完成第一次赛事照片打卡"));
        if(checked!=null && checked>=3) result.add(Map.of("name","打卡达人","icon","🏅","description","完成 3 次赛事照片打卡"));
        if(joined!=null && joined>=5) result.add(Map.of("name","运动积极分子","icon","🏃","description","参加 5 场结伴活动"));
        return result;
    }
    @GetMapping("/points/leaderboard")
    public Result<?> leaderboard() { return Result.ok(db.queryForList("SELECT u.id,u.nickname,COALESCE(SUM(p.points),0) AS totalPoints,COUNT(p.id) AS checkinCount FROM `user` u LEFT JOIN point_ledger p ON p.user_id=u.id WHERE u.status=1 GROUP BY u.id,u.nickname ORDER BY totalPoints DESC,u.id ASC LIMIT 50")); }
    @GetMapping("/stats/mine")
    public Result<?> stats(HttpServletRequest request) {
        long id=user(request).id();
        return Result.ok(Map.of("activityCount",db.queryForObject("SELECT COUNT(*) FROM activity_sign WHERE user_id=? AND status='JOINED'",Integer.class,id),
            "createdCount",db.queryForObject("SELECT COUNT(*) FROM activity WHERE publisher_id=?",Integer.class,id),
            "competitionCount",db.queryForObject("SELECT COUNT(*) FROM competition_sign WHERE user_id=? AND status IN ('ENROLLED','CHECKED')",Integer.class,id),
            "checkinCount",db.queryForObject("SELECT COUNT(*) FROM competition_sign WHERE user_id=? AND status='CHECKED'",Integer.class,id),
            "sportBreakdown",db.queryForList("SELECT sport_type AS sportType,COUNT(*) AS count FROM activity a JOIN activity_sign s ON s.activity_id=a.id WHERE s.user_id=? AND s.status='JOINED' GROUP BY sport_type ORDER BY count DESC",id)));
    }
    @GetMapping("/recommendations")
    public Result<?> recommendations(HttpServletRequest request) {
        long id=user(request).id();
        return Result.ok(db.queryForList("""
            SELECT a.id,a.publisher_id AS publisherId,u.nickname AS publisherName,a.venue_id AS venueId,v.name AS venueName,a.title,a.sport_type AS sportType,a.description,a.image_url AS imageUrl,a.start_time AS startTime,a.end_time AS endTime,a.max_people AS maxPeople,
              (SELECT COUNT(*) FROM activity_sign x WHERE x.activity_id=a.id AND x.status='JOINED') AS joinedCount,a.status,a.created_at AS createdAt
            FROM activity a JOIN `user` u ON u.id=a.publisher_id LEFT JOIN sport_venue v ON v.id=a.venue_id
            WHERE a.status='OPEN' AND a.start_time>NOW() AND a.publisher_id<>? AND a.sport_type IN (SELECT a2.sport_type FROM activity a2 JOIN activity_sign s2 ON s2.activity_id=a2.id WHERE s2.user_id=? GROUP BY a2.sport_type)
            ORDER BY a.start_time ASC LIMIT 6
            """,id,id));
    }
    private void competitionManager(CurrentUser u,long id) { ownCompetition(u,id); }
    @GetMapping("/competitions/{id}/participants")
    public Result<?> participants(HttpServletRequest request,@PathVariable long id) { competitionManager(user(request),id); return Result.ok(db.queryForList("SELECT s.id,s.user_id AS userId,u.nickname,u.username,s.status,s.approval_status AS approvalStatus,s.proof_image IS NOT NULL AS checked,s.result_rank AS resultRank,s.result_score AS resultScore FROM competition_sign s JOIN `user` u ON u.id=s.user_id WHERE s.competition_id=? ORDER BY s.created_at",id)); }
    @PutMapping("/competitions/{competitionId}/participants/{signId}/approval")
    public Result<?> approval(HttpServletRequest request,@PathVariable long competitionId,@PathVariable long signId,@RequestParam String status) { competitionManager(user(request),competitionId); if(!List.of("APPROVED","REJECTED","PENDING").contains(status)) throw new BusinessException("报名审核状态无效"); db.update("UPDATE competition_sign SET approval_status=? WHERE id=? AND competition_id=?",status,signId,competitionId); return Result.ok(null); }
    @PutMapping("/competitions/{competitionId}/participants/{signId}/result")
    public Result<?> result(HttpServletRequest request,@PathVariable long competitionId,@PathVariable long signId,@Valid @RequestBody ResultInput input) { competitionManager(user(request),competitionId); db.update("UPDATE competition_sign SET result_rank=?,result_score=? WHERE id=? AND competition_id=?",input.rank(),input.score(),signId,competitionId); return Result.ok(null); }
    @GetMapping("/community/posts")
    public Result<?> posts() { return Result.ok(db.queryForList("""
        SELECT p.id,p.user_id AS userId,u.nickname,p.post_type AS postType,p.content,p.image_url AS imageUrl,p.created_at AS createdAt,
          (SELECT COUNT(*) FROM community_comment c WHERE c.post_id=p.id) AS replyCount,
          (SELECT COUNT(*) FROM community_post_like l WHERE l.post_id=p.id) AS likeCount,
          (SELECT COUNT(*) FROM community_post_favorite f WHERE f.post_id=p.id) AS favoriteCount
        FROM community_post p JOIN `user` u ON u.id=p.user_id ORDER BY p.created_at DESC LIMIT 100
        """)); }
    @PostMapping("/community/posts")
    public Result<?> createPost(HttpServletRequest request,@Valid @RequestBody PostInput input) {
        if(input.imageUrl()!=null && !input.imageUrl().isBlank() &&
          (!input.imageUrl().matches("(?s)^data:image/(jpeg|png|webp);base64,.+") || input.imageUrl().length()>2800000))
            throw new BusinessException("请上传不超过 2MB 的 JPG、PNG 或 WebP 照片");
        return Result.ok(insert("INSERT INTO community_post(user_id,post_type,content,image_url) VALUES(?,?,?,?)",
            user(request).id(),input.postType()==null?"DYNAMIC":input.postType(),input.content(),input.imageUrl()));
    }
    @DeleteMapping("/community/posts/{id}")
    public Result<?> deletePost(HttpServletRequest request,@PathVariable long id) {
        CurrentUser u=user(request);
        if(db.update("DELETE FROM community_post WHERE id=? AND (user_id=? OR ?='ADMIN')",id,u.id(),u.role())==0)
            throw new BusinessException("只能删除自己的动态");
        return Result.ok(null);
    }
    @GetMapping("/community/posts/{id}/replies")
    public Result<?> replies(@PathVariable long id) {
        return Result.ok(db.queryForList("SELECT c.id,c.user_id AS userId,u.nickname,c.content,c.created_at AS createdAt FROM community_comment c JOIN `user` u ON u.id=c.user_id WHERE c.post_id=? ORDER BY c.created_at ASC",id));
    }
    @PostMapping("/community/posts/{id}/replies")
    public Result<?> reply(HttpServletRequest request,@PathVariable long id,@Valid @RequestBody ReplyInput input) {
        if(db.queryForObject("SELECT COUNT(*) FROM community_post WHERE id=?",Integer.class,id)==0) throw new BusinessException("帖子不存在");
        return Result.ok(insert("INSERT INTO community_comment(post_id,user_id,content) VALUES(?,?,?)",id,user(request).id(),input.content()));
    }
    @DeleteMapping("/community/replies/{id}")
    public Result<?> deleteReply(HttpServletRequest request,@PathVariable long id) {
        CurrentUser u=user(request);
        if(db.update("DELETE FROM community_comment WHERE id=? AND (user_id=? OR ?='ADMIN')",id,u.id(),u.role())==0)
            throw new BusinessException("只能删除自己的回复");
        return Result.ok(null);
    }
    @PostMapping("/community/posts/{id}/like")
    public Result<?> like(HttpServletRequest request,@PathVariable long id) { toggle("community_post_like",id,user(request).id()); return Result.ok(null); }
    @PostMapping("/community/posts/{id}/favorite")
    public Result<?> favorite(HttpServletRequest request,@PathVariable long id) { toggle("community_post_favorite",id,user(request).id()); return Result.ok(null); }
    private void toggle(String table,long postId,long userId) { int count=db.queryForObject("SELECT COUNT(*) FROM "+table+" WHERE post_id=? AND user_id=?",Integer.class,postId,userId); if(count==0) db.update("INSERT INTO "+table+"(post_id,user_id) VALUES(?,?)",postId,userId); else db.update("DELETE FROM "+table+" WHERE post_id=? AND user_id=?",postId,userId); }
    @PostMapping("/community/posts/{id}/report")
    public Result<?> report(HttpServletRequest request,@PathVariable long id,@Valid @RequestBody ReportInput input) { try { db.update("INSERT INTO community_report(post_id,user_id,reason) VALUES(?,?,?)",id,user(request).id(),input.reason()); } catch(DuplicateKeyException e) { throw new BusinessException("你已经举报过这条动态"); } return Result.ok(null); }
    @GetMapping("/admin/community/reports")
    public Result<?> reports(HttpServletRequest request) { if(!"ADMIN".equals(user(request).role())) throw new BusinessException("需要管理员权限"); return Result.ok(db.queryForList("SELECT r.id,r.post_id AS postId,r.reason,r.status,r.created_at AS createdAt,u.nickname,p.content FROM community_report r JOIN `user` u ON u.id=r.user_id JOIN community_post p ON p.id=r.post_id ORDER BY r.created_at DESC")); }
    @PutMapping("/admin/community/reports/{id}")
    public Result<?> reportStatus(HttpServletRequest request,@PathVariable long id,@RequestParam String status) { if(!"ADMIN".equals(user(request).role())) throw new BusinessException("需要管理员权限"); if(!List.of("PENDING","RESOLVED","IGNORED").contains(status)) throw new BusinessException("举报状态无效"); db.update("UPDATE community_report SET status=? WHERE id=?",status,id); return Result.ok(null); }
    @GetMapping("/admin/users")
    public Result<?> users() { return Result.ok(db.queryForList("SELECT id,username,nickname,role FROM `user` ORDER BY id")); }
    @PutMapping("/admin/users/{id}/role")
    public Result<?> role(@PathVariable long id,@Valid @RequestBody RoleInput input) {
        if(db.update("UPDATE `user` SET role=? WHERE id=? AND role<>'ADMIN'",input.role(),id)==0) throw new BusinessException("用户不存在或无法修改管理员角色");
        return Result.ok(null);
    }
}
