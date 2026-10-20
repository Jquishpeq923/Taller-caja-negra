package com.mediqa; // Define el paquete principal de la aplicación en Java

/**
 * ============================================================================
 * ARCHIVO: src/main/java/com/mediqa/ModelosCita.java
 * QUÉ HACE ESTE MÓDULO EN LA MIGRACIÓN A JAVA:
 * En JavaScript los datos e informes de las reglas eran objetos literales JSON
 * sin tipos fijos (`{ valido: true, ... }`). Al migrar a Java 17, los
 * convertimos en `record` inmutables y clases fuertemente tipadas para que el
 * compilador garantice que ningún tipo de dato sea nulo o incorrecto.
 * ============================================================================
 */
public class ModelosCita { // Clase contenedora de todos los modelos de datos y resultados de auditoría

    // ========================================================================
    // PARTE 1: RECORD DE RESULTADO PARA LA REGLA 1 (EDAD PEDIÁTRICA VS. ADULTA)
    // ========================================================================
    public record ResultadoEdad( // Declara un record inmutable para devolver el dictamen de la Regla 1
        boolean valido, // Oráculo booleano: true si la edad permite agendar la cita, false si se rechaza
        String categoria, // Clasificación del paciente: "PEDIATRICO", "ADULTO" o "EDAD_INVALIDA"
        int duracionMinutos, // Duración asignada a la consulta: 40 min (pediátrico), 30 min (adulto) o 0 (error)
        boolean requiereAcompaniante, // Indica si por ley clínica el paciente requiere adulto responsable
        String codigo, // Código único de auditoría ("ERR_RANGO_EDAD", "ERR_MENOR_SIN_TUTOR", "OK_PEDIATRICO", "OK_ADULTO")
        String mensaje // Mensaje descriptivo listo para mostrarse al usuario en la interfaz web
    ) {} // Cierra la definición del record ResultadoEdad

    // ========================================================================
    // PARTE 2: RECORD DE RESULTADO PARA LA REGLA 2 (HORARIO OPERATIVO Y ALMUERZO)
    // ========================================================================
    public record ResultadoHorario( // Declara un record inmutable para devolver el dictamen de la Regla 2
        boolean operativo, // Oráculo booleano: true si el consultorio está abierto en ese día y hora
        String codigo, // Código único ("ERR_FIN_DE_SEMANA", "ERR_FUERA_DE_HORARIO", "ERR_HORA_ALMUERZO", "OK_HORARIO_HABIL")
        String mensaje // Explicación del bloqueo o confirmación de disponibilidad horaria
    ) {} // Cierra la definición del record ResultadoHorario

    // ========================================================================
    // PARTE 3: RECORD DE RESULTADO PARA LA REGLA 3 (PREVENCIÓN DE CHOQUE DE CITAS)
    // ========================================================================
    public record ResultadoChoque( // Declara un record inmutable para devolver el dictamen de la Regla 3
        boolean hayChoque, // Oráculo booleano: true si existe solapamiento de horario con otra cita activa
        Integer citaConflictoId, // ID de la cita con la que chocó (o null si no existe conflicto)
        String codigo, // Código único de auditoría ("ERR_CHOQUE_CITA" o "OK_DISPONIBLE")
        String mensaje // Mensaje detallando con qué cita y paciente ocurrió el cruce de horario
    ) {} // Cierra la definición del record ResultadoChoque

    // ========================================================================
    // PARTE 4: RECORD DE RESULTADO PARA LA REGLA 4 (PENALIZACIÓN TARDÍA < 24H)
    // ========================================================================
    public record ResultadoCancelacion( // Declara un record inmutable para devolver el dictamen de la Regla 4 (Diapositiva 8)
        boolean penalizado, // Oráculo booleano: true si se cobró penalización, false si quedó exento
        int montoPenalizacion, // Oráculo numérico: 25 ($25.00 USD) cuando aplica multa, o 0 ($0.00 USD) cuando no aplica
        String codigo, // Código único de auditoría ("PENALIZACION_APLICADA" o "SIN_PENALIZACION")
        String mensaje // Explicación de por qué se aplicó o exoneró el cargo de cancelación
    ) {} // Cierra la definición del record ResultadoCancelacion

    // ========================================================================
    // PARTE 5: RECORD PARA DATOS DE UNA NUEVA SOLICITUD DE CITA
    // ========================================================================
    public record NuevaCita( // Representa los datos de intervalo horario al verificar choques de citas
        String medicoId, // Identificador del médico ("DR_GOMEZ" o "DRA_LOPEZ")
        String fecha, // Fecha de la cita en formato ISO "YYYY-MM-DD"
        int inicioMin, // Minuto del día en que inicia la cita (ej. 10:00 AM = 600)
        int finMin // Minuto del día en que termina la cita (ej. 10:30 AM = 630)
    ) {} // Cierra la definición del record NuevaCita

    // ========================================================================
    // PARTE 6: CLASE ENTIDAD CITA MÉDICA (Representa una fila de la tabla SQLite)
    // ========================================================================
    public static class CitaMedica { // Clase que modela una cita almacenada en la base de datos
        public int id; // Identificador único autoincremental de la cita (ej. 101, 102...)
        public String paciente; // Nombre completo del paciente
        public int edad; // Edad del paciente en años cumplidos
        public String categoria; // Categoría clínica ("PEDIATRICO" o "ADULTO")
        public boolean tieneAcompaniante; // Indica si asiste con un adulto tutor responsable
        public String medicoId; // Código identificador del médico familiar
        public String medicoNombre; // Nombre completo y especialidad del médico
        public String fecha; // Fecha programada de la consulta ("YYYY-MM-DD")
        public String hora; // Hora programada de inicio ("HH:MM")
        public int inicioMin; // Hora de inicio convertida a minutos desde las 00:00
        public int finMin; // Hora de finalización convertida a minutos desde las 00:00
        public int duracionMinutos; // Duración total de la cita en minutos (30 o 40)
        public String estado; // Estado actual de la reserva ("CONFIRMADA" o "CANCELADA")
        public boolean penalizado; // Indica si recibió penalización al cancelarse (< 24h sin emergencia)
        public int montoPenalizacion; // Monto de penalización aplicado en dólares ($0 o $25)

        public CitaMedica( // Constructor completo para instanciar una cita médica tipada
            int id, // Parámetro ID único
            String paciente, // Parámetro nombre del paciente
            int edad, // Parámetro edad en años
            String categoria, // Parámetro categoría clínica
            boolean tieneAcompaniante, // Parámetro booleano de tutor
            String medicoId, // Parámetro ID del médico
            String medicoNombre, // Parámetro nombre del médico
            String fecha, // Parámetro fecha YYYY-MM-DD
            String hora, // Parámetro hora HH:MM
            int inicioMin, // Parámetro minuto de inicio
            int finMin, // Parámetro minuto de fin
            int duracionMinutos, // Parámetro duración en minutos
            String estado, // Parámetro estado de la cita
            boolean penalizado, // Parámetro indicador de multa
            int montoPenalizacion // Parámetro valor de la multa
        ) { // Abre el cuerpo del constructor
            this.id = id; // Asigna el ID al atributo de la instancia
            this.paciente = paciente; // Asigna el nombre del paciente
            this.edad = edad; // Asigna la edad numérica
            this.categoria = categoria; // Asigna la categoría ("PEDIATRICO" o "ADULTO")
            this.tieneAcompaniante = tieneAcompaniante; // Asigna si cuenta con acompañante adulto
            this.medicoId = medicoId; // Asigna el código del médico
            this.medicoNombre = medicoNombre; // Asigna el nombre completo del médico
            this.fecha = fecha; // Asigna la fecha de la cita
            this.hora = hora; // Asigna la hora de inicio
            this.inicioMin = inicioMin; // Asigna el minuto inicial del intervalo
            this.finMin = finMin; // Asigna el minuto final del intervalo
            this.duracionMinutos = duracionMinutos; // Asigna la duración en minutos
            this.estado = estado; // Asigna el estado ("CONFIRMADA" o "CANCELADA")
            this.penalizado = penalizado; // Asigna el estado de penalización
            this.montoPenalizacion = montoPenalizacion; // Asigna el monto de penalización
        } // Cierra el constructor de CitaMedica

        public String toJson() { // Método que serializa la cita a formato JSON estándar para el frontend Alpine.js
            return String.format( // Construye la cadena JSON formateada con todos los campos de la cita
                "{\"id\":%d,\"paciente\":\"%s\",\"edad\":%d,\"categoria\":\"%s\",\"tieneAcompaniante\":%b," + // Primera mitad del JSON
                "\"medicoId\":\"%s\",\"medicoNombre\":\"%s\",\"fecha\":\"%s\",\"hora\":\"%s\"," + // Datos del médico y horario
                "\"inicioMin\":%d,\"finMin\":%d,\"duracionMinutos\":%d,\"estado\":\"%s\"," + // Intervalos y estado
                "\"penalizado\":%b,\"montoPenalizacion\":%d}", // Datos de auditoría de cancelación
                this.id, // Inyecta el id entero
                escaparJson(this.paciente), // Inyecta el nombre del paciente escapando comillas
                this.edad, // Inyecta la edad entera
                escaparJson(this.categoria), // Inyecta la categoría
                this.tieneAcompaniante, // Inyecta true o false para acompañante
                escaparJson(this.medicoId), // Inyecta el ID del médico
                escaparJson(this.medicoNombre), // Inyecta el nombre del médico
                escaparJson(this.fecha), // Inyecta la fecha
                escaparJson(this.hora), // Inyecta la hora
                this.inicioMin, // Inyecta el minuto de inicio
                this.finMin, // Inyecta el minuto de fin
                this.duracionMinutos, // Inyecta la duración en minutos
                escaparJson(this.estado), // Inyecta el estado actual
                this.penalizado, // Inyecta si fue penalizado
                this.montoPenalizacion // Inyecta el monto de la multa
            ); // Cierra String.format
        } // Fin del método toJson

        public static String escaparJson(String texto) { // Función utilitaria para limpiar caracteres especiales en JSON
            if (texto == null) { // Verifica si la cadena recibida es nula
                return ""; // Si es nula, devuelve cadena vacía para evitar NullPointerException
            } // Fin del if null
            return texto.replace("\\", "\\\\").replace("\"", "\\\""); // Escapa barras invertidas y comillas dobles
        } // Fin del método escaparJson
    } // Fin de la clase CitaMedica
} // Fin de la clase ModelosCita
