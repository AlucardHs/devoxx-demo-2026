import { useEffect, useState } from 'react'
import { fetchSpeakers, fetchTalks, type Speaker, type Talk } from './api'
import { ChatPanel } from './components/ChatPanel'
import { Modal } from './components/Modal'
import { Schedule } from './components/Schedule'
import { SpeakerDetails } from './components/SpeakerDetails'
import { Speakers } from './components/Speakers'
import { TalkDetails } from './components/TalkDetails'

type View = 'schedule' | 'speakers'
type Selection = { kind: 'talk'; talk: Talk } | { kind: 'speaker'; speaker: Speaker } | null

function App() {
  const [talks, setTalks] = useState<Talk[]>([])
  const [speakers, setSpeakers] = useState<Speaker[]>([])
  const [error, setError] = useState<string | null>(null)
  const [view, setView] = useState<View>('schedule')
  const [selection, setSelection] = useState<Selection>(null)
  const [chatOpen, setChatOpen] = useState(false)

  useEffect(() => {
    Promise.all([fetchTalks(), fetchSpeakers()])
      .then(([talks, speakers]) => {
        setTalks(talks)
        setSpeakers(speakers)
      })
      .catch((e: Error) => setError(e.message))
  }, [])

  const selectTalk = (talk: Talk) => setSelection({ kind: 'talk', talk })
  const selectSpeaker = (speaker: Speaker) => setSelection({ kind: 'speaker', speaker })

  return (
    <>
      <header className="hero">
        <div className="container">
          <p className="eyebrow">18–19 November 2026 · Port Ellis</p>
          <h1>Bytecode Bay 2026</h1>
          <p className="tagline">
            Two days of Java, AI and everything in between. {talks.length} sessions, {speakers.length} speakers.
          </p>
          <nav className="tabs">
            <button className={view === 'schedule' ? 'active' : ''} onClick={() => setView('schedule')}>
              Schedule
            </button>
            <button className={view === 'speakers' ? 'active' : ''} onClick={() => setView('speakers')}>
              Speakers
            </button>
          </nav>
        </div>
      </header>

      <main className="container">
        {error && <p className="error">Could not load the programme: {error}. Is the backend running?</p>}
        {!error && talks.length === 0 && <p className="empty">Loading programme…</p>}
        {talks.length > 0 &&
          (view === 'schedule' ? (
            <Schedule talks={talks} onSelectTalk={selectTalk} />
          ) : (
            <Speakers speakers={speakers} onSelectSpeaker={selectSpeaker} />
          ))}
      </main>

      {!chatOpen && (
        <button className="chat-toggle" onClick={() => setChatOpen(true)}>
          💬 Ask the assistant
        </button>
      )}
      <ChatPanel open={chatOpen} onClose={() => setChatOpen(false)} />

      {selection && (
        <Modal onClose={() => setSelection(null)}>
          {selection.kind === 'talk' ? (
            <TalkDetails talk={selection.talk} onSelectSpeaker={selectSpeaker} />
          ) : (
            <SpeakerDetails speaker={selection.speaker} talks={talks} onSelectTalk={selectTalk} />
          )}
        </Modal>
      )}
    </>
  )
}

export default App
