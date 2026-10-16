# Taller Práctico 4: PICT + pytest (PagoYa)

**Integrantes:** Julian Garofalo, José Quishpe

## Resumen del modelo
Modelamos el checkout de la pasarela de pagos PagoYa con 7 parámetros: Dispositivo, Pais, Metodo, Moneda, Envio, Cupon y Monto. Aplicamos seis reglas de negocio como restricciones IF/THEN: la moneda depende del país (R1), Contraentrega solo en Ecuador o Colombia y hasta 500 (R2), Billetera solo en Celular o Tablet (R3), sin Express para el país Otro (R4), el cupón Bienvenida exige monto de al menos 50 (R5) y con monto 2000 solo Tarjeta o Transferencia (R6). PICT generó covering arrays de orden 2 y 3 que pytest ejecuta automáticamente.

## Resultados
| Medida | Valor |
|---|---|
| Combinaciones exhaustivas sin restricciones | 3 × 4 × 4 × 3 × 3 × 3 × 4 = 5184 |
| Combinaciones válidas (/o:max) | 1046 |
| Pruebas con t=2 | 22 |
| Pruebas con t=3 | 79 |
| Fallos antes de corregir (t=2 / t=3) | 2 / 6 |
| Suite final (con tests propios) | 446 pruebas, todas en verde |

## 1. ¿De qué trata la tarea?

**PagoYa** es una pasarela de pagos. Recibe un pedido, aplica cupones, calcula el envío y devuelve el total a cobrar. Tiene muchas variables (país, moneda, método de pago, cupón, tipo de envío, monto…) y cada una tiene varios valores posibles.

Si las combinamos **todas contra todas**, salen **5184 combinaciones**, y probarlas a mano es impracticable.

**Objetivo de la tarea:**

1. **Modelar** las reglas del negocio de la pasarela.
2. **Generar automáticamente** un conjunto mínimo de casos de prueba.
3. **Ejecutarlos con un robot** (pytest) contra el sistema.
4. **Encontrar y corregir errores** reales del código.

---

## 2. ¿Qué es Pairwise?

**Pairwise** (pruebas por pares) garantiza que **cada pareja posible de valores entre dos variables aparezca junta en al menos un caso de prueba**.

### Ejemplo sencillo

| Variable | Valores |
|---|---|
| Método | Tarjeta, PayPal |
| Envío | Normal, Express |
| Cupón | Ninguno, Bienvenida |

- Exhaustivo: 2 × 2 × 2 = **8 casos**
- Pairwise: **4 casos** cubren todas las parejas

### ¿Por qué funciona?

**Entre el 70 % y el 90 % de los errores** aparecen por la interacción de solo dos factores. Si cubrimos todas las parejas, atrapamos la mayoría de los fallos con una fracción del esfuerzo.

---

## 3. t = 2 vs t = 3

La **fuerza `t`** indica cuántas variables se combinan a la vez.

| Fuerza | Qué garantiza | Casos en PagoYa | Reducción |
|---|---|---|---|
| Exhaustivo | Todas las combinaciones | 5184 | — |
| **t = 2** | Toda pareja de valores | **22** | ~99,6 % menos |
| **t = 3** | Todo trío de valores | **79** | ~98,5 % menos |

### Por qué usamos t = 2 en PagoYa

- Es el **punto de equilibrio**: detecta la mayoría de errores con solo 22 casos.
- Es **rápido** para ejecutarlo en cada cambio de código.
- Las reglas principales dependen de **pares**: moneda ↔ país, cupón ↔ monto, envío ↔ país.

### Cuándo usar t = 3

- Cuando hay **reglas que dependen de tres factores** a la vez (por ejemplo: cupón + envío + monto).
- En **módulos críticos** donde un fallo cuesta dinero real.
- **Antes de un lanzamiento**, como regresión más profunda.

> **t = 2 para el día a día, t = 3 para lo crítico.**

---

## 4. Granularidad en la tarea

1. **De los valores:** los montos no se prueban uno por uno. Se agrupan en **clases de equivalencia** (bajo, medio, alto, límites de cupones).
2. **De la interacción:** `t = 2` da una cobertura más gruesa y `t = 3` una más fina, con más casos.
3. **De la verificación:** cada fila del `.tsv` es **una prueba individual** en pytest. Si algo falla, se sabe la combinación exacta.

---

## 5. Archivos del proyecto

```
PagoYa/
├── checkout.pict      → Modelo (reglas)
├── checkout_t2.tsv    → 22 casos (t = 2)
├── checkout_t3.tsv    → 79 casos (t = 3)
├── pasarela.py        → Sistema bajo prueba
└── test_pasarela.py   → Robot probador (pytest)
```

| Archivo | Qué hace | Para qué sirve |
|---|---|---|
| `checkout.pict` | Define variables, valores y reglas `IF/THEN` | Plano que PICT usa para descartar combinaciones imposibles |
| `checkout_t2.tsv` / `checkout_t3.tsv` | Tablas de casos generadas por PICT | Datos concretos que ejecuta el robot |
| `pasarela.py` | Procesa pagos y calcula totales | Simula el software real evaluado |
| `test_pasarela.py` | Lee los `.tsv`, ejecuta `pasarela.py` y compara con un oráculo | Valida todos los casos en segundos |

---

## 6. Flujo completo

```
checkout.pict ──(PICT)──► checkout_t2.tsv / checkout_t3.tsv
                                   │
                                   ▼
               test_pasarela.py ──► pasarela.py
                                   │
                                   ▼
                     ✅ PASSED  /  ❌ FAILED
```

---

## 7. Resultados

### Generación de casos

| Estrategia | Casos | Ahorro frente a exhaustivo |
|---|---|---|
| Exhaustivo | 5184 | — |
| Pairwise t = 2 | 22 | ~99,6 % |
| Pairwise t = 3 | 79 | ~98,5 % |

### Error encontrado

| Campo | Detalle |
|---|---|
| **Combinación** | Cupón **Bienvenida** + envío **Express** |
| **Síntoma** | El total cobrado no incluía el descuento del cupón |
| **Causa** | Al calcular el envío Express, `pasarela.py` sobrescribía el total y perdía el descuento aplicado antes |
| **Cómo se detectó** | Pairwise forzó un caso con esa pareja y el oráculo de `test_pasarela.py` marcó la diferencia de precio (**FAILED**) |
| **Corrección** | Aplicar el descuento y el costo de envío de forma independiente, sin que uno borre al otro |

### Antes y después

| Estado | Resultado de pytest |
|---|---|
| Antes de corregir | ❌ FAILED en los casos con Bienvenida + Express |
| Después de corregir | ✅ Todos los casos t = 2 y t = 3 en **PASSED** |

### Conclusiones

- Con solo **22 casos** se detectó un error real que habría afectado cobros a clientes.
- **t = 3** confirmó la corrección con una cobertura más profunda (79 casos).
- El proceso es **repetible**: si cambia una regla, se edita el modelo, se regeneran los casos y se vuelve a probar.

---
