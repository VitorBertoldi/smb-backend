# Swiss Music Box: Backend Implementation Plan

For the domain group. Rewritten on 16 Nov 2026.

## The idea: one small working version, then one feature at a time

The first build (commit `2500c8f`) did everything at once: 11 endpoints, before any of them was
used. This plan starts again from the smallest thing that is useful, and grows it in steps.

- **Version 1 does one thing: vehicles move on the map.** Nothing else.
- **Each later version adds one feature the user can see** (click a train, a departure board, …).
  It brings exactly the endpoint, service and model code that feature needs, with tests.
- **A version is done when the frontend uses it**, not when the backend compiles. We don't build
  the next one before that.

The code already written is not lost: it is kept on the tag `full-api-draft` (step 1.1) and
brought back piece by piece, each time reviewed and understood by the group.

| Version | The user can… | Endpoints added | Status |
|---|---|---|---|
| **Foundations** | – | – | Done |
| **1 Moving vehicles** | watch every vehicle of one day move on the map | `/days`, `/stations`, `/movements`, `/paths` | Code done (16 Nov); waiting for the frontend and the gold image |
| 2 Plan vs reality | switch between the timetable and what really happened; show only some vehicle kinds | `basis`, `type` parameters | |
| 3 Click a train | click a vehicle and see its trip: line, stops, times, delays | `/trips/{date}/{tripId}` | |
| 4 Departure board | click a station and see its departures, cancelled ones included | `/stations/{id}/departures` | |
| 5 Delay map | colour stations by the day's delays | `/stations/stats` | |
| 6 Real tracks | see vehicles follow the real tracks, not straight lines | nothing new (data group's paths, phase 2) | Waiting for the data |
| Later | spectator mode, music, more days, performance | decided when we get there | |

Not planned: `/stations?name=`, `/lines?name=`, `/vehicles`, `/trips/{…}/position`. Version 1
already gives the frontend all stations, so a search box can filter them itself, and the
frontend computes positions itself. Each comes back only if a feature needs it ("Later" below).

---

## Foundations (done)

What every version stands on. Built and tested in the first commit; nothing to do here except
keep it green.

| Part | What it is | Where |
|---|---|---|
| Project | Spring Boot 4.1.2, Java 21, Maven; packages `controller`, `service`, `repository`, `model` | `pom.xml`, `src/main/java/ch/usi/smb_backend` |
| Architecture rules | `model` uses no framework; layers depend inwards | `ArchitectureTest` |
| Model: values | `geo` (`GeoPoint`, `Polyline`), `time` (`ServiceDay`, `ServiceTime`, `EventTime`, …) | `model/geo`, `model/time` |
| Model: network and trips | `Station`, `Line`, `PathSegment`; `Trip` → `TripTimeline` → `Stop` / `Leg` | `model/network`, `model/timetable` |
| Model: vehicles | `Vehicle` and its kinds, `VehicleFactory`, `MotionProfile`, `Movement` | `model/vehicle` |
| Database access | JPA entities (read-only), mappers to the model, the `model.port` interfaces | `repository` |
| Test database | PostgreSQL in Docker with our copy of the gold schema and 3 sample trips | `TestcontainersConfiguration`, `src/test/resources/db` |
| Run by hand | `./mvnw spring-boot:test-run`, then http://localhost:8080/swagger-ui.html | `TestSmbBackendApplication` |
| CI | `./mvnw -B verify` on every push | `.github/workflows/ci.yml` (runs once the repo is on GitHub) |

How the layers work together is in `docs/domain-model.md`; **how a vehicle moves on the map**, from
the data group's rows to the frontend's frame, is its section 7.

---

## Version 1: Moving vehicles

**The user can:** open the app, and watch every train, tram, bus, boat and cable car of one day
move across Switzerland, as it really ran (actual times).

### The contract: four endpoints

| Endpoint | Gives | The frontend calls it |
|---|---|---|
| `GET /api/v1/days` | the dates in the database: `{"dates":["2026-10-21"]}` | once, at start |
| `GET /api/v1/stations` | **all** stations: id, name, location (longitude, latitude, height) | once, at start; cached |
| `GET /api/v1/movements?date&from&to` | each leg moving in the window: trip, vehicle type, from/to station ids, path ids, departure and arrival seconds, motion curve | once per window (at most 3 h; e.g. every 30 playback minutes) |
| `GET /api/v1/paths?ids=1,2` | the coordinates of those paths | for path ids it doesn't have yet; cached |

How the frontend uses them, every frame, with no request:

1. For each movement with `departS < now < arriveS`: share of time →
   motion curve (`LINEAR` or `SMOOTH`) → share of distance → point along the joined paths
   (or along the straight line between the two stations when `pathIds` is empty).
2. Between two movements of the same trip, the vehicle stands at the arrival station of the
   first one (coordinates from the cached stations).

Why each one is needed:

- **`/days`**: without it the frontend must hard-code a date, which breaks when the data group
  loads another day.
- **`/stations`**: vehicles waiting at a station, and legs without a path, need station
  coordinates. Movements carry only station ids (with names), so the coordinates must come from
  somewhere once.
- **`/movements`**: says what moves, when, and along which paths.
- **`/paths`**: says where those paths go. It's separate from movements because one path is
  used by hundreds of trips a day; it is sent once and cached.

### What version 1 kept from the first draft, and what it parked

| Layer | In version 1 | Parked on `full-api-draft` until its version |
|---|---|---|
| `controller` | `ServiceDayController` (`/days`), `StationController` (`/stations`, all), `MovementController` (`/movements`, was `VehicleController`), `PathController`, `ApiPaths`, `RequestParameters`, `ApiExceptionHandler`, `BadRequestException` | `WebConfiguration` and `CaseInsensitiveEnumConverterFactory` (2, for `basis=actual`), `TripController` (3), departures (4), stats (5), `/vehicles` (later); `LineController` and the name searches (dropped) |
| `controller/dto` | `ServiceDaysDto`, `StationDto` (id, name, location), `StationRefDto`, `MovementDto`, `TripRefDto`, `PathDto`, `LocationDto` | `TripDto`, `StopEventDto`, `EventTimeDto` (3), `DepartureDto` (4), `StationStatsDto` (5), `VehiclePositionDto` (later), `LineDto` |
| `service` | `ServiceDayService`, `NetworkService` (stations, paths), `VehicleTrackingService.movementsBetween` (actual times), `ModelConfiguration`, `UnknownServiceDateException` | `TripService` and `NotFoundException` (3), `DepartureBoardService` (4), `StationStatsService` (5), `positionsAt` (later) |
| `model` | `geo`, `time`, `network`, `timetable`, `vehicle` without positions; `port`: `StationRepository.findAll`, `PathSegmentRepository`, `TripRepository` (`serviceDates`, `findCandidatesRunningBetween`) | `findByKey` (3); `Departure`, `findCandidatesDepartingBetween` (4); `statistics`, `StationStatsRepository` (5); `VehiclePosition`, `VehicleStatus`, `Vehicle.positionAt` (later); `LineRepository` |
| `repository` | entities for `stations`, `lines`, `path_segments`, `trips`, `stop_events`; `ReferenceDataCache`, the mappers; `JpaStationRepository`, `JpaPathSegmentRepository`, `JpaTripRepository` | `StationDayStatsEntity` and `JpaStationStatsRepository` (5), `JpaLineRepository` |

The model packages `time`, `network` and `timetable` stay whole even where version 1 doesn't
use every field (platforms, delays, levels): they describe the data group's tables, and later
versions read them.

### Steps

| # | Step | Done when |
|---|---|---|
| 1.1 | Tag the current code: `git tag full-api-draft` | Done |
| 1.2 | Remove the parked code from `main` (table above), with its tests | Done: 106 tests green, `ArchitectureTest` included |
| 1.3 | Add `GET /stations` returning every station, from the cache that already holds them | Done: `JpaRepositoriesIntegrationTest` and `ApiIntegrationTest` cases |
| 1.4 | Turn on response compression (`server.compression.enabled=true`): the station list and paths are the largest responses | Done: `curl --compressed` shows `Content-Encoding: gzip` |
| 1.5 | Make `/movements` use actual times only (drop `basis` and `type` for now) | Done; `ApiIntegrationTest` checks that OpenAPI lists exactly the four endpoints |
| 1.6 | Update `docs/domain-model.md` section 11 to the four endpoints | Done; later classes and endpoints are marked with their version |
| 1.7 | Put the repo on GitHub; share the OpenAPI link with the frontend group | CI green |
| 1.8 | Data: point `compose.yaml` at the data group's first gold image; replace `gold-schema.sql` with their `schema.sql` | The app starts against it (`ddl-auto=validate` passes); one full day loads without an invalid trip; trip counts by vehicle type match the data group's report |

### Gate

The frontend group shows one full day of vehicles moving, from these four endpoints only,
first on the sample data (`spring-boot:test-run`) and then on the gold image. A sample of
10 trips checked against their timetable: right place at the right time.

---

## Version 2: Plan vs reality, and vehicle kinds

**The user can:** switch between "timetable" and "what really happened", and show only trains,
only buses, etc.

| Adds | Detail |
|---|---|
| `basis=actual\|scheduled` on `/movements` | The model already builds both timelines (`Trip.timeline(basis)`); this is only the parameter |
| `type=TRAIN&type=BUS…` on `/movements` | Filter on the line's `VehicleType` |
| Tests | A trip with a delay appears at different times on each basis; a cancelled trip moves only on `scheduled` |

**Gate:** the frontend has a plan/reality toggle and vehicle-kind checkboxes.

## Version 3: Click a train

**The user can:** click a moving vehicle and see its trip: line and operator, every stop with
planned and actual times, delays, platforms, stations passed without stopping.

| Adds | Detail |
|---|---|
| `GET /trips/{date}/{tripId}` | `TripController`, `TripService`, `TripDto`, `StopEventDto`, `EventTimeDto` (from the tag) |
| Port | `TripRepository.findByKey` |
| Tests | IC 712 in full; an unknown trip gives 404 |

The trip id is already in every movement (`trip.id`), so the click needs nothing else.

**Gate:** clicking a vehicle opens its trip panel.

## Version 4: Departure board

**The user can:** click a station and see the departures in a time window, cancelled trips
included (they never move, so they are not in `/movements`).

| Adds | Detail |
|---|---|
| `GET /stations/{id}/departures?date&from&to` | `DepartureBoardService`, `Departure`, `DepartureDto` |
| Port | `TripRepository.findCandidatesDepartingBetween` |
| Tests | Zürich HB: IC 712 at 09:32 (+70 s, platform 34), IC 2512 at 11:00 cancelled |

**Gate:** clicking a station opens its board.

## Version 5: Delay map

**The user can:** see stations coloured by the day's punctuality.

| Adds | Detail |
|---|---|
| `GET /stations/stats?date` | `StationStatsService`, `StationDayStats`, `StationStatsRepository` and its JPA side, reading the data group's `station_day_stats` view |
| To confirm with the data group | Is `pct_on_time` 0–100 or 0–1? Which delay counts as on time? |

**Gate:** the frontend draws the layer for one day.

## Version 6: Real tracks

**The user can:** see vehicles follow rails, roads and lake routes instead of straight lines.

Nothing to build: the data group's phase 2 fills `path_segments.points` with real geometry and
sets `has_tunnel`. `Polyline` handles any number of points, and the API sends path ids, so the
contract doesn't change.

- Update the image tag; run all tests.
- Check on the map that `estimated` is false for most legs.

**Gate:** no domain code changed; 20 sample trips follow their tracks.

## Later (decide when we get there)

| Idea | Would bring back or add |
|---|---|
| Spectator mode ("which vehicle is near me?") | `/vehicles` snapshot (`VehiclePosition`, from the tag) |
| A server-side station search, if the station list gets too big for the browser | `/stations?name=` |
| Performance: `/movements` at rush hour | caching trips per day, or that one query in plain SQL (`JdbcClient`) |
| The show clock (service time 04:00 → 28:00), more days, comparing days | `ServiceDay` is ready for times past midnight |
| Music | when that group starts |

---

## Working rules

| Rule | Why |
|---|---|
| One version at a time; the next starts after the frontend uses the last | We build only what is used, and understand all of it |
| Every version adds tests for what it adds | The rules are the product; they must be provable without a database |
| Bring code back from `full-api-draft` only when its version starts, and review it like new code | Nobody owns code they haven't read |
| Domain classes immutable, records for values, rules in constructors | An invalid object can't exist |
| No data fixes in Java | Report to the data group; interpreting measurements is the only exception |
| One pull request per step, reviewed by another member | Shared code, shared understanding |
| Contract changes only by agreement: the gold schema with the data group, the API with the frontend group | Both are shared contracts |
