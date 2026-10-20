package com.mediqa; // Declara la pertenencia al paquete com.mediqa

import java.util.List; // Importa la interfaz List para recorrer citas
import com.mediqa.ModelosCita.CitaMedica; // Importa el modelo CitaMedica

/**
 * ============================================================================
 * ARCHIVO: src/main/java/com/mediqa/CodigoEspaguetiIA.java
 * QUÉ HACE ESTE MÓDULO EN LA MIGRACIÓN A JAVA (DIAPOSITIVA 11 - PASO 5):
 * Demuestra en Java cómo una función monolítica generada por IA sin supervisión
 * acumula 14 predicados (V(G) = 15 > 10), violando el umbral de calidad de
 * McCabe y obligando al Ingeniero QA a refactorizarla en `ReglasCitas.java`.
 * ============================================================================
 */
public class CodigoEspaguetiIA { // Clase de ejemplo pedagógico del código sin refactorizar

    public static String agendarCitaMonoliticaSinRefactorizar( // Método monolítico con V(G) = 15
        String paciente, // Nombre del paciente
        String medicoId, // ID del médico
        int edad, // Edad del paciente
        boolean tieneAcompaniante, // Si lleva adulto acompañante
        int diaSemana, // Día de la semana (0..6)
        int hora, // Hora del día (0..23)
        int inicioMin, // Minuto de inicio
        int finMin, // Minuto de fin
        List<CitaMedica> citasExistentes // Lista de citas guardadas
    ) { // Abre el método monolítico
        if (paciente == null || paciente.isEmpty() || medicoId == null) { // Predicados P1, P2 y P3: Validaciones básicas mezcladas
            return "ERROR_DATOS_INCOMPLETOS"; // Retorna error si falta nombre o médico
        } // Cierra validación de datos básicos
        if (edad < 0 || edad > 120) { // Predicados P4 y P5: Validación de rango de edad acoplada
            return "ERROR_EDAD_INVALIDA"; // Retorna error de edad fuera de límite
        } // Cierra validación de rango de edad
        if (edad < 18 && !tieneAcompaniante) { // Predicados P6 y P7: Validación pediátrica acoplada
            return "ERROR_MENOR_SIN_ACOMPANIANTE"; // Retorna rechazo pediátrico
        } // Cierra validación pediátrica
        if (diaSemana == 0 || diaSemana == 6) { // Predicados P8 y P9: Fin de semana acoplado en la misma función
            return "ERROR_CERRADO_FIN_DE_SEMANA"; // Retorna rechazo por sábado o domingo
        } // Cierra validación de fin de semana
        if (hora < 8 || hora >= 18 || hora == 13) { // Predicados P10, P11 y P12: Jornada y almuerzo mezclados
            return "ERROR_HORARIO_NO_OPERATIVO"; // Retorna rechazo horario
        } // Cierra validación de horario
        for (CitaMedica c : citasExistentes) { // Predicado P13: Bucle anidado dentro de la misma función gigante
            if (!"CANCELADA".equals(c.estado) && c.medicoId.equals(medicoId) && inicioMin < c.finMin && finMin > c.inicioMin) { // Predicados P14+: Eleva V(G) a 15+
                return "ERROR_CHOQUE_DE_CITAS"; // Retorna error por doble reserva
            } // Cierra condicional de choque
        } // Cierra el bucle for
        return "EXITO_CITA_AGENDADA"; // Retorna éxito si pasó los 14 predicados acoplados
    } // Fin del método monolítico (RECHAZADO por Auditoría QA por superar V(G) > 10)
} // Fin de la clase CodigoEspaguetiIA
