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
 * Unit tests for InscripcionService
 * Tests all business logic validation scenarios for activity registration
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
    private Actividad actividadSafari;
    private Actividad actividadTirolesa;
    private Actividad actividadPalestra;

    @BeforeEach
    void setUp() {
        // Actividad sin restricciones especiales (Safari)
        actividadSafari = Actividad.builder()
                .id(1L)
                .nombre("Safari")
                .requiereVestimenta(false)
                .edadMinima(0)
                .cupoMaximo(20)
                .descripcion("Safari por el parque")
                .build();

        // Actividad que requiere vestimenta (Tirolesa)
        actividadTirolesa = Actividad.builder()
                .id(2L)
                .nombre("Tirolesa")
                .requiereVestimenta(true)
                .edadMinima(12)
                .cupoMaximo(15)
                .descripcion("Tirolesa aventura")
                .build();

        // Actividad con edad minima 18 (Palestra)
        actividadPalestra = Actividad.builder()
                .id(3L)
                .nombre("Palestra")
                .requiereVestimenta(true)
                .edadMinima(18)
                .cupoMaximo(10)
                .descripcion("Escalada en palestra")
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
    @DisplayName("Should successfully register when all data is valid and slots are available")
    void shouldRegisterSuccessfullyWhenValidData() {
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

        // Mock repository behaviors
        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horarioConCupos));
        when(inscripcionRepository.existsByHorarioAndVisitanteDni(1L, "12345678")).thenReturn(false);
        when(visitanteService.crearVisitante(any(VisitanteRequest.class))).thenReturn(visitanteEntity);
        when(horarioRepository.save(any(HorarioActividad.class))).thenReturn(horarioConCupos);
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(invocation -> {
            Inscripcion inscripcion = invocation.getArgument(0);
            inscripcion.setId(1L);
            return inscripcion;
        });

        // Act
        InscripcionResponse result = inscripcionService.inscribirActividad(request);

        // Assert
        assertNotNull(result);
        assertEquals("juan@example.com", result.getEmail());
        assertEquals(1, result.getCantidadPersonas());

        // Verify cupos were decremented
        verify(horarioRepository).save(argThat(horario ->
            horario.getCuposDisponibles() == 9
        ));

        // Verify visitante was created
        verify(visitanteService, times(1)).crearVisitante(any(VisitanteRequest.class));

        // Verify inscripcion was saved
        verify(inscripcionRepository, times(1)).save(any(Inscripcion.class));
    }

    // ==================== CASO DE PRUEBA 2: DATOS FALTANTES (Jakarta Validation) ====================
    // Note: Jakarta validation is handled at controller level with @Valid
    // Service layer tests assume data passed validation

    @Test
    @DisplayName("Should throw exception when horario does not exist")
    void shouldThrowExceptionWhenHorarioNotFound() {
        // Arrange
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

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inscripcionService.inscribirActividad(request);
        });

        assertEquals("Horario no encontrado", exception.getMessage());
        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    // ==================== CASO DE PRUEBA 3: INSCRIPCION DUPLICADA ====================

    @Test
    @DisplayName("Should throw exception when visitor is already registered for the same schedule")
    void shouldThrowExceptionWhenVisitorAlreadyRegistered() {
        // Arrange
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

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inscripcionService.inscribirActividad(request);
        });

        assertTrue(exception.getMessage().contains("ya está inscripto en este horario"));
        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
        verify(horarioRepository, never()).save(any(HorarioActividad.class));
    }

    // ==================== CASO DE PRUEBA 4: HORARIO CON CUPOS LLENOS ====================

    @Test
    @DisplayName("Should throw exception when schedule has no available slots")
    void shouldThrowExceptionWhenNoSlotsAvailable() {
        // Arrange
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

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inscripcionService.inscribirActividad(request);
        });

        assertTrue(exception.getMessage().contains("No hay cupos suficientes"));
        assertTrue(exception.getMessage().contains("Cupos disponibles: 0"));
        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    // ==================== CASO DE PRUEBA 5: HORARIO SIN CUPOS SUFICIENTES ====================

    @Test
    @DisplayName("Should throw exception when requested slots exceed available slots")
    void shouldThrowExceptionWhenInsufficientSlots() {
        // Arrange
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

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inscripcionService.inscribirActividad(request);
        });

        assertTrue(exception.getMessage().contains("No hay cupos suficientes"));
        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    // ==================== CASO DE PRUEBA 6: NO ACEPTAR TERMINOS Y CONDICIONES ====================

    @Test
    @DisplayName("Should throw exception when terms and conditions are not accepted")
    void shouldThrowExceptionWhenTermsNotAccepted() {
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

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inscripcionService.inscribirActividad(request);
        });

        assertEquals("Debe aceptar los términos y condiciones.", exception.getMessage());
        verify(horarioRepository, never()).findById(anyLong());
        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    // ==================== CASO DE PRUEBA 7: NO INGRESAR TALLA DE ROPA ====================

    @Test
    @DisplayName("Should throw exception when clothing size is required but not provided")
    void shouldThrowExceptionWhenClothingSizeRequired() {
        // Arrange
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

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inscripcionService.inscribirActividad(request);
        });

        assertTrue(exception.getMessage().contains("requiere talla de vestimenta"));
        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    @DisplayName("Should throw exception when clothing size is empty string")
    void shouldThrowExceptionWhenClothingSizeEmpty() {
        // Arrange
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

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inscripcionService.inscribirActividad(request);
        });

        assertTrue(exception.getMessage().contains("requiere talla de vestimenta"));
    }

    // ==================== CASO DE PRUEBA 8: MENOR DE EDAD ====================

    @Test
    @DisplayName("Should throw exception when visitor is under minimum age")
    void shouldThrowExceptionWhenVisitorUnderAge() {
        // Arrange
        HorarioActividad horarioPalestra = HorarioActividad.builder()
                .id(3L)
                .actividad(actividadPalestra) // Edad minima 18
                .fecha(LocalDate.now().plusDays(7))
                .horaInicio(LocalTime.of(14, 0))
                .horaFin(LocalTime.of(16, 0))
                .cuposDisponibles(5)
                .build();

        VisitanteRequest visitanteMenor = VisitanteRequest.builder()
                .nombre("Pedro Martinez")
                .dni("11111111")
                .edad(10) // Menor de 18
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

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inscripcionService.inscribirActividad(request);
        });

        assertTrue(exception.getMessage().contains("tiene 10 años"));
        assertTrue(exception.getMessage().contains("edad mínima requerida es 18 años"));
        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    // ==================== CASO DE PRUEBA 14: DNI DUPLICADO EN EL MISMO REQUEST ====================

    @Test
    @DisplayName("Should throw exception when same DNI appears multiple times in request")
    void shouldThrowExceptionWhenDuplicateDniInRequest() {
        // Arrange
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

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inscripcionService.inscribirActividad(request);
        });

        assertTrue(exception.getMessage().contains("DNI 12345678"));
        assertTrue(exception.getMessage().contains("duplicado"));
        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    // ==================== ADDITIONAL EDGE CASES ====================

    @Test
    @DisplayName("Should throw exception when quantity does not match visitors list size")
    void shouldThrowExceptionWhenQuantityMismatch() {
        // Arrange
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

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inscripcionService.inscribirActividad(request);
        });

        assertTrue(exception.getMessage().contains("cantidad de personas no coincide"));
    }

    @Test
    @DisplayName("Should throw exception when trying to register for past schedule")
    void shouldThrowExceptionWhenScheduleInPast() {
        // Arrange
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

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inscripcionService.inscribirActividad(request);
        });

        assertTrue(exception.getMessage().contains("horario que ya ha pasado"));
    }

    @Test
    @DisplayName("Should successfully register multiple visitors")
    void shouldRegisterMultipleVisitorsSuccessfully() {
        // Arrange
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

        // Act
        InscripcionResponse result = inscripcionService.inscribirActividad(request);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getCantidadPersonas());
        verify(visitanteService, times(3)).crearVisitante(any(VisitanteRequest.class));
        verify(horarioRepository).save(argThat(horario -> horario.getCuposDisponibles() == 7));
    }

    // ==================== TESTS FOR obtenerInscripciones ====================

    @Test
    @DisplayName("Should return all inscriptions")
    void shouldReturnAllInscriptions() {
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

        // Act
        List<InscripcionResponse> result = inscripcionService.obtenerInscripciones();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(inscripcionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no inscriptions exist")
    void shouldReturnEmptyListWhenNoInscriptions() {
        // Arrange
        when(inscripcionRepository.findAll()).thenReturn(List.of());

        // Act
        List<InscripcionResponse> result = inscripcionService.obtenerInscripciones();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(inscripcionRepository, times(1)).findAll();
    }

    // ==================== TESTS FOR obtenerInscripcionPorId ====================

    @Test
    @DisplayName("Should return inscription by id when it exists")
    void shouldReturnInscriptionById() {
        // Arrange
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

        // Act
        InscripcionResponse result = inscripcionService.obtenerInscripcionPorId(1L);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(inscripcionRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when inscription not found by id")
    void shouldThrowExceptionWhenInscriptionNotFoundById() {
        // Arrange
        when(inscripcionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inscripcionService.obtenerInscripcionPorId(999L);
        });

        assertTrue(exception.getMessage().contains("No se encontró la inscripción"));
        assertTrue(exception.getMessage().contains("999"));
    }

    // ==================== CASOS DE PRUEBA: CONFLICTO DE HORARIO ====================

    @Test
    @DisplayName("Should throw exception when visitor is registered in another activity with overlapping schedule")
    void shouldThrowExceptionWhenVisitorHasConflictingSchedule() {
        // Arrange
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
        // 👇 Simula conflicto con otra actividad en la misma fecha/hora
        when(inscripcionRepository.existsConflictingScheduleForVisitor(
                "12345678",
                nuevoHorario.getFecha(),
                nuevoHorario.getHoraInicio(),
                nuevoHorario.getHoraFin(),
                nuevoHorario.getId()
        )).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inscripcionService.inscribirActividad(request);
        });

        assertTrue(exception.getMessage().contains("ya está inscripto en otra actividad"));
        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    @DisplayName("Should register successfully when there is no conflicting schedule")
    void shouldRegisterSuccessfullyWhenNoScheduleConflict() {
        // Arrange
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

        // Act
        InscripcionResponse result = inscripcionService.inscribirActividad(request);

        // Assert
        assertNotNull(result);
        assertEquals("maria@example.com", result.getEmail());
        assertEquals(1, result.getCantidadPersonas());
        verify(inscripcionRepository, times(1)).save(any(Inscripcion.class));
    }
}
