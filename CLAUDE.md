# Bytecode Bay 2026 — conference demo

Demo project for an internal company talk. It shows an AI agent answering questions from **private data**. The data is a conference schedule for Bytecode Bay 2026, a conference that doesn't exist. Because nothing about it is on the internet, the agent can only answer correctly by calling tools against our backend.

The demo is built up in stages, one stage per branch. `main` is the plain app with no AI: a schedule browser. Later branches add a chat box in the UI and an agent in the backend that uses tools to query the database. Keep each stage small and easy to follow, because the code will be shown on screen.

## Layout

- `backend/`: Micronaut 5 + Java 25 REST API on PostgreSQL. See `backend/CLAUDE.md`.
- `frontend/`: React + TypeScript + Vite UI. See `frontend/CLAUDE.md`.
- `docker-compose.yml`: Postgres 17 on `localhost:5432` (db, user and password are all `conference`).

## Running locally

```bash
docker compose up -d                       # database
cd backend && ./mvnw mn:run                # API on :8080 (or run Application from the IDE)
cd frontend && npm install && npm run dev  # UI on :5173, proxies /api to :8080
```

Reset the data with `docker compose down -v`.

## Conventions

- The conference data is made up on purpose. Keep it internally consistent: speakers must not be double-booked, rooms must not clash, and bios should stay specific and quirky, because they make good questions for the agent.
- Prefer clear demo code over clever abstractions. No extra libraries unless they make the demo easier to follow.
