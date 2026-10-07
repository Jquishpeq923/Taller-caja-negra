# Taller Autónomo: Modelado Combinatorio y All-Pairs

**Sistema:** Configurador SmartDrive Motors



---



## 1. Matriz de Parámetros y Valores (P&V)



| ID | Parámetro (P) | Valores Posibles (V) | n (Cantidad) |

| :---: | :--- | :--- | :---: |

| **P1** | Motor | Gasolina, Híbrido, Eléctrico | 3 |

| **P2** | Transmisión | Manual, Automática, Monomarcha | 3 |

| **P3** | Frenos | Estándar, ABS, Regenerativo | 3 |

| **P4** | Mercado | América, Europa, Asia | 3 |

| **P5** | Modo Conducción | Eco, Sport, Autónomo | 3 |



---



## 2. Peligro de la Explosión Combinatoria



### Cálculo Matemático:

$$Total = v_1 \times v_2 \times v_3 \times v_4 \times v_5 = 3^5 = 243 \text{ combinaciones puras}$$



### Estimación de Tiempo de Ejecución:

* **Tiempo por prueba individual:** 15 minutos.

* **Tiempo total requerido:** $243 \times 15 \text{ min} = 3645 \text{ minutos}$ (**60.75 horas laborales continuas de pruebas**).



### Justificación de Inviabilidad Financiera:

Ejecutar 243 casos de prueba de forma exhaustiva demandaría más de 60 horas de trabajo exclusivo de ingeniería de QA para una única versión del configurador. Esto incrementa los costos operativos, retrasa el lanzamiento comercial (*time-to-market*) y consume presupuesto crítico en combinaciones redundantes. La prueba exhaustiva resulta financieramente insostenible para el proyecto.



---



## 3. Suite Reducida All-Pairs (t = 2)



Filtro ortogonal aplicado para garantizar cobertura del 100% de combinaciones binarias cruzadas entre parámetros.



| Test Case | Motor | Transmisión | Frenos | Mercado | Modo Conducción |

| :---: | :--- | :--- | :--- | :--- | :--- |

| **TC-01** | Gasolina | Manual | Estándar | América | Eco |

| **TC-02** | Gasolina | Automática | ABS | Europa | Sport |

| **TC-03** | Gasolina | Monomarcha | Regenerativo | Asia | Autónomo |

| **TC-04** | Híbrido | Manual | ABS | Asia | Sport |

| **TC-05** | Híbrido | Automática | Regenerativo | América | Autónomo |

| **TC-06** | Híbrido | Monomarcha | Estándar | Europa | Eco |

| **TC-07** | Eléctrico | Manual | Regenerativo | Europa | Autónomo |

| **TC-08** | Eléctrico | Automática | Estándar | Asia | Eco |

| **TC-09** | Eléctrico | Monomarcha | ABS | América | Sport |

| **TC-10** | Gasolina | Manual | Regenerativo | Europa | Sport |

| **TC-11** | Gasolina | Automática | Estándar | Asia | Autónomo |

| **TC-12** | Gasolina | Monomarcha | ABS | América | Eco |

| **TC-13** | Híbrido | Manual | Estándar | Asia | Autónomo |

| **TC-14** | Híbrido | Automática | ABS | América | Eco |

| **TC-15** | Híbrido | Monomarcha | Regenerativo | Europa | Sport |

| **TC-16** | Eléctrico | Manual | ABS | América | Eco |

| **TC-17** | Eléctrico | Automática | Regenerativo | Europa | Sport |

| **TC-18** | Eléctrico | Monomarcha | Estándar | Asia | Autónomo |



---



## 4. Identificación de Restricciones Lógicas (Constraints)



```pseudocode

// REGLA 1: Exigencias mecánicas y motrices de tren eléctrico

IF Motor == "Eléctrico"

THEN Transmisión == "Monomarcha" AND Frenos == "Regenerativo";



// REGLA 2: Incompatibilidad de regeneración en combustión pura

IF Motor == "Gasolina"

THEN Frenos != "Regenerativo";



// REGLA 3: Requerimientos de frenado asistido para conducción autónoma

IF Modo_Conducción == "Autónomo"

THEN Frenos == "ABS" OR Frenos == "Regenerativo";





---

### 5. Cierre Cognitivo (Preguntas de Transferencia)



### Pregunta 1 (Transferencia)

**¿Qué riesgo financiero y operativo corremos si ignoramos los constraints lógicos y enviamos la matriz matemática pura directamente al equipo de automatización (QA)?**



Si se envía la matriz matemática ortogonal pura sin filtrar las restricciones físicas de ensamblaje, el equipo de automatización ejecutará scripts que intentarán configurar vehículos inviables (como acoplar un motor a gasolina con frenos regenerativos puros). Esto detonará una avalancha de falsos positivos en el pipeline de CI/CD, obligando a los ingenieros a gastar decenas de horas de triage depurando supuestos errores que en realidad son incompatibilidades lógicas conocidas. A nivel financiero, esto incrementa el costo por ciclo de pruebas, retrasa el lanzamiento comercial (*time-to-market*) y, en el peor escenario operativo, arriesga que una orden con fallo de manufactura llegue a la línea física de ensamblaje provocando detenciones de planta millonarias.



### Pregunta 2 (Elaboración)

**¿Por qué la técnica All-Pairs es matemáticamente y empíricamente superior a que un tester diseñe 20 casos de prueba basándose únicamente en su intuición?**



La técnica All-Pairs está respaldada por estudios empíricos del NIST que demuestran que la inmensa mayoría de las fallas críticas de software son activadas por la interacción de solo uno o dos parámetros concurrentes ($t=2$). Un tester que diseña pruebas mediante intuición tiende a concentrarse en combinaciones comunes, felices o casos borde obvios, omitiendo sin saberlo cruces binarios periféricos de alto riesgo. All-Pairs reemplaza el sesgo humano por una garantía matemática determinista de cobertura ortogonal: asegura que cada posible combinación de dos variables se evalúe al menos una vez en el menor número de pruebas posibles, logrando una eficiencia de detección de defectos que la selección empírica o intuitiva no puede garantizar.

