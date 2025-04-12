package com.att.tdp.popcorn_palace.exception.movie;

public class MovieAlreadyExistsException extends RuntimeException {
    public MovieAlreadyExistsException(String title) {
        super("Movie with title '" + title + "' already exists");
    }
} 