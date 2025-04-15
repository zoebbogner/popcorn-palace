package com.att.tdp.popcorn_palace.integration;

import com.att.tdp.popcorn_palace.dto.BookingDTO;
import com.att.tdp.popcorn_palace.model.Booking;
import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.model.Showtime;
import com.att.tdp.popcorn_palace.repository.BookingRepository;
import com.att.tdp.popcorn_palace.repository.MovieRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookingApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private MovieRepository movieRepository;

    private Showtime showtime;
    private BookingDTO validBookingDTO;
    private BookingDTO invalidBookingDTO;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        showtimeRepository.deleteAll();
        movieRepository.deleteAll();

        // Create and persist a movie
        Movie movie = new Movie();
        movie.setTitle("Inception");
        movie.setGenre("Sci-Fi");
        movie.setDuration(148);
        movie.setRating(8.8);
        movie.setReleaseYear(2010);
        movie = movieRepository.save(movie);

        // Create and persist a showtime
        showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setTheater("Theater 1");
        showtime.setStartTime(ZonedDateTime.now().plusHours(1));
        showtime.setEndTime(ZonedDateTime.now().plusHours(3));
        showtime.setPrice(12.99);
        showtime = showtimeRepository.save(showtime);

        // Create valid booking DTO
        validBookingDTO = new BookingDTO();
        validBookingDTO.setShowtimeId(showtime.getId());
        validBookingDTO.setSeatNumber(1);
        validBookingDTO.setUserId("user123");

        // Create invalid booking DTO
        invalidBookingDTO = new BookingDTO();
        invalidBookingDTO.setShowtimeId(showtime.getId());
        invalidBookingDTO.setSeatNumber(0); // Invalid: seat number must be positive
        invalidBookingDTO.setUserId(null); // Invalid: user ID is required
    }

    @Test
    void shouldCreateBookingSuccessfully() throws Exception {
        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validBookingDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").isNumber());
    }

    @Test
    void shouldReturnBadRequestForInvalidInput() throws Exception {
        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBookingDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.seatNumber").exists())
                .andExpect(jsonPath("$.userId").exists());
    }

    @Test
    void shouldReturnNotFoundWhenShowtimeDoesNotExist() throws Exception {
        validBookingDTO.setShowtimeId(999L);
        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validBookingDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnConflictWhenSeatIsAlreadyBooked() throws Exception {
        // Given
        Booking existingBooking = new Booking();
        existingBooking.setShowtime(showtime);
        existingBooking.setSeatNumber(1);
        existingBooking.setUserId("user456");
        bookingRepository.save(existingBooking);

        // When & Then
        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validBookingDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());
    }
} 