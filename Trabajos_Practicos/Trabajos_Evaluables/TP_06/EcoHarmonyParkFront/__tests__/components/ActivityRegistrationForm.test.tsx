import React from 'react'
import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import '@testing-library/jest-dom'
import { ActivityRegistrationForm } from '../../components/ActivityRegistrationForm'
import * as api from '../../lib/api'

// Mock the API module
jest.mock('../../lib/api', () => ({
  registerForActivity: jest.fn(),
}))

// Mock fetch globally
global.fetch = jest.fn()

describe('ActivityRegistrationForm', () => {
  beforeEach(() => {
    jest.clearAllMocks()
    // Default mock for activities fetch
    ;(global.fetch as jest.Mock).mockImplementation((url: string) => {
      if (url.includes('/actividades') && !url.includes('/horarios')) {
        return Promise.resolve({
          ok: true,
          json: async () => [
            {
              id: 1,
              nombre: 'Tirolesa',
              descripcion: 'Actividad de aventura',
              requiereVestimenta: true,
              cupoMaximo: 10,
              terminosCondiciones: 'Términos de tirolesa',
              edadMinima: 12,
            },
            {
              id: 2,
              nombre: 'Safari',
              descripcion: 'Tour por el parque',
              requiereVestimenta: false,
              cupoMaximo: 20,
              terminosCondiciones: 'Términos de safari',
              edadMinima: 5,
            },
          ],
        })
      }
      if (url.includes('/horarios')) {
        return Promise.resolve({
          ok: true,
          json: async () => [
            {
              id: 1,
              fecha: '2025-10-25',
              horaInicio: '10:00',
              horaFin: '12:00',
              cuposDisponibles: 5,
              cupoMaximo: 10,
              nombreActividad: 'Tirolesa',
            },
            {
              id: 2,
              fecha: '2025-10-25',
              horaInicio: '14:00',
              horaFin: '16:00',
              cuposDisponibles: 8,
              cupoMaximo: 10,
              nombreActividad: 'Tirolesa',
            },
          ],
        })
      }
      return Promise.reject(new Error('Unknown URL'))
    })
  })

  // ==========================================
  // Initial Rendering Tests
  // ==========================================
  it('should render the form header', async () => {
    // Arrange & Act
    render(<ActivityRegistrationForm />)

    // Assert
    await waitFor(() => {
      expect(screen.getByText(/inscripción a actividades/i)).toBeInTheDocument()
    })
  })

  it('should load and display activities on mount', async () => {
    // Arrange & Act
    render(<ActivityRegistrationForm />)

    // Assert
    await waitFor(() => {
      expect(screen.getByText('Tirolesa')).toBeInTheDocument()
      expect(screen.getByText('Safari')).toBeInTheDocument()
    })
  })

  it('should display error when activities fail to load', async () => {
    // Arrange
    ;(global.fetch as jest.Mock).mockRejectedValueOnce(new Error('Network error'))

    // Act
    render(<ActivityRegistrationForm />)

    // Assert
    await waitFor(() => {
      expect(screen.getByText(/no se pudieron cargar las actividades/i)).toBeInTheDocument()
    })
  })

  // ==========================================
  // CASO DE PRUEBA 10: Flujo Multi-Step
  // ==========================================
  it('should navigate through all steps correctly', async () => {
    // Arrange
    const user = userEvent.setup()
    ;(api.registerForActivity as jest.Mock).mockResolvedValue({
      success: true,
      message: 'Inscripción exitosa',
    })

    // Act & Assert - Step 1: Select Activity
    render(<ActivityRegistrationForm />)

    await waitFor(() => {
      expect(screen.getByText('Tirolesa')).toBeInTheDocument()
    })

    const selectActivityButton = screen.getAllByRole('button', { name: /seleccionar/i })[0]
    await user.click(selectActivityButton)

    const nextButton = screen.getByRole('button', { name: /siguiente/i })
    await user.click(nextButton)

    // Step 2: Select Date and Time
    await waitFor(() => {
      expect(screen.getByLabelText(/fecha/i)).toBeInTheDocument()
    })

    const dateInput = screen.getByLabelText(/fecha/i)
    await user.type(dateInput, '2025-10-25')

    await waitFor(() => {
      expect(screen.getByText('10:00 - 12:00')).toBeInTheDocument()
    })

    const selectTimeButton = screen.getByRole('button', { name: /seleccionar horario/i })
    await user.click(selectTimeButton)

    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    // Step 3: Fill Participants
    await waitFor(() => {
      expect(screen.getByText(/participantes/i)).toBeInTheDocument()
    })

    const nameInput = screen.getByLabelText(/nombre completo/i)
    const dniInput = screen.getByLabelText(/dni/i)
    const ageInput = screen.getByLabelText(/edad/i)
    const emailInput = screen.getByLabelText(/email de contacto/i)

    await user.type(nameInput, 'Juan Pérez')
    await user.type(dniInput, '12345678')
    await user.type(ageInput, '25')
    await user.type(emailInput, 'juan@example.com')

    const tallaSelect = screen.getByLabelText(/talla de vestimenta/i)
    await user.selectOptions(tallaSelect, 'M')

    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    // Step 4: Accept Terms
    await waitFor(() => {
      expect(screen.getByText(/términos y condiciones/i)).toBeInTheDocument()
    })

    const termsCheckbox = screen.getByRole('checkbox')
    await user.click(termsCheckbox)

    const confirmButton = screen.getByRole('button', { name: /confirmar inscripción/i })
    await user.click(confirmButton)

    // Step 5: Confirmation
    await waitFor(() => {
      expect(screen.getByText(/inscripción exitosa/i)).toBeInTheDocument()
    })
  })

  it('should allow going back to previous steps', async () => {
    // Arrange
    const user = userEvent.setup()
    render(<ActivityRegistrationForm />)

    await waitFor(() => {
      expect(screen.getByText('Tirolesa')).toBeInTheDocument()
    })

    // Act - Go forward
    const selectButton = screen.getAllByRole('button', { name: /seleccionar/i })[0]
    await user.click(selectButton)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    // Go back
    const backButton = screen.getByRole('button', { name: /anterior/i })
    await user.click(backButton)

    // Assert
    expect(screen.getByText(/seleccione una actividad/i)).toBeInTheDocument()
  })

  it('should disable back button on first step', async () => {
    // Arrange & Act
    render(<ActivityRegistrationForm />)

    await waitFor(() => {
      expect(screen.getByText('Tirolesa')).toBeInTheDocument()
    })

    // Assert
    const backButton = screen.getByRole('button', { name: /anterior/i })
    expect(backButton).toBeDisabled()
  })

  // ==========================================
  // CASO DE PRUEBA 1: Datos Faltantes - Validación de Campos Obligatorios
  // ==========================================
  it('should not allow advancing without selecting an activity', async () => {
    // Arrange
    const user = userEvent.setup()
    render(<ActivityRegistrationForm />)

    await waitFor(() => {
      expect(screen.getByText('Tirolesa')).toBeInTheDocument()
    })

    // Act
    const nextButton = screen.getByRole('button', { name: /siguiente/i })
    await user.click(nextButton)

    // Assert
    await waitFor(() => {
      expect(screen.getByText(/debe seleccionar una actividad/i)).toBeInTheDocument()
    })
  })

  it('should not allow advancing without selecting date and time', async () => {
    // Arrange
    const user = userEvent.setup()
    render(<ActivityRegistrationForm />)

    await waitFor(() => {
      expect(screen.getByText('Tirolesa')).toBeInTheDocument()
    })

    // Act - Select activity and advance
    const selectButton = screen.getAllByRole('button', { name: /seleccionar/i })[0]
    await user.click(selectButton)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    // Try to advance without selecting date/time
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    // Assert
    await waitFor(() => {
      expect(screen.getByText(/debe seleccionar una fecha/i)).toBeInTheDocument()
    })
  })

  it('should validate all required participant fields', async () => {
    // Arrange
    const user = userEvent.setup()
    ;(api.registerForActivity as jest.Mock).mockResolvedValue({
      success: true,
      message: 'Inscripción exitosa',
    })

    render(<ActivityRegistrationForm />)

    await waitFor(() => {
      expect(screen.getByText('Tirolesa')).toBeInTheDocument()
    })

    // Act - Navigate to participants step
    const selectActivityButton = screen.getAllByRole('button', { name: /seleccionar/i })[0]
    await user.click(selectActivityButton)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    const dateInput = screen.getByLabelText(/fecha/i)
    await user.type(dateInput, '2025-10-25')

    await waitFor(() => {
      expect(screen.getByText('10:00 - 12:00')).toBeInTheDocument()
    })

    const selectTimeButton = screen.getByRole('button', { name: /seleccionar horario/i })
    await user.click(selectTimeButton)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    // Try to advance without filling fields
    await waitFor(() => {
      expect(screen.getByLabelText(/nombre completo/i)).toBeInTheDocument()
    })

    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    // Assert - Should show validation errors
    await waitFor(() => {
      expect(screen.getByText(/el nombre es requerido/i)).toBeInTheDocument()
      expect(screen.getByText(/el dni es requerido/i)).toBeInTheDocument()
      expect(screen.getByText(/la edad es requerida/i)).toBeInTheDocument()
      expect(screen.getByText(/el email de contacto es requerido/i)).toBeInTheDocument()
    })
  })

  // ==========================================
  // CASO DE PRUEBA 6: Validación de DNI - Solo 8 dígitos
  // ==========================================
  it('should reject DNI with less than 8 digits', async () => {
    // Arrange
    const user = userEvent.setup()
    render(<ActivityRegistrationForm />)

    await waitFor(() => {
      expect(screen.getByText('Tirolesa')).toBeInTheDocument()
    })

    // Navigate to participants step
    const selectActivityButton = screen.getAllByRole('button', { name: /seleccionar/i })[0]
    await user.click(selectActivityButton)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    const dateInput = screen.getByLabelText(/fecha/i)
    await user.type(dateInput, '2025-10-25')

    await waitFor(() => {
      expect(screen.getByText('10:00 - 12:00')).toBeInTheDocument()
    })

    const selectTimeButton = screen.getByRole('button', { name: /seleccionar horario/i })
    await user.click(selectTimeButton)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    // Act - Enter invalid DNI
    await waitFor(() => {
      expect(screen.getByLabelText(/nombre completo/i)).toBeInTheDocument()
    })

    await user.type(screen.getByLabelText(/nombre completo/i), 'Juan Pérez')
    await user.type(screen.getByLabelText(/dni/i), '1234567') // Only 7 digits
    await user.type(screen.getByLabelText(/edad/i), '25')
    await user.type(screen.getByLabelText(/email de contacto/i), 'test@example.com')

    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    // Assert
    await waitFor(() => {
      expect(screen.getByText(/el dni debe tener exactamente 8 dígitos/i)).toBeInTheDocument()
    })
  })

  it('should reject DNI with more than 8 digits', async () => {
    // Arrange
    const user = userEvent.setup()
    render(<ActivityRegistrationForm />)

    await waitFor(() => {
      expect(screen.getByText('Tirolesa')).toBeInTheDocument()
    })

    // Navigate to participants
    const selectActivityButton = screen.getAllByRole('button', { name: /seleccionar/i })[0]
    await user.click(selectActivityButton)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    const dateInput = screen.getByLabelText(/fecha/i)
    await user.type(dateInput, '2025-10-25')

    await waitFor(() => {
      expect(screen.getByText('10:00 - 12:00')).toBeInTheDocument()
    })

    const selectTimeButton = screen.getByRole('button', { name: /seleccionar horario/i })
    await user.click(selectTimeButton)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    // Act
    await waitFor(() => {
      expect(screen.getByLabelText(/nombre completo/i)).toBeInTheDocument()
    })

    await user.type(screen.getByLabelText(/nombre completo/i), 'Juan Pérez')
    await user.type(screen.getByLabelText(/dni/i), '123456789') // 9 digits
    await user.type(screen.getByLabelText(/edad/i), '25')
    await user.type(screen.getByLabelText(/email de contacto/i), 'test@example.com')

    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    // Assert
    await waitFor(() => {
      expect(screen.getByText(/el dni debe tener exactamente 8 dígitos/i)).toBeInTheDocument()
    })
  })

  // ==========================================
  // CASO DE PRUEBA 7: Validación de Edad (1-98)
  // ==========================================
  it('should reject age less than 1', async () => {
    // Arrange
    const user = userEvent.setup()
    render(<ActivityRegistrationForm />)

    await waitFor(() => {
      expect(screen.getByText('Tirolesa')).toBeInTheDocument()
    })

    // Navigate to participants
    const selectActivityButton = screen.getAllByRole('button', { name: /seleccionar/i })[0]
    await user.click(selectActivityButton)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    const dateInput = screen.getByLabelText(/fecha/i)
    await user.type(dateInput, '2025-10-25')

    await waitFor(() => {
      expect(screen.getByText('10:00 - 12:00')).toBeInTheDocument()
    })

    const selectTimeButton = screen.getByRole('button', { name: /seleccionar horario/i })
    await user.click(selectTimeButton)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    // Act
    await waitFor(() => {
      expect(screen.getByLabelText(/nombre completo/i)).toBeInTheDocument()
    })

    await user.type(screen.getByLabelText(/nombre completo/i), 'Juan Pérez')
    await user.type(screen.getByLabelText(/dni/i), '12345678')
    await user.type(screen.getByLabelText(/edad/i), '0')
    await user.type(screen.getByLabelText(/email de contacto/i), 'test@example.com')

    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    // Assert
    await waitFor(() => {
      expect(screen.getByText(/la edad debe ser mayor a 0 y menor a 99/i)).toBeInTheDocument()
    })
  })

  it('should reject age greater than or equal to 99', async () => {
    // Arrange
    const user = userEvent.setup()
    render(<ActivityRegistrationForm />)

    await waitFor(() => {
      expect(screen.getByText('Tirolesa')).toBeInTheDocument()
    })

    // Navigate to participants
    const selectActivityButton = screen.getAllByRole('button', { name: /seleccionar/i })[0]
    await user.click(selectActivityButton)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    const dateInput = screen.getByLabelText(/fecha/i)
    await user.type(dateInput, '2025-10-25')

    await waitFor(() => {
      expect(screen.getByText('10:00 - 12:00')).toBeInTheDocument()
    })

    const selectTimeButton = screen.getByRole('button', { name: /seleccionar horario/i })
    await user.click(selectTimeButton)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    // Act
    await waitFor(() => {
      expect(screen.getByLabelText(/nombre completo/i)).toBeInTheDocument()
    })

    await user.type(screen.getByLabelText(/nombre completo/i), 'Juan Pérez')
    await user.type(screen.getByLabelText(/dni/i), '12345678')
    await user.type(screen.getByLabelText(/edad/i), '99')
    await user.type(screen.getByLabelText(/email de contacto/i), 'test@example.com')

    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    // Assert
    await waitFor(() => {
      expect(screen.getByText(/la edad debe ser mayor a 0 y menor a 99/i)).toBeInTheDocument()
    })
  })

  // ==========================================
  // CASO DE PRUEBA 8: Validación de Email
  // ==========================================
  it('should reject invalid email format', async () => {
    // Arrange
    const user = userEvent.setup()
    render(<ActivityRegistrationForm />)

    await waitFor(() => {
      expect(screen.getByText('Tirolesa')).toBeInTheDocument()
    })

    // Navigate to participants
    const selectActivityButton = screen.getAllByRole('button', { name: /seleccionar/i })[0]
    await user.click(selectActivityButton)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    const dateInput = screen.getByLabelText(/fecha/i)
    await user.type(dateInput, '2025-10-25')

    await waitFor(() => {
      expect(screen.getByText('10:00 - 12:00')).toBeInTheDocument()
    })

    const selectTimeButton = screen.getByRole('button', { name: /seleccionar horario/i })
    await user.click(selectTimeButton)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    // Act
    await waitFor(() => {
      expect(screen.getByLabelText(/nombre completo/i)).toBeInTheDocument()
    })

    await user.type(screen.getByLabelText(/nombre completo/i), 'Juan Pérez')
    await user.type(screen.getByLabelText(/dni/i), '12345678')
    await user.type(screen.getByLabelText(/edad/i), '25')
    await user.type(screen.getByLabelText(/email de contacto/i), 'invalid-email')

    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    // Assert
    await waitFor(() => {
      expect(screen.getByText(/el email de contacto no tiene un formato válido/i)).toBeInTheDocument()
    })
  })

  // ==========================================
  // CASO DE PRUEBA 3: No ingresar Talla de ropa
  // ==========================================
  it('should require clothing size when activity requires it', async () => {
    // Arrange
    const user = userEvent.setup()
    render(<ActivityRegistrationForm />)

    await waitFor(() => {
      expect(screen.getByText('Tirolesa')).toBeInTheDocument()
    })

    // Navigate to participants (Tirolesa requires clothing)
    const selectActivityButton = screen.getAllByRole('button', { name: /seleccionar/i })[0]
    await user.click(selectActivityButton)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    const dateInput = screen.getByLabelText(/fecha/i)
    await user.type(dateInput, '2025-10-25')

    await waitFor(() => {
      expect(screen.getByText('10:00 - 12:00')).toBeInTheDocument()
    })

    const selectTimeButton = screen.getByRole('button', { name: /seleccionar horario/i })
    await user.click(selectTimeButton)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    // Act - Fill all fields except clothing size
    await waitFor(() => {
      expect(screen.getByLabelText(/nombre completo/i)).toBeInTheDocument()
    })

    await user.type(screen.getByLabelText(/nombre completo/i), 'Juan Pérez')
    await user.type(screen.getByLabelText(/dni/i), '12345678')
    await user.type(screen.getByLabelText(/edad/i), '25')
    await user.type(screen.getByLabelText(/email de contacto/i), 'test@example.com')
    // Don't select clothing size

    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    // Assert
    await waitFor(() => {
      expect(screen.getByText(/la talla de vestimenta es requerida/i)).toBeInTheDocument()
    })
  })

  // ==========================================
  // CASO DE PRUEBA 2: No aceptar Términos y Condiciones
  // ==========================================
  it('should not allow submission without accepting terms', async () => {
    // Arrange
    const user = userEvent.setup()
    render(<ActivityRegistrationForm />)

    await waitFor(() => {
      expect(screen.getByText('Tirolesa')).toBeInTheDocument()
    })

    // Navigate to terms step
    const selectActivityButton = screen.getAllByRole('button', { name: /seleccionar/i })[0]
    await user.click(selectActivityButton)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    const dateInput = screen.getByLabelText(/fecha/i)
    await user.type(dateInput, '2025-10-25')

    await waitFor(() => {
      expect(screen.getByText('10:00 - 12:00')).toBeInTheDocument()
    })

    const selectTimeButton = screen.getByRole('button', { name: /seleccionar horario/i })
    await user.click(selectTimeButton)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    await waitFor(() => {
      expect(screen.getByLabelText(/nombre completo/i)).toBeInTheDocument()
    })

    await user.type(screen.getByLabelText(/nombre completo/i), 'Juan Pérez')
    await user.type(screen.getByLabelText(/dni/i), '12345678')
    await user.type(screen.getByLabelText(/edad/i), '25')
    await user.type(screen.getByLabelText(/email de contacto/i), 'test@example.com')

    const tallaSelect = screen.getByLabelText(/talla de vestimenta/i)
    await user.selectOptions(tallaSelect, 'M')

    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    // Act - Try to submit without accepting terms
    await waitFor(() => {
      expect(screen.getByRole('checkbox')).toBeInTheDocument()
    })

    const confirmButton = screen.getByRole('button', { name: /confirmar inscripción/i })
    await user.click(confirmButton)

    // Assert
    await waitFor(() => {
      expect(screen.getByText(/debe aceptar los términos y condiciones/i)).toBeInTheDocument()
    })
    // Should not call API
    expect(api.registerForActivity).not.toHaveBeenCalled()
  })

  // ==========================================
  // CASO DE PRUEBA 11: Verificación de Cupos Disponibles
  // ==========================================
  it('should prevent registering more participants than available spots', async () => {
    // Arrange
    const user = userEvent.setup()
    ;(global.fetch as jest.Mock).mockImplementation((url: string) => {
      if (url.includes('/actividades') && !url.includes('/horarios')) {
        return Promise.resolve({
          ok: true,
          json: async () => [
            {
              id: 1,
              nombre: 'Tirolesa',
              descripcion: 'Actividad de aventura',
              requiereVestimenta: true,
              cupoMaximo: 10,
              terminosCondiciones: 'Términos de tirolesa',
              edadMinima: 12,
            },
          ],
        })
      }
      if (url.includes('/horarios')) {
        return Promise.resolve({
          ok: true,
          json: async () => [
            {
              id: 1,
              fecha: '2025-10-25',
              horaInicio: '10:00',
              horaFin: '12:00',
              cuposDisponibles: 2, // Only 2 spots available
              cupoMaximo: 10,
              nombreActividad: 'Tirolesa',
            },
          ],
        })
      }
      return Promise.reject(new Error('Unknown URL'))
    })

    render(<ActivityRegistrationForm />)

    await waitFor(() => {
      expect(screen.getByText('Tirolesa')).toBeInTheDocument()
    })

    // Navigate to participants
    const selectActivityButton = screen.getByRole('button', { name: /seleccionar/i })
    await user.click(selectActivityButton)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    const dateInput = screen.getByLabelText(/fecha/i)
    await user.type(dateInput, '2025-10-25')

    await waitFor(() => {
      expect(screen.getByText('10:00 - 12:00')).toBeInTheDocument()
    })

    const selectTimeButton = screen.getByRole('button', { name: /seleccionar horario/i })
    await user.click(selectTimeButton)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    // Act - Try to select 3 participants (more than available)
    await waitFor(() => {
      expect(screen.getByLabelText(/número de participantes/i)).toBeInTheDocument()
    })

    // The select should only show up to 2 participants as options
    const participantSelect = screen.getByLabelText(/número de participantes/i)
    const options = within(participantSelect).getAllByRole('option')

    // Assert
    expect(options).toHaveLength(2) // Only 1 and 2 should be available
  })

  // ==========================================
  // CASO DE PRUEBA 9: API Error Handling
  // ==========================================
  it('should display error message when registration fails', async () => {
    // Arrange
    const user = userEvent.setup()
    ;(api.registerForActivity as jest.Mock).mockResolvedValue({
      success: false,
      message: 'Ya existe una inscripción con ese DNI',
    })

    render(<ActivityRegistrationForm />)

    await waitFor(() => {
      expect(screen.getByText('Tirolesa')).toBeInTheDocument()
    })

    // Complete full flow
    const selectActivityButton = screen.getAllByRole('button', { name: /seleccionar/i })[0]
    await user.click(selectActivityButton)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    const dateInput = screen.getByLabelText(/fecha/i)
    await user.type(dateInput, '2025-10-25')

    await waitFor(() => {
      expect(screen.getByText('10:00 - 12:00')).toBeInTheDocument()
    })

    const selectTimeButton = screen.getByRole('button', { name: /seleccionar horario/i })
    await user.click(selectTimeButton)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    await waitFor(() => {
      expect(screen.getByLabelText(/nombre completo/i)).toBeInTheDocument()
    })

    await user.type(screen.getByLabelText(/nombre completo/i), 'Juan Pérez')
    await user.type(screen.getByLabelText(/dni/i), '12345678')
    await user.type(screen.getByLabelText(/edad/i), '25')
    await user.type(screen.getByLabelText(/email de contacto/i), 'test@example.com')

    const tallaSelect = screen.getByLabelText(/talla de vestimenta/i)
    await user.selectOptions(tallaSelect, 'M')

    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    await waitFor(() => {
      expect(screen.getByRole('checkbox')).toBeInTheDocument()
    })

    const termsCheckbox = screen.getByRole('checkbox')
    await user.click(termsCheckbox)

    // Act
    const confirmButton = screen.getByRole('button', { name: /confirmar inscripción/i })
    await user.click(confirmButton)

    // Assert
    await waitFor(() => {
      expect(screen.getByText(/ya existe una inscripción con ese dni/i)).toBeInTheDocument()
    })
  })

  it('should handle network errors gracefully', async () => {
    // Arrange
    const user = userEvent.setup()
    ;(api.registerForActivity as jest.Mock).mockRejectedValue(new Error('Network error'))

    render(<ActivityRegistrationForm />)

    await waitFor(() => {
      expect(screen.getByText('Tirolesa')).toBeInTheDocument()
    })

    // Complete full flow
    const selectActivityButton = screen.getAllByRole('button', { name: /seleccionar/i })[0]
    await user.click(selectActivityButton)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    const dateInput = screen.getByLabelText(/fecha/i)
    await user.type(dateInput, '2025-10-25')

    await waitFor(() => {
      expect(screen.getByText('10:00 - 12:00')).toBeInTheDocument()
    })

    const selectTimeButton = screen.getByRole('button', { name: /seleccionar horario/i })
    await user.click(selectTimeButton)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    await waitFor(() => {
      expect(screen.getByLabelText(/nombre completo/i)).toBeInTheDocument()
    })

    await user.type(screen.getByLabelText(/nombre completo/i), 'Juan Pérez')
    await user.type(screen.getByLabelText(/dni/i), '12345678')
    await user.type(screen.getByLabelText(/edad/i), '25')
    await user.type(screen.getByLabelText(/email de contacto/i), 'test@example.com')

    const tallaSelect = screen.getByLabelText(/talla de vestimenta/i)
    await user.selectOptions(tallaSelect, 'M')

    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    await waitFor(() => {
      expect(screen.getByRole('checkbox')).toBeInTheDocument()
    })

    const termsCheckbox = screen.getByRole('checkbox')
    await user.click(termsCheckbox)

    // Act
    const confirmButton = screen.getByRole('button', { name: /confirmar inscripción/i })
    await user.click(confirmButton)

    // Assert
    await waitFor(() => {
      expect(screen.getByText(/error de conexión/i)).toBeInTheDocument()
    })
  })

  it('should reset form after successful registration', async () => {
    // Arrange
    const user = userEvent.setup()
    ;(api.registerForActivity as jest.Mock).mockResolvedValue({
      success: true,
      message: 'Inscripción exitosa',
    })

    render(<ActivityRegistrationForm />)

    await waitFor(() => {
      expect(screen.getByText('Tirolesa')).toBeInTheDocument()
    })

    // Complete full flow
    const selectActivityButton = screen.getAllByRole('button', { name: /seleccionar/i })[0]
    await user.click(selectActivityButton)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    const dateInput = screen.getByLabelText(/fecha/i)
    await user.type(dateInput, '2025-10-25')

    await waitFor(() => {
      expect(screen.getByText('10:00 - 12:00')).toBeInTheDocument()
    })

    const selectTimeButton = screen.getByRole('button', { name: /seleccionar horario/i })
    await user.click(selectTimeButton)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    await waitFor(() => {
      expect(screen.getByLabelText(/nombre completo/i)).toBeInTheDocument()
    })

    await user.type(screen.getByLabelText(/nombre completo/i), 'Juan Pérez')
    await user.type(screen.getByLabelText(/dni/i), '12345678')
    await user.type(screen.getByLabelText(/edad/i), '25')
    await user.type(screen.getByLabelText(/email de contacto/i), 'test@example.com')

    const tallaSelect = screen.getByLabelText(/talla de vestimenta/i)
    await user.selectOptions(tallaSelect, 'M')

    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    await waitFor(() => {
      expect(screen.getByRole('checkbox')).toBeInTheDocument()
    })

    const termsCheckbox = screen.getByRole('checkbox')
    await user.click(termsCheckbox)

    const confirmButton = screen.getByRole('button', { name: /confirmar inscripción/i })
    await user.click(confirmButton)

    await waitFor(() => {
      expect(screen.getByText(/inscripción exitosa/i)).toBeInTheDocument()
    })

    // Act - Click reset button
    const resetButton = screen.getByRole('button', { name: /realizar otra inscripción/i })
    await user.click(resetButton)

    // Assert - Should be back at activity selection
    await waitFor(() => {
      expect(screen.getByText(/seleccione una actividad/i)).toBeInTheDocument()
    })
  })

  it('should prevent selecting past dates', async () => {
    // Arrange
    const user = userEvent.setup()
    render(<ActivityRegistrationForm />)

    await waitFor(() => {
      expect(screen.getByText('Tirolesa')).toBeInTheDocument()
    })

    const selectActivityButton = screen.getAllByRole('button', { name: /seleccionar/i })[0]
    await user.click(selectActivityButton)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    // Act - Try to select a past date
    const dateInput = screen.getByLabelText(/fecha/i)
    await user.type(dateInput, '2020-01-01')

    // Assert
    await waitFor(() => {
      expect(screen.getByText(/no puede seleccionar una fecha anterior a hoy/i)).toBeInTheDocument()
    })
  })
})
