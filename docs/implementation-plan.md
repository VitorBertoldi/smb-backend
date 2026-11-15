# Swiss Music Box: Backend Implementation Plan

For the domain group. The order in which we build what `docs/domain-model.md` describes, on top of
the data group's Istdaten gold tables. Each phase ends with a **gate**: a check that must pass
before the next phase starts.

The phases follow one idea: **build from the inside out**. The domain is written and tested with
hand-made objects first, so we never wait for the database. The database arrives in phase 4, and
the frontend gets endpoints in phase 5.

| Phase | Builds | Needs from the data group | Gate | Status (15 Nov 2026) |
|---|---|---|---|---|
| 0 Setup | Project structure, tests, CI | Answers to the open questions (domain model, section 13) | `./mvnw verify` green; questions settled | Code done; GitHub repo and answers to do |
| 1 Values | `geo`, `time` | — | Unit tests for time, daylight saving, delays | Done |
| 2 Trips | `network`, `timetable` | — | IC 712 fixture timelines correct on both bases | Done |
| 3 Vehicles | `vehicle` | — | Position table of the fixture passes | Done |
| 4 Repositories | `repository` | Gold image with one day | One full day loads; counts match the data group's | Code done, tested on a copy of the schema; gate waits for the gold image |
| 5 API | `service`, `controller` | — | Frontend shows moving vehicles from `/movements` | Code done, tested end to end; gate waits for the frontend |
| 6 Real paths | Nothing new: checks only | `path_segments` phase 2 (OSM geometry, `has_tunnel`) | Vehicles follow tracks; `estimated` mostly false | Waiting for the data |
| 7 Later | Performance, show clock, more days | More service dates | Agreed when we get there | |

To try the API by hand before the gold image exists: `./mvnw spring-boot:test-run` starts the
backend on a throw-away PostgreSQL with the sample rows (`TestSmbBackendApplication`); open
http://localhost:8080/swagger-ui.html. The data is gone when it stops.

`./mvnw verify` runs 127 tests (Docker must be running): unit tests for the model, in-memory
tests for the services, and integration tests against PostgreSQL for the repositories and the
whole API.

---

## Phase 0: Setup

**Goal:** an empty but correctly shaped project, and a settled contract.

| Item | Where | Status |
|---|---|---|
| Spring Boot 4.1.2, Java 21, Maven | `pom.xml` | Done |
| **JPA** (Spring Data JPA, Hibernate 7.4) instead of plain JDBC | `spring-boot-starter-data-jpa` | Done |
| Read-only connection; Hibernate checks entities against the gold schema and never changes it | `application.properties`: `hikari.read-only`, `ddl-auto=validate`, schema `transport`, `open-in-view=false` | Done |
| Packages `controller`, `service`, `repository`, `model` (7 sub-packages), each with a `package-info.java` saying what belongs there | `src/main/java/ch/usi/smb_backend` | Done |
| Tests: JUnit 6 (Jupiter) and AssertJ, from the Spring test starters | `pom.xml` | Done |
| **ArchUnit** rules: `model` uses no Spring, JPA, Hibernate, JDBC or Jackson; layers depend inwards; `model` packages depend downwards | `ArchitectureTest` | Done, checked with a deliberate violation |
| **Testcontainers** 2.0: a PostgreSQL 17 for tests that start Spring | `TestcontainersConfiguration` | Done |
| CI running `./mvnw -B verify` on every push | `.github/workflows/ci.yml` | Written; runs once the repo is on GitHub |
| GitHub repository for `smb-backend` | | To do |
| Answers to the open questions (domain model, section 13) | Data group | To do |

How JPA is used, so the domain stays plain Java:

- **JPA entities are not model classes.** Entities (`TripEntity`, `StopEventEntity`, …) live
  in `repository`, marked `@Immutable`. A mapper turns them into model objects, whose
  constructors check the rules.
- **The model's repository interfaces stay** (`model.port`). `JpaTripRepository implements TripRepository`
  uses a Spring Data repository inside; Spring Data never leaks out of `repository`.
- **No N+1 queries.** Trips that are needed with their stop events are fetched in one query
  (`JOIN FETCH` or an entity graph), in `@Transactional(readOnly = true)` methods.
- **JDBC stays possible.** The JPA starter includes JDBC, so a query too slow through Hibernate
  (most likely `/movements`, phase 7) can use `JdbcClient` without changing anything else.

`./mvnw verify` needs Docker running, for the Spring context test.

**Gate:** `./mvnw verify` passes (it does on 15 Nov 2026); the repo is on GitHub with CI green;
section 13 has an answer for every row.

## Phase 1: Value objects (`geo`, `time`)

**Goal:** the small building blocks every other class uses, fully tested.

| Class | Key tests |
|---|---|
| `GeoPoint`, `Polyline` | Range checks; `pointAt` interpolates and clamps; length 0; `then` joins two lines |
| `ServiceDay` | `parseClock("25:10")` and `formatClock` round-trip; 25 Oct 2026 (clocks go back) and 29 Mar 2026 (clocks go forward) |
| `ServiceTime` | Comparison, `max`, `secondsUntil`, values over 86,400 |
| `TimeStatus`, `EventTime` | `on(ACTUAL)` falls back to scheduled; `delay()` empty without an actual time; `isReliable` |
| `Delay` | Sign; `isOnTime` at 2:59 and 3:00; text form |

**Gate:** every class above has tests, and the tests run in under a second (no Spring context).

## Phase 2: Network and trips (`network`, `timetable`)

**Goal:** a `Trip` that is always valid, and the `TripTimeline` that interprets it.

1. `StationId`, `Station`, `Level`, `Platform`, `Operator`, `Line`, `VehicleType`,
   `NetworkType`, `PathSegment`.
2. `TripKey`, `StopEvent` with its rules and `stopsOn(basis)`.
3. `Trip` with its constructor rules, `existsOn`, `destination`, `displayName`.
4. `Stop`, `Leg` (joining segments, 0-second legs, straight-line fallback).
5. `TripTimeline`: the four building steps and the out-of-order rule (domain model, 7.1 and 7.2),
   binary search for `stopAt` and `legAt`.
6. `Departure`.

Fixtures live in one test helper (`TestTrips`), built by hand: IC 712 from the domain model,
plus one trip each with a cancelled stop, a cancelled trip, an extra trip, out-of-order actual
times, and a missing path.

**Gate:** every row of the odd-cases table (domain model, 7.3) has a test that passes.

## Phase 3: Vehicles (`vehicle`)

**Goal:** positions, on both time bases.

- `MotionProfile`, `Vehicle` with the Template Method, the six subclasses, `VehicleFactory`.
- `VehicleStatus`, `VehiclePosition` with its three factory methods.

**Gate:** the IC 712 position table (domain model, section 10) passes; the factory test covers
every `VehicleType`.

At the end of phase 3 the domain is complete and has never touched a database.

## Phase 4: Repositories (`repository`)

**Goal:** real data in, domain objects out.

1. Point `compose.yaml` at the first gold image the data group publishes (the schema name is set
   in `application.properties`).
2. **Entities**, one per gold table, `@Immutable`, in `repository`:
   - `StationEntity`, `LineEntity`, `PathSegmentEntity` (`points` as JSON with
     `@JdbcTypeCode(SqlTypes.JSON)`).
   - `TripEntity` with an `@EmbeddedId` (`service_date`, `trip_id`); `StopEventEntity` with an
     `@EmbeddedId` (`service_date`, `trip_id`, `stop_sequence`); timestamps as `OffsetDateTime`.
   - `StationDayStatsEntity` for the view.
   Starting the app now checks every entity against the real schema (`ddl-auto=validate`): a
   mismatch stops the start-up with the column name.
3. **Mappers** from entities to model objects. Timestamps become `ServiceTime` through
   `ServiceDay`, here only.
4. **Repositories** implementing the `model.port` interfaces, each using a Spring Data repository:
   - `JpaStationRepository`, `JpaLineRepository`, `JpaPathSegmentRepository`: load everything at
     start-up into memory (`Map` by id).
   - `JpaTripRepository`: `findByKey` (one trip with its stop events), `findRunningAt` (filter on
     `first_departure`/`last_arrival` with a margin for delays, stop events fetched in the same
     query, then the exact check in `TripTimeline`), `findDepartingFrom`, `serviceDates`.
   - `JpaStationStatsRepository`.
5. **Tests:** `JpaRepositoriesIntegrationTest` (`@DataJpaTest` with Testcontainers). The
   container starts from `src/test/resources/db/gold-schema.sql`, our copy of the gold model
   written from the data group's document, plus `gold-sample-data.sql` (IC 712, a cancelled and
   an extra trip). **Replace `gold-schema.sql` with the data group's `schema.sql` as soon as it is
   published.** One smoke test against the full image, run by hand (too big for CI).

Choices made where the gold document is silent (to confirm, domain model section 13):

- The status columns are a PostgreSQL enum with Istdaten's labels; they are read as text.
- An actual time whose status is unknown is kept, as a `FORECAST`.
- A path point without a height, or a station without an elevation, is placed at 0 m.
- `pct_on_time` is a percentage (0–100); the model stores a share (0–1).

**Gate:** the app starts against the gold image (the schema check passes); the full day loads
without a single invalid `Trip`. Any invalid trip found is reported to the data group, not fixed
in Java. Counts of trips by vehicle type match the data group's validation report.

## Phase 5: Services and controllers (`service`, `controller`)

**Goal:** the endpoints in the domain model, section 11.

Order, from the simplest to the one the frontend needs most:

1. `GET /days`, `/stations`, `/stations/{id}`, `/lines`, `/paths`.
2. `GET /trips/{date}/{tripId}` and `/trips/{date}/{tripId}/position`.
3. `GET /stations/{id}/departures`, `/stations/stats`.
4. `GET /vehicles`.
5. `GET /movements`: the main endpoint for the animation.

Alongside:

- Request parsing in one place: `date`, `time` (`ServiceDay.parseClock`), `basis`, `type`;
  errors as in the domain model's error table, with `@ControllerAdvice`.
- DTO records in `controller.dto`, so model classes are never serialised directly.
- OpenAPI description (springdoc 3.2) at `/v3/api-docs`, so the frontend can generate its types;
  Swagger UI at `/swagger-ui.html`.
- Limits: a `/movements` window is at most 3 hours; station and line names must not be blank.
- Tests: services against in-memory repositories (`InMemoryTripRepository`, …), and
  `ApiIntegrationTest` for the whole stack, HTTP to PostgreSQL, including every error case.

**Gate:** the frontend group shows a day of vehicles moving from `/movements` and `/paths`, and
switches between `actual` and `scheduled`.

## Phase 6: Real paths

**Goal:** check that the domain needs no change when the data group delivers OSM geometry.

The data group's phase 2 fills `path_segments.points` with real geometry and sets `has_tunnel`.
The domain was written for this: `Polyline` handles any number of points, and `Leg.mayBeInTunnel`
reads `hasTunnel`.

- Update the image tag; run the phase 4 and 5 tests again.
- Check on the map: vehicles follow tracks, `estimated` is false for most legs, and
  `underground` reports `UNKNOWN` on legs with a tunnel.

**Gate:** no domain code changed; a sample of 20 trips looks right on the map.

## Phase 7: Later

To plan when phase 6 is done:

- Performance: response time of `/movements` for a 30-minute window at rush hour; caching of
  trips per day, or moving that one query to `JdbcClient`, if needed.
- The show clock (service time 04:00 → 28:00).
- Pass-through times as extra timing points inside a leg, for more accurate positions.
- More service dates; comparing days.
- Music, when that group starts.

---

## Working rules

| Rule | Why |
|---|---|
| Tests first for every domain rule | The rules are the product; they must be provable without a database |
| Domain classes immutable, records for values, rules in constructors | An invalid object can't exist |
| Descriptive names from the vocabulary (domain model, section 3) | Code, tests and conversation use the same words |
| No data fixes in Java | Report to the data group; interpreting measurements (7.2) is the only exception |
| One pull request per class group, reviewed by another member | Shared code, shared understanding |
| Contract changes only by agreement with the data group | The gold schema is the contract |
