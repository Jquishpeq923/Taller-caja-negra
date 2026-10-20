package com.mediqa; // Declara que esta clase pertenece al paquete com.mediqa

import java.sql.Connection; // Importa Connection de JDBC para conectarse a SQLite
import java.sql.DriverManager; // Importa DriverManager para abrir la conexión JDBC
import java.sql.PreparedStatement; // Importa PreparedStatement para consultas SQL seguras parametrizadas
import java.sql.ResultSet; // Importa ResultSet para leer las filas devueltas por SQLite
import java.sql.Statement; // Importa Statement para ejecutar sentencias DDL (CREATE TABLE)
import java.util.ArrayList; // Importa ArrayList para almacenar listas dinámicas de citas en memoria
import java.util.Comparator; // Importa Comparator para ordenar las citas por minuto de inicio
import java.util.List; // Importa la interfaz List de Java
import com.mediqa.ModelosCita.CitaMedica; // Importa la entidad CitaMedica

/**
 * ============================================================================
 * ARCHIVO: src/main/java/com/mediqa/BaseDeDatosCitas.java
 * QUÉ HACE ESTE MÓDULO EN LA MIGRACIÓN A JAVA:
 * Reemplaza a `src/db.js`. Utiliza la API estándar **JDBC (`java.sql.*`)** de
 * Java para conectarse a `jdbc:sqlite:citas_medicas.sqlite`. Igual que en la
 * versión Node.js, si el driver externo `sqlite-jdbc` no está presente al
 * correr sin Maven, activa automáticamente su motor en memoria (`synchronized`)
 * con las mismas 2 citas semilla para que el programa funcione sin errores.
 * ============================================================================
 */
public class BaseDeDatosCitas { // Clase que gestiona la persistencia en SQLite y memoria

    private boolean modoSqlite = false; // Indica si la conexión JDBC a SQLite en disco está activa
    private Connection conexionSqlite = null; // Guarda el objeto Connection de JDBC si el driver está cargado
    private final List<CitaMedica> citasMemoria = new ArrayList<>(); // Lista en memoria para ejecución inmediata sin dependencias externas
    private int secuenciaId = 103; // Contador de IDs autoincrementales (inicia en 103 tras las citas semilla 101 y 102)

    public BaseDeDatosCitas() { // Constructor que inicializa la base de datos al arrancar el servidor
        try { // Intenta conectar con el archivo físico SQLite mediante JDBC
            Class.forName("org.sqlite.JDBC"); // Verifica si el driver JDBC de SQLite está disponible en el classpath
            this.conexionSqlite = DriverManager.getConnection("jdbc:sqlite:citas_medicas.sqlite"); // Abre el archivo citas_medicas.sqlite
            this.modoSqlite = true; // Activa la bandera indicando que SQLite por JDBC está funcionando
            inicializarEsquemaSqlite(); // Crea la tabla SQL 'citas' e inserta los registros semilla
        } catch (Exception _ex) { // Si se ejecuta con javac/java puro sin descargar el jar de sqlite-jdbc
            this.modoSqlite = false; // Desactiva la bandera de disco y pasa al modo en memoria seguro
            inicializarDatosSemillaMemoria(); // Carga las 2 citas iniciales en la lista en memoria
        } // Fin del bloque try/catch
    } // Fin del constructor BaseDeDatosCitas

    public boolean isModoSqlite() { // Método getter para informar qué motor de persistencia está activo
        return this.modoSqlite; // Retorna true si usa JDBC SQLite en disco, o false si usa memoria RAM
    } // Fin de isModoSqlite

    private void inicializarEsquemaSqlite() throws Exception { // Crea la tabla 'citas' en SQLite si no existe
        try (Statement stmt = this.conexionSqlite.createStatement()) { // Abre un Statement JDBC con cierre automático
            stmt.execute( // Ejecuta la sentencia SQL CREATE TABLE IF NOT EXISTS
                "CREATE TABLE IF NOT EXISTS citas (" + // Define el nombre de la tabla
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " + // Clave primaria autoincremental
                "paciente TEXT NOT NULL, " + // Columna para el nombre del paciente
                "edad INTEGER NOT NULL, " + // Columna para la edad
                "categoria TEXT NOT NULL, " + // Columna para la categoría ("PEDIATRICO" o "ADULTO")
                "tieneAcompaniante INTEGER NOT NULL, " + // Columna entera (1 o 0) para el acompañante
                "medicoId TEXT NOT NULL, " + // Columna para el código del médico
                "medicoNombre TEXT NOT NULL, " + // Columna para el nombre completo del médico
                "fecha TEXT NOT NULL, " + // Columna para la fecha YYYY-MM-DD
                "hora TEXT NOT NULL, " + // Columna para la hora HH:MM
                "inicioMin INTEGER NOT NULL, " + // Columna para el minuto de inicio
                "finMin INTEGER NOT NULL, " + // Columna para el minuto de fin
                "duracionMinutos INTEGER NOT NULL, " + // Columna para la duración (30 o 40 min)
                "estado TEXT NOT NULL DEFAULT 'CONFIRMADA', " + // Estado inicial por defecto
                "penalizado INTEGER NOT NULL DEFAULT 0, " + // Indicador de multa (0 o 1)
                "montoPenalizacion INTEGER NOT NULL DEFAULT 0)" // Monto de la multa ($0 o $25)
            ); // Cierra la ejecución de CREATE TABLE

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM citas")) { // Consulta cuántas citas hay guardadas
                if (rs.next() && rs.getInt("total") == 0) { // Si la tabla recién creada tiene 0 registros
                    insertarCitaSqlite(new CitaMedica(101, "Carlos Ruiz", 42, "ADULTO", false, "DR_GOMEZ", "Dr. Alejandro Gómez (Medicina Familiar)", "2026-10-20", "10:00", 600, 630, 30, "CONFIRMADA", false, 0)); // Inserta semilla #101
                    insertarCitaSqlite(new CitaMedica(102, "Sofía Andrade", 8, "PEDIATRICO", true, "DR_GOMEZ", "Dr. Alejandro Gómez (Medicina Familiar)", "2026-10-20", "11:00", 660, 700, 40, "CONFIRMADA", false, 0)); // Inserta semilla #102
                } // Fin del if tabla vacía
            } // Cierra automáticamente el ResultSet
        } // Cierra automáticamente el Statement
    } // Fin de inicializarEsquemaSqlite

    private void insertarCitaSqlite(CitaMedica c) throws Exception { // Método auxiliar para insertar una fila en SQLite vía JDBC
        String sql = "INSERT INTO citas (paciente, edad, categoria, tieneAcompaniante, medicoId, medicoNombre, fecha, hora, inicioMin, finMin, duracionMinutos, estado, penalizado, montoPenalizacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; // SQL parametrizado
        try (PreparedStatement ps = this.conexionSqlite.prepareStatement(sql)) { // Prepara la sentencia segura contra inyección SQL
            ps.setString(1, c.paciente); // Asigna parámetro 1: paciente
            ps.setInt(2, c.edad); // Asigna parámetro 2: edad
            ps.setString(3, c.categoria); // Asigna parámetro 3: categoria
            ps.setInt(4, c.tieneAcompaniante ? 1 : 0); // Asigna parámetro 4: 1 si lleva tutor, 0 si no
            ps.setString(5, c.medicoId); // Asigna parámetro 5: medicoId
            ps.setString(6, c.medicoNombre); // Asigna parámetro 6: medicoNombre
            ps.setString(7, c.fecha); // Asigna parámetro 7: fecha
            ps.setString(8, c.hora); // Asigna parámetro 8: hora
            ps.setInt(9, c.inicioMin); // Asigna parámetro 9: inicioMin
            ps.setInt(10, c.finMin); // Asigna parámetro 10: finMin
            ps.setInt(11, c.duracionMinutos); // Asigna parámetro 11: duracionMinutos
            ps.setString(12, c.estado); // Asigna parámetro 12: estado
            ps.setInt(13, c.penalizado ? 1 : 0); // Asigna parámetro 13: penalizado
            ps.setInt(14, c.montoPenalizacion); // Asigna parámetro 14: montoPenalizacion
            ps.executeUpdate(); // Ejecuta el INSERT en el archivo SQLite
        } // Cierra el PreparedStatement
    } // Fin de insertarCitaSqlite

    public final synchronized void inicializarDatosSemillaMemoria() { // Carga las 2 citas iniciales en memoria
        this.citasMemoria.clear(); // Limpia cualquier registro previo de la lista en memoria
        this.secuenciaId = 103; // Reinicia el contador de IDs en 103
        this.citasMemoria.add(new CitaMedica( // Agrega la Cita #101 (Paciente Adulto a las 10:00)
            101, "Carlos Ruiz", 42, "ADULTO", false, "DR_GOMEZ", // Datos del paciente y código de médico
            "Dr. Alejandro Gómez (Medicina Familiar)", "2026-10-20", "10:00", // Médico, fecha y hora
            600, 630, 30, "CONFIRMADA", false, 0 // Intervalo [600, 630), 30 min, confirmada y sin multa
        )); // Cierra adición de la Cita #101
        this.citasMemoria.add(new CitaMedica( // Agrega la Cita #102 (Paciente Pediátrica a las 11:00)
            102, "Sofía Andrade", 8, "PEDIATRICO", true, "DR_GOMEZ", // Menor de 8 años con acompañante adulto
            "Dr. Alejandro Gómez (Medicina Familiar)", "2026-10-20", "11:00", // Médico, fecha y hora
            660, 700, 40, "CONFIRMADA", false, 0 // Intervalo [660, 700), 40 min, confirmada y sin multa
        )); // Cierra adición de la Cita #102
    } // Fin de inicializarDatosSemillaMemoria

    public synchronized List<CitaMedica> listarCitas() { // Devuelve todas las citas ordenadas por horario
        if (this.modoSqlite) { // Si está activo el motor SQLite en disco
            List<CitaMedica> resultado = new ArrayList<>(); // Crea lista para almacenar las filas leídas
            try (Statement stmt = this.conexionSqlite.createStatement(); // Crea Statement JDBC
                 ResultSet rs = stmt.executeQuery("SELECT * FROM citas ORDER BY fecha ASC, inicioMin ASC")) { // Ejecuta SELECT
                while (rs.next()) { // Recorre cada fila del ResultSet
                    resultado.add(new CitaMedica( // Construye un objeto CitaMedica por cada fila de la tabla
                        rs.getInt("id"), rs.getString("paciente"), rs.getInt("edad"), // Lee id, paciente y edad
                        rs.getString("categoria"), rs.getInt("tieneAcompaniante") == 1, // Lee categoría y tutor
                        rs.getString("medicoId"), rs.getString("medicoNombre"), // Lee datos del médico
                        rs.getString("fecha"), rs.getString("hora"), // Lee fecha y hora
                        rs.getInt("inicioMin"), rs.getInt("finMin"), rs.getInt("duracionMinutos"), // Lee minutos
                        rs.getString("estado"), rs.getInt("penalizado") == 1, rs.getInt("montoPenalizacion") // Lee estado y multa
                    )); // Agrega la cita a la lista resultado
                } // Fin del bucle while
                return resultado; // Devuelve la lista proveniente de SQLite
            } catch (Exception _e) { // En caso de fallo de lectura en disco
                // Continúa al retorno en memoria
            } // Fin try/catch
        } // Fin if modoSqlite
        List<CitaMedica> copia = new ArrayList<>(this.citasMemoria); // Crea una copia de la lista en memoria
        copia.sort(Comparator.comparingInt((CitaMedica c) -> c.inicioMin)); // Ordena las citas de menor a mayor minuto de inicio
        return copia; // Retorna la lista ordenada
    } // Fin de listarCitas

    public synchronized CitaMedica obtenerCitaPorId(int id) { // Busca una cita específica por su número de ID
        for (CitaMedica c : listarCitas()) { // Recorre las citas actuales
            if (c.id == id) { // Si el ID de la cita coincide con el buscado
                return c; // Devuelve la instancia encontrada
            } // Fin del if
        } // Fin del bucle for
        return null; // Si no existe ninguna cita con ese ID, retorna null
    } // Fin de obtenerCitaPorId

    public synchronized CitaMedica insertarCita( // Crea y almacena una nueva cita médica ya validada
        String paciente, int edad, String categoria, boolean tieneAcompaniante, // Datos del paciente
        String medicoId, String medicoNombre, String fecha, String hora, // Datos del médico y horario
        int inicioMin, int finMin, int duracionMinutos // Intervalo en minutos
    ) { // Abre el método insertarCita
        int nuevoId = this.secuenciaId++; // Genera el nuevo ID único e incrementa la secuencia
        CitaMedica nueva = new CitaMedica( // Instancia el nuevo objeto CitaMedica
            nuevoId, paciente, edad, categoria, tieneAcompaniante, // Pasa datos personales
            medicoId, medicoNombre, fecha, hora, // Pasa datos de agenda
            inicioMin, finMin, duracionMinutos, // Pasa tiempos calculados
            "CONFIRMADA", false, 0 // Estado inicial CONFIRMADA y $0 de penalización
        ); // Cierra instanciación
        if (this.modoSqlite) { // Si está activo SQLite en disco
            try { // Intenta guardar en la tabla SQL
                insertarCitaSqlite(nueva); // Ejecuta el INSERT parametrizado
            } catch (Exception _e) { // Si ocurre algún error de E/S
                this.citasMemoria.add(nueva); // Respalda en memoria
            } // Fin try/catch
        } else { // Si está en modo memoria
            this.citasMemoria.add(nueva); // Agrega la nueva cita a la colección en memoria
        } // Fin if/else
        return nueva; // Devuelve la cita creada
    } // Fin de insertarCita

    public synchronized CitaMedica actualizarCancelacion(int id, boolean penalizado, int montoPenalizacion) { // Registra la cancelación
        if (this.modoSqlite) { // Si usa SQLite en disco
            try (PreparedStatement ps = this.conexionSqlite.prepareStatement( // Prepara sentencia UPDATE
                "UPDATE citas SET estado = 'CANCELADA', penalizado = ?, montoPenalizacion = ? WHERE id = ?" // SQL UPDATE
            )) { // Abre bloque try-with-resources
                ps.setInt(1, penalizado ? 1 : 0); // Asigna 1 si hubo penalización, 0 si no
                ps.setInt(2, montoPenalizacion); // Asigna el monto ($25 o $0)
                ps.setInt(3, id); // Filtra por el ID de la cita
                ps.executeUpdate(); // Ejecuta la actualización en SQLite
            } catch (Exception _e) { // Si falla en disco, actualiza también en memoria
                // Continúa abajo
            } // Fin try/catch
        } // Fin if modoSqlite
        for (CitaMedica c : this.citasMemoria) { // Busca la cita en la lista en memoria
            if (c.id == id) { // Cuando encuentra el ID correspondiente
                c.estado = "CANCELADA"; // Actualiza el estado a CANCELADA
                c.penalizado = penalizado; // Guarda si fue penalizada por la Regla 4
                c.montoPenalizacion = montoPenalizacion; // Guarda el monto cobrado ($25 o $0)
                return c; // Retorna la cita actualizada
            } // Fin del if
        } // Fin del bucle for
        return obtenerCitaPorId(id); // Retorna la cita actualizada desde SQLite
    } // Fin de actualizarCancelacion
} // Fin de la clase BaseDeDatosCitas
