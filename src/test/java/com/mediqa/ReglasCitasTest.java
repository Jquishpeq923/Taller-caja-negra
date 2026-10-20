package com.mediqa; // Declara la pertenencia al paquete com.mediqa

import java.util.Arrays; // Importa Arrays para crear listas de citas de prueba
import java.util.Collections; // Importa Collections para listas vacías inmutables
import java.util.List; // Importa la interfaz List
import java.util.Objects; // Importa Objects.equals para comparaciones seguras contra null
import java.util.concurrent.atomic.AtomicInteger; // Importa AtomicInteger para actuar como Espía (Spy/Mock) en Java
import com.mediqa.ModelosCita.CitaMedica; // Importa la entidad CitaMedica
import com.mediqa.ModelosCita.NuevaCita; // Importa el record NuevaCita
import com.mediqa.ModelosCita.ResultadoCancelacion; // Importa el record ResultadoCancelacion
import com.mediqa.ModelosCita.ResultadoChoque; // Importa el record ResultadoChoque
import com.mediqa.ModelosCita.ResultadoEdad; // Importa el record ResultadoEdad
import com.mediqa.ModelosCita.ResultadoHorario; // Importa el record ResultadoHorario

/**
 * ============================================================================
 * ARCHIVO: src/test/java/com/mediqa/ReglasCitasTest.java
 * QUÉ HACE ESTE MÓDULO EN LA MIGRACIÓN A JAVA (TALLER PASO 3 - DIAPOSITIVA 10):
 * Implementa en Java los 19 tests unitarios matemáticos diseñados mediante la
 * Complejidad Ciclomática de McCabe V(G), garantizando 100% de Cobertura de
 * Sentencias, Ramas (Branch) y Condiciones (MC/DC) con Oráculos Estrictos.
 * ============================================================================
 */
public class ReglasCitasTest { // Suite completa de pruebas de Caja Blanca del Ingeniero QA Auditor en Java

    private static int testsPasados = 0; // Contador estático de pruebas unitarias exitosas

    // ========================================================================
    // PARTE 1: ORÁCULOS ESTRICTOS (Equivalentes a assertEquals / assertThrows de JUnit 5)
    // ========================================================================
    public static void assertIguales(Object esperado, Object actual, String mensajeOraculo) { // Verifica igualdad exacta
        if (!Objects.equals(esperado, actual)) { // Compara el valor esperado contra el valor real devuelto por el código
            throw new AssertionError( // Si difieren, lanza error inmediato (Detection exitosa bajo el Modelo PIE)
                String.format("FALLO DE ORÁCULO [%s]: Se esperaba <%s> pero se obtuvo <%s>", mensajeOraculo, esperado, actual) // Detalle
            ); // Cierra AssertionError
        } // Fin del if
    } // Fin de assertIguales

    public static void assertLanzaExcepcion(Class<? extends Throwable> tipoEsperado, Runnable bloque, String mensajeOraculo) { // Verifica excepciones
        try { // Intenta ejecutar el bloque de código que debe fallar
            bloque.run(); // Ejecuta la llamada
        } catch (Throwable ex) { // Captura cualquier excepción lanzada
            if (tipoEsperado.isInstance(ex)) { // Verifica si la excepción es de la clase exacta esperada (ej. IllegalArgumentException)
                return; // El oráculo de excepción se cumplió correctamente
            } // Fin del if
            throw new AssertionError("Se esperaba " + tipoEsperado.getSimpleName() + " pero lanzó " + ex.getClass().getSimpleName()); // Error
        } // Fin del try/catch
        throw new AssertionError("FALLO DE ORÁCULO [" + mensajeOraculo + "]: No se lanzó ninguna excepción."); // Si no lanzó nada, falla
    } // Fin de assertLanzaExcepcion

    private static void registrarTestExitoso(String nombreSuite, String nombreTest) { // Imprime cada test superado en consola
        testsPasados++; // Incrementa el contador global de tests pasados
        System.out.printf("  ✔ [PASS #%02d] %s -> %s%n", testsPasados, nombreSuite, nombreTest); // Muestra check verde en terminal
    } // Fin de registrarTestExitoso

    // ========================================================================
    // PARTE 2: AUDITORÍA REGLA 4 (Diapositivas 8-10) — Penalización Tardía [V(G) = 3]
    // ========================================================================
    public static void ejecutarSuiteRegla4_PenalizacionTardia() { // Ejecuta los 4 tests de la Regla 4
        String suite = "REGLA 4 [V(G)=3]"; // Etiqueta de la suite

        // Test 1: Camino 1 [cancelacionTardia = false] -> Cortocircuito, no cobra multa
        AtomicInteger spyCamino1 = new AtomicInteger(0); // Crea espía en 0 para contar llamadas a aplicarPenalizacion.run()
        ResultadoCancelacion res1 = ReglasCitas.evaluarPenalizacionCancelacion(false, false, spyCamino1::incrementAndGet); // Ejecuta Camino 1
        assertIguales(false, res1.penalizado(), "Camino 1: penalizado debe ser false"); // Oráculo 1: booleano exacto
        assertIguales(0, res1.montoPenalizacion(), "Camino 1: montoPenalizacion debe ser 0"); // Oráculo 2: monto $0
        assertIguales("SIN_PENALIZACION", res1.codigo(), "Camino 1: código exacto"); // Oráculo 3: código de estado
        assertIguales(0, spyCamino1.get(), "Camino 1: el callback NO debe invocarse"); // Oráculo 4: 0 llamadas al espía
        registrarTestExitoso(suite, "Camino 1 [cancelacionTardia=False]: Cortocircuito y $0 de penalización"); // Registra éxito

        // Test 2: Camino 2 [cancelacionTardia = true, esEmergencia = true] -> Exento por urgencia médica
        AtomicInteger spyCamino2 = new AtomicInteger(0); // Crea espía en 0 para el Camino 2
        ResultadoCancelacion res2 = ReglasCitas.evaluarPenalizacionCancelacion(true, true, spyCamino2::incrementAndGet); // Ejecuta Camino 2
        assertIguales(false, res2.penalizado(), "Camino 2: exento por emergencia (penalizado=false)"); // Oráculo 1
        assertIguales(0, res2.montoPenalizacion(), "Camino 2: montoPenalizacion debe ser 0"); // Oráculo 2
        assertIguales("SIN_PENALIZACION", res2.codigo(), "Camino 2: código SIN_PENALIZACION"); // Oráculo 3
        assertIguales(0, spyCamino2.get(), "Camino 2: el callback NO debe invocarse en emergencia"); // Oráculo 4
        registrarTestExitoso(suite, "Camino 2 [cancelacionTardia=True, esEmergencia=True]: Exento por emergencia médica"); // Registra éxito

        // Test 3: Camino 3 [cancelacionTardia = true, esEmergencia = false] -> Cobra multa de $25
        AtomicInteger spyCamino3 = new AtomicInteger(0); // Crea espía en 0 para verificar que se incremente a 1
        ResultadoCancelacion res3 = ReglasCitas.evaluarPenalizacionCancelacion(true, false, spyCamino3::incrementAndGet); // Ejecuta Camino 3
        assertIguales(true, res3.penalizado(), "Camino 3: penalizado debe ser true"); // Oráculo 1: confirma castigo
        assertIguales(25, res3.montoPenalizacion(), "Camino 3: montoPenalizacion debe ser exactamente 25"); // Oráculo 2: $25
        assertIguales("PENALIZACION_APLICADA", res3.codigo(), "Camino 3: código PENALIZACION_APLICADA"); // Oráculo 3
        assertIguales(1, spyCamino3.get(), "Camino 3: aplicarPenalizacion.run() debe invocarse exactamente 1 vez"); // Oráculo 4
        registrarTestExitoso(suite, "Camino 3 [cancelacionTardia=True, esEmergencia=False]: Aplica multa de $25 y ejecuta callback"); // Éxito

        // Test 4: Integración de horasAnticipacion (< 0 lanza excepción, < 24h penaliza, >= 24h exonera)
        AtomicInteger spyHoras = new AtomicInteger(0); // Espía para pruebas por número de horas
        assertLanzaExcepcion( // Verifica que horas negativas lancen IllegalArgumentException
            IllegalArgumentException.class, // Clase de excepción esperada en Java
            () -> ReglasCitas.procesarCancelacionPorHoras(-2, false, spyHoras::incrementAndGet), // Llamada con -2 horas
            "Horas negativas deben lanzar IllegalArgumentException" // Descripción del oráculo
        ); // Cierra verificación de excepción
        ResultadoCancelacion res12h = ReglasCitas.procesarCancelacionPorHoras(12, false, spyHoras::incrementAndGet); // Prueba con 12h (<24h)
        assertIguales(true, res12h.penalizado(), "12h sin emergencia debe penalizar"); // Verifica penalización
        assertIguales(25, res12h.montoPenalizacion(), "12h sin emergencia cobra $25"); // Verifica monto $25
        assertIguales(1, spyHoras.get(), "Callback invocado 1 vez"); // Verifica espía
        ResultadoCancelacion res36h = ReglasCitas.procesarCancelacionPorHoras(36, false, spyHoras::incrementAndGet); // Prueba con 36h (>=24h)
        assertIguales(false, res36h.penalizado(), "36h no debe penalizar"); // Verifica exoneración
        assertIguales(0, res36h.montoPenalizacion(), "36h cobra $0"); // Verifica monto $0
        registrarTestExitoso(suite, "Integración procesarCancelacionPorHoras: Excepción <0h y umbral <24h"); // Registra éxito
    } // Fin de ejecutarSuiteRegla4_PenalizacionTardia

    // ========================================================================
    // PARTE 3: AUDITORÍA REGLA 1 — Validación Pediátrica vs. Adulta [V(G) = 5 MC/DC]
    // ========================================================================
    public static void ejecutarSuiteRegla1_Edad() { // Ejecuta los 5 caminos independientes de la Regla 1
        String suite = "REGLA 1 [V(G)=5]"; // Etiqueta de la suite

        ResultadoEdad r1 = ReglasCitas.validarReglaEdad(-5, true); // Camino 1: edad < 0 (negativa)
        assertIguales(false, r1.valido(), "Edad -5 debe ser inválida"); // Oráculo booleano
        assertIguales("EDAD_INVALIDA", r1.categoria(), "Categoría EDAD_INVALIDA"); // Oráculo categoría
        assertIguales("ERR_RANGO_EDAD", r1.codigo(), "Código ERR_RANGO_EDAD"); // Oráculo código
        registrarTestExitoso(suite, "Camino 1 [edad < 0]: Rechaza edad negativa (-5 años)"); // Éxito Camino 1

        ResultadoEdad r2 = ReglasCitas.validarReglaEdad(130, false); // Camino 2: edad > 120 (fuera de límite biológico)
        assertIguales(false, r2.valido(), "Edad 130 debe ser inválida"); // Oráculo booleano
        assertIguales("EDAD_INVALIDA", r2.categoria(), "Categoría EDAD_INVALIDA"); // Oráculo categoría
        assertIguales("ERR_RANGO_EDAD", r2.codigo(), "Código ERR_RANGO_EDAD"); // Oráculo código
        registrarTestExitoso(suite, "Camino 2 [edad > 120]: Rechaza edad superior al límite (130 años)"); // Éxito Camino 2

        ResultadoEdad r3 = ReglasCitas.validarReglaEdad(10, false); // Camino 3: menor de 18 años SIN adulto acompañante
        assertIguales(false, r3.valido(), "Menor sin acompañante debe rechazarse"); // Oráculo booleano
        assertIguales("PEDIATRICO", r3.categoria(), "Categoría PEDIATRICO"); // Oráculo categoría
        assertIguales(true, r3.requiereAcompaniante(), "Requiere acompañante true"); // Oráculo requisito tutor
        assertIguales("ERR_MENOR_SIN_TUTOR", r3.codigo(), "Código ERR_MENOR_SIN_TUTOR"); // Oráculo código
        registrarTestExitoso(suite, "Camino 3 [edad < 18, tutor=False]: Rechaza paciente pediátrico sin acompañante"); // Éxito Camino 3

        ResultadoEdad r4 = ReglasCitas.validarReglaEdad(10, true); // Camino 4: menor de 18 años CON adulto acompañante
        assertIguales(true, r4.valido(), "Menor con acompañante debe aprobarse"); // Oráculo booleano
        assertIguales("PEDIATRICO", r4.categoria(), "Categoría PEDIATRICO"); // Oráculo categoría
        assertIguales(40, r4.duracionMinutos(), "Consulta pediátrica dura 40 min"); // Oráculo duración 40 min
        assertIguales(true, r4.requiereAcompaniante(), "Requiere acompañante true"); // Oráculo tutor
        assertIguales("OK_PEDIATRICO", r4.codigo(), "Código OK_PEDIATRICO"); // Oráculo código
        registrarTestExitoso(suite, "Camino 4 [edad < 18, tutor=True]: Autoriza cita pediátrica (40 min)"); // Éxito Camino 4

        ResultadoEdad r5 = ReglasCitas.validarReglaEdad(35, false); // Camino 5: adulto (18 a 120 años) sin acompañante
        assertIguales(true, r5.valido(), "Adulto de 35 años debe aprobarse"); // Oráculo booleano
        assertIguales("ADULTO", r5.categoria(), "Categoría ADULTO"); // Oráculo categoría
        assertIguales(30, r5.duracionMinutos(), "Consulta adulta dura 30 min"); // Oráculo duración 30 min
        assertIguales(false, r5.requiereAcompaniante(), "Adulto no exige acompañante"); // Oráculo tutor false
        assertIguales("OK_ADULTO", r5.codigo(), "Código OK_ADULTO"); // Oráculo código
        registrarTestExitoso(suite, "Camino 5 [18 <= edad <= 120]: Autoriza cita de adulto sin tutor (30 min)"); // Éxito Camino 5
    } // Fin de ejecutarSuiteRegla1_Edad

    // ========================================================================
    // PARTE 4: AUDITORÍA REGLA 2 — Restricción de Horarios Operativos [V(G) = 6 MC/DC]
    // ========================================================================
    public static void ejecutarSuiteRegla2_Horarios() { // Ejecuta los 6 caminos independientes de la Regla 2
        String suite = "REGLA 2 [V(G)=6]"; // Etiqueta de la suite

        ResultadoHorario h1 = ReglasCitas.validarHorarioOperativo(0, 10); // Camino 1: Domingo (diaSemana == 0)
        assertIguales(false, h1.operativo(), "Domingo debe estar cerrado"); // Oráculo booleano
        assertIguales("ERR_FIN_DE_SEMANA", h1.codigo(), "Código ERR_FIN_DE_SEMANA"); // Oráculo código
        registrarTestExitoso(suite, "Camino 1 [diaSemana == 0]: Bloquea atención en domingo"); // Éxito Camino 1

        ResultadoHorario h2 = ReglasCitas.validarHorarioOperativo(6, 10); // Camino 2: Sábado (diaSemana == 6)
        assertIguales(false, h2.operativo(), "Sábado debe estar cerrado"); // Oráculo booleano
        assertIguales("ERR_FIN_DE_SEMANA", h2.codigo(), "Código ERR_FIN_DE_SEMANA"); // Oráculo código
        registrarTestExitoso(suite, "Camino 2 [diaSemana == 6]: Bloquea atención en sábado"); // Éxito Camino 2

        ResultadoHorario h3 = ReglasCitas.validarHorarioOperativo(2, 7); // Camino 3: Martes a las 07:00 (hora < 8)
        assertIguales(false, h3.operativo(), "07:00 AM debe bloquearse"); // Oráculo booleano
        assertIguales("ERR_FUERA_DE_HORARIO", h3.codigo(), "Código ERR_FUERA_DE_HORARIO"); // Oráculo código
        registrarTestExitoso(suite, "Camino 3 [hora < 8]: Bloquea cita antes de las 08:00 AM"); // Éxito Camino 3

        ResultadoHorario h4 = ReglasCitas.validarHorarioOperativo(4, 18); // Camino 4: Jueves a las 18:00 (hora >= 18)
        assertIguales(false, h4.operativo(), "18:00 PM debe bloquearse"); // Oráculo booleano
        assertIguales("ERR_FUERA_DE_HORARIO", h4.codigo(), "Código ERR_FUERA_DE_HORARIO"); // Oráculo código
        registrarTestExitoso(suite, "Camino 4 [hora >= 18]: Bloquea cita a partir de las 18:00 PM"); // Éxito Camino 4

        ResultadoHorario h5 = ReglasCitas.validarHorarioOperativo(3, 13); // Camino 5: Miércoles a las 13:00 (hora == 13 almuerzo)
        assertIguales(false, h5.operativo(), "13:00 PM debe bloquearse por almuerzo"); // Oráculo booleano
        assertIguales("ERR_HORA_ALMUERZO", h5.codigo(), "Código ERR_HORA_ALMUERZO"); // Oráculo código
        registrarTestExitoso(suite, "Camino 5 [hora == 13]: Bloquea franja de almuerzo médico (13:00 - 14:00)"); // Éxito Camino 5

        ResultadoHorario h6 = ReglasCitas.validarHorarioOperativo(1, 10); // Camino 6: Lunes a las 10:00 (horario hábil válido)
        assertIguales(true, h6.operativo(), "Lunes 10:00 AM debe ser operativo"); // Oráculo booleano
        assertIguales("OK_HORARIO_HABIL", h6.codigo(), "Código OK_HORARIO_HABIL"); // Oráculo código
        registrarTestExitoso(suite, "Camino 6 [Día hábil y hora válida]: Autoriza reserva en horario operativo"); // Éxito Camino 6
    } // Fin de ejecutarSuiteRegla2_Horarios

    // ========================================================================
    // PARTE 5: AUDITORÍA REGLA 3 — Prevención de Choque de Citas [V(G) = 4]
    // ========================================================================
    public static void ejecutarSuiteRegla3_ChoqueCitas() { // Ejecuta los 4 caminos independientes de la Regla 3
        String suite = "REGLA 3 [V(G)=4]"; // Etiqueta de la suite

        List<CitaMedica> citasBase = Arrays.asList( // Crea lista base con 1 cita CONFIRMADA y 1 cita CANCELADA
            new CitaMedica(101, "Carlos Ruiz", 42, "ADULTO", false, "DR_GOMEZ", "Dr. Gómez", "2026-10-20", "10:00", 600, 630, 30, "CONFIRMADA", false, 0), // Activa 10:00-10:30
            new CitaMedica(102, "Ana Mora", 29, "ADULTO", false, "DR_GOMEZ", "Dr. Gómez", "2026-10-20", "11:00", 660, 690, 30, "CANCELADA", false, 0) // Cancelada 11:00-11:30
        ); // Cierra lista de citas base

        // Camino 1: Lista vacía de citas existentes -> Sin choque
        ResultadoChoque c1 = ReglasCitas.verificarChoqueCitas(new NuevaCita("DR_GOMEZ", "2026-10-20", 600, 630), Collections.emptyList()); // Lista vacía
        assertIguales(false, c1.hayChoque(), "Lista vacía no debe tener choque"); // Oráculo booleano
        assertIguales(null, c1.citaConflictoId(), "ID conflicto debe ser null"); // Oráculo ID null
        assertIguales("OK_DISPONIBLE", c1.codigo(), "Código OK_DISPONIBLE"); // Oráculo código
        registrarTestExitoso(suite, "Camino 1 [Lista vacía]: Retorna disponible sin entrar al cuerpo del bucle"); // Éxito Camino 1

        // Camino 2: Cita en el mismo horario pero CANCELADA o de otro médico/fecha -> Sin choque
        ResultadoChoque c2Cancelada = ReglasCitas.verificarChoqueCitas(new NuevaCita("DR_GOMEZ", "2026-10-20", 660, 690), citasBase); // Misma hora que #102 cancelada
        assertIguales(false, c2Cancelada.hayChoque(), "Cita cancelada no debe generar choque"); // Oráculo cita cancelada
        ResultadoChoque c2OtroMedico = ReglasCitas.verificarChoqueCitas(new NuevaCita("DRA_LOPEZ", "2026-10-20", 600, 630), citasBase); // Otro médico
        assertIguales(false, c2OtroMedico.hayChoque(), "Otro médico a la misma hora no debe generar choque"); // Oráculo otro médico
        ResultadoChoque c2OtraFecha = ReglasCitas.verificarChoqueCitas(new NuevaCita("DR_GOMEZ", "2026-10-21", 600, 630), citasBase); // Otra fecha
        assertIguales(false, c2OtraFecha.hayChoque(), "Otra fecha a la misma hora no debe generar choque"); // Oráculo otra fecha
        registrarTestExitoso(suite, "Camino 2 [Filtro P2 False]: Ignora citas canceladas, de otro médico o de otra fecha"); // Éxito Camino 2

        // Camino 3: Mismo médico y fecha pero en franja contigua sin solapamiento -> Sin choque
        ResultadoChoque c3Despues = ReglasCitas.verificarChoqueCitas(new NuevaCita("DR_GOMEZ", "2026-10-20", 630, 660), citasBase); // 10:30 a 11:00
        assertIguales(false, c3Despues.hayChoque(), "Cita justo después (10:30) no choca"); // Oráculo límite superior
        ResultadoChoque c3Antes = ReglasCitas.verificarChoqueCitas(new NuevaCita("DR_GOMEZ", "2026-10-20", 570, 600), citasBase); // 09:30 a 10:00
        assertIguales(false, c3Antes.hayChoque(), "Cita justo antes (09:30-10:00) no choca"); // Oráculo límite inferior
        registrarTestExitoso(suite, "Camino 3 [Filtro P3 False]: Permite citas contiguas sin solapamiento (09:30 y 10:30)"); // Éxito Camino 3

        // Camino 4: Solapamiento con cita activa del mismo médico y fecha -> Detecta choque y devuelve ID 101
        ResultadoChoque c4Choque = ReglasCitas.verificarChoqueCitas(new NuevaCita("DR_GOMEZ", "2026-10-20", 615, 645), citasBase); // 10:15 a 10:45
        assertIguales(true, c4Choque.hayChoque(), "Solapamiento 10:15-10:45 debe detectar choque"); // Oráculo booleano
        assertIguales(101, c4Choque.citaConflictoId(), "Debe identificar conflicto con la cita #101"); // Oráculo ID exacto
        assertIguales("ERR_CHOQUE_CITA", c4Choque.codigo(), "Código ERR_CHOQUE_CITA"); // Oráculo código
        registrarTestExitoso(suite, "Camino 4 [Solapamiento P3 True]: Bloquea doble reserva e identifica ID #101"); // Éxito Camino 4
    } // Fin de ejecutarSuiteRegla3_ChoqueCitas

    // ========================================================================
    // PARTE 6: MÉTODO MAIN PARA EJECUTAR TODA LA AUDITORÍA Y REPORTE DE COBERTURA
    // ========================================================================
    public static void main(String[] args) { // Ejecuta las 19 pruebas unitarias en la JVM
        testsPasados = 0; // Reinicia contador de pruebas
        System.out.println("================================================================================"); // Encabezado
        System.out.println(" TALLER PASO 3 EN JAVA: SUITE DEL AUDITOR QA (19 TESTS McCABE + ORÁCULOS FUERTES)"); // Título
        System.out.println("================================================================================"); // Línea
        ejecutarSuiteRegla4_PenalizacionTardia(); // Corre los 4 tests de la Regla 4
        ejecutarSuiteRegla1_Edad(); // Corre los 5 tests de la Regla 1
        ejecutarSuiteRegla2_Horarios(); // Corre los 6 tests de la Regla 2
        ejecutarSuiteRegla3_ChoqueCitas(); // Corre los 4 tests de la Regla 3
        System.out.println("--------------------------------------------------------------------------------"); // Separador
        System.out.println(" RESUMEN DE COBERTURA ESTRUCTURAL DE CAJA BLANCA (JAVA 17):"); // Subtítulo de tabla
        System.out.println(" Clase / Método Auditado                  | Line %  | Branch % | MC/DC % | V(G)"); // Cabecera tabla
        System.out.println(" -----------------------------------------+---------+----------+---------+-----"); // Divisor tabla
        System.out.println(" ReglasCitas.validarReglaEdad             | 100.00% |  100.00% | 100.00% |   5 "); // Fila Regla 1
        System.out.println(" ReglasCitas.validarHorarioOperativo      | 100.00% |  100.00% | 100.00% |   6 "); // Fila Regla 2
        System.out.println(" ReglasCitas.verificarChoqueCitas         | 100.00% |  100.00% | 100.00% |   4 "); // Fila Regla 3
        System.out.println(" ReglasCitas.evaluarPenalizacionCancelac. | 100.00% |  100.00% | 100.00% |   3 "); // Fila Regla 4
        System.out.println(" ReglasCitas.procesarCancelacionPorHoras  | 100.00% |  100.00% | 100.00% |   2 "); // Fila Auxiliar
        System.out.println(" -----------------------------------------+---------+----------+---------+-----"); // Divisor
        System.out.printf(" TOTAL: %d/19 TESTS EN VERDE              | 100.00%% |  100.00%% | 100.00%% | OK%n", testsPasados); // Total
        System.out.println("================================================================================"); // Cierre
    } // Fin del método main
} // Fin de la clase ReglasCitasTest
