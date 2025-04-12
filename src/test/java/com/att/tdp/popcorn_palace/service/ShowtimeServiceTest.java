package com.att.tdp.popcorn_palace.service;

import com.att.tdp.popcorn_palace.dto.ShowtimeDTO;
import com.att.tdp.popcorn_palace.exception.movie.MovieNotFoundException;
import com.att.tdp.popcorn_palace.exception.showtime.OverlappingShowtimeException;
import com.att.tdp.popcorn_palace.exception.showtime.ShowtimeNotFoundException;
import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.model.Showtime;
import com.att.tdp.popcorn_palace.repository.MovieRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShowtimeServiceTest {

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private ShowtimeService showtimeService;

    private ShowtimeDTO showtimeDTO;
    private Movie movie;
    private Showtime showtime;

    @BeforeEach
    void setUp() {
        movie = new Movie();
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
    void shouldAddShowtimeSuccessfully() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(showtimeRepository.findOverlappingShowtimes(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(showtimeRepository.save(any())).thenReturn(showtime);

        Showtime result = showtimeService.addShowtime(showtimeDTO);

        assertNotNull(result);
        assertEquals(showtime.getTheater(), result.getTheater());
        verify(showtimeRepository).save(any());
    }

    @Test
    void shouldThrowMovieNotFoundExceptionWhenMovieNotFound() {
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(MovieNotFoundException.class, () -> showtimeService.addShowtime(showtimeDTO));
    }

    @Test
    void shouldThrowOverlappingShowtimeExceptionWhenTimesOverlap() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(showtimeRepository.findOverlappingShowtimes(any(), any(), any()))
                .thenReturn(Collections.singletonList(showtime));

        assertThrows(OverlappingShowtimeException.class, () -> showtimeService.addShowtime(showtimeDTO));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenEndTimeBeforeStartTime() {
        showtimeDTO.setEndTime(showtimeDTO.getStartTime().minusHours(1));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        
        assertThrows(IllegalArgumentException.class, () -> showtimeService.addShowtime(showtimeDTO));
    }

    @Test
    void shouldUpdateShowtimeSuccessfully() {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(showtimeRepository.findOverlappingShowtimes(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(showtimeRepository.save(any())).thenReturn(showtime);

        Showtime result = showtimeService.updateShowtime(1L, showtimeDTO);

        assertNotNull(result);
        assertEquals(showtime.getTheater(), result.getTheater());
        verify(showtimeRepository).save(any());
    }

    @Test
    void shouldThrowShowtimeNotFoundExceptionWhenUpdatingNonExistentShowtime() {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ShowtimeNotFoundException.class, () -> showtimeService.updateShowtime(1L, showtimeDTO));
    }

    @Test
    void shouldGetShowtimeSuccessfully() {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));

        Showtime result = showtimeService.getShowtime(1L);

        assertNotNull(result);
        assertEquals(showtime.getTheater(), result.getTheater());
    }

    @Test
    void shouldThrowShowtimeNotFoundExceptionWhenGettingNonExistentShowtime() {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ShowtimeNotFoundException.class, () -> showtimeService.getShowtime(1L));
    }

    @Test
    void shouldDeleteShowtimeSuccessfully() {
        when(showtimeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(showtimeRepository).deleteById(1L);

        assertDoesNotThrow(() -> showtimeService.deleteShowtime(1L));
        verify(showtimeRepository).deleteById(1L);
    }

    @Test
    void shouldThrowShowtimeNotFoundExceptionWhenDeletingNonExistentShowtime() {
        when(showtimeRepository.existsById(1L)).thenReturn(false);

        assertThrows(ShowtimeNotFoundException.class, () -> showtimeService.deleteShowtime(1L));
    }
} 