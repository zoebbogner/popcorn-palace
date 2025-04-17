package com.att.tdp.popcorn_palace.controller;

import com.att.tdp.popcorn_palace.dto.MovieDTO;
import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.service.MovieService;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movies")
public class MovieController {

    private final MovieService movieService;

    @Autowired
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Movie>> getAllMovies() {
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @PostMapping
    public ResponseEntity<Movie> addMovie(@Valid @RequestBody MovieDTO movieDTO) {
        return ResponseEntity.ok(movieService.addMovie(movieDTO));
    }

    @PostMapping("/update/{title}")
    public ResponseEntity<Movie> updateMovie(@PathVariable String title, @Valid @RequestBody MovieDTO movieDTO) {
        return ResponseEntity.ok(movieService.updateMovie(title, movieDTO));
    }

    @Transactional
    @DeleteMapping("/{title}")
    public ResponseEntity<Void> deleteMovie(@PathVariable String title) {
        movieService.deleteMovie(title);
        return ResponseEntity.ok().build();
    }
} 