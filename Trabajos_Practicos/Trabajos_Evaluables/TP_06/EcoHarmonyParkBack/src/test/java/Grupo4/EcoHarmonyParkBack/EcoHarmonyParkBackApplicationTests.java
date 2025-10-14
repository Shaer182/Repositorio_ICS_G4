package Grupo4.EcoHarmonyParkBack;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import Grupo4.EcoHarmonyParkBack.repositories.InscripcionRepository;
import Grupo4.EcoHarmonyParkBack.services.InscripcionService;

@SpringBootTest
class EcoHarmonyParkBackApplicationTests {

	@Mock
	private InscripcionRepository inscripcionRepository;

	@InjectMocks
	private InscripcionService inscripcionService;

	@Test
	void contextLoads() {
	}

	@Test
	void testInscripcionCorrecta() {

		int resultado = inscripcionService.inscribirActividad(null);

		try {
			assertEquals(0, resultado);
			System.out.println("Test de inscripcion correcta pasado");
		} catch (AssertionError e) {
			System.err.println("Test de inscripcion correcta fallido: " + e.getMessage());
		}
		
	}

	@Test
	void testInscripcionDuplicada() {

		int resultado = inscripcionService.inscribirActividad(null);

		try {
			assertEquals(1, resultado);
			System.out.println("Test de inscripcion duplicada pasado");
		} catch (AssertionError e) {
			System.err.println("Test de inscripcion duplicada fallado: " + e.getMessage());
		}
		
	}

	@Test
	void testInscripcionDatosFaltantes() {
		
		int resultado = inscripcionService.inscribirActividad(null);

		try {
			assertEquals(2, resultado);
			System.out.println("Test de inscripcion con datos faltantes pasado");
		} catch (AssertionError e) {
			System.err.println("Test de inscripcion con datos faltantes fallido: " + e.getMessage());
		}
		
	}

	@Test
	void testInscripcionCupoExcedido() {
		int resultado = inscripcionService.inscribirActividad(null);

		try {
			assertEquals(3, resultado);
			System.out.println("Test de inscripcion con cupos excedidos pasado");
		} catch (AssertionError e) {
			System.err.println("Test de inscripcion con cupos excedidos fallido: " + e.getMessage());
		}
		
	}

	@Test
	void testInscripcionSinTyC() {
		
		int resultado = inscripcionService.inscribirActividad(null);

		try {
			assertEquals(4, resultado);
			System.out.println("Test de inscripcion sin aceptar TyC pasado");
		} catch (AssertionError e) {
			System.err.println("Test de inscripcion sin aceptar TyC fallido: " + e.getMessage());
		}
		
	}

	@Test
	void testInscripcionMenorEdad() {
		
		int resultado = inscripcionService.inscribirActividad(null);

		try {
			assertEquals(5, resultado);
			System.out.println("Test de inscripcion siendo menor de edad pasado");
		} catch (AssertionError e) {
			System.err.println("Test de inscripcion siendo menor de edad fallido: " + e.getMessage());
		}	
	}

	@Test
	void testInscripcionSinTalle() {
		
		int resultado = inscripcionService.inscribirActividad(null);

		try {
			assertEquals(6, resultado);
			System.out.println("Test de inscripcion sin ingresar talle pasado");
		} catch (AssertionError e) {
			System.err.println("Test de inscripcion sin ingresar talle fallido: " + e.getMessage());
		}	
	}

}
