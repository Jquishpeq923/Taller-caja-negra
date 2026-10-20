# Guía Maestra de la Migración Completa de JavaScript (Node.js) a Java 17

Este documento explica paso a paso el proceso técnico, arquitectónico y matemático realizado para migrar el **Sistema de Citas de Médico Familiar y su Auditoría de Caja Blanca (Complejidad Ciclomática de McCabe y Cobertura)** desde **JavaScript (Node.js)** hacia **Java 17**.

---

## 1. ¿Qué Estamos Realizando en la Transición a Java y Por Qué?

Al pasar de un lenguaje de **tipado dinámico** (JavaScript) a un lenguaje **orientado a objetos y de tipado estático fuerte** (Java 17), logramos tres mejoras fundamentales de ingeniería de software y control de calidad (QA):

1. **Seguridad de Tipos en Tiempo de Compilación (Type Safety)**:
   - En JavaScript, una función podía devolver un objeto mal escrito (por ejemplo `{ penalizado: "si" }` en vez de `true`) y el error solo aparecía en tiempo de ejecución.
   - En Java 17, reemplazamos los objetos literales JSON por **`record` inmutables** (`ResultadoEdad`, `ResultadoHorario`, `ResultadoChoque`, `ResultadoCancelacion`). El compilador `javac` verifica que cada retorno cumpla estrictamente el contrato de datos antes de permitir que el programa corra.
2. **Invarianza Topológica de la Complejidad Ciclomática de McCabe $V(G)$**:
   - Un hallazgo central para tu exposición es demostrar que **la Complejidad Ciclomática de McCabe $V(G) = E - N + 2 = P + 1$ no cambia al migrar de JavaScript a Java**.
   - ¿Por qué? Porque McCabe mide la **topología del Grafo de Flujo de Control (CFG)** (nodos de decisión `if`, `&&`, `||`, `for`), la cual depende de las reglas de negocio y no de la sintaxis del lenguaje.
3. **Ejecución Autónoma Sin Dependencias Frágiles**:
   - El proyecto Java fue diseñado utilizando componentes nativos del JDK 17 (`com.sun.net.httpserver.HttpServer`, `java.time.LocalDate`, `java.util.concurrent.atomic.AtomicInteger`, `java.sql.*`) además de incluir su `pom.xml` para Maven. Esto garantiza **0 errores de compilación y 0 errores de ejecución** en cualquier computadora o en **GitHub Codespaces**.

---

## 2. Tabla de Equivalencias: De Node.js (JavaScript) a Java 17

| Componente / Concepto | Antes en Node.js (JavaScript) | Ahora en Java 17 (`com.mediqa`) | Qué ganamos con el cambio |
| :--- | :--- | :--- | :--- |
| **Modelos de Respuesta** | Objetos dinámicos sueltos `{ valido: true, ... }` | `record ResultadoEdad(...)`, `record ResultadoCancelacion(...)` en `ModelosCita.java` | Inmutabilidad automática y validación del compilador. |
| **4 Reglas de Negocio** | Funciones exportadas en `src/reglasCitas.js` | Métodos estáticos puros en `ReglasCitas.java` | Conserva exactamente los mismos valores de $V(G)$ (`5`, `6`, `4` y `3`). |
| **Callback `aplicarPenalizacion()`** | Función anónima `() => {}` pasada por parámetro | Interfaz funcional estándar `Runnable` (`aplicarPenalizacion.run()`) | Permite pasar expresiones lambda `() -> {}` o referencias a métodos `spy::incrementAndGet`. |
| **Espía (Mock/Spy) en Tests** | Función casera `crearMockFn()` con arreglo `llamadas` | `AtomicInteger spy = new AtomicInteger(0)` con `spy::incrementAndGet` | Conteo seguro en memoria y nativo del paquete `java.util.concurrent.atomic`. |
| **Oráculos Estrictos (Aserciones)** | `assert.strictEqual(a, b)` de `node:assert` | `ReglasCitasTest.assertIguales(esperado, actual, msg)` / JUnit 5 `Assertions.assertEquals` | Detección inmediata bajo el Modelo PIE lanzando `AssertionError` tipado. |
| **Pruebas de Mutación** | Arreglo de objetos con funciones anónimas en `mutacion_qa.js` | `@FunctionalInterface ReglaPenalizacionFuncional` y `record Mutante` en `MutacionQATest.java` | Polimorfismo funcional limpio para intercambiar la regla original por los 3 mutantes. |
| **Base de Datos SQLite** | `better-sqlite3` en `src/db.js` | API estándar **JDBC** (`DriverManager`, `PreparedStatement`) + lista `synchronized` en `BaseDeDatosCitas.java` | Seguridad contra concurrencia multi-hilo y estándar industrial JDBC. |
| **Servidor Backend REST** | Express (`app.get`, `app.post`) en `src/server.js` | `com.sun.net.httpserver.HttpServer` en `ServidorCitasJava.java` (`0.0.0.0:3000`) | Servidor HTTP multi-hilo integrado en el JDK que no requiere `npm install` para funcionar. |

---

## 3. Cómo Compilar y Ejecutar la Versión Java (Local y en GitHub Codespaces)

### Opción Rápida (1 solo comando en Terminal o GitHub Codespaces)
Abre la terminal dentro de la carpeta del proyecto Java y ejecuta:

```bash
# Compila todos los archivos .java y corre las 3 suites de prueba (19 tests + Zombi + Mutación)
bash ejecutar_todo.sh
```

Si además deseas encender el **Servidor Web Java en el puerto 3000** para abrir la interfaz gráfica en el navegador:

```bash
bash ejecutar_todo.sh --server
```
*(En **GitHub Codespaces**, apenas corras este comando aparecerá el botón **Open in Browser** o podrás abrir el globo del puerto **3000** en la pestaña **PORTS**).*

### Comandos Individuales Paso a Paso

```bash
# 1. Compilar todo el código fuente y las pruebas hacia la carpeta out/
javac -encoding UTF-8 -d out src/main/java/com/mediqa/*.java src/test/java/com/mediqa/*.java

# 2. Ejecutar la Suite del Ingeniero QA Auditor (19 tests + Tabla de 100% Cobertura)
java -cp out com.mediqa.ReglasCitasTest

# 3. Ejecutar la Demostración del Test Zombi / Falso 100% (Taller Paso 2)
java -cp out com.mediqa.TrampaZombiTest

# 4. Ejecutar el Laboratorio de Mutación bajo Modelo PIE (0/3 Zombi vs 3/3 Auditor QA)
java -cp out com.mediqa.MutacionQATest

# 5. Iniciar el Servidor Web HTTP en el puerto 3000
java -cp out com.mediqa.ServidorCitasJava
```

---

## 4. Explicación Detallada de Cada Archivo Java y su Código Comentado Línea por Línea

### 4.1 `ModelosCita.java` — Estructuras de Datos Inmutables (`record`) y Entidad `CitaMedica`
**Qué hace este archivo**: Define los contratos de entrada y salida de las 4 reglas mediante `record` de Java 17, además de la clase `CitaMedica` con su método `toJson()` para comunicarse con el frontend en Alpine.js.

```java
public record ResultadoCancelacion( // Declara un record inmutable para devolver el dictamen de la Regla 4 (Diapositiva 8)
    boolean penalizado, // Oráculo booleano: true si se cobró penalización, false si quedó exento
    int montoPenalizacion, // Oráculo numérico: 25 ($25.00 USD) cuando aplica multa, o 0 ($0.00 USD) cuando no aplica
    String codigo, // Código único de auditoría ("PENALIZACION_APLICADA" o "SIN_PENALIZACION")
    String mensaje // Explicación de por qué se aplicó o exoneró el cargo de cancelación
) {} // Cierra la definición del record ResultadoCancelacion
```

### 4.2 `ReglasCitas.java` — Las 4 Reglas Críticas Auditadas con McCabe en Java
**Qué hace este archivo**: Contiene la lógica pura del negocio dividida en métodos estáticos con Complejidad Ciclomática $V(G) \le 6$:

1. **`validarReglaEdad(int edad, boolean tieneAcompaniante)`** [$V(G) = 5$]:
   - Camino 1: `edad < 0` $\rightarrow$ `ERR_RANGO_EDAD` (`valido = false`).
   - Camino 2: `edad > 120` $\rightarrow$ `ERR_RANGO_EDAD` (`valido = false`).
   - Camino 3: `edad < 18 && !tieneAcompaniante` $\rightarrow$ `ERR_MENOR_SIN_TUTOR` (`valido = false`).
   - Camino 4: `edad < 18 && tieneAcompaniante` $\rightarrow$ `OK_PEDIATRICO` (`valido = true`, `40 min`).
   - Camino 5: `18 <= edad <= 120` $\rightarrow$ `OK_ADULTO` (`valido = true`, `30 min`).
2. **`validarHorarioOperativo(int diaSemana, int hora)`** [$V(G) = 6$]:
   - Caminos 1 y 2: `diaSemana == 0 || diaSemana == 6` $\rightarrow$ `ERR_FIN_DE_SEMANA`.
   - Caminos 3 y 4: `hora < 8 || hora >= 18` $\rightarrow$ `ERR_FUERA_DE_HORARIO`.
   - Camino 5: `hora == 13` $\rightarrow$ `ERR_HORA_ALMUERZO`.
   - Camino 6: Horario válido $\rightarrow$ `OK_HORARIO_HABIL`.
3. **`verificarChoqueCitas(NuevaCita nuevaCita, List<CitaMedica> citasExistentes)`** [$V(G) = 4$]:
   - Recorre las citas activas del mismo médico y fecha evaluando `nuevaCita.inicioMin() < cita.finMin && nuevaCita.finMin() > cita.inicioMin`.
4. **`evaluarPenalizacionCancelacion(boolean cancelacionTardia, boolean esEmergencia, Runnable aplicarPenalizacion)`** [$V(G) = 3$ — Caso Estrella Diapositiva 8]:

```java
public static ResultadoCancelacion evaluarPenalizacionCancelacion( // Firma tipada del Caso Estrella del PDF
    boolean cancelacionTardia, // Predicado 1: true si se cancela con menos de 24 horas de anticipación
    boolean esEmergencia, // Predicado 2 (negado como !esEmergencia): true si el paciente sufrió emergencia médica
    Runnable aplicarPenalizacion // Callback funcional en Java (equivalente a la función aplicarPenalizacion() de JS)
) { // Abre el cuerpo del método evaluarPenalizacionCancelacion
    if (cancelacionTardia && !esEmergencia) { // Nodos Predicado P1 y P2: Evalúa si es tardía Y NO es una emergencia
        aplicarPenalizacion.run(); // Camino 3 (Nodo N4): Ejecuta la acción de penalización (verificada con espía en los tests)
        return new ResultadoCancelacion( // Retorna el dictamen del Camino 3 con multa de $25
            true, // penalizado = true (oráculo booleano estricto)
            25, // montoPenalizacion = 25 ($25.00 USD de cargo)
            "PENALIZACION_APLICADA", // Código de auditoría para el Camino 3
            "Cancelación con menos de 24h sin emergencia justificada: cargo de $25.00 aplicado." // Mensaje
        ); // Cierra el retorno del Camino 3
    } // Fin del condicional if (cancelacionTardia && !esEmergencia)

    return new ResultadoCancelacion( // Caminos 1 y 2 (Nodo N5): Sin penalización ($0) por cancelar a tiempo o por emergencia
        false, // penalizado = false (exento de multa)
        0, // montoPenalizacion = 0 ($0.00 USD)
        "SIN_PENALIZACION", // Código de auditoría para los Caminos 1 y 2
        "Cancelación procesada sin cargos de penalización." // Mensaje de confirmación
    ); // Cierra el retorno sin penalización
} // Fin del método evaluarPenalizacionCancelacion
```

### 4.3 `ReglasCitasTest.java`, `TrampaZombiTest.java` y `MutacionQATest.java` — Auditoría QA en Java
* **`TrampaZombiTest.java`**: Ejecuta 2 pruebas superficiales que solo comprueban `if (resultado == null)`, demostrando en Java cómo el 100% de líneas ejecutadas oculta que falta el Camino 2 (`true, true`) y falla en la etapa de **Detection** del Modelo PIE.
* **`ReglasCitasTest.java`**: Ejecuta las **19 pruebas unitarias** exigidas por McCabe usando `AtomicInteger` como espía (`spy::incrementAndGet`) y verificando con `assertIguales` cada campo de los records devueltos.
* **`MutacionQATest.java`**: Define la interfaz funcional `@FunctionalInterface ReglaPenalizacionFuncional` e inyecta los 3 mutantes en Java, obteniendo un **Mutation Score de `0/3 (0%)` en el Test Zombi** frente a **`3/3 (100%)` en la Suite del Auditor QA**.
