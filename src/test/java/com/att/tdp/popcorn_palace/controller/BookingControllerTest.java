package com.att.tdp.popcorn_palace.controller;

import com.att.tdp.popcorn_palace.dto.BookingDTO;
import com.att.tdp.popcorn_palace.exception.booking.SeatAlreadyBookedException;
import com.att.tdp.popcorn_palace.exception.showtime.ShowtimeNotFoundException;
import com.att.tdp.popcorn_palace.model.Booking;
import com.att.tdp.popcorn_palace.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private BookingService bookingService;

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
                // Given
                BookingDTO invalidDto = new BookingDTO(null, 0, null);

                // When & Then
                mockMvc.perform(post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidDto)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.showtimeId").exists())
                                .andExpect(jsonPath("$.seatNumber").exists())
                                .andExpect(jsonPath("$.userId").exists());
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
                                .andExpect(jsonPath("$.status").exists())
                                .andExpect(jsonPath("$.error").exists())
                                .andExpect(jsonPath("$.message").exists());
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
                                .andExpect(jsonPath("$.status").exists())
                                .andExpect(jsonPath("$.error").exists())
                                .andExpect(jsonPath("$.message").exists());
        }
}
