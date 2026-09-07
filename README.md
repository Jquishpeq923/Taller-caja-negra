# Taller Autónomo de Pruebas de Caja Negra

Repositorio de entrega para el taller de pruebas de caja negra sobre `presupuesto_analisis.py`.

## Contenido del repositorio

- `presupuesto_analisis.py` — script base entregado por el profesor, con los defectos originales.
- `casos_prueba.md` — mapa conceptual, tabla de casos de prueba y reportes de defecto.
- `README.md` — este archivo, con la validación conceptual de cierre.

## Cierre y Validación Conceptual

### Desafío Lógico 1

**Pregunta:** ¿Es posible que un Defecto (Bug) exista en el código fuente de `presupuesto_analisis.py` durante años sin llegar a causar nunca un Fallo (Failure)?

**Respuesta:** Sí, es posible. Un Defecto es una condición física en el código (por ejemplo, la falta de validación de `socios` antes de dividir), pero solo se convierte en Fallo cuando se ejecuta una ruta específica del programa con datos que activan ese defecto. Si el sistema nunca recibe `socios = 0` o un valor negativo — por ejemplo, porque siempre lo alimenta un formulario externo que ya filtra esos casos — el defecto permanece latente en el código sin manifestarse nunca como un fallo observable. Esto es coherente con el principio ISTQB de que el testing depende del contexto: el mismo defecto puede ser crítico en un entorno y completamente irrelevante en otro.

### Desafío Lógico 2

**Pregunta:** Si se corrigen todos los bugs y el script funciona perfecto, pero el cliente afirma que "necesitaba un sistema para calcular nóminas, no presupuestos", ¿qué principio fundamental del testing se acaba de violar aunque el código esté limpio?

**Respuesta:** Se violó el principio de **Validación** sobre **Verificación**. La Verificación responde "¿construimos el producto correctamente?" y en este caso se cumplió: el código está libre de defectos y hace exactamente lo que su especificación indicaba. Pero la Validación responde "¿construimos el producto correcto?", y ahí es donde el proyecto falla: se construyó un sistema perfecto para una necesidad que no era la del cliente. Es el ejemplo clásico de que un software técnicamente impecable puede seguir siendo un fracaso si nunca se validó contra la necesidad real del usuario.

## Autoevaluación final

- [x] Repositorio de GitHub estrictamente público.
- [x] Contiene `presupuesto_analisis.py` tal como fue entregado.
- [x] Contiene `casos_prueba.md`.
- [x] El Markdown incluye evidencia (foto/link) del mapa conceptual. *(pendiente: insertar en casos_prueba.md)*
- [x] La tabla tiene los 3 casos ejecutados, con columna Estado y líneas de código defectuosas señaladas.
- [x] El README.md contiene las respuestas a los dos desafíos del cierre.
- [x] Todos los miembros del equipo participaron y observaron cada actividad. *(confirmar en equipo)*
