package com.mediqa; // Declara la pertenencia al paquete com.mediqa

import com.sun.net.httpserver.HttpExchange; // Importa HttpExchange para leer peticiones y enviar respuestas HTTP
import com.sun.net.httpserver.HttpServer; // Importa el servidor HTTP nativo incluido en el JDK de Java
import java.io.IOException; // Importa IOException para manejo de errores de red/archivos
import java.io.InputStream; // Importa InputStream para leer el cuerpo JSON entrante
import java.io.OutputStream; // Importa OutputStream para enviar la respuesta al navegador
import java.net.InetSocketAddress; // Importa InetSocketAddress para enlazar el servidor a 0.0.0.0:3000
import java.nio.charset.StandardCharsets; // Importa UTF-8 para codificación correcta de tildes y eñes
import java.nio.file.Files; // Importa Files para leer el archivo public/index.html
import java.nio.file.Path; // Importa Path para ubicar archivos en el sistema
import java.time.LocalDate; // Importa LocalDate de Java para calcular el día de la semana de una fecha
import java.util.List; // Importa List para manejar la colección de citas
import java.util.concurrent.atomic.AtomicBoolean; // Importa AtomicBoolean para verificar la ejecución del callback Runnable
import java.util.regex.Matcher; // Importa Matcher para extraer valores del JSON y parámetros de URL
import java.util.regex.Pattern; // Importa Pattern para expresiones regulares sin requerir librerías externas
import com.mediqa.ModelosCita.CitaMedica; // Importa la clase entidad CitaMedica
import com.mediqa.ModelosCita.NuevaCita; // Importa el record NuevaCita
import com.mediqa.ModelosCita.ResultadoCancelacion; // Importa el record ResultadoCancelacion
import com.mediqa.ModelosCita.ResultadoChoque; // Importa el record ResultadoChoque
import com.mediqa.ModelosCita.ResultadoEdad; // Importa el record ResultadoEdad
import com.mediqa.ModelosCita.ResultadoHorario; // Importa el record ResultadoHorario

/**
 * ============================================================================
 * ARCHIVO: src/main/java/com/mediqa/ServidorCitasJava.java
 * QUÉ HACE ESTE MÓDULO EN LA MIGRACIÓN A JAVA:
 * Reemplaza a `src/server.js` (Express). Levanta un servidor HTTP REST real
 * en el puerto 3000 (`0.0.0.0:3000`) usando la librería oficial del JDK
 * (`com.sun.net.httpserver.HttpServer`), por lo que compila y arranca al
 * instante en GitHub Codespaces o en cualquier PC sin depender de descargas
 * externas. Sirve el frontend `public/index.html` y los endpoints `/api/citas`.
 * ============================================================================
 */
public class ServidorCitasJava { // Clase principal del servidor web y API REST en Java

    private final BaseDeDatosCitas db; // Instancia de la base de datos de citas médicas

    public ServidorCitasJava(BaseDeDatosCitas db) { // Constructor que recibe la capa de base de datos
        this.db = db; // Asigna la base de datos al atributo de la clase
    } // Fin del constructor

    public record RespuestaHttp(int status, String json) {} // Record que encapsula el código de estado HTTP y el cuerpo JSON

    public RespuestaHttp procesarCreacionCita( // Orquesta las Reglas 1, 2 y 3 antes de guardar en la base de datos
        String paciente, int edad, boolean tieneAcompaniante, // Datos del paciente
        String medicoId, String medicoNombre, String fecha, String hora // Datos de médico, fecha y hora
    ) { // Abre el método procesarCreacionCita
        ResultadoEdad resEdad = ReglasCitas.validarReglaEdad(edad, tieneAcompaniante); // Invoca la Regla 1 en Java
        if (!resEdad.valido()) { // Si la Regla 1 rechaza la cita (edad fuera de rango o menor sin tutor)
            String jsonErrorEdad = String.format( // Construye respuesta JSON de rechazo HTTP 400
                "{\"exito\":false,\"reglaViolada\":\"REGLA_1_EDAD\",\"valido\":false,\"categoria\":\"%s\",\"codigo\":\"%s\",\"mensaje\":\"%s\"}", // Plantilla JSON
                resEdad.categoria(), resEdad.codigo(), CitaMedica.escaparJson(resEdad.mensaje()) // Valores del record ResultadoEdad
            ); // Cierra String.format
            return new RespuestaHttp(400, jsonErrorEdad); // Retorna estado HTTP 400 (Bad Request)
        } // Fin de validación de la Regla 1

        LocalDate fechaLocal = LocalDate.parse(fecha); // Parsea la cadena "YYYY-MM-DD" usando java.time.LocalDate
        int valorIsoDia = fechaLocal.getDayOfWeek().getValue(); // Obtiene día ISO (1=Lunes ... 7=Domingo)
        int diaSemana = (valorIsoDia == 7) ? 0 : valorIsoDia; // Convierte Domingo (7) a 0 para coincidir con el estándar (0=Dom..6=Sáb)

        String[] partesHora = hora.split(":"); // Divide la hora "HH:MM" por el carácter dos puntos
        int horaNum = Integer.parseInt(partesHora[0]); // Convierte la parte de las horas a entero (0..23)
        int minNum = (partesHora.length > 1) ? Integer.parseInt(partesHora[1]) : 0; // Convierte los minutos a entero (0..59)

        ResultadoHorario resHorario = ReglasCitas.validarHorarioOperativo(diaSemana, horaNum); // Invoca la Regla 2 en Java
        if (!resHorario.operativo()) { // Si es fin de semana, fuera de jornada o las 13:00 (almuerzo)
            String jsonErrorHorario = String.format( // Construye respuesta JSON de rechazo horario
                "{\"exito\":false,\"reglaViolada\":\"REGLA_2_HORARIO\",\"operativo\":false,\"codigo\":\"%s\",\"mensaje\":\"%s\"}", // Plantilla JSON
                resHorario.codigo(), CitaMedica.escaparJson(resHorario.mensaje()) // Valores del record ResultadoHorario
            ); // Cierra String.format
            return new RespuestaHttp(400, jsonErrorHorario); // Retorna estado HTTP 400 (Bad Request)
        } // Fin de validación de la Regla 2

        int inicioMin = horaNum * 60 + minNum; // Calcula el minuto del día en que inicia la cita
        int finMin = inicioMin + resEdad.duracionMinutos(); // Suma la duración (40 min niño o 30 min adulto)
        NuevaCita solicitud = new NuevaCita(medicoId, fecha, inicioMin, finMin); // Empaqueta el intervalo en un record NuevaCita
        ResultadoChoque resChoque = ReglasCitas.verificarChoqueCitas(solicitud, this.db.listarCitas()); // Invoca la Regla 3 en Java

        if (resChoque.hayChoque()) { // Si el médico ya tiene otra cita activa que se solapa en ese horario
            String jsonErrorChoque = String.format( // Construye respuesta JSON de conflicto HTTP 409
                "{\"exito\":false,\"reglaViolada\":\"REGLA_3_CHOQUE\",\"hayChoque\":true,\"citaConflictoId\":%d,\"codigo\":\"%s\",\"mensaje\":\"%s\"}", // Plantilla JSON
                resChoque.citaConflictoId(), resChoque.codigo(), CitaMedica.escaparJson(resChoque.mensaje()) // Valores del record ResultadoChoque
            ); // Cierra String.format
            return new RespuestaHttp(409, jsonErrorChoque); // Retorna estado HTTP 409 (Conflict)
        } // Fin de validación de la Regla 3

        String nombreDoctor = (medicoNombre != null && !medicoNombre.isEmpty()) // Verifica si enviaron el nombre del médico
            ? medicoNombre // Si fue enviado, lo conserva
            : ("DRA_LOPEZ".equals(medicoId) ? "Dra. Valeria López (Medicina Familiar & Pediatría)" : "Dr. Alejandro Gómez (Medicina Familiar)"); // Asigna por defecto

        CitaMedica citaCreada = this.db.insertarCita( // Inserta el registro en BaseDeDatosCitas
            paciente, edad, resEdad.categoria(), tieneAcompaniante, // Datos del paciente
            medicoId, nombreDoctor, fecha, hora, // Datos de agenda
            inicioMin, finMin, resEdad.duracionMinutos() // Intervalos calculados
        ); // Cierra inserción

        String jsonExito = String.format( // Construye respuesta JSON HTTP 201 con la cita creada
            "{\"exito\":true,\"mensaje\":\"Cita médica agendada exitosamente en el Backend Java.\",\"cita\":%s}", // Plantilla JSON
            citaCreada.toJson() // Serializa la entidad CitaMedica a JSON
        ); // Cierra String.format
        return new RespuestaHttp(201, jsonExito); // Retorna estado HTTP 201 (Created)
    } // Fin de procesarCreacionCita

    public RespuestaHttp procesarCancelacionCita(int idCita, int horasAnticipacion, boolean esEmergencia) { // Procesa cancelación
        CitaMedica cita = this.db.obtenerCitaPorId(idCita); // Busca la cita por su ID en la base de datos
        if (cita == null) { // Si el ID no existe
            return new RespuestaHttp(404, "{\"exito\":false,\"mensaje\":\"Cita no encontrada.\"}"); // Retorna HTTP 404
        } // Fin de verificación de existencia

        try { // Bloque try por si horasAnticipacion es negativo (< 0)
            AtomicBoolean penalizacionRegistrada = new AtomicBoolean(false); // Bandera atómica modificada por el callback Runnable
            ResultadoCancelacion resCancelacion = ReglasCitas.procesarCancelacionPorHoras( // Invoca la Regla 4 en Java
                horasAnticipacion, // Horas restantes antes de la cita
                esEmergencia, // Si es emergencia médica justificada
                () -> penalizacionRegistrada.set(true) // Lambda Runnable: se ejecuta solo en el Camino 3 de McCabe
            ); // Cierra invocación de procesarCancelacionPorHoras

            CitaMedica actualizada = this.db.actualizarCancelacion( // Actualiza la cita en la base de datos
                idCita, // ID de la cita cancelada
                penalizacionRegistrada.get() && resCancelacion.penalizado(), // Confirma que el callback se ejecutó
                resCancelacion.montoPenalizacion() // Monto de penalización ($25 o $0)
            ); // Cierra actualizarCancelacion

            String jsonRespuesta = String.format( // Construye respuesta JSON con el dictamen de la Regla 4
                "{\"exito\":true,\"penalizado\":%b,\"montoPenalizacion\":%d,\"codigo\":\"%s\",\"mensaje\":\"%s\",\"cita\":%s}", // Plantilla JSON
                resCancelacion.penalizado(), resCancelacion.montoPenalizacion(), resCancelacion.codigo(), // Datos de Regla 4
                CitaMedica.escaparJson(resCancelacion.mensaje()), actualizada.toJson() // Mensaje y cita actualizada
            ); // Cierra String.format
            return new RespuestaHttp(200, jsonRespuesta); // Retorna HTTP 200 (OK)
        } catch (IllegalArgumentException ex) { // Si horasAnticipacion fue negativo
            return new RespuestaHttp(400, "{\"exito\":false,\"mensaje\":\"" + CitaMedica.escaparJson(ex.getMessage()) + "\"}"); // Retorna HTTP 400
        } // Fin del bloque try/catch
    } // Fin de procesarCancelacionCita

    private static String extraerCampoTexto(String json, String clave, String valorPorDefecto) { // Extrae un String de un JSON
        Matcher m = Pattern.compile("\"" + clave + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json); // Busca "clave": "valor"
        return m.find() ? m.group(1) : valorPorDefecto; // Si lo encuentra devuelve el texto, si no el valor por defecto
    } // Fin de extraerCampoTexto

    private static int extraerCampoEntero(String json, String clave, int valorPorDefecto) { // Extrae un int de un JSON
        Matcher m = Pattern.compile("\"" + clave + "\"\\s*:\\s*(-?\\d+)").matcher(json); // Busca "clave": 123
        return m.find() ? Integer.parseInt(m.group(1)) : valorPorDefecto; // Parsea el entero encontrado
    } // Fin de extraerCampoEntero

    private static boolean extraerCampoBooleano(String json, String clave, boolean valorPorDefecto) { // Extrae un boolean de un JSON
        Matcher m = Pattern.compile("\"" + clave + "\"\\s*:\\s*(true|false)").matcher(json); // Busca "clave": true/false
        return m.find() ? Boolean.parseBoolean(m.group(1)) : valorPorDefecto; // Parsea el booleano encontrado
    } // Fin de extraerCampoBooleano

    private static void enviarRespuestaHttp(HttpExchange exchange, int status, String contentType, String cuerpo) throws IOException { // Envía respuesta HTTP
        byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8); // Convierte el texto de respuesta a bytes UTF-8
        exchange.getResponseHeaders().set("Content-Type", contentType); // Define la cabecera Content-Type
        exchange.sendResponseHeaders(status, bytes.length); // Envía el código de estado HTTP y la longitud en bytes
        try (OutputStream os = exchange.getResponseBody()) { // Abre el flujo de salida hacia el cliente
            os.write(bytes); // Escribe todos los bytes de la respuesta
        } // Cierra automáticamente el flujo de salida
    } // Fin de enviarRespuestaHttp

    private static String leerCuerpoPeticion(HttpExchange exchange) throws IOException { // Lee el cuerpo JSON enviado por POST
        try (InputStream is = exchange.getRequestBody()) { // Abre el flujo de entrada de la petición HTTP
            return new String(is.readAllBytes(), StandardCharsets.UTF_8); // Convierte los bytes leídos a un String UTF-8
        } // Cierra el InputStream
    } // Fin de leerCuerpoPeticion

    public static void main(String[] args) throws Exception { // Punto de entrada principal para ejecutar el servidor Java
        int puerto = Integer.parseInt(System.getenv().getOrDefault("PORT", "3000")); // Usa la variable PORT o 3000 por defecto
        BaseDeDatosCitas db = new BaseDeDatosCitas(); // Inicializa la base de datos SQLite / Memoria en Java
        ServidorCitasJava servicio = new ServidorCitasJava(db); // Instancia el controlador de negocio en Java

        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", puerto), 0); // Enlaza a 0.0.0.0:3000 para GitHub Codespaces

        server.createContext("/", (HttpExchange exchange) -> { // Registra el manejador de la ruta raíz "/"
            String ruta = exchange.getRequestURI().getPath(); // Obtiene la ruta solicitada por el navegador
            if ("GET".equals(exchange.getRequestMethod()) && ("/".equals(ruta) || "/index.html".equals(ruta))) { // Si piden la página web
                Path htmlPath = Path.of("public", "index.html"); // Ubica el archivo public/index.html
                String html = Files.exists(htmlPath) // Verifica si existe el archivo en la carpeta public
                    ? Files.readString(htmlPath, StandardCharsets.UTF_8) // Lo lee completo en UTF-8
                    : "<h1>MediQA Backend Java Activo</h1>"; // Mensaje de respaldo si se ejecuta desde otra ruta
                enviarRespuestaHttp(exchange, 200, "text/html; charset=utf-8", html); // Devuelve el HTML con código 200
                return; // Finaliza el manejo de la petición
            } // Fin del if de ruta raíz
            enviarRespuestaHttp(exchange, 404, "application/json; charset=utf-8", "{\"exito\":false,\"mensaje\":\"Ruta no encontrada\"}"); // Devuelve 404
        }); // Fin del contexto "/"

        server.createContext("/api/citas", (HttpExchange exchange) -> { // Registra el manejador de "/api/citas"
            String metodo = exchange.getRequestMethod(); // Obtiene el verbo HTTP (GET o POST)
            String ruta = exchange.getRequestURI().getPath(); // Obtiene la URL exacta solicitada

            if ("GET".equals(metodo) && "/api/citas".equals(ruta)) { // Si consultan el listado de citas con GET /api/citas
                List<CitaMedica> lista = db.listarCitas(); // Obtiene todas las citas desde BaseDeDatosCitas
                StringBuilder sb = new StringBuilder(); // Crea constructor de cadena para armar el arreglo JSON
                sb.append("{\"exito\":true,\"modoPersistencia\":\"") // Abre el objeto JSON de respuesta
                  .append(db.isModoSqlite() ? "Java JDBC SQLite (citas_medicas.sqlite)" : "Java 17 En-Memoria (HttpServer)") // Indica motor activo
                  .append("\",\"citas\":["); // Abre el arreglo de citas
                for (int i = 0; i < lista.size(); i++) { // Recorre cada cita almacenada
                    if (i > 0) sb.append(","); // Agrega coma separadora entre elementos JSON
                    sb.append(lista.get(i).toJson()); // Agrega el JSON de la cita actual
                } // Fin del bucle for
                sb.append("]}"); // Cierra el arreglo y el objeto JSON
                enviarRespuestaHttp(exchange, 200, "application/json; charset=utf-8", sb.toString()); // Envía respuesta HTTP 200
                return; // Termina atención de GET /api/citas
            } // Fin de GET /api/citas

            if ("POST".equals(metodo) && "/api/citas".equals(ruta)) { // Si envían una nueva cita con POST /api/citas
                String body = leerCuerpoPeticion(exchange); // Lee el cuerpo JSON enviado por el formulario
                RespuestaHttp resp = servicio.procesarCreacionCita( // Llama al orquestador de las Reglas 1, 2 y 3
                    extraerCampoTexto(body, "paciente", "Paciente"), // Extrae el nombre del paciente
                    extraerCampoEntero(body, "edad", 30), // Extrae la edad entera
                    extraerCampoBooleano(body, "tieneAcompaniante", false), // Extrae si lleva acompañante
                    extraerCampoTexto(body, "medicoId", "DR_GOMEZ"), // Extrae el ID del médico
                    extraerCampoTexto(body, "medicoNombre", ""), // Extrae el nombre del médico
                    extraerCampoTexto(body, "fecha", "2026-10-20"), // Extrae la fecha YYYY-MM-DD
                    extraerCampoTexto(body, "hora", "10:00") // Extrae la hora HH:MM
                ); // Cierra procesarCreacionCita
                enviarRespuestaHttp(exchange, resp.status(), "application/json; charset=utf-8", resp.json()); // Envía el JSON resultante
                return; // Termina atención de POST /api/citas
            } // Fin de POST /api/citas

            Matcher matchCancelar = Pattern.compile("^/api/citas/(\\d+)/cancelar$").matcher(ruta); // Evalúa si la ruta es /api/citas/{id}/cancelar
            if ("POST".equals(metodo) && matchCancelar.find()) { // Si es una petición POST de cancelación
                int idCita = Integer.parseInt(matchCancelar.group(1)); // Extrae el ID numérico de la URL
                String body = leerCuerpoPeticion(exchange); // Lee el cuerpo JSON con horasAnticipacion y esEmergencia
                RespuestaHttp resp = servicio.procesarCancelacionCita( // Ejecuta la Regla 4 (< 24h y emergencia)
                    idCita, // ID de la cita a cancelar
                    extraerCampoEntero(body, "horasAnticipacion", 6), // Horas de anticipación antes de la cita
                    extraerCampoBooleano(body, "esEmergencia", false) // Booleano de emergencia médica
                ); // Cierra procesarCancelacionCita
                enviarRespuestaHttp(exchange, resp.status(), "application/json; charset=utf-8", resp.json()); // Envía el resultado JSON
                return; // Termina atención de cancelación
            } // Fin de POST /api/citas/{id}/cancelar

            enviarRespuestaHttp(exchange, 404, "application/json; charset=utf-8", "{\"exito\":false,\"mensaje\":\"Endpoint no encontrado\"}"); // Ruta no válida
        }); // Fin del contexto "/api/citas"

        server.start(); // Inicia el servidor HTTP en segundo plano escuchando peticiones
        System.out.println("===================================================================="); // Imprime borde superior en consola
        System.out.println(" Servidor MediQA (JAVA 17 HttpServer) activo en http://localhost:" + puerto); // Muestra URL activa
        System.out.println(" En GitHub Codespaces: abre la pestaña 'PORTS' y haz clic en el globo del puerto " + puerto); // Guía Codespaces
        System.out.println("===================================================================="); // Imprime borde inferior
    } // Fin del método main
} // Fin de la clase ServidorCitasJava
