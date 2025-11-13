import '@testing-library/jest-dom'
import { vi } from 'vitest'

// Global test setup
global.fetch = vi.fn()

// Mock API responses
export const mockApiResponse = (data: any) => {
  return {
    ok: true,
    status: 200,
    json: async () => data,
  }
}