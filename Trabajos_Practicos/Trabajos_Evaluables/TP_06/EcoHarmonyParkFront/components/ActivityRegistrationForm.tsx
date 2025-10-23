"use client"

import React, { useEffect, useState } from "react"
import '../app/globals.css'

// Importar tipos, constantes y helpers
import {
  ActividadResponse,
  HorarioResponse,
  Step,
  TimeSlot,
  ActivityFromApi,
  Participant,
  API_BASE_URL,
  minDateForInput,
} from "../lib/types"

// Importar todos los subcomponentes
import {
  Header,
  ErrorBox,
  Spinner,
  ActivityCard,
  TimeSlotCard,
  ParticipantsForm,
  TermsSection,
  ConfirmationModal
} from "./ActivityComponents"

import { registerForActivity } from "../lib/api"


export function ActivityRegistrationForm() {
  const [currentStep, setCurrentStep] = useState<Step>("activity")
  const [submitting, setSubmitting] = useState(false)
  const [activities, setActivities] = useState<ActivityFromApi[]>([])
  const [loading, setLoading] = useState(false)
  const [selectedActivity, setSelectedActivity] = useState<ActivityFromApi | null>(null)
  const [selectedDate, setSelectedDate] = useState<string | undefined>(undefined) // YYYY-MM-DD
  const [timeSlots, setTimeSlots] = useState<TimeSlot[]>([])
  const [selectedTimeSlot, setSelectedTimeSlot] = useState<TimeSlot | null>(null)
  const [participantCount, setParticipantCount] = useState<number>(1)
  const [participants, setParticipants] = useState<Participant[]>(
    [{ name: "", dni: "", age: "", clothingSize: "" }]
  )
  const [contactEmail, setContactEmail] = useState<string>("") // <- nuevo campo: email de contacto único
  const [termsAccepted, setTermsAccepted] = useState(false)
  const [errors, setErrors] = useState<string[]>([])
  const [registrationComplete, setRegistrationComplete] = useState(false)

  useEffect(() => {
    let mounted = true
    setLoading(true)
    fetch(`${API_BASE_URL}/actividades`, { headers: { "Content-Type": "application/json" } })
      .then(async (res) => {
        if (!res.ok) throw new Error("Error al cargar actividades")
        const data: ActividadResponse[] = await res.json()
        if (!mounted) return
        const mapped: ActivityFromApi[] = data.map((a) => ({
          id: a.id,
          name: a.nombre,
          description: a.descripcion,
          requiereVestimenta: a.requiereVestimenta,
          capacity: a.cupoMaximo,
          terminosCondiciones: a.terminosCondiciones,
          horarios: [],
        }))
        setActivities(mapped)
      })
      .catch((err) => {
        console.error(err)
        setErrors(["No se pudieron cargar las actividades. Intente nuevamente."])
      })
      .finally(() => setLoading(false))

    return () => {
      mounted = false
    }
  }, [])

  async function fetchHorarios(activityId: number, dateISO: string) {
    try {
      const res = await fetch(`${API_BASE_URL}/actividades/${activityId}/horarios?fecha=${dateISO}`, {
        headers: { "Content-Type": "application/json" },
      })
      if (!res.ok) return [] as HorarioResponse[]
      const horarios: HorarioResponse[] = await res.json()
      return horarios
    } catch (err) {
      console.error(err)
      return [] as HorarioResponse[]
    }
  }

  const handleActivitySelect = async (activityId: number) => {
    const activity = activities.find((a) => a.id === activityId) || null
    setSelectedActivity(activity)
    setSelectedDate(undefined)
    setSelectedTimeSlot(null)
    setTimeSlots([])
    setErrors([])
    setParticipantCount(1)
    setParticipants([{ name: "", dni: "", age: "", clothingSize: "" }])
    setContactEmail("")
  }

  const handleDateChange = async (dateValue: string) => {
    if (!dateValue) return

    const today = new Date();
    today.setHours(0,0,0,0);

    const [y, m, d] = dateValue.split('-');
    const selectedDateObj = new Date(Number(y), Number(m) -1, Number(d));

    if (selectedDateObj < today) {
      setErrors(["No puede seleccionar una fecha anterior a hoy"])
      return
    }

    setSelectedDate(dateValue)
    setSelectedTimeSlot(null)
    setErrors([])

    if (selectedActivity) {
      const dateISO = dateValue
      const horarios = await fetchHorarios(selectedActivity.id, dateISO)
      const mapped: TimeSlot[] = horarios.map((h) => ({
        id: String(h.id),
        time: h.horaInicio ? `${h.horaInicio}${h.horaFin ? ` - ${h.horaFin}` : ""}` : "",
        date: h.fecha,
        availableSpots: h.cuposDisponibles,
        isAvailable: h.cuposDisponibles > 0,
        raw: h,
      }))
      setTimeSlots(mapped)
    }
  }

  const handleTimeSlotSelect = (timeSlotId: string) => {
    const timeSlot = timeSlots.find((ts) => ts.id === timeSlotId) || null
    if (timeSlot) {
      if (!timeSlot.isAvailable) {
        setErrors(["El horario seleccionado no está disponible"])
        return
      }
      setSelectedTimeSlot(timeSlot)
      setErrors([])
    }
  }

  const handleParticipantCountChange = (count: number) => {
    if (!selectedTimeSlot) return

    if (count > selectedTimeSlot.availableSpots) {
      setErrors([`Solo hay ${selectedTimeSlot.availableSpots} cupos disponibles para este horario`])
      return
    }

    setParticipantCount(count)
    const newParticipants = Array.from({ length: count }, (_, i) => participants[i] || { name: "", dni: "", age: "", clothingSize: ""})
    setParticipants(newParticipants)
    setErrors([])
  }

  const handleParticipantChange = (index: number, field: keyof Participant, value: string) => {
    const newParticipants = [...participants];
    newParticipants[index] = { ...newParticipants[index], [field]: value };
    setParticipants(newParticipants);
  };

  const validateStep = (step: Step): boolean => {
    const newErrors: string[] = []

    switch (step) {
      case "activity":
        if (!selectedActivity) newErrors.push("Debe seleccionar una actividad")
        break
      case "timeslot":
        if (!selectedDate) newErrors.push("Debe seleccionar una fecha")
        if (!selectedTimeSlot) newErrors.push("Debe seleccionar un horario")
        else if (!selectedTimeSlot.isAvailable) newErrors.push("El horario seleccionado no está disponible")
        break
      case "participants":
        if (participantCount < 1) newErrors.push("Debe haber al menos un participante")
        if (selectedTimeSlot && participantCount > selectedTimeSlot.availableSpots) newErrors.push(`Solo hay ${selectedTimeSlot.availableSpots} cupos disponibles`)
        const nameRegex = /^[\p{L} '\-]+$/u;
        const dniRegex = /^\d{8}$/;

        participants.forEach((p, i) => {
          const idx = i + 1;
          const nombre = (p.name || "").trim();
          const dni = (p.dni || "").trim();
          const edadStr = (p.age || "").trim();

          if (!nombre) newErrors.push(`Participante ${idx}: El nombre es requerido`);
          else if (!nameRegex.test(nombre)) newErrors.push(`Participante ${idx}: El nombre debe contener solo letras, espacios, guiones o apóstrofes`);

          if (!dni) newErrors.push(`Participante ${idx}: El DNI es requerido`);
          else if (!dniRegex.test(dni)) newErrors.push(`Participante ${idx}: El DNI debe tener exactamente 8 dígitos`);

          if (!edadStr) newErrors.push(`Participante ${idx}: La edad es requerida`);
          else {
            const edadNum = Number(edadStr);
            if (!Number.isFinite(edadNum) || !Number.isInteger(edadNum)) newErrors.push(`Participante ${idx}: La edad debe ser un número entero`);
            else if (edadNum <= 0 || edadNum >= 99) newErrors.push(`Participante ${idx}: La edad debe ser mayor a 0 y menor a 99`);
          }

          if (selectedActivity?.requiereVestimenta && !p.clothingSize) newErrors.push(`Participante ${idx}: La talla de vestimenta es requerida`);
        });

        // validar email de contacto (único)
        const contactEmailTrim = contactEmail.trim();
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!contactEmailTrim) newErrors.push("El email de contacto es requerido");
        else if (!emailRegex.test(contactEmailTrim)) newErrors.push("El email de contacto no tiene un formato válido");

        break
      case "terms":
        if (!termsAccepted) newErrors.push("Debe aceptar los términos y condiciones para continuar")
        break
    }

    setErrors(newErrors)
    return newErrors.length === 0
  }

  const handleNext = () => {
    if (!validateStep(currentStep)) return
    const steps: Step[] = ["activity", "timeslot", "participants", "terms", "confirmation"]
    const currentIndex = steps.indexOf(currentStep)
    if (currentIndex < steps.length - 1) setCurrentStep(steps[currentIndex + 1])
  }

  const handleBack = () => {
    const steps: Step[] = ["activity", "timeslot", "participants", "terms", "confirmation"]
    const currentIndex = steps.indexOf(currentStep)
    if (currentIndex > 0) {
      setCurrentStep(steps[currentIndex - 1])
      setErrors([])
    }
  }

  const handleSubmit = async () => {
    if (submitting) return;
    if (!validateStep("terms")) return;
    if (!selectedActivity || !selectedTimeSlot || !selectedDate) return;

    // Construir visitantes (SIN email por visitante)
    const visitantesPayload = participants.map((p) => {
      const edadNum = Number(p.age);
      return {
        nombre: p.name.trim(),
        dni: p.dni.trim(),
        edad: Number.isFinite(edadNum) && edadNum > 0 ? edadNum : 0,
        tallaVestimenta: p.clothingSize ? p.clothingSize : null,
      };
    });

    const contactoEmail = contactEmail.trim() || null;

    const horarioIdFromRaw =
      selectedTimeSlot.raw && (selectedTimeSlot.raw as any).id
        ? Number((selectedTimeSlot.raw as any).id)
        : Number(selectedTimeSlot.id);

    const inscripcionRequest = {
      visitantes: visitantesPayload,
      horarioActividadId: horarioIdFromRaw,
      cantidadPersonas: visitantesPayload.length,
      email: contactoEmail,
    };

    setSubmitting(true);
    setErrors([]);

    try {
      const result = await registerForActivity(inscripcionRequest);

      if (!result.success) {
        setErrors([result.message || "Error al procesar la inscripción"]);
        return;
      }

      setRegistrationComplete(true);
      setCurrentStep("confirmation");

      // refrescar horarios opcional
      try {
        if (selectedActivity && selectedDate) {
          const horarios = await fetchHorarios(selectedActivity.id, selectedDate);
          const mapped = horarios.map((h) => ({
            id: String(h.id),
            time: h.horaInicio ? `${h.horaInicio}${h.horaFin ? ` - ${h.horaFin}` : ""}` : "",
            date: h.fecha,
            availableSpots: h.cuposDisponibles,
            isAvailable: h.cuposDisponibles > 0,
            raw: h,
          }));
          setTimeSlots(mapped);
        }
      } catch (e) {
        console.warn("No se pudo refrescar horarios tras inscripción", e);
      }
    } catch (err) {
      console.error(err);
      setErrors(["Error de conexión. Por favor, intente nuevamente."]);
    } finally {
      setSubmitting(false);
    }
  };

  const handleReset = () => {
    setCurrentStep("activity")
    setSelectedActivity(null)
    setSelectedDate(undefined)
    setSelectedTimeSlot(null)
    setTimeSlots([])
    setParticipantCount(1)
    setParticipants([{ name: "", dni: "", age: "", clothingSize: "" }])
    setContactEmail("")
    setTermsAccepted(false)
    setErrors([])
    setRegistrationComplete(false)
  }

  return (
    <div>
      <Header />

      <main className="main-content">
        <section className="section-header"> </section>

        <ErrorBox errors={errors} />

        {currentStep === "activity" && (
          <>
            <section className="section-header">
              <h2 className="section-title">Seleccione una actividad</h2>
              <p className="section-description">Elija entre las actividades disponibles y siga los pasos para reservar.</p>
            </section>

            <div className="activities-grid">
              {loading ? (
                <Spinner />
              ) : (
                activities.map((activity) => (
                  <ActivityCard
                    key={activity.id}
                    activity={activity}
                    onSelect={handleActivitySelect}
                    selected={selectedActivity?.id === activity.id}
                  />
                ))
              )}
            </div>
          </>
        )}

        {currentStep === "timeslot" && selectedActivity && (
          <div style={{ marginTop: 24 }}>
            <h3 className="section-title">Seleccione la fecha</h3>
            <p className="section-description">Elija el día en que desea realizar la actividad</p>

            <div style={{ marginTop: 12 }}>
              <label className="form-label">Fecha</label>
              <input
                type="date"
                className="form-input"
                min={minDateForInput()}
                value={selectedDate || ""}
                onChange={(e) => handleDateChange(e.target.value)}
              />
            </div>

            {selectedDate && (

              <div style={{ marginTop: 20 }}>
                <h4 className="section-title">Horarios disponibles para {selectedActivity.name}</h4>
                <p className="section-description">Seleccione el horario que prefiera</p>

                <div className="activities-grid">
                  {timeSlots.length === 0 ? (
                    <div className="badge badge-muted">No hay horarios para la fecha seleccionada</div>
                  ) : (
                    timeSlots.map((slot) => (
                      <TimeSlotCard
                        key={slot.id}
                        slot={slot}
                        onSelect={handleTimeSlotSelect}
                        selected={selectedTimeSlot?.id === slot.id}
                      />
                    ))
                  )}
                </div>
              </div>
            )}
          </div>
        )}

        {currentStep === "participants" && selectedActivity && selectedTimeSlot && (
          <ParticipantsForm
            participants={participants}
            onChange={handleParticipantChange}
            onCountChange={handleParticipantCountChange}
            selectedTimeSlot={selectedTimeSlot}
            selectedActivity={selectedActivity}
            contactEmail={contactEmail}
            onContactEmailChange={setContactEmail}
          />
        )}

        {currentStep === "terms" && selectedActivity && (
          <TermsSection selectedActivity={selectedActivity} termsAccepted={termsAccepted} onToggle={setTermsAccepted} />
        )}

        <ConfirmationModal
          open={currentStep === "confirmation" && registrationComplete}
          onClose={handleReset}
          selectedActivity={selectedActivity}
          selectedTimeSlot={selectedTimeSlot}
          selectedDate={selectedDate}
          participants={participants}
        />

        {currentStep !== "confirmation" && (
          <div style={{ marginTop: 24, display: "flex", justifyContent: "space-between", gap: 12 }} className="form-actions">
            <button className="btn btn-secondary" onClick={handleBack} disabled={currentStep === "activity" || submitting}>
              Anterior
            </button>

            {currentStep !== "terms" ? (
              <button className="btn btn-primary" onClick={handleNext} disabled={submitting}>
                Siguiente
              </button>
            ) : (
              <button
                className="btn btn-primary"
                onClick={handleSubmit}
                disabled={submitting}
                aria-busy={submitting}
                aria-live="polite"
              >
                {submitting ? (
                  <span style={{ display: "inline-flex", alignItems: "center", gap: 8 }}>
                    <svg width="18" height="18" viewBox="0 0 50 50" aria-hidden="true" focusable="false" style={{ animation: "spin 1s linear infinite" }}>
                      <circle cx="25" cy="25" r="20" fill="none" strokeWidth="5" stroke="currentColor" strokeOpacity="0.25" />
                      <path d="M45 25a20 20 0 0 1-6.6 14.4" stroke="currentColor" strokeWidth="5" strokeLinecap="round" fill="none" />
                    </svg>
                    <span>Confirmando...</span>
                  </span>
                ) : (
                  "Confirmar inscripción"
                )}
              </button>
            )}
          </div>
        )}
      </main>
    </div>
  )
}
