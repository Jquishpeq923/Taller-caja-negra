/**
 * ============================================================================
 * ARCHIVO: tests/trampa_zombi.test.js
 * QUÉ HACE ESTE MÓDULO (TALLER PASO 2 - DIAPOSITIVA 9: EL ENGAÑO DEL FALSO 100%):
 * Reproduce el error clásico del "Desarrollador Copiador" que pide a la IA:
 * "Genera tests para este código".
 * Demuestra cómo con solo 2 pruebas superficiales se alcanza 100% de cobertura
 * de líneas en `evaluarPenalizacionCancelacion`, pero:
 *   1) Se deja sin probar el Camino 2 (cancelacionTardia=true, esEmergencia=true).
 *   2) Se usan aserciones débiles ("Tests Zombis") que fallan en la etapa de
 *      DETECTION del Modelo PIE (Execution -> Infection & Propagation -> Detection).
 * ============================================================================
 */

const assert = require('node:assert/strict'); // Importa el módulo de aserciones nativo de Node.js
const { evaluarPenalizacionCancelacion } = require('../src/reglasCitas'); // Importa la función de penalización tardía (Regla 4)

// Bloque de compatibilidad para que el archivo funcione tanto con Jest como con 'node --test'
let describeFn = global.describe; // Intenta usar la función global 'describe' si se ejecuta desde Jest
let testFn = global.test; // Intenta usar la función global 'test' si se ejecuta desde Jest

if (typeof describeFn !== 'function') { // Si no se está corriendo con Jest, sino con 'node --test' nativo
  const nodeTest = require('node:test'); // Carga el ejecutor de pruebas nativo de Node.js
  describeFn = nodeTest.describe; // Asigna 'describe' del módulo nativo node:test
  testFn = nodeTest.test; // Asigna 'test' del módulo nativo node:test
} // Fin del bloque de compatibilidad

// ============================================================================
// SUITE DE PRUEBAS INGENUA (2 TESTS ZOMBIS CON FALSO POSITIVO DE COBERTURA)
// ============================================================================
describeFn('TALLER PASO 2: Suite Ingenua (Tests Zombis - Falso Positivo de Líneas)', () => { // Define el grupo de pruebas ingenuas
  testFn('Test Zombi 1: Ejecuta la rama verdadera pero con aserción débil (sin oráculo estricto)', () => { // Primer test superficial
    const resultado = evaluarPenalizacionCancelacion(true, false, () => {}); // Llama a la función con tardía=true y emergencia=false (entra al if)
    assert.notStrictEqual(resultado, undefined); // ORÁCULO DÉBIL: Solo comprueba que no sea undefined, ¡no revisa si cobró los $25!
  }); // Fin del Test Zombi 1

  testFn('Test Zombi 2: Ejecuta la rama falsa sin verificar estado ni efectos secundarios', () => { // Segundo test superficial
    const resultado = evaluarPenalizacionCancelacion(false, false, () => {}); // Llama a la función con tardía=false (salta el bloque if)
    assert.strictEqual(typeof resultado, 'object'); // ORÁCULO DÉBIL: Solo revisa que devuelva un objeto; si cobrara multa por error, ¡pasa en VERDE!
  }); // Fin del Test Zombi 2
  // NOTA CRÍTICA PARA TU EXPOSICIÓN: Observa que aquí NUNCA se llamó a evaluarPenalizacionCancelacion(true, true, ...),
  // es decir, el Camino 2 de McCabe (paciente con emergencia médica) quedó en una zona ciega total aunque todas las líneas se tocaron.
}); // Fin de la suite de Tests Zombis
