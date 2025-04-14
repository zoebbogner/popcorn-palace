package com.att.tdp.popcorn_palace.exception.booking;

public class SeatAlreadyBookedException extends RuntimeException {
    public SeatAlreadyBookedException(Long showtimeId, Integer seatNumber) {
        super("Seat " + seatNumber + " is already booked for showtime " + showtimeId);
    }
} 