export interface Room {
  id: number
  name: string
  floor: number
  capacity: number
}

export interface Speaker {
  id: number
  name: string
  jobTitle: string
  company: string
  country: string
  bio: string
}

export interface Talk {
  id: number
  title: string
  description: string
  track: string
  level: string
  format: string
  startsAt: string
  endsAt: string
  room: Room
  speakers: Speaker[]
}

async function get<T>(path: string): Promise<T> {
  const response = await fetch(path)
  if (!response.ok) {
    throw new Error(`GET ${path} failed with ${response.status}`)
  }
  return response.json() as Promise<T>
}

export const fetchTalks = () => get<Talk[]>('/api/talks')
export const fetchSpeakers = () => get<Speaker[]>('/api/speakers')

export interface ChatReply {
  reply: string
}

export async function sendChatMessage(conversationId: string, message: string): Promise<ChatReply> {
  const response = await fetch('/api/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ conversationId, message }),
  })
  if (!response.ok) {
    throw new Error(`The assistant is unavailable (HTTP ${response.status})`)
  }
  return response.json() as Promise<ChatReply>
}
