import { useEffect, useRef, useState, type FormEvent } from 'react'
import { sendChatMessage } from '../api'

interface Message {
  role: 'user' | 'assistant'
  text: string
}

interface ChatPanelProps {
  open: boolean
  onToggle: () => void
}

const WELCOME: Message = {
  role: 'assistant',
  text: 'Hi! I’m the Bytecode Bay 2026 assistant. Ask me anything about the conference.',
}

export function ChatPanel({ open, onToggle }: ChatPanelProps) {
  const [conversationId] = useState(() => crypto.randomUUID())
  const [messages, setMessages] = useState<Message[]>([WELCOME])
  const [input, setInput] = useState('')
  const [thinking, setThinking] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const list = useRef<HTMLDivElement>(null)

  useEffect(() => {
    list.current?.scrollTo({ top: list.current.scrollHeight, behavior: 'smooth' })
  }, [messages, thinking, open])

  const send = async (event: FormEvent) => {
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

  if (!open) {
    return (
      <button className="chat-toggle" onClick={onToggle}>
        💬 Ask the assistant
      </button>
    )
  }

  return (
    <aside className="chat-panel">
      <header className="chat-header">
        <h3>Conference assistant</h3>
        <button className="modal-close" onClick={onToggle} aria-label="Close chat">
          ×
        </button>
      </header>
      <div className="chat-messages" ref={list}>
        {messages.map((message, index) => (
          <p key={index} className={`chat-message ${message.role}`}>
            {message.text}
          </p>
        ))}
        {thinking && <p className="chat-message assistant">thinking…</p>}
        {error && <p className="chat-message assistant">Something went wrong: {error}</p>}
      </div>
      <form className="chat-input" onSubmit={send}>
        <input
          value={input}
          onChange={(event) => setInput(event.target.value)}
          placeholder="Ask about talks, speakers, rooms…"
          autoFocus
        />
        <button type="submit" disabled={thinking || !input.trim()}>
          Send
        </button>
      </form>
    </aside>
  )
}
