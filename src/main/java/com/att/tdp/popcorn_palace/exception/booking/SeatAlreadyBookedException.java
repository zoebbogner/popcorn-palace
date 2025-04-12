package com.att.tdp.popcorn_palace.exception.booking;

public class SeatAlreadyBookedException extends RuntimeException {
    public SeatAlreadyBookedException(String message) {
        super(message);
    }
} 