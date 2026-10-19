import { useEffect, useRef, useState, type FormEvent } from 'react'
import { sendChatMessage, type ToolCall } from '../api'

interface Message {
  role: 'user' | 'assistant' | 'error'
  text: string
  toolCalls?: ToolCall[]
}

interface ChatPanelProps {
  open: boolean
  onClose: () => void
}

export function ChatPanel({ open, onClose }: ChatPanelProps) {
  const [conversationId] = useState(() => crypto.randomUUID())
  const [messages, setMessages] = useState<Message[]>([])
  const [input, setInput] = useState('')
  const [thinking, setThinking] = useState(false)
  const bottomRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, thinking])

  const send = async (event: FormEvent) => {
    event.preventDefault()
    const text = input.trim()
    if (!text || thinking) return

    setMessages((current) => [...current, { role: 'user', text }])
    setInput('')
    setThinking(true)
    try {
      const response = await sendChatMessage(conversationId, text)
      setMessages((current) => [
        ...current,
        { role: 'assistant', text: response.reply, toolCalls: response.toolCalls },
      ])
    } catch (e) {
      setMessages((current) => [...current, { role: 'error', text: `Something went wrong: ${(e as Error).message}` }])
    } finally {
      setThinking(false)
    }
  }

  return (
    <aside className="chat-panel" hidden={!open} aria-label="Conference assistant">
      <header className="chat-header">
        <h2>Ask Bytecode Bay</h2>
        <button className="chat-close" onClick={onClose} aria-label="Close assistant">
          ×
        </button>
      </header>

      <div className="chat-messages">
        {messages.length === 0 && (
          <p className="chat-hint">Ask anything about the programme, e.g. “Which talks about AI are on day 2?”</p>
        )}
        {messages.map((message, index) => (
          <div key={index} className={`chat-message ${message.role}`}>
            {message.toolCalls?.map((call, callIndex) => (
              <details key={callIndex} className="tool-call">
                <summary>🔧 {call.name}</summary>
                <pre>{call.arguments}</pre>
              </details>
            ))}
            <p>{message.text}</p>
          </div>
        ))}
        {thinking && <div className="chat-message assistant thinking">thinking…</div>}
        <div ref={bottomRef} />
      </div>

      <form className="chat-input" onSubmit={send}>
        <input
          value={input}
          onChange={(event) => setInput(event.target.value)}
          placeholder="Ask a question…"
          aria-label="Your question"
        />
        <button type="submit" disabled={thinking || !input.trim()}>
          Send
        </button>
      </form>
    </aside>
  )
}
