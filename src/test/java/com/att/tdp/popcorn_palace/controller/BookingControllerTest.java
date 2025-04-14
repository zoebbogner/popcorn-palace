package com.att.tdp.popcorn_palace.controller;

import com.att.tdp.popcorn_palace.dto.BookingDTO;
import com.att.tdp.popcorn_palace.exception.GlobalExceptionHandler;
import com.att.tdp.popcorn_palace.exception.booking.SeatAlreadyBookedException;
import com.att.tdp.popcorn_palace.exception.showtime.ShowtimeNotFoundException;
import com.att.tdp.popcorn_palace.model.Booking;
import com.att.tdp.popcorn_palace.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateBookingSuccessfully() throws Exception {
        // Given
        BookingDTO bookingDTO = new BookingDTO(1L, 1, "user123");
        Booking savedBooking = mock(Booking.class);
        when(savedBooking.getId()).thenReturn(1L);

        when(bookingService.createBooking(any(BookingDTO.class))).thenReturn(savedBooking);

        // When & Then
        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().string("1"));
    }

    @Test
    void shouldReturnBadRequestForMissingFields() throws Exception {
        BookingDTO invalidDto = new BookingDTO(null, 0, null);

        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.showtimeId").value("Showtime ID is required"))
                .andExpect(jsonPath("$.seatNumber").value("Seat number must be at least 1"))
                .andExpect(jsonPath("$.userId").value("User ID is required"));
    }

    @Test
    void shouldReturn404WhenShowtimeNotFound() throws Exception {
        // Given
        BookingDTO bookingDTO = new BookingDTO(999L, 1, "user123");

        when(bookingService.createBooking(any(BookingDTO.class)))
                .thenThrow(new ShowtimeNotFoundException(999L));

        // When & Then
        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Showtime Not Found"))
                .andExpect(jsonPath("$.message").value("Showtime with ID 999 not found"));
    }

    @Test
    void shouldReturn409WhenSeatAlreadyBooked() throws Exception {
        // Given
        BookingDTO bookingDTO = new BookingDTO(1L, 1, "user123");

        when(bookingService.createBooking(any(BookingDTO.class)))
                .thenThrow(new SeatAlreadyBookedException(1L, 1));

        // When & Then
        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Seat Already Booked"))
                .andExpect(jsonPath("$.message").value("Seat 1 is already booked for showtime with ID 1"));
    }
}
