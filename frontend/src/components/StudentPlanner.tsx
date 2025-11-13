import React, { useEffect, useState } from 'react'
import { api } from '../services/api'

type Meeting = { dayOfWeek:number; startTime:string; durationMinutes:number }
type Section = { id:number; courseCode:string; courseName:string; sectionNumber:number; teacherName:string; roomName:string; capacity:number; enrolled:number; meetings:Meeting[] }

export function StudentPlanner({ semesterId }: { semesterId: number }) {
  const [studentId, setStudentId] = useState<number>(100) // demo id
  const [progress, setProgress] = useState<any | null>(null)
  const [sections, setSections] = useState<Section[]>([])
  const [msg, setMsg] = useState<string>('')

  useEffect(() => {
    api.get(`/students/${studentId}/progress`).then(setProgress)
  }, [studentId])

  useEffect(() => {
    if (!semesterId) return
    api.get(`/schedule?semesterId=${semesterId}`).then(setSections)
  }, [semesterId])

  const enroll = async (sectionId: number) => {
    const res = await api.post(`/students/${studentId}/enroll?sectionId=${sectionId}`)
    if (res && (res.message || typeof res === 'string')) setMsg(res.message || String(res))
    const p = await api.get(`/students/${studentId}/progress`)
    setProgress(p)
  }

  return (
    <div>
      <h2>Student Planner</h2>
      <div style={{display:'flex', alignItems:'center', gap:12}}>
        <label>Student ID:&nbsp;<input type="number" value={studentId} onChange={e => setStudentId(parseInt(e.target.value || '100'))} style={{width:100}}/></label>
        {progress && <div style={{fontSize:14}}>
          <strong>{progress.firstName} {progress.lastName}</strong> · Grade {progress.gradeLevel} · GPA {progress.gpa?.toFixed(2)} · Credits {progress.creditsEarned}
        </div>}
      </div>
      {msg && <div style={{marginTop:8, padding:8, border:'1px solid #ddd', background:'#f9f9f9'}}>{msg}</div>}

      <h3 style={{marginTop:16}}>Open Sections (capacity remaining)</h3>
      <div style={{display:'grid', gridTemplateColumns:'repeat(auto-fit, minmax(260px, 1fr))', gap:12}}>
        {sections.map(s => {
          const capacityLeft = s.capacity - s.enrolled
          return (
            <div key={s.id} style={{border:'1px solid #ddd', borderRadius:6, padding:12}}>
              <div style={{fontWeight:600}}>{s.courseCode}-{s.sectionNumber} {s.courseName}</div>
              <div style={{fontSize:12, opacity:0.8, margin:'4px 0'}}>{s.teacherName} · {s.roomName}</div>
              <div style={{fontSize:12}}>{s.meetings.map(m => `D${m.dayOfWeek} ${m.startTime}`).join(' · ')}</div>
              <div style={{marginTop:8}}>
                <button disabled={capacityLeft<=0} onClick={() => enroll(s.id)}>
                  {capacityLeft>0 ? `Enroll (${capacityLeft} left)` : 'Full'}
                </button>
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}
