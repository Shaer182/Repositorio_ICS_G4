package Grupo4.EcoHarmonyParkBack;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import Grupo4.EcoHarmonyParkBack.dtos.InscripcionRequest;
import Grupo4.EcoHarmonyParkBack.dtos.InscripcionResponse;
import Grupo4.EcoHarmonyParkBack.dtos.VisitanteRequest;
import Grupo4.EcoHarmonyParkBack.entities.*;
import Grupo4.EcoHarmonyParkBack.repositories.HorarioActividadRepository;
import Grupo4.EcoHarmonyParkBack.repositories.VisitanteRepository;
import Grupo4.EcoHarmonyParkBack.services.ActividadService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import Grupo4.EcoHarmonyParkBack.repositories.InscripcionRepository;
import Grupo4.EcoHarmonyParkBack.services.InscripcionService;
import Grupo4.EcoHarmonyParkBack.entities.Visitante;
import net.bytebuddy.description.annotation.AnnotationList.Empty;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class EcoHarmonyParkBackApplicationTests {



	@InjectMocks
	private InscripcionService inscripcionService;

	@Test
	void contextLoads() {
	}

    @MockBean private HorarioActividadRepository horarioRepository;
    @MockBean
    private VisitanteRepository visitanteRepository;
    @MockBean private InscripcionRepository inscripcionRepository;

    @Autowired
    private ActividadService actividadService;

    private HorarioActividad horario;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Simular actividad con vestimenta
        Actividad actividad = Actividad.builder()
                .id(1L)
                .nombre("Escalada")
                .requiereVestimenta(true)
                .build();

        // Simular horario con cupos
        horario = HorarioActividad.builder()
                .id(3L)
                .actividad(actividad)
                .cuposDisponibles(5)
                .build();

        when(horarioRepository.findById(3L)).thenReturn(Optional.of(horario));
    }

	@Test
	void testInscripcionCorrecta() {

		List<VisitanteRequest> visitantes = List.of(
				new VisitanteRequest("Juan Perez", "12345678", 30, "M"),
				new VisitanteRequest("Maria Gomez", "87654321", 25, "S")
		);

        InscripcionRequest request = new InscripcionRequest();
        request.setHorarioActividadId(3L);
        request.setCantidadPersonas(2);
        request.setVisitantes(visitantes);

        // ---  Mockear comportamiento del repositorio ---
        when(visitanteRepository.findByDni(anyString())).thenReturn(Optional.empty());
        when(visitanteRepository.save(any(Visitante.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

        InscripcionResponse proceso = actividadService.inscribirActividad(request);

        try {
            proceso = actividadService.inscribirActividad(request);
        } catch (RuntimeException e) {
            proceso = null;
        }

        Boolean resultado;

        if (proceso != null){
            resultado = true;
        }
        else{
            resultado = false;
        }

		try {
			assertEquals(true, resultado);
			System.out.println("Test de inscripcion correcta pasado");
		} catch (AssertionError e) {
			System.err.println("Test de inscripcion correcta fallido: " + e.getMessage());
		}
		
	}

	@Test
	void testInscripcionDuplicada() {
		
		List<VisitanteRequest> visitantes = List.of(
				new VisitanteRequest("Juan Perez", "12345678", 30, "M"),
                new VisitanteRequest("Juan Perez", "12345678", 30, "M")
		);

        InscripcionRequest request = new InscripcionRequest();
        request.setHorarioActividadId(3L);
        request.setCantidadPersonas(2);
        request.setVisitantes(visitantes);

        // ---  Mockear comportamiento del repositorio ---
        when(visitanteRepository.findByDni(anyString())).thenReturn(Optional.empty());
        when(visitanteRepository.save(any(Visitante.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

        InscripcionResponse proceso;

        try {
            proceso = actividadService.inscribirActividad(request);
        } catch (RuntimeException e) {
            proceso = null;
        }
        boolean resultado;

        if (proceso != null){
            resultado = true;
        }
        else{
            resultado = false;
        }


		try {
			assertEquals(false, resultado);
			System.out.println("Test de inscripcion duplicada pasado");
		} catch (AssertionError e) {
			System.err.println("Test de inscripcion duplicada fallado: " + e.getMessage());
            fail();
		}
		
	}

	@Test
	void testInscripcionDatosFaltantes() {
		
		List<VisitanteRequest> visitantes = List.of(
				new VisitanteRequest("Juan Perez", "12345678", 30, "M")
		);

        InscripcionRequest request = new InscripcionRequest();
        //request.setHorarioActividadId(3L);
        request.setCantidadPersonas(2);
        request.setVisitantes(visitantes);

        // ---  Mockear comportamiento del repositorio ---
        when(visitanteRepository.findByDni(anyString())).thenReturn(Optional.empty());
        when(visitanteRepository.save(any(Visitante.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

        InscripcionResponse proceso;

        try {
            proceso = actividadService.inscribirActividad(request);
        } catch (RuntimeException e) {
            proceso = null;
        }

        boolean resultado;

        if (proceso != null){
            resultado = true;
            System.out.println("proceso: " + proceso.toString());

        }
        else{
            resultado = false;
        }

		try {
			assertEquals(false, resultado);
			System.out.println("Test de inscripcion con datos faltantes pasado");

		} catch (AssertionError e) {
			System.err.println("Test de inscripcion con datos faltantes fallido: " + e.getMessage());
            fail();
		}
		
	}

	@Test
	void testInscripcionCupoExcedido() {

		List<VisitanteRequest> visitantes = List.of(
				new VisitanteRequest("Juan Perez", "12345678", 30, "M")
		);

        InscripcionRequest request = new InscripcionRequest();
        request.setHorarioActividadId(3L);
        request.setCantidadPersonas(7);
        //
        request.setVisitantes(visitantes);

        // ---  Mockear comportamiento del repositorio ---
        when(visitanteRepository.findByDni(anyString())).thenReturn(Optional.empty());
        when(visitanteRepository.save(any(Visitante.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

        InscripcionResponse proceso;

        try {
            proceso = actividadService.inscribirActividad(request);
        } catch (RuntimeException e) {
            proceso = null;
        }
        boolean resultado;

        if (proceso != null){
            resultado = true;
        }
        else{
            resultado = false;
        }

		try {
			assertEquals(false, resultado);
			System.out.println("Test de inscripcion con cupos excedidos pasado");
		} catch (AssertionError e) {
			System.err.println("Test de inscripcion con cupos excedidos fallido: " + e.getMessage());
            fail();
		}
		
	}

	@Test
	void testInscripcionSinTyC() {
        //TyC: terminos y condiciones
		List<Visitante> visitantes = List.of(
				new Visitante("Juan Perez", "12345678", 30, "M")
		);

		int resultado = inscripcionService.inscribirActividad(visitantes, 1L, false);

		try {
			assertEquals(4, resultado);
			System.out.println("Test de inscripcion sin aceptar TyC pasado");
		} catch (AssertionError e) {
			System.err.println("Test de inscripcion sin aceptar TyC fallido: " + e.getMessage());
            fail();
		}
		
	}

	@Test
	void testInscripcionMenorEdad() {
		
		List<VisitanteRequest> visitantes = List.of(
				new VisitanteRequest("Juan Perez", "12345678", 10, "M")
		);

        InscripcionRequest request = new InscripcionRequest();
        request.setHorarioActividadId(3L);
        request.setCantidadPersonas(2);
        request.setVisitantes(visitantes);

        // ---  Mockear comportamiento del repositorio ---
        when(visitanteRepository.findByDni(anyString())).thenReturn(Optional.empty());
        when(visitanteRepository.save(any(Visitante.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

        InscripcionResponse proceso;

        try {
            proceso = actividadService.inscribirActividad(request);
        } catch (RuntimeException e) {
            proceso = null;
        }
        boolean resultado;

        if (proceso != null){
            resultado = true;
        }
        else{
            resultado = false;
        }
		try {
			assertEquals(true, resultado);
			System.out.println("Test de inscripcion siendo menor de edad pasado");
		} catch (AssertionError e) {
			System.err.println("Test de inscripcion siendo menor de edad fallido: " + e.getMessage());
            fail();
		}	
	}

	@Test
	void testInscripcionSinTalle() {
		
		List<VisitanteRequest> visitantes = List.of(
				new VisitanteRequest("Juan Perez", "12345678", 30, null)
		);

        InscripcionRequest request = new InscripcionRequest();
        request.setHorarioActividadId(3L);
        request.setCantidadPersonas(2);
        request.setVisitantes(visitantes);

        // ---  Mockear comportamiento del repositorio ---
        when(visitanteRepository.findByDni(anyString())).thenReturn(Optional.empty());
        when(visitanteRepository.save(any(Visitante.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

        InscripcionResponse proceso;

        try {
            proceso = actividadService.inscribirActividad(request);
        } catch (RuntimeException e) {
            proceso = null;
        }

        boolean resultado;

        if (proceso != null){
            resultado = true;
        }
        else{
            resultado = false;
        }
		try {
			assertEquals(false, resultado);
			System.out.println("Test de inscripcion sin ingresar talle pasado");
		} catch (AssertionError e) {
			System.err.println("Test de inscripcion sin ingresar talle fallido: " + e.getMessage());
            fail();
		}	
	}

}
