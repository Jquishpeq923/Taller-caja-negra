# MediQA — Sistema de Citas de Médico Familiar y Auditoría de Caja Blanca (McCabe & Cobertura)

Este repositorio contiene el desarrollo completo, las pruebas matemáticas de Caja Blanca, el análisis de Complejidad Ciclomática de McCabe y el laboratorio de pruebas de mutación correspondientes a la tarea **«Pruebas de Caja Blanca en la Era de la IA: Complejidad Ciclomática de McCabe y Cobertura de Código»**.

---

## 1. Guía Paso a Paso: Cómo Ejecutar y Ver la Web en GitHub Codespaces

Este proyecto incluye configuración oficial de **GitHub Codespaces** (`.devcontainer/devcontainer.json`) y un servidor de doble motor (**Express + HTTP Nativo en `0.0.0.0:3000`**) para garantizar que la aplicación web y las pruebas funcionen al primer intento.

### Paso 1.1: Subir el proyecto y abrir GitHub Codespaces
1. Descomprime el archivo `sistema_citas_medicas_qa.zip` en tu computadora.
2. Sube los archivos a tu repositorio de GitHub (o arrástralos directamente a la ventana de tu Codespace abierto).
3. En la página principal de tu repositorio en GitHub, haz clic en el botón verde **`<> Code`** → pestaña **`Codespaces`** → **`Create codespace on main`**.

### Paso 1.2: Levantar la aplicación web y verla en el navegador desde Codespaces
En la terminal inferior de GitHub Codespaces, ejecuta:

```bash
# 1. Instalar dependencias (Express, SQLite y Jest) - Opcional gracias al modo nativo incluido
npm install

# 2. Iniciar el servidor web en el puerto 3000
npm start
```

**¿Cómo ver la página web funcionando en GitHub Codespaces?**
1. Apenas ejecutes `npm start` (o `node src/server.js`), aparecerá en la esquina inferior derecha de Codespaces un aviso diciendo: **`Your application running on port 3000 is available`** con el botón **`Open in Browser`**. Haz clic en **`Open in Browser`**.
2. Si no ves el aviso emergente, mira el panel inferior (junto a la pestaña `TERMINAL`), haz clic en la pestaña **`PORTS`** (Puertos), busca el puerto **`3000`** y haz clic en el **ícono del planeta/globo (`Open in Browser`)** que aparece en la columna `Forwarded Address`.
3. Se abrirá una pestaña de tu navegador con la aplicación **MediQA** conectada en vivo al backend en el puerto `3000`.

### Paso 1.3: Ejecutar las 3 demostraciones de Auditoría QA en la terminal de Codespaces
Abre una segunda terminal en Codespaces (botón `+` en el panel de terminal) y ejecuta estos 3 comandos durante tu exposición:

1. **Suite Completa del Auditor QA (19 pruebas unitarias + 100% Cobertura de Líneas, Ramas y Funciones)**:
   ```bash
   npm run test:nativo
   # O con Jest (si ejecutaste npm install):
   npm test
   ```
2. **Demostración del «Falso 100% / Test Zombi» (Taller Paso 2 — Diapositiva 9)**:
   ```bash
   node --test --experimental-test-coverage tests/trampa_zombi.test.js
   ```
3. **Demostración de Pruebas de Mutación bajo el Modelo PIE (Diapositiva 12 — Pregunta 4)**:
   ```bash
   npm run test:mutacion
   ```

---

## 2. Resumen de Todo lo que Pide la Tarea (Diapositivas 1 a 13)

| Diapositiva del PDF | Qué pide la tarea | Cómo está resuelto en tu proyecto |
| :--- | :--- | :--- |
| **Diap. 2: El Diagnóstico** | Diferenciar al *Desarrollador Copiador* del *Ingeniero QA Auditor*. | Se compara la suite ingenua (`tests/trampa_zombi.test.js`) frente a la suite matemática (`tests/reglasCitas.test.js`). |
| **Diap. 3: El Prompt Maestro** | Crear app de citas médicas en **Node.js (Express), SQLite y Alpine.js** con 4 reglas críticas: Edad, Horarios, Choque de Citas y Penalización Tardía (`< 24h`). | Implementado en `src/reglasCitas.js`, `src/db.js`, `src/server.js` y `public/index.html`. |
| **Diap. 4 a 6: Caja Blanca, CFG y McCabe** | Extraer el Grafo de Flujo de Control (CFG) y calcular $V(G) = E - N + 2 = P + 1 = \text{Regiones}$. | Documentado en cada función de `src/reglasCitas.js` y graficado interactivamente en la Pestaña 2 de la web. |
| **Diap. 7: Jerarquía de Cobertura** | Superar la cobertura débil (Sentencias) hacia Cobertura de Ramas (Branch) y Condiciones (MC/DC). | Las 19 pruebas unitarias evalúan cada sub-condición booleana de forma independiente logrando 100% en ramas. |
| **Diap. 8: Taller Paso 1 (Cálculo Manual)** | Analizar `if (cancelacionTardia && !esEmergencia) { aplicarPenalizacion(); }` donde $P = 2 \Rightarrow V(G) = 3$. | Función `evaluarPenalizacionCancelacion` implementada y probada en sus 3 caminos independientes exactos. |
| **Diap. 9: Taller Paso 2 (Falso 100%)** | Demostrar los «Tests Zombis» que fallan en la etapa de **Detection** del **Modelo PIE**. | `tests/trampa_zombi.test.js` toca el 100% de las líneas con solo 2 pruebas sin verificar el monto ni el Camino 2. |
| **Diap. 10: Taller Paso 3 (Re-Prompting)** | Usar el Prompt del Auditor QA exigiendo $V(G)$ caminos y oráculos estrictos. | `tests/reglasCitas.test.js` verifica booleano, monto (`$25` vs `$0`), código y número de llamadas al espía (`mock`). |
| **Diap. 11: Framework QA Aumentado (Paso 5)** | Si una función generada por IA tiene $V(G) > 10$, ordenar refactorización antes de testear. | `src/codigoEspaguetiIA.js` muestra el código inicial con $V(G) = 15$ rechazado y dividido en las 4 funciones de `src/reglasCitas.js`. |
| **Diap. 12: Cuestionario de Auditoría** | Responder las 5 preguntas metacognitivas e inyectar mutaciones para probar los tests. | Script ejecutable `tests/mutacion_qa.js` (0/3 en Test Zombi vs. 3/3 en Suite Auditor QA). |

---

## 3. Explicación de Qué Hace Cada Parte y Comentarios Línea por Línea del Código

A continuación tienes la explicación detallada de cada archivo del proyecto con **un comentario al lado de cada línea de código** para que durante la exposición de mañana puedas explicar exactamente qué hace cualquier línea que te señale el profesor.

### 3.1 Archivo `src/reglasCitas.js` — Las 4 Reglas de Negocio Auditadas con McCabe

* **Qué hace la Parte 1 (`validarReglaEdad`)**: Evalúa si la edad está dentro del rango humano válido (`0` a `120` años). Si el paciente tiene menos de `18` años, lo clasifica como `PEDIATRICO`, exige que `tieneAcompaniante` sea `true` y le asigna una cita de `40 minutos`. Si tiene `18` o más años, lo clasifica como `ADULTO` y le asigna `30 minutos` sin exigir tutor. Tiene $P = 4$ sub-condiciones booleanas y $V(G) = 5$ caminos independientes.
* **Qué hace la Parte 2 (`validarHorarioOperativo`)**: Verifica que la cita no caiga en domingo (`0`) ni sábado (`6`), que la hora esté dentro de la jornada (`08:00` a `17:59`) y que no sea las `13:00` (bloqueada por almuerzo médico). Tiene $P = 5$ sub-condiciones y $V(G) = 6$ caminos independientes.
* **Qué hace la Parte 3 (`verificarChoqueCitas`)**: Recorre con un bucle `for` todas las citas de la base de datos; filtra las que no estén canceladas y pertenezcan al mismo médico y fecha, y aplica la desigualdad de intersección de intervalos `nuevaCita.inicioMin < cita.finMin && nuevaCita.finMin > cita.inicioMin` para impedir dobles reservas. Tiene $V(G) = 4$ caminos.
* **Qué hace la Parte 4 (`evaluarPenalizacionCancelacion` — Caso Estrella Diapositiva 8)**: Evalúa `if (cancelacionTardia && !esEmergencia)`. Tiene $P = 2$ predicados y $V(G) = P + 1 = 3$ caminos independientes. Solo cuando `cancelacionTardia` es `true` (`< 24h`) y `esEmergencia` es `false` (`!esEmergencia === true`), invoca `aplicarPenalizacion()` y aplica la multa de `$25`.

```javascript
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
```

---

### 3.2 Archivo `tests/reglasCitas.test.js` — Suite del Ingeniero QA Auditor (Diapositiva 10)

* **Qué hace este archivo**: Ejecuta las pruebas unitarias correspondientes a cada camino linealmente independiente $V(G)$ de las 4 reglas de negocio. A diferencia del Test Zombi, utiliza **oráculos estrictos (`assert.strictEqual`)** y un **espía (`spyPenalizacion`)** que cuenta cuántas veces se ejecutó `aplicarPenalizacion()`.

```javascript
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
}); // Fin de la suite de la Regla 4
```

---

### 3.3 Archivos `src/db.js` y `src/server.js` — Persistencia SQLite y Servidor Express/HTTP

* **Qué hace `src/db.js`**: Administra la tabla SQL `citas` en el archivo `citas_medicas.sqlite` (usando `better-sqlite3`), insertando automáticamente 2 citas semilla (`#101 Carlos Ruiz` a las `10:00` y `#102 Sofía Andrade` a las `11:00`). Si el usuario aún no ha corrido `npm install`, utiliza automáticamente su arreglo en memoria con los mismos métodos (`listarCitas()`, `obtenerCitaPorId()`, `insertarCita()` y `actualizarCancelacion()`).
* **Qué hace `src/server.js`**: Expone los endpoints `GET /api/citas`, `POST /api/citas` (ejecutando en cascada las Reglas 1, 2 y 3 antes de guardar en SQLite) y `POST /api/citas/:id/cancelar` (ejecutando la Regla 4 de penalización tardía `< 24h`), además de servir la interfaz web `public/index.html` en `0.0.0.0:3000` para **GitHub Codespaces**.
