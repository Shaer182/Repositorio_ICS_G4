// Tipos del backend
export interface ActividadResponse {
  id: number
  nombre: string
  requiereVestimenta: boolean
  cupoMaximo: number
  descripcion: string
  terminosCondiciones: string
  edadMinima: number
}

export interface HorarioResponse {
  id: number
  fecha: string
  horaInicio: string
  horaFin: string
  cuposDisponibles: number
  cupoMaximo: number
  nombreActividad: string
}

// Tipos del frontend
export interface Activity {
  id: number
  name: "Tirolesa" | "Safari" | "Palestra" | "Jardinería" | string
  description: string
  capacity: number
  availableSlots: number
  isAvailableToday: boolean
  horarios: HorarioResponse[]
  requiereVestimenta: boolean
}

export interface ActivityRegistration {
  activityId: number
  visitorName: string
  visitorDni: string
  visitorAge: number
  clothingSize?: "S" | "M" | "L" | "XL"
  horarioActividadId: number
}

export interface ApiResponse<T> {
  success: boolean
  data?: T
  message?: string
}
  