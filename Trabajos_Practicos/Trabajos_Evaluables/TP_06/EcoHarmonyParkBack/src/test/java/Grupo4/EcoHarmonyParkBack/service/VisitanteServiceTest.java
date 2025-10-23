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
 * Pruebas unitarias para VisitanteService
 * Prueba la lógica de creación y actualización de visitantes
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

    // ==================== PRUEBAS PARA crearVisitante - NUEVO VISITANTE ====================

    @Test
    @DisplayName("Debería crear un nuevo visitante cuando el DNI no existe")
    void deberiaCrearNuevoVisitanteCuandoDniNoExiste() {
        when(visitanteRepository.findByDni("12345678")).thenReturn(Optional.empty());
        when(visitanteRepository.save(any(Visitante.class))).thenAnswer(invocation -> {
            Visitante visitante = invocation.getArgument(0);
            visitante.setId(1L);
            return visitante;
        });

        Visitante result = visitanteService.crearVisitante(visitanteRequest);

        assertNotNull(result);
        assertEquals("Juan Perez", result.getNombre());
        assertEquals("12345678", result.getDni());
        assertEquals(30, result.getEdad());
        assertEquals("M", result.getTallaVestimenta());

        verify(visitanteRepository, times(1)).findByDni("12345678");
        verify(visitanteRepository, times(1)).save(any(Visitante.class));

        ArgumentCaptor<Visitante> visitanteCaptor = ArgumentCaptor.forClass(Visitante.class);
        verify(visitanteRepository).save(visitanteCaptor.capture());
        Visitante savedVisitante = visitanteCaptor.getValue();

        assertEquals("Juan Perez", savedVisitante.getNombre());
        assertEquals("12345678", savedVisitante.getDni());
        assertEquals(30, savedVisitante.getEdad());
        assertEquals("M", savedVisitante.getTallaVestimenta());
    }

    @Test
    @DisplayName("Debería crear un visitante sin talla de vestimenta cuando no es requerida")
    void deberiaCrearVisitanteSinTallaDeVestimenta() {
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

        Visitante result = visitanteService.crearVisitante(requestSinTalla);

        assertNotNull(result);
        assertEquals("Maria Lopez", result.getNombre());
        assertNull(result.getTallaVestimenta());

        verify(visitanteRepository, times(1)).save(any(Visitante.class));
    }

    // ==================== PRUEBAS PARA crearVisitante - ACTUALIZAR EXISTENTE ====================

    @Test
    @DisplayName("Debería actualizar un visitante existente cuando el DNI ya existe")
    void deberiaActualizarVisitanteExistenteCuandoDniExiste() {
        when(visitanteRepository.findByDni("12345678")).thenReturn(Optional.of(visitanteExistente));
        when(visitanteRepository.save(any(Visitante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Visitante result = visitanteService.crearVisitante(visitanteRequest);

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
    @DisplayName("Debería actualizar el visitante pero mantener la talla anterior cuando la nueva talla es nula")
    void deberiaMantenerTallaAnteriorCuandoNuevaTallaEsNula() {
        VisitanteRequest requestSinTalla = VisitanteRequest.builder()
                .nombre("Juan Perez Actualizado")
                .dni("12345678")
                .edad(31)
                .tallaVestimenta(null) // No especifica talla
                .build();

        when(visitanteRepository.findByDni("12345678")).thenReturn(Optional.of(visitanteExistente));
        when(visitanteRepository.save(any(Visitante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Visitante result = visitanteService.crearVisitante(requestSinTalla);

        assertNotNull(result);
        assertEquals("Juan Perez Actualizado", result.getNombre());
        assertEquals(31, result.getEdad());
        assertEquals("S", result.getTallaVestimenta()); // Mantiene la talla anterior
        assertNotEquals("M", result.getTallaVestimenta());

        verify(visitanteRepository, times(1)).save(visitanteExistente);
    }

    @Test
    @DisplayName("Debería actualizar la talla cuando se proporciona una nueva talla para un visitante existente")
    void deberiaActualizarTallaCuandoSeProporcionaNuevaTalla() {
        VisitanteRequest requestConTalla = VisitanteRequest.builder()
                .nombre("Juan Perez Actualizado")
                .dni("12345678")
                .edad(31)
                .tallaVestimenta("XL") // Nueva talla
                .build();

        when(visitanteRepository.findByDni("12345678")).thenReturn(Optional.of(visitanteExistente));
        when(visitanteRepository.save(any(Visitante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Visitante result = visitanteService.crearVisitante(requestConTalla);

        assertNotNull(result);
        assertEquals("XL", result.getTallaVestimenta()); // Talla actualizada
        assertNotEquals("S", result.getTallaVestimenta());

        verify(visitanteRepository, times(1)).save(visitanteExistente);
    }

    // ==================== CASOS EXTREMOS ====================

    @Test
    @DisplayName("Debería manejar un visitante con la edad mínima válida")
    void deberiaManejarVisitanteConEdadMinima() {
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

        Visitante result = visitanteService.crearVisitante(requestEdadMinima);

        assertEquals(1, result.getEdad());
    }

    @Test
    @DisplayName("Debería manejar un visitante con la edad máxima válida")
    void deberiaManejarVisitanteConEdadMaxima() {
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

        Visitante result = visitanteService.crearVisitante(requestEdadMaxima);

        assertEquals(120, result.getEdad());
    }

    @Test
    @DisplayName("Debería manejar nombres largos de visitantes")
    void deberiaManejarNombresLargosDeVisitantes() {
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

        Visitante result = visitanteService.crearVisitante(requestNombreLargo);

        assertEquals(nombreLargo, result.getNombre());
    }

    @Test
    @DisplayName("Debería manejar un DNI con 7 dígitos")
    void deberiaManejarDniCon7Digitos() {
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

        Visitante result = visitanteService.crearVisitante(request7Digitos);

        assertEquals("1234567", result.getDni());
    }

    @Test
    @DisplayName("Debería manejar un DNI con 8 dígitos")
    void deberiaManejarDniCon8Digitos() {
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

        Visitante result = visitanteService.crearVisitante(request8Digitos);

        assertEquals("12345678", result.getDni());
    }

    @Test
    @DisplayName("Debería actualizar todas las propiedades de un visitante existente")
    void deberiaActualizarTodasLasPropiedadesDeVisitanteExistente() {
        VisitanteRequest updateRequest = VisitanteRequest.builder()
                .nombre("Nombre Completamente Nuevo")
                .dni("12345678")
                .edad(50)
                .tallaVestimenta("XXL")
                .build();

        when(visitanteRepository.findByDni("12345678")).thenReturn(Optional.of(visitanteExistente));
        when(visitanteRepository.save(any(Visitante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Visitante result = visitanteService.crearVisitante(updateRequest);

        assertEquals(1L, result.getId()); // ID no cambia
        assertEquals("Nombre Completamente Nuevo", result.getNombre()); // Nombre actualizado
        assertEquals(50, result.getEdad()); // Edad actualizada
        assertEquals("XXL", result.getTallaVestimenta()); // Talla actualizada
        assertEquals("12345678", result.getDni()); // DNI igual

        assertNotEquals("Juan Perez Viejo", result.getNombre());
        assertNotEquals(28, result.getEdad());
        assertNotEquals("S", result.getTallaVestimenta());
    }


    @Test
    @DisplayName("Debería invocar correctamente los métodos del repositorio en el orden correcto")
    void deberiaInvocarMetodosDelRepositorioEnOrdenCorrecto() {
        when(visitanteRepository.findByDni("12345678")).thenReturn(Optional.empty());
        when(visitanteRepository.save(any(Visitante.class))).thenAnswer(invocation -> {
            Visitante visitante = invocation.getArgument(0);
            visitante.setId(1L);
            return visitante;
        });

        visitanteService.crearVisitante(visitanteRequest);

        var inOrder = inOrder(visitanteRepository);
        inOrder.verify(visitanteRepository).findByDni("12345678");
        inOrder.verify(visitanteRepository).save(any(Visitante.class));
    }
}
