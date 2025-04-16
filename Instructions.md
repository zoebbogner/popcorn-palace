# Popcorn Palace - Setup and Running Instructions

## Prerequisites
- Java 17 or higher
- Maven 3.8 or higher
- Docker and Docker Compose
- Git

## Setup Instructions

1. Clone the repository:
```bash
git clone https://github.com/zoebbogner/popcorn-palace.git
cd popcorn-palace
```

2. Start the PostgreSQL database using Docker Compose:
```bash
docker-compose up -d
```

3. Build the project:
```bash
./mvnw clean install
```

## Running the Application

### Option 1: Using Maven
```bash
./mvnw spring-boot:run
```

### Option 2: Using Docker
1. Build the Docker image:
```bash
docker build -t popcorn-palace .
```

2. Run the container:
```bash
docker run -p 8080:8080 --network popcorn-palace_default popcorn-palace
```

The application will start on port 8080. You can access the API at `http://localhost:8080`

## Database Setup

The application uses:
- PostgreSQL for production (configured in `application.properties`)
- H2 in-memory database for tests (configured in `application-test.properties`)

The database schema and initial data are automatically created when the application starts. The schema includes tables for:
- Movies
- Showtimes
- Bookings

## API Documentation

### Movies APIs

| API Description | Endpoint | Request Body | Response Status | Response Body |
|----------------|----------|--------------|-----------------|---------------|
| Get all movies | GET `/movies/all` | - | 200 OK | `[ { "id": 12345, "title": "Sample Movie Title 1", "genre": "Action", "duration": 120, "rating": 8.7, "releaseYear": 2025 }, { "id": 67890, "title": "Sample Movie Title 2", "genre": "Comedy", "duration": 90, "rating": 7.5, "releaseYear": 2024 } ]` |
| Add a movie | POST `/movies` | `{ "title": "Sample Movie Title", "genre": "Action", "duration": 120, "rating": 8.7, "releaseYear": 2025 }` | 200 OK | `{ "id": 1, "title": "Sample Movie Title", "genre": "Action", "duration": 120, "rating": 8.7, "releaseYear": 2025 }` |
| Update a movie | POST `/movies/update/{movieTitle}` | `{ "title": "Sample Movie Title", "genre": "Action", "duration": 120, "rating": 8.7, "releaseYear": 2025 }` | 200 OK | - |
| Delete a movie | DELETE `/movies/{movieTitle}` | - | 200 OK | - |

### Showtimes APIs

| API Description | Endpoint | Request Body | Response Status | Response Body |
|----------------|----------|--------------|-----------------|---------------|
| Get showtime by ID | GET `/showtimes/{showtimeId}` | - | 200 OK | `{ "id": 1, "price": 50.2, "movieId": 1, "theater": "Sample Theater", "startTime": "2025-02-14T11:47:46.125405Z", "endTime": "2025-02-14T14:47:46.125405Z" }` |
| Add a showtime | POST `/showtimes` | `{ "movieId": 1, "price": 20.2, "theater": "Sample Theater", "startTime": "2025-02-14T11:47:46.125405Z", "endTime": "2025-02-14T14:47:46.125405Z" }` | 200 OK | `{ "id": 1, "price": 50.2, "movieId": 1, "theater": "Sample Theater", "startTime": "2025-02-14T11:47:46.125405Z", "endTime": "2025-02-14T14:47:46.125405Z" }` |
| Update a showtime | POST `/showtimes/update/{showtimeId}` | `{ "movieId": 1, "price": 50.2, "theater": "Sample Theater", "startTime": "2025-02-14T11:47:46.125405Z", "endTime": "2025-02-14T14:47:46.125405Z" }` | 200 OK | - |
| Delete a showtime | DELETE `/showtimes/{showtimeId}` | - | 200 OK | - |

### Bookings APIs

| API Description | Endpoint | Request Body | Response Status | Response Body |
|----------------|----------|--------------|-----------------|---------------|
| Book a ticket | POST `/bookings` | `{ "showtimeId": 1, "seatNumber": 15, "userId": "84438967-f68f-4fa0-b620-0f08217e76af" }` | 200 OK | `{ "bookingId": "d1a6423b-4469-4b00-8c5f-e3cfc42eacae" }` |

## Testing

### Running Unit Tests
```bash
./mvnw test
```

### Running Integration Tests
```bash
./mvnw verify
```

### Running Specific Test Classes
```bash
./mvnw test -Dtest=MovieControllerTest
./mvnw test -Dtest=ShowtimeApiIntegrationTest
```

## Project Structure
- `src/main/java/com/att/tdp/popcorn_palace/`
  - `controller/` - REST controllers
  - `service/` - Business logic
  - `repository/` - Data access layer
  - `model/` - Entity classes
  - `dto/` - Data Transfer Objects
  - `exception/` - Custom exceptions
- `src/test/` - Test classes
  - `java/` - Unit and integration tests
  - `resources/` - Test resources

## Troubleshooting

1. Database Connection Issues
- Ensure PostgreSQL container is running: `docker ps`
- Check database logs: `docker-compose logs postgres`
- Verify network connectivity: `docker network ls`

2. Build Issues
- Clean and rebuild: `./mvnw clean install`
- Check Java version: `java -version`
- For Docker builds, ensure you're in the project root directory

3. Test Failures
- Run tests with debug output: `./mvnw test -X`
- Check test reports in `target/surefire-reports/`

## Additional Notes
- The application uses an in-memory H2 database for tests
- PostgreSQL is used for the main application
- All API endpoints return JSON responses
- Error responses include status, error, and message fields
- The Docker container needs to be on the same network as the PostgreSQL container
