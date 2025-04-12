package com.att.tdp.popcorn_palace.controller;

import com.att.tdp.popcorn_palace.dto.ShowtimeDTO;
import com.att.tdp.popcorn_palace.model.Showtime;
import com.att.tdp.popcorn_palace.service.ShowtimeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/showtimes")
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    @Autowired
    public ShowtimeController(ShowtimeService showtimeService) {
        this.showtimeService = showtimeService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Showtime> getShowtime(@PathVariable Long id) {
        Showtime showtime = showtimeService.getShowtime(id);
        return ResponseEntity.ok(showtime);
    }

    @PostMapping
    public ResponseEntity<Showtime> addShowtime(@Valid @RequestBody ShowtimeDTO showtimeDTO) {
        Showtime showtime = showtimeService.addShowtime(showtimeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(showtime);
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<Showtime> updateShowtime(
            @PathVariable Long id,
            @Valid @RequestBody ShowtimeDTO showtimeDTO) {
        Showtime showtime = showtimeService.updateShowtime(id, showtimeDTO);
        return ResponseEntity.ok(showtime);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteShowtime(@PathVariable Long id) {
        showtimeService.deleteShowtime(id);
        return ResponseEntity.ok(Map.of("message", "Showtime with id " + id + " was deleted successfully."));
    }
} 