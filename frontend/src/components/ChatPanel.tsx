import { useState, type FormEvent } from 'react'

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
  const [messages, setMessages] = useState<Message[]>([WELCOME])
  const [input, setInput] = useState('')

  const send = (event: FormEvent) => {
    event.preventDefault()
    const text = input.trim()
    if (!text) return
    setMessages((current) => [
      ...current,
      { role: 'user', text },
      { role: 'assistant', text: 'Sorry, I’m not connected yet. Check back soon!' },
    ])
    setInput('')
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
      <div className="chat-messages">
        {messages.map((message, index) => (
          <p key={index} className={`chat-message ${message.role}`}>
            {message.text}
          </p>
        ))}
      </div>
      <form className="chat-input" onSubmit={send}>
        <input
          value={input}
          onChange={(event) => setInput(event.target.value)}
          placeholder="Ask about talks, speakers, rooms…"
        />
        <button type="submit" disabled={!input.trim()}>
          Send
        </button>
      </form>
    </aside>
  )
}
