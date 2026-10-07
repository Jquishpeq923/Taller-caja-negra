# Taller de modelado combinatorio y All-Pairs

**Universidad Internacional del Ecuador — Facultad de Ciencias de la Computación**
**Análisis de pruebas para el configurador SmartDrive Motors**

**Integrantes**

- Andrés Quisilema
- Julián Garofalo
- José Quishpe

---

## 1. Contexto y modelo de entrada

Diseñamos una suite de pruebas para el configurador web de SmartDrive Motors. El objetivo es cubrir las interacciones entre sus opciones con menos casos y respetar las reglas de compatibilidad.

Obtuvimos **11 casos** para el modelo sin restricciones y **12** para el modelo restringido; ambos cubren el **100 %** de los pares que les corresponden. Verificamos el diseño del modelo, pero no ejecutamos pruebas sobre una aplicación real.

### 1.1 Organización del equipo

| Integrante | Rol | Responsabilidad |
|---|---|---|
| Andrés Quisilema | Analista de modelado | Definir parámetros y restricciones. |
| Julián Garofalo | Tester combinatorio | Generar casos y comprobar cobertura. |
| José Quishpe | Documentador y Git Lead | Documentar resultados y gestionar la entrega. |

### 1.2 Parámetros y valores

| ID | Parámetro | Valores | Cantidad |
|---|---|---|---|
| P1 | Motor | Gasolina, Híbrido, Eléctrico | 3 |
| P2 | Transmisión | Manual, Automática, Monomarcha | 3 |
| P3 | Frenos | Estándar, ABS, Regenerativo | 3 |
| P4 | Mercado | América, Europa, Asia | 3 |
| P5 | Modo | Eco, Sport, Autónomo | 3 |

La página 4 del taller mezcla nombres y valores y repite P5. Adoptamos los cinco parámetros de la tabla de la página 9 (Taller autónomo, s. f., pp. 4, 9). **Asia** es un supuesto del equipo para completar Mercado, ya que el PDF solo muestra América y Europa. Las reglas utilizadas pertenecen al ejercicio, no a especificaciones universales de vehículos.

### 1.3 Explosión combinatoria

Con cinco parámetros de tres valores, el total sin restricciones es:

```
3 × 3 × 3 × 3 × 3 = 243 configuraciones
```

A 15 minutos por prueba, necesitaríamos `243 × 15 = 3 645 minutos`, equivalentes a **60 horas y 45 minutos** o aproximadamente **7,6 jornadas de ocho horas**, sin contar preparación ni correcciones.

Probar todo consume demasiadas horas para el tiempo disponible del taller. No podemos calcular un costo monetario sin una tarifa por hora; la carga de trabajo sí permite justificar la reducción.

---

## 2. Suite All-Pairs sin restricciones

All-Pairs, con fuerza **t = 2**, exige que cada combinación de valores de dos parámetros aparezca al menos una vez. Hay 10 parejas de parámetros y 9 combinaciones de valores por pareja: `10 × 9 = 90 pares`. La siguiente tabla cubre los 90 con 11 casos.

| Caso | Motor | Transmisión | Frenos | Mercado | Modo |
|---|---|---|---|---|---|
| AP-01 | Gasolina | Monomarcha | Estándar | Asia | Eco |
| AP-02 | Eléctrico | Manual | ABS | Asia | Autónomo |
| AP-03 | Híbrido | Monomarcha | Regenerativo | Europa | Autónomo |
| AP-04 | Gasolina | Automática | ABS | Europa | Sport |
| AP-05 | Eléctrico | Automática | Regenerativo | América | Eco |
| AP-06 | Híbrido | Manual | Estándar | América | Sport |
| AP-07 | Eléctrico | Manual | Estándar | Europa | Eco |
| AP-08 | Eléctrico | Monomarcha | Regenerativo | Asia | Sport |
| AP-09 | Gasolina | Manual | Regenerativo | América | Autónomo |
| AP-10 | Híbrido | Monomarcha | ABS | América | Eco |
| AP-11 | Híbrido | Automática | Estándar | Asia | Autónomo |

Esta matriz representa el modelo matemático puro. Incluye configuraciones incompatibles con las reglas que aplicamos después; por eso todavía no es una suite de configuraciones aceptables para el negocio.

### 2.1 Generación y comprobación de los casos

Enumeramos las 243 configuraciones y calculamos los pares que cubre cada una. Luego elegimos, en cada paso, una configuración que cubriera la mayor cantidad de pares pendientes. Repetimos la selección con 150 semillas de desempate, de 0 a 149, y conservamos la suite más corta encontrada. La comprobación posterior compara los pares de la suite con el universo completo.

Utilizamos una heurística de cobertura voraz implementada en Python. No afirmamos que 11 sea el mínimo absoluto: el resultado comprobado es su cobertura completa. Tampoco se requiere que todos los pares se repitan el mismo número de veces; la condición es que aparezcan al menos una vez.

### 2.2 Reducción y tiempo de ejecución

```
Reducción = (243 − 11) / 243 × 100 = 95,47 %
```

Ejecutar estos 11 casos tomaría **165 minutos**, es decir, **2 horas y 45 minutos**. El PDF estima unos 15–20 casos, pero esa cifra es orientativa; el criterio decisivo es la cobertura.

---

## 3. Restricciones y suite válida

Formalizamos tres restricciones a partir de las dos reglas compuestas de la página 11. Separamos las dos obligaciones del motor eléctrico para que cada restricción pueda comprobarse por sí sola.

| ID | Restricción |
|---|---|
| R1 | `IF Motor = Eléctrico THEN Transmisión = Monomarcha` |
| R2 | `IF Motor = Eléctrico THEN Frenos = Regenerativo` |
| R3 | `IF Motor = Gasolina THEN Frenos != Regenerativo` |

No añadimos restricciones sobre Mercado o Modo porque el taller no las define. Al aplicar R1, R2 y R3 quedan **144 configuraciones válidas**:

- 54 de gasolina (3 × 2 × 3 × 3)
- 81 híbridas (3 × 3 × 3 × 3)
- 9 eléctricas (1 × 1 × 3 × 3)

Las otras 99 se excluyen de la suite positiva.

### 3.1 Casos válidos

Volvimos a generar la suite usando únicamente configuraciones válidas. El resultado tiene **12 casos** y cubre los **85 pares factibles** del modelo, sin violar ninguna regla.

| Caso | Motor | Transmisión | Frenos | Mercado | Modo |
|---|---|---|---|---|---|
| TC-01 | Híbrido | Manual | ABS | América | Autónomo |
| TC-02 | Eléctrico | Monomarcha | Regenerativo | Europa | Sport |
| TC-03 | Gasolina | Automática | ABS | Asia | Eco |
| TC-04 | Híbrido | Monomarcha | Estándar | Asia | Sport |
| TC-05 | Gasolina | Automática | Estándar | Europa | Autónomo |
| TC-06 | Híbrido | Manual | Regenerativo | Europa | Eco |
| TC-07 | Gasolina | Monomarcha | Estándar | América | Eco |
| TC-08 | Híbrido | Automática | Regenerativo | América | Sport |
| TC-09 | Eléctrico | Monomarcha | Regenerativo | Asia | Autónomo |
| TC-10 | Gasolina | Manual | Estándar | Asia | Sport |
| TC-11 | Gasolina | Monomarcha | ABS | Europa | Sport |
| TC-12 | Eléctrico | Monomarcha | Regenerativo | América | Eco |

### 3.2 Resultados y tiempo de ejecución

Para todos los casos TC, el resultado esperado es que el configurador **acepte** la selección según estas tres reglas. Esto define el resultado esperado del modelo; aún debe compararse con el comportamiento de la aplicación.

La suite toma **180 minutos**, equivalentes a **3 horas**. Reduce el conjunto válido de 144 a 12 casos en un **91,67 %**, y el universo inicial de 243 en un **95,06 %**. Una heurística puede producir más casos al agregar restricciones; no basta con borrar filas de la primera tabla.

---

## 4. Verificación del diseño de pruebas

Un par es **factible** cuando existe al menos una configuración completa que lo contiene y cumple todas las restricciones. Construimos ese universo a partir de las 144 configuraciones válidas y lo comparamos con los pares presentes en los 12 casos TC.

| Pareja de parámetros | Pares factibles | Pares cubiertos |
|---|---|---|
| Motor y Transmisión | 7 | 7 |
| Motor y Frenos | 6 | 6 |
| Motor y Mercado | 9 | 9 |
| Motor y Modo | 9 | 9 |
| Transmisión y Frenos | 9 | 9 |
| Transmisión y Mercado | 9 | 9 |
| Transmisión y Modo | 9 | 9 |
| Frenos y Mercado | 9 | 9 |
| Frenos y Modo | 9 | 9 |
| Mercado y Modo | 9 | 9 |
| **Total** | **85** | **85** |

Los cinco pares imposibles son Eléctrico–Manual, Eléctrico–Automática, Eléctrico–Estándar, Eléctrico–ABS y Gasolina–Regenerativo. Por eso Motor–Transmisión tiene 7 pares factibles y Motor–Frenos tiene 6; las otras ocho parejas conservan sus 9 combinaciones.

```
Cobertura final = 85 / 85 × 100 = 100 %
```

También comprobamos que las 12 filas cumplen R1, R2 y R3, que no hay casos duplicados y que todos los valores pertenecen a la matriz definida. La suite sin restricciones cubre 90 / 90 pares.

### 4.1 Evidencias de cobertura

En Mercado–Modo aparecen las nueve combinaciones: América con Eco, Sport y Autónomo; Europa con Eco, Sport y Autónomo; y Asia con Eco, Sport y Autónomo. En Motor–Frenos aparecen las seis combinaciones permitidas y ninguna de las tres prohibidas.

### 4.2 Autoevaluación antes de la entrega

- [x] La matriz contiene los cinco parámetros y sus valores.
- [x] El cálculo de 243 casos y su tiempo está justificado.
- [x] Ambas suites están listadas y su cobertura está comprobada.
- [x] Las tres restricciones reflejan las reglas del ejercicio.

Estas verificaciones confirman el diseño de pruebas, no la ausencia de defectos en un sistema real.

---

## 5. Reflexión y entrega

### 5.1 Riesgo de ignorar las restricciones

Si enviamos la matriz pura a QA sin revisar las restricciones, podemos automatizar configuraciones que el negocio no permite, como gasolina con frenos regenerativos. Eso desperdicia horas de desarrollo y ejecución, provoca retrabajo y puede confundir un rechazo correcto con un defecto. Si además el configurador acepta esas combinaciones, podríamos enviar pedidos incompatibles a fabricación.

Primero debemos separar las configuraciones válidas; los casos inválidos pueden usarse en pruebas negativas, cuyo resultado esperado sea el rechazo.

### 5.2 Ventajas de All-Pairs frente a la intuición

All-Pairs aporta una cobertura medible: podemos demostrar que todos los pares factibles están presentes, mientras que 20 casos elegidos solo por intuición pueden repetir combinaciones y omitir otras. Muchos fallos dependen de pocas variables, lo que respalda las pruebas combinatorias (National Institute of Standards and Technology [NIST], 2026).

Aun así, cubrir pares no garantiza detectar todos los defectos: algunos requieren tres o más condiciones. Por eso usamos All-Pairs como base y añadimos pruebas según los riesgos del sistema, con revisión humana de parámetros, restricciones y resultados esperados.

### 5.3 Resumen

Modelamos cinco parámetros con tres valores cada uno. El universo inicial contiene 243 configuraciones y requeriría 60 horas y 45 minutos de pruebas. Generamos una matriz pura de 11 casos con cobertura de 90 pares; después aplicamos tres restricciones y construimos una suite válida de 12 casos que cubre los 85 pares factibles. Esta última tarda 3 horas y reduce en 91,67 % las pruebas frente a las 144 configuraciones válidas. La cobertura está comprobada sobre el modelo; la ejecución sobre el configurador queda pendiente.

---

## Referencias

National Institute of Standards and Technology. (2026, 8 de junio). *Combinatorial methods for trust and assurance*. https://csrc.nist.gov/projects/automated-combinatorial-testing-for-software/combinatorial-methods-in-testing/interactions-involved-in-software-failures

*Taller autónomo: Modelado combinatorio y All-Pairs* [Material de clase, Clase 09, Bloque 2]. (s. f.).
