package com.att.tdp.popcorn_palace.service;

import com.att.tdp.popcorn_palace.dto.ShowtimeDTO;
import com.att.tdp.popcorn_palace.exception.movie.MovieNotFoundException;
import com.att.tdp.popcorn_palace.exception.showtime.OverlappingShowtimeException;
import com.att.tdp.popcorn_palace.exception.showtime.ShowtimeNotFoundException;
import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.model.Showtime;
import com.att.tdp.popcorn_palace.repository.MovieRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;

    @Autowired
    public ShowtimeService(ShowtimeRepository showtimeRepository, MovieRepository movieRepository) {
        this.showtimeRepository = showtimeRepository;
        this.movieRepository = movieRepository;
    }

    @Transactional
    public Showtime addShowtime(ShowtimeDTO showtimeDTO) {
        Movie movie = movieRepository.findById(showtimeDTO.getMovieId())
                .orElseThrow(() -> new MovieNotFoundException(showtimeDTO.getMovieId().toString()));

        checkForOverlappingShowtimes(showtimeDTO);

        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setTheater(showtimeDTO.getTheater());
        showtime.setStartTime(showtimeDTO.getStartTime());
        showtime.setEndTime(showtimeDTO.getEndTime());
        showtime.setPrice(showtimeDTO.getPrice());

        return showtimeRepository.save(showtime);
    }

    @Transactional
    public Showtime updateShowtime(Long id, ShowtimeDTO showtimeDTO) {
        Showtime existingShowtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ShowtimeNotFoundException(id));

        Movie movie = movieRepository.findById(showtimeDTO.getMovieId())
                .orElseThrow(() -> new MovieNotFoundException(showtimeDTO.getMovieId().toString()));

        checkForOverlappingShowtimes(showtimeDTO, id);

        existingShowtime.setMovie(movie);
        existingShowtime.setTheater(showtimeDTO.getTheater());
        existingShowtime.setStartTime(showtimeDTO.getStartTime());
        existingShowtime.setEndTime(showtimeDTO.getEndTime());
        existingShowtime.setPrice(showtimeDTO.getPrice());

        return showtimeRepository.save(existingShowtime);
    }

    public Showtime getShowtime(Long id) {
        return showtimeRepository.findById(id)
                .orElseThrow(() -> new ShowtimeNotFoundException(id));
    }

    @Transactional
    public void deleteShowtime(Long id) {
        if (!showtimeRepository.existsById(id)) {
            throw new ShowtimeNotFoundException(id);
        }
        showtimeRepository.deleteById(id);
    }

    private void checkForOverlappingShowtimes(ShowtimeDTO showtimeDTO) {
        checkForOverlappingShowtimes(showtimeDTO, null);
    }

    private void checkForOverlappingShowtimes(ShowtimeDTO showtimeDTO, Long excludeId) {
        if (!showtimeDTO.getEndTime().isAfter(showtimeDTO.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        List<Showtime> overlapping = showtimeRepository.findOverlappingShowtimes(
                showtimeDTO.getTheater(),
                showtimeDTO.getStartTime(),
                showtimeDTO.getEndTime()
        );

        if (excludeId != null) {
            overlapping.removeIf(s -> s.getId().equals(excludeId));
        }

        if (!overlapping.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            throw new OverlappingShowtimeException(
                    showtimeDTO.getTheater(),
                    showtimeDTO.getStartTime().format(formatter),
                    showtimeDTO.getEndTime().format(formatter)
            );
        }
    }
} 