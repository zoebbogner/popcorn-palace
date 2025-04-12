package com.att.tdp.popcorn_palace.exception.showtime;

public class ShowtimeNotFoundException extends RuntimeException {
    public ShowtimeNotFoundException(Long id) {
        super("Showtime with ID " + id + " not found");
    }
} 