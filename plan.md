# Pothole Detection Platform - Implementation Plan

## 0. Project Context

This file is the source of truth for future AI coding sessions. Follow the tasks in order and preserve the current technology choices unless the user explicitly changes them.

### How to use task checkboxes

- Open the Markdown preview in VS Code with `Ctrl+Shift+V` or `Ctrl+K`, then `V`.
- Click the checkbox next to a task to toggle it between incomplete and complete.
- In the source editor, checkboxes appear as Markdown text (`- [ ]` and `- [x]`) and are not interactive buttons.

### Current technology baseline

- Java: Eclipse Temurin 21 LTS
- Maven: 3.9.16
- Build system: Maven only; do not add Gradle files
- Spring Boot: 4.1.1 stable, selected because this is the current stable version shown by Spring Initializr
- Database: Supabase PostgreSQL
- Database connection: Supabase shared Session Pooler, port 5432, SSL required
- Authentication: JWT, stateless
- Image storage: Cloudinary free tier
- Deployment target: Render
- API client: Thunder Client
- Future optional spatial branch: PostGIS

### Important decisions

- Do not upgrade Java to Java 25 automatically. Keep Java 21 LTS.
- Do not replace Maven with Gradle.
- Do not commit secrets, passwords, JWT keys, or Cloudinary credentials.
- Use environment variables for database and external service credentials.
- Use Flyway for schema migrations. Do not rely on `ddl-auto=update` for the final implementation.
- Keep `confirm_count` and `user_id` out of public pothole response DTOs.
- The MVP core uses latitude/longitude plus bounding-box filtering and Haversine. PostGIS is an isolated experimental branch in Phase 12.

## Phase 0 - Environment Setup

### Task 0.1 - Install platform tools
- [x] Status: completed

**Purpose:** Make sure the toolchain works before writing application logic.

**Work:**

- Install Eclipse Temurin JDK 21 LTS.
- Install VS Code, Java Extension Pack, Spring Boot Extension Pack, and Thunder Client.
- Verify `java -version` and `mvn -version`.
- Verify the VS Code Command Palette contains Spring Initializr commands.

**Done when:** Java and Maven show the expected versions and VS Code recognizes Spring Initializr.

### Task 0.2 - Create cloud accounts and connection information
- [ ] Status: not completed

**Purpose:** Have infrastructure ready before database work.

**Work:**

- Create a Supabase project.
- Record the Session Pooler host, port 5432, username, database name, and password securely.
- Use SSL with `sslmode=require`.
- Create a Cloudinary account and record Cloud name, API key, and API secret securely.
- Create a Render account; deployment is not needed yet.
- Create `.env.example` with variable names only.
- Run `SELECT 1;` in Supabase SQL Editor.

**Done when:** Supabase SQL Editor runs `SELECT 1;` successfully and credentials are stored outside Git.

## Phase 1 - Project Skeleton

### Task 1.1 - Initialize Spring Boot project
- [x] Status: completed; `mvn -DskipTests package` passes

**Purpose:** Confirm the code-to-build pipeline.

**Work:**

- Use Maven, Java 21, Spring Boot 4.1.1, and Jar packaging.
- Use dependencies: Spring Web, Spring Data JPA, PostgreSQL Driver, Validation, Lombok, and Flyway Migration.
- Keep packages: `auth/`, `user/`, `pothole/`, `score/`, and `common/`.
- Run `mvn -DskipTests package` before database configuration.

**Done when:** `pom.xml` is present and the project compiles without dependency errors.

### Task 1.2 - `GET /ping` and standard response
- [x] Status: completed; `/ping` returned `{ "message": "pong" }`

**Purpose:** Confirm the Controller-to-HTTP response path.

**Work:**

- Keep `common/dto/ApiResponse.java` as the response wrapper.
- Keep `common/controller/PingController.java` with `GET /ping`.
- Return `{ "message": "pong" }`.
- After the database is configured, run `mvn spring-boot:run` and test with Thunder Client.

**Done when:** `GET http://localhost:8080/ping` returns the expected JSON.

## Phase 2 - Database Connection

### Task 2.1 - Configure datasource with environment variables
- [x] Status: completed; HikariPool connected to Supabase successfully

**Purpose:** Keep secrets out of source code and prepare for deployment.

**Work:**

- Configure `application.properties` with `${DATABASE_URL}`, `${DATABASE_USERNAME}`, and `${DATABASE_PASSWORD}`.
- Add `sslmode=require` to the JDBC URL.
- Optionally create `application-dev.properties` for local-only settings.
- Set environment variables through the terminal or VS Code launch configuration.
- Do not paste real credentials into `plan.md`, README, or chat.

**Done when:** the application starts without datasource errors and logs a started Hikari pool.

### Task 2.2 - Verify JPA with a temporary entity
- [x] Status: completed; JPA write/read was verified and temporary objects were removed

**Purpose:** Confirm JPA can read and write Supabase.

**Work:**

- Add a temporary `PingLog` entity with `id` and `message`.
- Temporarily use `ddl-auto=create` only for this check.
- Insert and query one record.
- Remove the temporary entity and test table after verification.

**Done when:** the table appears in Supabase, one record can be written/read, and the temporary objects are removed.

## Phase 3 - Flyway Schema

### Task 3.1 - Users migration
- [x] Status: completed; Flyway applied the users migration successfully

**Purpose:** Version and control the database schema.

**Work:**

- Set Hibernate DDL behavior to `none`.
- Add `V1__create_users.sql`.
- Create `users` with UUID or BIGINT primary key, name, unique email, password hash, role, total distance, and created timestamp.

**Done when:** `users` exists and `flyway_schema_history` contains the migration.

### Task 3.2 - Pothole reports migration
- [x] Status: completed; pothole_reports and its index are managed by Flyway

**Purpose:** Create the main table and indexes.

**Work:**

- Add `V2__create_pothole_reports.sql`.
- Include `user_id`, `lat`, `lng`, `region`, `severity`, `source`, `image_url`, `status`, `confirm_count`, `detected_at`, and `created_at`.
- Add `CREATE INDEX idx_pothole_region_lat_lng ON pothole_reports(region, lat, lng);`.

**Done when:** the table and index exist with the expected columns.

### Task 3.3 - User scores migration and entities
- [x] Status: completed; all three entities were initialized by Hibernate without mapping errors

**Purpose:** Complete the first schema and Java mappings.

**Work:**

- Add `V3__create_user_scores.sql`.
- Create `User`, `PotholeReport`, and `UserScore` entities.
- Map `PotholeReport -> User` with `@ManyToOne`.
- Make `user_scores.user_id` both primary key and foreign key.

**Done when:** the application starts without entity mapping errors or unexpected Hibernate warnings.

## Phase 4 - JWT Authentication

### Task 4.1 - Register DTO, validation, and repository
- [x] Status: completed; DTO validation tests and project build pass

- Create `RegisterRequestDTO` with `@NotBlank`, `@Email`, and `@Size(min = 8)`.
- Add `UserRepository.findByEmail`.
- Verify invalid email returns HTTP 400.

### Task 4.2 - Register service and BCrypt
- [x] Status: completed; BCrypt, register endpoint, duplicate-email handling, and end-to-end tests pass

- Check duplicate email.
- Hash passwords with BCrypt.
- Add `POST /api/v1/auth/register`.
- Verify the database never stores plaintext passwords.

### Task 4.3 - JWT service and login
- [x] Status: completed; JWT unit tests and HTTP login test pass with a runtime secret

- Add JJWT dependencies.
- Implement token generation, validation, and username extraction.
- Add `POST /api/v1/auth/login`.
- Return 401 for incorrect credentials.

### Task 4.4 - Security filter and endpoint rules
- [x] Status: completed; full tests and HTTP protected-endpoint checks pass

- Add `SecurityFilterChain`.
- Add `JwtAuthFilter` extending `OncePerRequestFilter`.
- Permit `/api/v1/auth/**`, `/ping`, `/swagger-ui/**`, and `/v3/api-docs/**`.
- Require authentication for protected APIs.
- Verify no token returns 401 and a valid token reaches a protected endpoint.

## Phase 5 - Basic Pothole CRUD

### Task 5.1 - Sensor DTO and mapper
- [ ] Status: not completed

- Create `SensorReportRequestDTO` with latitude, longitude, severity, region, and detected time.
- Create `PotholeResponseDTO` with id, latitude, longitude, severity, region, and status.
- Do not expose `confirmCount` or `userId`.
- Add a plain `PotholeMapper`.

### Task 5.2 - Create sensor report without deduplication
- [ ] Status: not completed

- Add `PotholeRepository`.
- Implement `POST /api/v1/potholes/sensor`.
- Take `user_id` from JWT, not the request body.
- Set `source=SENSOR`, `status=PENDING`, and `confirmCount=1`.

### Task 5.3 - Paginated list
- [ ] Status: not completed

- Add `findByRegion(String region, Pageable pageable)`.
- Add `GET /api/v1/potholes?region=&page=&size=`.
- Return a `PageResponse` containing content, total elements, page, and size.

## Phase 6 - Deduplication with Bounding Box and Haversine

### Task 6.1 - Candidate bounding-box query
- [ ] Status: not completed

- Implement `findCandidatesInBoundingBox(region, minLat, maxLat, minLng, maxLng)`.
- Test the repository query independently.

### Task 6.2 - Haversine decision logic
- [ ] Status: not completed

- Add `HaversineUtil.distanceInMeters(...)`.
- Use a 20-meter threshold.
- If a nearby report exists, increment `confirmCount`.
- If `confirmCount >= 3`, set status to `VERIFIED`.
- Otherwise create a new report.
- Use `@Transactional`.
- Later add concurrency testing so two simultaneous requests cannot create incorrect duplicates.

## Phase 7 - Manual Reports and Images

### Task 7.1 - Cloudinary service
- [ ] Status: not completed

- Add `cloudinary-http44`.
- Implement `CloudinaryService.upload(MultipartFile file)`.
- Test upload independently.

### Task 7.2 - Manual report endpoint
- [ ] Status: not completed

- Add `POST /api/v1/potholes/manual` using `multipart/form-data`.
- Validate file size and image MIME type.
- Upload to Cloudinary and save only `imageUrl`.
- Reuse deduplication logic.

## Phase 8 - Scores and Leaderboard

### Task 8.1 - Award points
- [ ] Status: not completed

- Use one transaction for report and score changes.
- New SENSOR report: 5 points.
- New MANUAL report: 10 points.
- A duplicate confirmation must not award another report point.
- Update `total_points` and `reports_count`.

### Task 8.2 - Leaderboard
- [ ] Status: not completed

- Add `findAllByOrderByTotalPointsDesc(Pageable pageable)`.
- Add `GET /api/v1/leaderboard?limit=50`.
- Verify descending score order.

## Phase 9 - Dashboard and Reports

### Task 9.1 - Personal dashboard
- [ ] Status: not completed

- Add `GET /api/v1/dashboard/me`.
- Return total reports, total distance, points, and current rank.
- Calculate rank as one plus the number of users with more points.

### Task 9.2 - Summary report
- [ ] Status: not completed

- Add `GET /api/v1/reports/summary?region=&period=daily|weekly`.
- Group by severity and filter by created time.
- Verify results against direct SQL queries.

## Phase 10 - Testing

### Task 10.1 - Auth tests
- [ ] Status: not completed

Test registration, duplicate email, correct login, incorrect password, validation, and protected endpoints without a token.

### Task 10.2 - Deduplication tests
- [ ] Status: not completed

Use Mockito to test nearby, non-nearby, confirm increment, and auto-verification threshold behavior.

### Task 10.3 - Score and leaderboard tests
- [ ] Status: not completed

Test new report points, duplicate report no extra points, and leaderboard ordering.

**Done when:** `mvn test` passes without skipped tests. Integration tests requiring a database must use a configured test database or Testcontainers; do not silently depend on a developer's production Supabase credentials.

## Phase 11 - Docker, Deploy, Swagger, and README

### Task 11.1 - Dockerfile
- [ ] Status: not completed

- Use a Maven build stage.
- Use `eclipse-temurin:21-jre` for runtime.
- Build and run locally.
- Verify `/ping` from outside the container.

### Task 11.2 - Render and Swagger
- [ ] Status: not completed

- Add `springdoc-openapi-starter-webmvc-ui` compatible with Spring Boot 4.
- Configure Render environment variables.
- Deploy the container.
- Verify Swagger UI and an auth request.

### Task 11.3 - Documentation
- [ ] Status: not completed

- Document architecture, ERD, environment variables, local setup, and deployment.
- Explain bounding box plus Haversine for the MVP.
- Explain why `confirm_count` is hidden from public responses.
- Explain the three main technical decisions in the portfolio writeup.

## Phase 12 - Optional PostGIS Experimental Branch

This phase is optional and must not break the MVP branch.

### Task 12.1 - PostGIS setup
- [ ] Status: not completed

- Create branch `feature/postgis-nearby`.
- Enable PostGIS.
- Add migration `V4` with `location geography(Point, 4326)`.
- Add a GiST index.
- Sync existing latitude/longitude values into `location`.
- Verify `SELECT PostGIS_Version();`.

### Task 12.2 - Experimental nearby endpoint
- [ ] Status: not completed

- Add a separate `GET /api/v1/potholes/nearby-experimental` endpoint.
- Use native `ST_DWithin` and `ST_Distance` queries.
- Do not replace the MVP list endpoint.
- Verify results are ordered by actual distance.

## Endpoint checklist

- `GET /ping`
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/potholes/sensor`
- `POST /api/v1/potholes/manual`
- `GET /api/v1/potholes`
- `GET /api/v1/potholes/{id}`
- `GET /api/v1/leaderboard`
- `GET /api/v1/dashboard/me`
- `GET /api/v1/reports/summary`
- Optional: `GET /api/v1/potholes/nearby-experimental`

## Working rules for future AI sessions

1. Read this file before making changes.
2. Check the current task status and repository diff.
3. Work on one task at a time.
4. Before editing, state the local hypothesis and the validation command.
5. After each edit, run the narrowest useful test or build command.
6. Do not overwrite user changes or reset the repository.
7. Do not add unrelated refactors.
8. Never expose or request secrets in chat.
9. Update the checkbox on the task itself when it is completed or blocked.
10. When a task is blocked by missing credentials, explain the exact user action required and stop at that boundary.

## Estimated effort

- 32 tasks, approximately 2 hours each
- Approximately 64 hours total
- Approximately 8 full-time days
- Approximately 2.5-3 weeks at 3-4 hours per evening
