# Connecting the front end to the backend

For the front-end group. This explains how our API (version 1) works, how the front end is meant to
use it, and how it compares with the API the front end currently calls (`d101_web/src/lib/api`,
"frontend.yaml v0.1.0"). The last section lists open questions to decide together.

The live, always-up-to-date contract is the OpenAPI document served by the backend:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 1. Why the front end goes through the domain API

Three groups, two contracts:

```
data group ──(gold database: transport.*)──▶ backend (domain) ──(HTTP API /api/v1)──▶ front end
              contract A                                         contract B
```

- **Contract A** is the gold database the data group publishes (stations, lines, path segments,
  trips, stop events). The backend reads it; the front end never reads it directly.
- **Contract B** is this HTTP API. The domain group owns it, and changes are agreed with the front
  end.

The current front-end contract (`station_id`, `elevation_m`, `is_underground`, `segment_id`,
`next_segment_id`, `time_quality`, …) mirrors the **database columns**. That means the front end
would have to work out the timetable logic itself: which times to trust (measured, forecast,
scheduled), delays, pass-throughs, cancelled stops, and how a vehicle moves between two stops. In
our design that logic belongs to the domain. The backend gives the front end **ready-to-animate
movements**, and the front end only draws them.

## 2. How our API works (version 1)

All endpoints are `GET`, under the base path **`/api/v1`**, and return JSON (gzip-compressed when it
is large).

| Endpoint | Purpose | How often the front end calls it |
|---|---|---|
| `/api/v1/days` | Which service days have data | Once at start-up (fills the date picker) |
| `/api/v1/stations` | All stations with their location | Once at start-up, then cached by id |
| `/api/v1/movements?date&from&to` | Every leg of every vehicle moving in a time window | Per time window (e.g. every 30–60 min of playback) |
| `/api/v1/paths?ids=…` | Geometry of path segments, by id | Only for ids not already cached |

### Units and conventions

- Field names are **camelCase**.
- Coordinates are `[longitude, latitude, heightM]` (WGS84, metres above sea level; height is `0` when
  unknown).
- **Service seconds** (fields ending in `S`): seconds since the start of the service day (local
  midnight, Europe/Zurich). They can exceed 86 400, because a trip running after midnight still
  belongs to the day it started on. To get a real instant:
  `instant = startOfServiceDay(date) + departS seconds`.
- Clock parameters (`from`, `to`) are wall-clock text: `"09:32"`, `"09:32:15"` or `"25:10"` (1:10 at
  night, still on the same service day).
- Errors are `application/problem+json` (RFC 9457):
  `{ "type", "title", "status", "detail", "instance" }`.

### `GET /api/v1/days`

```json
{ "dates": ["2026-10-20", "2026-10-21"] }
```

### `GET /api/v1/stations`

```json
[
  { "id": "8503000", "name": "Zürich HB",
    "location": { "longitude": 8.5402, "latitude": 47.3782, "heightM": 408.0 } },
  { "id": "8502113", "name": "Aarau",
    "location": { "longitude": 8.0510, "latitude": 47.3913, "heightM": 383.0 } }
]
```

### `GET /api/v1/movements?date=2026-10-21&from=08:00&to=09:00`

- `date`: the service day (`YYYY-MM-DD`). An unknown date returns **404** with the list of
  `availableDates`.
- `from`, `to`: clock times on that day. The window may be at most **3 hours**, and `from` must not
  be after `to` (otherwise **400**).
- Returns every **leg** (one hop from one station to the next) that is moving during the window,
  once.

```json
[
  {
    "trip": { "date": "2026-10-21", "id": "85:11:712:001" },
    "displayName": "IC 712",
    "vehicleType": "TRAIN",
    "pathIds": [1],
    "fromStation": { "id": "8503000", "name": "Zürich HB" },
    "toStation":   { "id": "8502113", "name": "Aarau" },
    "departS": 30120,
    "arriveS": 31800,
    "motion": "SMOOTH",
    "delayS": 60,
    "estimated": true
  }
]
```

| Field | Meaning |
|---|---|
| `trip` | The trip's key (service date + Istdaten trip id). Every leg of the same trip has the same key. |
| `displayName` | Label for passengers: `category + number` (e.g. `IC 712`), or the line name. |
| `vehicleType` | `TRAIN`, `RACK_RAILWAY`, `METRO`, `TRAM`, `BUS`, `BOAT`, `FUNICULAR`, `CABLE_CAR`. Chooses the 3D model and sound. |
| `pathIds` | Path segments to follow, in travel order. Several ids when the train passes stations without stopping. **Empty = straight line** between the two stations (buses and trams today). |
| `departS` / `arriveS` | When the vehicle leaves `fromStation` / reaches `toStation`, in service seconds. These are the **actual** times (measured or forecast), already chosen by the backend. |
| `motion` | `SMOOTH` (accelerate, then brake: trains, boats…) or `LINEAR` (constant speed). |
| `delayS` | Departure delay in seconds, or `null` when unknown. Informational only; the times already include it. |
| `estimated` | `true` when there is no real track geometry (straight line). |

Between `arriveS` of one leg and `departS` of the next one, the vehicle is **standing at the
station**.

### `GET /api/v1/paths?ids=1,2`

Unknown ids are left out. The front end should keep a cache by id and only ask for missing ones.

```json
[
  { "id": 1, "fromStation": "8503000", "toStation": "8502113", "network": "TRACK",
    "estimated": true, "hasTunnel": false,
    "coordinates": [[8.5402, 47.3782, 408.0], [8.0510, 47.3913, 383.0]] }
]
```

`network` is one of `TRACK`, `ROAD`, `WATER`, `ROPE`. Today the data group only provides 2-point
(straight) paths. Later the same ids get the real track shape with hundreds of points, without any
change in the API or the front-end code.

### How the front end animates a vehicle (every frame)

```ts
// now: playback time in service seconds
const t = clamp((now - m.departS) / (m.arriveS - m.departS), 0, 1)  // share of time
const d = m.motion === "SMOOTH" ? t * t * (3 - 2 * t) : t           // share of distance
const line = m.pathIds.length
  ? join(m.pathIds.map((id) => pathCache.get(id)!.coordinates))
  : [stationById(m.fromStation.id).location, stationById(m.toStation.id).location]
const position = pointAtFraction(line, d)                           // [lon, lat, height]
```

The backend uses exactly the same `SMOOTH` formula, so the front end and the backend agree on where a
vehicle is.

Typical loop:

1. At start-up: `/days` and `/stations`.
2. When the playback enters a new window: `/movements` for the next window (prefetch a little
   before the current one ends), then `/paths` for the `pathIds` not cached yet.
3. Every frame: the formula above for every movement where `departS <= now <= arriveS`.

## 3. Comparison with the current front-end client

### Endpoints

| Front end calls today | Our API | Status |
|---|---|---|
| `GET /stations?bbox&modes` → GeoJSON FeatureCollection | `GET /api/v1/stations` → plain list, all stations | Different shape; no `bbox`/`modes` filter |
| `GET /segments?bbox&modes` → GeoJSON LineStrings | `GET /api/v1/paths?ids=…` | Lookup by id only; no "all segments in an area" |
| `GET /trips?service_date&from&to&bbox&modes` → trips with all stops (max 2 h) | `GET /api/v1/movements?date&from&to` → legs to animate (max 3 h) | Different model: legs, not full trips |
| — | `GET /api/v1/days` | New for the front end |

### Parameters

| Front end | Ours |
|---|---|
| `service_date=2026-10-21` | `date=2026-10-21` |
| `from`/`to` as ISO date-time with offset (`2026-10-21T08:00:00+02:00`) | `from`/`to` as clock text (`08:00`, `25:10`) |
| `bbox=west,south,east,north` | not supported (see open questions) |
| `modes=TRAIN,BUS` | not supported yet (planned for version 2 as `type=`) |

### Fields

| Front end (`contract.ts`) | Ours | Note |
|---|---|---|
| `station_id`, `name` | `id`, `name` | |
| geometry `[lon, lat]` / `[lon, lat, elevation]` | `location { longitude, latitude, heightM }` | |
| `elevation_m` (nullable) | `location.heightM` (0 when unknown) | |
| `is_underground`, `modes` (per station) | — | Not exposed |
| `segment_id`, `from_station_id`, `to_station_id` | `id`, `fromStation`, `toStation` (in `/paths`) | |
| `network` (a `Mode`) | `network`: `TRACK`/`ROAD`/`WATER`/`ROPE` | Different meaning: what it runs on, not the vehicle |
| `length_m` | — | The front end can compute it from the coordinates |
| `is_estimated`, `has_tunnel` | `estimated`, `hasTunnel` | |
| `Trip.mode` | `vehicleType` (per movement) | See the mode table below |
| `Trip.line`, `category`, `operator` | `displayName` | Only the combined label |
| `Stop.arrival`/`departure` (ISO) | `arriveS`/`departS` (service seconds) | Already the right times to animate |
| `arrival_scheduled`/`departure_scheduled` | — | Not exposed in version 1 (version 2 adds `basis=scheduled`) |
| `delay_s` | `delayS` | |
| `time_quality` (REAL/ESTIMATED/FORECAST/SCHEDULED) | — | Only `estimated` (about the geometry) |
| `is_pass_through` | — | Pass-throughs are inside a leg: several `pathIds` |
| `is_stop_cancelled`, `is_cancelled`, `is_extra_trip` | — | Cancelled stops/trips are skipped by the backend |
| `next_segment_id` | `pathIds` | |
| Errors `{ error, message }` | `{ type, title, status, detail, instance }` | |

### Modes

| Front end `Mode` | Our `vehicleType` |
|---|---|
| `TRAIN` | `TRAIN` |
| `RACK_RAILWAY` | `RACK_RAILWAY` |
| `METRO` | `METRO` |
| `TRAM` | `TRAM` |
| `BUS` | `BUS` |
| `BOAT` | `BOAT` |
| `CABLE_RAILWAY` | `FUNICULAR` |
| `CABLE_CAR`, `CHAIRLIFT` | `CABLE_CAR` |
| `ELEVATOR`, `UNKNOWN` | — (not in the data) |

## 4. What the current front end does, mapped onto our API

Today the app shows a **drawer when the user clicks the map**: the nearest station, the nearest
segment, and the trips serving them in the selected window. With our API:

| Drawer need | With version 1 |
|---|---|
| Nearest station to the click | `/stations` once, then search locally (all of Switzerland fits in memory). |
| Nearest segment to the click | Only segments used by the loaded movements: collect their `pathIds`, load them with `/paths`, search locally. |
| Trips at the clicked station | `/movements` for the window, keep legs where `fromStation.id` or `toStation.id` matches. Shows name, type, times and delay. |
| Trips on the clicked segment | Same, keep legs whose `pathIds` contain the segment id. |
| Scheduled vs actual time, time quality, platform, operator, full stop list | **Not available yet**: see the open questions. |

## 5. Running both together locally

1. Start the database and the backend (from `smb-backend`):

   ```bash
   docker compose up -d     # the data group's database on localhost:5433
   ./mvnw spring-boot:run   # the API on http://localhost:8080
   ```

2. In `d101_web`, use a Vite dev proxy so the browser calls the same origin (no CORS setup needed in
   the backend during development). In `vite.config.ts`:

   ```ts
   server: {
     proxy: { "/api": "http://localhost:8080" },
   },
   ```

   and in `d101_web/.env.local`:

   ```bash
   VITE_API_BASE_URL=/api/v1
   ```

3. `npm install && npm run dev`, then open `http://localhost:5173`.

Without the proxy, the backend would need a CORS rule for `http://localhost:5173`. It has none today.

## 6. Open questions for the front-end and domain groups

1. **Adopt `/movements` for drawing vehicles.** The front end replaces its `/trips` model with
   movements for the animation. Agreed?
2. **Area filter (`bbox`).** Is it needed? Stations can be loaded once for the whole country. For
   `/movements`, a `bbox` would reduce the data when zoomed in, which we could add.
3. **Mode filter.** Planned for version 2 as `type=TRAIN,BOAT,…` on `/movements`. Should
   `/stations` get it too (this needs the modes per station, which we don't expose yet)?
4. **Details for the drawer.** A new endpoint for one trip, e.g.
   `GET /api/v1/trips/{date}/{id}`: the full stop list with scheduled and actual times, time
   quality, pass-throughs, cancellations, operator and line. Which fields does the drawer really need?
5. **Segments in an area.** Does the front end need to draw or click **all** segments (the network
   map), or only those vehicles are using? The first needs a new `/paths?bbox=…`.
6. **Format details.** Both sides can live with the others' choices, but we should pick one:
   camelCase vs snake_case, plain JSON vs GeoJSON, service seconds vs ISO date-times, problem+json
   vs `{ error, message }`. The backend's proposal is to keep its current choices: service seconds
   make the per-frame math cheap, and GeoJSON adds nothing that Cesium needs.
7. **Mode names.** Agree on one list (see the mode table). `CABLE_RAILWAY` vs `FUNICULAR` and
   `CHAIRLIFT` vs `CABLE_CAR` come from the data, so the data group should be asked too.
