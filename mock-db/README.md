# Mock database

A local copy of the data group's Azure database, so the backend runs without Azure access.

- `schema.sql`: the Azure schema, exactly (`pg_dump --schema-only`).
- `data/`: one service day, **2026-11-20**, every trip except buses: 29,202 trips,
  441,175 stop events, 2,233 stations, 5,628 path segments, 356 lines.
- `initdb/10-load.sh`: loads both on the container's first start and creates the read-only
  user `backenduser` / `dev`, like on Azure.

Start it with `docker compose up -d` from the project folder, then `./mvnw spring-boot:run`.
To take another day or a newer schema, run `export-day.sh` (it says how).

The tests use the same `schema.sql` with a few hand-written rows (`src/test/resources/db`), so
`./mvnw verify` fails as soon as our entities stop matching the real schema.
