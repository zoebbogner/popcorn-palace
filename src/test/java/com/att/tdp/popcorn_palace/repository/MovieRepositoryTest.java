package com.att.tdp.popcorn_palace.repository;

import com.att.tdp.popcorn_palace.model.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class MovieRepositoryTest {

    @Autowired
    private MovieRepository movieRepository;

    private Movie movie;

    @BeforeEach
    void setUp() {
        movie = new Movie();
        movie.setTitle("Inception");
        movie.setGenre("Sci-Fi");
        movie.setDuration(148);
        movie.setRating(8.8);
        movie.setReleaseYear(2010);
        movieRepository.save(movie);
    }

    @Test
    void shouldFindMovieByTitle() {
        // When
        Optional<Movie> foundMovie = movieRepository.findByTitle(movie.getTitle());

        // Then
        assertTrue(foundMovie.isPresent());
        assertEquals(movie.getTitle(), foundMovie.get().getTitle());
    }

    @Test
    void shouldReturnEmptyWhenMovieNotFound() {
        // When
        Optional<Movie> foundMovie = movieRepository.findByTitle("NonExistentMovie");

        // Then
        assertFalse(foundMovie.isPresent());
    }

    @Test
    void shouldCheckIfMovieExistsByTitle() {
        // When
        boolean exists = movieRepository.existsByTitle(movie.getTitle());

        // Then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenMovieDoesNotExist() {
        // When
        boolean exists = movieRepository.existsByTitle("NonExistentMovie");

        // Then
        assertFalse(exists);
    }

    @Test
    void shouldDeleteMovieByTitle() {
        // When
        movieRepository.deleteByTitle(movie.getTitle());

        // Then
        assertFalse(movieRepository.existsByTitle(movie.getTitle()));
    }
} 