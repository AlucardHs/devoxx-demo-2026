# Bytecode Bay 2026 — conference demo

A demo app for showing an AI agent working on **private data**. Bytecode Bay 2026 is a made-up conference: its talks, speakers and rooms exist only in this database, so an agent can only answer questions about them by calling tools against our backend. It can't rely on what the model already knows.

This branch (`main`) is the plain starting point: a schedule browser with no AI yet. Later stages build on it in separate branches.

| Folder      | Stack                                                                |
|-------------|----------------------------------------------------------------------|
| `backend/`  | Micronaut 5.2 · Java 25 · Micronaut Data JDBC · Flyway · PostgreSQL  |
| `frontend/` | React 19 · TypeScript · Vite                                         |

## Running it

Prerequisites: JDK 25, Node 22+, Docker.

```bash
# 1. Database (Postgres 17 on localhost:5432)
docker compose up -d

# 2. Backend on http://localhost:8080 (Flyway creates and seeds the schema on startup)
cd backend && ./mvnw mn:run

# 3. Frontend on http://localhost:5173 (proxies /api to the backend)
cd frontend && npm install && npm run dev
```

To reset the data: `docker compose down -v`.

## API

| Endpoint                | Returns                                   |
|-------------------------|-------------------------------------------|
| `GET /api/talks`        | All talks, with room and speakers         |
| `GET /api/talks/{id}`   | One talk                                  |
| `GET /api/speakers`     | All speakers                              |
| `GET /api/speakers/{id}`| One speaker                               |
| `GET /api/rooms`        | All rooms                                 |

## Data

The seed data is in `backend/src/main/resources/db/migration/V2__seed_bytecode_bay_2026.sql`: 2 days, 4 rooms, 6 tracks, 37 sessions and 24 speakers. The speaker bios include odd, specific details (who keeps bees, whose sloop is called "NullPointer") that make good questions for the agent later.

## Tests

```bash
cd backend && ./mvnw test   # spins up Postgres via Testcontainers, needs Docker
```
