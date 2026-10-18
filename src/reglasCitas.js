/**
 * ============================================================================
 * ARCHIVO: src/reglasCitas.js
 * QUÉ HACE ESTE MÓDULO:
 * Contiene las 4 reglas críticas de negocio del Sistema de Citas de Médico
 * Familiar (Diapositiva 3 del PDF), refactorizadas para que cada función tenga
 * una Complejidad Ciclomática de McCabe V(G) baja (entre 3 y 6, muy por debajo
 * del límite máximo permitido de 10) y comentadas línea por línea.
 * ============================================================================
 */

// ============================================================================
// PARTE 1: REGLA DE EDAD (Validación Pediátrica vs. Adulta) — V(G) = 5 (MC/DC)
// Qué hace esta parte: Recibe la edad del paciente y un booleano indicando si
// asiste con un adulto acompañante. Bloquea edades fuera de [0, 120], exige
// acompañante si es menor de 18 años (consulta pediátrica de 40 min) y aprueba
// consulta adulta (30 min) si tiene 18 años o más.
// ============================================================================
function validarReglaEdad(edad, tieneAcompaniante) { // Declara la función que recibe la edad numérica y si lleva acompañante (true/false)
  if (edad < 0 || edad > 120) { // Nodos Predicado P1 y P2: Evalúa si la edad es negativa (< 0) o supera el límite biológico (> 120)
    return { // Camino 1 y 2: Si la edad es inválida, retorna inmediatamente el objeto de rechazo
      valido: false, // Oráculo booleano: indica que la cita NO es válida
      categoria: 'EDAD_INVALIDA', // Clasificación asignada cuando el dato de edad es irreal
      duracionMinutos: 0, // Asigna 0 minutos porque no se agendará ninguna consulta
      requiereAcompaniante: false, // No aplica tutor porque la edad en sí misma es un error de entrada
      codigo: 'ERR_RANGO_EDAD', // Código único de error para auditar con aserciones estrictas en los tests
      mensaje: 'La edad debe estar comprendida entre 0 y 120 años.' // Mensaje explicativo para mostrar en la interfaz web
    }; // Cierra el objeto de retorno de los Caminos 1 y 2
  } // Fin del primer bloque condicional if (rango de edad)

  if (edad < 18) { // Nodo Predicado P3: Evalúa si el paciente es menor de edad (categoría pediátrica)
    if (!tieneAcompaniante) { // Nodo Predicado P4: Si es menor de 18, evalúa si NO viene con un adulto responsable
      return { // Camino 3: Es niño/adolescente (< 18) pero NO tiene acompañante -> se rechaza la reserva
        valido: false, // Oráculo booleano: reserva bloqueada por política de protección pediátrica
        categoria: 'PEDIATRICO', // Se identifica como paciente pediátrico
        duracionMinutos: 0, // Asigna 0 minutos porque la cita fue rechazada
        requiereAcompaniante: true, // Indica que obligatoriamente necesita un adulto tutor
        codigo: 'ERR_MENOR_SIN_TUTOR', // Código exacto que verifica el test unitario del Camino 3
        mensaje: 'Paciente pediátrico (< 18 años) requiere acudir con un adulto responsable.' // Explicación del rechazo
      }; // Cierra el objeto de retorno del Camino 3
    } // Fin del condicional interno !tieneAcompaniante

    return { // Camino 4: Es menor de 18 años Y SÍ tiene acompañante adulto -> se autoriza la cita pediátrica
      valido: true, // Oráculo booleano: reserva aprobada exitosamente
      categoria: 'PEDIATRICO', // Clasifica la consulta como atención pediátrica
      duracionMinutos: 40, // Asigna 40 minutos de duración (las citas pediátricas requieren más tiempo)
      requiereAcompaniante: true, // Deja constancia de que asiste con tutor responsable
      codigo: 'OK_PEDIATRICO', // Código exacto que verifica el test unitario del Camino 4
      mensaje: 'Cita pediátrica validada con acompañante adulto (duración: 40 min).' // Mensaje de confirmación
    }; // Cierra el objeto de retorno del Camino 4
  } // Fin del bloque condicional if (edad < 18)

  return { // Camino 5: Tiene entre 18 y 120 años (Paciente Adulto) -> se autoriza sin exigir acompañante
    valido: true, // Oráculo booleano: reserva aprobada para adulto
    categoria: 'ADULTO', // Clasifica al paciente como medicina familiar adulta
    duracionMinutos: 30, // Asigna la duración estándar de 30 minutos para consulta de adulto
    requiereAcompaniante: false, // El paciente adulto puede asistir por cuenta propia
    codigo: 'OK_ADULTO', // Código exacto que verifica el test unitario del Camino 5
    mensaje: 'Cita de medicina familiar para adulto validada (duración: 30 min).' // Mensaje de éxito
  }; // Cierra el objeto de retorno del Camino 5
} // Fin de la función validarReglaEdad

// ============================================================================
// PARTE 2: RESTRICCIÓN DE HORARIOS OPERATIVOS — V(G) = 6 (MC/DC)
// Qué hace esta parte: Bloquea citas en fin de semana (Domingo=0, Sábado=6),
// bloquea horas antes de las 08:00 o desde las 18:00 en adelante, y bloquea
// la franja de las 13:00 a 14:00 destinada al almuerzo del médico.
// ============================================================================
function validarHorarioOperativo(diaSemana, hora) { // Recibe el día de la semana (0=Dom..6=Sáb) y la hora entera (0..23)
  if (diaSemana === 0 || diaSemana === 6) { // Nodos Predicado P1 (Domingo=0) y P2 (Sábado=6): verifica si es fin de semana
    return { // Caminos 1 y 2: Si es domingo o sábado, rechaza por consultorio cerrado
      operativo: false, // Oráculo booleano: indica que el horario NO es operativo
      codigo: 'ERR_FIN_DE_SEMANA', // Identificador de rechazo por día inhábil
      mensaje: 'El consultorio permanece cerrado los sábados y domingos.' // Mensaje al usuario
    }; // Cierra el objeto de retorno de fin de semana
  } // Fin del bloque condicional de días de fin de semana

  if (hora < 8 || hora >= 18) { // Nodos Predicado P3 (antes de las 8am) y P4 (18pm o más tarde): verifica jornada laboral
    return { // Caminos 3 y 4: Si la hora está fuera de [08:00, 18:00), bloquea la cita
      operativo: false, // Oráculo booleano: franja fuera de horario de atención
      codigo: 'ERR_FUERA_DE_HORARIO', // Identificador de rechazo por hora nocturna o madrugada
      mensaje: 'Franja no operativa. El horario de atención es de 08:00 a 18:00.' // Mensaje explicativo
    }; // Cierra el objeto de retorno de fuera de horario
  } // Fin del bloque condicional de límites de jornada

  if (hora === 13) { // Nodo Predicado P5: Evalúa si la cita cae exactamente a las 13:00 (1:00 PM - hora de almuerzo)
    return { // Camino 5: Bloquea la franja de 13:00 a 14:00 para proteger el descanso médico
      operativo: false, // Oráculo booleano: franja bloqueada por almuerzo
      codigo: 'ERR_HORA_ALMUERZO', // Identificador exacto auditado por el test del Camino 5
      mensaje: 'Franja de 13:00 a 14:00 bloqueada por receso de almuerzo médico.' // Mensaje explicativo
    }; // Cierra el objeto de retorno de hora de almuerzo
  } // Fin del condicional de hora de almuerzo

  return { // Camino 6: Es lunes a viernes, entre 08:00 y 17:59, y no es las 13:00 -> Horario válido
    operativo: true, // Oráculo booleano: horario hábil autorizado
    codigo: 'OK_HORARIO_HABIL', // Identificador exacto de horario válido
    mensaje: 'Franja horaria operativa disponible.' // Confirmación de disponibilidad horaria
  }; // Cierra el objeto de retorno del Camino 6
} // Fin de la función validarHorarioOperativo

// ============================================================================
// PARTE 3: PREVENCIÓN DE CHOQUE DE CITAS (Doble Reserva) — V(G) = 4
// Qué hace esta parte: Recorre la lista de citas existentes y verifica si
// alguna cita ACTIVA (no cancelada) del mismo médico y en la misma fecha se
// cruza en minutos (inicioMin < cita.finMin && finMin > cita.inicioMin).
// ============================================================================
function verificarChoqueCitas(nuevaCita, citasExistentes) { // Recibe los datos de la nueva cita y el arreglo de citas en BD
  for (const cita of citasExistentes) { // Nodo Predicado P1 (Bucle): Itera sobre cada cita registrada en la base de datos
    if ( // Nodo Predicado P2: Verifica si la cita existente compete al mismo médico, fecha y sigue activa
      cita.estado !== 'CANCELADA' && // Condición 2a: Ignora citas que ya fueron canceladas previamente
      cita.medicoId === nuevaCita.medicoId && // Condición 2b: Solo compara citas asignadas al mismo doctor
      cita.fecha === nuevaCita.fecha // Condición 2c: Solo compara citas programadas en el mismo día calendario
    ) { // Abre el bloque si la cita existente es del mismo médico, mismo día y está confirmada
      if (nuevaCita.inicioMin < cita.finMin && nuevaCita.finMin > cita.inicioMin) { // Nodo Predicado P3: Fórmula matemática de solapamiento de intervalos
        return { // Camino 4: Existe cruce de horarios -> bloquea la doble reserva y reporta el ID en conflicto
          hayChoque: true, // Oráculo booleano: confirma que SÍ hay colisión de horario
          citaConflictoId: cita.id, // Devuelve el ID exacto de la cita con la que chocó (oráculo fuerte)
          codigo: 'ERR_CHOQUE_CITA', // Código de error para el test unitario de colisión
          mensaje: `Choque de horario detectado con la cita #${cita.id} (${cita.paciente}).` // Detalle del choque
        }; // Cierra el retorno de choque detectado
      } // Fin del condicional de intersección de minutos
    } // Fin del condicional de coincidencia de médico, fecha y estado activo
  } // Fin del bucle for que revisa todas las citas existentes

  return { // Caminos 1, 2 y 3: Si termina el bucle sin encontrar solapamientos, autoriza el horario
    hayChoque: false, // Oráculo booleano: garantiza que NO existe doble reserva
    citaConflictoId: null, // No hay ningún ID de cita en conflicto
    codigo: 'OK_DISPONIBLE', // Código de éxito verificado en las pruebas unitarias
    mensaje: 'No existen choques de horario para el médico y fecha seleccionados.' // Mensaje de disponibilidad
  }; // Cierra el objeto de retorno sin choque
} // Fin de la función verificarChoqueCitas

// ============================================================================
// PARTE 4: PENALIZACIÓN TARDÍA (< 24h) — V(G) = 3 (Caso Estrella Diapositiva 8)
// Qué hace esta parte: Implementa exactamente el fragmento de código auditado
// en las Diapositivas 8, 9 y 10 del PDF:
//   if (cancelacionTardia && !esEmergencia) { aplicarPenalizacion(); }
// - Nodos Predicado (P): 2 (P1: cancelacionTardia, P2: !esEmergencia)
// - Complejidad de McCabe: V(G) = P + 1 = 2 + 1 = 3 caminos independientes:
//   * Camino 1: cancelacionTardia = false -> No cobra multa (cortocircuito).
//   * Camino 2: cancelacionTardia = true, esEmergencia = true -> Exento por urgencia.
//   * Camino 3: cancelacionTardia = true, esEmergencia = false -> Cobra $25.00 USD.
// ============================================================================
function evaluarPenalizacionCancelacion(cancelacionTardia, esEmergencia, aplicarPenalizacion) { // Recibe los 2 booleanos y el callback de cobro
  if (cancelacionTardia && !esEmergencia) { // Nodos Predicado P1 (cancelacionTardia) y P2 (!esEmergencia) unidos por &&
    aplicarPenalizacion(); // Camino 3 (Nodo de Acción N4): Ejecuta el efecto secundario que registra la penalización
    return { // Retorna el resultado del Camino 3 con los valores exactos para el oráculo de prueba
      penalizado: true, // Oráculo booleano: confirma que el paciente FUE penalizado
      montoPenalizacion: 25, // Oráculo numérico: cargo fijo de $25.00 USD por cancelación tardía injustificada
      codigo: 'PENALIZACION_APLICADA', // Código de auditoría para el Camino 3
      mensaje: 'Cancelación con menos de 24h sin emergencia justificada: cargo de $25.00 aplicado.' // Explicación
    }; // Cierra el objeto de retorno del Camino 3
  } // Fin del bloque condicional if (cancelacionTardia && !esEmergencia)

  return { // Caminos 1 y 2 (Nodo de Salida N5): Retorna exoneración de multa ($0) cuando no aplica castigo
    penalizado: false, // Oráculo booleano: confirma que NO se aplicó penalización
    montoPenalizacion: 0, // Oráculo numérico: monto a cobrar es exactamente $0
    codigo: 'SIN_PENALIZACION', // Código de auditoría para los Caminos 1 y 2
    mensaje: 'Cancelación procesada sin cargos de penalización.' // Mensaje de confirmación sin multa
  }; // Cierra el objeto de retorno sin penalización
} // Fin de la función evaluarPenalizacionCancelacion

// ============================================================================
// PARTE 5: CONVERSOR DE HORAS DE ANTICIPACIÓN (< 24h)
// Qué hace esta parte: Valida que las horas de anticipación no sean negativas
// (cita en el pasado), calcula el booleano `cancelacionTardia = horas < 24`
// y delega el dictamen a `evaluarPenalizacionCancelacion`.
// ============================================================================
function procesarCancelacionPorHoras(horasAnticipacion, esEmergencia, aplicarPenalizacion) { // Recibe las horas numéricas antes de la cita
  if (horasAnticipacion < 0) { // Nodo Predicado P1: Verifica si el número de horas es negativo (cita ya vencida)
    throw new Error('No se puede cancelar una cita cuya fecha/hora ya ha transcurrido.'); // Lanza excepción controlada
  } // Fin de la validación de horas negativas

  const cancelacionTardia = horasAnticipacion < 24; // Calcula el booleano: es tardía (true) si faltan menos de 24 horas
  return evaluarPenalizacionCancelacion(cancelacionTardia, esEmergencia, aplicarPenalizacion); // Llama a la Regla 4 auditada y retorna su respuesta
} // Fin de la función procesarCancelacionPorHoras

module.exports = { // Exporta las 5 funciones para ser usadas por el servidor Express y por los tests de Jest/Node
  validarReglaEdad, // Exporta la Regla 1 (Edad pediátrica vs. adulta)
  validarHorarioOperativo, // Exporta la Regla 2 (Restricción de horarios y almuerzo)
  verificarChoqueCitas, // Exporta la Regla 3 (Prevención de choque de citas)
  evaluarPenalizacionCancelacion, // Exporta la Regla 4 (Penalización tardía < 24h - Diapositiva 8)
  procesarCancelacionPorHoras // Exporta el integrador por número de horas
}; // Cierra la exportación del módulo
