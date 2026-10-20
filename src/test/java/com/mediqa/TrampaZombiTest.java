package com.mediqa; // Declara la pertenencia al paquete com.mediqa

import com.mediqa.ModelosCita.ResultadoCancelacion; // Importa el record ResultadoCancelacion

/**
 * ============================================================================
 * ARCHIVO: src/test/java/com/mediqa/TrampaZombiTest.java
 * QUÉ HACE ESTE MÓDULO EN LA MIGRACIÓN A JAVA (TALLER PASO 2 - DIAPOSITIVA 9):
 * Reproduce en Java el error del "Desarrollador Copiador" que genera "Tests Zombis".
 * Con solo 2 pruebas superficiales (`true, false` y `false, false`), ejecuta el
 * 100% de las líneas del método `evaluarPenalizacionCancelacion`, pero:
 *   1) Omite el Camino 2 de McCabe (`cancelacionTardia=true, esEmergencia=true`).
 *   2) Falla en DETECTION (Modelo PIE) porque solo verifica `resultado != null`
 *      sin auditar el monto ($25) ni si `aplicarPenalizacion.run()` se ejecutó.
 * ============================================================================
 */
public class TrampaZombiTest { // Clase que demuestra el Falso Positivo de Cobertura en Java

    public static void testZombi1_RamaVerdaderaSinOraculoEstricto() { // Test Zombi 1: Entra al if pero no verifica nada útil
        ResultadoCancelacion resultado = ReglasCitas.evaluarPenalizacionCancelacion(true, false, () -> {}); // Ejecuta Camino 3 con lambda vacía
        if (resultado == null) { // ORÁCULO DÉBIL: Solo revisa que el objeto devuelto no sea null (como assertNotNull)
            throw new AssertionError("El resultado no debería ser null"); // Nunca falla aunque el monto esté mal
        } // Fin de la comprobación superficial
        System.out.println("  [PASS ENGAÑOSO] Test Zombi 1: Ejecutó la rama verdadera (solo verificó resultado != null)"); // Log de consola
    } // Fin de testZombi1

    public static void testZombi2_RamaFalsaSinOraculoEstricto() { // Test Zombi 2: Salta el if sin revisar valores
        ResultadoCancelacion resultado = ReglasCitas.evaluarPenalizacionCancelacion(false, false, () -> {}); // Ejecuta Camino 1
        if (resultado == null) { // ORÁCULO DÉBIL: Si un bug cobrara $25 aquí, ¡este test seguiría pasando en VERDE!
            throw new AssertionError("El resultado no debería ser null"); // Nunca detecta infección de estado
        } // Fin de la comprobación superficial
        System.out.println("  [PASS ENGAÑOSO] Test Zombi 2: Ejecutó la rama falsa (solo verificó resultado != null)"); // Log de consola
    } // Fin de testZombi2

    public static void main(String[] args) { // Punto de entrada para correr la demostración del Falso 100% en consola
        System.out.println("================================================================================"); // Encabezado
        System.out.println(" TALLER PASO 2 EN JAVA: SUITE INGENUA (TESTS ZOMBIS - FALSO POSITIVO DE LÍNEAS)"); // Título
        System.out.println("================================================================================"); // Cierre encabezado
        testZombi1_RamaVerdaderaSinOraculoEstricto(); // Corre el primer test zombi
        testZombi2_RamaFalsaSinOraculoEstricto(); // Corre el segundo test zombi
        System.out.println("--------------------------------------------------------------------------------"); // Separador
        System.out.println(" -> Líneas ejecutadas en evaluarPenalizacionCancelacion : 100.00% (FALSO POSITIVO)"); // Muestra engaño
        System.out.println(" -> Caminos de McCabe probados                          : 2 de 3 (Falta Camino 2: true, true)"); // Zona ciega
        System.out.println(" -> Estado del Modelo PIE                               : FALLO EN DETECTION (Oráculo Débil)"); // Diagnóstico
        System.out.println("================================================================================"); // Cierre
    } // Fin de main
} // Fin de la clase TrampaZombiTest
