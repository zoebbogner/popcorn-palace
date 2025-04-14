package com.att.tdp.popcorn_palace.service;

import com.att.tdp.popcorn_palace.dto.BookingDTO;
import com.att.tdp.popcorn_palace.exception.booking.SeatAlreadyBookedException;
import com.att.tdp.popcorn_palace.exception.showtime.ShowtimeNotFoundException;
import com.att.tdp.popcorn_palace.model.Booking;
import com.att.tdp.popcorn_palace.model.Showtime;
import com.att.tdp.popcorn_palace.repository.BookingRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowtimeRepository showtimeRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository, ShowtimeRepository showtimeRepository) {
        this.bookingRepository = bookingRepository;
        this.showtimeRepository = showtimeRepository;
    }

    @Transactional
    public Booking createBooking(BookingDTO bookingDTO) {
        // First check if showtime exists
        Showtime showtime = showtimeRepository.findById(bookingDTO.getShowtimeId())
                .orElseThrow(() -> new ShowtimeNotFoundException(bookingDTO.getShowtimeId()));

        // Only check seat availability if showtime exists
        if (bookingRepository.existsByShowtimeAndSeatNumber(showtime, bookingDTO.getSeatNumber())) {
            throw new SeatAlreadyBookedException(bookingDTO.getShowtimeId(), bookingDTO.getSeatNumber());
        }

        // Create and save the booking
        Booking booking = new Booking();
        booking.setShowtime(showtime);
        booking.setSeatNumber(bookingDTO.getSeatNumber());
        booking.setUserId(bookingDTO.getUserId());

        return bookingRepository.save(booking);
    }
} 