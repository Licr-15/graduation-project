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

    @GetMapping("/competitions")
    public Result<?> competitions() {
        return Result.ok(db.queryForList("""
            SELECT c.id,c.title,c.sport_type AS sportType,c.description,c.start_time AS startTime,
              c.end_time AS endTime,c.points_reward AS pointsReward,c.status,c.manager_id AS managerId,
              u.nickname AS managerName,(SELECT COUNT(*) FROM competition_sign s WHERE s.competition_id=c.id AND s.status IN ('ENROLLED','CHECKED')) AS enrolledCount
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
        try { db.update("INSERT INTO competition_sign(competition_id,user_id) VALUES(?,?) ON DUPLICATE KEY UPDATE status=IF(status='CANCELLED','ENROLLED',status)",id,u.id()); }
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
        if(db.update("UPDATE competition_sign SET status='CHECKED',proof_image=?,checked_at=NOW() WHERE competition_id=? AND user_id=? AND status='ENROLLED'",
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
            "enrollments",db.queryForList("SELECT competition_id AS competitionId,status,proof_image IS NOT NULL AS checked FROM competition_sign WHERE user_id=?",id)));
    }
    @GetMapping("/community/posts")
    public Result<?> posts() { return Result.ok(db.queryForList("""
        SELECT p.id,p.user_id AS userId,u.nickname,p.post_type AS postType,p.content,p.image_url AS imageUrl,p.created_at AS createdAt,
          (SELECT COUNT(*) FROM community_comment c WHERE c.post_id=p.id) AS replyCount
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
    @GetMapping("/admin/users")
    public Result<?> users() { return Result.ok(db.queryForList("SELECT id,username,nickname,role FROM `user` ORDER BY id")); }
    @PutMapping("/admin/users/{id}/role")
    public Result<?> role(@PathVariable long id,@Valid @RequestBody RoleInput input) {
        if(db.update("UPDATE `user` SET role=? WHERE id=? AND role<>'ADMIN'",input.role(),id)==0) throw new BusinessException("用户不存在或无法修改管理员角色");
        return Result.ok(null);
    }
}
