/**
 * ============================================================================
 * ARCHIVO: src/codigoEspaguetiIA.js
 * QUÉ HACE ESTE MÓDULO (DIAPOSITIVA 11 - PASO 5: REFACTORIZACIÓN GUIADA):
 * Demuestra cómo el primer código entregado por una IA sin supervisión suele
 * meter todas las reglas en una sola función gigante con Complejidad Ciclomática
 * V(G) = 15 (superando el límite máximo de 10 recomendado por McCabe).
 * Este ejemplo sirve en tu exposición para justificar por qué el Ingeniero QA
 * ordenó refactorizar el código en las funciones pequeñas de `src/reglasCitas.js`.
 * ============================================================================
 */

function agendarCitaMonoliticaSinRefactorizar(datos, citasExistentes) { // Declara la función monolítica sin modularizar
  if (!datos || !datos.paciente || !datos.medicoId) { // Predicados P1, P2 y P3: Valida existencia de objeto, paciente y médico en una sola línea
    return { exito: false, error: 'Datos incompletos' }; // Retorna fallo si falta alguno de los datos básicos
  } // Cierra validación de datos básicos
  if (datos.edad < 0 || datos.edad > 120) { // Predicados P4 y P5: Valida límites inferior y superior de edad
    return { exito: false, error: 'Edad inválida' }; // Retorna fallo por edad fuera de rango
  } // Cierra validación de rango de edad
  if (datos.edad < 18 && !datos.tieneAcompaniante) { // Predicados P6 y P7: Evalúa minoría de edad y ausencia de tutor juntos
    return { exito: false, error: 'Menor sin acompañante' }; // Retorna rechazo pediátrico
  } // Cierra validación pediátrica
  if (datos.diaSemana === 0 || datos.diaSemana === 6) { // Predicados P8 y P9: Evalúa si el día es domingo o sábado
    return { exito: false, error: 'Cerrado en fin de semana' }; // Retorna rechazo por fin de semana
  } // Cierra validación de fin de semana
  if (datos.hora < 8 || datos.hora >= 18 || datos.hora === 13) { // Predicados P10, P11 y P12: Mezcla horario laboral y hora de almuerzo
    return { exito: false, error: 'Horario no operativo' }; // Retorna rechazo por horario inválido
  } // Cierra validación de horas
  for (const c of citasExistentes) { // Predicado P13: Inicia bucle sobre todas las citas guardadas
    if (c.estado !== 'CANCELADA' && c.medicoId === datos.medicoId && datos.inicioMin < c.finMin && datos.finMin > c.inicioMin) { // Predicados P14+: Condición múltiple anidada que dispara V(G) a 15+
      return { exito: false, error: 'Choque de citas' }; // Retorna error si detecta solapamiento
    } // Cierra el if de choque de citas
  } // Cierra el bucle for
  return { exito: true }; // Retorna éxito si sobrevivió a todas las validaciones acopladas
} // Fin de la función monolítica (RECHAZADA por Auditoría QA por tener V(G) > 10)

module.exports = { agendarCitaMonoliticaSinRefactorizar }; // Exporta la función para análisis comparativo
