package com.att.tdp.popcorn_palace.exception;

import com.att.tdp.popcorn_palace.exception.movie.MovieAlreadyExistsException;
import com.att.tdp.popcorn_palace.exception.movie.MovieNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleMethodArgumentNotValidException() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "title", "Title is required");
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        MethodParameter methodParameter = new MethodParameter(this.getClass().getDeclaredMethods()[0], -1);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("title"));
        assertEquals("Title is required", response.getBody().get("title"));
    }

    @Test
    void shouldHandleMovieNotFoundException() {
        MovieNotFoundException ex = new MovieNotFoundException("Inception");

        ResponseEntity<Map<String, String>> response = handler.handleMovieNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Movie with title 'Inception' not found", response.getBody().get("error"));
    }

    @Test
    void shouldHandleMovieAlreadyExistsException() {
        MovieAlreadyExistsException ex = new MovieAlreadyExistsException("Inception");

        ResponseEntity<Map<String, String>> response = handler.handleMovieAlreadyExistsException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Movie with title 'Inception' already exists", response.getBody().get("error"));
    }
}
