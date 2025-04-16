package com.att.tdp.popcorn_palace.repository;

import com.att.tdp.popcorn_palace.model.Booking;
import com.att.tdp.popcorn_palace.model.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.showtime = :showtime AND b.seatNumber = :seatNumber")
    boolean existsByShowtimeAndSeatNumber(Showtime showtime, Integer seatNumber);
} 