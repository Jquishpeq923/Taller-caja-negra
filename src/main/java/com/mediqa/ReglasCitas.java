package com.mediqa; // Declara que esta clase pertenece al paquete com.mediqa

import java.util.List; // Importa la interfaz List de Java para recibir la lista de citas existentes
import com.mediqa.ModelosCita.CitaMedica; // Importa la clase entidad CitaMedica
import com.mediqa.ModelosCita.NuevaCita; // Importa el record NuevaCita para evaluar solapamientos
import com.mediqa.ModelosCita.ResultadoCancelacion; // Importa el record de respuesta de la Regla 4
import com.mediqa.ModelosCita.ResultadoChoque; // Importa el record de respuesta de la Regla 3
import com.mediqa.ModelosCita.ResultadoEdad; // Importa el record de respuesta de la Regla 1
import com.mediqa.ModelosCita.ResultadoHorario; // Importa el record de respuesta de la Regla 2

/**
 * ============================================================================
 * ARCHIVO: src/main/java/com/mediqa/ReglasCitas.java
 * QUÉ HACE ESTE MÓDULO EN LA MIGRACIÓN A JAVA:
 * Traduce de JavaScript a Java 17 las 4 reglas críticas de negocio conservando
 * exactamente el mismo Grafo de Flujo de Control (CFG) y la misma Complejidad
 * Ciclomática de McCabe V(G). Esto demuestra que la métrica matemática V(G)
 * es invariante al lenguaje de programación porque mide la topología lógica.
 * ============================================================================
 */
public class ReglasCitas { // Clase que contiene los métodos estáticos puros de las 4 reglas críticas

    // ========================================================================
    // PARTE 1: REGLA DE EDAD (Validación Pediátrica vs. Adulta) — V(G) = 5 (MC/DC)
    // Qué hace esta parte: Evalúa el rango de edad [0, 120]. Si es menor de 18,
    // exige acompañante adulto y asigna 40 minutos. Si es >= 18, asigna 30 min.
    // Topología McCabe: P = 4 predicados booleanos -> V(G) = P + 1 = 5 caminos.
    // ========================================================================
    public static ResultadoEdad validarReglaEdad(int edad, boolean tieneAcompaniante) { // Recibe edad entera y booleano de tutor
        if (edad < 0 || edad > 120) { // Nodos Predicado P1 (edad < 0) y P2 (edad > 120): verifica límites biológicos
            return new ResultadoEdad( // Caminos 1 y 2: Retorna rechazo inmediato si la edad es negativa o > 120
                false, // valido = false (la cita no se puede agendar)
                "EDAD_INVALIDA", // Categoría asignada al error de rango
                0, // Duración 0 minutos porque se rechaza la reserva
                false, // No aplica tutor porque el dato de edad es inválido
                "ERR_RANGO_EDAD", // Código exacto verificado por el oráculo en los tests JUnit
                "La edad debe estar comprendida entre 0 y 120 años." // Mensaje explicativo para el usuario
            ); // Cierra la creación del record ResultadoEdad para Caminos 1 y 2
        } // Fin del primer condicional if (rango de edad)

        if (edad < 18) { // Nodo Predicado P3: Evalúa si el paciente es menor de edad (paciente pediátrico)
            if (!tieneAcompaniante) { // Nodo Predicado P4: Evalúa si el menor NO cuenta con adulto acompañante
                return new ResultadoEdad( // Camino 3: Menor de edad sin acompañante -> rechaza la cita
                    false, // valido = false (bloqueado por regla de protección pediátrica)
                    "PEDIATRICO", // Se clasifica dentro de la categoría pediátrica
                    0, // Duración 0 minutos al no autorizarse la cita
                    true, // requiereAcompaniante = true (indica que sí era obligatorio el tutor)
                    "ERR_MENOR_SIN_TUTOR", // Código exacto verificado por el oráculo del Camino 3
                    "Paciente pediátrico (< 18 años) requiere acudir con un adulto responsable." // Motivo del rechazo
                ); // Cierra el retorno del Camino 3
            } // Fin del condicional interno !tieneAcompaniante

            return new ResultadoEdad( // Camino 4: Menor de 18 años CON adulto acompañante -> aprueba cita pediátrica
                true, // valido = true (reserva pediátrica autorizada)
                "PEDIATRICO", // Categoría clínica pediátrica
                40, // Asigna 40 minutos de duración a la consulta pediátrica
                true, // Confirma que requiere y lleva acompañante adulto
                "OK_PEDIATRICO", // Código exacto verificado por el oráculo del Camino 4
                "Cita pediátrica validada con acompañante adulto (duración: 40 min)." // Mensaje de confirmación
            ); // Cierra el retorno del Camino 4
        } // Fin del condicional if (edad < 18)

        return new ResultadoEdad( // Camino 5: Edad entre 18 y 120 años -> aprueba cita de adulto sin exigir tutor
            true, // valido = true (reserva de adulto autorizada)
            "ADULTO", // Categoría clínica de medicina familiar para adulto
            30, // Asigna 30 minutos de duración estándar para adulto
            false, // requiereAcompaniante = false (el adulto asiste por cuenta propia)
            "OK_ADULTO", // Código exacto verificado por el oráculo del Camino 5
            "Cita de medicina familiar para adulto validada (duración: 30 min)." // Mensaje de éxito
        ); // Cierra el retorno del Camino 5
    } // Fin del método validarReglaEdad

    // ========================================================================
    // PARTE 2: RESTRICCIÓN DE HORARIOS OPERATIVOS — V(G) = 6 (MC/DC)
    // Qué hace esta parte: Bloquea fines de semana (Domingo=0, Sábado=6),
    // bloquea horas fuera de [08:00, 18:00) y bloquea las 13:00 (almuerzo).
    // Topología McCabe: P = 5 predicados booleanos -> V(G) = P + 1 = 6 caminos.
    // ========================================================================
    public static ResultadoHorario validarHorarioOperativo(int diaSemana, int hora) { // Recibe día (0=Dom..6=Sáb) y hora (0..23)
        if (diaSemana == 0 || diaSemana == 6) { // Nodos Predicado P1 (Domingo=0) y P2 (Sábado=6): verifica fin de semana
            return new ResultadoHorario( // Caminos 1 y 2: Rechaza reservas en sábado o domingo
                false, // operativo = false (consultorio cerrado)
                "ERR_FIN_DE_SEMANA", // Código exacto para auditar rechazo por fin de semana
                "El consultorio permanece cerrado los sábados y domingos." // Mensaje explicativo
            ); // Cierra retorno de fin de semana
        } // Fin del condicional de fin de semana

        if (hora < 8 || hora >= 18) { // Nodos Predicado P3 (hora < 8) y P4 (hora >= 18): verifica jornada de 08:00 a 18:00
            return new ResultadoHorario( // Caminos 3 y 4: Rechaza horas de madrugada o nocturnas
                false, // operativo = false (fuera de horario laboral)
                "ERR_FUERA_DE_HORARIO", // Código exacto auditado en los Caminos 3 y 4
                "Franja no operativa. El horario de atención es de 08:00 a 18:00." // Mensaje explicativo
            ); // Cierra retorno de fuera de horario
        } // Fin del condicional de límites de jornada

        if (hora == 13) { // Nodo Predicado P5: Evalúa si la hora solicitada cae en el almuerzo médico (13:00 a 14:00)
            return new ResultadoHorario( // Camino 5: Bloquea la franja de las 13:00 horas
                false, // operativo = false (receso de almuerzo)
                "ERR_HORA_ALMUERZO", // Código exacto auditado en el Camino 5
                "Franja de 13:00 a 14:00 bloqueada por receso de almuerzo médico." // Mensaje explicativo
            ); // Cierra retorno de hora de almuerzo
        } // Fin del condicional de hora de almuerzo

        return new ResultadoHorario( // Camino 6: Día hábil (Lun-Vie) en hora operativa válida (08..12 o 14..17)
            true, // operativo = true (franja disponible)
            "OK_HORARIO_HABIL", // Código exacto auditado en el Camino 6
            "Franja horaria operativa disponible." // Confirmación de horario hábil
        ); // Cierra retorno del Camino 6
    } // Fin del método validarHorarioOperativo

    // ========================================================================
    // PARTE 3: PREVENCIÓN DE CHOQUE DE CITAS (Doble Reserva) — V(G) = 4
    // Qué hace esta parte: Recorre la lista tipada `List<CitaMedica>` buscando
    // si otra cita CONFIRMADA del mismo médico y fecha se solapa en minutos.
    // ========================================================================
    public static ResultadoChoque verificarChoqueCitas(NuevaCita nuevaCita, List<CitaMedica> citasExistentes) { // Recibe nueva cita y lista actual
        for (CitaMedica cita : citasExistentes) { // Nodo Predicado P1: Bucle for-each que recorre cada cita registrada
            if ( // Nodo Predicado P2: Filtra solo citas activas que coincidan en médico y fecha
                !"CANCELADA".equals(cita.estado) && // Condición 2a: Ignora citas cuyo estado sea "CANCELADA"
                cita.medicoId.equals(nuevaCita.medicoId()) && // Condición 2b: Compara que sea el mismo médico
                cita.fecha.equals(nuevaCita.fecha()) // Condición 2c: Compara que sea exactamente la misma fecha
            ) { // Abre el bloque si es una cita activa del mismo médico en ese mismo día
                if (nuevaCita.inicioMin() < cita.finMin && nuevaCita.finMin() > cita.inicioMin) { // Nodo Predicado P3: Intersección de intervalos
                    return new ResultadoChoque( // Camino 4: Choque detectado -> bloquea doble reserva y reporta el ID
                        true, // hayChoque = true (confirma colisión de horario)
                        cita.id, // Devuelve el ID exacto de la cita existente con la que chocó
                        "ERR_CHOQUE_CITA", // Código exacto auditado por el test del Camino 4
                        String.format("Choque de horario detectado con la cita #%d (%s).", cita.id, cita.paciente) // Mensaje detallado
                    ); // Cierra el retorno de choque detectado
                } // Fin del condicional de intersección de minutos
            } // Fin del condicional de coincidencia de médico, fecha y estado activo
        } // Fin del bucle for-each

        return new ResultadoChoque( // Caminos 1, 2 y 3: Terminó el recorrido sin encontrar ningún solapamiento
            false, // hayChoque = false (horario libre)
            null, // Sin ID de cita en conflicto (null)
            "OK_DISPONIBLE", // Código exacto auditado cuando no hay choque
            "No existen choques de horario para el médico y fecha seleccionados." // Mensaje de éxito
        ); // Cierra el retorno sin choque
    } // Fin del método verificarChoqueCitas

    // ========================================================================
    // PARTE 4: PENALIZACIÓN TARDÍA (< 24h) — V(G) = 3 (Caso Estrella Diapositiva 8)
    // Qué hace esta parte: Implementa en Java el código exacto de la Diapositiva 8:
    //   if (cancelacionTardia && !esEmergencia) { aplicarPenalizacion.run(); }
    // En lugar de pasar una función suelta como en JS, en Java usamos la interfaz
    // funcional estándar `Runnable` para ejecutar `aplicarPenalizacion.run()`.
    // ========================================================================
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

    // ========================================================================
    // PARTE 5: CONVERSOR DE HORAS DE ANTICIPACIÓN (< 24h) EN JAVA
    // ========================================================================
    public static ResultadoCancelacion procesarCancelacionPorHoras( // Método integrador por número de horas
        int horasAnticipacion, // Horas enteras que faltan antes de la cita
        boolean esEmergencia, // Indica si existe justificación de urgencia médica
        Runnable aplicarPenalizacion // Acción de cobro a ejecutar si corresponde multa
    ) { // Abre el cuerpo del método
        if (horasAnticipacion < 0) { // Nodo Predicado P1: Verifica si las horas son negativas (cita ya ocurrida)
            throw new IllegalArgumentException("No se puede cancelar una cita cuya fecha/hora ya ha transcurrido."); // Lanza excepción estándar de Java
        } // Fin de la validación de horas negativas

        boolean cancelacionTardia = horasAnticipacion < 24; // Calcula el booleano: es tardía si faltan menos de 24 horas
        return evaluarPenalizacionCancelacion(cancelacionTardia, esEmergencia, aplicarPenalizacion); // Invoca la Regla 4 auditada
    } // Fin del método procesarCancelacionPorHoras
} // Fin de la clase ReglasCitas
