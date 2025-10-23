package Grupo4.EcoHarmonyParkBack.controller;

import Grupo4.EcoHarmonyParkBack.controllers.InscripcionController;
import Grupo4.EcoHarmonyParkBack.dtos.InscripcionRequest;
import Grupo4.EcoHarmonyParkBack.dtos.InscripcionResponse;
import Grupo4.EcoHarmonyParkBack.dtos.VisitanteRequest;
import Grupo4.EcoHarmonyParkBack.services.EmailService;
import Grupo4.EcoHarmonyParkBack.services.InscripcionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for InscripcionController
 * Tests REST API endpoints for activity registration
 */
@WebMvcTest(InscripcionController.class)
class InscripcionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InscripcionService inscripcionService;

    @MockBean
    private EmailService emailService;

    private InscripcionResponse inscripcionResponse;

    @BeforeEach
    void setUp() {
        inscripcionResponse = InscripcionResponse.builder()
                .id(1L)
                .cantidadPersonas(2)
                .email("test@example.com")
                .fechaInscripcion(LocalDateTime.now())
                .build();
    }

    // ==================== TESTS FOR GET /inscripciones ====================

    @Test
    @DisplayName("Should return list of all inscriptions with HTTP 200")
    void shouldReturnAllInscriptions() throws Exception {
        // Arrange
        InscripcionResponse response1 = InscripcionResponse.builder()
                .id(1L)
                .cantidadPersonas(2)
                .email("test1@example.com")
                .fechaInscripcion(LocalDateTime.now())
                .build();

        InscripcionResponse response2 = InscripcionResponse.builder()
                .id(2L)
                .cantidadPersonas(3)
                .email("test2@example.com")
                .fechaInscripcion(LocalDateTime.now())
                .build();

        List<InscripcionResponse> inscripciones = Arrays.asList(response1, response2);

        when(inscripcionService.obtenerInscripciones()).thenReturn(inscripciones);

        // Act & Assert
        mockMvc.perform(get("/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].email", is("test1@example.com")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].email", is("test2@example.com")));

        verify(inscripcionService, times(1)).obtenerInscripciones();
    }

    @Test
    @DisplayName("Should return empty list when no inscriptions exist")
    void shouldReturnEmptyListWhenNoInscriptions() throws Exception {
        // Arrange
        when(inscripcionService.obtenerInscripciones()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(inscripcionService, times(1)).obtenerInscripciones();
    }

    // ==================== TESTS FOR GET /inscripciones/{id} ====================

    @Test
    @DisplayName("Should return inscription by id with HTTP 200")
    void shouldReturnInscriptionById() throws Exception {
        // Arrange
        when(inscripcionService.obtenerInscripcionPorId(1L)).thenReturn(inscripcionResponse);

        // Act & Assert
        mockMvc.perform(get("/inscripciones/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.cantidadPersonas", is(2)));

        verify(inscripcionService, times(1)).obtenerInscripcionPorId(1L);
    }

    @Test
    @DisplayName("Should return HTTP 500 when inscription not found")
    void shouldReturnErrorWhenInscriptionNotFound() throws Exception {
        // Arrange
        when(inscripcionService.obtenerInscripcionPorId(999L))
                .thenThrow(new RuntimeException("No se encontró la inscripción con id: 999"));

        // Act & Assert
        mockMvc.perform(get("/inscripciones/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(inscripcionService, times(1)).obtenerInscripcionPorId(999L);
    }

    // ==================== TESTS FOR POST /inscripciones ====================

    @Test
    @DisplayName("Should create inscription successfully and send confirmation email")
    void shouldCreateInscriptionSuccessfully() throws Exception {
        // Arrange
        VisitanteRequest visitante = VisitanteRequest.builder()
                .nombre("Juan Perez")
                .dni("12345678")
                .edad(30)
                .tallaVestimenta("M")
                .build();

        InscripcionRequest request = InscripcionRequest.builder()
                .horarioActividadId(1L)
                .cantidadPersonas(1)
                .visitantes(List.of(visitante))
                .email("juan@example.com")
                .aceptoTyC(true)
                .build();

        InscripcionResponse response = InscripcionResponse.builder()
                .id(1L)
                .cantidadPersonas(1)
                .email("juan@example.com")
                .fechaInscripcion(LocalDateTime.now())
                .build();

        when(inscripcionService.inscribirActividad(any(InscripcionRequest.class))).thenReturn(response);
        doNothing().when(emailService).enviarConfirmacionInscripcion(anyString(), any(InscripcionResponse.class));

        // Act & Assert
        mockMvc.perform(post("/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("juan@example.com")))
                .andExpect(jsonPath("$.cantidadPersonas", is(1)));

        verify(inscripcionService, times(1)).inscribirActividad(any(InscripcionRequest.class));
        verify(emailService, times(1)).enviarConfirmacionInscripcion(eq("juan@example.com"), any(InscripcionResponse.class));
    }

    @Test
    @DisplayName("Should return validation error when email is missing")
    void shouldReturnValidationErrorWhenEmailMissing() throws Exception {
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
                .email(null) // Email faltante
                .aceptoTyC(true)
                .build();

        // Act & Assert
        mockMvc.perform(post("/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(inscripcionService, never()).inscribirActividad(any(InscripcionRequest.class));
        verify(emailService, never()).enviarConfirmacionInscripcion(anyString(), any(InscripcionResponse.class));
    }

    @Test
    @DisplayName("Should return validation error when email format is invalid")
    void shouldReturnValidationErrorWhenEmailInvalid() throws Exception {
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
                .email("invalid-email") // Email inválido
                .aceptoTyC(true)
                .build();

        // Act & Assert
        mockMvc.perform(post("/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(inscripcionService, never()).inscribirActividad(any(InscripcionRequest.class));
    }

    @Test
    @DisplayName("Should return validation error when horarioActividadId is null")
    void shouldReturnValidationErrorWhenHorarioIdNull() throws Exception {
        // Arrange
        VisitanteRequest visitante = VisitanteRequest.builder()
                .nombre("Juan Perez")
                .dni("12345678")
                .edad(30)
                .build();

        InscripcionRequest request = InscripcionRequest.builder()
                .horarioActividadId(null) // Horario nulo
                .cantidadPersonas(1)
                .visitantes(List.of(visitante))
                .email("juan@example.com")
                .aceptoTyC(true)
                .build();

        // Act & Assert
        mockMvc.perform(post("/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(inscripcionService, never()).inscribirActividad(any(InscripcionRequest.class));
    }

    @Test
    @DisplayName("Should return validation error when visitantes list is empty")
    void shouldReturnValidationErrorWhenVisitantesEmpty() throws Exception {
        // Arrange
        InscripcionRequest request = InscripcionRequest.builder()
                .horarioActividadId(1L)
                .cantidadPersonas(1)
                .visitantes(List.of()) // Lista vacía
                .email("juan@example.com")
                .aceptoTyC(true)
                .build();

        // Act & Assert
        mockMvc.perform(post("/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(inscripcionService, never()).inscribirActividad(any(InscripcionRequest.class));
    }

    @Test
    @DisplayName("Should return validation error when cantidadPersonas is zero")
    void shouldReturnValidationErrorWhenQuantityZero() throws Exception {
        // Arrange
        VisitanteRequest visitante = VisitanteRequest.builder()
                .nombre("Juan Perez")
                .dni("12345678")
                .edad(30)
                .build();

        InscripcionRequest request = InscripcionRequest.builder()
                .horarioActividadId(1L)
                .cantidadPersonas(0) // Cantidad inválida
                .visitantes(List.of(visitante))
                .email("juan@example.com")
                .aceptoTyC(true)
                .build();

        // Act & Assert
        mockMvc.perform(post("/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(inscripcionService, never()).inscribirActividad(any(InscripcionRequest.class));
    }

    @Test
    @DisplayName("Should return validation error when visitante nombre is blank")
    void shouldReturnValidationErrorWhenVisitanteNombreBlank() throws Exception {
        // Arrange
        VisitanteRequest visitante = VisitanteRequest.builder()
                .nombre("") // Nombre vacío
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

        // Act & Assert
        mockMvc.perform(post("/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(inscripcionService, never()).inscribirActividad(any(InscripcionRequest.class));
    }

    @Test
    @DisplayName("Should return validation error when visitante DNI format is invalid")
    void shouldReturnValidationErrorWhenDniInvalid() throws Exception {
        // Arrange
        VisitanteRequest visitante = VisitanteRequest.builder()
                .nombre("Juan Perez")
                .dni("123") // DNI inválido (menos de 7 dígitos)
                .edad(30)
                .build();

        InscripcionRequest request = InscripcionRequest.builder()
                .horarioActividadId(1L)
                .cantidadPersonas(1)
                .visitantes(List.of(visitante))
                .email("juan@example.com")
                .aceptoTyC(true)
                .build();

        // Act & Assert
        mockMvc.perform(post("/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(inscripcionService, never()).inscribirActividad(any(InscripcionRequest.class));
    }

    @Test
    @DisplayName("Should return validation error when visitante edad is zero")
    void shouldReturnValidationErrorWhenEdadZero() throws Exception {
        // Arrange
        VisitanteRequest visitante = VisitanteRequest.builder()
                .nombre("Juan Perez")
                .dni("12345678")
                .edad(0) // Edad inválida
                .build();

        InscripcionRequest request = InscripcionRequest.builder()
                .horarioActividadId(1L)
                .cantidadPersonas(1)
                .visitantes(List.of(visitante))
                .email("juan@example.com")
                .aceptoTyC(true)
                .build();

        // Act & Assert
        mockMvc.perform(post("/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(inscripcionService, never()).inscribirActividad(any(InscripcionRequest.class));
    }

    @Test
    @DisplayName("Should return error when service throws exception")
    void shouldReturnErrorWhenServiceThrowsException() throws Exception {
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

        when(inscripcionService.inscribirActividad(any(InscripcionRequest.class)))
                .thenThrow(new RuntimeException("No hay cupos disponibles"));

        // Act & Assert
        mockMvc.perform(post("/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(inscripcionService, times(1)).inscribirActividad(any(InscripcionRequest.class));
        verify(emailService, never()).enviarConfirmacionInscripcion(anyString(), any(InscripcionResponse.class));
    }

    @Test
    @DisplayName("Should create inscription with multiple visitors and send email")
    void shouldCreateInscriptionWithMultipleVisitors() throws Exception {
        // Arrange
        List<VisitanteRequest> visitantes = Arrays.asList(
                VisitanteRequest.builder().nombre("Juan Perez").dni("12345678").edad(30).build(),
                VisitanteRequest.builder().nombre("Maria Gomez").dni("87654321").edad(28).build()
        );

        InscripcionRequest request = InscripcionRequest.builder()
                .horarioActividadId(1L)
                .cantidadPersonas(2)
                .visitantes(visitantes)
                .email("grupo@example.com")
                .aceptoTyC(true)
                .build();

        InscripcionResponse response = InscripcionResponse.builder()
                .id(1L)
                .cantidadPersonas(2)
                .email("grupo@example.com")
                .fechaInscripcion(LocalDateTime.now())
                .build();

        when(inscripcionService.inscribirActividad(any(InscripcionRequest.class))).thenReturn(response);
        doNothing().when(emailService).enviarConfirmacionInscripcion(anyString(), any(InscripcionResponse.class));

        // Act & Assert
        mockMvc.perform(post("/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadPersonas", is(2)))
                .andExpect(jsonPath("$.email", is("grupo@example.com")));

        verify(emailService, times(1)).enviarConfirmacionInscripcion(eq("grupo@example.com"), any(InscripcionResponse.class));
    }
}
