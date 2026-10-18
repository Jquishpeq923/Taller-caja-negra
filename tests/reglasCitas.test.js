/**
 * ============================================================================
 * ARCHIVO: tests/reglasCitas.test.js
 * QUÉ HACE ESTE MÓDULO (TALLER PASO 3 - DIAPOSITIVA 10: SUITE DEL AUDITOR QA):
 * Implementa las 19 pruebas unitarias matemáticas diseñadas con la Complejidad
 * Ciclomática de McCabe V(G) y Cobertura de Ramas (Branch) / Condiciones (MC/DC).
 * Cada prueba incluye Oráculos Estrictos (Modelo PIE) que validan:
 *   1) El booleano exacto de decisión.
 *   2) Los valores numéricos exactos (duración en minutos o multa de $25 vs $0).
 *   3) El código único de la regla de negocio.
 *   4) La invocación real del callback espía (`spyPenalizacion`).
 * ============================================================================
 */

const assert = require('node:assert/strict'); // Importa el módulo de aserciones estrictas nativo de Node.js
const { // Importa las 5 funciones del módulo auditado src/reglasCitas.js
  validarReglaEdad, // Función de la Regla 1 (Edad pediátrica vs. adulta)
  validarHorarioOperativo, // Función de la Regla 2 (Restricción de horarios y almuerzo)
  verificarChoqueCitas, // Función de la Regla 3 (Prevención de doble reserva)
  evaluarPenalizacionCancelacion, // Función de la Regla 4 (Penalización tardía < 24h - Diapositiva 8)
  procesarCancelacionPorHoras // Función integradora de cancelación por número de horas
} = require('../src/reglasCitas'); // Cierra la importación del módulo bajo prueba

// ============================================================================
// PARTE 1: ADAPTADOR DUAL (Permite correr con Jest o con 'node --test' nativo)
// ============================================================================
let describeFn = global.describe; // Toma 'describe' global si el archivo es ejecutado por Jest
let testFn = global.test; // Toma 'test' global si el archivo es ejecutado por Jest

if (typeof describeFn !== 'function') { // Si se ejecuta directamente con Node.js (sin Jest instalado)
  const nodeTest = require('node:test'); // Importa el motor nativo de pruebas de Node.js v18+
  describeFn = nodeTest.describe; // Usa 'describe' de node:test
  testFn = nodeTest.test; // Usa 'test' de node:test
} // Fin del adaptador dual

// Función auxiliar que crea un "Espía" (Mock/Spy) para contar cuántas veces se invoca aplicarPenalizacion()
function crearMockFn() { // Declara el creador de función espía para auditar efectos secundarios
  const llamadas = []; // Arreglo donde se guardará cada llamada realizada a la función
  const fn = (...args) => { // Define la función espía que recibe cualquier argumento
    llamadas.push(args); // Registra en el arreglo que la función fue invocada
    return 25; // Simula el retorno del cargo de $25
  }; // Cierra la definición de la función espía
  fn.llamadas = llamadas; // Expone el historial de llamadas como propiedad para inspeccionarlo en los tests
  return fn; // Devuelve el espía listo para usar como oráculo
} // Fin de crearMockFn

// ============================================================================
// PARTE 2: AUDITORÍA REGLA 4 (Diapositivas 8 a 10) — Penalización Tardía [V(G) = 3]
// Qué hace esta parte: Prueba exactamente los 3 caminos linealmente independientes
// calculados con V(G) = P + 1 = 2 + 1 = 3 sobre `if (cancelacionTardia && !esEmergencia)`.
// ============================================================================
describeFn('AUDITORÍA QA - REGLA 4 (Diapositivas 8-10): Penalización Tardía [V(G) = 3]', () => { // Abre suite de la Regla 4
  testFn('Camino 1 [cancelacionTardia = False]: No evalúa emergencia (cortocircuito) y NO aplica penalización', () => { // Prueba Camino 1
    const spyPenalizacion = crearMockFn(); // Crea un espía nuevo para verificar que NO se llame a aplicarPenalizacion()
    const res = evaluarPenalizacionCancelacion(false, false, spyPenalizacion); // Ejecuta Camino 1: cancela a tiempo (tardía=false)

    assert.strictEqual(res.penalizado, false); // Oráculo 1: Verifica estrictamente que penalizado sea false
    assert.strictEqual(res.montoPenalizacion, 0); // Oráculo 2: Verifica estrictamente que el monto cobrado sea $0
    assert.strictEqual(res.codigo, 'SIN_PENALIZACION'); // Oráculo 3: Verifica el código de estado exacto
    assert.strictEqual(spyPenalizacion.llamadas.length, 0); // Oráculo 4: Confirma que aplicarPenalizacion() se ejecutó 0 veces
  }); // Fin de la prueba del Camino 1

  testFn('Camino 2 [cancelacionTardia = True, esEmergencia = True (!esEmergencia = False)]: Exento por emergencia médica', () => { // Prueba Camino 2
    const spyPenalizacion = crearMockFn(); // Crea espía para verificar que la emergencia médica bloquea el cobro
    const res = evaluarPenalizacionCancelacion(true, true, spyPenalizacion); // Ejecuta Camino 2: tardía=true pero esEmergencia=true

    assert.strictEqual(res.penalizado, false); // Oráculo 1: Aunque sea <24h, NO debe ser penalizado por ser emergencia
    assert.strictEqual(res.montoPenalizacion, 0); // Oráculo 2: El cargo monetario debe ser exactamente $0
    assert.strictEqual(res.codigo, 'SIN_PENALIZACION'); // Oráculo 3: Código de exoneración verificado
    assert.strictEqual(spyPenalizacion.llamadas.length, 0); // Oráculo 4: Garantiza que el callback de cobro jamás fue invocado
  }); // Fin de la prueba del Camino 2

  testFn('Camino 3 [cancelacionTardia = True, esEmergencia = False (!esEmergencia = True)]: Aplica penalización de $25', () => { // Prueba Camino 3
    const spyPenalizacion = crearMockFn(); // Crea espía para comprobar que ahora SÍ se ejecuta el cobro
    const res = evaluarPenalizacionCancelacion(true, false, spyPenalizacion); // Ejecuta Camino 3: tardía=true y NO es emergencia

    assert.strictEqual(res.penalizado, true); // Oráculo 1: Confirma que penalizado es estrictamente true
    assert.strictEqual(res.montoPenalizacion, 25); // Oráculo 2: Confirma que la multa cobrada es exactamente $25
    assert.strictEqual(res.codigo, 'PENALIZACION_APLICADA'); // Oráculo 3: Verifica el código de penalización aplicada
    assert.strictEqual(spyPenalizacion.llamadas.length, 1); // Oráculo 4: Verifica que aplicarPenalizacion() se llamó exactamente 1 vez
  }); // Fin de la prueba del Camino 3

  testFn('Integración horasAnticipacion: lanza excepción si horas < 0 y evalúa umbral < 24h', () => { // Prueba la función auxiliar de horas
    const spy = crearMockFn(); // Crea espía para las pruebas de umbral horario
    assert.throws( // Oráculo de excepción: verifica que pasar horas negativas lance un Error
      () => procesarCancelacionPorHoras(-2, false, spy), // Intenta cancelar con -2 horas (cita ya pasada)
      /ya ha transcurrido/ // Exige que el mensaje de error coincida con la expresión regular
    ); // Cierra assert.throws

    const resTardia = procesarCancelacionPorHoras(12, false, spy); // Prueba con 12 horas (< 24h) y sin emergencia
    assert.strictEqual(resTardia.penalizado, true); // Verifica que con 12h se active la penalización
    assert.strictEqual(resTardia.montoPenalizacion, 25); // Verifica el cobro de $25
    assert.strictEqual(spy.llamadas.length, 1); // Verifica que el callback fue llamado 1 vez

    const resATiempo = procesarCancelacionPorHoras(36, false, spy); // Prueba con 36 horas (>= 24h)
    assert.strictEqual(resATiempo.penalizado, false); // Verifica que con 36h NO se penalice
    assert.strictEqual(resATiempo.montoPenalizacion, 0); // Verifica que el monto sea $0
  }); // Fin de prueba de integración de horas
}); // Fin de la suite de la Regla 4

// ============================================================================
// PARTE 3: AUDITORÍA REGLA 1 — Validación Pediátrica vs. Adulta [V(G) = 5 MC/DC]
// Qué hace esta parte: Cubre los 5 caminos independientes de `validarReglaEdad`.
// ============================================================================
describeFn('AUDITORÍA QA - REGLA 1: Validación de Edad Pediátrica vs. Adulta [V(G) = 5 MC/DC]', () => { // Abre suite Regla 1
  testFn('Camino 1 [edad < 0]: Rechaza edad negativa inválida', () => { // Prueba sub-condición P1 (edad < 0)
    const res = validarReglaEdad(-5, true); // Envía una edad negativa (-5 años)
    assert.strictEqual(res.valido, false); // Oráculo: debe rechazar la cita (valido = false)
    assert.strictEqual(res.categoria, 'EDAD_INVALIDA'); // Oráculo: categoría debe ser EDAD_INVALIDA
    assert.strictEqual(res.codigo, 'ERR_RANGO_EDAD'); // Oráculo: código debe ser ERR_RANGO_EDAD
  }); // Fin Camino 1 de Edad

  testFn('Camino 2 [edad > 120]: Rechaza edad superior al límite válido', () => { // Prueba sub-condición P2 (edad > 120)
    const res = validarReglaEdad(130, false); // Envía una edad de 130 años (> 120)
    assert.strictEqual(res.valido, false); // Oráculo: debe rechazar la cita
    assert.strictEqual(res.categoria, 'EDAD_INVALIDA'); // Oráculo: categoría EDAD_INVALIDA
    assert.strictEqual(res.codigo, 'ERR_RANGO_EDAD'); // Oráculo: código ERR_RANGO_EDAD
  }); // Fin Camino 2 de Edad

  testFn('Camino 3 [0 <= edad < 18, tieneAcompaniante = False]: Rechaza paciente pediátrico sin tutor', () => { // Prueba Camino 3
    const res = validarReglaEdad(10, false); // Niño de 10 años sin adulto acompañante (false)
    assert.strictEqual(res.valido, false); // Oráculo: debe bloquear la reserva
    assert.strictEqual(res.categoria, 'PEDIATRICO'); // Oráculo: identifica que es paciente PEDIATRICO
    assert.strictEqual(res.requiereAcompaniante, true); // Oráculo: indica que requiere acompañante obligatorio
    assert.strictEqual(res.codigo, 'ERR_MENOR_SIN_TUTOR'); // Oráculo: verifica el código exacto de rechazo
  }); // Fin Camino 3 de Edad

  testFn('Camino 4 [0 <= edad < 18, tieneAcompaniante = True]: Autoriza cita pediátrica (40 min)', () => { // Prueba Camino 4
    const res = validarReglaEdad(10, true); // Niño de 10 años CON adulto acompañante (true)
    assert.strictEqual(res.valido, true); // Oráculo: autoriza la cita
    assert.strictEqual(res.categoria, 'PEDIATRICO'); // Oráculo: categoría PEDIATRICO
    assert.strictEqual(res.duracionMinutos, 40); // Oráculo: verifica que asigne 40 minutos a consulta pediátrica
    assert.strictEqual(res.requiereAcompaniante, true); // Oráculo: confirma requisito de tutor
    assert.strictEqual(res.codigo, 'OK_PEDIATRICO'); // Oráculo: código de aprobación pediátrica
  }); // Fin Camino 4 de Edad

  testFn('Camino 5 [18 <= edad <= 120]: Autoriza cita de paciente adulto sin exigir tutor (30 min)', () => { // Prueba Camino 5
    const res = validarReglaEdad(35, false); // Paciente adulto de 35 años sin acompañante
    assert.strictEqual(res.valido, true); // Oráculo: autoriza la cita
    assert.strictEqual(res.categoria, 'ADULTO'); // Oráculo: categoría ADULTO
    assert.strictEqual(res.duracionMinutos, 30); // Oráculo: verifica que asigne 30 minutos a consulta adulta
    assert.strictEqual(res.requiereAcompaniante, false); // Oráculo: adulto no requiere tutor
    assert.strictEqual(res.codigo, 'OK_ADULTO'); // Oráculo: código de aprobación adulta
  }); // Fin Camino 5 de Edad
}); // Fin de la suite de la Regla 1

// ============================================================================
// PARTE 4: AUDITORÍA REGLA 2 — Restricción de Horarios Operativos [V(G) = 6 MC/DC]
// Qué hace esta parte: Cubre los 6 caminos independientes de `validarHorarioOperativo`.
// ============================================================================
describeFn('AUDITORÍA QA - REGLA 2: Restricción de Horarios Operativos [V(G) = 6 MC/DC]', () => { // Abre suite Regla 2
  testFn('Camino 1 [diaSemana === 0 (Domingo)]: Bloquea atención en domingo', () => { // Prueba Domingo (0)
    const res = validarHorarioOperativo(0, 10); // Domingo a las 10:00 AM
    assert.strictEqual(res.operativo, false); // Oráculo: consultorio cerrado
    assert.strictEqual(res.codigo, 'ERR_FIN_DE_SEMANA'); // Oráculo: código de rechazo por fin de semana
  }); // Fin Camino 1 de Horario

  testFn('Camino 2 [diaSemana === 6 (Sábado)]: Bloquea atención en sábado', () => { // Prueba Sábado (6)
    const res = validarHorarioOperativo(6, 10); // Sábado a las 10:00 AM
    assert.strictEqual(res.operativo, false); // Oráculo: consultorio cerrado
    assert.strictEqual(res.codigo, 'ERR_FIN_DE_SEMANA'); // Oráculo: código de rechazo por fin de semana
  }); // Fin Camino 2 de Horario

  testFn('Camino 3 [Día hábil, hora < 8]: Bloquea cita antes de las 08:00', () => { // Prueba madrugada (< 8)
    const res = validarHorarioOperativo(2, 7); // Martes (2) a las 07:00 AM
    assert.strictEqual(res.operativo, false); // Oráculo: fuera de horario operativo
    assert.strictEqual(res.codigo, 'ERR_FUERA_DE_HORARIO'); // Oráculo: código ERR_FUERA_DE_HORARIO
  }); // Fin Camino 3 de Horario

  testFn('Camino 4 [Día hábil, hora >= 18]: Bloquea cita a partir de las 18:00', () => { // Prueba cierre (>= 18)
    const res = validarHorarioOperativo(4, 18); // Jueves (4) a las 18:00 PM
    assert.strictEqual(res.operativo, false); // Oráculo: fuera de horario operativo
    assert.strictEqual(res.codigo, 'ERR_FUERA_DE_HORARIO'); // Oráculo: código ERR_FUERA_DE_HORARIO
  }); // Fin Camino 4 de Horario

  testFn('Camino 5 [Día hábil, hora === 13]: Bloquea franja de almuerzo médico (13:00 - 14:00)', () => { // Prueba Almuerzo (13)
    const res = validarHorarioOperativo(3, 13); // Miércoles (3) a las 13:00 PM
    assert.strictEqual(res.operativo, false); // Oráculo: bloqueado por hora de almuerzo
    assert.strictEqual(res.codigo, 'ERR_HORA_ALMUERZO'); // Oráculo: código ERR_HORA_ALMUERZO
  }); // Fin Camino 5 de Horario

  testFn('Camino 6 [Día hábil, hora operativa válida]: Autoriza reserva en horario hábil', () => { // Prueba horario válido
    const res = validarHorarioOperativo(1, 10); // Lunes (1) a las 10:00 AM
    assert.strictEqual(res.operativo, true); // Oráculo: franja operativa permitida
    assert.strictEqual(res.codigo, 'OK_HORARIO_HABIL'); // Oráculo: código OK_HORARIO_HABIL
  }); // Fin Camino 6 de Horario
}); // Fin de la suite de la Regla 2

// ============================================================================
// PARTE 5: AUDITORÍA REGLA 3 — Prevención de Choque de Citas [V(G) = 4]
// Qué hace esta parte: Cubre los 4 caminos estructurales de `verificarChoqueCitas`.
// ============================================================================
describeFn('AUDITORÍA QA - REGLA 3: Prevención de Choque de Citas [V(G) = 4]', () => { // Abre suite Regla 3
  const citasBase = [ // Define arreglo de citas de prueba para evaluar colisiones
    { // Cita 101: Confirmada de 10:00 (600) a 10:30 (630) con DR_GOMEZ
      id: 101, // ID de la cita activa
      paciente: 'Carlos Ruiz', // Nombre del paciente
      medicoId: 'DR_GOMEZ', // Médico asignado
      fecha: '2026-10-20', // Fecha de la cita
      inicioMin: 600, // 10:00 AM en minutos
      finMin: 630, // 10:30 AM en minutos
      estado: 'CONFIRMADA' // Cita activa que sí bloquea horario
    }, // Fin de Cita 101
    { // Cita 102: Cancelada de 11:00 (660) a 11:30 (690) con DR_GOMEZ
      id: 102, // ID de la cita cancelada
      paciente: 'Ana Mora', // Nombre de la paciente
      medicoId: 'DR_GOMEZ', // Médico asignado
      fecha: '2026-10-20', // Fecha de la cita
      inicioMin: 660, // 11:00 AM en minutos
      finMin: 690, // 11:30 AM en minutos
      estado: 'CANCELADA' // Estado CANCELADA (no debe generar choque)
    } // Fin de Cita 102
  ]; // Cierra arreglo citasBase

  testFn('Camino 1: Lista vacía de citas existentes -> Sin choque', () => { // Prueba cuando no hay citas previas
    const res = verificarChoqueCitas( // Llama a la función con un arreglo vacío []
      { medicoId: 'DR_GOMEZ', fecha: '2026-10-20', inicioMin: 600, finMin: 630 }, // Nueva cita a las 10:00
      [] // Arreglo vacío de citas existentes
    ); // Cierra llamada
    assert.strictEqual(res.hayChoque, false); // Oráculo: no hay choque
    assert.strictEqual(res.citaConflictoId, null); // Oráculo: sin ID en conflicto
    assert.strictEqual(res.codigo, 'OK_DISPONIBLE'); // Oráculo: código OK_DISPONIBLE
  }); // Fin Camino 1 de Choque

  testFn('Camino 2: Cita en el mismo horario pero estaba CANCELADA o es de otro médico -> Sin choque', () => { // Prueba filtro P2
    const resCancelada = verificarChoqueCitas( // Intenta agendar a las 11:00 donde había una cita CANCELADA
      { medicoId: 'DR_GOMEZ', fecha: '2026-10-20', inicioMin: 660, finMin: 690 }, // Misma hora que la cita #102 cancelada
      citasBase // Pasa la lista de citas de prueba
    ); // Cierra llamada
    assert.strictEqual(resCancelada.hayChoque, false); // Oráculo: permite reservar porque la cita previa estaba cancelada

    const resOtroMedico = verificarChoqueCitas( // Intenta agendar a las 10:00 pero con otra doctora (DRA_LOPEZ)
      { medicoId: 'DRA_LOPEZ', fecha: '2026-10-20', inicioMin: 600, finMin: 630 }, // Misma hora que #101 pero distinto médico
      citasBase // Pasa la lista de citas de prueba
    ); // Cierra llamada
    assert.strictEqual(resOtroMedico.hayChoque, false); // Oráculo: permite reservar porque es otro consultorio/médico
  }); // Fin Camino 2 de Choque

  testFn('Camino 3: Mismo médico y fecha pero en franja contigua sin solapamiento (10:30 a 11:00) -> Sin choque', () => { // Prueba P3 False
    const resDespues = verificarChoqueCitas( // Cita que inicia exactamente a las 10:30 (cuando termina la #101)
      { medicoId: 'DR_GOMEZ', fecha: '2026-10-20', inicioMin: 630, finMin: 660 }, // Intervalo [630, 660)
      citasBase // Lista de citas base
    ); // Cierra llamada
    assert.strictEqual(resDespues.hayChoque, false); // Oráculo: no se solapa al iniciar justo a las 10:30

    const resAntes = verificarChoqueCitas( // Cita que termina exactamente a las 10:00 (cuando empieza la #101)
      { medicoId: 'DR_GOMEZ', fecha: '2026-10-20', inicioMin: 570, finMin: 600 }, // Intervalo [570, 600) = 09:30 a 10:00
      citasBase // Lista de citas base
    ); // Cierra llamada
    assert.strictEqual(resAntes.hayChoque, false); // Oráculo: no se solapa al terminar justo a las 10:00
  }); // Fin Camino 3 de Choque

  testFn('Camino 4: Solapamiento parcial o total con cita activa del mismo médico -> Detecta choque y devuelve ID', () => { // Prueba P3 True
    const res = verificarChoqueCitas( // Intenta agendar a las 10:15 (615) invadiendo la cita #101 [600, 630)
      { medicoId: 'DR_GOMEZ', fecha: '2026-10-20', inicioMin: 615, finMin: 645 }, // Intervalo [615, 645) = 10:15 a 10:45
      citasBase // Lista de citas base
    ); // Cierra llamada
    assert.strictEqual(res.hayChoque, true); // Oráculo: detecta el choque de horario (true)
    assert.strictEqual(res.citaConflictoId, 101); // Oráculo: identifica que chocó específicamente con la cita #101
    assert.strictEqual(res.codigo, 'ERR_CHOQUE_CITA'); // Oráculo: devuelve el código ERR_CHOQUE_CITA
  }); // Fin Camino 4 de Choque
}); // Fin de la suite de la Regla 3
