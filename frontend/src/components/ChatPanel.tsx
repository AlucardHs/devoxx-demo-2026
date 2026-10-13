import { useEffect, useRef, useState, type FormEvent } from 'react'
import { sendChatMessage } from '../api'

interface Message {
  role: 'user' | 'assistant'
  text: string
}

interface ChatPanelProps {
  onClose: () => void
}

export function ChatPanel({ onClose }: ChatPanelProps) {
  // One conversation per page load: the backend keeps the history for this id.
  const [conversationId] = useState(() => crypto.randomUUID())
  const [messages, setMessages] = useState<Message[]>([])
  const [draft, setDraft] = useState('')
  const [thinking, setThinking] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const endRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, thinking, error])

  const send = async (event: FormEvent) => {
    event.preventDefault()
    const text = draft.trim()
    if (!text || thinking) return

    setMessages((current) => [...current, { role: 'user', text }])
    setDraft('')
    setError(null)
    setThinking(true)
    try {
      const { reply } = await sendChatMessage(conversationId, text)
      setMessages((current) => [...current, { role: 'assistant', text: reply }])
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setThinking(false)
    }
  }

  return (
    <aside className="chat-panel" aria-label="Conference assistant">
      <header className="chat-header">
        <div>
          <h2>Ask Bytecode Bay</h2>
          <p className="muted">Your conference assistant</p>
        </div>
        <button className="chat-close" onClick={onClose} aria-label="Close assistant">
          ×
        </button>
      </header>

      <div className="chat-messages">
        {messages.length === 0 && (
          <p className="chat-hint muted">Ask anything about the conference: talks, speakers, rooms, what to see next…</p>
        )}
        {messages.map((message, index) => (
          <div key={index} className={`chat-message ${message.role}`}>
            {message.text}
          </div>
        ))}
        {thinking && <div className="chat-message assistant thinking">Thinking…</div>}
        {error && <p className="chat-error">{error}</p>}
        <div ref={endRef} />
      </div>

      <form className="chat-input" onSubmit={send}>
        <input
          value={draft}
          onChange={(event) => setDraft(event.target.value)}
          placeholder="Ask a question…"
          aria-label="Your question"
          autoFocus
        />
        <button type="submit" disabled={thinking || !draft.trim()}>
          Send
        </button>
      </form>
    </aside>
  )
}
