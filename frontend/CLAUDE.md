# Frontend

React 19 + TypeScript + Vite. Plain CSS, with no UI library, router or state library.

## Commands

```bash
npm run dev     # http://localhost:5173, proxies /api to the backend on :8080 (see vite.config.ts)
npm run build   # type-checks with tsc, then builds
npm run lint    # oxlint
```

## Structure

- `src/api.ts`: TypeScript types that mirror the backend JSON, plus fetch helpers
- `src/format.ts`: date/time helpers and the track → CSS class map
- `src/App.tsx`: loads all talks and speakers once; switches between the Schedule and Speakers views; opens the detail modal
- `src/components/`: one component per file
- `src/index.css`: all styles. Track colours are CSS variables (`--track-*`).

## Notes

- All filtering (day, track, search, a speaker's talks) happens on the client, using the full talk list.
- The API returns local times without a time zone (`2026-11-18T10:00:00`). Use the helpers in `format.ts` instead of converting with `Date`.
- When a new track is added in the seed data, add a colour for it in `format.ts` and `index.css`.
- `tsconfig.app.json` targets ES2024 so that `Map.groupBy` is available.
