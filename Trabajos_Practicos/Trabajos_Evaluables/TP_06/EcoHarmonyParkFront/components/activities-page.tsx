"use client"

import { useEffect, useState } from "react"
import type { Activity } from "@/lib/types"
import { getActivities } from "@/lib/api"
import { ActivityCard } from "./activity-card"
import { RegistrationModal } from "./registration-modal"

export function ActivitiesPage() {
  const [activities, setActivities] = useState<Activity[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedActivity, setSelectedActivity] = useState<Activity | null>(null)

  useEffect(() => {
    loadActivities()
  }, [])

  async function loadActivities() {
    try {
      setLoading(true)
      setError(null)
      const data = await getActivities()
      setActivities(data)
    } catch (err) {
      console.error("Error loading activities:", err)
      setError("No se pudieron cargar las actividades. Por favor, intente nuevamente.")
    } finally {
      setLoading(false)
    }
  }

  function handleRegistrationSuccess(activityId: number) {
    setActivities((prev) =>
      prev.map((activity) =>
        activity.id === activityId ? { ...activity, availableSlots: activity.availableSlots - 1 } : activity,
      ),
    )
    setSelectedActivity(null)
  }

  return (
    <div style={{ minHeight: '100vh' }}>
      {/* Header */}
      <header className="header">
        <div className="header-container">
          <h1 className="header-title">EcoHarmony Park</h1>
          <p className="header-subtitle">Inscripción a Actividades</p>
        </div>
      </header>

      {/* Main Content */}
      <main className="main-content">
        {loading ? (
          <div className="loading-container">
            <div className="spinner"></div>
          </div>
        ) : error ? (
          <div className="error-box">
            <p>{error}</p>
          </div>
        ) : (
          <>
            <div className="section-header">
              <h2 className="section-title">Actividades Disponibles</h2>
              <p className="section-description">Selecciona una actividad para inscribirte</p>
            </div>

            <div className="activities-grid">
              {activities.map((activity) => (
                <ActivityCard key={activity.id} activity={activity} onRegister={() => setSelectedActivity(activity)} />
              ))}
            </div>
          </>
        )}
      </main>

      {/* Registration Modal */}
      {selectedActivity && (
        <RegistrationModal
          activity={selectedActivity}
          onClose={() => setSelectedActivity(null)}
          onSuccess={() => handleRegistrationSuccess(selectedActivity.id)}
        />
      )}
    </div>
  )
}
