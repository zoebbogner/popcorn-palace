package com.att.tdp.popcorn_palace.repository;

import com.att.tdp.popcorn_palace.model.Booking;
import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.model.Showtime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class BookingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;

    private Showtime showtime;

    @BeforeEach
    void setUp() {
        // Create and persist a Movie
        Movie movie = new Movie();
        movie.setTitle("Test Movie");
        movie.setGenre("Genre");
        movie.setDuration(120);
        movie.setRating(8.0);
        movie.setReleaseYear(2022);
        movie = entityManager.persist(movie);

        // Create and persist a Showtime
        showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setStartTime(ZonedDateTime.now().plusHours(1));
        showtime.setEndTime(ZonedDateTime.now().plusHours(2));
        showtime.setPrice(10.0);
        showtime.setTheater("Test Theater");
        showtime = entityManager.persist(showtime);
    }

    @Test
    void shouldFindBookingById() {
        // Given
        Booking booking = new Booking();
        booking.setShowtime(showtime);
        booking.setSeatNumber(1);
        booking.setUserId("user123");
        booking = entityManager.persist(booking);

        // When
        Optional<Booking> found = bookingRepository.findById(booking.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(booking.getId(), found.get().getId());
        assertEquals(showtime.getId(), found.get().getShowtime().getId());
        assertEquals(1, found.get().getSeatNumber());
        assertEquals("user123", found.get().getUserId());
    }

    @Test
    void shouldCheckIfSeatIsAlreadyBooked() {
        // Given
        Booking booking = new Booking();
        booking.setShowtime(showtime);
        booking.setSeatNumber(1);
        booking.setUserId("user123");
        entityManager.persist(booking);

        // When
        boolean exists = bookingRepository.existsByShowtimeAndSeatNumber(showtime, 1);

        // Then
        assertTrue(exists);
    }

    @Test
    void shouldNotFindNonExistentBooking() {
        // When
        Optional<Booking> found = bookingRepository.findById(999L);

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void shouldNotFindBookingForDifferentSeat() {
        // Given
        Booking booking = new Booking();
        booking.setShowtime(showtime);
        booking.setSeatNumber(1);
        booking.setUserId("user123");
        entityManager.persist(booking);

        // When
        boolean exists = bookingRepository.existsByShowtimeAndSeatNumber(showtime, 2);

        // Then
        assertFalse(exists);
    }

    @Test
    void shouldNotFindBookingForDifferentShowtime() {
        // Given
        Booking booking = new Booking();
        booking.setShowtime(showtime);
        booking.setSeatNumber(1);
        booking.setUserId("user123");
        entityManager.persist(booking);

        // Create a different showtime
        Showtime differentShowtime = new Showtime();
        differentShowtime.setMovie(showtime.getMovie());
        differentShowtime.setStartTime(ZonedDateTime.now().plusHours(3));
        differentShowtime.setEndTime(ZonedDateTime.now().plusHours(4));
        differentShowtime.setPrice(10.0);
        differentShowtime.setTheater("Test Theater");
        differentShowtime = entityManager.persist(differentShowtime);

        // When
        boolean exists = bookingRepository.existsByShowtimeAndSeatNumber(differentShowtime, 1);

        // Then
        assertFalse(exists);
    }
} 