# Backend

Micronaut 5.2 on Java 25, built with Maven (`./mvnw`). Uses Micronaut Data JDBC, Flyway and PostgreSQL. Serialization is Micronaut Serde (`@Serdeable`), not Jackson.

## Commands

```bash
./mvnw mn:run   # run (needs `docker compose up -d` from the repo root)
./mvnw test     # tests use a Testcontainers Postgres, so Docker must be running
```

## Structure

Code is in `src/main/java/com/example/conference`, with one package per feature: `room`, `speaker`, `talk`. Each holds a record entity, a repository and a controller. Everything is read-only under `/api/...`.

Database schema and seed data are Flyway migrations in `src/main/resources/db/migration`:
- `V1__schema.sql`: tables `room`, `speaker`, `talk` and the join table `talk_speaker`
- `V2__seed_bytecode_bay_2026.sql`: all conference data

## Gotchas

- Never edit a migration that has already been applied. Add a new `V3__...sql` instead, or reset the local DB with `docker compose down -v`.
- `@JoinTable` and `@JoinColumn` are in `io.micronaut.data.annotation.sql`, not `io.micronaut.data.annotation`.
- `Talk` fetch-joins `room` and `speakers`. Don't filter on a fetch-joined collection, for example `findBySpeakersId`: it drops co-speakers from the result. Query the IDs first, or filter in Java.
- `ConferenceApiTest` checks the seed counts (37 talks, 24 speakers, 4 rooms). Update it when the seed data changes.
- Local DB settings are in `application.properties` and can be overridden with `DB_URL`, `DB_USER` and `DB_PASSWORD`. Tests override the URL in `src/test/resources/application-test.properties`.
