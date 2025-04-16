package com.att.tdp.popcorn_palace.service;

import com.att.tdp.popcorn_palace.dto.BookingDTO;
import com.att.tdp.popcorn_palace.exception.booking.SeatAlreadyBookedException;
import com.att.tdp.popcorn_palace.exception.showtime.ShowtimeNotFoundException;
import com.att.tdp.popcorn_palace.model.Booking;
import com.att.tdp.popcorn_palace.model.Showtime;
import com.att.tdp.popcorn_palace.repository.BookingRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.OptimisticLockException;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowtimeRepository showtimeRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository, ShowtimeRepository showtimeRepository) {
        this.bookingRepository = bookingRepository;
        this.showtimeRepository = showtimeRepository;
    }

    @Retryable(
        value = {OptimisticLockException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Booking createBooking(BookingDTO bookingDTO) {
        // First check if showtime exists
        Showtime showtime = showtimeRepository.findById(bookingDTO.getShowtimeId())
                .orElseThrow(() -> new ShowtimeNotFoundException(bookingDTO.getShowtimeId()));

        // Check seat availability
        if (bookingRepository.existsByShowtimeAndSeatNumber(showtime, bookingDTO.getSeatNumber())) {
            throw new SeatAlreadyBookedException(bookingDTO.getShowtimeId(), bookingDTO.getSeatNumber());
        }

        // Create and save the booking
        Booking booking = new Booking();
        booking.setShowtime(showtime);
        booking.setSeatNumber(bookingDTO.getSeatNumber());
        booking.setUserId(bookingDTO.getUserId());

        try {
            return bookingRepository.save(booking);
        } catch (DataIntegrityViolationException e) {
            // If we get a unique constraint violation, it means another thread beat us to it
            throw new SeatAlreadyBookedException(bookingDTO.getShowtimeId(), bookingDTO.getSeatNumber());
        } catch (OptimisticLockException e) {
            // If we get an optimistic lock exception, it means another thread modified the data
            throw new SeatAlreadyBookedException(bookingDTO.getShowtimeId(), bookingDTO.getSeatNumber());
        }
    }
} 