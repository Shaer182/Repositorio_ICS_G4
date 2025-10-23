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


export type Step = "activity" | "timeslot" | "participants" | "terms" | "confirmation"


export type TimeSlot = {
id: string
time: string
date: string
availableSpots: number
isAvailable: boolean
raw?: HorarioResponse
}


export type ActivityFromApi = {
id: number
name: string
description: string
requiereVestimenta: boolean
capacity: number
terminosCondiciones?: string
horarios: HorarioResponse[]
}


export type Participant = {
name: string
dni: string
age: string
clothingSize?: string
email: string;
}


export const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080"
export const clothingSizes = ["XS", "S", "M", "L", "XL", "XXL"]

export function formatDateLong(iso?: string) {
if (!iso) return ""
try {
const d = new Date(iso)
return d.toLocaleDateString("es-ES", { weekday: "long", year: "numeric", month: "long", day: "numeric" })
} catch {
return iso
}
}

export const minDateForInput = () => {
  const t = new Date()
  t.setHours(0, 0, 0, 0)
  return t.toISOString().split("T")[0]
}