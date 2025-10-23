package Grupo4.EcoHarmonyParkBack.service;

import Grupo4.EcoHarmonyParkBack.dtos.InscripcionRequest;
import Grupo4.EcoHarmonyParkBack.dtos.InscripcionResponse;
import Grupo4.EcoHarmonyParkBack.dtos.VisitanteRequest;
import Grupo4.EcoHarmonyParkBack.entities.Actividad;
import Grupo4.EcoHarmonyParkBack.entities.Grupo;
import Grupo4.EcoHarmonyParkBack.entities.HorarioActividad;
import Grupo4.EcoHarmonyParkBack.entities.Inscripcion;
import Grupo4.EcoHarmonyParkBack.entities.Visitante;
import Grupo4.EcoHarmonyParkBack.repositories.HorarioActividadRepository;
import Grupo4.EcoHarmonyParkBack.repositories.InscripcionRepository;
import Grupo4.EcoHarmonyParkBack.services.InscripcionService;
import Grupo4.EcoHarmonyParkBack.services.VisitanteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para InscripcionService
 * Prueba todos los escenarios de validación de la lógica de negocio para la inscripción a actividades
 */
@ExtendWith(MockitoExtension.class)
class InscripcionServiceTest {

    @Mock
    private InscripcionRepository inscripcionRepository;

    @Mock
    private HorarioActividadRepository horarioRepository;

    @Mock
    private VisitanteService visitanteService;

    @InjectMocks
    private InscripcionService inscripcionService;

    private HorarioActividad horarioConCupos;
    private Actividad actividadJardineria;
    private Actividad actividadSafari;
    private Actividad actividadTirolesa;
    private Actividad actividadPalestra;

    @BeforeEach
    void setUp() {
        actividadJardineria = Actividad.builder()
                .id(4L)
                .nombre("Jardinería")
                .requiereVestimenta(false)
                .edadMinima(0)
                .cupoMaximo(12)
                .descripcion("Actividad práctica de plantación, riego y cuidado de plantas en el vivero del parque.")
                .terminosCondiciones("El participante debe seguir las instrucciones del encargado y respetar las zonas delimitadas.")
                .build();

        actividadPalestra = Actividad.builder()
                .id(3L)
                .nombre("Palestra")
                .requiereVestimenta(true)
                .edadMinima(12)
                .cupoMaximo(12)
                .descripcion("Actividad de escalada en muro vertical con asistencia de un instructor.")
                .terminosCondiciones("El participante debe usar arnés y casco provistos por el parque.")
                .build();

        actividadSafari = Actividad.builder()
                .id(2L)
                .nombre("Safari")
                .requiereVestimenta(false)
                .edadMinima(12)
                .cupoMaximo(8)
                .descripcion("Recorrido guiado por las zonas de animales del parque con observación educativa.")
                .terminosCondiciones("El participante debe permanecer dentro del vehículo durante todo el recorrido.")
                .build();

        actividadTirolesa = Actividad.builder()
                .id(1L)
                .nombre("Tirolesa")
                .requiereVestimenta(true)
                .edadMinima(12)
                .cupoMaximo(10)
                .descripcion("Recorrido aéreo por cable con arnés de seguridad y casco obligatorio.")
                .terminosCondiciones("El participante debe usar el equipo de seguridad completo y seguir las instrucciones del guía.")
                .build();

        // Horario con cupos disponibles (futuro)
        horarioConCupos = HorarioActividad.builder()
                .id(1L)
                .actividad(actividadSafari)
                .fecha(LocalDate.now().plusDays(7))
                .horaInicio(LocalTime.of(10, 0))
                .horaFin(LocalTime.of(12, 0))
                .cuposDisponibles(10)
                .build();
    }

    // ==================== CASO DE PRUEBA 1: INSCRIPCION CORRECTA ====================

    @Test
    @DisplayName("Debería registrarse exitosamente cuando todos los datos sean válidos y haya espacios disponibles")
    void deberiaRegistrarConExitoCuandoLosDatosSonValidos() {
        // Arrange
        VisitanteRequest visitante1 = VisitanteRequest.builder()
                .nombre("Juan Perez")
                .dni("12345678")
                .edad(30)
                .tallaVestimenta(null) // Safari no requiere vestimenta
                .build();

        InscripcionRequest request = InscripcionRequest.builder()
                .horarioActividadId(1L)
                .cantidadPersonas(1)
                .visitantes(List.of(visitante1))
                .email("juan@example.com")
                .aceptoTyC(true)
                .build();

        Visitante visitanteEntity = Visitante.builder()
                .id(1L)
                .nombre("Juan Perez")
                .dni("12345678")
                .edad(30)
                .build();

        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horarioConCupos));
        when(inscripcionRepository.existsByHorarioAndVisitanteDni(1L, "12345678")).thenReturn(false);
        when(visitanteService.crearVisitante(any(VisitanteRequest.class))).thenReturn(visitanteEntity);
        when(horarioRepository.save(any(HorarioActividad.class))).thenReturn(horarioConCupos);
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(invocation -> {
            Inscripcion inscripcion = invocation.getArgument(0);
            inscripcion.setId(1L);
            return inscripcion;
        });

        InscripcionResponse result = inscripcionService.inscribirActividad(request);

        assertNotNull(result);
        assertEquals("juan@example.com", result.getEmail());
        assertEquals(1, result.getCantidadPersonas());

        verify(horarioRepository).save(argThat(horario ->
            horario.getCuposDisponibles() == 9
        ));

        verify(visitanteService, times(1)).crearVisitante(any(VisitanteRequest.class));

        verify(inscripcionRepository, times(1)).save(any(Inscripcion.class));
    }

    // ==================== CASO DE PRUEBA 2: DATOS FALTANTES (Validación de Jakarta) ====================
    // Nota: La validación de Jakarta se maneja a nivel del controlador con @Valid
    // Las pruebas de la capa de servicio asumen que los datos ya pasaron la validación

    @Test
    @DisplayName("Debería lanzar una excepción cuando el horario no existe")
    void deberiaLanzarExcepcionCuandoNoSeEncuentraElHorario() {
        InscripcionRequest request = InscripcionRequest.builder()
                .horarioActividadId(999L)
                .cantidadPersonas(1)
                .visitantes(List.of(VisitanteRequest.builder()
                        .nombre("Juan Perez")
                        .dni("12345678")
                        .edad(30)
                        .build()))
                .email("juan@example.com")
                .aceptoTyC(true)
                .build();

        when(horarioRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inscripcionService.inscribirActividad(request);
        });

        assertEquals("Horario no encontrado", exception.getMessage());
        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    // ==================== CASO DE PRUEBA 3: INSCRIPCION DUPLICADA ====================

    @Test
    @DisplayName("Debería lanzar una excepción cuando el visitante ya está registrado en el mismo horario")
    void deberiaLanzarExcepcionCuandoElVisitanteYaEstaRegistradoEnElMismoHorario() {
        VisitanteRequest visitante = VisitanteRequest.builder()
                .nombre("Juan Perez")
                .dni("12345678")
                .edad(30)
                .build();

        InscripcionRequest request = InscripcionRequest.builder()
                .horarioActividadId(1L)
                .cantidadPersonas(1)
                .visitantes(List.of(visitante))
                .email("juan@example.com")
                .aceptoTyC(true)
                .build();

        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horarioConCupos));
        when(inscripcionRepository.existsByHorarioAndVisitanteDni(1L, "12345678")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inscripcionService.inscribirActividad(request);
        });

        assertTrue(exception.getMessage().contains("ya está inscripto en este horario"));
        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
        verify(horarioRepository, never()).save(any(HorarioActividad.class));
    }

    // ==================== CASO DE PRUEBA 4: HORARIO CON CUPOS LLENOS ====================

    @Test
    @DisplayName("Debería lanzar una excepción cuando el horario no tiene cupos disponibles")
    void deberiaLanzarExcepcionCuandoNoHayCuposDisponibles() {
        HorarioActividad horarioSinCupos = HorarioActividad.builder()
                .id(1L)
                .actividad(actividadSafari)
                .fecha(LocalDate.now().plusDays(7))
                .horaInicio(LocalTime.of(10, 0))
                .horaFin(LocalTime.of(12, 0))
                .cuposDisponibles(0) // Sin cupos
                .build();

        VisitanteRequest visitante = VisitanteRequest.builder()
                .nombre("Juan Perez")
                .dni("12345678")
                .edad(30)
                .build();

        InscripcionRequest request = InscripcionRequest.builder()
                .horarioActividadId(1L)
                .cantidadPersonas(1)
                .visitantes(List.of(visitante))
                .email("juan@example.com")
                .aceptoTyC(true)
                .build();

        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horarioSinCupos));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inscripcionService.inscribirActividad(request);
        });

        assertTrue(exception.getMessage().contains("No hay cupos suficientes"));
        assertTrue(exception.getMessage().contains("Cupos disponibles: 0"));
        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    // ==================== CASO DE PRUEBA 5: HORARIO SIN CUPOS SUFICIENTES ====================

    @Test
    @DisplayName("Debería lanzar una excepción cuando los cupos solicitados exceden los disponibles")
    void deberiaLanzarExcepcionCuandoLosCuposSonInsuficientes() {
        HorarioActividad horarioConPococCupos = HorarioActividad.builder()
                .id(1L)
                .actividad(actividadSafari)
                .fecha(LocalDate.now().plusDays(7))
                .horaInicio(LocalTime.of(10, 0))
                .horaFin(LocalTime.of(12, 0))
                .cuposDisponibles(1) // Solo 1 cupo
                .build();

        List<VisitanteRequest> visitantes = Arrays.asList(
                VisitanteRequest.builder().nombre("Juan Perez").dni("12345678").edad(30).build(),
                VisitanteRequest.builder().nombre("Maria Gomez").dni("87654321").edad(25).build()
        );

        InscripcionRequest request = InscripcionRequest.builder()
                .horarioActividadId(1L)
                .cantidadPersonas(2) // Solicita 2 cupos
                .visitantes(visitantes)
                .email("juan@example.com")
                .aceptoTyC(true)
                .build();

        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horarioConPococCupos));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inscripcionService.inscribirActividad(request);
        });

        assertTrue(exception.getMessage().contains("No hay cupos suficientes"));
        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    // ==================== CASO DE PRUEBA 6: NO ACEPTAR TERMINOS Y CONDICIONES ====================

    @Test
    @DisplayName("Debería lanzar una excepción cuando no se aceptan los términos y condiciones")
    void deberiaLanzarExcepcionCuandoNoSeAceptanLosTerminosYCondiciones() {
        // Arrange
        InscripcionRequest request = InscripcionRequest.builder()
                .horarioActividadId(1L)
                .cantidadPersonas(1)
                .visitantes(List.of(VisitanteRequest.builder()
                        .nombre("Juan Perez")
                        .dni("12345678")
                        .edad(30)
                        .build()))
                .email("juan@example.com")
                .aceptoTyC(false) // No acepta TyC
                .build();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inscripcionService.inscribirActividad(request);
        });

        assertEquals("Debe aceptar los términos y condiciones.", exception.getMessage());
        verify(horarioRepository, never()).findById(anyLong());
        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    // ==================== CASO DE PRUEBA 7: NO INGRESAR TALLA DE ROPA ====================

    @Test
    @DisplayName("Debería lanzar una excepción cuando se requiere la talla de vestimenta pero no se proporciona")
    void deberiaLanzarExcepcionCuandoSeRequiereTallaDeVestimentaPeroNoSeProporciona() {
        HorarioActividad horarioTirolesa = HorarioActividad.builder()
                .id(2L)
                .actividad(actividadTirolesa) // Requiere vestimenta
                .fecha(LocalDate.now().plusDays(7))
                .horaInicio(LocalTime.of(14, 0))
                .horaFin(LocalTime.of(16, 0))
                .cuposDisponibles(5)
                .build();

        VisitanteRequest visitanteSinTalla = VisitanteRequest.builder()
                .nombre("Juan Perez")
                .dni("12345678")
                .edad(25)
                .tallaVestimenta(null) // Sin talla
                .build();

        InscripcionRequest request = InscripcionRequest.builder()
                .horarioActividadId(2L)
                .cantidadPersonas(1)
                .visitantes(List.of(visitanteSinTalla))
                .email("juan@example.com")
                .aceptoTyC(true)
                .build();

        when(horarioRepository.findById(2L)).thenReturn(Optional.of(horarioTirolesa));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inscripcionService.inscribirActividad(request);
        });

        assertTrue(exception.getMessage().contains("requiere talla de vestimenta"));
        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    @DisplayName("Debería lanzar una excepción cuando la talla de vestimenta está vacía")
    void deberiaLanzarExcepcionCuandoLaTallaDeVestimentaEstaVacia() {
        HorarioActividad horarioTirolesa = HorarioActividad.builder()
                .id(2L)
                .actividad(actividadTirolesa)
                .fecha(LocalDate.now().plusDays(7))
                .horaInicio(LocalTime.of(14, 0))
                .horaFin(LocalTime.of(16, 0))
                .cuposDisponibles(5)
                .build();

        VisitanteRequest visitanteSinTalla = VisitanteRequest.builder()
                .nombre("Juan Perez")
                .dni("12345678")
                .edad(25)
                .tallaVestimenta("") // Talla vacía
                .build();

        InscripcionRequest request = InscripcionRequest.builder()
                .horarioActividadId(2L)
                .cantidadPersonas(1)
                .visitantes(List.of(visitanteSinTalla))
                .email("juan@example.com")
                .aceptoTyC(true)
                .build();

        when(horarioRepository.findById(2L)).thenReturn(Optional.of(horarioTirolesa));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inscripcionService.inscribirActividad(request);
        });

        assertTrue(exception.getMessage().contains("requiere talla de vestimenta"));
    }

    // ==================== CASO DE PRUEBA 8: MENOR DE EDAD ====================

    @Test
    @DisplayName("Debería lanzar una excepción cuando el visitante es menor a la edad mínima")
    void deberiaLanzarExcepcionCuandoElVisitanteEsMenorALaEdadMinima() {
        HorarioActividad horarioPalestra = HorarioActividad.builder()
                .id(3L)
                .actividad(actividadTirolesa)
                .fecha(LocalDate.now().plusDays(7))
                .horaInicio(LocalTime.of(14, 0))
                .horaFin(LocalTime.of(16, 0))
                .cuposDisponibles(5)
                .build();

        VisitanteRequest visitanteMenor = VisitanteRequest.builder()
                .nombre("Pedro Martinez")
                .dni("11111111")
                .edad(6)
                .tallaVestimenta("M")
                .build();

        InscripcionRequest request = InscripcionRequest.builder()
                .horarioActividadId(3L)
                .cantidadPersonas(1)
                .visitantes(List.of(visitanteMenor))
                .email("pedro@example.com")
                .aceptoTyC(true)
                .build();

        when(horarioRepository.findById(3L)).thenReturn(Optional.of(horarioPalestra));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inscripcionService.inscribirActividad(request);
        });

        assertTrue(exception.getMessage().contains("tiene 6 años"));
        assertTrue(exception.getMessage().contains("edad mínima requerida es 12 años"));
        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    // ==================== CASO DE PRUEBA 14: DNI DUPLICADO EN EL MISMO REQUEST ====================

    @Test
    @DisplayName("Debería lanzar una excepción cuando el mismo DNI aparece varias veces en la solicitud")
    void deberiaLanzarExcepcionCuandoElMismoDniApareceVariasVecesEnLaSolicitud() {
        List<VisitanteRequest> visitantesConDniDuplicado = Arrays.asList(
                VisitanteRequest.builder().nombre("Juan Perez").dni("12345678").edad(30).build(),
                VisitanteRequest.builder().nombre("Juan Perez Duplicado").dni("12345678").edad(30).build()
        );

        InscripcionRequest request = InscripcionRequest.builder()
                .horarioActividadId(1L)
                .cantidadPersonas(2)
                .visitantes(visitantesConDniDuplicado)
                .email("juan@example.com")
                .aceptoTyC(true)
                .build();

        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horarioConCupos));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inscripcionService.inscribirActividad(request);
        });

        assertTrue(exception.getMessage().contains("DNI 12345678"));
        assertTrue(exception.getMessage().contains("duplicado"));
        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    // ==================== CASOS DE PRUEBA 15: CONFLICTO DE HORARIO ====================

    @Test
    @DisplayName("Debería lanzar una excepción cuando el visitante está registrado en otra actividad con horario que se superpone")
    void deberiaLanzarExcepcionCuandoElVisitanteTieneHorarioConflictivo() {
        HorarioActividad nuevoHorario = HorarioActividad.builder()
                .id(10L)
                .actividad(actividadSafari)
                .fecha(LocalDate.now().plusDays(2))
                .horaInicio(LocalTime.of(10, 0))
                .horaFin(LocalTime.of(12, 0))
                .cuposDisponibles(10)
                .build();

        VisitanteRequest visitante = VisitanteRequest.builder()
                .nombre("Juan Perez")
                .dni("12345678")
                .edad(30)
                .build();

        InscripcionRequest request = InscripcionRequest.builder()
                .horarioActividadId(10L)
                .cantidadPersonas(1)
                .visitantes(List.of(visitante))
                .email("juan@example.com")
                .aceptoTyC(true)
                .build();

        when(horarioRepository.findById(10L)).thenReturn(Optional.of(nuevoHorario));
        when(inscripcionRepository.existsByHorarioAndVisitanteDni(10L, "12345678")).thenReturn(false);
        // Simula conflicto con otra actividad en la misma fecha/hora
        when(inscripcionRepository.existsConflictingScheduleForVisitor(
                "12345678",
                nuevoHorario.getFecha(),
                nuevoHorario.getHoraInicio(),
                nuevoHorario.getHoraFin(),
                nuevoHorario.getId()
        )).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inscripcionService.inscribirActividad(request);
        });

        assertTrue(exception.getMessage().contains("ya está inscripto en otra actividad"));
        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    @DisplayName("Debería registrar exitosamente cuando no hay conflicto de horarios")
    void deberiaRegistrarExitosamenteCuandoNoHayConflictoDeHorarios() {
        HorarioActividad nuevoHorario = HorarioActividad.builder()
                .id(11L)
                .actividad(actividadSafari)
                .fecha(LocalDate.now().plusDays(2))
                .horaInicio(LocalTime.of(10, 0))
                .horaFin(LocalTime.of(12, 0))
                .cuposDisponibles(10)
                .build();

        VisitanteRequest visitante = VisitanteRequest.builder()
                .nombre("Maria Gomez")
                .dni("87654321")
                .edad(25)
                .build();

        InscripcionRequest request = InscripcionRequest.builder()
                .horarioActividadId(11L)
                .cantidadPersonas(1)
                .visitantes(List.of(visitante))
                .email("maria@example.com")
                .aceptoTyC(true)
                .build();

        Visitante visitanteEntity = Visitante.builder()
                .id(1L)
                .nombre("Maria Gomez")
                .dni("87654321")
                .edad(25)
                .build();

        when(horarioRepository.findById(11L)).thenReturn(Optional.of(nuevoHorario));
        when(inscripcionRepository.existsByHorarioAndVisitanteDni(11L, "87654321")).thenReturn(false);
        when(inscripcionRepository.existsConflictingScheduleForVisitor(
                "87654321",
                nuevoHorario.getFecha(),
                nuevoHorario.getHoraInicio(),
                nuevoHorario.getHoraFin(),
                nuevoHorario.getId()
        )).thenReturn(false);
        when(visitanteService.crearVisitante(any(VisitanteRequest.class))).thenReturn(visitanteEntity);
        when(horarioRepository.save(any(HorarioActividad.class))).thenReturn(nuevoHorario);
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(invocation -> {
            Inscripcion inscripcion = invocation.getArgument(0);
            inscripcion.setId(1L);
            return inscripcion;
        });

        InscripcionResponse result = inscripcionService.inscribirActividad(request);

        assertNotNull(result);
        assertEquals("maria@example.com", result.getEmail());
        assertEquals(1, result.getCantidadPersonas());
        verify(inscripcionRepository, times(1)).save(any(Inscripcion.class));
    }

    // ==================== ADDITIONAL EDGE CASES ====================

    @Test
    @DisplayName("Debería lanzar una excepción cuando la cantidad no coincide con el tamaño de la lista de visitantes")
    void deberiaLanzarExcepcionCuandoLaCantidadNoCoincideConLaListaDeVisitantes() {
        InscripcionRequest request = InscripcionRequest.builder()
                .horarioActividadId(1L)
                .cantidadPersonas(3) // Dice 3 personas
                .visitantes(List.of(
                        VisitanteRequest.builder().nombre("Juan").dni("12345678").edad(30).build()
                )) // Pero solo envía 1
                .email("juan@example.com")
                .aceptoTyC(true)
                .build();

        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horarioConCupos));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inscripcionService.inscribirActividad(request);
        });

        assertTrue(exception.getMessage().contains("cantidad de personas no coincide"));
    }

    @Test
    @DisplayName("Debería lanzar una excepción al intentar registrarse en un horario pasado")
    void deberiaLanzarExcepcionAlIntentarRegistrarseEnUnHorarioPasado() {
        HorarioActividad horarioPasado = HorarioActividad.builder()
                .id(1L)
                .actividad(actividadSafari)
                .fecha(LocalDate.now().minusDays(1)) // Ayer
                .horaInicio(LocalTime.of(10, 0))
                .horaFin(LocalTime.of(12, 0))
                .cuposDisponibles(10)
                .build();

        InscripcionRequest request = InscripcionRequest.builder()
                .horarioActividadId(1L)
                .cantidadPersonas(1)
                .visitantes(List.of(VisitanteRequest.builder()
                        .nombre("Juan Perez")
                        .dni("12345678")
                        .edad(30)
                        .build()))
                .email("juan@example.com")
                .aceptoTyC(true)
                .build();

        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horarioPasado));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inscripcionService.inscribirActividad(request);
        });

        assertTrue(exception.getMessage().contains("horario que ya ha pasado"));
    }

    @Test
    @DisplayName("Debería registrar exitosamente a múltiples visitantes")
    void deberiaRegistrarExitosamenteAMultiplesVisitantes() {
        List<VisitanteRequest> visitantes = Arrays.asList(
                VisitanteRequest.builder().nombre("Juan Perez").dni("12345678").edad(30).build(),
                VisitanteRequest.builder().nombre("Maria Gomez").dni("87654321").edad(28).build(),
                VisitanteRequest.builder().nombre("Pedro Lopez").dni("11111111").edad(35).build()
        );

        InscripcionRequest request = InscripcionRequest.builder()
                .horarioActividadId(1L)
                .cantidadPersonas(3)
                .visitantes(visitantes)
                .email("grupo@example.com")
                .aceptoTyC(true)
                .build();

        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horarioConCupos));
        when(inscripcionRepository.existsByHorarioAndVisitanteDni(anyLong(), anyString())).thenReturn(false);
        when(visitanteService.crearVisitante(any(VisitanteRequest.class)))
                .thenReturn(Visitante.builder().id(1L).nombre("Test").dni("12345678").edad(30).build());
        when(horarioRepository.save(any(HorarioActividad.class))).thenReturn(horarioConCupos);
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(invocation -> {
            Inscripcion inscripcion = invocation.getArgument(0);
            inscripcion.setId(1L);
            return inscripcion;
        });

        InscripcionResponse result = inscripcionService.inscribirActividad(request);

        assertNotNull(result);
        assertEquals(3, result.getCantidadPersonas());
        verify(visitanteService, times(3)).crearVisitante(any(VisitanteRequest.class));
        verify(horarioRepository).save(argThat(horario -> horario.getCuposDisponibles() == 7));
    }

    // ==================== TESTS FOR obtenerInscripciones ====================

    @Test
    @DisplayName("Debería devolver todas las inscripciones")
    void deberiaDevolverTodasLasInscripciones() {
        // Arrange
        // Crear visitantes y grupos de prueba
        Visitante visitante1 = Visitante.builder()
                .id(1L)
                .nombre("Juan Perez")
                .dni("12345678")
                .edad(30)
                .build();

        Visitante visitante2 = Visitante.builder()
                .id(2L)
                .nombre("Maria Lopez")
                .dni("87654321")
                .edad(25)
                .build();

        Inscripcion inscripcion1 = Inscripcion.builder()
                .id(1L)
                .cantidadPersonas(2)
                .email("test1@example.com")
                .horarioActividad(horarioConCupos)
                .fechaInscripcion(LocalDateTime.now())
                .build();

        Grupo grupo1 = Grupo.builder()
                .id(1L)
                .visitante(visitante1)
                .inscripcion(inscripcion1)
                .build();

        inscripcion1.setGrupos(List.of(grupo1));

        Inscripcion inscripcion2 = Inscripcion.builder()
                .id(2L)
                .cantidadPersonas(3)
                .email("test2@example.com")
                .horarioActividad(horarioConCupos)
                .fechaInscripcion(LocalDateTime.now())
                .build();

        Grupo grupo2 = Grupo.builder()
                .id(2L)
                .visitante(visitante2)
                .inscripcion(inscripcion2)
                .build();

        inscripcion2.setGrupos(List.of(grupo2));

        when(inscripcionRepository.findAll()).thenReturn(Arrays.asList(inscripcion1, inscripcion2));

        List<InscripcionResponse> result = inscripcionService.obtenerInscripciones();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(inscripcionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debería devolver una lista vacía cuando no existen inscripciones")
    void deberiaDevolverListaVaciaCuandoNoExistenInscripciones() {
        when(inscripcionRepository.findAll()).thenReturn(List.of());

        List<InscripcionResponse> result = inscripcionService.obtenerInscripciones();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(inscripcionRepository, times(1)).findAll();
    }

    // ==================== TESTS PARA obtenerInscripcionPorId ====================

    @Test
    @DisplayName("Debería devolver la inscripción por id cuando existe")
    void deberiaDevolverInscripcionPorId() {
        Visitante visitante = Visitante.builder()
                .id(1L)
                .nombre("Juan Perez")
                .dni("12345678")
                .edad(30)
                .build();

        Inscripcion inscripcion = Inscripcion.builder()
                .id(1L)
                .cantidadPersonas(2)
                .email("test@example.com")
                .horarioActividad(horarioConCupos)
                .fechaInscripcion(LocalDateTime.now())
                .build();

        Grupo grupo = Grupo.builder()
                .id(1L)
                .visitante(visitante)
                .inscripcion(inscripcion)
                .build();

        inscripcion.setGrupos(List.of(grupo));

        when(inscripcionRepository.findById(1L)).thenReturn(Optional.of(inscripcion));

        InscripcionResponse result = inscripcionService.obtenerInscripcionPorId(1L);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(inscripcionRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debería lanzar una excepción cuando no se encuentra la inscripción por id")
    void deberiaLanzarExcepcionCuandoNoSeEncuentraLaInscripcionPorId() {
        when(inscripcionRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inscripcionService.obtenerInscripcionPorId(999L);
        });

        assertTrue(exception.getMessage().contains("No se encontró la inscripción"));
        assertTrue(exception.getMessage().contains("999"));
    }
}
