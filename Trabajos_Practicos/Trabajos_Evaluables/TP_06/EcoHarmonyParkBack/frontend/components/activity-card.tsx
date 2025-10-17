"use client"

import type { Activity } from "@/lib/types"
import { Users, CheckCircle2, XCircle } from "lucide-react"

interface ActivityCardProps {
  activity: Activity
  onRegister: () => void
}

export function ActivityCard({ activity, onRegister }: ActivityCardProps) {
  const isDisabled = !activity.isAvailableToday || activity.availableSlots === 0

  return (
    <div className="activity-card">
      <div className="activity-card-header">
        <h3 className="activity-title">{activity.name}</h3>
        {activity.isAvailableToday ? (
          <span className="badge badge-success">
            <CheckCircle2 className="badge-icon" />
            Disponible hoy
          </span>
        ) : (
          <span className="badge badge-muted">
            <XCircle className="badge-icon" />
            No disponible
          </span>
        )}
      </div>
      <p className="activity-description">{activity.description}</p>
      <div className="cupos-container">
        <Users className="cupos-icon" />
        <span className="cupos-text">
          Cupos disponibles:{" "}
          <span className={activity.availableSlots === 0 ? "cupos-number-zero" : "cupos-number"}>
            {activity.availableSlots}
          </span>
          <span className="cupos-total"> / {activity.capacity}</span>
        </span>
      </div>
      <button
        onClick={onRegister}
        disabled={isDisabled}
        className="btn btn-primary"
      >
        {activity.availableSlots === 0 ? "Sin cupos" : "Inscribirme"}
      </button>
    </div>
  )
}
