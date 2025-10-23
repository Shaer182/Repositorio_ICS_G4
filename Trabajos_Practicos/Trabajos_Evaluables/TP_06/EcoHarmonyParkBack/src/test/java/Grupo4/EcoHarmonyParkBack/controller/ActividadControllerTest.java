package Grupo4.EcoHarmonyParkBack.controller;

import Grupo4.EcoHarmonyParkBack.controllers.ActividadController;
import Grupo4.EcoHarmonyParkBack.dtos.ActividadResponse;
import Grupo4.EcoHarmonyParkBack.dtos.HorarioResponse;
import Grupo4.EcoHarmonyParkBack.entities.Actividad;
import Grupo4.EcoHarmonyParkBack.services.ActividadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ActividadController
 * Tests REST API endpoints for activities and schedules
 */
@WebMvcTest(ActividadController.class)
class ActividadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ActividadService actividadService;

    private ActividadResponse actividadResponse1;
    private ActividadResponse actividadResponse2;
    private Actividad actividad;

    @BeforeEach
    void setUp() {
        actividadResponse1 = ActividadResponse.builder()
                .id(1L)
                .nombre("Safari")
                .requiereVestimenta(false)
                .edadMinima(0)
                .cupoMaximo(20)
                .descripcion("Safari por el parque")
                .build();

        actividadResponse2 = ActividadResponse.builder()
                .id(2L)
                .nombre("Tirolesa")
                .requiereVestimenta(true)
                .edadMinima(12)
                .cupoMaximo(15)
                .descripcion("Tirolesa aventura")
                .build();

        actividad = Actividad.builder()
                .id(1L)
                .nombre("Safari")
                .requiereVestimenta(false)
                .edadMinima(0)
                .cupoMaximo(20)
                .descripcion("Safari por el parque")
                .build();
    }

    // ==================== TESTS FOR GET /actividades ====================

    @Test
    @DisplayName("Should return list of all activities with HTTP 200")
    void shouldReturnAllActivities() throws Exception {
        // Arrange
        List<ActividadResponse> actividades = Arrays.asList(actividadResponse1, actividadResponse2);
        when(actividadService.obtenerActividades()).thenReturn(actividades);

        // Act & Assert
        mockMvc.perform(get("/actividades")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].nombre", is("Safari")))
                .andExpect(jsonPath("$[0].requiereVestimenta", is(false)))
                .andExpect(jsonPath("$[0].edadMinima", is(0)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].nombre", is("Tirolesa")))
                .andExpect(jsonPath("$[1].requiereVestimenta", is(true)))
                .andExpect(jsonPath("$[1].edadMinima", is(12)));

        verify(actividadService, times(1)).obtenerActividades();
    }

    @Test
    @DisplayName("Should return empty list when no activities exist")
    void shouldReturnEmptyListWhenNoActivities() throws Exception {
        // Arrange
        when(actividadService.obtenerActividades()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/actividades")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(actividadService, times(1)).obtenerActividades();
    }

    // ==================== TESTS FOR GET /actividades/{actividadId} ====================

    @Test
    @DisplayName("Should return actividad by id with HTTP 200")
    void shouldReturnActividadById() throws Exception {
        // Arrange
        when(actividadService.obtenerActividadPorId(1L)).thenReturn(Optional.of(actividad));

        // Act & Assert
        mockMvc.perform(get("/actividades/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombre", is("Safari")))
                .andExpect(jsonPath("$.descripcion", is("Safari por el parque")))
                .andExpect(jsonPath("$.requiereVestimenta", is(false)))
                .andExpect(jsonPath("$.edadMinima", is(0)))
                .andExpect(jsonPath("$.cupoMaximo", is(20)));

        verify(actividadService, times(1)).obtenerActividadPorId(1L);
    }

    @Test
    @DisplayName("Should throw NoSuchElementException when actividad not found")
    void shouldThrowExceptionWhenActividadNotFound() throws Exception {
        // Arrange
        when(actividadService.obtenerActividadPorId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/actividades/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // 404 - NoSuchElementException handled by GlobalExceptionHandler

        verify(actividadService, times(1)).obtenerActividadPorId(999L);
    }

    @Test
    @DisplayName("Should return actividad with all properties correctly mapped")
    void shouldReturnActividadWithAllProperties() throws Exception {
        // Arrange
        Actividad actividadCompleta = Actividad.builder()
                .id(3L)
                .nombre("Palestra")
                .requiereVestimenta(true)
                .edadMinima(18)
                .cupoMaximo(10)
                .descripcion("Escalada en palestra")
                .terminosCondiciones("Debe firmar descargo")
                .build();

        when(actividadService.obtenerActividadPorId(3L)).thenReturn(Optional.of(actividadCompleta));

        // Act & Assert
        mockMvc.perform(get("/actividades/3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.nombre", is("Palestra")))
                .andExpect(jsonPath("$.requiereVestimenta", is(true)))
                .andExpect(jsonPath("$.edadMinima", is(18)));
    }

    // ==================== TESTS FOR GET /actividades/{actividadId}/horarios ====================

    @Test
    @DisplayName("Should return schedules for actividad and date with HTTP 200")
    void shouldReturnHorariosForActividadAndDate() throws Exception {
        // Arrange
        LocalDate fecha = LocalDate.of(2025, 11, 15);

        HorarioResponse horario1 = HorarioResponse.builder()
                .id(1L)
                .fecha(fecha)
                .horaInicio(LocalTime.of(10, 0))
                .horaFin(LocalTime.of(12, 0))
                .cuposDisponibles(15)
                .build();

        HorarioResponse horario2 = HorarioResponse.builder()
                .id(2L)
                .fecha(fecha)
                .horaInicio(LocalTime.of(14, 0))
                .horaFin(LocalTime.of(16, 0))
                .cuposDisponibles(10)
                .build();

        List<HorarioResponse> horarios = Arrays.asList(horario1, horario2);

        when(actividadService.obtenerHorarios(1L, fecha)).thenReturn(horarios);

        // Act & Assert
        mockMvc.perform(get("/actividades/1/horarios")
                        .param("fecha", "2025-11-15")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].horaInicio", is("10:00:00")))
                .andExpect(jsonPath("$[0].horaFin", is("12:00:00")))
                .andExpect(jsonPath("$[0].cuposDisponibles", is(15)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].horaInicio", is("14:00:00")))
                .andExpect(jsonPath("$[1].cuposDisponibles", is(10)));

        verify(actividadService, times(1)).obtenerHorarios(1L, fecha);
    }

    @Test
    @DisplayName("Should return empty list when no schedules for date")
    void shouldReturnEmptyListWhenNoSchedulesForDate() throws Exception {
        // Arrange
        LocalDate fecha = LocalDate.of(2025, 12, 25);
        when(actividadService.obtenerHorarios(1L, fecha)).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/actividades/1/horarios")
                        .param("fecha", "2025-12-25")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(actividadService, times(1)).obtenerHorarios(1L, fecha);
    }

    @Test
    @DisplayName("Should return error when fecha parameter is missing")
    void shouldReturnErrorWhenFechaParameterMissing() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/actividades/1/horarios")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(actividadService, never()).obtenerHorarios(anyLong(), any(LocalDate.class));
    }

    @Test
    @DisplayName("Should return error when fecha is in invalid format")
    void shouldReturnErrorWhenFechaInvalidFormat() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/actividades/1/horarios")
                        .param("fecha", "invalid-date")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(actividadService, never()).obtenerHorarios(anyLong(), any(LocalDate.class));
    }

    @Test
    @DisplayName("Should return error when service throws InvalidParameterException for past date")
    void shouldReturnErrorWhenDateInPast() throws Exception {
        // Arrange
        LocalDate pastDate = LocalDate.of(2020, 1, 1);
        when(actividadService.obtenerHorarios(1L, pastDate))
                .thenThrow(new InvalidParameterException("La fecha debe ser igual o posterior a la actual."));

        // Act & Assert
        mockMvc.perform(get("/actividades/1/horarios")
                        .param("fecha", "2020-01-01")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()); // 400 - InvalidParameterException extends IllegalArgumentException

        verify(actividadService, times(1)).obtenerHorarios(1L, pastDate);
    }

    @Test
    @DisplayName("Should return error when actividad does not exist")
    void shouldReturnErrorWhenActividadNotFoundForHorarios() throws Exception {
        // Arrange
        LocalDate fecha = LocalDate.of(2025, 11, 15);
        when(actividadService.obtenerHorarios(999L, fecha))
                .thenThrow(new InvalidParameterException("No se encontró la actividad."));

        // Act & Assert
        mockMvc.perform(get("/actividades/999/horarios")
                        .param("fecha", "2025-11-15")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()); // 400 - InvalidParameterException extends IllegalArgumentException

        verify(actividadService, times(1)).obtenerHorarios(999L, fecha);
    }

    @Test
    @DisplayName("Should accept today's date as valid parameter")
    void shouldAcceptTodayDate() throws Exception {
        // Arrange
        LocalDate today = LocalDate.now();
        when(actividadService.obtenerHorarios(1L, today)).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/actividades/1/horarios")
                        .param("fecha", today.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(actividadService, times(1)).obtenerHorarios(1L, today);
    }

    @Test
    @DisplayName("Should handle multiple schedules with different times")
    void shouldHandleMultipleSchedulesWithDifferentTimes() throws Exception {
        // Arrange
        LocalDate fecha = LocalDate.of(2025, 11, 20);

        List<HorarioResponse> horarios = Arrays.asList(
                HorarioResponse.builder()
                        .id(1L)
                        .fecha(fecha)
                        .horaInicio(LocalTime.of(8, 0))
                        .horaFin(LocalTime.of(10, 0))
                        .cuposDisponibles(5)
                        .build(),
                HorarioResponse.builder()
                        .id(2L)
                        .fecha(fecha)
                        .horaInicio(LocalTime.of(10, 30))
                        .horaFin(LocalTime.of(12, 30))
                        .cuposDisponibles(8)
                        .build(),
                HorarioResponse.builder()
                        .id(3L)
                        .fecha(fecha)
                        .horaInicio(LocalTime.of(14, 0))
                        .horaFin(LocalTime.of(16, 0))
                        .cuposDisponibles(12)
                        .build()
        );

        when(actividadService.obtenerHorarios(1L, fecha)).thenReturn(horarios);

        // Act & Assert
        mockMvc.perform(get("/actividades/1/horarios")
                        .param("fecha", "2025-11-20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].horaInicio", is("08:00:00")))
                .andExpect(jsonPath("$[1].horaInicio", is("10:30:00")))
                .andExpect(jsonPath("$[2].horaInicio", is("14:00:00")));
    }

    @Test
    @DisplayName("Should return schedules with zero available slots")
    void shouldReturnSchedulesWithZeroSlots() throws Exception {
        // Arrange
        LocalDate fecha = LocalDate.of(2025, 11, 15);

        HorarioResponse horarioLleno = HorarioResponse.builder()
                .id(1L)
                .fecha(fecha)
                .horaInicio(LocalTime.of(10, 0))
                .horaFin(LocalTime.of(12, 0))
                .cuposDisponibles(0) // Sin cupos
                .build();

        when(actividadService.obtenerHorarios(1L, fecha)).thenReturn(List.of(horarioLleno));

        // Act & Assert
        mockMvc.perform(get("/actividades/1/horarios")
                        .param("fecha", "2025-11-15")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cuposDisponibles", is(0)));
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("Should handle long actividad id correctly")
    void shouldHandleLongActividadId() throws Exception {
        // Arrange
        Long longId = 999999999L;
        when(actividadService.obtenerActividadPorId(longId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/actividades/" + longId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // 404 - NoSuchElementException
    }

    @Test
    @DisplayName("Should reject negative actividad id")
    void shouldRejectNegativeActividadId() throws Exception {
        // Arrange
        when(actividadService.obtenerActividadPorId(-1L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/actividades/-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // 404 - NoSuchElementException
    }
}
