import React from 'react'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import '@testing-library/jest-dom'
import {
  Header,
  ErrorBox,
  Spinner,
  ActivityCard,
  TimeSlotCard,
  ParticipantsForm,
  TermsSection,
  ConfirmationModal,
} from '../../components/ActivityComponents'
import { mockActivity, mockTimeSlot, mockParticipant } from '../utils/testHelpers'
import type { ActivityFromApi, TimeSlot, Participant } from '../../lib/types'

// ==========================================
// CASO DE PRUEBA 4: ActivityCard - Renderizado
// ==========================================
describe('ActivityCard', () => {
  it('should render activity name, description, capacity, and edad minima', () => {
    // Arrange
    const activity = mockActivity({
      name: 'Tirolesa',
      description: 'Actividad de aventura en tirolesa',
      capacity: 10,
    })
    const onSelect = jest.fn()

    // Act
    render(<ActivityCard activity={activity} onSelect={onSelect} selected={false} />)

    // Assert
    expect(screen.getByText('Tirolesa')).toBeInTheDocument()
    expect(screen.getByText('Actividad de aventura en tirolesa')).toBeInTheDocument()
    expect(screen.getByText('10')).toBeInTheDocument()
    expect(screen.getByText(/personas/i)).toBeInTheDocument()
  })

  it('should show "Requiere talla" badge when activity requires clothing', () => {
    // Arrange
    const activity = mockActivity({ requiereVestimenta: true })
    const onSelect = jest.fn()

    // Act
    render(<ActivityCard activity={activity} onSelect={onSelect} selected={false} />)

    // Assert
    expect(screen.getByText('Requiere talla')).toBeInTheDocument()
  })

  it('should show "Sin talla" badge when activity does not require clothing', () => {
    // Arrange
    const activity = mockActivity({ requiereVestimenta: false })
    const onSelect = jest.fn()

    // Act
    render(<ActivityCard activity={activity} onSelect={onSelect} selected={false} />)

    // Assert
    expect(screen.getByText('Sin talla')).toBeInTheDocument()
  })

  it('should call onSelect with activity id when select button is clicked', async () => {
    // Arrange
    const activity = mockActivity({ id: 42 })
    const onSelect = jest.fn()
    const user = userEvent.setup()

    // Act
    render(<ActivityCard activity={activity} onSelect={onSelect} selected={false} />)
    const button = screen.getByRole('button', { name: /seleccionar/i })
    await user.click(button)

    // Assert
    expect(onSelect).toHaveBeenCalledWith(42)
    expect(onSelect).toHaveBeenCalledTimes(1)
  })

  it('should show "Seleccionada" when activity is selected', () => {
    // Arrange
    const activity = mockActivity()
    const onSelect = jest.fn()

    // Act
    render(<ActivityCard activity={activity} onSelect={onSelect} selected={true} />)

    // Assert
    expect(screen.getByRole('button', { pressed: true })).toHaveTextContent('Seleccionada')
  })

  it('should show different styling when selected', () => {
    // Arrange
    const activity = mockActivity()
    const onSelect = jest.fn()

    // Act
    const { container } = render(<ActivityCard activity={activity} onSelect={onSelect} selected={true} />)
    const card = container.querySelector('.activity-card')

    // Assert
    expect(card).toHaveStyle({ borderColor: 'var(--verde-claro)' })
  })

  it('should display zero capacity correctly', () => {
    // Arrange
    const activity = mockActivity({ capacity: 0 })
    const onSelect = jest.fn()

    // Act
    render(<ActivityCard activity={activity} onSelect={onSelect} selected={false} />)

    // Assert
    expect(screen.getByText('0')).toBeInTheDocument()
  })
})

// ==========================================
// CASO DE PRUEBA 5: TimeSlotCard - Renderizado
// ==========================================
describe('TimeSlotCard', () => {
  it('should show time, date, and available spots when available', () => {
    // Arrange
    const slot = mockTimeSlot({
      time: '10:00 - 12:00',
      date: '2025-10-25',
      availableSpots: 5,
      isAvailable: true,
    })
    const onSelect = jest.fn()

    // Act
    render(<TimeSlotCard slot={slot} onSelect={onSelect} selected={false} />)

    // Assert
    expect(screen.getByText('10:00 - 12:00')).toBeInTheDocument()
    expect(screen.getByText('5 cupos')).toBeInTheDocument()
    expect(screen.getByText('disponibles')).toBeInTheDocument()
  })

  it('should show "Completo" badge when no spots available', () => {
    // Arrange
    const slot = mockTimeSlot({
      availableSpots: 0,
      isAvailable: false,
    })
    const onSelect = jest.fn()

    // Act
    render(<TimeSlotCard slot={slot} onSelect={onSelect} selected={false} />)

    // Assert
    expect(screen.getByText('Completo')).toBeInTheDocument()
  })

  it('should disable button when slot is not available', () => {
    // Arrange
    const slot = mockTimeSlot({ isAvailable: false })
    const onSelect = jest.fn()

    // Act
    render(<TimeSlotCard slot={slot} onSelect={onSelect} selected={false} />)

    // Assert
    const button = screen.getByRole('button', { name: /no disponible/i })
    expect(button).toBeDisabled()
  })

  it('should call onSelect with slot id when button clicked', async () => {
    // Arrange
    const slot = mockTimeSlot({ id: '123', isAvailable: true })
    const onSelect = jest.fn()
    const user = userEvent.setup()

    // Act
    render(<TimeSlotCard slot={slot} onSelect={onSelect} selected={false} />)
    const button = screen.getByRole('button', { name: /seleccionar horario/i })
    await user.click(button)

    // Assert
    expect(onSelect).toHaveBeenCalledWith('123')
  })

  it('should show "Seleccionado" when slot is selected', () => {
    // Arrange
    const slot = mockTimeSlot({ isAvailable: true })
    const onSelect = jest.fn()

    // Act
    render(<TimeSlotCard slot={slot} onSelect={onSelect} selected={true} />)

    // Assert
    expect(screen.getByRole('button', { pressed: true })).toHaveTextContent('Seleccionado')
  })
})

// ==========================================
// ParticipantsForm Tests
// ==========================================
describe('ParticipantsForm', () => {
  const defaultProps = {
    participants: [mockParticipant()],
    onChange: jest.fn(),
    onCountChange: jest.fn(),
    selectedTimeSlot: mockTimeSlot({ availableSpots: 5 }),
    selectedActivity: mockActivity({ requiereVestimenta: true }),
    contactEmail: 'test@example.com',
    onContactEmailChange: jest.fn(),
  }

  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('should render participant form fields', () => {
    // Arrange & Act
    render(<ParticipantsForm {...defaultProps} />)

    // Assert
    expect(screen.getByLabelText(/número de participantes/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/email de contacto/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/nombre completo/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/dni/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/edad/i)).toBeInTheDocument()
  })

  it('should show available spots', () => {
    // Arrange & Act
    render(<ParticipantsForm {...defaultProps} />)

    // Assert
    expect(screen.getByText(/cupos disponibles: 5/i)).toBeInTheDocument()
  })

  // ==========================================
  // CASO DE PRUEBA 6: Validación de DNI
  // ==========================================
  it('should accept valid 8-digit DNI', async () => {
    // Arrange
    const onChange = jest.fn()
    const user = userEvent.setup()

    // Act
    render(<ParticipantsForm {...defaultProps} onChange={onChange} />)
    const dniInput = screen.getByLabelText(/dni/i)
    await user.clear(dniInput)
    await user.type(dniInput, '12345678')

    // Assert
    expect(onChange).toHaveBeenCalledWith(0, 'dni', expect.stringContaining('12345678'))
  })

  // ==========================================
  // CASO DE PRUEBA 7: Validación de Edad
  // ==========================================
  it('should accept valid age within range', async () => {
    // Arrange
    const onChange = jest.fn()
    const user = userEvent.setup()

    // Act
    render(<ParticipantsForm {...defaultProps} onChange={onChange} />)
    const ageInput = screen.getByLabelText(/edad/i)
    await user.clear(ageInput)
    await user.type(ageInput, '25')

    // Assert
    expect(onChange).toHaveBeenCalledWith(0, 'age', expect.stringContaining('25'))
  })

  // ==========================================
  // CASO DE PRUEBA 8: Validación de Email
  // ==========================================
  it('should call onContactEmailChange when email input changes', async () => {
    // Arrange
    const onContactEmailChange = jest.fn()
    const user = userEvent.setup()

    // Act
    render(<ParticipantsForm {...defaultProps} onContactEmailChange={onContactEmailChange} />)
    const emailInput = screen.getByLabelText(/email de contacto/i)
    await user.clear(emailInput)
    await user.type(emailInput, 'test@example.com')

    // Assert
    expect(onContactEmailChange).toHaveBeenCalledWith(expect.stringContaining('test@example.com'))
  })

  // ==========================================
  // CASO DE PRUEBA 3: No ingresar Talla de ropa
  // ==========================================
  it('should show clothing size field when activity requires it', () => {
    // Arrange
    const activity = mockActivity({ requiereVestimenta: true })

    // Act
    render(<ParticipantsForm {...defaultProps} selectedActivity={activity} />)

    // Assert
    expect(screen.getByLabelText(/talla de vestimenta/i)).toBeInTheDocument()
  })

  it('should NOT show clothing size field when activity does not require it', () => {
    // Arrange
    const activity = mockActivity({ requiereVestimenta: false })

    // Act
    render(<ParticipantsForm {...defaultProps} selectedActivity={activity} />)

    // Assert
    expect(screen.queryByLabelText(/talla de vestimenta/i)).not.toBeInTheDocument()
  })

  it('should allow selecting clothing size', async () => {
    // Arrange
    const onChange = jest.fn()
    const activity = mockActivity({ requiereVestimenta: true })
    const user = userEvent.setup()

    // Act
    render(<ParticipantsForm {...defaultProps} selectedActivity={activity} onChange={onChange} />)
    const sizeSelect = screen.getByLabelText(/talla de vestimenta/i)
    await user.selectOptions(sizeSelect, 'L')

    // Assert
    expect(onChange).toHaveBeenCalledWith(0, 'clothingSize', 'L')
  })

  it('should call onCountChange when participant count changes', async () => {
    // Arrange
    const onCountChange = jest.fn()
    const user = userEvent.setup()

    // Act
    render(<ParticipantsForm {...defaultProps} onCountChange={onCountChange} />)
    const countSelect = screen.getByLabelText(/número de participantes/i)
    await user.selectOptions(countSelect, '3')

    // Assert
    expect(onCountChange).toHaveBeenCalledWith(3)
  })

  it('should render multiple participant forms based on count', () => {
    // Arrange
    const participants = [
      mockParticipant({ name: 'Juan' }),
      mockParticipant({ name: 'María' }),
      mockParticipant({ name: 'Pedro' }),
    ]

    // Act
    render(<ParticipantsForm {...defaultProps} participants={participants} />)

    // Assert
    expect(screen.getByText('Participante 1')).toBeInTheDocument()
    expect(screen.getByText('Participante 2')).toBeInTheDocument()
    expect(screen.getByText('Participante 3')).toBeInTheDocument()
  })

  it('should highlight empty email field with error styling', () => {
    // Arrange & Act
    render(<ParticipantsForm {...defaultProps} contactEmail="" />)
    const emailInput = screen.getByLabelText(/email de contacto/i)

    // Assert
    expect(emailInput).toHaveClass('form-input-error')
  })

  it('should highlight empty participant name with error styling', () => {
    // Arrange
    const participants = [mockParticipant({ name: '' })]

    // Act
    render(<ParticipantsForm {...defaultProps} participants={participants} />)
    const nameInput = screen.getByLabelText(/nombre completo/i)

    // Assert
    expect(nameInput).toHaveClass('form-input-error')
  })
})

// ==========================================
// CASO DE PRUEBA 2: No aceptar Términos y Condiciones
// ==========================================
describe('TermsSection', () => {
  it('should render terms and conditions text', () => {
    // Arrange
    const activity = mockActivity({
      terminosCondiciones: 'Estos son los términos y condiciones',
    })
    const onToggle = jest.fn()

    // Act
    render(<TermsSection selectedActivity={activity} termsAccepted={false} onToggle={onToggle} />)

    // Assert
    expect(screen.getByText('Estos son los términos y condiciones')).toBeInTheDocument()
  })

  it('should render unchecked checkbox by default', () => {
    // Arrange
    const activity = mockActivity()
    const onToggle = jest.fn()

    // Act
    render(<TermsSection selectedActivity={activity} termsAccepted={false} onToggle={onToggle} />)
    const checkbox = screen.getByRole('checkbox')

    // Assert
    expect(checkbox).not.toBeChecked()
  })

  it('should call onToggle when checkbox is clicked', async () => {
    // Arrange
    const activity = mockActivity()
    const onToggle = jest.fn()
    const user = userEvent.setup()

    // Act
    render(<TermsSection selectedActivity={activity} termsAccepted={false} onToggle={onToggle} />)
    const checkbox = screen.getByRole('checkbox')
    await user.click(checkbox)

    // Assert
    expect(onToggle).toHaveBeenCalledWith(true)
  })

  it('should show checkbox as checked when termsAccepted is true', () => {
    // Arrange
    const activity = mockActivity()
    const onToggle = jest.fn()

    // Act
    render(<TermsSection selectedActivity={activity} termsAccepted={true} onToggle={onToggle} />)
    const checkbox = screen.getByRole('checkbox')

    // Assert
    expect(checkbox).toBeChecked()
  })
})

// ==========================================
// ConfirmationModal Tests
// ==========================================
describe('ConfirmationModal', () => {
  it('should not render when open is false', () => {
    // Arrange & Act
    render(
      <ConfirmationModal
        open={false}
        onClose={jest.fn()}
        selectedActivity={mockActivity()}
        selectedTimeSlot={mockTimeSlot()}
        selectedDate="2025-10-25"
        participants={[mockParticipant()]}
      />
    )

    // Assert
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('should render when open is true', () => {
    // Arrange & Act
    render(
      <ConfirmationModal
        open={true}
        onClose={jest.fn()}
        selectedActivity={mockActivity({ name: 'Tirolesa' })}
        selectedTimeSlot={mockTimeSlot({ time: '10:00 - 12:00' })}
        selectedDate="2025-10-25"
        participants={[mockParticipant({ name: 'Juan Pérez', dni: '12345678' })]}
      />
    )

    // Assert
    expect(screen.getByRole('dialog')).toBeInTheDocument()
    expect(screen.getByText(/inscripción exitosa/i)).toBeInTheDocument()
  })

  it('should display activity details', () => {
    // Arrange & Act
    render(
      <ConfirmationModal
        open={true}
        onClose={jest.fn()}
        selectedActivity={mockActivity({ name: 'Tirolesa' })}
        selectedTimeSlot={mockTimeSlot({ time: '14:00 - 16:00' })}
        selectedDate="2025-10-25"
        participants={[mockParticipant()]}
      />
    )

    // Assert
    expect(screen.getByText('Tirolesa')).toBeInTheDocument()
    expect(screen.getByText('14:00 - 16:00')).toBeInTheDocument()
  })

  it('should display participant information', () => {
    // Arrange
    const participants = [
      mockParticipant({ name: 'Juan Pérez', dni: '12345678' }),
      mockParticipant({ name: 'María García', dni: '87654321' }),
    ]

    // Act
    render(
      <ConfirmationModal
        open={true}
        onClose={jest.fn()}
        selectedActivity={mockActivity()}
        selectedTimeSlot={mockTimeSlot()}
        selectedDate="2025-10-25"
        participants={participants}
      />
    )

    // Assert
    expect(screen.getByText(/juan pérez/i)).toBeInTheDocument()
    expect(screen.getByText(/12345678/)).toBeInTheDocument()
    expect(screen.getByText(/maría garcía/i)).toBeInTheDocument()
    expect(screen.getByText(/87654321/)).toBeInTheDocument()
  })

  it('should call onClose when button is clicked', async () => {
    // Arrange
    const onClose = jest.fn()
    const user = userEvent.setup()

    // Act
    render(
      <ConfirmationModal
        open={true}
        onClose={onClose}
        selectedActivity={mockActivity()}
        selectedTimeSlot={mockTimeSlot()}
        selectedDate="2025-10-25"
        participants={[mockParticipant()]}
      />
    )
    const button = screen.getByRole('button', { name: /realizar otra inscripción/i })
    await user.click(button)

    // Assert
    expect(onClose).toHaveBeenCalledTimes(1)
  })
})

// ==========================================
// ErrorBox Tests
// ==========================================
describe('ErrorBox', () => {
  it('should not render when errors array is empty', () => {
    // Arrange & Act
    render(<ErrorBox errors={[]} />)

    // Assert
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })

  it('should render error messages', () => {
    // Arrange
    const errors = ['Error 1', 'Error 2', 'Error 3']

    // Act
    render(<ErrorBox errors={errors} />)

    // Assert
    expect(screen.getByRole('alert')).toBeInTheDocument()
    expect(screen.getByText('Error 1')).toBeInTheDocument()
    expect(screen.getByText('Error 2')).toBeInTheDocument()
    expect(screen.getByText('Error 3')).toBeInTheDocument()
  })

  it('should render multiple error items in a list', () => {
    // Arrange
    const errors = ['First error', 'Second error']

    // Act
    const { container } = render(<ErrorBox errors={errors} />)
    const listItems = container.querySelectorAll('li')

    // Assert
    expect(listItems).toHaveLength(2)
  })
})

// ==========================================
// Header Tests
// ==========================================
describe('Header', () => {
  it('should render header title and subtitle', () => {
    // Arrange & Act
    render(<Header />)

    // Assert
    expect(screen.getByText(/inscripción a actividades/i)).toBeInTheDocument()
    expect(screen.getByText(/complete el formulario/i)).toBeInTheDocument()
  })
})

// ==========================================
// Spinner Tests
// ==========================================
describe('Spinner', () => {
  it('should render spinner element', () => {
    // Arrange & Act
    const { container } = render(<Spinner />)
    const spinner = container.querySelector('.spinner')

    // Assert
    expect(spinner).toBeInTheDocument()
  })
})
