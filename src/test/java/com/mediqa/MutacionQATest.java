package com.mediqa; // Declara la pertenencia al paquete com.mediqa

import java.util.List; // Importa List para almacenar la lista de mutantes
import java.util.concurrent.atomic.AtomicInteger; // Importa AtomicInteger para el espía de efectos secundarios
import com.mediqa.ModelosCita.ResultadoCancelacion; // Importa el record ResultadoCancelacion

/**
 * ============================================================================
 * ARCHIVO: src/test/java/com/mediqa/MutacionQATest.java
 * QUÉ HACE ESTE MÓDULO EN LA MIGRACIÓN A JAVA (DIAPOSITIVA 12 - PREGUNTA 4):
 * Ejecuta en Java las Pruebas de Mutación bajo el Modelo PIE (Execution,
 * Infection & Propagation, Detection). Siembra 3 mutantes (bugs intencionales)
 * y demuestra que el Test Zombi mata 0/3 (0%), mientras que la Suite del
 * Auditor QA en Java mata 3/3 (100%).
 * ============================================================================
 */
public class MutacionQATest { // Clase ejecutora de pruebas de mutación en Java 17

    @FunctionalInterface // Anotación oficial de Java para interfaz funcional de la regla bajo prueba
    public interface ReglaPenalizacionFuncional { // Define la firma que comparten la función real y los 3 mutantes
        ResultadoCancelacion evaluar(boolean cancelacionTardia, boolean esEmergencia, Runnable aplicarPenalizacion); // Método abstracto
    } // Fin de la interfaz ReglaPenalizacionFuncional

    public record Mutante( // Record que describe cada bug intencional sembrado
        String id, // Identificador del mutante ("MUTANTE_1", "MUTANTE_2", "MUTANTE_3")
        String nombre, // Qué operador o instrucción se alteró en el código Java
        String descripcion, // Qué fallo provoca en la clínica médica
        ReglaPenalizacionFuncional implementacion // Lambda con el código mutado defectuoso
    ) {} // Cierra record Mutante

    // Ejecuta el Test Zombi (Diapositiva 9) contra un mutante dado
    public static void ejecutarTestZombiContra(ReglaPenalizacionFuncional fnBajoPrueba) { // Simula la suite ingenua
        ResultadoCancelacion r1 = fnBajoPrueba.evaluar(true, false, () -> {}); // Llama (true, false) sin espía
        ResultadoCancelacion r2 = fnBajoPrueba.evaluar(false, false, () -> {}); // Llama (false, false) sin espía
        if (r1 == null || r2 == null) { // Oráculo débil: solo mira que no devuelvan null
            throw new AssertionError("Retornó null"); // Jamás detecta alteraciones en el monto ni en las condiciones
        } // Fin del if
    } // Fin de ejecutarTestZombiContra

    // Ejecuta los 3 Tests Matemáticos del Auditor QA (Diapositiva 10) contra un mutante dado
    public static void ejecutarTestAuditorContra(ReglaPenalizacionFuncional fnBajoPrueba) { // Suite con oráculos estrictos
        AtomicInteger llamadasC1 = new AtomicInteger(0); // Espía para el Camino 1
        ResultadoCancelacion r1 = fnBajoPrueba.evaluar(false, false, llamadasC1::incrementAndGet); // Evalúa Camino 1
        ReglasCitasTest.assertIguales(false, r1.penalizado(), "C1 penalizado"); // Exige false
        ReglasCitasTest.assertIguales(0, r1.montoPenalizacion(), "C1 monto"); // Exige $0
        ReglasCitasTest.assertIguales(0, llamadasC1.get(), "C1 llamadas"); // Exige 0 llamadas

        AtomicInteger llamadasC2 = new AtomicInteger(0); // Espía para el Camino 2
        ResultadoCancelacion r2 = fnBajoPrueba.evaluar(true, true, llamadasC2::incrementAndGet); // Evalúa Camino 2 (emergencia)
        ReglasCitasTest.assertIguales(false, r2.penalizado(), "C2 penalizado"); // Exige false
        ReglasCitasTest.assertIguales(0, r2.montoPenalizacion(), "C2 monto"); // Exige $0
        ReglasCitasTest.assertIguales(0, llamadasC2.get(), "C2 llamadas"); // Exige 0 llamadas

        AtomicInteger llamadasC3 = new AtomicInteger(0); // Espía para el Camino 3
        ResultadoCancelacion r3 = fnBajoPrueba.evaluar(true, false, llamadasC3::incrementAndGet); // Evalúa Camino 3 (multa)
        ReglasCitasTest.assertIguales(true, r3.penalizado(), "C3 penalizado"); // Exige true
        ReglasCitasTest.assertIguales(25, r3.montoPenalizacion(), "C3 monto"); // Exige $25
        ReglasCitasTest.assertIguales(1, llamadasC3.get(), "C3 llamadas"); // Exige 1 llamada exacta
    } // Fin de ejecutarTestAuditorContra

    public static void main(String[] args) { // Punto de entrada del laboratorio de mutación en Java
        List<Mutante> mutantes = List.of( // Crea la lista inmutable con los 3 mutantes sembrados
            new Mutante( // Mutante 1: Cambia && por ||
                "MUTANTE_1", // ID
                "Operador lógico invertido: if (cancelacionTardia || !esEmergencia)", // Mutación
                "Cobra penalización de $25 incluso si el paciente cancela con 48h de anticipación.", // Efecto
                (tardia, emergencia, accion) -> { // Implementación mutada 1
                    if (tardia || !emergencia) { // BUG SEMBRADO: || en vez de &&
                        accion.run(); // Ejecuta acción de cobro por error
                        return new ResultadoCancelacion(true, 25, "PENALIZACION_APLICADA", "Multa"); // Retorna $25
                    } // Fin del if
                    return new ResultadoCancelacion(false, 0, "SIN_PENALIZACION", "Sin multa"); // Retorno falso
                } // Fin lambda Mutante 1
            ), // Cierra Mutante 1
            new Mutante( // Mutante 2: Elimina el operador ! en !esEmergencia
                "MUTANTE_2", // ID
                "Negación omitida: if (cancelacionTardia && esEmergencia)", // Mutación
                "Penaliza a los pacientes en emergencia médica real y exonera a quienes no tienen justificación.", // Efecto
                (tardia, emergencia, accion) -> { // Implementación mutada 2
                    if (tardia && emergencia) { // BUG SEMBRADO: falta el operador '!' antes de emergencia
                        accion.run(); // Cobra multa a pacientes en emergencia
                        return new ResultadoCancelacion(true, 25, "PENALIZACION_APLICADA", "Multa"); // Retorna $25
                    } // Fin del if
                    return new ResultadoCancelacion(false, 0, "SIN_PENALIZACION", "Sin multa"); // Retorno falso
                } // Fin lambda Mutante 2
            ), // Cierra Mutante 2
            new Mutante( // Mutante 3: Omite invocar aplicarPenalizacion.run()
                "MUTANTE_3", // ID
                "Omisión de efecto secundario: olvida llamar a aplicarPenalizacion.run()", // Mutación
                "Devuelve el record con monto=25 pero jamás ejecuta el callback que registra el cargo en BD.", // Efecto
                (tardia, emergencia, _accion) -> { // Implementación mutada 3
                    if (tardia && !emergencia) { // Evalúa bien la condición pero...
                        // BUG SEMBRADO: Se borró la línea accion.run();
                        return new ResultadoCancelacion(true, 25, "PENALIZACION_APLICADA", "Multa"); // Engaña si no hay espía
                    } // Fin del if
                    return new ResultadoCancelacion(false, 0, "SIN_PENALIZACION", "Sin multa"); // Retorno falso
                } // Fin lambda Mutante 3
            ) // Cierra Mutante 3
        ); // Fin de la lista de mutantes

        System.out.println("================================================================================"); // Encabezado
        System.out.println(" REPORTE DE PRUEBAS DE MUTACIÓN EN JAVA (MODELO PIE: Execution->Infection->Detection)"); // Título
        System.out.println("================================================================================\n"); // Salto

        int matadosPorZombi = 0; // Contador de mutantes detectados por el Test Zombi
        int matadosPorAuditor = 0; // Contador de mutantes detectados por la Suite del Auditor QA

        for (Mutante m : mutantes) { // Recorre cada uno de los 3 mutantes
            boolean zombiDetecto = false; // Indica si el Test Zombi lanzó AssertionError
            boolean auditorDetecto = false; // Indica si la Suite del Auditor QA lanzó AssertionError

            try { // Prueba el Test Zombi contra el mutante
                ejecutarTestZombiContra(m.implementacion()); // Corre el test débil
            } catch (AssertionError _err) { // Si fallara la aserción
                zombiDetecto = true; // Marcaría detección
                matadosPorZombi++; // Incrementaría contador
            } // Fin try/catch zombi

            try { // Prueba la Suite del Auditor QA contra el mutante
                ejecutarTestAuditorContra(m.implementacion()); // Corre los 3 tests con oráculos estrictos
            } catch (AssertionError _err) { // Al fallar un oráculo estricto, el mutante es eliminado con éxito
                auditorDetecto = true; // Marca que el Auditor QA detectó el bug
                matadosPorAuditor++; // Incrementa el contador de mutantes eliminados
            } // Fin try/catch auditor

            System.out.println("[" + m.id() + "] " + m.nombre()); // Imprime ID y nombre del mutante
            System.out.println("  - Descripción del bug : " + m.descripcion()); // Imprime el impacto del bug
            System.out.println("  - Test Zombi (Falso 100%) : " + (zombiDetecto ? "MATÓ AL MUTANTE" : "SOBREVIVIÓ (Falso Positivo - No detectó el bug)")); // Resultado zombi
            System.out.println("  - Test Auditor QA (McCabe): " + (auditorDetecto ? "MATÓ AL MUTANTE (Bug detectado con éxito)" : "SOBREVIVIÓ") + "\n"); // Resultado auditor
        } // Fin del bucle for

        System.out.println("--------------------------------------------------------------------------------"); // Línea
        System.out.printf(" Mutation Score en Java (Test Zombi Ingenuo) : %d/%d (0%% efectividad real)%n", matadosPorZombi, mutantes.size()); // 0/3
        System.out.printf(" Mutation Score en Java (Suite Auditor QA)   : %d/%d (100%% efectividad real)%n", matadosPorAuditor, mutantes.size()); // 3/3
        System.out.println("================================================================================"); // Cierre
    } // Fin de main
} // Fin de la clase MutacionQATest
