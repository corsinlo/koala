import React, { useMemo } from 'react'

type Meeting = { dayOfWeek:number; startTime:string; durationMinutes:number }
type Section = {
  sectionId:number;
  courseCode:string;
  courseName:string;
  sectionNumber:number;
  teacherName:string;
  roomName:string;
  capacity:number;
  enrolledStudents:number;
  availableSpots:number;
  meetings:Meeting[]
}

const DAYS = ['Mon','Tue','Wed','Thu','Fri']
const SLOTS = ['09:00','10:00','11:00','13:00','14:00','15:00','16:00']

export function ScheduleGrid({ sections }: { sections: Section[] }) {
  const grid = useMemo(() => {
    const map: Record<string, Section[]> = {}
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
      <h2>Master Schedule</h2>
      <div style={{display:'grid', gridTemplateColumns:`120px repeat(${DAYS.length}, 1fr)`, border:'1px solid #ddd'}}>
        <div style={{background:'#fafafa', padding:8, borderBottom:'1px solid #ddd', borderRight:'1px solid #ddd'}}></div>
        {DAYS.map(d => <div key={d} style={{background:'#fafafa', padding:8, borderBottom:'1px solid #ddd', borderRight:'1px solid #ddd', textAlign:'center', fontWeight:600}}>{d}</div>)}
        {SLOTS.map(slot => (
          <React.Fragment key={slot}>
            <div style={{padding:8, borderRight:'1px solid #eee', borderBottom:'1px solid #eee'}}>{slot}</div>
            {DAYS.map((d, i) => {
              const key = `${i+1}-${slot}`
              const list = grid[key] || []
              return (
                <div key={key} style={{padding:8, borderRight:'1px solid #eee', borderBottom:'1px solid #eee', minHeight:64}}>
                  {list.map(s => (
                    <div key={s.sectionId} style={{border:'1px solid #ccc', padding:6, marginBottom:6, borderRadius:4}}>
                      <div style={{fontWeight:600}}>{s.courseCode}-{s.sectionNumber}</div>
                      <div style={{fontSize:11, color:'#666'}}>{s.courseName}</div>
                      <div style={{fontSize:12}}>{s.teacherName}</div>
                      <div style={{fontSize:12, color:'#666'}}>{s.roomName}</div>
                      <div style={{fontSize:11, color:'#888'}}>{s.enrolledStudents}/{s.capacity} students</div>
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
