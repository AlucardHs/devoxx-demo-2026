import { useState } from 'react'
import type { Talk } from '../api'
import { dayOf, formatDay, timeOf } from '../format'
import { TalkCard } from './TalkCard'

interface ScheduleProps {
  talks: Talk[]
  onSelectTalk: (talk: Talk) => void
}

export function Schedule({ talks, onSelectTalk }: ScheduleProps) {
  const days = [...new Set(talks.map((talk) => dayOf(talk.startsAt)))]
  const tracks = [...new Set(talks.map((talk) => talk.track))].sort()

  const [selectedDay, setSelectedDay] = useState(days[0])
  const [selectedTrack, setSelectedTrack] = useState<string | null>(null)
  const [search, setSearch] = useState('')

  const query = search.trim().toLowerCase()
  const visibleTalks = talks
    .filter(
      (talk) =>
        dayOf(talk.startsAt) === selectedDay &&
        (selectedTrack === null || talk.track === selectedTrack) &&
        (query === '' ||
          talk.title.toLowerCase().includes(query) ||
          talk.speakers.some((speaker) => speaker.name.toLowerCase().includes(query))),
    )
    .sort((a, b) => a.startsAt.localeCompare(b.startsAt) || a.room.id - b.room.id)

  const slots = Map.groupBy(visibleTalks, (talk) => timeOf(talk.startsAt))

  return (
    <section>
      <div className="toolbar">
        <div className="segmented">
          {days.map((day, index) => (
            <button key={day} className={day === selectedDay ? 'active' : ''} onClick={() => setSelectedDay(day)}>
              Day {index + 1} <span className="muted">· {formatDay(day)}</span>
            </button>
          ))}
        </div>
        <input
          className="search"
          type="search"
          placeholder="Search talks or speakers…"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
        />
      </div>

      <div className="chips">
        <button className={selectedTrack === null ? 'chip active' : 'chip'} onClick={() => setSelectedTrack(null)}>
          All tracks
        </button>
        {tracks.map((track) => (
          <button
            key={track}
            className={selectedTrack === track ? 'chip active' : 'chip'}
            onClick={() => setSelectedTrack(selectedTrack === track ? null : track)}
          >
            {track}
          </button>
        ))}
      </div>

      {slots.size === 0 && <p className="empty">No talks match your filters.</p>}

      {[...slots].map(([time, slotTalks]) => (
        <div key={time} className="slot">
          <div className="slot-time">{time}</div>
          <div className="slot-talks">
            {slotTalks.map((talk) => (
              <TalkCard key={talk.id} talk={talk} onSelect={onSelectTalk} />
            ))}
          </div>
        </div>
      ))}
    </section>
  )
}
