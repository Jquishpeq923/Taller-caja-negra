/**
 * ============================================================================
 * ARCHIVO: tests/mutacion_qa.js
 * QUÉ HACE ESTE MÓDULO (DIAPOSITIVA 12 - PREGUNTA 4 DEL CUESTIONARIO):
 * Responde con evidencia real a la pregunta:
 * "¿He forzado intencionalmente fallos (mutaciones) para verificar si los tests
 *  de la IA los detectan?"
 * Crea 3 versiones mutadas (con bugs intencionales) de la función de penalización
 * y las enfrenta contra el Test Zombi (Paso 2) y contra el Test Auditor QA (Paso 3).
 * ============================================================================
 */

const assert = require('node:assert/strict'); // Importa el módulo de aserciones estrictas para evaluar los mutantes

// ============================================================================
// PARTE 1: DEFINICIÓN DE LOS 3 MUTANTES (BUGS INTENCIONALES INYECTADOS)
// ============================================================================
const mutantes = [ // Arreglo que contiene las 3 funciones con defectos lógicos sembrados a propósito
  { // Primer Mutante: Cambio de operador booleano && por ||
    id: 'MUTANTE_1', // Identificador del primer experimento de mutación
    nombre: 'Operador lógico invertido: if (cancelacionTardia || !esEmergencia)', // Título descriptivo del fallo
    descripcion: 'Cobra penalización incluso si se cancela con más de 24h de anticipación.', // Consecuencia en producción
    fn: (cancelacionTardia, esEmergencia, aplicarPenalizacion) => { // Función mutada 1
      if (cancelacionTardia || !esEmergencia) { // BUG SEMBRADO: Se cambió '&&' por '||', entrando al if cuando no debe
        aplicarPenalizacion(); // Ejecuta el cobro de penalización erróneamente
        return { penalizado: true, montoPenalizacion: 25 }; // Devuelve multa de $25
      } // Cierra el if mutado
      return { penalizado: false, montoPenalizacion: 0 }; // Retorno sin penalización
    } // Fin de la función del Mutante 1
  }, // Cierra el objeto del Mutante 1
  { // Segundo Mutante: Eliminación del operador de negación '!' en !esEmergencia
    id: 'MUTANTE_2', // Identificador del segundo experimento de mutación
    nombre: 'Negación omitida: if (cancelacionTardia && esEmergencia)', // Título descriptivo del fallo
    descripcion: 'Penaliza a los pacientes que tienen una emergencia médica y exonera a los que no.', // Consecuencia clínica grave
    fn: (cancelacionTardia, esEmergencia, aplicarPenalizacion) => { // Función mutada 2
      if (cancelacionTardia && esEmergencia) { // BUG SEMBRADO: Falta el signo '!', invirtiendo la regla de emergencia
        aplicarPenalizacion(); // Cobra multa justamente a quien está en urgencias médicas
        return { penalizado: true, montoPenalizacion: 25 }; // Devuelve multa de $25
      } // Cierra el if mutado
      return { penalizado: false, montoPenalizacion: 0 }; // Retorno sin penalización
    } // Fin de la función del Mutante 2
  }, // Cierra el objeto del Mutante 2
  { // Tercer Mutante: Eliminación del efecto secundario aplicarPenalizacion()
    id: 'MUTANTE_3', // Identificador del tercer experimento de mutación
    nombre: 'Omisión de llamada a efecto secundario: no invoca aplicarPenalizacion()', // Título del fallo
    descripcion: 'Devuelve el objeto pero olvida registrar el cargo real en el sistema.', // Consecuencia financiera
    fn: (cancelacionTardia, esEmergencia, _aplicarPenalizacion) => { // Función mutada 3
      if (cancelacionTardia && !esEmergencia) { // Evalúa la condición pero...
        // BUG SEMBRADO: Se omitió la línea aplicarPenalizacion();
        return { penalizado: true, montoPenalizacion: 25 }; // Retorna el JSON engañando a quien no use espía (mock)
      } // Cierra el if
      return { penalizado: false, montoPenalizacion: 0 }; // Retorno sin penalización
    } // Fin de la función del Mutante 3
  } // Cierra el objeto del Mutante 3
]; // Fin del arreglo de mutantes

// ============================================================================
// PARTE 2: EJECUTORES DE PRUEBA (TEST ZOMBI VS. TEST AUDITOR QA)
// ============================================================================
function ejecutarTestZombiContra(fnBajoPrueba) { // Simula la suite ingenua del Desarrollador Copiador (Diapositiva 9)
  const r1 = fnBajoPrueba(true, false, () => {}); // Ejecuta caso (true, false) sin espía
  const r2 = fnBajoPrueba(false, false, () => {}); // Ejecuta caso (false, false) sin espía
  assert.ok(r1 !== undefined && typeof r2 === 'object'); // Oráculo débil: solo mira que devuelva objetos definidos
} // Fin de ejecutarTestZombiContra

function ejecutarTestAuditorContra(fnBajoPrueba) { // Ejecuta los 3 caminos de McCabe con Oráculos Estrictos (Diapositiva 10)
  let llamadasC1 = 0; // Contador de invocaciones del callback para el Camino 1
  const r1 = fnBajoPrueba(false, false, () => { llamadasC1++; }); // Prueba Camino 1 (false, false)
  assert.strictEqual(r1.penalizado, false); // Exige que penalizado sea false
  assert.strictEqual(r1.montoPenalizacion, 0); // Exige que monto sea $0
  assert.strictEqual(llamadasC1, 0); // Exige que el callback no se haya llamado

  let llamadasC2 = 0; // Contador de invocaciones del callback para el Camino 2
  const r2 = fnBajoPrueba(true, true, () => { llamadasC2++; }); // Prueba Camino 2 (true, true: emergencia médica)
  assert.strictEqual(r2.penalizado, false); // Exige que penalizado sea false por emergencia
  assert.strictEqual(r2.montoPenalizacion, 0); // Exige que monto sea $0
  assert.strictEqual(llamadasC2, 0); // Exige que el callback no se haya llamado

  let llamadasC3 = 0; // Contador de invocaciones del callback para el Camino 3
  const r3 = fnBajoPrueba(true, false, () => { llamadasC3++; }); // Prueba Camino 3 (true, false: tardía sin emergencia)
  assert.strictEqual(r3.penalizado, true); // Exige que penalizado sea true
  assert.strictEqual(r3.montoPenalizacion, 25); // Exige que el monto sea exactamente $25
  assert.strictEqual(llamadasC3, 1); // Exige que aplicarPenalizacion() se haya llamado exactamente 1 vez
} // Fin de ejecutarTestAuditorContra

// ============================================================================
// PARTE 3: COMPARACIÓN Y REPORTE DE MUTATION SCORE EN CONSOLA
// ============================================================================
console.log('================================================================================'); // Imprime encabezado
console.log(' REPORTE DE PRUEBAS DE MUTACIÓN (MODELO PIE: Execution -> Infection -> Detection)'); // Título del reporte
console.log('================================================================================\n'); // Cierra encabezado

let mutantesMatadosPorZombi = 0; // Contador de bugs detectados por el Test Zombi
let mutantesMatadosPorAuditor = 0; // Contador de bugs detectados por la Suite del Auditor QA

for (const m of mutantes) { // Recorre cada uno de los 3 mutantes
  let zombiDetecto = false; // Bandera para saber si el Test Zombi falló (detectó el bug)
  let auditorDetecto = false; // Bandera para saber si el Test Auditor falló (detectó el bug)

  try { // Ejecuta el Test Zombi contra el mutante actual
    ejecutarTestZombiContra(m.fn); // Si no lanza error, el mutante sobrevivió (falso positivo)
  } catch (_e) { // Si lanzó error de aserción, el test mató al mutante
    zombiDetecto = true; // Marca que el zombi lo detectó
    mutantesMatadosPorZombi++; // Incrementa el contador del zombi
  } // Fin try/catch del Test Zombi

  try { // Ejecuta la Suite del Auditor QA contra el mutante actual
    ejecutarTestAuditorContra(m.fn); // Evalúa los 3 caminos con oráculos fuertes
  } catch (_e) { // Al fallar una aserción estricta, el mutante es detectado y eliminado
    auditorDetecto = true; // Marca que el Auditor QA atrapó el bug
    mutantesMatadosPorAuditor++; // Incrementa el contador del Auditor QA
  } // Fin try/catch del Auditor QA

  console.log(`[${m.id}] ${m.nombre}`); // Muestra el nombre del mutante en la terminal
  console.log(`  - Descripción del bug : ${m.descripcion}`); // Muestra qué hace el bug
  console.log(`  - Test Zombi (Falso 100%) : ${zombiDetecto ? 'MATÓ AL MUTANTE' : 'SOBREVIVIÓ (Falso Positivo - No detectó el bug)'}`); // Resultado zombi
  console.log(`  - Test Auditor QA (McCabe): ${auditorDetecto ? 'MATÓ AL MUTANTE (Bug detectado con éxito)' : 'SOBREVIVIÓ'}\n`); // Resultado auditor
} // Fin del bucle de mutantes

console.log('--------------------------------------------------------------------------------'); // Separador final
console.log(` Mutation Score (Test Zombi Ingenuo) : ${mutantesMatadosPorZombi}/${mutantes.length} (0% efectividad real)`); // Imprime 0/3 (0%)
console.log(` Mutation Score (Suite Auditor QA)   : ${mutantesMatadosPorAuditor}/${mutantes.length} (100% efectividad real)`); // Imprime 3/3 (100%)
console.log('================================================================================'); // Cierre del reporte
