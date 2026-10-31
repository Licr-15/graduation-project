package com.licr.graduation.repository;

import com.licr.graduation.entity.SportVenueEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class VenueRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<SportVenueEntity> mapper = (r,n) -> new SportVenueEntity(r.getLong("id"),r.getString("name"),r.getString("address"),r.getString("sport_type"),r.getString("description"),r.getInt("status"),r.getTimestamp("created_at").toLocalDateTime());
    public VenueRepository(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    public List<SportVenueEntity> list() { return jdbc.query("SELECT * FROM sport_venue WHERE status=1 ORDER BY id DESC",mapper); }
    public Optional<SportVenueEntity> find(Long id) { return jdbc.query("SELECT * FROM sport_venue WHERE id=?",mapper,id).stream().findFirst(); }
    public long insert(String name,String address,String type,String description) { jdbc.update("INSERT INTO sport_venue(name,address,sport_type,description) VALUES(?,?,?,?)",name,address,type,description); return jdbc.queryForObject("SELECT LAST_INSERT_ID()",Long.class); }
    public void update(Long id,String name,String address,String type,String description) { jdbc.update("UPDATE sport_venue SET name=?,address=?,sport_type=?,description=? WHERE id=?",name,address,type,description,id); }
    public void disable(Long id) { jdbc.update("UPDATE sport_venue SET status=0 WHERE id=?",id); }
}
