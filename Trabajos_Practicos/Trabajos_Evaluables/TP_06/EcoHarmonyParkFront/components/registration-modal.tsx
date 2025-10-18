"use client"

import type React from "react"

import { useState } from "react"
import type { Activity, ActivityRegistration } from "@/lib/types"
import { registerForActivity } from "@/lib/api"
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Checkbox } from "@/components/ui/checkbox"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Loader2, CheckCircle2 } from "lucide-react"
import { useToast } from "@/hooks/use-toast"

interface RegistrationModalProps {
  activity: Activity
  onClose: () => void
  onSuccess: () => void
}

export function RegistrationModal({ activity, onClose, onSuccess }: RegistrationModalProps) {
  const { toast } = useToast()
  const [loading, setLoading] = useState(false)
  const [showSuccess, setShowSuccess] = useState(false)

  // Form state
  const [name, setName] = useState("")
  const [dni, setDni] = useState("")
  const [age, setAge] = useState("")
  const [clothingSize, setClothingSize] = useState<"S" | "M" | "L" | "XL" | "">("")
  const [acceptTerms, setAcceptTerms] = useState(false)

  // Validation state
  const [errors, setErrors] = useState({
    name: "",
    dni: "",
    age: "",
    clothingSize: "",
  })

  const requiresClothingSize = activity.requiereVestimenta

  function validateName(value: string): string {
    if (!value.trim()) return "El nombre es requerido"
    if (value.trim().length < 6) return "El nombre debe tener al menos 6 caracteres"
    if (value.trim().length > 30) return "El nombre no puede exceder 30 caracteres"
    return ""
  }

  function validateDni(value: string): string {
    if (!value.trim()) return "El DNI es requerido"
    if (!/^\d+$/.test(value)) return "El DNI debe contener solo números"
    if (value.length !== 8) return "El DNI debe tener exactamente 8 dígitos"
    return ""
  }

  function validateAge(value: string): string {
    if (!value.trim()) return "La edad es requerida"
    const ageNum = Number.parseInt(value)
    if (isNaN(ageNum) || ageNum <= 0) return "La edad debe ser un número positivo"
    return ""
  }

  function validateClothingSize(): string {
    if (requiresClothingSize && !clothingSize) {
      return "La talla de vestimenta es requerida para esta actividad"
    }
    return ""
  }

  function handleNameChange(value: string) {
    setName(value)
    setErrors((prev) => ({ ...prev, name: validateName(value) }))
  }

  function handleDniChange(value: string) {
    setDni(value)
    setErrors((prev) => ({ ...prev, dni: validateDni(value) }))
  }

  function handleAgeChange(value: string) {
    setAge(value)
    setErrors((prev) => ({ ...prev, age: validateAge(value) }))
  }

  function handleClothingSizeChange(value: "S" | "M" | "L" | "XL") {
    setClothingSize(value)
    setErrors((prev) => ({ ...prev, clothingSize: "" }))
  }

  const isFormValid =
    name.trim() !== "" &&
    dni.trim() !== "" &&
    age.trim() !== "" &&
    !errors.name &&
    !errors.dni &&
    !errors.age &&
    (!requiresClothingSize || clothingSize !== "") &&
    acceptTerms

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()

    // Final validation
    const nameError = validateName(name)
    const dniError = validateDni(dni)
    const ageError = validateAge(age)
    const sizeError = validateClothingSize()

    if (nameError || dniError || ageError || sizeError) {
      setErrors({
        name: nameError,
        dni: dniError,
        age: ageError,
        clothingSize: sizeError,
      })
      return
    }

    setLoading(true)

    // Obtener el primer horario disponible
    const primerHorario = activity.horarios.find(h => h.cuposDisponibles > 0)

    if (!primerHorario) {
      toast({
        variant: "destructive",
        title: "Error en la inscripción",
        description: "No hay horarios disponibles para esta actividad",
      })
      setLoading(false)
      return
    }

    const registration: ActivityRegistration = {
      activityId: activity.id,
      visitorName: name,
      visitorDni: dni,
      visitorAge: Number.parseInt(age),
      horarioActividadId: primerHorario.id,
      ...(requiresClothingSize && clothingSize && { clothingSize }),
    }

    const result = await registerForActivity(registration)

    setLoading(false)

    if (result.success) {
      setShowSuccess(true)
    } else {
      toast({
        variant: "destructive",
        title: "Error en la inscripción",
        description: result.message || "Lo sentimos, los cupos para esta actividad se han agotado",
      })
    }
  }

  if (showSuccess) {
    return (
      <Dialog open={true} onOpenChange={onClose}>
        <DialogContent className="sm:max-w-md">
          <div className="flex flex-col items-center justify-center py-8 text-center">
            <div className="rounded-full bg-success/20 p-4 mb-4">
              <CheckCircle2 className="h-12 w-12 text-success" />
            </div>
            <h3 className="text-2xl font-bold text-foreground mb-2">¡Inscripción exitosa!</h3>
            <p className="text-muted-foreground mb-6">La inscripción confirmada llegará a su correo.</p>
            <Button
              onClick={() => {
                onSuccess()
                onClose()
              }}
              className="bg-primary hover:bg-primary/90 text-primary-foreground"
            >
              Volver a actividades
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    )
  }

  return (
    <Dialog open={true} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-lg max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="text-2xl font-bold text-foreground">Inscripción a {activity.name}</DialogTitle>
          <DialogDescription className="text-muted-foreground">Complete el formulario para inscribirse a esta actividad</DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4 mt-4">
          {/* Name Field */}
          <div className="space-y-2">
            <Label htmlFor="name" className="text-foreground font-semibold">
              Nombre completo *
            </Label>
            <Input
              id="name"
              value={name}
              onChange={(e) => handleNameChange(e.target.value)}
              placeholder="Ingrese su nombre completo"
              className={errors.name ? "border-destructive" : ""}
            />
            {errors.name && <p className="text-sm text-destructive">{errors.name}</p>}
          </div>

          {/* DNI Field */}
          <div className="space-y-2">
            <Label htmlFor="dni" className="text-foreground font-semibold">
              DNI *
            </Label>
            <Input
              id="dni"
              value={dni}
              onChange={(e) => handleDniChange(e.target.value)}
              placeholder="Ingrese su DNI"
              className={errors.dni ? "border-destructive" : ""}
            />
            {errors.dni && <p className="text-sm text-destructive">{errors.dni}</p>}
          </div>

          {/* Age Field */}
          <div className="space-y-2">
            <Label htmlFor="age" className="text-foreground font-semibold">
              Edad *
            </Label>
            <Input
              id="age"
              type="number"
              value={age}
              onChange={(e) => handleAgeChange(e.target.value)}
              placeholder="Ingrese su edad"
              className={errors.age ? "border-destructive" : ""}
            />
            {errors.age && <p className="text-sm text-destructive">{errors.age}</p>}
          </div>

          {/* Clothing Size Field (Conditional) */}
          {requiresClothingSize && (
            <div className="space-y-2">
              <Label htmlFor="size" className="text-foreground font-semibold">
                Talla de vestimenta *
              </Label>
              <Select value={clothingSize} onValueChange={handleClothingSizeChange}>
                <SelectTrigger className={errors.clothingSize ? "border-destructive" : ""}>
                  <SelectValue placeholder="Seleccione su talla" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="S">S - Pequeña</SelectItem>
                  <SelectItem value="M">M - Mediana</SelectItem>
                  <SelectItem value="L">L - Grande</SelectItem>
                  <SelectItem value="XL">XL - Extra Grande</SelectItem>
                </SelectContent>
              </Select>
              {errors.clothingSize && <p className="text-sm text-destructive">{errors.clothingSize}</p>}
            </div>
          )}

          {/* Terms Checkbox */}
          <div className="flex items-start space-x-2 pt-2">
            <Checkbox
              id="terms"
              checked={acceptTerms}
              onCheckedChange={(checked) => setAcceptTerms(checked as boolean)}
            />
            <label
              htmlFor="terms"
              className="text-sm text-foreground leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
            >
              Acepto los términos y condiciones
            </label>
          </div>

          {/* Submit Button */}
          <div className="flex gap-3 pt-4">
            <Button
              type="button"
              variant="outline"
              onClick={onClose}
              className="flex-1 bg-transparent"
              disabled={loading}
            >
              Cancelar
            </Button>
            <Button type="submit" disabled={!isFormValid || loading} className="flex-1 bg-primary hover:bg-primary/90">
              {loading ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Procesando...
                </>
              ) : (
                "Confirmar Inscripción"
              )}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  )
}
