**Matriz de Evaluación: Proyecto de Automatización y Calidad**

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
