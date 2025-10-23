import { getActivities, registerForActivity } from '../../lib/api'

// Mock fetch globally
global.fetch = jest.fn()

describe('API Module', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  afterEach(() => {
    jest.resetAllMocks()
  })

  // ==========================================
  // getActivities Tests
  // ==========================================
  describe('getActivities', () => {
    it('should fetch activities successfully', async () => {
      // Arrange
      const mockActivities = [
        {
          id: 1,
          nombre: 'Tirolesa',
          descripcion: 'Actividad de aventura',
          requiereVestimenta: true,
          cupoMaximo: 10,
          terminosCondiciones: 'Términos',
          edadMinima: 12,
        },
      ]

      const mockHorarios = [
        {
          id: 1,
          fecha: '2025-10-22',
          horaInicio: '10:00',
          horaFin: '12:00',
          cuposDisponibles: 5,
          cupoMaximo: 10,
          nombreActividad: 'Tirolesa',
        },
      ]

      ;(global.fetch as jest.Mock)
        .mockResolvedValueOnce({
          ok: true,
          json: async () => mockActivities,
        })
        .mockResolvedValueOnce({
          ok: true,
          json: async () => mockHorarios,
        })

      // Act
      const result = await getActivities()

      // Assert
      expect(result).toHaveLength(1)
      expect(result[0].name).toBe('Tirolesa')
      expect(result[0].availableSlots).toBe(5)
      expect(result[0].isAvailableToday).toBe(true)
      expect(fetch).toHaveBeenCalledTimes(2)
    })

    it('should handle activities fetch error', async () => {
      // Arrange
      ;(global.fetch as jest.Mock).mockRejectedValueOnce(new Error('Network error'))

      // Act & Assert
      await expect(getActivities()).rejects.toThrow('Network error')
    })

    it('should handle non-ok response for activities', async () => {
      // Arrange
      ;(global.fetch as jest.Mock).mockResolvedValueOnce({
        ok: false,
        status: 500,
        statusText: 'Internal Server Error',
      })

      // Act & Assert
      await expect(getActivities()).rejects.toThrow('Error al cargar las actividades')
    })

    it('should handle horarios fetch error gracefully', async () => {
      // Arrange
      const mockActivities = [
        {
          id: 1,
          nombre: 'Tirolesa',
          descripcion: 'Actividad de aventura',
          requiereVestimenta: true,
          cupoMaximo: 10,
          terminosCondiciones: 'Términos',
          edadMinima: 12,
        },
      ]

      ;(global.fetch as jest.Mock)
        .mockResolvedValueOnce({
          ok: true,
          json: async () => mockActivities,
        })
        .mockRejectedValueOnce(new Error('Horarios error'))

      // Act
      const result = await getActivities()

      // Assert - Should still return activity with default values
      expect(result).toHaveLength(1)
      expect(result[0].availableSlots).toBe(0)
      expect(result[0].isAvailableToday).toBe(false)
    })

    it('should calculate total available spots correctly', async () => {
      // Arrange
      const mockActivities = [
        {
          id: 1,
          nombre: 'Tirolesa',
          descripcion: 'Actividad de aventura',
          requiereVestimenta: true,
          cupoMaximo: 10,
          terminosCondiciones: 'Términos',
          edadMinima: 12,
        },
      ]

      const mockHorarios = [
        {
          id: 1,
          fecha: '2025-10-22',
          horaInicio: '10:00',
          horaFin: '12:00',
          cuposDisponibles: 5,
          cupoMaximo: 10,
          nombreActividad: 'Tirolesa',
        },
        {
          id: 2,
          fecha: '2025-10-22',
          horaInicio: '14:00',
          horaFin: '16:00',
          cuposDisponibles: 3,
          cupoMaximo: 10,
          nombreActividad: 'Tirolesa',
        },
      ]

      ;(global.fetch as jest.Mock)
        .mockResolvedValueOnce({
          ok: true,
          json: async () => mockActivities,
        })
        .mockResolvedValueOnce({
          ok: true,
          json: async () => mockHorarios,
        })

      // Act
      const result = await getActivities()

      // Assert
      expect(result[0].availableSlots).toBe(8) // 5 + 3
    })

    it('should mark activity as unavailable when no horarios', async () => {
      // Arrange
      const mockActivities = [
        {
          id: 1,
          nombre: 'Tirolesa',
          descripcion: 'Actividad de aventura',
          requiereVestimenta: true,
          cupoMaximo: 10,
          terminosCondiciones: 'Términos',
          edadMinima: 12,
        },
      ]

      ;(global.fetch as jest.Mock)
        .mockResolvedValueOnce({
          ok: true,
          json: async () => mockActivities,
        })
        .mockResolvedValueOnce({
          ok: true,
          json: async () => [], // Empty horarios
        })

      // Act
      const result = await getActivities()

      // Assert
      expect(result[0].isAvailableToday).toBe(false)
      expect(result[0].availableSlots).toBe(0)
    })

    it('should include requiereVestimenta flag', async () => {
      // Arrange
      const mockActivities = [
        {
          id: 1,
          nombre: 'Tirolesa',
          descripcion: 'Actividad de aventura',
          requiereVestimenta: true,
          cupoMaximo: 10,
          terminosCondiciones: 'Términos',
          edadMinima: 12,
        },
      ]

      ;(global.fetch as jest.Mock)
        .mockResolvedValueOnce({
          ok: true,
          json: async () => mockActivities,
        })
        .mockResolvedValueOnce({
          ok: true,
          json: async () => [],
        })

      // Act
      const result = await getActivities()

      // Assert
      expect(result[0].requiereVestimenta).toBe(true)
    })
  })

  // ==========================================
  // registerForActivity Tests
  // ==========================================
  describe('registerForActivity', () => {
    it('should successfully register for activity', async () => {
      // Arrange
      const mockRequest = {
        visitantes: [
          {
            nombre: 'Juan Pérez',
            dni: '12345678',
            edad: 25,
            tallaVestimenta: 'M',
          },
        ],
        horarioActividadId: 1,
        cantidadPersonas: 1,
        email: 'test@example.com',
      }

      const mockResponse = {
        id: 123,
        message: 'Inscripción exitosa',
      }

      ;(global.fetch as jest.Mock).mockResolvedValueOnce({
        ok: true,
        text: async () => JSON.stringify(mockResponse),
      })

      // Act
      const result = await registerForActivity(mockRequest)

      // Assert
      expect(result.success).toBe(true)
      expect(result.message).toBe('Inscripción exitosa')
      expect(result.data).toEqual(mockResponse)
      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('/inscripciones'),
        expect.objectContaining({
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(mockRequest),
        })
      )
    })

    it('should handle duplicate registration error', async () => {
      // Arrange
      const mockRequest = {
        visitantes: [
          {
            nombre: 'Juan Pérez',
            dni: '12345678',
            edad: 25,
            tallaVestimenta: null,
          },
        ],
        horarioActividadId: 1,
        cantidadPersonas: 1,
        email: 'test@example.com',
      }

      const errorResponse = {
        message: 'Ya existe una inscripción con ese DNI',
      }

      ;(global.fetch as jest.Mock).mockResolvedValueOnce({
        ok: false,
        status: 400,
        text: async () => JSON.stringify(errorResponse),
      })

      // Act
      const result = await registerForActivity(mockRequest)

      // Assert
      expect(result.success).toBe(false)
      expect(result.message).toBe('Ya existe una inscripción con ese DNI')
    })

    it('should handle full capacity error', async () => {
      // Arrange
      const mockRequest = {
        visitantes: [
          {
            nombre: 'Juan Pérez',
            dni: '12345678',
            edad: 25,
            tallaVestimenta: null,
          },
        ],
        horarioActividadId: 1,
        cantidadPersonas: 1,
        email: 'test@example.com',
      }

      ;(global.fetch as jest.Mock).mockResolvedValueOnce({
        ok: false,
        status: 400,
        text: async () => 'No hay cupos disponibles',
      })

      // Act
      const result = await registerForActivity(mockRequest)

      // Assert
      expect(result.success).toBe(false)
      expect(result.message).toBe('No hay cupos disponibles')
    })

    it('should handle network errors', async () => {
      // Arrange
      const mockRequest = {
        visitantes: [
          {
            nombre: 'Juan Pérez',
            dni: '12345678',
            edad: 25,
            tallaVestimenta: null,
          },
        ],
        horarioActividadId: 1,
        cantidadPersonas: 1,
        email: 'test@example.com',
      }

      ;(global.fetch as jest.Mock).mockRejectedValueOnce(new Error('Network error'))

      // Act
      const result = await registerForActivity(mockRequest)

      // Assert
      expect(result.success).toBe(false)
      expect(result.message).toBe('Error de conexión. Por favor, intente nuevamente.')
    })

    it('should handle empty response body', async () => {
      // Arrange
      const mockRequest = {
        visitantes: [
          {
            nombre: 'Juan Pérez',
            dni: '12345678',
            edad: 25,
            tallaVestimenta: null,
          },
        ],
        horarioActividadId: 1,
        cantidadPersonas: 1,
        email: 'test@example.com',
      }

      ;(global.fetch as jest.Mock).mockResolvedValueOnce({
        ok: true,
        text: async () => '',
      })

      // Act
      const result = await registerForActivity(mockRequest)

      // Assert
      expect(result.success).toBe(true)
      expect(result.message).toBe('Inscripción exitosa')
    })

    it('should handle non-JSON error response', async () => {
      // Arrange
      const mockRequest = {
        visitantes: [
          {
            nombre: 'Juan Pérez',
            dni: '12345678',
            edad: 25,
            tallaVestimenta: null,
          },
        ],
        horarioActividadId: 1,
        cantidadPersonas: 1,
        email: 'test@example.com',
      }

      ;(global.fetch as jest.Mock).mockResolvedValueOnce({
        ok: false,
        status: 500,
        text: async () => 'Internal Server Error',
      })

      // Act
      const result = await registerForActivity(mockRequest)

      // Assert
      expect(result.success).toBe(false)
      expect(result.message).toBe('Internal Server Error')
    })

    it('should handle multiple participants', async () => {
      // Arrange
      const mockRequest = {
        visitantes: [
          {
            nombre: 'Juan Pérez',
            dni: '12345678',
            edad: 25,
            tallaVestimenta: 'M',
          },
          {
            nombre: 'María García',
            dni: '87654321',
            edad: 30,
            tallaVestimenta: 'S',
          },
        ],
        horarioActividadId: 1,
        cantidadPersonas: 2,
        email: 'test@example.com',
      }

      ;(global.fetch as jest.Mock).mockResolvedValueOnce({
        ok: true,
        text: async () => JSON.stringify({ id: 123 }),
      })

      // Act
      const result = await registerForActivity(mockRequest)

      // Assert
      expect(result.success).toBe(true)
      const calledBody = JSON.parse((fetch as jest.Mock).mock.calls[0][1].body)
      expect(calledBody.visitantes).toHaveLength(2)
      expect(calledBody.cantidadPersonas).toBe(2)
    })

    it('should handle null email', async () => {
      // Arrange
      const mockRequest = {
        visitantes: [
          {
            nombre: 'Juan Pérez',
            dni: '12345678',
            edad: 25,
            tallaVestimenta: null,
          },
        ],
        horarioActividadId: 1,
        cantidadPersonas: 1,
        email: null,
      }

      ;(global.fetch as jest.Mock).mockResolvedValueOnce({
        ok: true,
        text: async () => JSON.stringify({ id: 123 }),
      })

      // Act
      const result = await registerForActivity(mockRequest)

      // Assert
      expect(result.success).toBe(true)
    })

    it('should use correct API endpoint', async () => {
      // Arrange
      const mockRequest = {
        visitantes: [
          {
            nombre: 'Juan Pérez',
            dni: '12345678',
            edad: 25,
            tallaVestimenta: null,
          },
        ],
        horarioActividadId: 1,
        cantidadPersonas: 1,
        email: 'test@example.com',
      }

      ;(global.fetch as jest.Mock).mockResolvedValueOnce({
        ok: true,
        text: async () => JSON.stringify({ id: 123 }),
      })

      // Act
      await registerForActivity(mockRequest)

      // Assert
      expect(fetch).toHaveBeenCalledWith(
        expect.stringMatching(/\/inscripciones$/),
        expect.any(Object)
      )
    })

    it('should send correct Content-Type header', async () => {
      // Arrange
      const mockRequest = {
        visitantes: [
          {
            nombre: 'Juan Pérez',
            dni: '12345678',
            edad: 25,
            tallaVestimenta: null,
          },
        ],
        horarioActividadId: 1,
        cantidadPersonas: 1,
        email: 'test@example.com',
      }

      ;(global.fetch as jest.Mock).mockResolvedValueOnce({
        ok: true,
        text: async () => JSON.stringify({ id: 123 }),
      })

      // Act
      await registerForActivity(mockRequest)

      // Assert
      const callArgs = (fetch as jest.Mock).mock.calls[0]
      expect(callArgs[1].headers['Content-Type']).toBe('application/json')
    })
  })
})
