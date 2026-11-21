--
-- PostgreSQL database dump
--

\restrict a9VlUb0QzConK5aVEihnZbHlCX8l7IMgLYNqR92bYGDri9HLtb1GN1RnbPQBKlE

-- Dumped from database version 18.6
-- Dumped by pg_dump version 18.7 (Debian 18.7-1.pgdg13+5)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: -
--

-- *not* creating schema, since initdb creates it


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: lines; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.lines (
    line_id text NOT NULL,
    name text,
    category text,
    vehicle_type text NOT NULL,
    agency_name text,
    agency_code text
);


--
-- Name: path_segments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.path_segments (
    segment_id integer NOT NULL,
    from_station_id text NOT NULL,
    to_station_id text NOT NULL,
    network text NOT NULL,
    length_m integer NOT NULL,
    points jsonb NOT NULL,
    has_tunnel boolean,
    is_estimated boolean NOT NULL
);


--
-- Name: stop_events; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.stop_events (
    service_date date NOT NULL,
    trip_id text NOT NULL,
    stop_sequence integer NOT NULL,
    station_id text NOT NULL,
    platform_sloid text,
    scheduled_arrival timestamp with time zone,
    scheduled_departure timestamp with time zone,
    actual_arrival timestamp with time zone,
    actual_departure timestamp with time zone,
    arrival_status text NOT NULL,
    departure_status text NOT NULL,
    is_stop_cancelled boolean NOT NULL,
    is_pass_through boolean NOT NULL,
    next_segment_id integer
);


--
-- Name: station_day_stats; Type: MATERIALIZED VIEW; Schema: public; Owner: -
--

CREATE MATERIALIZED VIEW public.station_day_stats AS
 SELECT station_id,
    service_date,
    (count(*))::integer AS n_events,
    (count(*) FILTER (WHERE is_stop_cancelled))::integer AS n_cancelled,
    (round(avg(EXTRACT(epoch FROM (actual_departure - scheduled_departure))) FILTER (WHERE ((departure_status = 'MEASURED'::text) AND (NOT is_stop_cancelled)))))::integer AS avg_dep_delay_s,
    ((100.0 * avg((((actual_departure - scheduled_departure) < '00:03:00'::interval))::integer) FILTER (WHERE ((departure_status = 'MEASURED'::text) AND (NOT is_stop_cancelled)))))::real AS pct_on_time
   FROM public.stop_events
  GROUP BY station_id, service_date
  WITH NO DATA;


--
-- Name: stations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.stations (
    station_id text NOT NULL,
    sloid text,
    name text NOT NULL,
    latitude double precision NOT NULL,
    longitude double precision NOT NULL,
    elevation real
);


--
-- Name: trips; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.trips (
    service_date date NOT NULL,
    trip_id text NOT NULL,
    trip_number text,
    line_id text NOT NULL,
    is_cancelled boolean NOT NULL,
    is_extra_trip boolean NOT NULL,
    first_departure timestamp with time zone,
    last_arrival timestamp with time zone,
    stop_count integer NOT NULL
);


--
-- Name: lines lines_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.lines
    ADD CONSTRAINT lines_pkey PRIMARY KEY (line_id);


--
-- Name: path_segments path_segments_from_station_id_to_station_id_network_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.path_segments
    ADD CONSTRAINT path_segments_from_station_id_to_station_id_network_key UNIQUE (from_station_id, to_station_id, network);


--
-- Name: path_segments path_segments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.path_segments
    ADD CONSTRAINT path_segments_pkey PRIMARY KEY (segment_id);


--
-- Name: stations stations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stations
    ADD CONSTRAINT stations_pkey PRIMARY KEY (station_id);


--
-- Name: stop_events stop_events_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stop_events
    ADD CONSTRAINT stop_events_pkey PRIMARY KEY (service_date, trip_id, stop_sequence);


--
-- Name: trips trips_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.trips
    ADD CONSTRAINT trips_pkey PRIMARY KEY (service_date, trip_id);


--
-- Name: station_day_stats_key; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX station_day_stats_key ON public.station_day_stats USING btree (station_id, service_date);


--
-- Name: path_segments path_segments_from_station_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.path_segments
    ADD CONSTRAINT path_segments_from_station_id_fkey FOREIGN KEY (from_station_id) REFERENCES public.stations(station_id);


--
-- Name: path_segments path_segments_to_station_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.path_segments
    ADD CONSTRAINT path_segments_to_station_id_fkey FOREIGN KEY (to_station_id) REFERENCES public.stations(station_id);


--
-- Name: stop_events stop_events_next_segment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stop_events
    ADD CONSTRAINT stop_events_next_segment_id_fkey FOREIGN KEY (next_segment_id) REFERENCES public.path_segments(segment_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: stop_events stop_events_service_date_trip_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stop_events
    ADD CONSTRAINT stop_events_service_date_trip_id_fkey FOREIGN KEY (service_date, trip_id) REFERENCES public.trips(service_date, trip_id);


--
-- Name: stop_events stop_events_station_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stop_events
    ADD CONSTRAINT stop_events_station_id_fkey FOREIGN KEY (station_id) REFERENCES public.stations(station_id);


--
-- Name: trips trips_line_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.trips
    ADD CONSTRAINT trips_line_id_fkey FOREIGN KEY (line_id) REFERENCES public.lines(line_id);


--
-- PostgreSQL database dump complete
--

\unrestrict a9VlUb0QzConK5aVEihnZbHlCX8l7IMgLYNqR92bYGDri9HLtb1GN1RnbPQBKlE

