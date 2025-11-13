import React, { useEffect, useState } from 'react'
import { api } from '../services/api'
import { StudentScheduleGrid } from './StudentScheduleGrid'

type StudentProgress = {
  studentId: number
  firstName: string
  lastName: string
  gradeLevel: number
  coursesTaken: number
  coursesPassed: number
  creditsEarned: number
  gpa: number
  creditsRequired: number
  creditsRemaining: number
  expectedGraduationYear: number
  onTrackToGraduate: boolean
  graduationStatus: string
}

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

type StudentSchedule = {
  studentId: number
  studentName: string
  semesterId: number
  semesterName: string
  enrolledSections: EnrolledSection[]
  progress: StudentProgress
}

type AvailableSection = {
  sectionId: number
  courseCode: string
  courseName: string
  sectionNumber: number
  teacherName: string
  roomName: string
  credits: number
  hoursPerWeek: number
  meetings: SectionMeeting[]
  capacity: number
  enrolledStudents: number
  availableSpots: number
  canEnroll: boolean
  enrollmentStatus: string
  statusMessage: string
  prerequisiteCourse?: string
  gradeLevel: number
  specializationName: string
  isCore: boolean
}

const DAYS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri']

export function StudentPlanning({ semesterId }: { semesterId: number }) {
  const [studentId, setStudentId] = useState(101) // Start with Linda Moore
  const [schedule, setSchedule] = useState<StudentSchedule | null>(null)
  const [availableSections, setAvailableSections] = useState<AvailableSection[]>([])
  const [loading, setLoading] = useState(true)
  const [enrolling, setEnrolling] = useState<number | null>(null)
  const [filter, setFilter] = useState('')
  const [showOnlyAvailable, setShowOnlyAvailable] = useState(true)

  useEffect(() => {
    loadStudentData()
  }, [studentId, semesterId])

  const loadStudentData = async () => {
    setLoading(true)
    try {
      const [scheduleData, sectionsData] = await Promise.all([
        api.get<StudentSchedule>(`/students/${studentId}/schedule?semesterId=${semesterId}`),
        api.get<AvailableSection[]>(`/students/${studentId}/available-sections?semesterId=${semesterId}`)
      ])
      setSchedule(scheduleData)
      setAvailableSections(sectionsData)
    } catch (error) {
      console.error('Error loading student data:', error)
      alert('Error loading student data')
    }
    setLoading(false)
  }

  const enroll = async (sectionId: number) => {
    setEnrolling(sectionId)
    try {
      const response = await api.post(`/students/${studentId}/enroll?sectionId=${sectionId}`)
      if (response.success) {
        alert('Successfully enrolled!')
        loadStudentData() // Refresh data
      } else {
        alert('Failed to enroll: ' + response.message)
      }
    } catch (error) {
      console.error('Error enrolling:', error)
      alert('Error enrolling in course')
    }
    setEnrolling(null)
  }

  const getDayName = (dayOfWeek: number) => DAYS[dayOfWeek - 1] || 'Unknown'

  const formatMeetings = (meetings: SectionMeeting[]) => {
    return meetings.map(m => `${getDayName(m.dayOfWeek)} ${m.startTime}`).join(', ')
  }

  const filteredSections = availableSections.filter(section => {
    if (showOnlyAvailable && !section.canEnroll) return false
    if (filter && !section.courseCode.toLowerCase().includes(filter.toLowerCase()) &&
        !section.courseName.toLowerCase().includes(filter.toLowerCase())) {
      return false
    }
    return true
  })

  if (loading) return <div>Loading student planning...</div>
  if (!schedule) return <div>No schedule data found</div>

  const progress = schedule.progress

  return (
    <div style={{ padding: 16, maxWidth: 1200, margin: '0 auto' }}>
      <h1>Student Course Planning</h1>

      {/* Student Selector */}
      <div style={{ marginBottom: 24 }}>
        <label style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <strong>Student ID:</strong>
          <input
            type="number"
            value={studentId}
            onChange={e => setStudentId(parseInt(e.target.value || '101'))}
            style={{
              padding: '8px 12px',
              borderRadius: 4,
              border: '1px solid #ddd',
              width: 100,
              fontSize: 14
            }}
            placeholder="101"
          />
          <span style={{ fontSize: 14, color: '#666' }}>
            (Try: 1=Ryan Adams, 101=Linda Moore, 2-10=Other 9th graders)
          </span>
        </label>
      </div>

      {/* Student Progress Section */}
      <div style={{
        background: '#f9f9f9',
        padding: 16,
        borderRadius: 8,
        marginBottom: 24,
        border: '1px solid #ddd'
      }}>
        <h2>{progress.firstName} {progress.lastName} - Grade {progress.gradeLevel}</h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 16 }}>
          <div>
            <strong>Academic Progress</strong>
            <div>Courses Taken: {progress.coursesTaken}</div>
            <div>Courses Passed: {progress.coursesPassed}</div>
            <div>Current GPA: {progress.gpa.toFixed(2)}</div>
          </div>
          <div>
            <strong>Credit Progress</strong>
            <div>Credits Earned: {progress.creditsEarned} / {progress.creditsRequired}</div>
            <div>Credits Remaining: {progress.creditsRemaining}</div>
            <div>Progress: {((progress.creditsEarned / progress.creditsRequired) * 100).toFixed(1)}%</div>
          </div>
          <div>
            <strong>Graduation Timeline</strong>
            <div>Expected Graduation: {progress.expectedGraduationYear}</div>
            <div style={{
              color: progress.onTrackToGraduate ? 'green' : 'orange',
              fontWeight: 600
            }}>
              Status: {progress.graduationStatus}
            </div>
          </div>
        </div>
      </div>

      {/* Current Schedule */}
      <div style={{ marginBottom: 24 }}>
        <h2>Current Schedule ({schedule.semesterName})</h2>
        {schedule.enrolledSections.length === 0 ? (
          <p>No courses enrolled for this semester.</p>
        ) : (
          <div style={{ display: 'grid', gap: 12 }}>
            {schedule.enrolledSections.map(section => (
              <div key={section.sectionId} style={{
                border: '1px solid #ccc',
                padding: 12,
                borderRadius: 8,
                background: '#fff'
              }}>
                <div style={{ fontWeight: 600, fontSize: 18 }}>
                  {section.courseCode}-{section.sectionNumber}: {section.courseName}
                </div>
                <div>Instructor: {section.teacherName}</div>
                <div>Room: {section.roomName}</div>
                <div>Credits: {section.credits}</div>
                <div>Schedule: {formatMeetings(section.meetings)}</div>
                <div>Enrollment: {section.enrolledStudents}/{section.capacity} students</div>
              </div>
            ))}
          </div>
        )}

        {/* Weekly Calendar View */}
        {schedule.enrolledSections.length > 0 && (
          <div style={{ marginTop: 24 }}>
            <StudentScheduleGrid sections={schedule.enrolledSections} />
          </div>
        )}
      </div>

      {/* Available Courses */}
      <div>
        <h2>Available Courses for Enrollment</h2>

        {/* Filters */}
        <div style={{ display: 'flex', gap: 16, marginBottom: 16, alignItems: 'center' }}>
          <input
            type="text"
            placeholder="Search courses..."
            value={filter}
            onChange={e => setFilter(e.target.value)}
            style={{ padding: 8, borderRadius: 4, border: '1px solid #ddd', width: 300 }}
          />
          <label style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
            <input
              type="checkbox"
              checked={showOnlyAvailable}
              onChange={e => setShowOnlyAvailable(e.target.checked)}
            />
            Show only available courses
          </label>
        </div>

        {filteredSections.length === 0 ? (
          <p>No courses available for enrollment.</p>
        ) : (
          <div style={{ display: 'grid', gap: 12 }}>
            {filteredSections.map(section => (
              <div key={section.sectionId} style={{
                border: '1px solid #ccc',
                padding: 12,
                borderRadius: 8,
                background: section.canEnroll ? '#fff' : '#f5f5f5',
                opacity: section.canEnroll ? 1 : 0.7
              }}>
                <div style={{ display: 'flex', justifyContent: 'between', alignItems: 'flex-start' }}>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontWeight: 600, fontSize: 18, marginBottom: 4 }}>
                      {section.courseCode}-{section.sectionNumber}: {section.courseName}
                      {section.isCore && (
                        <span style={{
                          background: '#007bff',
                          color: 'white',
                          padding: '2px 6px',
                          borderRadius: 4,
                          fontSize: 12,
                          marginLeft: 8
                        }}>
                          CORE
                        </span>
                      )}
                    </div>
                    <div style={{ fontSize: 14, color: '#666', marginBottom: 8 }}>
                      {section.specializationName} • Grade {section.gradeLevel}+ • {section.credits} credits
                    </div>
                    <div>Instructor: {section.teacherName}</div>
                    <div>Room: {section.roomName}</div>
                    <div>Schedule: {formatMeetings(section.meetings)}</div>
                    <div>Enrollment: {section.enrolledStudents}/{section.capacity} students
                      ({section.availableSpots} spots available)</div>
                    {section.prerequisiteCourse && (
                      <div style={{ color: '#666' }}>Prerequisite: {section.prerequisiteCourse}</div>
                    )}
                    {!section.canEnroll && (
                      <div style={{ color: 'red', fontWeight: 600, marginTop: 4 }}>
                        {section.statusMessage}
                      </div>
                    )}
                  </div>

                  <button
                    onClick={() => enroll(section.sectionId)}
                    disabled={!section.canEnroll || enrolling === section.sectionId}
                    style={{
                      padding: '8px 16px',
                      backgroundColor: section.canEnroll ? '#28a745' : '#6c757d',
                      color: 'white',
                      border: 'none',
                      borderRadius: 4,
                      cursor: section.canEnroll ? 'pointer' : 'not-allowed',
                      marginLeft: 16
                    }}
                  >
                    {enrolling === section.sectionId ? 'Enrolling...' :
                     section.canEnroll ? 'Enroll' : 'Cannot Enroll'}
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}