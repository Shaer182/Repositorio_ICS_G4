import type { Activity, ActivityRegistration, ApiResponse, ActividadResponse, HorarioResponse } from "./types"

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080"

export async function getActivities(): Promise<Activity[]> {
  try {
    // Obtener todas las actividades
    const actividadesResponse = await fetch(`${API_BASE_URL}/actividades`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    })

    if (!actividadesResponse.ok) {
      throw new Error("Error al cargar las actividades")
    }

    const actividades: ActividadResponse[] = await actividadesResponse.json()

    // Para cada actividad, obtener sus horarios de hoy
    //const today = new Date().toISOString().split('T')[0] // formato YYYY-MM-DD
    const today = "2025-10-22" // Fecha fija para pruebas

    const actividadesConHorarios = await Promise.all(
      actividades.map(async (actividad) => {
        try {
          const horariosResponse = await fetch(
            `${API_BASE_URL}/actividades/${actividad.id}/horarios?fecha=${today}`,
            {
              method: "GET",
              headers: {
                "Content-Type": "application/json",
              },
            }
          )

          let horarios: HorarioResponse[] = []
          if (horariosResponse.ok) {
            horarios = await horariosResponse.json()
          }

          // Calcular cupos disponibles totales para hoy
          const totalCuposDisponibles = horarios.reduce((sum, h) => sum + h.cuposDisponibles, 0)

          // Transformar al formato esperado por el frontend
          const activity: Activity = {
            id: actividad.id,
            name: actividad.nombre as any,
            description: actividad.descripcion,
            capacity: actividad.cupoMaximo as any,
            availableSlots: totalCuposDisponibles,
            isAvailableToday: horarios.length > 0 && totalCuposDisponibles > 0,
            horarios: horarios, // Guardar los horarios para usarlos en la inscripción
            requiereVestimenta: actividad.requiereVestimenta
          }

          return activity
        } catch (error) {
          console.error(`Error obteniendo horarios para actividad ${actividad.id}:`, error)
          return {
            id: actividad.id,
            name: actividad.nombre as any,
            description: actividad.descripcion,
            capacity: actividad.cupoMaximo as any,
            availableSlots: 0,
            isAvailableToday: false,
            horarios: [],
            requiereVestimenta: actividad.requiereVestimenta
          }
        }
      })
    )

    return actividadesConHorarios
  } catch (error) {
    console.error("[v0] Error fetching activities:", error)
    throw error
  }
}

export async function registerForActivity(registration: ActivityRegistration): Promise<ApiResponse<void>> {
  try {
    // Transformar al formato esperado por el backend
    const inscripcionRequest = {
      visitantes: [
        {
          nombre: registration.visitorName,
          dni: registration.visitorDni,
          edad: registration.visitorAge,
          tallaVestimenta: registration.clothingSize || null
        }
      ],
      horarioActividadId: registration.horarioActividadId,
      cantidadPersonas: 1
    }

    const response = await fetch(`${API_BASE_URL}/actividades/inscripciones`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(inscripcionRequest),
    })

    if (!response.ok) {
      const errorText = await response.text()
      let errorMessage = "Error al procesar la inscripción"

      try {
        const errorData = JSON.parse(errorText)
        errorMessage = errorData.message || errorMessage
      } catch {
        errorMessage = errorText || errorMessage
      }

      return {
        success: false,
        message: errorMessage,
      }
    }

    const data = await response.json()

    return {
      success: true,
      data: undefined,
      message: "Inscripción exitosa",
    }
  } catch (error) {
    console.error("[v0] Error registering for activity:", error)
    return {
      success: false,
      message: "Error de conexión. Por favor, intente nuevamente.",
    }
  }
}
