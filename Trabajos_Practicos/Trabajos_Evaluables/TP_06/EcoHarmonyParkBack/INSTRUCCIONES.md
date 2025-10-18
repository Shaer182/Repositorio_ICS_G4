# 📋 Instrucciones de Ejecución - EcoHarmony Park

## 🎯 Requisitos Previos

Antes de comenzar, asegúrate de tener instalado:

- ✅ **Java JDK 17 o superior**
- ✅ **Maven** (o usar el wrapper incluido `mvnw`)
- ✅ **Node.js 18 o superior** y **npm**
- ✅ **Acceso a la base de datos PostgreSQL** (Supabase configurada)

---

## 🚀 Parte 1: Preparar la Base de Datos

### Paso 1: Ejecutar Script de Datos de Prueba

1. Abre tu proyecto en **Supabase**:
   ```
   https://supabase.com/dashboard/project/[tu-proyecto]
   ```

2. Ve al **SQL Editor**:
   - Menú lateral → **"SQL Editor"**
   - O **"Database" → "SQL Editor"**

3. Copia el contenido del archivo `datos_prueba.sql` y pégalo en el editor

4. Presiona **"RUN"** o **Ctrl + Enter**

5. Verifica que se insertaron correctamente:
   - Deberías ver 4 actividades y varios horarios para hoy
   - La consulta SELECT al final del script mostrará los datos

---

## 🔴 Parte 2: Levantar el Backend (Spring Boot)

### Opción A: Usando IntelliJ IDEA / Eclipse

1. **Abre el proyecto** en tu IDE
   ```
   Archivo → Open → Selecciona la carpeta: EcoHarmonyParkBack
   ```

2. **Espera a que Maven descargue las dependencias**
   - Puede tomar unos minutos la primera vez

3. **Ejecuta la aplicación**:
   - Busca el archivo: `src/main/java/Grupo4/EcoHarmonyParkBack/EcoHarmonyParkBackApplication.java`
   - Click derecho → **Run 'EcoHarmonyParkBackApplication'**

4. **Verifica que esté corriendo**:
   - En la consola debería aparecer: `Tomcat started on port 8080`
   - Abre tu navegador en: `http://localhost:8080/actividades`
   - Deberías ver un JSON con las actividades

### Opción B: Usando la Terminal

1. **Abre una terminal** en la carpeta del backend:
   ```bash
   cd EcoHarmonyParkBack
   ```

2. **Ejecuta con Maven Wrapper** (Windows):
   ```bash
   mvnw.cmd spring-boot:run
   ```

   **O en Linux/Mac**:
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Verifica que esté corriendo**:
   - Espera a ver: `Started EcoHarmonyParkBackApplication in X seconds`
   - Abre: `http://localhost:8080/actividades`

---

## 🟢 Parte 3: Levantar el Frontend (Next.js)

### Paso 1: Instalar Dependencias (Solo la primera vez)

1. **Abre una NUEVA terminal** (deja el backend corriendo)

2. **Navega a la carpeta del frontend**:
   ```bash
   cd EcoHarmonyParkFront
   ```

3. **Instala las dependencias**:
   ```bash
   npm install
   ```
   - Esto puede tomar unos minutos
   - Solo necesitas hacerlo una vez (o cuando cambien las dependencias)

### Paso 2: Ejecutar el Servidor de Desarrollo

1. **En la misma terminal del frontend**, ejecuta:
   ```bash
   npm run dev
   ```

2. **Verifica que esté corriendo**:
   - Deberías ver en consola:
     ```
     ✓ Ready in Xms
     - Local: http://localhost:3000
     ```

3. **Abre tu navegador en**:
   ```
   http://localhost:3000
   ```

### Paso 3: Verificar que Todo Funciona

Deberías ver:
- ✅ Header verde oscuro con "EcoHarmony Park"
- ✅ Fondo blanco hueso (#E8FCCF)
- ✅ 4 tarjetas de actividades (Tirolesa, Safari, Palestra, Jardinería)
- ✅ Badges verdes "Disponible hoy" en las actividades con cupos
- ✅ Cupos disponibles visibles en cada tarjeta
- ✅ Botones "Inscribirme" funcionales

---

## 🛑 Detener los Servidores

### Detener el Backend:
- En la terminal del backend: **Ctrl + C**

### Detener el Frontend:
- En la terminal del frontend: **Ctrl + C**

---

## 🔄 Para Volver a Ejecutar

### Si ya ejecutaste todo antes:

1. **Terminal 1 - Backend**:
   ```bash
   cd EcoHarmonyParkBack
   mvnw.cmd spring-boot:run    # Windows
   # o
   ./mvnw spring-boot:run       # Linux/Mac
   ```

2. **Terminal 2 - Frontend**:
   ```bash
   cd EcoHarmonyParkFront
   npm run dev
   ```

3. **Abre el navegador**: `http://localhost:3000`

---

## 📦 Subir al Repositorio Git

### Antes de hacer commit:

El archivo `.gitignore` ya está configurado para **NO subir**:
- ❌ `node_modules/` (demasiado pesado)
- ❌ `.next/` (archivos compilados)
- ❌ `target/` (archivos compilados del backend)
- ❌ `*.sql` (scripts de datos de prueba)
- ❌ Archivos temporales y logs

### Comandos Git:

```bash
# Ver archivos que se van a subir
git status

# Agregar todos los archivos (excepto los del .gitignore)
git add .

# Crear commit
git commit -m "Implementación completa de EcoHarmony Park"

# Subir al repositorio
git push origin main
```

---

## 🐛 Solución de Problemas

### El backend no arranca:

1. **Verifica que Java esté instalado**:
   ```bash
   java -version
   ```
   - Debería mostrar versión 17 o superior

2. **Verifica que Maven esté funcionando**:
   ```bash
   mvnw.cmd --version    # Windows
   ./mvnw --version      # Linux/Mac
   ```

3. **Verifica la conexión a la base de datos**:
   - Revisa `src/main/resources/application.properties`
   - Asegúrate de que las credenciales sean correctas

### El frontend no arranca:

1. **Verifica que Node.js esté instalado**:
   ```bash
   node -version
   npm -version
   ```

2. **Limpia la caché y reinstala**:
   ```bash
   cd frontend
   rm -rf node_modules package-lock.json .next
   npm install
   npm run dev
   ```

### El frontend no muestra las actividades:

1. **Verifica que el backend esté corriendo**: `http://localhost:8080/actividades`
2. **Revisa la consola del navegador** (F12) para ver errores
3. **Verifica CORS**: El backend debe estar en puerto 8080 y el frontend en 3000

### No se ven los estilos:

1. **Recarga sin caché**: Presiona **Ctrl + Shift + R** (o Cmd + Shift + R en Mac)
2. **Verifica que `globals.css` se esté cargando** en las DevTools → Network
3. **Limpia el caché de Next.js**:
   ```bash
   cd frontend
   rm -rf .next
   npm run dev
   ```

---

## 📞 Contacto

Si tienes problemas, verifica:
1. ✅ Backend corriendo en puerto 8080
2. ✅ Frontend corriendo en puerto 3000
3. ✅ Base de datos con datos de prueba insertados
4. ✅ Sin errores en las consolas

---

**¡Listo! Tu aplicación EcoHarmony Park debería estar funcionando correctamente.** 🎉
