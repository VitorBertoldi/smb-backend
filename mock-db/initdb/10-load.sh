#!/bin/bash
# Builds the mock on the container's first start: the Azure schema (mock-db/schema.sql, from
# pg_dump --schema-only) and one day of Azure data (mock-db/data, see mock-db/README.md).
set -euo pipefail
psql=(psql -q -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB")

"${psql[@]}" -f /mock-db/schema.sql

# Parents before children, for the foreign keys.
for table in lines stations path_segments trips stop_events; do
	"${psql[@]}" -c "\\copy public.$table FROM PROGRAM 'gunzip -c /mock-db/data/$table.csv.gz' WITH (FORMAT csv, HEADER)"
done

# The same read-only user as on Azure, with a local password.
"${psql[@]}" <<'SQL'
REFRESH MATERIALIZED VIEW public.station_day_stats;
ANALYZE;
CREATE ROLE backenduser LOGIN PASSWORD 'dev';
GRANT USAGE ON SCHEMA public TO backenduser;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO backenduser;
SQL
