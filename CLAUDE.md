# Bytecode Bay 2026 — conference demo

Demo project for an internal company talk. It shows an AI agent answering questions from **private data**. The data is a conference schedule for Bytecode Bay 2026, a conference that doesn't exist. Because nothing about it is on the internet, the agent can only answer correctly by calling tools against our backend.

The whole project is built live, in front of an audience, starting from an empty directory that holds only this file and `bytecode-bay-2026.csv`. The presenter asks for features in plain, natural language, the way a product owner would. They will **not** say which stage they are on or name the limits of a stage. This file tells you what each request really means.

## How to work out which stage you're building

Look at the code, not the wording of the request or the branch name:

| What the directory has now                      | What to build next        |
|-------------------------------------------------|---------------------------|
| Only this file and the CSV                      | The schedule app (no AI)  |
| The schedule app and chat UI, no AI in backend  | Stage 1                   |
| A chat agent with no tools                      | Stage 2                   |
| An agent with tools, but no guardrails          | Stage 3                   |

Build **only** the next step. Don't add anything from a later stage, even if the request seems to ask for it. For example, "build an assistant that can answer my questions about the conference" on the plain app means stage 1: an agent **without tools**, even though it then can't answer those questions. That gap is the point of the demo.

## While presenting

- The audience reads your output on a projector. **Never mention this file, the stage plan, or that something was left out on purpose.** Don't write "as per the plan", "stage 1", "no AI yet", "without tools for now", or hint at what comes next. Just build what was asked, and describe it naturally.
- Keep changes small and readable: they will be shown on screen. Prefer a few clear files over abstractions.
- Don't create branches or commit unless asked. The presenter manages git.
- **Speed matters more than coverage.** Don't write tests and don't run a test suite. To check that things work, only compile: run `./mvnw -q compile` in `backend/` and `npm run build` in `frontend/`. Fix whatever they report, then stop.
- Don't write a README, and don't create or update any `CLAUDE.md` file.

## The schedule app (built first)

A small, read-only web app for browsing the Bytecode Bay 2026 programme: a Micronaut backend serving the conference data from PostgreSQL, and a React frontend showing the schedule and the speakers. The frontend also includes the complete chat UI for the assistant from the start; it calls `POST /api/chat`, which doesn't exist until stage 1, so until then every question shows an error.

### Layout

```
docker-compose.yml   Postgres 17
backend/             Micronaut 5 + Java 25 REST API
frontend/            React + TypeScript + Vite UI
```

### Database: `docker-compose.yml`

One service, `postgres`, using the `postgres:17` image. The database, user and password are all `conference`. Expose port `5432`, keep the data in a named volume `postgres-data`, and add a `pg_isready` healthcheck.

### Data: `bytecode-bay-2026.csv`

All the conference data is in this file, denormalised: **one row per talk–speaker pair**. A talk with two speakers appears on two rows, and a speaker with two talks has their details repeated. It contains 37 talks, 24 speakers and 4 rooms.

Columns: `talk_id, title, description, track, level, format, starts_at, ends_at, room, room_floor, room_capacity, speaker, speaker_job_title, speaker_company, speaker_country, speaker_bio`. Times are local conference times (`2026-11-18 09:00`), with no time zone.

Turn the CSV into a Flyway seed migration (see below). Normalise it into rooms, speakers, talks and talk–speaker links:
- Talk ids: use `talk_id`.
- Room ids: 1 The Foundry, 2 Harbor Hall, 3 Lighthouse, 4 The Galley. The schedule sorts talks in the same slot by room id, so the main stage comes first.
- Speaker ids: number the speakers in order of first appearance in the CSV.

Copy every value exactly, including the bios: they are quirky on purpose, because they make good questions for the agent later. Generate the SQL with a short script instead of typing it by hand, and remember to escape single quotes. The CSV is the source of the data; keep it in the repository root.

### Backend

Micronaut 5.2 on Java 25, built with Maven, package `com.example.conference`.

Create it with Micronaut Launch, then unzip it as `backend/`:

```bash
curl -sSL -o backend.zip "https://launch.micronaut.io/create/default/com.example.conference.backend?lang=JAVA&build=MAVEN&test=JUNIT&javaVersion=JDK_25&features=data-jdbc,postgres,flyway,serialization-jsonp,validation"
```

After generating:
- The database comes from docker-compose, not from Micronaut Test Resources. In `pom.xml`, set `micronaut.test.resources.enabled` to `false` and remove the `micronaut-test-resources-client` dependency. Delete the generated `BackendTest`.
- `application.properties`:
  ```properties
  micronaut.application.name=conference-backend
  micronaut.server.port=8080
  datasources.default.url=${DB_URL:`jdbc:postgresql://localhost:5432/conference`}
  datasources.default.username=${DB_USER:conference}
  datasources.default.password=${DB_PASSWORD:conference}
  datasources.default.db-type=postgres
  datasources.default.dialect=POSTGRES
  datasources.default.driver-class-name=org.postgresql.Driver
  flyway.datasources.default.enabled=true
  ```

Flyway migrations live in `src/main/resources/db/migration`:
- `V1__schema.sql`:
  - `room (id, name, floor, capacity)`
  - `speaker (id, name, job_title, company, country, bio)`
  - `talk (id, title, description, track, level, format, starts_at, ends_at, room_id → room)`
  - `talk_speaker (talk_id, speaker_id)` with a composite primary key
  - Use `BIGSERIAL` ids, `TEXT` columns, `TIMESTAMP` for the times, and `NOT NULL` everywhere.
- `V2__seed_bytecode_bay_2026.sql`: the data from the CSV, inserted with explicit ids. End with `setval` on each id sequence.

Code: one package per feature (`room`, `speaker`, `talk`), each with a record entity, a repository and a controller.
- Entities are `@Serdeable @MappedEntity` records:
  - `Room(id, name, floor, capacity)`
  - `Speaker(id, name, jobTitle, company, country, bio)`
  - `Talk(id, title, description, track, level, format, LocalDateTime startsAt, LocalDateTime endsAt, Room room, List<Speaker> speakers)`. `room` is `@Relation(MANY_TO_ONE)`. `speakers` is `@Relation(MANY_TO_MANY)` with `@JoinTable(name = "talk_speaker", joinColumns = @JoinColumn(name = "talk_id"), inverseJoinColumns = @JoinColumn(name = "speaker_id"))`.
- Repositories use `@JdbcRepository(dialect = Dialect.POSTGRES)`:
  - `RoomRepository`: `findAllOrderById()`.
  - `SpeakerRepository`: `findAllOrderByName()`.
  - `TalkRepository` extends `GenericRepository<Talk, Long>`, with `findAllOrderByStartsAt()` and `findById(Long id)`. Put `@Join(value = "room", type = LEFT_FETCH)` and `@Join(value = "speakers", type = LEFT_FETCH)` on both.
- Read-only JSON API:

  | Endpoint                 | Returns                                             |
  |--------------------------|-----------------------------------------------------|
  | `GET /api/talks`         | all talks, ordered by start time, with room and speakers |
  | `GET /api/talks/{id}`    | one talk, or 404                                    |
  | `GET /api/speakers`      | all speakers, ordered by name                       |
  | `GET /api/speakers/{id}` | one speaker, or 404                                 |
  | `GET /api/rooms`         | all rooms                                           |

  A missing item is returned as `Optional`, so Micronaut answers 404.

Backend gotchas:
- `@JoinTable` and `@JoinColumn` are in `io.micronaut.data.annotation.sql`, not `io.micronaut.data.annotation`.
- Don't filter on a fetch-joined collection, for example `findBySpeakersId`: it drops co-speakers from the result. Filter in Java, or in the frontend.
- Serialisation is Micronaut Serde (`@Serdeable`), not Jackson.

### Frontend

React 19 + TypeScript + Vite, with plain CSS. No UI library, router or state library. Create it with:

```bash
npm create vite@latest frontend -- --template react-ts --no-interactive
```

- `vite.config.ts`: proxy `/api` to `http://localhost:8080`.
- `tsconfig.app.json`: target and lib `ES2024`, so that `Map.groupBy` is available.
- Page title: "Bytecode Bay 2026".

Structure:
- `src/api.ts`: TypeScript types mirroring the backend JSON (`Room`, `Speaker`, `Talk`, `ToolCall`, `ChatResponse`), plus `fetchTalks`, `fetchSpeakers` and `sendChatMessage`.
- `src/format.ts`: date helpers, `initials(name)`, and a map from track to CSS class. The API returns local times without a zone (`2026-11-18T10:00:00`), so take the day and time by slicing the string. Don't convert with `Date`, which would shift the times to the viewer's zone.
- `src/App.tsx`: loads all talks and speakers once, switches between the two views, and opens the detail modal.
- `src/components/`: one component per file: `Schedule`, `TalkCard`, `Speakers`, `TalkDetails`, `SpeakerDetails`, `Modal`, `ChatPanel`.
- `src/index.css`: all styles.

What it shows:
- **Header:** a dark hero band with "18–19 November 2026 · Port Ellis", the title "Bytecode Bay 2026", and a tagline: "Two days of Java, AI and everything in between. 37 sessions, 24 speakers." (the counts come from the data). Below them are two tabs, **Schedule** and **Speakers**.
- **Schedule view:**
  - Day tabs ("Day 1 · Wednesday 18 November"), a search box (matching talk titles and speaker names) and track filter chips ("All tracks" plus one chip per track).
  - Talks are grouped into time slots: the time on the left, and the talks starting then shown as a responsive grid of cards. Workshops run in parallel with the talks.
  - Cards have a fixed size (190px tall, at least 240px wide), with the title clamped to 3 lines and the speakers to 1, and the page keeps a stable scrollbar gutter, so the grid doesn't jump when switching days.
  - Show "No talks match your filters." when the filters leave nothing.
- **Talk card:**
  - The track label in the track's colour, with a badge when the format isn't a regular Talk (Keynote, Workshop).
  - The title, the speakers joined with " & ", then a footer with "10:00–10:50 · Harbor Hall" and the level.
  - Each track has its own accent colour, defined as CSS variables (`--track-java`, `--track-ai`, …). The tracks are Java & JVM, AI & Data, Cloud & Ops, Frontend & UX, Architecture and Security.
- **Speakers view:** a grid of speaker cards, each with a round avatar showing their initials, plus their name, job title and company.
- **Modal** (closes on Escape, on a backdrop click and with a close button):
  - Talk details: track, format and level; the title; day, time, room and floor; the description; and the speakers as clickable rows that open the speaker.
  - Speaker details: a large avatar, the name, job title, company and country; the bio; and their sessions as clickable rows that open the talk. Filter the full talk list on the client to find them.
- States: "Loading programme…" while loading. If loading fails, show "Could not load the programme: … Is the backend running?".
- **Chat:** a floating "💬 Ask the assistant" button in the bottom-right corner opens a 400×600px chat window that overlays the page (full screen at phone width) without moving the layout. Closing it keeps the conversation.
  - The browser generates the conversation id. The window shows the message history, a hint when empty, a "thinking…" state while waiting, and errors.
  - It sends `POST /api/chat` with `{ conversationId, message }` and expects `{ reply, toolCalls? }`, where each tool call is `{ name, arguments }` and `arguments` is the JSON string from LangChain4j. Tool calls are shown as small, expandable lines above the answer.
- Style: a light, warm paper background, white cards with subtle borders and rounded corners, a teal accent, and a system font stack. It must work at phone width.

### Running locally

```bash
docker compose up -d                       # database
cd backend && ./mvnw mn:run                # API on :8080
cd frontend && npm install && npm run dev  # UI on :5173
```

Reset the data with `docker compose down -v`. Never edit a migration that has already been applied: add a new one, or reset the database.

## Shared technical choices for the AI stages

- **LangChain4j through the Micronaut integration**: `micronaut-langchain4j-anthropic`, with the version managed by the Micronaut parent POM, and a declarative `@AiService` interface.
- **Model:** Anthropic `claude-haiku-4-5`, temperature `0` (so answers repeat between rehearsal and the live run). The API key comes from the `ANTHROPIC_API_KEY` environment variable and is never committed.
- **API:** `POST /api/chat` with `{ conversationId, message }` returns the reply as JSON. No streaming: Haiku is fast enough, and it keeps the code simple.
- **UI:** already built (see **Chat** above). The AI stages are backend-only: don't change the frontend. The backend must match the JSON contract the chat window expects.

## Stage 1: a chat agent with no tools

The goal is for the audience to see a working, friendly agent that doesn't actually know anything about Bytecode Bay. Asked about the schedule, it either makes up an answer or admits it doesn't know.

- Backend: an `@AiService` with a short system prompt saying it is the assistant for the Bytecode Bay 2026 conference. Add per-conversation chat memory (a message window), so follow-up questions work. **No tools, no access to the database or the repositories, and no conference data in the prompt.**
- Endpoint: `POST /api/chat` returning `{ "reply": "…" }`. Leave out `toolCalls`.

## Stage 2: give the agent tools

The goal is the same questions, now answered correctly from our database, with the audience able to see which tools the agent called.

- Tools are Java methods (`@Tool`) that call the existing repositories. They should cover finding talks (by day, time, track, room or text), getting a talk, finding speakers and getting a speaker. Return compact, structured results.
- All tools are **read-only**. Don't add any tool, endpoint or table that changes data.
- Return the tool calls with the reply as `toolCalls: [{ name, arguments }]`. The chat window already shows them.
- Log every tool call in the backend application log at `INFO`: the tool name, its arguments and a short summary of the result (for example how many talks were found).
- System prompt: tell the agent to answer conference questions using the tools.

## Stage 3: guardrails

Protect the agent from misuse with LangChain4j guardrails:

- It must not reveal its system prompt, instructions or tools.
- It must only help with the Bytecode Bay programme, and refuse off-topic requests.

Keep each guardrail in its own small class, so each one can be explained in turn.
