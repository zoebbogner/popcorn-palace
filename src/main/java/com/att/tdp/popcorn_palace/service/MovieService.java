package com.att.tdp.popcorn_palace.service;

import com.att.tdp.popcorn_palace.dto.MovieDTO;
import com.att.tdp.popcorn_palace.exception.movie.MovieAlreadyExistsException;
import com.att.tdp.popcorn_palace.exception.movie.MovieNotFoundException;
import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    @Autowired
    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public Movie addMovie(MovieDTO movieDTO) {
        if (movieRepository.existsByTitle(movieDTO.getTitle())) {
            throw new MovieAlreadyExistsException(movieDTO.getTitle());
        }

        Movie movie = new Movie();
        movie.setTitle(movieDTO.getTitle());
        movie.setGenre(movieDTO.getGenre());
        movie.setDuration(movieDTO.getDuration());
        movie.setRating(movieDTO.getRating());
        movie.setReleaseYear(movieDTO.getReleaseYear());

        return movieRepository.save(movie);
    }

    public Movie updateMovie(String title, MovieDTO movieDTO) {
        Movie existingMovie = movieRepository.findByTitle(title)
                .orElseThrow(() -> new MovieNotFoundException(title));

        existingMovie.setGenre(movieDTO.getGenre());
        existingMovie.setDuration(movieDTO.getDuration());
        existingMovie.setRating(movieDTO.getRating());
        existingMovie.setReleaseYear(movieDTO.getReleaseYear());

        return movieRepository.save(existingMovie);
    }

    public void deleteMovie(String title) {
        if (!movieRepository.existsByTitle(title)) {
            throw new MovieNotFoundException(title);
        }
        movieRepository.deleteByTitle(title);
    }
} 