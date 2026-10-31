package com.licr.graduation.repository;

import com.licr.graduation.entity.ActivityEntity;
import com.licr.graduation.vo.Views;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ActivityRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<ActivityEntity> mapper = (r,n) -> new ActivityEntity(r.getLong("id"),r.getLong("publisher_id"),r.getObject("venue_id",Long.class),r.getString("title"),r.getString("sport_type"),r.getString("description"),r.getTimestamp("start_time").toLocalDateTime(),r.getTimestamp("end_time").toLocalDateTime(),r.getInt("max_people"),r.getString("status"),r.getTimestamp("created_at").toLocalDateTime());
    public ActivityRepository(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    public List<Views.Activity> search(String keyword,String sportType,int page,int size) {
        StringBuilder sql=new StringBuilder("SELECT a.*,u.nickname publisher_name,v.name venue_name,(SELECT COUNT(*) FROM activity_sign s WHERE s.activity_id=a.id AND s.status='JOINED') joined_count FROM activity a JOIN `user` u ON u.id=a.publisher_id LEFT JOIN sport_venue v ON v.id=a.venue_id WHERE a.status='OPEN'");
        List<Object> args=new ArrayList<>();
        if(keyword!=null&&!keyword.isBlank()){sql.append(" AND (a.title LIKE ? OR a.description LIKE ? OR v.name LIKE ?)");String k="%"+keyword.trim()+"%";args.add(k);args.add(k);args.add(k);}
        if(sportType!=null&&!sportType.isBlank()){sql.append(" AND a.sport_type=?");args.add(sportType.trim());}
        sql.append(" ORDER BY a.start_time ASC LIMIT ? OFFSET ?");args.add(size);args.add((page-1)*size);
        return jdbc.query(sql.toString(),(r,n)->new Views.Activity(r.getLong("id"),r.getLong("publisher_id"),r.getString("publisher_name"),r.getObject("venue_id",Long.class),r.getString("venue_name"),r.getString("title"),r.getString("sport_type"),r.getString("description"),r.getTimestamp("start_time").toLocalDateTime(),r.getTimestamp("end_time").toLocalDateTime(),r.getInt("max_people"),r.getInt("joined_count"),r.getString("status"),r.getTimestamp("created_at").toLocalDateTime()),args.toArray());
    }
    public Optional<ActivityEntity> find(Long id) { return jdbc.query("SELECT * FROM activity WHERE id=?",mapper,id).stream().findFirst(); }
    public long insert(Long publisherId,Long venueId,String title,String type,String description,java.time.LocalDateTime start,java.time.LocalDateTime end,int max) { jdbc.update("INSERT INTO activity(publisher_id,venue_id,title,sport_type,description,start_time,end_time,max_people) VALUES(?,?,?,?,?,?,?,?)",publisherId,venueId,title,type,description,start,end,max);return jdbc.queryForObject("SELECT LAST_INSERT_ID()",Long.class); }
    public void update(Long id,Long venueId,String title,String type,String description,java.time.LocalDateTime start,java.time.LocalDateTime end,int max) { jdbc.update("UPDATE activity SET venue_id=?,title=?,sport_type=?,description=?,start_time=?,end_time=?,max_people=? WHERE id=?",venueId,title,type,description,start,end,max,id); }
    public void cancel(Long id) { jdbc.update("UPDATE activity SET status='CANCELLED' WHERE id=?",id); }
    public int countJoined(Long id) { return jdbc.queryForObject("SELECT COUNT(*) FROM activity_sign WHERE activity_id=? AND status='JOINED'",Integer.class,id); }
    public int lockAndCount(Long id) { jdbc.queryForObject("SELECT id FROM activity WHERE id=? FOR UPDATE",Long.class,id); return countJoined(id); }
    public void join(Long activityId,Long userId) { jdbc.update("INSERT INTO activity_sign(activity_id,user_id,status) VALUES(?,?,'JOINED') ON DUPLICATE KEY UPDATE status='JOINED'",activityId,userId); }
    public void leave(Long activityId,Long userId) { jdbc.update("UPDATE activity_sign SET status='CANCELLED' WHERE activity_id=? AND user_id=?",activityId,userId); }
    public List<Views.Activity> mine(Long userId) { return jdbc.query("SELECT a.*,u.nickname publisher_name,v.name venue_name,(SELECT COUNT(*) FROM activity_sign s WHERE s.activity_id=a.id AND s.status='JOINED') joined_count FROM activity a JOIN `user` u ON u.id=a.publisher_id LEFT JOIN sport_venue v ON v.id=a.venue_id WHERE a.publisher_id=? ORDER BY a.created_at DESC",(r,n)->new Views.Activity(r.getLong("id"),r.getLong("publisher_id"),r.getString("publisher_name"),r.getObject("venue_id",Long.class),r.getString("venue_name"),r.getString("title"),r.getString("sport_type"),r.getString("description"),r.getTimestamp("start_time").toLocalDateTime(),r.getTimestamp("end_time").toLocalDateTime(),r.getInt("max_people"),r.getInt("joined_count"),r.getString("status"),r.getTimestamp("created_at").toLocalDateTime()),userId); }
}
