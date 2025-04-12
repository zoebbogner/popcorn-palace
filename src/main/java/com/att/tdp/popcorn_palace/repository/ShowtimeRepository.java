package com.att.tdp.popcorn_palace.repository;

import com.att.tdp.popcorn_palace.model.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
    @Query("SELECT s FROM Showtime s WHERE s.theater = :theater " +
           "AND s.startTime < :endTime AND s.endTime > :startTime")
    List<Showtime> findOverlappingShowtimes(
            @Param("theater") String theater,
            @Param("startTime") ZonedDateTime startTime,
            @Param("endTime") ZonedDateTime endTime
    );
} 