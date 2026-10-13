// The API returns local conference times without a zone, e.g. "2026-11-18T10:00:00".
export const dayOf = (dateTime: string) => dateTime.slice(0, 10)
export const timeOf = (dateTime: string) => dateTime.slice(11, 16)

export function formatDay(day: string) {
  return new Date(`${day}T12:00:00`).toLocaleDateString('en-GB', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
  })
}

export function initials(name: string) {
  return name
    .split(' ')
    .map((part) => part[0])
    .slice(0, 2)
    .join('')
}

const trackClasses: Record<string, string> = {
  'Java & JVM': 'track-java',
  'AI & Data': 'track-ai',
  'Cloud & Ops': 'track-cloud',
  'Frontend & UX': 'track-frontend',
  Architecture: 'track-architecture',
  Security: 'track-security',
}

export const trackClass = (track: string) => trackClasses[track] ?? 'track-other'
