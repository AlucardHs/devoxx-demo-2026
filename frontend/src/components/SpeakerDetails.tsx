import type { Speaker, Talk } from '../api'
import { dayOf, formatDay, initials, timeOf } from '../format'

interface SpeakerDetailsProps {
  speaker: Speaker
  talks: Talk[]
  onSelectTalk: (talk: Talk) => void
}

export function SpeakerDetails({ speaker, talks, onSelectTalk }: SpeakerDetailsProps) {
  const speakerTalks = talks.filter((talk) => talk.speakers.some((s) => s.id === speaker.id))

  return (
    <article>
      <div className="speaker-heading">
        <span className="avatar large">{initials(speaker.name)}</span>
        <div>
          <h2>{speaker.name}</h2>
          <p className="muted">
            {speaker.jobTitle}, {speaker.company} · {speaker.country}
          </p>
        </div>
      </div>
      <p>{speaker.bio}</p>

      <h4>Sessions</h4>
      {speakerTalks.map((talk) => (
        <button key={talk.id} className="session-row" onClick={() => onSelectTalk(talk)}>
          <strong>{talk.title}</strong>
          <span className="muted">
            {formatDay(dayOf(talk.startsAt))}, {timeOf(talk.startsAt)} · {talk.room.name}
          </span>
        </button>
      ))}
    </article>
  )
}
