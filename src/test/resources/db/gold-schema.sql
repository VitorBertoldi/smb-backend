-- The data group's gold model ("Istdaten pipeline: Bronze and Gold", 11 Nov 2026), written out
-- for tests until their schema.sql is published. Types follow the document; the enum's name and
-- labels are our proposal (domain model, section 13).
CREATE SCHEMA transport;

CREATE TYPE transport.time_status AS ENUM ('REAL', 'GESCHAETZT', 'PROGNOSE', 'UNBEKANNT');

CREATE TABLE transport.stations (
    station_id     TEXT PRIMARY KEY,
    sloid          TEXT NOT NULL,
    name           TEXT NOT NULL,
    latitude       DOUBLE PRECISION NOT NULL,
    longitude      DOUBLE PRECISION NOT NULL,
    elevation      REAL,
    is_underground BOOLEAN
);

CREATE TABLE transport.lines (
    line_id      TEXT PRIMARY KEY,
    name         TEXT NOT NULL,
    category     TEXT NOT NULL,
    vehicle_type TEXT NOT NULL,
    agency_name  TEXT NOT NULL,
    agency_code  TEXT NOT NULL
);

CREATE TABLE transport.path_segments (
    segment_id      INTEGER PRIMARY KEY,
    from_station_id TEXT NOT NULL REFERENCES transport.stations,
    to_station_id   TEXT NOT NULL REFERENCES transport.stations,
    network         TEXT NOT NULL,
    length_m        INTEGER NOT NULL,
    points          JSONB NOT NULL,
    has_tunnel      BOOLEAN,
    is_estimated    BOOLEAN NOT NULL,
    UNIQUE (from_station_id, to_station_id, network)
);

CREATE TABLE transport.trips (
    service_date    DATE NOT NULL,
    trip_id         TEXT NOT NULL,
    trip_number     TEXT,
    line_id         TEXT NOT NULL REFERENCES transport.lines,
    is_cancelled    BOOLEAN NOT NULL,
    is_extra_trip   BOOLEAN NOT NULL,
    first_departure TIMESTAMPTZ NOT NULL,
    last_arrival    TIMESTAMPTZ NOT NULL,
    stop_count      INTEGER NOT NULL,
    PRIMARY KEY (service_date, trip_id)
);

CREATE TABLE transport.stop_events (
    service_date        DATE NOT NULL,
    trip_id             TEXT NOT NULL,
    stop_sequence       INTEGER NOT NULL,
    station_id          TEXT NOT NULL REFERENCES transport.stations,
    platform_sloid      TEXT,
    scheduled_arrival   TIMESTAMPTZ,
    scheduled_departure TIMESTAMPTZ,
    actual_arrival      TIMESTAMPTZ,
    actual_departure    TIMESTAMPTZ,
    arrival_status      transport.time_status,
    departure_status    transport.time_status,
    is_stop_cancelled   BOOLEAN NOT NULL,
    is_pass_through     BOOLEAN NOT NULL,
    next_segment_id     INTEGER REFERENCES transport.path_segments,
    PRIMARY KEY (service_date, trip_id, stop_sequence),
    FOREIGN KEY (service_date, trip_id) REFERENCES transport.trips
);

CREATE MATERIALIZED VIEW transport.station_day_stats AS
SELECT station_id,
       service_date,
       count(*)::INTEGER                                   AS n_events,
       (count(*) FILTER (WHERE is_stop_cancelled))::INTEGER AS n_cancelled,
       avg(extract(EPOCH FROM actual_departure - scheduled_departure))::INTEGER AS avg_dep_delay_s,
       (100.0 * avg(CASE WHEN actual_departure - scheduled_departure < INTERVAL '3 minutes' THEN 1 ELSE 0 END)
            FILTER (WHERE actual_departure IS NOT NULL))::REAL AS pct_on_time
  FROM transport.stop_events
 GROUP BY station_id, service_date
WITH NO DATA;
