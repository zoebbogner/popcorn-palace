package com.att.tdp.popcorn_palace.repository;

import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.model.Showtime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ShowtimeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    private Movie movie;
    private Showtime showtime1;
    private Showtime showtime2;
    private Showtime showtime3;

    @BeforeEach
    void setUp() {
        movie = new Movie();
        movie.setTitle("Inception");
        movie.setGenre("Sci-Fi");
        movie.setDuration(148);
        movie.setRating(8.8);
        movie.setReleaseYear(2010);
        entityManager.persist(movie);

        ZonedDateTime now = ZonedDateTime.now();

        showtime1 = new Showtime();
        showtime1.setMovie(movie);
        showtime1.setTheater("Theater 1");
        showtime1.setStartTime(now.plusHours(1));
        showtime1.setEndTime(now.plusHours(3));
        showtime1.setPrice(12.99);
        entityManager.persist(showtime1);

        showtime2 = new Showtime();
        showtime2.setMovie(movie);
        showtime2.setTheater("Theater 1");
        showtime2.setStartTime(now.plusHours(4));
        showtime2.setEndTime(now.plusHours(6));
        showtime2.setPrice(12.99);
        entityManager.persist(showtime2);

        showtime3 = new Showtime();
        showtime3.setMovie(movie);
        showtime3.setTheater("Theater 2");
        showtime3.setStartTime(now.plusHours(1));
        showtime3.setEndTime(now.plusHours(3));
        showtime3.setPrice(12.99);
        entityManager.persist(showtime3);

        entityManager.flush();
    }

    @Test
    void shouldFindOverlappingShowtimesInSameTheater() {
        ZonedDateTime startTime = showtime1.getStartTime().plusMinutes(30);
        ZonedDateTime endTime = showtime1.getEndTime().plusMinutes(30);

        List<Showtime> overlapping = showtimeRepository.findOverlappingShowtimes(
                "Theater 1",
                startTime,
                endTime
        );

        assertEquals(1, overlapping.size());
        assertEquals(showtime1.getId(), overlapping.get(0).getId());
    }

    @Test
    void shouldNotFindOverlappingShowtimesWhenTimesDontOverlap() {
        ZonedDateTime startTime = showtime1.getEndTime().plusMinutes(1);
        ZonedDateTime endTime = showtime2.getStartTime().minusMinutes(1);

        List<Showtime> overlapping = showtimeRepository.findOverlappingShowtimes(
                "Theater 1",
                startTime,
                endTime
        );

        assertTrue(overlapping.isEmpty());
    }

    @Test
    void shouldNotFindOverlappingShowtimesInDifferentTheater() {
        ZonedDateTime startTime = showtime1.getStartTime().plusMinutes(30);
        ZonedDateTime endTime = showtime1.getEndTime().plusMinutes(30);

        List<Showtime> overlapping = showtimeRepository.findOverlappingShowtimes(
                "Theater 2",
                startTime,
                endTime
        );

        assertEquals(1, overlapping.size());
        assertEquals(showtime3.getId(), overlapping.get(0).getId());
    }
} 