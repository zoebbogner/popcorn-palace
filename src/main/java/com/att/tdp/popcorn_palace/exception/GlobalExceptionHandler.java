package com.att.tdp.popcorn_palace.exception;

import com.att.tdp.popcorn_palace.exception.booking.SeatAlreadyBookedException;
import com.att.tdp.popcorn_palace.exception.movie.MovieAlreadyExistsException;
import com.att.tdp.popcorn_palace.exception.movie.MovieNotFoundException;
import com.att.tdp.popcorn_palace.exception.showtime.OverlappingShowtimeException;
import com.att.tdp.popcorn_palace.exception.showtime.ShowtimeNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(MovieNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMovieNotFoundException(MovieNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, "Movie Not Found", ex.getMessage());
    }

    @ExceptionHandler(MovieAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleMovieAlreadyExistsException(MovieAlreadyExistsException ex) {
        return buildError(HttpStatus.CONFLICT, "Movie Already Exists", ex.getMessage());
    }

    @ExceptionHandler(ShowtimeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleShowtimeNotFoundException(ShowtimeNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, "Showtime Not Found", ex.getMessage());
    }

    @ExceptionHandler(OverlappingShowtimeException.class)
    public ResponseEntity<ErrorResponse> handleOverlappingShowtimeException(OverlappingShowtimeException ex) {
        return buildError(HttpStatus.CONFLICT, "Overlapping Showtime", ex.getMessage());
    }

    @ExceptionHandler(SeatAlreadyBookedException.class)
    public ResponseEntity<ErrorResponse> handleSeatAlreadyBookedException(SeatAlreadyBookedException ex) {
        return buildError(HttpStatus.CONFLICT, "Seat Already Booked", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildError(HttpStatus.BAD_REQUEST, "Invalid Argument", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String error, String message) {
        ErrorResponse errorResponse = new ErrorResponse(status.value(), error, message);
        return ResponseEntity.status(status).body(errorResponse);
    }
} 