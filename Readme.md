# Popcorn Palace Movie Ticket Booking System

## Overview
The Popcorn Palace Movie Ticket Booking System is a backend service designed to handle various operations related to movie,showtime, and booking management. The system is built with robust concurrency support to handle multiple simultaneous booking requests.

## Functionality
The system provides the following APIs:

- **Movie API**: Manages movies available on the platform.
- **Showtime API**: Manages movies showtime on the theaters.
- **Booking API**: Manages the movie tickets booking with built-in concurrency support.

## Technical Aspects
The system is built using Java Spring Boot, leveraging its robust framework for creating RESTful APIs. Data persistence can be managed using an in-memory database like H2 for simplicity, or a more robust solution like PostgreSQL for production.

### Concurrency Support
The system implements several mechanisms to ensure thread safety and data consistency:

1. **Optimistic Locking**: All entities use `@Version` fields to prevent lost updates in concurrent scenarios.
2. **Pessimistic Locking**: Critical operations like seat booking use pessimistic locking to prevent race conditions.
3. **Retry Mechanism**: The booking service implements a retry mechanism with exponential backoff for transient failures.
4. **Transaction Management**: All critical operations are wrapped in `@Transactional` with appropriate isolation levels.
5. **Unique Constraints**: The booking system uses unique constraints to prevent double-booking of seats.

## Homework Task
Candidates are expected to design and implement the above APIs, adhering to RESTful principles.

## APIs

### Movies  APIs

| API Description           | Endpoint               | Request Body                          | Response Status | Response Body |
|---------------------------|------------------------|---------------------------------------|-----------------|---------------|
| Get all movies | GET /movies/all | | 200 OK | [ { "id": 12345, "title": "Sample Movie Title 1", "genre": "Action", "duration": 120, "rating": 8.7, "releaseYear": 2025 }, { "id": 67890, "title": "Sample Movie Title 2", "genre": "Comedy", "duration": 90, "rating": 7.5, "releaseYear": 2024 } ] |
| Add a movie | POST /movies | { "title": "Sample Movie Title", "genre": "Action", "duration": 120, "rating": 8.7, "releaseYear": 2025 } | 200 OK | { "id": 1, "title": "Sample Movie Title", "genre": "Action", "duration": 120, "rating": 8.7, "releaseYear": 2025 }|
| Update a movie | POST /movies/update/{movieTitle} | { "title": "Sample Movie Title", "genre": "Action", "duration": 120, "rating": 8.7, "releaseYear": 2025 } | 200 OK | |
| DELETE /movies/{movieTitle} | | 200 OK | |

### Showtimes APIs

| API Description            | Endpoint                           | Request Body                                                                                                                                      | Response Status | Response Body                                                                                                                                                                                                                                                                   |
|----------------------------|------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|-----------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Get showtime by ID | GET /showtimes/{showtimeId} |                                                                                                                                                   | 200 OK | { "id": 1, "price":50.2, "movieId": 1, "theater": "Sample Theater", "startTime": "2025-02-14T11:47:46.125405Z", "endTime": "2025-02-14T14:47:46.125405Z" }                                                                                                                      | | Delete a restaurant        | DELETE /restaurants/{id}           |                                                                              | 204 No Content  |                                                                                                        |
| Add a showtime | POST /showtimes | { "movieId": 1, "price":20.2, "theater": "Sample Theater", "startTime": "2025-02-14T11:47:46.125405Z", "endTime": "2025-02-14T14:47:46.125405Z" } | 200 OK | { "id": 1, "price":50.2,"movieId": 1, "theater": "Sample Theater", "startTime": "2025-02-14T11:47:46.125405Z", "endTime": "2025-02-14T14:47:46.125405Z" }                                                                                                                                    |
| Update a showtime | POST /showtimes/update/{showtimeId}| { "movieId": 1, "price":50.2, "theater": "Sample Theater", "startTime": "2025-02-14T11:47:46.125405Z", "endTime": "2025-02-14T14:47:46.125405Z" } | 200 OK |                                                                                                                                                                                                                                                                                 |
| Delete a showtime | DELETE /showtimes/{showtimeId} |                                                                                                                                                   | 200 OK |                                                                                                                                                                                                                                                                                 |

### Bookings APIs

| API Description           | Endpoint       | Request Body                                     | Response Status | Response Body                                                                                                                                          |
|---------------------------|----------------|--------------------------------------------------|-----------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| Book a ticket | POST /bookings | { "showtimeId": 1, "seatNumber": 15 , userId:"84438967-f68f-4fa0-b620-0f08217e76af"} | 201 Created | { "bookingId":"d1a6423b-4469-4b00-8c5f-e3cfc42eacae" } |
| Book a ticket (seat already taken) | POST /bookings | { "showtimeId": 1, "seatNumber": 15 , userId:"84438967-f68f-4fa0-b620-0f08217e76af"} | 409 Conflict | { "status": 409, "error": "Seat Already Booked", "message": "Seat 15 is already booked for showtime with ID 1" } |

## Concurrency Handling
The booking system is designed to handle concurrent booking requests safely:

1. **Seat Booking**: When multiple users try to book the same seat simultaneously:
   - Only one booking will succeed (201 Created)
   - Other attempts will receive 409 Conflict
   - The system uses optimistic locking and retries to handle race conditions

2. **Showtime Management**: When managing showtimes:
   - Overlapping showtimes are prevented
   - Concurrent updates are handled safely
   - Pessimistic locking is used for critical operations

3. **Error Handling**:
   - 409 Conflict: When a seat is already booked
   - 400 Bad Request: For invalid input
   - 404 Not Found: When showtime doesn't exist
   - 500 Internal Server Error: For unexpected errors

## Jump Start
For your convenience, compose.yml includes Postgresql DB, the app is already pointing to this connection. In addition, you have the schema and data SQL files that can setup your DB schema and init data.

## Prerequisite
1. Java SDK - https://www.oracle.com/java/technologies/downloads/#java17
2. Java IDE - https://www.jetbrains.com/idea/download or any other IDE
3. Docker - https://www.docker.com/products/docker-desktop/

## Instructions
1. You may use the compose.yml file to spin up a local PostgreSQL Docker container
2. Complete the task.
3. On completion, put your public git repo link on the hackerrank test, make sure to push all the files.

