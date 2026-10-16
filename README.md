# Taller Práctico 4: PICT + pytest (PagoYa)

**Integrantes:** Julian, (agrega aquí a tu equipo)

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

## El error que encontramos
Todas las filas que fallaban compartían el par **Cupon = Bienvenida + Envio = Express**. En `pasarela.py` había una condición que ponía el descuento en 0 cuando se daba esa combinación, así que el cliente pagaba el monto completo y perdía su 5 % de descuento. Eliminamos esa condición y la suite pasó a estar completamente en verde.

## Tests propios
- `test_total_nunca_negativo`: ningún pago aprobado tiene total negativo.
- `test_retiro_no_supera_el_monto`: con envío Retiro el total nunca supera el monto original.
- `test_combinacion_prohibida_es_rechazada`: 7 pruebas negativas escritas a mano (al menos una por cada regla R1 a R6) que deben devolver RECHAZADO.

## Cómo ejecutar
    pict checkout.pict /o:2 > checkout_t2.tsv
    pict checkout.pict /o:3 > checkout_t3.tsv
    pytest -v
