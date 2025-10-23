package Grupo4.EcoHarmonyParkBack.service;

import Grupo4.EcoHarmonyParkBack.dtos.ActividadResponse;
import Grupo4.EcoHarmonyParkBack.dtos.HorarioResponse;
import Grupo4.EcoHarmonyParkBack.entities.Actividad;
import Grupo4.EcoHarmonyParkBack.entities.HorarioActividad;
import Grupo4.EcoHarmonyParkBack.repositories.ActividadRepository;
import Grupo4.EcoHarmonyParkBack.repositories.HorarioActividadRepository;
import Grupo4.EcoHarmonyParkBack.services.ActividadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ActividadService
 * Tests business logic for activities and schedules
 */
@ExtendWith(MockitoExtension.class)
class ActividadServiceTest {

    @Mock
    private ActividadRepository actividadRepository;

    @Mock
    private HorarioActividadRepository horarioRepository;

    @InjectMocks
    private ActividadService actividadService;

    private Actividad actividadSafari;
    private Actividad actividadTirolesa;
    private Actividad actividadPalestra;

    @BeforeEach
    void setUp() {
        actividadSafari = Actividad.builder()
                .id(1L)
                .nombre("Safari")
                .requiereVestimenta(false)
                .edadMinima(0)
                .cupoMaximo(20)
                .descripcion("Safari por el parque")
                .build();

        actividadTirolesa = Actividad.builder()
                .id(2L)
                .nombre("Tirolesa")
                .requiereVestimenta(true)
                .edadMinima(12)
                .cupoMaximo(15)
                .descripcion("Tirolesa aventura")
                .build();

        actividadPalestra = Actividad.builder()
                .id(3L)
                .nombre("Palestra")
                .requiereVestimenta(true)
                .edadMinima(18)
                .cupoMaximo(10)
                .descripcion("Escalada en palestra")
                .build();
    }

    // ==================== TESTS FOR obtenerActividades ====================

    @Test
    @DisplayName("Should return all activities sorted by name")
    void shouldReturnAllActivitiesSortedByName() {
        // Arrange - actividades en orden no alfabético
        List<Actividad> actividades = Arrays.asList(
                actividadTirolesa,  // T
                actividadSafari,    // S
                actividadPalestra   // P
        );

        when(actividadRepository.findAll()).thenReturn(actividades);

        // Act
        List<ActividadResponse> result = actividadService.obtenerActividades();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());

        // Verificar que están ordenadas alfabéticamente
        assertEquals("Palestra", result.get(0).getNombre());
        assertEquals("Safari", result.get(1).getNombre());
        assertEquals("Tirolesa", result.get(2).getNombre());

        verify(actividadRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no activities exist")
    void shouldReturnEmptyListWhenNoActivities() {
        // Arrange
        when(actividadRepository.findAll()).thenReturn(List.of());

        // Act
        List<ActividadResponse> result = actividadService.obtenerActividades();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(actividadRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should map actividad properties to response correctly")
    void shouldMapActividadPropertiesToResponse() {
        // Arrange
        when(actividadRepository.findAll()).thenReturn(List.of(actividadTirolesa));

        // Act
        List<ActividadResponse> result = actividadService.obtenerActividades();

        // Assert
        assertEquals(1, result.size());
        ActividadResponse response = result.get(0);
        assertEquals(2L, response.getId());
        assertEquals("Tirolesa", response.getNombre());
        assertEquals(true, response.isRequiereVestimenta());
        assertEquals(12, response.getEdadMinima());
        assertEquals("Tirolesa aventura", response.getDescripcion());
    }

    // ==================== TESTS FOR obtenerHorarios ====================

    @Test
    @DisplayName("Should return schedules for future date sorted by start time")
    void shouldReturnSchedulesForFutureDate() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusDays(7);

        HorarioActividad horario1 = HorarioActividad.builder()
                .id(1L)
                .actividad(actividadSafari)
                .fecha(futureDate)
                .horaInicio(LocalTime.of(14, 0))
                .horaFin(LocalTime.of(16, 0))
                .cuposDisponibles(10)
                .build();

        HorarioActividad horario2 = HorarioActividad.builder()
                .id(2L)
                .actividad(actividadSafari)
                .fecha(futureDate)
                .horaInicio(LocalTime.of(10, 0))
                .horaFin(LocalTime.of(12, 0))
                .cuposDisponibles(15)
                .build();

        // Horarios desordenados (14:00 antes que 10:00)
        List<HorarioActividad> horarios = Arrays.asList(horario1, horario2);

        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividadSafari));
        when(horarioRepository.findByActividadAndFecha(actividadSafari, futureDate)).thenReturn(horarios);

        // Act
        List<HorarioResponse> result = actividadService.obtenerHorarios(1L, futureDate);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verificar que están ordenados por hora de inicio
        assertEquals(LocalTime.of(10, 0), result.get(0).getHoraInicio());
        assertEquals(LocalTime.of(14, 0), result.get(1).getHoraInicio());

        verify(actividadRepository, times(1)).findById(1L);
        verify(horarioRepository, times(1)).findByActividadAndFecha(actividadSafari, futureDate);
    }

    @Test
    @DisplayName("Should filter out past schedules when date is today")
    void shouldFilterPastSchedulesWhenDateIsToday() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        // Horario en el pasado (2 horas antes)
        HorarioActividad horarioPasado = HorarioActividad.builder()
                .id(1L)
                .actividad(actividadSafari)
                .fecha(today)
                .horaInicio(currentTime.minusHours(2))
                .horaFin(currentTime.minusHours(1))
                .cuposDisponibles(10)
                .build();

        // Horario futuro (2 horas después)
        HorarioActividad horarioFuturo = HorarioActividad.builder()
                .id(2L)
                .actividad(actividadSafari)
                .fecha(today)
                .horaInicio(currentTime.plusHours(2))
                .horaFin(currentTime.plusHours(4))
                .cuposDisponibles(15)
                .build();

        List<HorarioActividad> horarios = Arrays.asList(horarioPasado, horarioFuturo);

        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividadSafari));
        when(horarioRepository.findByActividadAndFecha(actividadSafari, today)).thenReturn(horarios);

        // Act
        List<HorarioResponse> result = actividadService.obtenerHorarios(1L, today);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
        assertTrue(result.get(0).getHoraInicio().isAfter(currentTime));
    }

    @Test
    @DisplayName("Should include current hour schedule when date is today")
    void shouldIncludeCurrentHourSchedule() {
        // Arrange
        LocalDate today = LocalDate.now();
        // Usar una hora en el futuro para evitar problemas de timing
        LocalTime futureTime = LocalTime.now().plusMinutes(30);

        HorarioActividad horarioActual = HorarioActividad.builder()
                .id(1L)
                .actividad(actividadSafari)
                .fecha(today)
                .horaInicio(futureTime) // Hora en el futuro cercano
                .horaFin(futureTime.plusHours(2))
                .cuposDisponibles(10)
                .build();

        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividadSafari));
        when(horarioRepository.findByActividadAndFecha(actividadSafari, today))
                .thenReturn(List.of(horarioActual));

        // Act
        List<HorarioResponse> result = actividadService.obtenerHorarios(1L, today);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should throw exception when fecha is null")
    void shouldThrowExceptionWhenFechaIsNull() {
        // Act & Assert
        InvalidParameterException exception = assertThrows(InvalidParameterException.class, () -> {
            actividadService.obtenerHorarios(1L, null);
        });

        assertEquals("La fecha es obligatoria.", exception.getMessage());
        verify(actividadRepository, never()).findById(anyLong());
        verify(horarioRepository, never()).findByActividadAndFecha(any(), any());
    }

    @Test
    @DisplayName("Should throw exception when fecha is in the past")
    void shouldThrowExceptionWhenFechaIsInPast() {
        // Arrange
        LocalDate pastDate = LocalDate.now().minusDays(1);

        // Act & Assert
        InvalidParameterException exception = assertThrows(InvalidParameterException.class, () -> {
            actividadService.obtenerHorarios(1L, pastDate);
        });

        assertTrue(exception.getMessage().contains("fecha debe ser igual o posterior a la actual"));
        verify(actividadRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Should throw exception when actividad does not exist")
    void shouldThrowExceptionWhenActividadNotFound() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusDays(7);
        when(actividadRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        InvalidParameterException exception = assertThrows(InvalidParameterException.class, () -> {
            actividadService.obtenerHorarios(999L, futureDate);
        });

        assertEquals("No se encontró la actividad.", exception.getMessage());
        verify(horarioRepository, never()).findByActividadAndFecha(any(), any());
    }

    @Test
    @DisplayName("Should return empty list when no schedules exist for date")
    void shouldReturnEmptyListWhenNoSchedulesForDate() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusDays(7);

        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividadSafari));
        when(horarioRepository.findByActividadAndFecha(actividadSafari, futureDate))
                .thenReturn(List.of());

        // Act
        List<HorarioResponse> result = actividadService.obtenerHorarios(1L, futureDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should map horario properties to response correctly")
    void shouldMapHorarioPropertiesToResponse() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusDays(7);

        HorarioActividad horario = HorarioActividad.builder()
                .id(1L)
                .actividad(actividadSafari)
                .fecha(futureDate)
                .horaInicio(LocalTime.of(10, 0))
                .horaFin(LocalTime.of(12, 0))
                .cuposDisponibles(15)
                .build();

        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividadSafari));
        when(horarioRepository.findByActividadAndFecha(actividadSafari, futureDate))
                .thenReturn(List.of(horario));

        // Act
        List<HorarioResponse> result = actividadService.obtenerHorarios(1L, futureDate);

        // Assert
        assertEquals(1, result.size());
        HorarioResponse response = result.get(0);
        assertEquals(1L, response.getId());
        assertEquals(futureDate, response.getFecha());
        assertEquals(LocalTime.of(10, 0), response.getHoraInicio());
        assertEquals(LocalTime.of(12, 0), response.getHoraFin());
        assertEquals(15, response.getCuposDisponibles());
    }

    // ==================== TESTS FOR obtenerActividadPorId ====================

    @Test
    @DisplayName("Should return actividad when id exists")
    void shouldReturnActividadWhenIdExists() {
        // Arrange
        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividadSafari));

        // Act
        Optional<Actividad> result = actividadService.obtenerActividadPorId(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Safari", result.get().getNombre());
        verify(actividadRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return empty optional when actividad not found")
    void shouldReturnEmptyOptionalWhenActividadNotFound() {
        // Arrange
        when(actividadRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Actividad> result = actividadService.obtenerActividadPorId(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(actividadRepository, times(1)).findById(999L);
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("Should handle multiple schedules on same day correctly")
    void shouldHandleMultipleSchedulesOnSameDay() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusDays(7);

        List<HorarioActividad> horarios = Arrays.asList(
                HorarioActividad.builder()
                        .id(1L)
                        .actividad(actividadSafari)
                        .fecha(futureDate)
                        .horaInicio(LocalTime.of(8, 0))
                        .horaFin(LocalTime.of(10, 0))
                        .cuposDisponibles(5)
                        .build(),
                HorarioActividad.builder()
                        .id(2L)
                        .actividad(actividadSafari)
                        .fecha(futureDate)
                        .horaInicio(LocalTime.of(12, 0))
                        .horaFin(LocalTime.of(14, 0))
                        .cuposDisponibles(8)
                        .build(),
                HorarioActividad.builder()
                        .id(3L)
                        .actividad(actividadSafari)
                        .fecha(futureDate)
                        .horaInicio(LocalTime.of(16, 0))
                        .horaFin(LocalTime.of(18, 0))
                        .cuposDisponibles(10)
                        .build()
        );

        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividadSafari));
        when(horarioRepository.findByActividadAndFecha(actividadSafari, futureDate))
                .thenReturn(horarios);

        // Act
        List<HorarioResponse> result = actividadService.obtenerHorarios(1L, futureDate);

        // Assert
        assertEquals(3, result.size());
        assertEquals(LocalTime.of(8, 0), result.get(0).getHoraInicio());
        assertEquals(LocalTime.of(12, 0), result.get(1).getHoraInicio());
        assertEquals(LocalTime.of(16, 0), result.get(2).getHoraInicio());
    }

    @Test
    @DisplayName("Should accept today's date when valid")
    void shouldAcceptTodayDateWhenValid() {
        // Arrange
        LocalDate today = LocalDate.now();

        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividadSafari));
        when(horarioRepository.findByActividadAndFecha(actividadSafari, today))
                .thenReturn(List.of());

        // Act
        List<HorarioResponse> result = actividadService.obtenerHorarios(1L, today);

        // Assert
        assertNotNull(result);
        verify(actividadRepository, times(1)).findById(1L);
    }
}
