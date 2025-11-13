import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { ScheduleGrid } from './ScheduleGrid'

// Mock data
const mockSections = [
  {
    sectionId: 1,
    courseCode: 'MAT101',
    courseName: 'Algebra I',
    sectionNumber: 1,
    teacherName: 'John Smith',
    roomName: 'Room-205',
    meetings: [
      {
        id: 1,
        dayOfWeek: 1, // Monday
        startTime: '09:00',
        durationMinutes: 60
      },
      {
        id: 2,
        dayOfWeek: 3, // Wednesday
        startTime: '10:00',
        durationMinutes: 60
      }
    ],
    capacity: 10,
    enrolledStudents: 5,
    availableSpots: 5
  }
]

describe('ScheduleGrid', () => {
  it('renders schedule grid with sections', () => {
    render(<ScheduleGrid sections={mockSections} />)

    // Check for course information
    expect(screen.getByText('MAT101')).toBeInTheDocument()
    expect(screen.getByText('Algebra I')).toBeInTheDocument()
    expect(screen.getByText('John Smith')).toBeInTheDocument()
    expect(screen.getByText('Room-205')).toBeInTheDocument()
  })

  it('displays time slots correctly', () => {
    render(<ScheduleGrid sections={mockSections} />)

    // Check for time slots (should include all valid school hours)
    expect(screen.getByText('9:00')).toBeInTheDocument()
    expect(screen.getByText('10:00')).toBeInTheDocument()
    expect(screen.getByText('11:00')).toBeInTheDocument()

    // Should NOT include lunch time (12:00)
    expect(screen.queryByText('12:00')).not.toBeInTheDocument()

    // Should include afternoon slots
    expect(screen.getByText('13:00')).toBeInTheDocument()
    expect(screen.getByText('14:00')).toBeInTheDocument()
  })

  it('shows capacity information', () => {
    render(<ScheduleGrid sections={mockSections} />)

    expect(screen.getByText('5/10')).toBeInTheDocument() // enrolled/capacity
  })

  it('handles empty sections gracefully', () => {
    render(<ScheduleGrid sections={[]} />)

    expect(screen.getByText('No sections scheduled')).toBeInTheDocument()
  })

  it('enforces time constraints (no classes during lunch)', () => {
    const sectionsWithLunchTime = [
      {
        ...mockSections[0],
        meetings: [
          {
            id: 1,
            dayOfWeek: 1,
            startTime: '12:00', // This should not be allowed
            durationMinutes: 60
          }
        ]
      }
    ]

    render(<ScheduleGrid sections={sectionsWithLunchTime} />)

    // Should show warning or filter out lunch time slots
    expect(screen.queryByText('Invalid time slot')).toBeInTheDocument()
  })
})