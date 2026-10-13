import type { Talk } from '../api'
import { timeOf, trackClass } from '../format'

interface TalkCardProps {
  talk: Talk
  onSelect: (talk: Talk) => void
}

export function TalkCard({ talk, onSelect }: TalkCardProps) {
  return (
    <button className={`talk-card ${trackClass(talk.track)}`} onClick={() => onSelect(talk)}>
      <div className="talk-card-meta">
        <span className="track-label">{talk.track}</span>
        {talk.format !== 'Talk' && <span className="badge">{talk.format}</span>}
      </div>
      <h3>{talk.title}</h3>
      <p className="talk-card-speakers">{talk.speakers.map((speaker) => speaker.name).join(' & ')}</p>
      <p className="talk-card-footer">
        <span>
          {timeOf(talk.startsAt)}–{timeOf(talk.endsAt)} · {talk.room.name}
        </span>
        <span>{talk.level}</span>
      </p>
    </button>
  )
}
