package Grupo4.EcoHarmonyParkBack.service;

import Grupo4.EcoHarmonyParkBack.dtos.VisitanteRequest;
import Grupo4.EcoHarmonyParkBack.entities.Visitante;
import Grupo4.EcoHarmonyParkBack.repositories.VisitanteRepository;
import Grupo4.EcoHarmonyParkBack.services.VisitanteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VisitanteService
 * Tests visitor creation and update logic
 */
@ExtendWith(MockitoExtension.class)
class VisitanteServiceTest {

    @Mock
    private VisitanteRepository visitanteRepository;

    @InjectMocks
    private VisitanteService visitanteService;

    private VisitanteRequest visitanteRequest;
    private Visitante visitanteExistente;

    @BeforeEach
    void setUp() {
        visitanteRequest = VisitanteRequest.builder()
                .nombre("Juan Perez")
                .dni("12345678")
                .edad(30)
                .tallaVestimenta("M")
                .build();

        visitanteExistente = Visitante.builder()
                .id(1L)
                .nombre("Juan Perez Viejo")
                .dni("12345678")
                .edad(28)
                .tallaVestimenta("S")
                .build();
    }

    // ==================== TESTS FOR crearVisitante - NEW VISITOR ====================

    @Test
    @DisplayName("Should create new visitor when DNI does not exist")
    void shouldCreateNewVisitorWhenDniNotExists() {
        // Arrange
        when(visitanteRepository.findByDni("12345678")).thenReturn(Optional.empty());
        when(visitanteRepository.save(any(Visitante.class))).thenAnswer(invocation -> {
            Visitante visitante = invocation.getArgument(0);
            visitante.setId(1L);
            return visitante;
        });

        // Act
        Visitante result = visitanteService.crearVisitante(visitanteRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Juan Perez", result.getNombre());
        assertEquals("12345678", result.getDni());
        assertEquals(30, result.getEdad());
        assertEquals("M", result.getTallaVestimenta());

        // Verify repository interactions
        verify(visitanteRepository, times(1)).findByDni("12345678");
        verify(visitanteRepository, times(1)).save(any(Visitante.class));

        // Verify the saved visitante has correct properties
        ArgumentCaptor<Visitante> visitanteCaptor = ArgumentCaptor.forClass(Visitante.class);
        verify(visitanteRepository).save(visitanteCaptor.capture());
        Visitante savedVisitante = visitanteCaptor.getValue();

        assertEquals("Juan Perez", savedVisitante.getNombre());
        assertEquals("12345678", savedVisitante.getDni());
        assertEquals(30, savedVisitante.getEdad());
        assertEquals("M", savedVisitante.getTallaVestimenta());
    }

    @Test
    @DisplayName("Should create visitor without clothing size when not required")
    void shouldCreateVisitorWithoutClothingSize() {
        // Arrange
        VisitanteRequest requestSinTalla = VisitanteRequest.builder()
                .nombre("Maria Lopez")
                .dni("87654321")
                .edad(25)
                .tallaVestimenta(null)
                .build();

        when(visitanteRepository.findByDni("87654321")).thenReturn(Optional.empty());
        when(visitanteRepository.save(any(Visitante.class))).thenAnswer(invocation -> {
            Visitante visitante = invocation.getArgument(0);
            visitante.setId(2L);
            return visitante;
        });

        // Act
        Visitante result = visitanteService.crearVisitante(requestSinTalla);

        // Assert
        assertNotNull(result);
        assertEquals("Maria Lopez", result.getNombre());
        assertNull(result.getTallaVestimenta());

        verify(visitanteRepository, times(1)).save(any(Visitante.class));
    }

    // ==================== TESTS FOR crearVisitante - UPDATE EXISTING ====================

    @Test
    @DisplayName("Should update existing visitor when DNI already exists")
    void shouldUpdateExistingVisitorWhenDniExists() {
        // Arrange
        when(visitanteRepository.findByDni("12345678")).thenReturn(Optional.of(visitanteExistente));
        when(visitanteRepository.save(any(Visitante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Visitante result = visitanteService.crearVisitante(visitanteRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId()); // Mantiene el mismo ID
        assertEquals("Juan Perez", result.getNombre()); // Nombre actualizado
        assertEquals("12345678", result.getDni()); // DNI igual
        assertEquals(30, result.getEdad()); // Edad actualizada
        assertEquals("M", result.getTallaVestimenta()); // Talla actualizada

        verify(visitanteRepository, times(1)).findByDni("12345678");
        verify(visitanteRepository, times(1)).save(visitanteExistente);
    }

    @Test
    @DisplayName("Should update visitor but keep old talla when new talla is null")
    void shouldKeepOldTallaWhenNewTallaIsNull() {
        // Arrange
        VisitanteRequest requestSinTalla = VisitanteRequest.builder()
                .nombre("Juan Perez Actualizado")
                .dni("12345678")
                .edad(31)
                .tallaVestimenta(null) // No especifica talla
                .build();

        when(visitanteRepository.findByDni("12345678")).thenReturn(Optional.of(visitanteExistente));
        when(visitanteRepository.save(any(Visitante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Visitante result = visitanteService.crearVisitante(requestSinTalla);

        // Assert
        assertNotNull(result);
        assertEquals("Juan Perez Actualizado", result.getNombre());
        assertEquals(31, result.getEdad());
        assertEquals("S", result.getTallaVestimenta()); // Mantiene la talla anterior
        assertNotEquals("M", result.getTallaVestimenta());

        verify(visitanteRepository, times(1)).save(visitanteExistente);
    }

    @Test
    @DisplayName("Should update talla when new talla is provided for existing visitor")
    void shouldUpdateTallaWhenNewTallaProvided() {
        // Arrange
        VisitanteRequest requestConTalla = VisitanteRequest.builder()
                .nombre("Juan Perez Actualizado")
                .dni("12345678")
                .edad(31)
                .tallaVestimenta("XL") // Nueva talla
                .build();

        when(visitanteRepository.findByDni("12345678")).thenReturn(Optional.of(visitanteExistente));
        when(visitanteRepository.save(any(Visitante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Visitante result = visitanteService.crearVisitante(requestConTalla);

        // Assert
        assertNotNull(result);
        assertEquals("XL", result.getTallaVestimenta()); // Talla actualizada
        assertNotEquals("S", result.getTallaVestimenta());

        verify(visitanteRepository, times(1)).save(visitanteExistente);
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("Should handle visitor with minimum valid age")
    void shouldHandleVisitorWithMinimumAge() {
        // Arrange
        VisitanteRequest requestEdadMinima = VisitanteRequest.builder()
                .nombre("Niño Pequeño")
                .dni("11111111")
                .edad(1)
                .build();

        when(visitanteRepository.findByDni("11111111")).thenReturn(Optional.empty());
        when(visitanteRepository.save(any(Visitante.class))).thenAnswer(invocation -> {
            Visitante visitante = invocation.getArgument(0);
            visitante.setId(3L);
            return visitante;
        });

        // Act
        Visitante result = visitanteService.crearVisitante(requestEdadMinima);

        // Assert
        assertEquals(1, result.getEdad());
    }

    @Test
    @DisplayName("Should handle visitor with maximum valid age")
    void shouldHandleVisitorWithMaximumAge() {
        // Arrange
        VisitanteRequest requestEdadMaxima = VisitanteRequest.builder()
                .nombre("Persona Mayor")
                .dni("99999999")
                .edad(120)
                .build();

        when(visitanteRepository.findByDni("99999999")).thenReturn(Optional.empty());
        when(visitanteRepository.save(any(Visitante.class))).thenAnswer(invocation -> {
            Visitante visitante = invocation.getArgument(0);
            visitante.setId(4L);
            return visitante;
        });

        // Act
        Visitante result = visitanteService.crearVisitante(requestEdadMaxima);

        // Assert
        assertEquals(120, result.getEdad());
    }

    @Test
    @DisplayName("Should handle long visitor names")
    void shouldHandleLongVisitorNames() {
        // Arrange
        String nombreLargo = "Juan Carlos Alberto Fernandez Gonzalez Martinez Rodriguez Lopez Perez";
        VisitanteRequest requestNombreLargo = VisitanteRequest.builder()
                .nombre(nombreLargo)
                .dni("22222222")
                .edad(30)
                .build();

        when(visitanteRepository.findByDni("22222222")).thenReturn(Optional.empty());
        when(visitanteRepository.save(any(Visitante.class))).thenAnswer(invocation -> {
            Visitante visitante = invocation.getArgument(0);
            visitante.setId(5L);
            return visitante;
        });

        // Act
        Visitante result = visitanteService.crearVisitante(requestNombreLargo);

        // Assert
        assertEquals(nombreLargo, result.getNombre());
    }

    @Test
    @DisplayName("Should handle DNI with 7 digits")
    void shouldHandleDniWith7Digits() {
        // Arrange
        VisitanteRequest request7Digitos = VisitanteRequest.builder()
                .nombre("Persona DNI Corto")
                .dni("1234567") // 7 dígitos
                .edad(25)
                .build();

        when(visitanteRepository.findByDni("1234567")).thenReturn(Optional.empty());
        when(visitanteRepository.save(any(Visitante.class))).thenAnswer(invocation -> {
            Visitante visitante = invocation.getArgument(0);
            visitante.setId(6L);
            return visitante;
        });

        // Act
        Visitante result = visitanteService.crearVisitante(request7Digitos);

        // Assert
        assertEquals("1234567", result.getDni());
    }

    @Test
    @DisplayName("Should handle DNI with 8 digits")
    void shouldHandleDniWith8Digits() {
        // Arrange
        VisitanteRequest request8Digitos = VisitanteRequest.builder()
                .nombre("Persona DNI Largo")
                .dni("12345678") // 8 dígitos
                .edad(25)
                .build();

        when(visitanteRepository.findByDni("12345678")).thenReturn(Optional.empty());
        when(visitanteRepository.save(any(Visitante.class))).thenAnswer(invocation -> {
            Visitante visitante = invocation.getArgument(0);
            visitante.setId(7L);
            return visitante;
        });

        // Act
        Visitante result = visitanteService.crearVisitante(request8Digitos);

        // Assert
        assertEquals("12345678", result.getDni());
    }

    @Test
    @DisplayName("Should update all properties for existing visitor")
    void shouldUpdateAllPropertiesForExistingVisitor() {
        // Arrange
        VisitanteRequest updateRequest = VisitanteRequest.builder()
                .nombre("Nombre Completamente Nuevo")
                .dni("12345678")
                .edad(50)
                .tallaVestimenta("XXL")
                .build();

        when(visitanteRepository.findByDni("12345678")).thenReturn(Optional.of(visitanteExistente));
        when(visitanteRepository.save(any(Visitante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Visitante result = visitanteService.crearVisitante(updateRequest);

        // Assert
        assertEquals(1L, result.getId()); // ID no cambia
        assertEquals("Nombre Completamente Nuevo", result.getNombre()); // Nombre actualizado
        assertEquals(50, result.getEdad()); // Edad actualizada
        assertEquals("XXL", result.getTallaVestimenta()); // Talla actualizada
        assertEquals("12345678", result.getDni()); // DNI igual

        // Verify old values were overwritten
        assertNotEquals("Juan Perez Viejo", result.getNombre());
        assertNotEquals(28, result.getEdad());
        assertNotEquals("S", result.getTallaVestimenta());
    }


    @Test
    @DisplayName("Should properly invoke repository methods in correct order")
    void shouldInvokeRepositoryMethodsInCorrectOrder() {
        // Arrange
        when(visitanteRepository.findByDni("12345678")).thenReturn(Optional.empty());
        when(visitanteRepository.save(any(Visitante.class))).thenAnswer(invocation -> {
            Visitante visitante = invocation.getArgument(0);
            visitante.setId(1L);
            return visitante;
        });

        // Act
        visitanteService.crearVisitante(visitanteRequest);

        // Assert - verify order of method calls
        var inOrder = inOrder(visitanteRepository);
        inOrder.verify(visitanteRepository).findByDni("12345678");
        inOrder.verify(visitanteRepository).save(any(Visitante.class));
    }
}
