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
 * Pruebas unitarias para ActividadService
 * Prueban la lógica de negocio para actividades y horarios
 */
@ExtendWith(MockitoExtension.class)
class ActividadServiceTest {

    @Mock
    private ActividadRepository actividadRepository;

    @Mock
    private HorarioActividadRepository horarioRepository;

    @InjectMocks
    private ActividadService actividadService;

    private Actividad actividadJardineria;
    private Actividad actividadPalestra;
    private Actividad actividadSafari;
    private Actividad actividadTirolesa;

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
    }


    // ==================== TESTS PARA obtenerActividades ====================

    @Test
    @DisplayName("Debería devolver todas las actividades ordenadas por nombre")
    void deberiaDevolverTodasLasActividadesOrdenadasPorNombre() {
        // Actividades (sin orden específico)
        List<Actividad> actividades = Arrays.asList(
                actividadTirolesa,   // T
                actividadSafari,     // S
                actividadPalestra,   // P
                actividadJardineria  // J
        );

        when(actividadRepository.findAll()).thenReturn(actividades);

        List<ActividadResponse> result = actividadService.obtenerActividades();

        assertNotNull(result);
        assertEquals(4, result.size());

        // Verificar que están ordenadas alfabéticamente
        assertEquals("Jardinería", result.get(0).getNombre());
        assertEquals("Palestra", result.get(1).getNombre());
        assertEquals("Safari", result.get(2).getNombre());
        assertEquals("Tirolesa", result.get(3).getNombre());

        verify(actividadRepository, times(1)).findAll();
    }


    @Test
    @DisplayName("Debería devolver una lista vacía cuando no existen actividades")
    void deberiaDevolverListaVaciaCuandoNoExistenActividades() {
        when(actividadRepository.findAll()).thenReturn(List.of());

        List<ActividadResponse> result = actividadService.obtenerActividades();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(actividadRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debería mapear correctamente las propiedades de la actividad a la respuesta")
    void deberiaMapearCorrectamenteLasPropiedadesDeLaActividadALaRespuesta() {
        when(actividadRepository.findAll()).thenReturn(List.of(actividadTirolesa));

        List<ActividadResponse> result = actividadService.obtenerActividades();

        assertEquals(1, result.size());
        ActividadResponse response = result.get(0);
        assertEquals(1L, response.getId());
        assertEquals("Tirolesa", response.getNombre());
        assertTrue(response.isRequiereVestimenta());
        assertEquals(12, response.getEdadMinima());
        assertEquals("Recorrido aéreo por cable con arnés de seguridad y casco obligatorio.", response.getDescripcion());
        assertEquals(10, response.getCupoMaximo());
        assertEquals("El participante debe usar el equipo de seguridad completo y seguir las instrucciones del guía.", response.getTerminosCondiciones());
    }


    // ==================== TESTS PARA obtenerHorarios ====================

    @Test
    @DisplayName("Debería devolver los horarios para una fecha futura ordenados por hora de inicio")
    void deberiaDevolverHorariosParaFechaFutura() {
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

        List<HorarioResponse> result = actividadService.obtenerHorarios(1L, futureDate);

        assertNotNull(result);
        assertEquals(2, result.size());

        // Verificar que están ordenados por hora de inicio
        assertEquals(LocalTime.of(10, 0), result.get(0).getHoraInicio());
        assertEquals(LocalTime.of(14, 0), result.get(1).getHoraInicio());

        verify(actividadRepository, times(1)).findById(1L);
        verify(horarioRepository, times(1)).findByActividadAndFecha(actividadSafari, futureDate);
    }

    @Test
    @DisplayName("Debería filtrar los horarios pasados cuando la fecha es hoy")
    void deberiaFiltrarHorariosPasadosCuandoLaFechaEsHoy() {
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

        List<HorarioResponse> result = actividadService.obtenerHorarios(1L, today);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
        assertTrue(result.get(0).getHoraInicio().isAfter(currentTime));
    }

    @Test
    @DisplayName("Debe incluir el horario de la hora actual cuando la fecha es hoy")
    void debeIncluirHorarioDeLaHoraActual() {
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

        List<HorarioResponse> result = actividadService.obtenerHorarios(1L, today);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Debe lanzar una excepción cuando la fecha es nula")
    void debeLanzarExcepcionCuandoLaFechaEsNula() {
        InvalidParameterException exception = assertThrows(InvalidParameterException.class, () -> {
            actividadService.obtenerHorarios(1L, null);
        });

        assertEquals("La fecha es obligatoria.", exception.getMessage());
        verify(actividadRepository, never()).findById(anyLong());
        verify(horarioRepository, never()).findByActividadAndFecha(any(), any());
    }

    @Test
    @DisplayName("Debe lanzar una excepción cuando la fecha es en el pasado")
    void debeLanzarExcepcionCuandoLaFechaEsEnElPasado() {
        LocalDate pastDate = LocalDate.now().minusDays(1);

        InvalidParameterException exception = assertThrows(InvalidParameterException.class, () -> {
            actividadService.obtenerHorarios(1L, pastDate);
        });

        assertTrue(exception.getMessage().contains("fecha debe ser igual o posterior a la actual"));
        verify(actividadRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Debe lanzar una excepción cuando la actividad no existe")
    void debeLanzarExcepcionCuandoActividadNoEncontrada() {

        LocalDate futureDate = LocalDate.now().plusDays(7);
        when(actividadRepository.findById(999L)).thenReturn(Optional.empty());

        InvalidParameterException exception = assertThrows(InvalidParameterException.class, () -> {
            actividadService.obtenerHorarios(999L, futureDate);
        });

        assertEquals("No se encontró la actividad.", exception.getMessage());
        verify(horarioRepository, never()).findByActividadAndFecha(any(), any());
    }

    @Test
    @DisplayName("Debe retornar una lista vacía cuando no existen horarios para la fecha")
    void debeRetornarListaVaciaCuandoNoExistenHorariosParaLaFecha() {
        LocalDate futureDate = LocalDate.now().plusDays(7);

        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividadSafari));
        when(horarioRepository.findByActividadAndFecha(actividadSafari, futureDate))
                .thenReturn(List.of());

        List<HorarioResponse> result = actividadService.obtenerHorarios(1L, futureDate);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debe mapear correctamente las propiedades del horario a la respuesta")
    void debeMapearPropiedadesDelHorarioALaRespuesta() {
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

        List<HorarioResponse> result = actividadService.obtenerHorarios(1L, futureDate);

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
    @DisplayName("Debe retornar la actividad cuando el id existe")
    void debeRetornarActividadCuandoIdExiste() {
        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividadSafari));

        Optional<Actividad> result = actividadService.obtenerActividadPorId(1L);

        assertTrue(result.isPresent());
        assertEquals("Safari", result.get().getNombre());
        verify(actividadRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe retornar un optional vacío cuando la actividad no se encuentra")
    void debeRetornarOptionalVacioCuandoActividadNoEncontrada() {
        when(actividadRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Actividad> result = actividadService.obtenerActividadPorId(999L);

        assertFalse(result.isPresent());
        verify(actividadRepository, times(1)).findById(999L);
    }

    // ==================== Casos extremos ====================

    @Test
    @DisplayName("Debe manejar correctamente múltiples horarios en el mismo día")
    void debeManejarMultiplesHorariosEnMismoDia() {
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

        List<HorarioResponse> result = actividadService.obtenerHorarios(1L, futureDate);

        assertEquals(3, result.size());
        assertEquals(LocalTime.of(8, 0), result.get(0).getHoraInicio());
        assertEquals(LocalTime.of(12, 0), result.get(1).getHoraInicio());
        assertEquals(LocalTime.of(16, 0), result.get(2).getHoraInicio());
    }

    @Test
    @DisplayName("Debe aceptar la fecha de hoy cuando es válida")
    void debeAceptarFechaDeHoyCuandoEsValida() {
        LocalDate today = LocalDate.now();

        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividadSafari));
        when(horarioRepository.findByActividadAndFecha(actividadSafari, today))
                .thenReturn(List.of());

        List<HorarioResponse> result = actividadService.obtenerHorarios(1L, today);

        assertNotNull(result);
        verify(actividadRepository, times(1)).findById(1L);
    }
}
