import type { Speaker } from '../api'
import { initials } from '../format'

interface SpeakersProps {
  speakers: Speaker[]
  onSelectSpeaker: (speaker: Speaker) => void
}

export function Speakers({ speakers, onSelectSpeaker }: SpeakersProps) {
  return (
    <section className="speaker-grid">
      {speakers.map((speaker) => (
        <button key={speaker.id} className="speaker-card" onClick={() => onSelectSpeaker(speaker)}>
          <span className="avatar">{initials(speaker.name)}</span>
          <span>
            <strong>{speaker.name}</strong>
            <span className="muted">
              {speaker.jobTitle}, {speaker.company}
            </span>
          </span>
        </button>
      ))}
    </section>
  )
}
