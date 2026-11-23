# Informe de auditoría de liberación — ReservaLab

**Curso:** Aseguramiento de la Calidad de Software · Unidad 3 · Clase 16
**Grupo:** 2 · Equipo de 3
**Integrantes y roles:**
- _Completar_ — Usabilidad y accesibilidad (Estaciones A y B)
- Julián Garófalo — Confiabilidad / SRE (Estación C)
- _Completar_ — Seguridad (Estación D)

**Fuente de criterios:** `Clase16_Fundamentos_Teoricos.pdf` (las anclas §, T, F y R se refieren a ese documento).
**Entorno de pruebas de seguridad:** OWASP Juice Shop en `localhost:3000` (R8). No se escaneó ningún sistema de terceros.

---

## 1. Veredicto

> ## 🔴 NO-GO
>
> Según **R9**, la liberación no procede porque hay **5 hallazgos P1 abiertos** (T11):
> **SEC-01**, **SEC-02**, **A11Y-01**, **SRE-01** y **UX-02**.

**Justificación:** según los hallazgos P1 tenemos SEC-01, inyección SQL en `/labs` (Crítica); SEC-02, IDOR en reservas (Alta); A11Y-01, el botón "Reservar" no es operable con teclado y bloquea el flujo (2.1.1, nivel A); SRE-01, el presupuesto de error se consumió al 116.7 %, con el SLO incumplido y estado rojo según R5; y UX-02, la fecha se interpreta como mes/día sin aviso (severidad 4). Por el desempate de §5.1 (seguridad → accesibilidad A → confiabilidad → usabilidad), el primero en corregirse es **SEC-01**.

Antes de reevaluar la liberación, los hallazgos P2 (SEC-03, A11Y-02, SRE-02) deben tener responsable y fecha de corrección en un máximo de dos sprints (R9).

---

## 2. Top 5 de prioridades (§5.1, T11)

| # | ID | Hallazgo | Prioridad | Motivo del orden |
|---|----|----------|-----------|------------------|
| 1 | SEC-01 | Inyección SQL en `/labs` | P1 | Seguridad, Crítica |
| 2 | SEC-02 | IDOR en reservas | P1 | Seguridad, Alta |
| 3 | A11Y-01 | "Reservar" no operable con teclado | P1 | Accesibilidad nivel A |
| 4 | SRE-01 | Presupuesto de error al 116.7 % | P1 | Confiabilidad |
| 5 | UX-02 | Fecha interpretada como mes/día sin aviso | P1 | Usabilidad |

---

## 3. Resumen de hallazgos

| ID | Título | Severidad de origen | Prioridad (T11) |
|----|--------|---------------------|-----------------|
| SEC-01 | Inyección SQL en `/labs` | Crítica (T10) | **P1** |
| SEC-02 | IDOR en reservas | Alta (T10) | **P1** |
| A11Y-01 | Botón "Reservar" no operable con teclado | 2.1.1 · A, bloquea el flujo | **P1** |
| SRE-01 | Presupuesto de error consumido al 116.7 % | > 100 % · Rojo (R5) | **P1** |
| SRE-03 | El respaldo actual no cumple RPO/RTO | Sin estrategia de DR válida (R7) | **P1** |
| UX-02 | Fecha interpretada como mes/día sin aviso | 4 (T2, R1) | **P1** |
| SEC-03 | Cabeceras de seguridad ausentes | Media (T10) | P2 |
| A11Y-02 | Texto de ayuda con contraste 3.45:1 | 1.4.3 · AA | P2 |
| SRE-02 | λ(12) = 2.98 defectos/semana | No cumple R6 | P2 |
| UX-08 | Inicio saturado de avisos | 2 (T2) | P3 |
| SEC-04 | El servidor revela su versión de Apache | Baja (T10) | P4 |

> Los hallazgos UX, A11Y y SEC los documentaron los compañeros de esas estaciones. Cada uno debe completar la evidencia exacta (captura, petición HTTP, regla de axe, alerta de ZAP) en su sección.

---

## 4. Detalle de hallazgos

### 4.1 Seguridad (OWASP Top 10)

#### SEC-01 · Inyección SQL en `/labs`
| Campo | Contenido |
|---|---|
| Evidencia | Ítem E4 de la estación D. Con una comilla simple (`'`) el servidor responde 500 con un mensaje SQL, y con `' OR '1'='1` devuelve más filas. Captura: `evidencias/zap-sqli-labs.png` |
| Criterio violado | A03:2021 Injection (A05:2025) · CWE-89 |
| Severidad de origen | Crítica: explotable de forma remota y permite leer toda la base de datos |
| Prioridad unificada | **P1 · Bloqueante** |
| Recomendación | Usar consultas parametrizadas u ORM, validar con lista permitida, usar una cuenta de BD con privilegio mínimo y no mostrar errores internos. |
| Ancla | §4.4 · T9 · T10 · T11 |

#### SEC-02 · IDOR en reservas
| Campo | Contenido |
|---|---|
| Evidencia | Ítem E1 de la estación D. Con el token del usuario B, `GET /reservas/{id}` de una reserva de A responde 200 con datos ajenos. Prueba en Postman: `evidencias/postman-idor.png` |
| Criterio violado | A01:2021 Broken Access Control (A01:2025) · CWE-639 (API1 BOLA) |
| Severidad de origen | Alta: expone datos de otros usuarios con una cuenta normal |
| Prioridad unificada | **P1 · Bloqueante** |
| Recomendación | Verificar en el servidor que el recurso pertenece al usuario (`WHERE id = ? AND owner_id = usuario_actual`), denegar por defecto y añadir un test en Postman que espere 403/404. |
| Ancla | §4.2 · T9 · T10 · T11 |

#### SEC-03 · Cabeceras de seguridad ausentes
| Campo | Contenido |
|---|---|
| Evidencia | Ítem E5 de la estación D. El escaneo pasivo de ZAP reporta que faltan `Content-Security-Policy` y `X-Content-Type-Options`. Captura: `evidencias/zap-cabeceras.png` |
| Criterio violado | A05:2021 Security Misconfiguration (A02:2025) · CWE-693 |
| Severidad de origen | Media |
| Prioridad unificada | **P2 · Alta** |
| Recomendación | Configurar CSP, `X-Content-Type-Options: nosniff` y HSTS en el servidor o en el balanceador. |
| Ancla | T9 · T10 · T11 |

#### SEC-04 · El servidor revela su versión de Apache
| Campo | Contenido |
|---|---|
| Evidencia | Cabecera `Server: Apache/x.y.z` en las respuestas, alerta informativa/baja de ZAP |
| Criterio violado | A05:2021 Security Misconfiguration (A02:2025) · CWE-209/CWE-693 |
| Severidad de origen | Baja |
| Prioridad unificada | **P4 · Baja** |
| Recomendación | Usar `ServerTokens Prod` y `ServerSignature Off`. |
| Ancla | T9 · T10 · T11 |

### 4.2 Accesibilidad (WCAG 2.2 AA)

#### A11Y-01 · Botón "Reservar" no operable con teclado
| Campo | Contenido |
|---|---|
| Evidencia | Ítem S3 de la estación B. En el recorrido solo con teclado (R3) no se llega al botón ni se activa con Enter/Espacio. Captura de axe: `evidencias/axe-reservar.png` |
| Criterio violado | SC 2.1.1 Teclado · nivel A |
| Severidad de origen | Falla de nivel A que impide completar el flujo crítico |
| Prioridad unificada | **P1 · Bloqueante** |
| Recomendación | Usar `<button>` nativo, o bien `role="button"`, `tabindex="0"` y un manejador de Enter/Espacio. Verificar de nuevo con teclado. |
| Ancla | T4 · R2 · R3 · T11 |

#### A11Y-02 · Texto de ayuda con contraste 3.45:1
| Campo | Contenido |
|---|---|
| Evidencia | Ítem S2 de la estación B. Axe y Lighthouse reportan `color-contrast` con 3.45:1 en texto normal. Captura: `evidencias/lighthouse-contraste.png` |
| Criterio violado | SC 1.4.3 Contraste (mínimo) · nivel AA (requiere ≥ 4.5:1) |
| Severidad de origen | Falla de nivel AA |
| Prioridad unificada | **P2 · Alta** |
| Recomendación | Oscurecer el texto al menos a `#767676` sobre blanco (4.54:1). |
| Ancla | F1 · T5 · T11 |

### 4.3 Confiabilidad (SRE)

**Datos del piloto:** noviembre (30 días, T = 720 h). SLO = 99.5 % mensual. SLI = `/health` responde 200 en menos de 2 s.

| ID | Inicio | Descripción | Duración | ¿Cuenta? |
|----|--------|-------------|----------|----------|
| INC-01 | 02-nov 08:10 | Pool de conexiones de BD agotado | 42 min | Sí |
| INC-02 | 06-nov 13:25 | Certificado TLS del balanceador expirado | 18 min | Sí |
| MNT-01 | 09-nov 22:00 | Actualización del motor de BD (anunciada el 05-nov) | 60 min | **No** (anunciada con ≥ 48 h, R4) |
| INC-03 | 12-nov 10:05 | Despliegue defectuoso 1.4.0, rollback manual | 95 min | Sí |
| INC-04 | 17-nov 16:40 | Reinicio del contenedor por falta de memoria | 12 min | Sí |
| INC-05 | 21-nov 07:55 | Caída del proveedor de DNS | 30 min | Sí |
| INC-06 | 27-nov 11:30 | Fuga de memoria tras pico de reservas | 55 min | Sí |

**Métricas (F2–F5, R4):**

| Métrica | Cálculo | Resultado |
|---|---|---|
| n | INC-01 a INC-06 | **6** |
| D | 42+18+95+12+30+55 | **252 min = 4.2 h** |
| U | 720 − 4.2 | 715.8 h |
| MTTF | 715.8 / 6 | **119.3 h** |
| MTTR | 252 / 6 | **42 min** |
| MTBF | 720 / 6 | **120.0 h** |
| Disponibilidad A | 715.8 / 720 | **99.42 %** |
| R(24 h) | e^(−24/119.3) | **81.78 %** |
| R(168 h) | e^(−168/119.3) | **24.46 %** |

#### SRE-01 · Presupuesto de error consumido al 116.7 %
| Campo | Contenido |
|---|---|
| Evidencia | Registro de incidentes de noviembre: D = 252 min frente a un presupuesto de 216 min (0.5 % de 720 h). 252 / 216 × 100 = **116.67 %** |
| Criterio violado | SLO 99.5 % mensual (§3.3), política de presupuesto R5 |
| Severidad de origen | > 100 %, estado **Rojo** (SLO incumplido) |
| Prioridad unificada | **P1 · Bloqueante** |
| Recomendación | Congelar nuevas funcionalidades y hacer un postmortem obligatorio (R5). Bajar el MTTR automatizando el rollback (INC-03 sumó 95 min, el 38 % de D) y añadir alertas de certificados por vencer (INC-02). Ajustar el pool de conexiones y los límites de memoria (INC-01, INC-04, INC-06). |
| Ancla | §3.3 · T6 · R5 · T11 |

#### SRE-02 · Intensidad de fallas λ(12) = 2.98 defectos/semana
| Campo | Contenido |
|---|---|
| Evidencia | Modelo Goel–Okumoto con a = 120 y b = 0.15/semana, ajustado a 10 semanas de prueba (93 defectos acumulados). m(10) = 93.2 y quedan **26.8** defectos sin detectar. λ(12) = 120 · 0.15 · e^(−1.8) = **2.98** |
| Criterio violado | Criterio de liberación R6 (λ ≤ 1 defecto/semana) |
| Severidad de origen | No cumple R6. t* = ln(18)/0.15 = 19.3, por lo que se cumpliría desde la **semana 20** de prueba. Para detectar el 95 % hacen falta ≈ **20 semanas** (F10). |
| Prioridad unificada | **P2 · Alta** |
| Recomendación | Extender las pruebas de sistema hasta la semana 20, o liberar solo después de corregir los P1 y con monitoreo reforzado. Volver a estimar a y b con los datos nuevos. |
| Ancla | F8 · F9 · F10 · R6 · T11 |

#### SRE-03 · El respaldo actual no cumple RPO/RTO
| Campo | Contenido |
|---|---|
| Evidencia | Hoy solo hay un respaldo nocturno de la BD en el mismo servidor. El negocio exige **RPO ≤ 5 min** y **RTO ≤ 30 min**. |
| Criterio violado | §3.6: hay una sola copia, en un solo medio, sin copia fuera del sitio, con un RPO real de hasta 24 h, y nunca se ha probado una restauración (incumple la regla 3-2-1, y un plan de DR no probado no es válido). |
| Severidad de origen | RPO/RTO sin estrategia que los cumpla |
| Prioridad unificada | **P1 · Bloqueante** (T11) |
| Recomendación | Adoptar **espera en caliente (warm standby)**: RPO ≤ 1 min y RTO de 10–30 min, peor caso 30 min ≤ 30 min. Se descarta **luz piloto** (la opción inmediatamente más barata) porque su RPO de ≤ 15 min supera los 5 min y su RTO de 1–4 h supera los 30 min. Aplicar la regla 3-2-1 con una copia inmutable y hacer un simulacro de restauración antes de liberar. |
| Ancla | §3.6 · T7 · R7 · T11 |

### 4.4 Usabilidad (heurísticas de Nielsen)

#### UX-02 · Fecha interpretada como mes/día sin aviso
| Campo | Contenido |
|---|---|
| Evidencia | Observación O2: en el campo de texto libre, "03/12/2026" (3 de diciembre) se guarda como el 12 de marzo, y la confirmación no muestra la fecha. 3 de 5 participantes se enteraron días después. |
| Criterio violado | H5 Prevención de errores |
| Severidad de origen | 4: resultado erróneo silencioso (T2, R1) |
| Prioridad unificada | **P1 · Bloqueante** |
| Recomendación | Usar un selector de fecha (`<input type="date">`) y mostrar la fecha en formato largo ("miércoles 3 de diciembre de 2026") en la confirmación. |
| Ancla | T1 · T2 · R1 · T11 |

#### UX-08 · Inicio saturado de avisos
| Campo | Contenido |
|---|---|
| Evidencia | Observación O8: 6 avisos y 3 carruseles dejan el botón "Nueva reserva" bajo el pliegue en una portátil de 1366×768. Los participantes tardaron en promedio 14 s en encontrarlo. |
| Criterio violado | H8 Diseño estético y minimalista |
| Severidad de origen | 2: demora pasajera (T2, R1) |
| Prioridad unificada | **P3 · Media** |
| Recomendación | Subir "Nueva reserva" por encima del pliegue y agrupar los avisos en un solo bloque que se pueda colapsar. |
| Ancla | T1 · §1.4 · T11 |

---

## 5. Evidencias

Las capturas están en [`evidencias/`](evidencias/). Antes de subirlas se revisó que no muestren contraseñas, tokens, cookies ni datos personales.

| Archivo | Herramienta | Hallazgo |
|---|---|---|
| `axe-reservar.png` | axe DevTools | A11Y-01 |
| `lighthouse-contraste.png` | Lighthouse | A11Y-02 |
| `zap-sqli-labs.png` | OWASP ZAP | SEC-01 |
| `postman-idor.png` | Postman | SEC-02 |
| `zap-cabeceras.png` | OWASP ZAP | SEC-03 |
