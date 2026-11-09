package com.licr.graduation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.licr.graduation.persistence.ActivityRow;
import com.licr.graduation.vo.Views;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ActivityMapper extends BaseMapper<ActivityRow> {
    @Select("""
        SELECT a.id, a.publisher_id AS publisherId, u.nickname AS publisherName,
               a.venue_id AS venueId, v.name AS venueName, a.title, a.sport_type AS sportType,
               a.description, a.image_url AS imageUrl, a.start_time AS startTime, a.end_time AS endTime,
               a.max_people AS maxPeople,
               (SELECT COUNT(*) FROM activity_sign s WHERE s.activity_id=a.id AND s.status='JOINED') AS joinedCount,
               a.status, a.created_at AS createdAt
        FROM activity a JOIN `user` u ON u.id=a.publisher_id LEFT JOIN sport_venue v ON v.id=a.venue_id
        ORDER BY a.created_at DESC
        """)
    List<Views.Activity> all();
    @Select("SELECT id FROM activity WHERE id=#{id} FOR UPDATE")
    Long lock(@Param("id") Long id);

    @Select("""
        <script>
        SELECT a.id, a.publisher_id AS publisherId, u.nickname AS publisherName,
               a.venue_id AS venueId, v.name AS venueName, a.title,
               a.sport_type AS sportType, a.description, a.image_url AS imageUrl, a.start_time AS startTime,
               a.end_time AS endTime, a.max_people AS maxPeople,
               (SELECT COUNT(*) FROM activity_sign s WHERE s.activity_id=a.id AND s.status='JOINED') AS joinedCount,
               a.status, a.created_at AS createdAt
        FROM activity a JOIN `user` u ON u.id=a.publisher_id
        LEFT JOIN sport_venue v ON v.id=a.venue_id
        WHERE a.status='OPEN'
        <if test="keyword != null and keyword != ''">
          AND (a.title LIKE CONCAT('%',#{keyword},'%') OR a.description LIKE CONCAT('%',#{keyword},'%') OR v.name LIKE CONCAT('%',#{keyword},'%'))
        </if>
        <if test="sportType != null and sportType != ''">AND a.sport_type=#{sportType}</if>
        ORDER BY a.start_time ASC LIMIT #{size} OFFSET #{offset}
        </script>
        """)
    List<Views.Activity> search(@Param("keyword") String keyword, @Param("sportType") String sportType, @Param("size") int size, @Param("offset") int offset);

    @Select("""
        SELECT a.id, a.publisher_id AS publisherId, u.nickname AS publisherName,
               a.venue_id AS venueId, v.name AS venueName, a.title,
               a.sport_type AS sportType, a.description, a.image_url AS imageUrl, a.start_time AS startTime,
               a.end_time AS endTime, a.max_people AS maxPeople,
               (SELECT COUNT(*) FROM activity_sign s WHERE s.activity_id=a.id AND s.status='JOINED') AS joinedCount,
               a.status, a.created_at AS createdAt
        FROM activity a JOIN `user` u ON u.id=a.publisher_id
        LEFT JOIN sport_venue v ON v.id=a.venue_id
        WHERE a.publisher_id=#{userId} ORDER BY a.created_at DESC
        """)
    List<Views.Activity> mine(@Param("userId") Long userId);

    @Select("""
        SELECT a.id, a.publisher_id AS publisherId, u.nickname AS publisherName,
               a.venue_id AS venueId, v.name AS venueName, a.title,
               a.sport_type AS sportType, a.description, a.image_url AS imageUrl, a.start_time AS startTime,
               a.end_time AS endTime, a.max_people AS maxPeople,
               (SELECT COUNT(*) FROM activity_sign s WHERE s.activity_id=a.id AND s.status='JOINED') AS joinedCount,
               a.status, a.created_at AS createdAt
        FROM activity a JOIN `user` u ON u.id=a.publisher_id
        LEFT JOIN sport_venue v ON v.id=a.venue_id WHERE a.id=#{id}
        """)
    Views.Activity detail(@Param("id") Long id);

    @Select("""
        SELECT a.id, a.publisher_id AS publisherId, u.nickname AS publisherName,
               a.venue_id AS venueId, v.name AS venueName, a.title,
               a.sport_type AS sportType, a.description, a.image_url AS imageUrl, a.start_time AS startTime,
               a.end_time AS endTime, a.max_people AS maxPeople,
               (SELECT COUNT(*) FROM activity_sign x WHERE x.activity_id=a.id AND x.status='JOINED') AS joinedCount,
               a.status, a.created_at AS createdAt
        FROM activity_sign s JOIN activity a ON a.id=s.activity_id
        JOIN `user` u ON u.id=a.publisher_id
        LEFT JOIN sport_venue v ON v.id=a.venue_id
        WHERE s.user_id=#{userId} AND s.status='JOINED' ORDER BY a.start_time ASC
        """)
    List<Views.Activity> joined(@Param("userId") Long userId);
}
