#!/bin/bash
# Re-exports the mock from the data group's Azure database: the schema and one service day
# without buses (buses are ~85% of the stop events and would make the files too big for git).
#
#   AZURE_DB_PASSWORD=... mock-db/export-day.sh 2026-11-20
#   docker compose down -v && docker compose up -d      reload the mock with the new files
#
# Needs Docker and a network the Azure firewall allows (USI or VPN).
set -euo pipefail
day=${1:?usage: export-day.sh YYYY-MM-DD}
here=$(cd "$(dirname "$0")" && pwd)
conn="host=d101-swisstransport-db.postgres.database.azure.com port=5432 dbname=swisstransport user=backenduser sslmode=require"
pg=(docker run --rm -e "PGPASSWORD=${AZURE_DB_PASSWORD:?set AZURE_DB_PASSWORD}" postgres:18)

"${pg[@]}" pg_dump "$conn" --schema-only --no-owner --no-privileges --no-comments > "$here/schema.sql"

trips="SELECT t.* FROM trips t JOIN lines l USING (line_id) WHERE t.service_date = DATE '$day' AND l.vehicle_type <> 'BUS'"
events="SELECT s.* FROM stop_events s JOIN ($trips) t USING (service_date, trip_id)"
segments="SELECT p.* FROM path_segments p WHERE p.segment_id IN (SELECT next_segment_id FROM ($events) e)"
lines="SELECT * FROM lines WHERE line_id IN (SELECT line_id FROM ($trips) t)"
stations="SELECT * FROM stations WHERE station_id IN (SELECT station_id FROM ($events) e
  UNION SELECT from_station_id FROM ($segments) p UNION SELECT to_station_id FROM ($segments) p)"

export_table() {
	"${pg[@]}" psql "$conn" -v ON_ERROR_STOP=1 \
		-c "\\copy ($2 ORDER BY 1, 2) TO STDOUT WITH (FORMAT csv, HEADER)" | gzip -9 > "$here/data/$1.csv.gz"
	echo "$1: $(gunzip -c "$here/data/$1.csv.gz" | tail -n +2 | wc -l | tr -d ' ') rows"
}
mkdir -p "$here/data"
export_table lines "$lines"
export_table stations "$stations"
export_table path_segments "$segments"
export_table trips "$trips"
export_table stop_events "$events"
