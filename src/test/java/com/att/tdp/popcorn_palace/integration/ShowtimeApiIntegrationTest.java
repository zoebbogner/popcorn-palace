package com.att.tdp.popcorn_palace.integration;

import com.att.tdp.popcorn_palace.dto.ShowtimeDTO;
import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.model.Showtime;
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
class ShowtimeApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private MovieRepository movieRepository;

    private Movie movie;
    private ShowtimeDTO validShowtimeDTO;
    private ShowtimeDTO invalidShowtimeDTO;

    @BeforeEach
    void setUp() {
        showtimeRepository.deleteAll();
        movieRepository.deleteAll();

        // Create and persist a movie
        movie = new Movie();
        movie.setTitle("Inception");
        movie.setGenre("Sci-Fi");
        movie.setDuration(148);
        movie.setRating(8.8);
        movie.setReleaseYear(2010);
        movie = movieRepository.save(movie);

        // Create valid showtime DTO
        validShowtimeDTO = new ShowtimeDTO();
        validShowtimeDTO.setMovieId(movie.getId());
        validShowtimeDTO.setTheater("Theater 1");
        validShowtimeDTO.setStartTime(ZonedDateTime.now().plusHours(1));
        validShowtimeDTO.setEndTime(ZonedDateTime.now().plusHours(3));
        validShowtimeDTO.setPrice(12.99);

        // Create invalid showtime DTO
        invalidShowtimeDTO = new ShowtimeDTO();
        invalidShowtimeDTO.setMovieId(movie.getId());
        invalidShowtimeDTO.setTheater(""); // Invalid: empty theater
        invalidShowtimeDTO.setStartTime(ZonedDateTime.now().minusHours(1)); // Invalid: past start time
        invalidShowtimeDTO.setEndTime(ZonedDateTime.now().minusHours(2)); // Invalid: past end time
        invalidShowtimeDTO.setPrice(-1.0); // Invalid: negative price
    }

    @Test
    void shouldGetShowtimeSuccessfully() throws Exception {
        // Given
        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setTheater("Theater 1");
        showtime.setStartTime(ZonedDateTime.now().plusHours(1));
        showtime.setEndTime(ZonedDateTime.now().plusHours(3));
        showtime.setPrice(12.99);
        showtime = showtimeRepository.save(showtime);

        // When & Then
        mockMvc.perform(get("/showtimes/" + showtime.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(showtime.getId().intValue())))
                .andExpect(jsonPath("$.theater", is("Theater 1")))
                .andExpect(jsonPath("$.price", is(12.99)));
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentShowtime() throws Exception {
        mockMvc.perform(get("/showtimes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldAddShowtimeSuccessfully() throws Exception {
        mockMvc.perform(post("/showtimes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validShowtimeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.theater", is("Theater 1")))
                .andExpect(jsonPath("$.price", is(12.99)));
    }

    @Test
    void shouldReturnConflictOnOverlappingShowtime() throws Exception {
        // Given
        Showtime existingShowtime = new Showtime();
        existingShowtime.setMovie(movie);
        existingShowtime.setTheater("Theater 1");
        existingShowtime.setStartTime(ZonedDateTime.now().plusHours(1));
        existingShowtime.setEndTime(ZonedDateTime.now().plusHours(3));
        existingShowtime.setPrice(12.99);
        showtimeRepository.save(existingShowtime);

        // When & Then
        mockMvc.perform(post("/showtimes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validShowtimeDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnBadRequestForInvalidInput() throws Exception {
        mockMvc.perform(post("/showtimes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidShowtimeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.theater").exists())
                .andExpect(jsonPath("$.startTime").exists())
                .andExpect(jsonPath("$.endTime").exists())
                .andExpect(jsonPath("$.price").exists());
    }

    @Test
    void shouldReturnNotFoundWhenMovieDoesNotExist() throws Exception {
        validShowtimeDTO.setMovieId(999L);
        mockMvc.perform(post("/showtimes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validShowtimeDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldUpdateShowtimeSuccessfully() throws Exception {
        // Given
        Showtime existingShowtime = new Showtime();
        existingShowtime.setMovie(movie);
        existingShowtime.setTheater("Theater 1");
        existingShowtime.setStartTime(ZonedDateTime.now().plusHours(1));
        existingShowtime.setEndTime(ZonedDateTime.now().plusHours(3));
        existingShowtime.setPrice(12.99);
        existingShowtime = showtimeRepository.save(existingShowtime);

        // When & Then
        mockMvc.perform(post("/showtimes/update/" + existingShowtime.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validShowtimeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.theater", is("Theater 1")))
                .andExpect(jsonPath("$.price", is(12.99)));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentShowtime() throws Exception {
        mockMvc.perform(post("/showtimes/update/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validShowtimeDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldDeleteShowtimeSuccessfully() throws Exception {
        // Given
        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setTheater("Theater 1");
        showtime.setStartTime(ZonedDateTime.now().plusHours(1));
        showtime.setEndTime(ZonedDateTime.now().plusHours(3));
        showtime.setPrice(12.99);
        showtime = showtimeRepository.save(showtime);

        // When & Then
        mockMvc.perform(delete("/showtimes/" + showtime.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        // Verify deletion
        mockMvc.perform(get("/showtimes/" + showtime.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentShowtime() throws Exception {
        mockMvc.perform(delete("/showtimes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldHandleEndTimeBeforeStartTime() throws Exception {
        validShowtimeDTO.setEndTime(validShowtimeDTO.getStartTime().minusHours(1));
        mockMvc.perform(post("/showtimes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validShowtimeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.endTime").exists());
    }
} 