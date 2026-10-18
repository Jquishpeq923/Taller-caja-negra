/**
 * ============================================================================
 * ARCHIVO: src/server.js
 * QUÉ HACE ESTE MÓDULO:
 * Levanta el servidor web y la API REST del Sistema de Citas Médicas en el
 * puerto 3000 (escuchando en 0.0.0.0 para que GitHub Codespaces abra la vista
 * web automáticamente).
 * Funciona con Express (si ejecutaste `npm install`) e incluye además un
 * servidor HTTP nativo de respaldo para que funcione incluso SIN `npm install`.
 * ============================================================================
 */

const fs = require('node:fs'); // Importa el módulo nativo 'fs' para leer el archivo public/index.html
const http = require('node:http'); // Importa el módulo nativo 'http' para el servidor de respaldo sin dependencias
const path = require('node:path'); // Importa 'path' para resolver rutas de carpetas en cualquier sistema operativo
const db = require('./db'); // Importa la capa de base de datos SQLite (src/db.js)
const { // Importa las funciones de negocio auditadas con McCabe desde src/reglasCitas.js
  validarReglaEdad, // Regla 1: Validación de paciente pediátrico (<18 con tutor) vs. adulto
  validarHorarioOperativo, // Regla 2: Horario Lun-Vie 08:00-18:00 y bloqueo de almuerzo (13:00)
  verificarChoqueCitas, // Regla 3: Prevención de doble reserva (solapamiento de minutos)
  procesarCancelacionPorHoras // Regla 4: Penalización de $25 por cancelación tardía (<24h) sin emergencia
} = require('./reglasCitas'); // Cierra la importación de reglas de negocio

// ============================================================================
// PARTE 1: LÓGICA CENTRAL DE LOS ENDPOINTS DE LA API (Compartida por Express y HTTP nativo)
// Qué hace esta parte: Procesa la creación y cancelación de citas invocando
// en orden estricto las 4 reglas auditadas antes de tocar la base de datos.
// ============================================================================
function procesarCreacionCita(body) { // Función que valida y registra una nueva cita a partir del JSON recibido
  const { paciente, edad, tieneAcompaniante, medicoId, medicoNombre, fecha, hora } = body; // Extrae los campos enviados por el frontend

  // Paso 1: Ejecutar Regla 1 (Edad pediátrica vs. adulta)
  const resultadoEdad = validarReglaEdad(Number(edad), Boolean(tieneAcompaniante)); // Evalúa la edad y si lleva acompañante
  if (!resultadoEdad.valido) { // Si la Regla 1 rechaza al paciente (edad inválida o menor sin tutor)
    return { status: 400, payload: { exito: false, reglaViolada: 'REGLA_1_EDAD', ...resultadoEdad } }; // Devuelve HTTP 400 con el motivo exacto
  } // Fin de validación de la Regla 1

  // Paso 2: Ejecutar Regla 2 (Restricción de horarios operativos y almuerzo)
  const [anio, mes, dia] = fecha.split('-').map(Number); // Separa la fecha 'YYYY-MM-DD' en números de año, mes y día
  const fechaObj = new Date(anio, mes - 1, dia); // Construye el objeto Date local para saber qué día de la semana es
  const diaSemana = fechaObj.getDay(); // Obtiene el día de la semana (0 = Domingo ... 6 = Sábado)
  const [horaNum, minNum] = hora.split(':').map(Number); // Separa la hora 'HH:MM' en números de hora y minutos

  const resultadoHorario = validarHorarioOperativo(diaSemana, horaNum); // Evalúa si el día y la hora están permitidos
  if (!resultadoHorario.operativo) { // Si es fin de semana, fuera de 08:00-18:00 o es la 13:00 (almuerzo)
    return { status: 400, payload: { exito: false, reglaViolada: 'REGLA_2_HORARIO', ...resultadoHorario } }; // Devuelve HTTP 400 bloqueando la franja
  } // Fin de validación de la Regla 2

  // Paso 3: Ejecutar Regla 3 (Prevención de choque de citas / doble reserva)
  const inicioMin = horaNum * 60 + minNum; // Convierte la hora de inicio al minuto del día (ej. 10:00 -> 600)
  const finMin = inicioMin + resultadoEdad.duracionMinutos; // Calcula el minuto de salida sumando 40 min (niño) o 30 min (adulto)
  const resultadoChoque = verificarChoqueCitas( // Invoca la Regla 3 comparando contra las citas actuales en SQLite
    { medicoId, fecha, inicioMin, finMin }, // Datos de intervalo de la nueva cita solicitada
    db.listarCitas() // Lista actual de todas las citas guardadas en la base de datos
  ); // Cierra llamada a verificarChoqueCitas

  if (resultadoChoque.hayChoque) { // Si la Regla 3 detecta que el médico ya está ocupado en ese rango de minutos
    return { status: 409, payload: { exito: false, reglaViolada: 'REGLA_3_CHOQUE', ...resultadoChoque } }; // Devuelve HTTP 409 (Conflict)
  } // Fin de validación de la Regla 3

  // Paso 4: Si pasó las 3 reglas críticas, guarda la cita en SQLite
  const citaCreada = db.insertarCita({ // Inserta el registro validado en la base de datos
    paciente, // Nombre del paciente
    edad: Number(edad), // Edad convertida a número
    categoria: resultadoEdad.categoria, // 'PEDIATRICO' o 'ADULTO'
    tieneAcompaniante: Boolean(tieneAcompaniante), // Booleano de tutor
    medicoId, // Identificador del médico
    medicoNombre: medicoNombre || 'Dr. Alejandro Gómez (Medicina Familiar)', // Nombre descriptivo del médico
    fecha, // Fecha de la cita
    hora, // Hora de inicio
    inicioMin, // Minuto de inicio
    finMin, // Minuto de fin
    duracionMinutos: resultadoEdad.duracionMinutos // Duración calculada por la Regla 1
  }); // Cierra la inserción en base de datos

  return { // Devuelve código HTTP 201 (Created) con la cita almacenada
    status: 201, // Código estándar REST para recurso creado con éxito
    payload: { // Cuerpo JSON de respuesta
      exito: true, // Indica operación exitosa
      mensaje: 'Cita médica agendada exitosamente tras superar las auditorías de reglas.', // Mensaje informativo
      cita: citaCreada // Objeto completo de la cita guardada con su ID
    } // Cierra payload
  }; // Cierra retorno de procesarCreacionCita
} // Fin de procesarCreacionCita

function procesarCancelacionCita(idCita, body) { // Función que procesa la cancelación aplicando la Regla 4 (< 24h y emergencia)
  const cita = db.obtenerCitaPorId(idCita); // Busca la cita en SQLite por su ID
  if (!cita) { // Si el ID no existe en la base de datos
    return { status: 404, payload: { exito: false, mensaje: 'Cita no encontrada.' } }; // Retorna HTTP 404 (Not Found)
  } // Fin de verificación de existencia

  const { horasAnticipacion, esEmergencia } = body; // Extrae las horas de anticipación y si es emergencia médica
  try { // Bloque try por si horasAnticipacion es negativo
    let penalizacionRegistrada = false; // Variable bandera que el callback de penalización cambiará a true si se ejecuta
    const resultadoCancelacion = procesarCancelacionPorHoras( // Llama a la Regla 4 auditada con McCabe V(G) = 3
      Number(horasAnticipacion), // Horas restantes antes de la cita
      Boolean(esEmergencia), // Booleano que indica si existe urgencia médica justificada
      () => { // Callback aplicarPenalizacion() que se ejecuta solo en el Camino 3 (tardía && !emergencia)
        penalizacionRegistrada = true; // Marca que el efecto secundario de cobro de multa se disparó
      } // Cierra el callback
    ); // Cierra llamada a procesarCancelacionPorHoras

    const citaActualizada = db.actualizarCancelacion( // Actualiza el estado de la cita en SQLite
      cita.id, // ID de la cita a cancelar
      penalizacionRegistrada && resultadoCancelacion.penalizado, // Confirma si quedó penalizada
      resultadoCancelacion.montoPenalizacion // Registra el monto ($25 o $0)
    ); // Cierra actualización en BD

    return { // Retorna HTTP 200 con el dictamen de la Regla 4
      status: 200, // Código HTTP de éxito
      payload: { exito: true, ...resultadoCancelacion, cita: citaActualizada } // Respuesta JSON completa
    }; // Cierra retorno exitoso
  } catch (err) { // Si procesarCancelacionPorHoras lanzó error (ej. horas < 0)
    return { status: 400, payload: { exito: false, mensaje: err.message } }; // Retorna HTTP 400 con el mensaje de error
  } // Fin del bloque try/catch
} // Fin de procesarCancelacionCita

// ============================================================================
// PARTE 2: CONFIGURACIÓN DEL SERVIDOR EXPRESS (Cuando se instalaron paquetes npm)
// ============================================================================
function crearServidorExpress() { // Crea y configura la aplicación Express
  const express = require('express'); // Carga el paquete express desde node_modules
  const app = express(); // Inicializa la instancia de Express

  app.use(express.json()); // Habilita el middleware para leer cuerpos JSON en peticiones POST
  app.use(express.static(path.join(__dirname, '..', 'public'))); // Sirve automáticamente la carpeta 'public' (donde vive index.html)

  app.get('/api/citas', (_req, res) => { // Endpoint GET /api/citas para consultar todas las citas
    res.json({ // Envía respuesta en formato JSON
      exito: true, // Bandera de éxito
      modoPersistencia: db.modoSqlite ? 'SQLite (citas_medicas.sqlite)' : 'SQLite En-Memoria', // Indica qué motor está activo
      citas: db.listarCitas() // Lista de citas ordenada
    }); // Cierra res.json
  }); // Fin de GET /api/citas

  app.post('/api/citas', (req, res) => { // Endpoint POST /api/citas para crear una nueva cita validando Reglas 1, 2 y 3
    const resultado = procesarCreacionCita(req.body); // Ejecuta el flujo de validación y registro
    return res.status(resultado.status).json(resultado.payload); // Responde con el código HTTP (201, 400 o 409) y el JSON
  }); // Fin de POST /api/citas

  app.post('/api/citas/:id/cancelar', (req, res) => { // Endpoint POST /api/citas/:id/cancelar para aplicar la Regla 4 (<24h)
    const resultado = procesarCancelacionCita(req.params.id, req.body); // Evalúa la penalización tardía
    return res.status(resultado.status).json(resultado.payload); // Responde con el dictamen y monto ($25 o $0)
  }); // Fin de POST /api/citas/:id/cancelar

  return app; // Retorna la aplicación Express configurada
} // Fin de crearServidorExpress

// ============================================================================
// PARTE 3: SERVIDOR HTTP NATIVO DE RESPALDO (Funciona en Codespaces aún sin npm install)
// ============================================================================
function crearServidorHttpNativo() { // Crea un servidor web idéntico usando solo módulos nativos de Node.js
  const indexHtmlPath = path.join(__dirname, '..', 'public', 'index.html'); // Ruta al archivo frontend Alpine.js

  return http.createServer((req, res) => { // Crea el servidor HTTP que escucha cada petición del navegador
    if (req.method === 'GET' && (req.url === '/' || req.url.startsWith('/index.html'))) { // Si piden la página principal
      const html = fs.readFileSync(indexHtmlPath, 'utf-8'); // Lee el archivo public/index.html del disco
      res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' }); // Define cabecera HTML con soporte de tildes/ñ
      return res.end(html); // Envía la aplicación web al navegador
    } // Fin de ruta principal GET /

    if (req.method === 'GET' && req.url === '/api/citas') { // Si el frontend solicita la lista de citas por GET /api/citas
      res.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8' }); // Define cabecera JSON
      return res.end(JSON.stringify({ // Convierte a texto JSON la lista de citas actual
        exito: true, // Indica respuesta exitosa
        modoPersistencia: db.modoSqlite ? 'SQLite (citas_medicas.sqlite)' : 'SQLite En-Memoria (HTTP Nativo)', // Estado del motor
        citas: db.listarCitas() // Lista de citas desde db.js
      })); // Envía el JSON y cierra la respuesta
    } // Fin de GET /api/citas

    if (req.method === 'POST' && req.url === '/api/citas') { // Si envían datos para agendar una nueva cita
      let cuerpo = ''; // Variable para acumular el texto JSON entrante
      req.on('data', (chunk) => { cuerpo += chunk; }); // Escucha cada fragmento de datos enviado por el cliente
      req.on('end', () => { // Cuando termina de llegar el cuerpo de la petición
        const resultado = procesarCreacionCita(JSON.parse(cuerpo || '{}')); // Parsea el JSON y ejecuta las Reglas 1, 2 y 3
        res.writeHead(resultado.status, { 'Content-Type': 'application/json; charset=utf-8' }); // Escribe el código HTTP resultante
        res.end(JSON.stringify(resultado.payload)); // Envía la respuesta JSON al navegador
      }); // Fin del evento end
      return; // Termina el manejo de POST /api/citas
    } // Fin de POST /api/citas

    const matchCancelar = req.url && req.url.match(/^\/api\/citas\/(\d+)\/cancelar$/); // Verifica si la URL es /api/citas/:id/cancelar
    if (req.method === 'POST' && matchCancelar) { // Si es una petición POST de cancelación de cita
      let cuerpo = ''; // Variable para acumular el JSON de cancelación
      req.on('data', (chunk) => { cuerpo += chunk; }); // Recibe los fragmentos de datos
      req.on('end', () => { // Cuando termina de recibir los datos
        const resultado = procesarCancelacionCita(matchCancelar[1], JSON.parse(cuerpo || '{}')); // Ejecuta la Regla 4 para ese ID
        res.writeHead(resultado.status, { 'Content-Type': 'application/json; charset=utf-8' }); // Escribe cabecera con estado HTTP
        res.end(JSON.stringify(resultado.payload)); // Envía el dictamen de penalización al navegador
      }); // Fin del evento end
      return; // Termina el manejo de cancelación
    } // Fin de POST /api/citas/:id/cancelar

    res.writeHead(404, { 'Content-Type': 'application/json; charset=utf-8' }); // Si piden una ruta desconocida, devuelve 404
    res.end(JSON.stringify({ exito: false, mensaje: 'Ruta no encontrada' })); // Cuerpo JSON de error 404
  }); // Cierra http.createServer
} // Fin de crearServidorHttpNativo

// ============================================================================
// PARTE 4: ARRANQUE AUTOMÁTICO EN PUERTO 3000 (Compatible con GitHub Codespaces)
// ============================================================================
if (require.main === module) { // Verifica si este archivo fue ejecutado directamente con 'node src/server.js' o 'npm start'
  const PORT = process.env.PORT || 3000; // Toma el puerto de entorno o usa por defecto el puerto 3000
  const HOST = '0.0.0.0'; // Escucha en 0.0.0.0 (obligatorio para que GitHub Codespaces reenvíe el puerto al navegador)

  try { // Intenta iniciar con Express primero
    const app = crearServidorExpress(); // Construye el servidor Express
    app.listen(PORT, HOST, () => { // Pone a escuchar Express en 0.0.0.0:3000
      console.log(`====================================================================`); // Línea decorativa en consola
      console.log(` Servidor MediQA (Express + SQLite) activo en http://localhost:${PORT}`); // Muestra URL local
      console.log(` En GitHub Codespaces: abre la pestaña 'PORTS' y haz clic en el globo del puerto ${PORT}`); // Guía Codespaces
      console.log(`====================================================================`); // Cierra cartel en consola
    }); // Fin de app.listen
  } catch (_err) { // Si no se ha corrido 'npm install', arranca automáticamente con el servidor HTTP nativo
    const server = crearServidorHttpNativo(); // Construye el servidor HTTP nativo de respaldo
    server.listen(PORT, HOST, () => { // Pone a escuchar el servidor nativo en 0.0.0.0:3000
      console.log(`====================================================================`); // Línea decorativa en consola
      console.log(` Servidor MediQA (Modo Nativo Instantáneo) activo en http://localhost:${PORT}`); // Muestra URL activa
      console.log(` En GitHub Codespaces: abre la pestaña 'PORTS' y haz clic en el globo del puerto ${PORT}`); // Guía Codespaces
      console.log(`====================================================================`); // Cierra cartel en consola
    }); // Fin de server.listen
  } // Fin del bloque try/catch de arranque
} // Fin de validación require.main === module

module.exports = { crearServidorExpress, crearServidorHttpNativo, procesarCreacionCita, procesarCancelacionCita }; // Exporta funciones
