import { describe, it, expect, vi, beforeEach } from 'vitest'
import { api } from './api'

// Mock fetch globally
global.fetch = vi.fn()

describe('API Service', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('makes GET request successfully', async () => {
    const mockData = [{ id: 1, name: 'Fall 2024' }]

    vi.mocked(fetch).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => mockData,
    } as Response)

    const result = await api.get('/semesters')

    expect(fetch).toHaveBeenCalledWith(
      'http://localhost:8080/api/semesters',
      expect.objectContaining({
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      })
    )

    expect(result).toEqual(mockData)
  })

  it('makes POST request successfully', async () => {
    const mockResponse = { message: 'Schedule generated', success: true }
    const requestData = { semesterId: 1 }

    vi.mocked(fetch).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => mockResponse,
    } as Response)

    const result = await api.post('/master-schedule/generate', requestData)

    expect(fetch).toHaveBeenCalledWith(
      'http://localhost:8080/api/master-schedule/generate',
      expect.objectContaining({
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestData),
      })
    )

    expect(result).toEqual(mockResponse)
  })

  it('handles API errors gracefully', async () => {
    vi.mocked(fetch).mockResolvedValueOnce({
      ok: false,
      status: 500,
      statusText: 'Internal Server Error',
    } as Response)

    await expect(api.get('/semesters')).rejects.toThrow('HTTP error! status: 500')
  })

  it('handles network errors', async () => {
    vi.mocked(fetch).mockRejectedValueOnce(new Error('Network error'))

    await expect(api.get('/semesters')).rejects.toThrow('Network error')
  })

  it('uses correct base URL', async () => {
    vi.mocked(fetch).mockResolvedValueOnce({
      ok: true,
      json: async () => ({}),
    } as Response)

    await api.get('/test')

    expect(fetch).toHaveBeenCalledWith(
      'http://localhost:8080/api/test',
      expect.any(Object)
    )
  })
})