"use client"

import React from "react"
import { ActivityFromApi, TimeSlot, Participant, clothingSizes, formatDateLong } from "../lib/types"
import '../app/globals.css'
// ---------------------------
// Header
// ---------------------------
export function Header() {
  return (
    <header className="header">
      <div className="header-container">
        <h1 className="header-title">Inscripción a Actividades</h1>
        <p className="header-subtitle">Complete el formulario para reservar su lugar en una actividad</p>
      </div>
    </header>
  )
}

// ---------------------------
// ErrorBox
// ---------------------------
export function ErrorBox({ errors }: { errors: string[] }) {
  if (!errors || errors.length === 0) return null
  return (
    <div className="error-box" role="alert">
      <ul>
        {errors.map((e, i) => (
          <li key={i}>{e}</li>
        ))}
      </ul>
    </div>
  )
}

// ---------------------------
// Spinner
// ---------------------------
export function Spinner() {
  return (
    <div className="loading-container">
      <div className="spinner" aria-hidden />
    </div>
  )
}

// ---------------------------
// ActivityCard
// ---------------------------
export function ActivityCard({
  activity,
  onSelect,
  selected,
}: {
  activity: ActivityFromApi
  onSelect: (id: number) => void
  selected: boolean
}) {
  const selectedStyle: React.CSSProperties | undefined = selected
    ? { borderColor: "var(--verde-claro)", boxShadow: "0 8px 12px rgba(0,0,0,0.12)" }
    : undefined

  return (
    <div className={"activity-card"} style={selectedStyle}>
      <div className="activity-card-header">
        <div className="activity-title">{activity.name}</div>
        <div>
          {activity.requiereVestimenta ? (
            <span className="badge badge-success">Requiere talla</span>
          ) : (
            <span className="badge badge-muted">Sin talla</span>
          )}
        </div>
      </div>

      <p className="activity-description">{activity.description}</p>

      <div className="cupos-container">
        <div className="cupos-text">
          Hasta <span className={`cupos-number ${activity.capacity === 0 ? "cupos-number-zero" : ""}`}>{activity.capacity}</span> personas
        </div>
      </div>

      <div style={{ display: "flex", gap: 12 }}>
        <button
          className={"btn btn-primary"}
          onClick={() => onSelect(activity.id)}
          aria-pressed={selected}
          title={selected ? "Actividad seleccionada" : "Seleccionar actividad"}
        >
          {selected ? "Seleccionada" : "Seleccionar"}
        </button>
      </div>
    </div>
  )
}

// ---------------------------
// TimeSlotCard
// ---------------------------
export function TimeSlotCard({ slot, onSelect, selected }: { slot: TimeSlot; onSelect: (id: string) => void; selected: boolean }) {
  const selectedStyle: React.CSSProperties | undefined = selected
    ? { borderColor: "var(--verde-claro)", boxShadow: "0 8px 12px rgba(0,0,0,0.12)" }
    : undefined

  return (
    <div className={"activity-card"} style={selectedStyle}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <div>
          <div style={{ fontWeight: 700 }}>{slot.time}</div>
          <div className="cupos-total">{formatDateLong(slot.date)}</div>
        </div>
        <div style={{ textAlign: "right" }}>
          {slot.isAvailable ? (
            <div>
              <div className="cupos-number">{slot.availableSpots} cupos</div>
              <div className="cupos-text">disponibles</div>
            </div>
          ) : (
            <div className="badge badge-muted">Completo</div>
          )}
        </div>
      </div>

      <div style={{ marginTop: 12, display: "flex", gap: 12 }}>
        <button className="btn btn-primary" onClick={() => onSelect(slot.id)} disabled={!slot.isAvailable} aria-pressed={selected}>
          {selected ? "Seleccionado" : slot.isAvailable ? "Seleccionar horario" : "No disponible"}
        </button>
      </div>
    </div>
  )
}

// ---------------------------
// ParticipantsForm
// ---------------------------
// Dentro de ActivityComponents file: reemplazar la sección ParticipantsForm por esto

export function ParticipantsForm({
  participants,
  onChange,
  onCountChange,
  selectedTimeSlot,
  selectedActivity,
  contactEmail,
  onContactEmailChange,
}: {
  participants: Participant[]
  onChange: (index: number, field: keyof Participant, value: string) => void
  onCountChange: (n: number) => void
  selectedTimeSlot: TimeSlot | null
  selectedActivity: ActivityFromApi | null
  contactEmail: string
  onContactEmailChange: (v: string) => void
}) {
  return (
    <div>
      <h3 className="section-title">Participantes</h3>
      <p className="section-description">Cupos disponibles: {selectedTimeSlot?.availableSpots ?? "-"}</p>

      <div style={{ marginTop: 12 }}>
        <label className="form-label">Número de participantes</label>
        <select className="form-select" value={participants.length} onChange={(e) => onCountChange(Number(e.target.value))}>
          {Array.from({ length: Math.min(selectedTimeSlot?.availableSpots ?? 1, 10) }, (_, i) => i + 1).map((n) => (
            <option key={n} value={n}>{n} {n === 1 ? "persona" : "personas"}</option>
          ))}
        </select>
      </div>

      {/* Email de contacto único */}
      <div style={{ marginTop: 12 }} className="form-group">
        <label className="form-label">Email de contacto </label>
        <input
          type="email"
          className={`form-input ${!contactEmail.trim() ? "form-input-error" : ""}`}
          value={contactEmail}
          onChange={(e) => onContactEmailChange(e.target.value)}
          placeholder="ejemplo@correo.com"
        />
      </div>

      <div style={{ marginTop: 18 }} className="form">
        {participants.map((participant, index) => (
          <div key={index} className="activity-card">
            <div style={{ marginBottom: 8, fontWeight: 700 }}>Participante {index + 1}</div>

            <div className="form-group">
              <label className="form-label">Nombre completo</label>
              <input className={`form-input ${!participant.name.trim() ? "form-input-error" : ""}`} value={participant.name} onChange={(e) => onChange(index, "name", e.target.value)} placeholder="Ej: Juan Pérez" />
            </div>

            <div className="form-group">
              <label className="form-label">DNI</label>
              <input className={`form-input ${!participant.dni.trim() ? "form-input-error" : ""}`} value={participant.dni} onChange={(e) => onChange(index, "dni", e.target.value)} placeholder="Ej: 12345678" />
            </div>

            <div className="form-group">
              <label className="form-label">Edad</label>
              <input type="number" className={`form-input ${!participant.age.trim() ? "form-input-error" : ""}`} value={participant.age} onChange={(e) => onChange(index, "age", e.target.value)} placeholder="Ej: 25" />
            </div>

            {selectedActivity?.requiereVestimenta && (
              <div className="form-group">
                <label className="form-label">Talla de vestimenta</label>
                <select className="form-select" value={participant.clothingSize} onChange={(e) => onChange(index, "clothingSize", e.target.value)}>
                  <option value="">Seleccione talla</option>
                  {clothingSizes.map((s) => (
                    <option key={s} value={s}>{s}</option>
                  ))}
                </select>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}


// ---------------------------
// TermsSection
// ---------------------------
export function TermsSection({ selectedActivity, termsAccepted, onToggle }: { selectedActivity: ActivityFromApi | null; termsAccepted: boolean; onToggle: (v: boolean) => void }) {
  return (
    <div>
      <h3 className="section-title">Términos y condiciones</h3>
      <div className="activity-card">
        <div className="modal-description">{selectedActivity?.terminosCondiciones}</div>
      </div>

      <div className="checkbox-container" style={{ marginTop: 12 }}>
        <input id="terms" type="checkbox" className="checkbox" checked={termsAccepted} onChange={(e) => onToggle(e.target.checked)} />
        <label htmlFor="terms" className="checkbox-label">Acepto los términos y condiciones específicos de la actividad</label>
      </div>
    </div>
  )
}

// ---------------------------
// ConfirmationModal
// ---------------------------
export function ConfirmationModal({
  open,
  onClose,
  selectedActivity,
  selectedTimeSlot,
  selectedDate,
  participants,
}: {
  open: boolean
  onClose: () => void
  selectedActivity: ActivityFromApi | null
  selectedTimeSlot: TimeSlot | null
  selectedDate?: string
  participants: Participant[]
}) {
  if (!open) return null
  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-content">
        <div className="success-container">
          <div className="success-icon-container">
            <div className="success-icon">✓</div>
          </div>
          <div className="success-title">¡Inscripción exitosa!</div>
          <div className="success-message">Su reserva ha sido confirmada para la actividad</div>
        </div>

        <div className="activity-card">
          <div style={{ marginBottom: 12 }}>
            <div style={{ fontWeight: 700 }}>Actividad</div>
            <div>{selectedActivity?.name}</div>
          </div>

          <div style={{ marginBottom: 12 }}>
            <div style={{ fontWeight: 700 }}>Horario</div>
            <div>{selectedTimeSlot?.time}</div>
          </div>

          <div style={{ marginBottom: 12 }}>
            <div style={{ fontWeight: 700 }}>Fecha</div>
            <div>{formatDateLong(selectedDate)}</div>
          </div>

          <div style={{ marginBottom: 12 }}>
            <div style={{ fontWeight: 700 }}>Participantes</div>
            <div>{participants.length}</div>
          </div>

          <div style={{ marginTop: 12 }}>
            <div style={{ fontWeight: 700 }}>Participantes registrados:</div>
            <ul>
              {participants.map((p, i) => (
                <li key={i}>{i + 1}. {p.name} - DNI: {p.dni}</li>
              ))}
            </ul>
          </div>

          <div style={{ marginTop: 16, display: "flex", gap: 8 }}>
            <button className="btn btn-secondary" onClick={onClose}>Realizar otra inscripción</button>
          </div>
        </div>
      </div>
    </div>
  )
}