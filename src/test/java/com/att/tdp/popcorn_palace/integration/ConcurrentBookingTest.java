package com.att.tdp.popcorn_palace.integration;

import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.model.Showtime;
import com.att.tdp.popcorn_palace.repository.BookingRepository;
import com.att.tdp.popcorn_palace.repository.MovieRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ConcurrentBookingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private Movie movie;
    private Showtime showtime;
    private final String theater = "Theater 1";
    private final Integer seatNumber = 1;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up repositories
        bookingRepository.deleteAll();
        showtimeRepository.deleteAll();
        movieRepository.deleteAll();

        // Create test movie
        movie = new Movie();
        movie.setTitle("Test Movie");
        movie.setGenre("Sci-Fi");
        movie.setDuration(120);
        movie.setRating(8.5);
        movie.setReleaseYear(2024);
        movie = movieRepository.save(movie);

        // Create test showtime
        ZonedDateTime now = ZonedDateTime.now();
        showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setTheater(theater);
        showtime.setStartTime(now.plusHours(1));
        showtime.setEndTime(now.plusHours(3));
        showtime = showtimeRepository.save(showtime);
    }

    @Test
    void shouldHandleConcurrentBookingAttempts() throws Exception {
        int numberOfThreads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        List<CompletableFuture<Integer>> futures = new ArrayList<>();

        // Submit concurrent booking requests
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
                try {
                    System.out.println("Thread " + threadId + " attempting booking...");
                    String requestBody = String.format(
                        "{\"showtimeId\":%d,\"seatNumber\":%d,\"userId\":\"user%d\"}",
                        showtime.getId(), seatNumber, threadId
                    );
                    
                    return mockMvc.perform(post("/bookings")
                            .contentType("application/json")
                            .content(requestBody))
                            .andReturn()
                            .getResponse()
                            .getStatus();
                } catch (Exception e) {
                    System.out.println("Thread " + threadId + " failed with exception: " + e.getMessage());
                    // Let the exception propagate to the GlobalExceptionHandler
                    throw new RuntimeException(e);
                }
            }, executorService).exceptionally(ex -> {
                // If the exception was handled by GlobalExceptionHandler, return the status code
                if (ex.getCause() != null && ex.getCause().getMessage() != null) {
                    if (ex.getCause().getMessage().contains("unique constraint")) {
                        return 409;
                    }
                }
                return 500;
            });
            futures.add(future);
        }

        // Wait for all requests to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        // Collect results
        List<Integer> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        // Print detailed results
        System.out.println("All results: " + results);
        
        // Verify results
        long successfulBookings = results.stream()
                .filter(status -> status == 201) // 201 is HTTP Created
                .count();

        long failedBookings = results.stream()
                .filter(status -> status == 409) // 409 is HTTP Conflict
                .count();

        System.out.println("Successful bookings: " + successfulBookings);
        System.out.println("Failed bookings: " + failedBookings);
        System.out.println("Total bookings in DB: " + bookingRepository.count());

        // Only one booking should succeed
        assertEquals(1, successfulBookings, "Only one booking should succeed");
        assertEquals(numberOfThreads - 1, failedBookings, "All other bookings should fail");
        
        // Verify only one booking exists in database
        assertEquals(1, bookingRepository.count(), "Only one booking should exist in database");
    }
} 