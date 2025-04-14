package com.att.tdp.popcorn_palace.service;

import com.att.tdp.popcorn_palace.dto.BookingDTO;
import com.att.tdp.popcorn_palace.exception.booking.SeatAlreadyBookedException;
import com.att.tdp.popcorn_palace.exception.showtime.ShowtimeNotFoundException;
import com.att.tdp.popcorn_palace.model.Booking;
import com.att.tdp.popcorn_palace.model.Showtime;
import com.att.tdp.popcorn_palace.repository.BookingRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ShowtimeRepository showtimeRepository;

    @InjectMocks
    private BookingService bookingService;

    private BookingDTO bookingDTO;
    private Showtime showtime;
    private Booking savedBooking;
    private Validator validator;

    @BeforeEach
    void setUp() {
        bookingDTO = new BookingDTO(1L, 1, "user123");

        showtime = new Showtime();
        showtime.setId(1L);

        savedBooking = new Booking();
        savedBooking.setId(1L);
        savedBooking.setShowtime(showtime);
        savedBooking.setSeatNumber(1);
        savedBooking.setUserId("user123");

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateBookingSuccessfully() {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(bookingRepository.existsByShowtimeAndSeatNumber(showtime, 1)).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        Booking result = bookingService.createBooking(bookingDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(showtime, result.getShowtime());
        assertEquals(1, result.getSeatNumber());
        assertEquals("user123", result.getUserId());

        verify(showtimeRepository).findById(1L);
        verify(bookingRepository).existsByShowtimeAndSeatNumber(showtime, 1);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void shouldThrowShowtimeNotFoundException() {
        when(showtimeRepository.findById(999L)).thenReturn(Optional.empty());

        bookingDTO.setShowtimeId(999L);

        assertThrows(ShowtimeNotFoundException.class, () -> {
            bookingService.createBooking(bookingDTO);
        });

        verify(showtimeRepository).findById(999L);
        verify(bookingRepository, never()).existsByShowtimeAndSeatNumber(any(), any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void shouldThrowSeatAlreadyBookedException() {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(bookingRepository.existsByShowtimeAndSeatNumber(showtime, 1)).thenReturn(true);

        assertThrows(SeatAlreadyBookedException.class, () -> {
            bookingService.createBooking(bookingDTO);
        });

        verify(showtimeRepository).findById(1L);
        verify(bookingRepository).existsByShowtimeAndSeatNumber(showtime, 1);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void shouldFailValidationWhenSeatNumberIsNull() {
        bookingDTO.setSeatNumber(null);
        Set<ConstraintViolation<BookingDTO>> violations = validator.validate(bookingDTO);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("seatNumber")));
    }

    @Test
    void shouldFailValidationWhenUserIdIsNull() {
        bookingDTO.setUserId(null);
        Set<ConstraintViolation<BookingDTO>> violations = validator.validate(bookingDTO);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("userId")));
    }

    @Test
    void shouldFailValidationWhenSeatNumberIsZero() {
        bookingDTO.setSeatNumber(0);
        Set<ConstraintViolation<BookingDTO>> violations = validator.validate(bookingDTO);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("seatNumber")));
    }

    @Test
    void shouldFailValidationWhenSeatNumberIsNegative() {
        bookingDTO.setSeatNumber(-5);
        Set<ConstraintViolation<BookingDTO>> violations = validator.validate(bookingDTO);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("seatNumber")));
    }
}
