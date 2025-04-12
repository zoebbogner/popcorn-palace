package com.att.tdp.popcorn_palace.service;

import com.att.tdp.popcorn_palace.dto.MovieDTO;
import com.att.tdp.popcorn_palace.exception.movie.MovieAlreadyExistsException;
import com.att.tdp.popcorn_palace.exception.movie.MovieNotFoundException;
import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
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
    void shouldReturnAllMovies() {
        // Given
        List<Movie> movies = Arrays.asList(movie);
        given(movieRepository.findAll()).willReturn(movies);

        // When
        List<Movie> result = movieService.getAllMovies();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(movie.getTitle(), result.get(0).getTitle());
        verify(movieRepository).findAll();
    }

    @Test
    void shouldAddNewMovie() {
        // Given
        given(movieRepository.existsByTitle(movieDTO.getTitle())).willReturn(false);
        given(movieRepository.save(any(Movie.class))).willReturn(movie);

        // When
        Movie result = movieService.addMovie(movieDTO);

        // Then
        assertNotNull(result);
        assertEquals(movie.getTitle(), result.getTitle());
        verify(movieRepository).existsByTitle(movieDTO.getTitle());
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    void shouldThrowWhenAddingExistingMovie() {
        // Given
        given(movieRepository.existsByTitle(movieDTO.getTitle())).willReturn(true);

        // When & Then
        assertThrows(MovieAlreadyExistsException.class, () -> movieService.addMovie(movieDTO));
        verify(movieRepository).existsByTitle(movieDTO.getTitle());
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void shouldUpdateExistingMovie() {
        // Given
        given(movieRepository.findByTitle(movie.getTitle())).willReturn(Optional.of(movie));
        given(movieRepository.save(any(Movie.class))).willReturn(movie);

        // When
        Movie result = movieService.updateMovie(movie.getTitle(), movieDTO);

        // Then
        assertNotNull(result);
        assertEquals(movie.getTitle(), result.getTitle());
        verify(movieRepository).findByTitle(movie.getTitle());
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentMovie() {
        // Given
        given(movieRepository.findByTitle(movie.getTitle())).willReturn(Optional.empty());

        // When & Then
        assertThrows(MovieNotFoundException.class, () -> movieService.updateMovie(movie.getTitle(), movieDTO));
        verify(movieRepository).findByTitle(movie.getTitle());
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void shouldDeleteExistingMovie() {
        // Given
        given(movieRepository.existsByTitle(movie.getTitle())).willReturn(true);
        doNothing().when(movieRepository).deleteByTitle(movie.getTitle());

        // When
        movieService.deleteMovie(movie.getTitle());

        // Then
        verify(movieRepository).existsByTitle(movie.getTitle());
        verify(movieRepository).deleteByTitle(movie.getTitle());
    }

    @Test
    void shouldThrowWhenDeletingNonExistentMovie() {
        // Given
        given(movieRepository.existsByTitle(movie.getTitle())).willReturn(false);

        // When & Then
        assertThrows(MovieNotFoundException.class, () -> movieService.deleteMovie(movie.getTitle()));
        verify(movieRepository).existsByTitle(movie.getTitle());
        verify(movieRepository, never()).deleteByTitle(movie.getTitle());
    }
} 