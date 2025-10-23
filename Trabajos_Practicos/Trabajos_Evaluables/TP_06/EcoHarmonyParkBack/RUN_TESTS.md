# Quick Test Execution Guide

## Prerequisites

Ensure you have:
- Java 21 installed
- JAVA_HOME environment variable configured
- Maven (or use the included Maven wrapper)

## Running All Tests

### Windows (Command Prompt or PowerShell)
```cmd
mvnw.cmd clean test
```

### Linux/Mac or Windows (Git Bash)
```bash
./mvnw clean test
```

## Running Specific Test Files

### Run only Service tests
```bash
./mvnw test -Dtest="InscripcionServiceTest,ActividadServiceTest,VisitanteServiceTest"
```

### Run only Controller tests
```bash
./mvnw test -Dtest="InscripcionControllerTest,ActividadControllerTest"
```

### Run a single test class
```bash
./mvnw test -Dtest=InscripcionServiceTest
```

### Run a specific test method
```bash
./mvnw test -Dtest=InscripcionServiceTest#shouldRegisterSuccessfullyWhenValidData
```

## Test File Locations

All test files are located in: `src/test/java/Grupo4/EcoHarmonyParkBack/`

```
src/test/java/Grupo4/EcoHarmonyParkBack/
├── service/
│   ├── InscripcionServiceTest.java       (17 tests)
│   ├── ActividadServiceTest.java         (15 tests)
│   └── VisitanteServiceTest.java         (13 tests)
├── controller/
│   ├── InscripcionControllerTest.java    (13 tests)
│   └── ActividadControllerTest.java      (13 tests)
└── EcoHarmonyParkBackApplicationTests.java (existing integration tests)
```

**Total: 71 unit tests + existing integration tests**

## Expected Output

Successful test run:
```
[INFO] Results:
[INFO]
[INFO] Tests run: 71, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] BUILD SUCCESS
```

## Troubleshooting

### "JAVA_HOME not defined"
Set the JAVA_HOME environment variable:
```bash
# Windows
set JAVA_HOME=C:\Program Files\Java\jdk-21

# Linux/Mac
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
```

### "Command mvnw not found"
Use the full path or ensure you're in the project root directory:
```bash
cd C:\Users\kh\Desktop\ISW\repositorio\Repositorio_ICS_G4\Trabajos_Practicos\Trabajos_Evaluables\TP_06\EcoHarmonyParkBack
```

### Tests fail
1. Check that PostgreSQL is NOT required (these are unit tests with mocked repositories)
2. Verify Java version: `java -version` (should be 21)
3. Clean and rebuild: `./mvnw clean compile test-compile`

## Test Coverage by Test Case

| Test Case | Test File | Test Method |
|-----------|-----------|-------------|
| CP1: Inscripción Correcta | InscripcionServiceTest | shouldRegisterSuccessfullyWhenValidData |
| CP2: Datos Faltantes | InscripcionControllerTest | shouldReturnValidationError* (multiple) |
| CP3: Inscripción Duplicada | InscripcionServiceTest | shouldThrowExceptionWhenVisitorAlreadyRegistered |
| CP4: Cupos llenos | InscripcionServiceTest | shouldThrowExceptionWhenNoSlotsAvailable |
| CP5: Cupos insuficientes | InscripcionServiceTest | shouldThrowExceptionWhenInsufficientSlots |
| CP6: No aceptar TyC | InscripcionServiceTest | shouldThrowExceptionWhenTermsNotAccepted |
| CP7: Sin talla de ropa | InscripcionServiceTest | shouldThrowExceptionWhenClothingSizeRequired |
| CP8: Menor de edad | InscripcionServiceTest | shouldThrowExceptionWhenVisitorUnderAge |
| CP9: GET /inscripciones | InscripcionControllerTest | shouldReturnAllInscriptions |
| CP10: GET /inscripciones/{id} | InscripcionControllerTest | shouldReturnInscriptionById |
| CP11: POST con email | InscripcionControllerTest | shouldCreateInscriptionSuccessfully |
| CP12: Obtener actividades | ActividadServiceTest | shouldReturnAllActivitiesSortedByName |
| CP13: Obtener horarios | ActividadServiceTest | shouldReturnSchedulesForFutureDate |
| CP14: DNI duplicado | InscripcionServiceTest | shouldThrowExceptionWhenDuplicateDniInRequest |

## Running Tests in IDE

### IntelliJ IDEA
1. Right-click on test class or method
2. Select "Run 'TestClassName'" or "Run 'testMethodName()'"
3. View results in Run window

### Eclipse
1. Right-click on test class or method
2. Select "Run As" > "JUnit Test"
3. View results in JUnit view

### VS Code
1. Install "Test Runner for Java" extension
2. Click the play button next to test class/method
3. View results in Test Explorer

## Continuous Integration

Add to your CI/CD pipeline (e.g., GitHub Actions, Jenkins):

```yaml
- name: Run tests
  run: ./mvnw clean test

- name: Generate test report
  if: always()
  uses: dorny/test-reporter@v1
  with:
    name: Maven Tests
    path: target/surefire-reports/*.xml
    reporter: java-junit
```

## Additional Commands

### Skip tests during build
```bash
./mvnw clean package -DskipTests
```

### Run tests with verbose output
```bash
./mvnw test -X
```

### Run tests and generate coverage report (if JaCoCo configured)
```bash
./mvnw clean test jacoco:report
```

## Notes

- These are **unit tests** - they don't require database or external services
- All dependencies are mocked using Mockito
- Tests are fast (should complete in seconds)
- Tests are isolated and can run in any order
- Each test follows Arrange-Act-Assert pattern

---

For detailed test documentation, see [TEST_DOCUMENTATION.md](./TEST_DOCUMENTATION.md)
