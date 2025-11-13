import React, { useMemo } from 'react'

type SectionMeeting = {
  id: number
  sectionId: number
  dayOfWeek: number
  startTime: string
  durationMinutes: number
}

type EnrolledSection = {
  sectionId: number
  courseCode: string
  courseName: string
  sectionNumber: number
  teacherName: string
  roomName: string
  credits: number
  meetings: SectionMeeting[]
  capacity: number
  enrolledStudents: number
  availableSpots: number
}

const DAYS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri']
const SLOTS = ['09:00', '10:00', '11:00', '13:00', '14:00', '15:00', '16:00']

export function StudentScheduleGrid({ sections }: { sections: EnrolledSection[] }) {
  const grid = useMemo(() => {
    const map: Record<string, EnrolledSection[]> = {}
    for (const s of sections) {
      for (const m of s.meetings) {
        const key = `${m.dayOfWeek}-${m.startTime}`
        if (!map[key]) map[key] = []
        map[key].push(s)
      }
    }
    return map
  }, [sections])

  return (
    <div>
      <h3>Weekly Schedule</h3>
      <div style={{
        display: 'grid',
        gridTemplateColumns: `120px repeat(${DAYS.length}, 1fr)`,
        border: '1px solid #ddd',
        borderRadius: 8,
        overflow: 'hidden'
      }}>
        {/* Header */}
        <div style={{
          background: '#f8f9fa',
          padding: 12,
          borderBottom: '1px solid #ddd',
          borderRight: '1px solid #ddd',
          fontWeight: 600
        }}>
          Time
        </div>
        {DAYS.map(d => (
          <div key={d} style={{
            background: '#f8f9fa',
            padding: 12,
            borderBottom: '1px solid #ddd',
            borderRight: '1px solid #ddd',
            textAlign: 'center',
            fontWeight: 600
          }}>
            {d}
          </div>
        ))}

        {/* Time slots */}
        {SLOTS.map(slot => (
          <React.Fragment key={slot}>
            <div style={{
              padding: 12,
              borderRight: '1px solid #eee',
              borderBottom: '1px solid #eee',
              background: '#fafafa',
              fontWeight: 500,
              fontSize: 14
            }}>
              {slot}
            </div>
            {DAYS.map((d, i) => {
              const key = `${i+1}-${slot}`
              const list = grid[key] || []
              return (
                <div key={key} style={{
                  padding: 8,
                  borderRight: '1px solid #eee',
                  borderBottom: '1px solid #eee',
                  minHeight: 80,
                  background: list.length > 0 ? '#fff' : '#f9f9f9'
                }}>
                  {list.map(s => (
                    <div key={s.sectionId} style={{
                      border: '2px solid #007bff',
                      padding: 8,
                      marginBottom: 4,
                      borderRadius: 6,
                      background: '#e3f2fd',
                      fontSize: 12
                    }}>
                      <div style={{ fontWeight: 600, color: '#1565c0' }}>
                        {s.courseCode}-{s.sectionNumber}
                      </div>
                      <div style={{ fontSize: 11, color: '#424242', marginBottom: 2 }}>
                        {s.courseName}
                      </div>
                      <div style={{ fontSize: 11, color: '#666' }}>
                        {s.teacherName}
                      </div>
                      <div style={{ fontSize: 11, color: '#666' }}>
                        {s.roomName}
                      </div>
                      <div style={{ fontSize: 10, color: '#888', marginTop: 2 }}>
                        {s.credits} credits
                      </div>
                    </div>
                  ))}
                </div>
              )
            })}
          </React.Fragment>
        ))}
      </div>
    </div>
  )
}