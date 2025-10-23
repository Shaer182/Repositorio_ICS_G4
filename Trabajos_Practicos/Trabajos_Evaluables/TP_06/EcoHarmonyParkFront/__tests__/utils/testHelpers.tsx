import React from 'react'
import { render, RenderOptions } from '@testing-library/react'

// Mock data generators for tests
export const mockActivity = (overrides = {}) => ({
  id: 1,
  name: 'Tirolesa',
  description: 'Actividad de tirolesa',
  requiereVestimenta: true,
  capacity: 10,
  terminosCondiciones: 'Términos y condiciones de la actividad',
  horarios: [],
  ...overrides,
})

export const mockTimeSlot = (overrides = {}) => ({
  id: '1',
  time: '10:00 - 12:00',
  date: '2025-10-25',
  availableSpots: 5,
  isAvailable: true,
  raw: {
    id: 1,
    fecha: '2025-10-25',
    horaInicio: '10:00',
    horaFin: '12:00',
    cuposDisponibles: 5,
    cupoMaximo: 10,
    nombreActividad: 'Tirolesa',
  },
  ...overrides,
})

export const mockParticipant = (overrides = {}) => ({
  name: 'Juan Pérez',
  dni: '12345678',
  age: '25',
  clothingSize: 'M',
  ...overrides,
})

export const mockApiResponse = (data: any, success = true) => ({
  ok: success,
  json: async () => data,
  text: async () => JSON.stringify(data),
  status: success ? 200 : 400,
  statusText: success ? 'OK' : 'Bad Request',
})

// Custom render function that includes common providers
export function renderWithProviders(
  ui: React.ReactElement,
  options?: Omit<RenderOptions, 'wrapper'>
) {
  return render(ui, { ...options })
}

// Wait for async operations
export const waitForAsync = () => new Promise((resolve) => setTimeout(resolve, 0))

// Mock fetch helper
export const mockFetch = (response: any, success = true) => {
  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: success,
      json: async () => response,
      text: async () => JSON.stringify(response),
      status: success ? 200 : 400,
      statusText: success ? 'OK' : 'Bad Request',
    } as Response)
  )
}

// Reset fetch mock
export const resetFetchMock = () => {
  global.fetch = jest.fn()
}
