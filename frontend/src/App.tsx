import React, { useEffect, useState } from 'react'
import { api } from './services/api'
import { ScheduleGrid } from './components/ScheduleGrid'
import { StudentPlanner } from './components/StudentPlanner'
import { StudentPlanning } from './components/StudentPlanning'

type Semester = { id:number; name:string; year:number; orderInYear:number; isActive:boolean }

export default function App() {
  const [semesters, setSemesters] = useState<Semester[]>([])
  const [semesterId, setSemesterId] = useState<number | null>(null)
  const [loading, setLoading] = useState(false)
  const [sections, setSections] = useState<any[]>([])
  const [sectionsPerCourse, setSectionsPerCourse] = useState(1)
  const [activeTab, setActiveTab] = useState<'schedule' | 'students' | 'planning'>('schedule')

  useEffect(() => {
    api.get<Semester[]>('/semesters').then(r => {
      setSemesters(r)
      if (r.length) setSemesterId(r[0].id)
    })
  }, [])

  const onGenerate = async () => {
    if (!semesterId) return
    setLoading(true)
    try {
      const response = await api.post('/master-schedule/generate', { semesterId })
      if (response.success) {
        setSections(response.data?.entries || [])
      } else {
        alert('Failed to generate schedule: ' + response.message)
      }
    } catch (error) {
      console.error('Error generating schedule:', error)
      alert('Error generating schedule')
    }
    setLoading(false)
  }

  const onLoad = async () => {
    if (!semesterId) return
    setLoading(true)
    try {
      const response = await api.get(`/master-schedule/semester/${semesterId}`)
      if (response.success) {
        setSections(response.data?.entries || [])
      } else {
        alert('Failed to load schedule: ' + response.message)
      }
    } catch (error) {
      console.error('Error loading schedule:', error)
      alert('Error loading schedule')
    }
    setLoading(false)
  }

  return (
    <div style={{ fontFamily: 'system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial', padding: 16, maxWidth: 1200, margin: '0 auto' }}>
      <h1>Maplewood Scheduler</h1>

      {/* Navigation Tabs */}
      <div style={{ display: 'flex', gap: 0, marginBottom: 24, borderBottom: '1px solid #ddd' }}>
        {[
          { key: 'schedule' as const, label: 'Master Schedule' },
          { key: 'students' as const, label: 'Student Planner' },
          { key: 'planning' as const, label: 'Course Planning' }
        ].map(tab => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            style={{
              padding: '12px 24px',
              border: 'none',
              backgroundColor: activeTab === tab.key ? '#007bff' : 'transparent',
              color: activeTab === tab.key ? 'white' : '#007bff',
              cursor: 'pointer',
              borderBottom: activeTab === tab.key ? '2px solid #007bff' : '2px solid transparent'
            }}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Semester Selector */}
      <div style={{ display: 'flex', gap: 12, alignItems: 'center', flexWrap: 'wrap', marginBottom: 24 }}>
        <label>Semester:&nbsp;
          <select value={semesterId ?? ''} onChange={e => setSemesterId(parseInt(e.target.value))}>
            <option value="">Select semester...</option>
            {semesters.map(s => <option key={s.id} value={s.id}>{s.name} {s.year}</option>)}
          </select>
        </label>
      </div>

      {/* Tab Content */}
      {activeTab === 'schedule' && (
        <div>
          <div style={{ display: 'flex', gap: 12, alignItems: 'center', flexWrap: 'wrap', marginBottom: 16 }}>
            <label>Sections / Course:&nbsp;
              <input type="number" min={1} max={5} value={sectionsPerCourse} onChange={e => setSectionsPerCourse(parseInt(e.target.value || '1'))} style={{width:60}}/>
            </label>
            <button onClick={onGenerate} disabled={loading || !semesterId}>Generate</button>
            <button onClick={onLoad} disabled={loading || !semesterId}>Refresh</button>
          </div>
          <ScheduleGrid sections={sections} />
        </div>
      )}

      {activeTab === 'students' && (
        <StudentPlanner semesterId={semesterId || 0} />
      )}

      {activeTab === 'planning' && semesterId && (
        <StudentPlanning semesterId={semesterId} />
      )}

      {activeTab === 'planning' && !semesterId && (
        <div>Please select a semester to view course planning.</div>
      )}
    </div>
  )
}
