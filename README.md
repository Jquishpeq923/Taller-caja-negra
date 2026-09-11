**Matriz de Evaluación: Proyecto de Automatización y Calidad**
### MATRIZ DE CRITERIOS Y DESCRIPTORES DE EVALUACIÓN

| Criterio de Evaluación | Excelente | Satisfactorio | En Desarrollo | Insuficiente |
|---|---|---|---|---|
| **C1: Demostración CI/CD**<br>(Puntaje Máximo: 2.5 pts) | **2.5 pts:** Muestran la pestaña Actions en vivo. Explican el YAML (.github/workflows/ci_pipeline.yml) y demuestran la corrida VERDE y ROJA por sabotaje. | **2.0 pts:** Muestran Actions y explican el YAML, pero la demostración del sabotaje (luz roja) es apresurada o poco clara. | **1.0 pt:** Muestran GitHub pero no explican el YAML o el pipeline falló por errores de sintaxis no resueltos. | **0.0 pts:** No presentan la integración con GitHub Actions ni el pipeline automatizado. |
| **C2: PyTest & Cobertura**<br>(Puntaje Máximo: 2.0 pts) | **2.0 pts:** Muestran y ejecutan PyTest. Las pruebas cubren casos felices, límites, división por cero e inputs negativos con 'assert' limpios. | **1.5 pts:** Ejecutan PyTest y tienen pruebas automatizadas, pero la cobertura de escenarios límite es parcial. | **0.8 pts:** Escribieron scripts de prueba pero presentan errores de ejecución en local o falta de aserciones formales. | **0.0 pts:** No desarrollaron scripts automatizados con PyTest. |
| **C3: Mapeo STLC & E/S**<br>(Puntaje Máximo: 2.0 pts) | **2.0 pts:** Relacionan las tareas del proyecto con las fases formales del STLC (ISTQB/ISO 29119). Justifican 2 Criterios de Entrada y 2 de Salida. | **1.5 pts:** Clasifican las tareas en el STLC, pero los Criterios de Entrada/Salida son genéricos o poco contextualizados. | **0.8 pts:** Confunden fases del STLC o no logran justificar la función de los Criterios de Entrada y Salida. | **0.0 pts:** Omite el marco conceptual del STLC y los criterios E/S. |
| **C4: Análisis Markdown**<br>(Puntaje Máximo: 1.5 pts) | **1.5 pts:** responde con rigor técnico las 3 preguntas (nube vs local, significado de luz verde e Exit Criteria bajo presión). | **1.0 pt:** Responden las 3 preguntas en el Markdown, pero con explicaciones breves o argumentos superficiales. | **0.5 pts:** Respuestas incompletas o erróneas en alguno de los dilemas conceptuales. | **0.0 pts:** No entregaron o dejaron en blanco la sección de análisis metacognitivo. |
| **C5: Exposición & Git**<br>(Puntaje Máximo: 2.0 pts) | **2.0 pts:** Exposición fluida y bien dividida por roles (Tester, Dev, Git Lead). Repositorio ordenado con carpeta '/semana-2/' y commits claros. | **1.5 pts:** Buena exposición oral y repositorio funcional, pero un integrante acapara la presentación o los commits son genéricos. | **0.8 pts:** Presentación desorganizada, mal manejo del tiempo o estructura deficiente en GitHub. | **0.0 pts:** No logran exponer el trabajo o el repositorio no es accesible. |

**C1: Demostración CI/CD y Pipeline Automatizado**
* El proyecto integra un pipeline de Integración Continua utilizando GitHub Actions.
* El archivo de configuración YAML (`.github/workflows/tests.yml`) orquesta la descarga del código, la instalación de dependencias y la ejecución de pruebas en un entorno virtual Ubuntu.
* **Ejecución Exitosa (Luz Verde):** Se demuestra cuando el código cumple con todas las reglas de negocio y PyTest finaliza sin errores.
* **Sabotaje Intencional (Luz Roja):** Se inyectó un error lógico en la calculadora cambiando la suma del total por una resta. El pipeline detectó el fallo matemático mediante un AssertionError, abortó el proceso y marcó una cruz roja, demostrando el bloqueo inmediato de código defectuoso.

**C2: Cobertura de Pruebas con PyTest**
* Se desarrolló un script estructurado de validación (`test_presupuesto.py`) para verificar exhaustivamente la lógica de negocio.
* **Casos Felices:** Validación de cálculos matemáticos con valores típicos y aserciones limpias.
* **Casos Límite:** Comprobación del comportamiento del sistema simulando periodos de inversión de cero meses.
* **Manejo de Excepciones:** Prueba estricta utilizando `pytest.raises` para atrapar errores críticos como la división por cero.
* **Inputs Negativos:** Inyección de presupuestos negativos simulando deudas para evitar cálculos financieros anómalos.

**C3: Mapeo STLC y Criterios de Entrada/Salida**
* **Fase de Planificación y Configuración (STLC):** Representada por el diseño de la receta YAML y la creación de la estructura de directorios para aislar las pruebas.
* **Fase de Ejecución (STLC):** Evidenciada por el proceso automatizado en la nube cada vez que un push dispara el comando de validación.
* **Criterios de Entrada:** 
  * El código fuente debe estar refactorizado, aislando la lógica matemática pura de las entradas de consola.
  * Las dependencias de validación deben estar declaradas explícitamente en el pipeline.
* **Criterios de Salida:**
  * El flujo de GitHub Actions debe finalizar con estado de éxito visual comprobable.
  * Todas las pruebas de casos límite, excepciones y valores negativos deben pasar sus aserciones lógicas sin fallos.

**C4: Análisis Técnico y Metacognitivo**
* **Pruebas en la nube vs entorno local:** Las pruebas locales dependen de la configuración específica de cada máquina, lo cual oculta problemas de compatibilidad. Las pruebas en la nube proporcionan un entorno virtual estandarizado e imparcial, garantizando que el código funcione universalmente.
* **Significado de la luz verde:** Actúa como un seguro anti regresiones. Confirma matemáticamente que los cambios recientes son correctos y asegura que las nuevas modificaciones no rompieron ninguna funcionalidad previa.
* **Consecuencias de ignorar Exit Criteria bajo presión:** Omitir los criterios de salida por urgencias de entrega introduce deuda técnica inmediata al proyecto. Forzar pases a producción saltándose estas métricas incrementa drásticamente el riesgo de que fallos críticos causen caídas del sistema para los usuarios finales.

**C5: Organización del Repositorio y Git**
* El repositorio refleja un historial de desarrollo estructurado mediante mensajes de commit descriptivos que documentan cada corrección y prueba.
