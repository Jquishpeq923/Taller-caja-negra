/**
 * ============================================================================
 * ARCHIVO: src/db.js
 * QUÉ HACE ESTE MÓDULO:
 * Implementa la capa de Base de Datos SQLite solicitada en el Prompt Maestro
 * (Diapositiva 3). Si `better-sqlite3` está instalado (tras `npm install`),
 * guarda todo en el archivo real `citas_medicas.sqlite`. Si aún no se ha corrido
 * `npm install`, activa automáticamente un motor SQLite en memoria con los
 * mismos datos semilla para que la app jamás falle en GitHub Codespaces.
 * ============================================================================
 */

const path = require('node:path'); // Importa el módulo nativo 'path' de Node.js para construir rutas de archivo seguras

// ============================================================================
// PARTE 1: CLASE GESTORA DE BASE DE DATOS (SQLite + Respaldo en Memoria)
// ============================================================================
class BaseDeDatosCitas { // Define la clase que administra la creación de tablas, consultas e inserciones de citas
  constructor() { // Método constructor que se ejecuta automáticamente al iniciar el servidor
    this.modoSqlite = false; // Bandera que indica si estamos usando el archivo físico SQLite (true) o memoria (false)
    this.db = null; // Propiedad que almacenará la instancia de conexión a SQLite
    this.citasMemoria = []; // Arreglo de respaldo en memoria por si se ejecuta sin instalar paquetes npm
    this.secuenciaId = 103; // Contador de IDs autoincrementales (inicia en 103 porque hay 2 citas semilla: 101 y 102)

    try { // Bloque try para intentar cargar el driver nativo de SQLite
      const Database = require('better-sqlite3'); // Carga la librería 'better-sqlite3' desde node_modules
      const dbPath = path.join(__dirname, '..', 'citas_medicas.sqlite'); // Define la ruta del archivo citas_medicas.sqlite en la raíz
      this.db = new Database(dbPath); // Abre o crea el archivo de base de datos SQLite en disco
      this.modoSqlite = true; // Marca que el motor SQLite en disco está activo exitosamente
      this._inicializarEsquemaSqlite(); // Llama al método que crea la tabla 'citas' e inserta los datos iniciales
    } catch (_err) { // Si no se ha ejecutado 'npm install' todavía, captura el error sin detener el programa
      this._inicializarDatosSemillaMemoria(); // Inicializa las citas de prueba en memoria para que el sistema funcione igual
    } // Fin del bloque try/catch
  } // Fin del constructor

  // ==========================================================================
  // PARTE 2: CREACIÓN DE TABLA SQL Y DATOS SEMILLA EN SQLITE
  // ==========================================================================
  _inicializarEsquemaSqlite() { // Método privado que prepara la tabla SQL 'citas' si aún no existe
    this.db.exec(` // Ejecuta la sentencia DDL de creación de tabla en el motor SQLite
      CREATE TABLE IF NOT EXISTS citas (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        paciente TEXT NOT NULL,
        edad INTEGER NOT NULL,
        categoria TEXT NOT NULL,
        tieneAcompaniante INTEGER NOT NULL,
        medicoId TEXT NOT NULL,
        medicoNombre TEXT NOT NULL,
        fecha TEXT NOT NULL,
        hora TEXT NOT NULL,
        inicioMin INTEGER NOT NULL,
        finMin INTEGER NOT NULL,
        duracionMinutos INTEGER NOT NULL,
        estado TEXT NOT NULL DEFAULT 'CONFIRMADA',
        penalizado INTEGER NOT NULL DEFAULT 0,
        montoPenalizacion REAL NOT NULL DEFAULT 0
      );
    `); // Cierra la ejecución de la consulta CREATE TABLE

    const conteo = this.db.prepare('SELECT COUNT(*) AS total FROM citas').get(); // Consulta cuántas filas tiene actualmente la tabla
    if (conteo.total === 0) { // Si la tabla está vacía (0 registros), inserta 2 citas iniciales para demostración
      const insert = this.db.prepare(` // Prepara la sentencia SQL parametrizada INSERT INTO para evitar inyección SQL
        INSERT INTO citas (
          paciente, edad, categoria, tieneAcompaniante, medicoId, medicoNombre,
          fecha, hora, inicioMin, finMin, duracionMinutos, estado, penalizado, montoPenalizacion
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      `); // Cierra la preparación del INSERT
      insert.run('Carlos Ruiz', 42, 'ADULTO', 0, 'DR_GOMEZ', 'Dr. Alejandro Gómez (Medicina Familiar)', '2026-10-20', '10:00', 600, 630, 30, 'CONFIRMADA', 0, 0); // Inserta la Cita 1 (Adulto a las 10:00)
      insert.run('Sofía Andrade', 8, 'PEDIATRICO', 1, 'DR_GOMEZ', 'Dr. Alejandro Gómez (Medicina Familiar)', '2026-10-20', '11:00', 660, 700, 40, 'CONFIRMADA', 0, 0); // Inserta la Cita 2 (Pediátrica a las 11:00)
    } // Fin del condicional de tabla vacía
  } // Fin de _inicializarEsquemaSqlite

  // ==========================================================================
  // PARTE 3: INICIALIZACIÓN DE DATOS SEMILLA EN MEMORIA (MODO SIN DEPENDENCIAS)
  // ==========================================================================
  _inicializarDatosSemillaMemoria() { // Carga exactamente las mismas 2 citas iniciales en memoria RAM
    this.citasMemoria = [ // Asigna el arreglo inicial de objetos de cita
      { // Primer registro semilla (Cita #101)
        id: 101, // Identificador único de la cita
        paciente: 'Carlos Ruiz', // Nombre del paciente adulto
        edad: 42, // Edad de 42 años (>= 18 -> Adulto)
        categoria: 'ADULTO', // Categoría asignada por la Regla 1
        tieneAcompaniante: false, // Adulto no requiere acompañante
        medicoId: 'DR_GOMEZ', // Código del médico asignado
        medicoNombre: 'Dr. Alejandro Gómez (Medicina Familiar)', // Nombre visible del facultativo
        fecha: '2026-10-20', // Fecha hábil (Martes)
        hora: '10:00', // Hora de inicio (10:00 AM)
        inicioMin: 600, // Minuto del día correspondiente a las 10:00 (10 * 60 = 600)
        finMin: 630, // Minuto de fin sumando 30 min de consulta adulta (630 = 10:30)
        duracionMinutos: 30, // Duración estándar de adulto
        estado: 'CONFIRMADA', // Estado activo inicial
        penalizado: false, // Sin penalización de cancelación
        montoPenalizacion: 0 // Monto de multa en $0
      }, // Fin del primer objeto de cita
      { // Segundo registro semilla (Cita #102)
        id: 102, // Identificador único de la segunda cita
        paciente: 'Sofía Andrade', // Nombre de la paciente pediátrica
        edad: 8, // Edad de 8 años (< 18 -> Pediátrico)
        categoria: 'PEDIATRICO', // Categoría asignada por la Regla 1
        tieneAcompaniante: true, // Cumple el requisito de acudir con un adulto responsable
        medicoId: 'DR_GOMEZ', // Código del médico asignado
        medicoNombre: 'Dr. Alejandro Gómez (Medicina Familiar)', // Nombre del médico
        fecha: '2026-10-20', // Fecha hábil (Martes)
        hora: '11:00', // Hora de inicio (11:00 AM)
        inicioMin: 660, // Minuto del día correspondiente a las 11:00 (11 * 60 = 660)
        finMin: 700, // Minuto de fin sumando 40 min de consulta pediátrica (700 = 11:40)
        duracionMinutos: 40, // Duración extendida para paciente pediátrico
        estado: 'CONFIRMADA', // Estado activo inicial
        penalizado: false, // Sin penalización
        montoPenalizacion: 0 // Monto de multa en $0
      } // Fin del segundo objeto de cita
    ]; // Cierra el arreglo de citas en memoria
  } // Fin de _inicializarDatosSemillaMemoria

  // ==========================================================================
  // PARTE 4: OPERACIONES CRUD (Listar, Buscar por ID, Insertar y Cancelar)
  // ==========================================================================
  listarCitas() { // Devuelve todas las citas ordenadas por fecha y hora de inicio
    if (this.modoSqlite) { // Si está activo el archivo SQLite en disco
      return this.db.prepare('SELECT * FROM citas ORDER BY fecha ASC, inicioMin ASC').all(); // Ejecuta SELECT y retorna las filas
    } // Fin del if modoSqlite
    return [...this.citasMemoria].sort((a, b) => a.inicioMin - b.inicioMin); // En modo memoria, retorna copia ordenada por minuto de inicio
  } // Fin de listarCitas

  obtenerCitaPorId(id) { // Busca y devuelve una cita específica según su número de ID
    const idNum = Number(id); // Convierte el parámetro id a número entero
    if (this.modoSqlite) { // Si usa SQLite en disco
      return this.db.prepare('SELECT * FROM citas WHERE id = ?').get(idNum) || null; // Busca la fila por ID o devuelve null
    } // Fin del if modoSqlite
    return this.citasMemoria.find((c) => c.id === idNum) || null; // Busca en el arreglo en memoria por ID o devuelve null
  } // Fin de obtenerCitaPorId

  insertarCita(cita) { // Guarda una nueva cita validada en la base de datos
    if (this.modoSqlite) { // Si usa SQLite en disco
      const stmt = this.db.prepare(` // Prepara la consulta SQL INSERT
        INSERT INTO citas (
          paciente, edad, categoria, tieneAcompaniante, medicoId, medicoNombre,
          fecha, hora, inicioMin, finMin, duracionMinutos, estado, penalizado, montoPenalizacion
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'CONFIRMADA', 0, 0)
      `); // Cierra la sentencia preparada
      const info = stmt.run( // Ejecuta la inserción pasando cada campo validado
        cita.paciente, // Nombre del paciente
        cita.edad, // Edad numérica
        cita.categoria, // Categoría ('PEDIATRICO' o 'ADULTO')
        cita.tieneAcompaniante ? 1 : 0, // Convierte booleano a entero 1/0 para SQLite
        cita.medicoId, // ID del médico
        cita.medicoNombre, // Nombre del médico
        cita.fecha, // Fecha YYYY-MM-DD
        cita.hora, // Hora HH:MM
        cita.inicioMin, // Inicio en minutos desde las 00:00
        cita.finMin, // Fin en minutos desde las 00:00
        cita.duracionMinutos // Duración en minutos (30 o 40)
      ); // Cierra la ejecución de stmt.run
      return this.obtenerCitaPorId(info.lastInsertRowid); // Retorna el registro recién insertado desde SQLite
    } // Fin del if modoSqlite

    const nueva = { // Si está en modo memoria, construye el nuevo objeto de cita
      id: this.secuenciaId++, // Asigna el siguiente ID disponible e incrementa el contador
      ...cita, // Copia todos los campos de la cita validada
      estado: 'CONFIRMADA', // Inicializa el estado como CONFIRMADA
      penalizado: false, // Inicializa sin penalización
      montoPenalizacion: 0 // Inicializa el cargo de penalización en $0
    }; // Cierra el objeto nueva
    this.citasMemoria.push(nueva); // Agrega la nueva cita al arreglo en memoria
    return nueva; // Devuelve la cita creada al controlador del servidor
  } // Fin de insertarCita

  actualizarCancelacion(id, penalizado, montoPenalizacion) { // Actualiza el estado de una cita a CANCELADA y registra si hubo multa
    const idNum = Number(id); // Asegura que el ID sea numérico
    if (this.modoSqlite) { // Si usa SQLite en disco
      this.db // Accede a la instancia SQLite
        .prepare('UPDATE citas SET estado = ?, penalizado = ?, montoPenalizacion = ? WHERE id = ?') // Prepara sentencia UPDATE
        .run('CANCELADA', penalizado ? 1 : 0, montoPenalizacion, idNum); // Ejecuta la actualización con el resultado de la Regla 4
      return this.obtenerCitaPorId(idNum); // Devuelve la cita actualizada desde SQLite
    } // Fin del if modoSqlite

    const cita = this.obtenerCitaPorId(idNum); // En modo memoria, localiza el objeto de la cita
    if (cita) { // Si la cita existe en memoria
      cita.estado = 'CANCELADA'; // Cambia su estado a CANCELADA
      cita.penalizado = penalizado; // Registra si la Regla 4 aplicó penalización (true/false)
      cita.montoPenalizacion = montoPenalizacion; // Guarda el monto exacto ($25 o $0)
    } // Fin del if (cita)
    return cita; // Retorna la cita modificada
  } // Fin de actualizarCancelacion
} // Fin de la clase BaseDeDatosCitas

module.exports = new BaseDeDatosCitas(); // Exporta una instancia única (Singleton) lista para usar en server.js
