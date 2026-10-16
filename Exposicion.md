# Guía Explicativa Integral: Taller Práctico de Pruebas de Software (PICT + pytest / Pasarela PagoYa)

---

## 1. ¿De qué trata esta tarea en general?
La idea principal de esta práctica es **automatizar y optimizar el aseguramiento de la calidad del software (QA)** para un sistema real de pagos en línea (*PagoYa*). 

En el desarrollo de software profesional, probar todas las combinaciones posibles de un sistema a mano es una misión imposible. Por ello, en esta tarea dejamos de probar a ciegas y aplicamos **ingeniería combinatoria y automatización** para:
1. Diseñar un modelo matemático de pruebas inteligente.
2. Generar matrices de prueba reducidas pero altamente efectivas.
3. Construir un "robot probador" con `pytest` que ejecute los casos automáticamente, cace errores ocultos en el código y valide que todo funcione en verde.

---

## 2. ¿Qué es el *Pairwise Testing* y cómo lo usamos?
El ***Pairwise Testing*** (o pruebas por pares / cobertura de 2 vías) es una técnica matemática basada en el Diseño de Experimentos (DoE) y los arreglos ortogonales.

* **¿Cómo funciona?** Garantiza que **todas las parejas posibles de valores entre dos parámetros cualesquiera** aparezcan juntas en al menos un caso de prueba.
* **¿Por qué lo usamos?** Los estudios estadísticos del NIST (*National Institute of Standards and Technology*) demuestran que **entre el 70% y el 90% de los errores de software ocurren por la interacción de solo uno o dos factores**. 
* **El impacto en PagoYa:** Si hubiéramos querido probar la pasarela de forma exhaustiva combinando sus 7 parámetros, habríamos obtenido **5,184 combinaciones imposibles de cubrir a mano**. Al aplicar *pairwise* ($t=2$), logramos comprimir todo ese universo a **tan solo 22 pruebas**, atrapando la gran mayoría de los fallos invirtiendo un 95% menos de tiempo y recursos.

---

## 3. ¿Por qué usamos $t=2$ y en qué casos se usa $t=3$?
La letra $t$ representa el **nivel de interacción o fuerza de cobertura** (*strength*):

* **¿Por qué se usa $t=2$ (*pairwise*)?** 
  Se usa como el estándar de la industria para la gran mayoría de los sistemas porque ofrece la mejor relación costo-beneficio. Como se mencionó antes, la mayoría de los bugs nacen del choque entre dos variables. Con $t=2$ obtenemos una suite de pruebas pequeña, rápida y altamente detectora de errores (en PagoYa pasamos de 5,184 a **22 pruebas**).
* **¿En qué casos se usa $t=3$ (*3-way* / tríos)?** 
  Se utiliza en **sistemas de misión crítica o de alta complejidad** (por ejemplo, software médico, aeroespacial, sistemas financieros estrictos o pasarelas de pago globales) donde un fallo puede costar millones. Se emplea cuando sabemos que un error crítico solo se desencadena si **tres factores específicos colisionan al mismo tiempo**. El costo de usar $t=3$ es que la suite de pruebas crece notablemente (en PagoYa subió de 22 a **79 pruebas**), por lo que requiere más tiempo de ejecución a cambio de una seguridad casi total.

---

## 4. ¿Qué es la granularidad y cómo se aplica en esta tarea?
La **granularidad** define el nivel de detalle o desglose con el que se estudian los elementos de un sistema.

* **¿Cómo se aplica?** En lugar de tratar la pasarela de pagos como un bloque genérico o una única variable global, aplicamos una *granularidad fina* dividiendo el problema en **7 parámetros independientes** con valores muy bien acotados (dispositivo, país, método, moneda, envío, cupón y monto).
* **¿Por qué se hace así?** 
  * Si la granularidad fuera muy gruesa, el sistema pasaría por alto combinaciones críticas de negocio y el robot probador no podría detectar fallos específicos.
  * Si fuera excesivamente fina (por ejemplo, añadiendo 50 países o montos al centavo), caeríamos en la **explosión combinatoria**, volviendo el software imposible de probar.
* **Lista de valores (Granularidad del modelo PagoYa):**
  1. **Dispositivo:** `Celular`, `Laptop`, `Tablet` (3 valores)
  2. **País:** `Ecuador`, `Colombia`, `Peru`, `Otro` (4 valores)
  3. **Método de pago:** `Tarjeta`, `Billetera`, `Transferencia`, `Contraentrega` (4 valores)
  4. **Moneda:** `USD`, `COP`, `PEN` (3 valores)
  5. **Envío:** `Estandar`, `Express`, `Retiro` (3 valores)
  6. **Cupón:** `Ninguno`, `Bienvenida`, `Estudiante` (3 valores)
  7. **Monto:** `10`, `50`, `500`, `2000` (4 valores numéricos discretos)

---

## 5. Explicación detallada de los archivos del proyecto
El taller se compone de 5 archivos clave que simulan el ciclo de vida de un equipo profesional de QA:

1. **`checkout.pict` (El Modelo):** 
   Escribe las reglas de negocio de la tienda (ej. que la moneda coincida con el país, restricciones de montos, envíos, etc.) usando formato condicional `IF/THEN`. Funciona como el "plano matemático" que el motor de PICT utiliza para saber qué combinaciones son válidas y cuáles prohibir.
2. **`checkout_t2.tsv` y `checkout_t3.tsv` (Las Tablas de Pruebas):** 
   Son archivos tabulares generados automáticamente por PICT a partir del modelo. Contienen los casos de prueba optimizados (22 filas para $t=2$ y 79 filas para $t=3$). Cada fila es un caso de prueba independiente con datos reales listos para ejecutarse.
3. **`pasarela.py` (El Sistema bajo prueba):** 
   Es el código fuente principal de la tienda. Recibe los datos de una transacción, valida las reglas del negocio (si se incumplen, devuelve `"RECHAZADO"`) y calcula matemáticamente el precio final aplicando descuentos y envíos. *(Este archivo contenía el bug original del cupón Bienvenida con envío Express que anulaba el descuento).*
4. **`test_pasarela.py` (El Robot Probador con `pytest`):** 
   Una suite de pruebas automatizadas que actúa como un oráculo independiente. Lee los archivos `.tsv`, inyecta los datos en `pasarela.py` y verifica mediante aserciones (`assert`) si los totales calculados son correctos, ejecutando cientos de validaciones en segundos y asegurando que todo quede en verde (`PASSED`).
5. **`README.md` (La Documentación):** 
   El informe en Markdown para el repositorio de GitHub que documenta los integrantes, el resumen del modelo, la tabla oficial de resultados y el error que se cazó y corrigió.

---

## 6. ¿Cómo funcionan las dos herramientas de Pairwise en línea?
Durante la investigación y desarrollo de la tarea, exploramos dos enfoques web para generar combinaciones:

* **Pairwise Pict Online (`https://pairwise.yuuniworks.com/`):**
  Funciona mediante **texto plano y código**. En su panel izquierdo pegas el modelo estructurado siguiendo la sintaxis oficial de Microsoft PICT (parámetros arriba y reglas `IF/THEN` abajo). Al hacer clic en *Generate*, el motor procesa el texto en la nube y arroja la matriz tabular en el panel derecho, advirtiendo al instante si hay errores de sintaxis (como el manejo de comillas).
* **Pairwise Online Tool (`https://pairwise.teremokgames.com/`):**
  Funciona mediante una **interfaz gráfica visual (GUI)** tipo hoja de cálculo. Los parámetros se configuran visualmente como columnas y los valores como filas. Para las reglas de negocio, en lugar de escribir código, utilizas un panel interactivo (*Edit Conditions*) con menús desplegables en lenguaje natural (ej. seleccionar que un valor `cannot exist with` otro), generando la tabla optimizada de forma totalmente visual.

---