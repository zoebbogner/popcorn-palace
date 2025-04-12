package com.att.tdp.popcorn_palace.controller;

import com.att.tdp.popcorn_palace.dto.ShowtimeDTO;
import com.att.tdp.popcorn_palace.exception.movie.MovieNotFoundException;
import com.att.tdp.popcorn_palace.exception.showtime.OverlappingShowtimeException;
import com.att.tdp.popcorn_palace.exception.showtime.ShowtimeNotFoundException;
import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.model.Showtime;
import com.att.tdp.popcorn_palace.service.ShowtimeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShowtimeController.class)
class ShowtimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShowtimeService showtimeService;

    private ShowtimeDTO showtimeDTO;
    private Showtime showtime;

    @BeforeEach
    void setUp() {
        Movie movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Inception");

        showtime = new Showtime();
        showtime.setId(1L);
        showtime.setMovie(movie);
        showtime.setTheater("Theater 1");
        showtime.setStartTime(ZonedDateTime.now().plusHours(1));
        showtime.setEndTime(ZonedDateTime.now().plusHours(3));
        showtime.setPrice(12.99);

        showtimeDTO = new ShowtimeDTO();
        showtimeDTO.setMovieId(1L);
        showtimeDTO.setTheater("Theater 1");
        showtimeDTO.setStartTime(ZonedDateTime.now().plusHours(1));
        showtimeDTO.setEndTime(ZonedDateTime.now().plusHours(3));
        showtimeDTO.setPrice(12.99);
    }

    @Test
    void shouldGetShowtimeSuccessfully() throws Exception {
        when(showtimeService.getShowtime(1L)).thenReturn(showtime);

        mockMvc.perform(get("/showtimes/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.theater").value("Theater 1"));
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentShowtime() throws Exception {
        when(showtimeService.getShowtime(1L)).thenThrow(new ShowtimeNotFoundException(1L));

        mockMvc.perform(get("/showtimes/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldAddShowtimeSuccessfully() throws Exception {
        when(showtimeService.addShowtime(any())).thenReturn(showtime);

        mockMvc.perform(post("/showtimes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(showtimeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.theater").value("Theater 1"));
    }

    @Test
    void shouldReturnConflictWhenAddingOverlappingShowtime() throws Exception {
        when(showtimeService.addShowtime(any()))
                .thenThrow(new OverlappingShowtimeException("Theater 1", "10:00", "12:00"));

        mockMvc.perform(post("/showtimes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(showtimeDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldUpdateShowtimeSuccessfully() throws Exception {
        when(showtimeService.updateShowtime(any(), any())).thenReturn(showtime);

        mockMvc.perform(post("/showtimes/update/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(showtimeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.theater").value("Theater 1"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentShowtime() throws Exception {
        when(showtimeService.updateShowtime(any(), any()))
                .thenThrow(new ShowtimeNotFoundException(1L));

        mockMvc.perform(post("/showtimes/update/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(showtimeDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldDeleteShowtimeSuccessfully() throws Exception {
        doNothing().when(showtimeService).deleteShowtime(1L);

        mockMvc.perform(delete("/showtimes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Showtime with id 1 was deleted successfully."));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentShowtime() throws Exception {
        doThrow(new ShowtimeNotFoundException(1L)).when(showtimeService).deleteShowtime(1L);

        mockMvc.perform(delete("/showtimes/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }
} 