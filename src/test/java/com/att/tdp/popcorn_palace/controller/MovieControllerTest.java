package com.att.tdp.popcorn_palace.controller;

import com.att.tdp.popcorn_palace.dto.MovieDTO;
import com.att.tdp.popcorn_palace.exception.movie.MovieAlreadyExistsException;
import com.att.tdp.popcorn_palace.exception.movie.MovieNotFoundException;
import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.service.MovieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovieController.class)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MovieService movieService;

    private Movie movie;
    private MovieDTO movieDTO;

    @BeforeEach
    void setUp() {
        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Inception");
        movie.setGenre("Sci-Fi");
        movie.setDuration(148);
        movie.setRating(8.8);
        movie.setReleaseYear(2010);

        movieDTO = new MovieDTO();
        movieDTO.setTitle("Inception");
        movieDTO.setGenre("Sci-Fi");
        movieDTO.setDuration(148);
        movieDTO.setRating(8.8);
        movieDTO.setReleaseYear(2010);
    }

    @Test
    void shouldReturnAllMovies() throws Exception {
        // Given
        List<Movie> movies = Arrays.asList(movie);
        given(movieService.getAllMovies()).willReturn(movies);

        // When & Then
        mockMvc.perform(get("/movies/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].title").value(movie.getTitle()));
    }

    @Test
    void shouldAddNewMovie() throws Exception {
        // Given
        given(movieService.addMovie(any(MovieDTO.class))).willReturn(movie);

        // When & Then
        mockMvc.perform(post("/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movieDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(movie.getTitle()));
    }

    @Test
    void shouldReturnConflictWhenAddingExistingMovie() throws Exception {
        // Given
        given(movieService.addMovie(any(MovieDTO.class)))
                .willThrow(new MovieAlreadyExistsException(movieDTO.getTitle()));

        // When & Then
        mockMvc.perform(post("/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movieDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldUpdateExistingMovie() throws Exception {
        // Given
        given(movieService.updateMovie(any(String.class), any(MovieDTO.class))).willReturn(movie);

        // When & Then
        mockMvc.perform(post("/movies/update/Inception")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movieDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(movie.getTitle()));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentMovie() throws Exception {
        // Given
        given(movieService.updateMovie(any(String.class), any(MovieDTO.class)))
                .willThrow(new MovieNotFoundException("Inception"));

        // When & Then
        mockMvc.perform(post("/movies/update/Inception")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movieDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldDeleteExistingMovie() throws Exception {
        // Given
        doNothing().when(movieService).deleteMovie(movie.getTitle());

        // When & Then
        mockMvc.perform(delete("/movies/Inception"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentMovie() throws Exception {
        // Given
        doThrow(new MovieNotFoundException("Inception")).when(movieService).deleteMovie(movie.getTitle());

        // When & Then
        mockMvc.perform(delete("/movies/Inception"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }
} 