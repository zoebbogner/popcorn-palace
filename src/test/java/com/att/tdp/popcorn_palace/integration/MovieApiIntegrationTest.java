package com.att.tdp.popcorn_palace.integration;

import com.att.tdp.popcorn_palace.dto.MovieDTO;
import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.repository.MovieRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MovieApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MovieRepository movieRepository;

    private MovieDTO validMovieDTO;
    private MovieDTO invalidMovieDTO;

    @BeforeEach
    void setUp() {
        movieRepository.deleteAll();

        validMovieDTO = new MovieDTO();
        validMovieDTO.setTitle("Inception");
        validMovieDTO.setGenre("Sci-Fi");
        validMovieDTO.setDuration(148);
        validMovieDTO.setRating(8.8);
        validMovieDTO.setReleaseYear(2010);

        invalidMovieDTO = new MovieDTO();
        invalidMovieDTO.setTitle(""); // Invalid: empty title
        invalidMovieDTO.setGenre(""); // Invalid: empty genre
        invalidMovieDTO.setDuration(0); // Invalid: duration must be positive
        invalidMovieDTO.setRating(-1.0); // Invalid: rating must be non-negative
        invalidMovieDTO.setReleaseYear(1887); // Invalid: year must be >= 1888
    }

    @Test
    void shouldReturnEmptyListWhenNoMoviesExist() throws Exception {
        mockMvc.perform(get("/movies/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldReturnAllMovies() throws Exception {
        // Given
        Movie movie = new Movie();
        movie.setTitle("Inception");
        movie.setGenre("Sci-Fi");
        movie.setDuration(148);
        movie.setRating(8.8);
        movie.setReleaseYear(2010);
        movieRepository.save(movie);

        // When & Then
        mockMvc.perform(get("/movies/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Inception")));
    }

    @Test
    void shouldAddNewMovie() throws Exception {
        mockMvc.perform(post("/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validMovieDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Inception")))
                .andExpect(jsonPath("$.genre", is("Sci-Fi")));
    }

    @Test
    void shouldReturnConflictWhenAddingDuplicateMovie() throws Exception {
        // Given
        Movie movie = new Movie();
        movie.setTitle("Inception");
        movie.setGenre("Sci-Fi");
        movie.setDuration(148);
        movie.setRating(8.8);
        movie.setReleaseYear(2010);
        movieRepository.save(movie);

        // When & Then
        mockMvc.perform(post("/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validMovieDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnBadRequestWhenAddingInvalidMovie() throws Exception {
        mockMvc.perform(post("/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidMovieDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.genre").exists())
                .andExpect(jsonPath("$.duration").exists())
                .andExpect(jsonPath("$.rating").exists())
                .andExpect(jsonPath("$.releaseYear").exists());
    }

    @Test
    void shouldUpdateExistingMovie() throws Exception {
        // Given
        Movie existingMovie = new Movie();
        existingMovie.setTitle("Inception");
        existingMovie.setGenre("Sci-Fi");
        existingMovie.setDuration(148);
        existingMovie.setRating(8.8);
        existingMovie.setReleaseYear(2010);
        movieRepository.save(existingMovie);

        MovieDTO updateDTO = new MovieDTO();
        updateDTO.setTitle("Inception");
        updateDTO.setGenre("Action");
        updateDTO.setDuration(150);
        updateDTO.setRating(9.0);
        updateDTO.setReleaseYear(2010);

        // When & Then
        mockMvc.perform(post("/movies/update/Inception")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.genre", is("Action")))
                .andExpect(jsonPath("$.duration", is(150)))
                .andExpect(jsonPath("$.rating", is(9.0)));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentMovie() throws Exception {
        mockMvc.perform(post("/movies/update/NonExistent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validMovieDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldDeleteExistingMovie() throws Exception {
        // Given
        Movie movie = new Movie();
        movie.setTitle("Inception");
        movie.setGenre("Sci-Fi");
        movie.setDuration(148);
        movie.setRating(8.8);
        movie.setReleaseYear(2010);
        movieRepository.save(movie);

        // When & Then
        mockMvc.perform(delete("/movies/Inception"))
                .andExpect(status().isOk());

        // Verify deletion
        mockMvc.perform(get("/movies/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentMovie() throws Exception {
        mockMvc.perform(delete("/movies/NonExistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());
    }
} 