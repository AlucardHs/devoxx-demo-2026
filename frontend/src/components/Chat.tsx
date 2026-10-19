import { useEffect, useRef, useState, type FormEvent } from 'react'
import { sendChatMessage } from '../api'

type Message = { role: 'user' | 'assistant'; text: string }

interface Props {
  onClose: () => void
}

export function Chat({ onClose }: Props) {
  const [conversationId] = useState(() => crypto.randomUUID())
  const [messages, setMessages] = useState<Message[]>([])
  const [input, setInput] = useState('')
  const [thinking, setThinking] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const bottom = useRef<HTMLDivElement>(null)

  useEffect(() => {
    bottom.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, thinking])

  async function send(event: FormEvent) {
    event.preventDefault()
    const text = input.trim()
    if (!text || thinking) return

    setMessages((current) => [...current, { role: 'user', text }])
    setInput('')
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
    <aside className="chat">
      <header className="chat-header">
        <h3>Conference assistant</h3>
        <button className="chat-close" onClick={onClose} aria-label="Close assistant">
          ×
        </button>
      </header>

      <div className="chat-messages">
        {messages.length === 0 && (
          <p className="chat-hint">Ask me anything about Bytecode Bay 2026.</p>
        )}
        {messages.map((message, index) => (
          <div key={index} className={`chat-message ${message.role}`}>
            {message.text}
          </div>
        ))}
        {thinking && <div className="chat-message assistant thinking">thinking…</div>}
        {error && <p className="chat-error">Something went wrong: {error}</p>}
        <div ref={bottom} />
      </div>

      <form className="chat-input" onSubmit={send}>
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Ask a question…"
          autoFocus
        />
        <button type="submit" disabled={thinking || !input.trim()}>
          Send
        </button>
      </form>
    </aside>
  )
}
