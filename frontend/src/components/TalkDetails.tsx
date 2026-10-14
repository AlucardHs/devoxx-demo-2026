import type { Speaker, Talk } from '../api'
import { dayOf, formatDay, initials, timeOf, trackClass } from '../format'

interface TalkDetailsProps {
  talk: Talk
  onSelectSpeaker: (speaker: Speaker) => void
}

export function TalkDetails({ talk, onSelectSpeaker }: TalkDetailsProps) {
  return (
    <article className={trackClass(talk.track)}>
      <p className="track-label">
        {talk.track} · {talk.format} · {talk.level}
      </p>
      <h2>{talk.title}</h2>
      <p className="muted">
        {formatDay(dayOf(talk.startsAt))}, {timeOf(talk.startsAt)}–{timeOf(talk.endsAt)} · {talk.room.name} (floor{' '}
        {talk.room.floor})
      </p>
      <p>{talk.description}</p>

      <h4>{talk.speakers.length > 1 ? 'Speakers' : 'Speaker'}</h4>
      {talk.speakers.map((speaker) => (
        <button key={speaker.id} className="speaker-row" onClick={() => onSelectSpeaker(speaker)}>
          <span className="avatar">{initials(speaker.name)}</span>
          <span>
            <strong>{speaker.name}</strong>
            <span className="muted">
              {speaker.jobTitle}, {speaker.company}
            </span>
          </span>
        </button>
      ))}
    </article>
  )
}
