package com.att.tdp.popcorn_palace.exception.showtime;

public class OverlappingShowtimeException extends RuntimeException {
    public OverlappingShowtimeException(String theater, String startTime, String endTime) {
        super("There is already a showtime in theater '" + theater + 
              "' between " + startTime + " and " + endTime);
    }
} 