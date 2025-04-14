package com.att.tdp.popcorn_palace.exception;

import com.att.tdp.popcorn_palace.exception.movie.MovieAlreadyExistsException;
import com.att.tdp.popcorn_palace.exception.movie.MovieNotFoundException;
import com.att.tdp.popcorn_palace.exception.showtime.OverlappingShowtimeException;
import com.att.tdp.popcorn_palace.exception.showtime.ShowtimeNotFoundException;
import com.att.tdp.popcorn_palace.exception.booking.SeatAlreadyBookedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    void shouldHandleMethodArgumentNotValidException() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "title", "Title is required");
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        MethodParameter methodParameter = new MethodParameter(this.getClass().getDeclaredMethods()[0], -1);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationExceptions(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("title"));
        assertEquals("Title is required", response.getBody().get("title"));
    }

    @Test
    void shouldHandleMovieNotFoundException() {
        MovieNotFoundException ex = new MovieNotFoundException("Inception");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMovieNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Movie Not Found", body.getError());
        assertEquals(ex.getMessage(), body.getMessage());
    }

    @Test
    void shouldHandleMovieAlreadyExistsException() {
        MovieAlreadyExistsException ex = new MovieAlreadyExistsException("Inception");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMovieAlreadyExistsException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Movie Already Exists", body.getError());
        assertEquals(ex.getMessage(), body.getMessage());
    }

    @Test
    void shouldHandleShowtimeNotFoundException() {
        ShowtimeNotFoundException ex = new ShowtimeNotFoundException(1L);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleShowtimeNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Showtime Not Found", body.getError());
        assertEquals(ex.getMessage(), body.getMessage());
    }

    @Test
    void shouldHandleOverlappingShowtimeException() {
        OverlappingShowtimeException ex = new OverlappingShowtimeException("Theater 1", "10:00", "12:00");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleOverlappingShowtimeException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Overlapping Showtime", body.getError());
        assertEquals(ex.getMessage(), body.getMessage());
    }

    @Test
    void shouldHandleSeatAlreadyBookedException() {
        SeatAlreadyBookedException exception = new SeatAlreadyBookedException(1L, 1);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleSeatAlreadyBookedException(exception);
        assertNotNull(response);

        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Seat Already Booked", body.getError());
        assertEquals(exception.getMessage(), body.getMessage());
    }

    // Test controller to trigger exceptions
    @RestController
    static class TestController {
        @GetMapping("/test/seat-booked")
        public void throwSeatAlreadyBooked() {
            throw new SeatAlreadyBookedException(1L, 1);
        }

        @GetMapping("/test/showtime-not-found")
        public void throwShowtimeNotFound() {
            throw new ShowtimeNotFoundException(999L);
        }
    }
}
