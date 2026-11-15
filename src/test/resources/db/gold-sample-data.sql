-- A few rows of 21 Oct 2026, matching the model tests' IC 712 fixture. Times are invented.
INSERT INTO transport.stations VALUES
    ('8503000', 'ch:1:sloid:3000', 'Zürich HB', 47.3782, 8.5402, 408, TRUE),
    ('8502113', 'ch:1:sloid:2113', 'Aarau',     47.3913, 8.0510, 383, FALSE),
    ('8507000', 'ch:1:sloid:7000', 'Bern',      46.9490, 7.4391, 540, NULL);

INSERT INTO transport.lines VALUES
    ('11:IC1', 'IC1', 'IC', 'TRAIN', 'Schweizerische Bundesbahnen SBB', '11');

INSERT INTO transport.path_segments VALUES
    (1, '8503000', '8502113', 'TRACK', 37000, '[[8.5402, 47.3782, 408], [8.0510, 47.3913, 383]]', FALSE, TRUE),
    (2, '8502113', '8507000', 'TRACK', 61000, '[[8.0510, 47.3913, 383], [7.4391, 46.9490, 540]]', FALSE, TRUE);

INSERT INTO transport.trips VALUES
    ('2026-10-21', '85:11:712:001',  '712',  '11:IC1', FALSE, FALSE,
     '2026-10-21 09:32:00+02', '2026-10-21 10:28:00+02', 3),
    ('2026-10-21', '85:11:2512:001', '2512', '11:IC1', TRUE,  FALSE,
     '2026-10-21 11:00:00+02', '2026-10-21 12:00:00+02', 2),
    ('2026-10-21', '85:11:9999:001', '9999', '11:IC1', FALSE, TRUE,
     '2026-10-21 23:30:00+02', '2026-10-22 00:20:00+02', 2);

INSERT INTO transport.stop_events VALUES
    -- IC 712: leaves 70 s late, passes Aarau, forecast to reach Bern 2:40 late.
    ('2026-10-21', '85:11:712:001', 1, '8503000', 'ch:1:sloid:3000:503:34',
     NULL, '2026-10-21 09:32:00+02', NULL, '2026-10-21 09:33:10+02', NULL, 'REAL', FALSE, FALSE, 1),
    ('2026-10-21', '85:11:712:001', 2, '8502113', NULL,
     '2026-10-21 10:00:00+02', '2026-10-21 10:00:00+02', '2026-10-21 10:01:00+02', '2026-10-21 10:01:00+02',
     'REAL', 'REAL', FALSE, TRUE, 2),
    ('2026-10-21', '85:11:712:001', 3, '8507000', 'ch:1:sloid:7000:3:7',
     '2026-10-21 10:28:00+02', NULL, '2026-10-21 10:30:40+02', NULL, 'PROGNOSE', NULL, FALSE, FALSE, NULL),
    -- IC 2512: cancelled.
    ('2026-10-21', '85:11:2512:001', 1, '8503000', NULL,
     NULL, '2026-10-21 11:00:00+02', NULL, NULL, NULL, 'UNBEKANNT', TRUE, FALSE, 1),
    ('2026-10-21', '85:11:2512:001', 2, '8502113', NULL,
     '2026-10-21 11:30:00+02', NULL, NULL, NULL, 'UNBEKANNT', NULL, TRUE, FALSE, NULL),
    -- An extra trip across midnight, no path.
    ('2026-10-21', '85:11:9999:001', 1, '8502113', NULL,
     NULL, '2026-10-21 23:30:00+02', NULL, '2026-10-21 23:31:00+02', NULL, 'REAL', FALSE, FALSE, NULL),
    ('2026-10-21', '85:11:9999:001', 2, '8507000', NULL,
     '2026-10-22 00:20:00+02', NULL, NULL, NULL, NULL, NULL, FALSE, FALSE, NULL);

REFRESH MATERIALIZED VIEW transport.station_day_stats;
