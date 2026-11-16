# Suite de regresión · MercaMóvil (bug #482)

**Cambio:** fix en `recalcularTotal()`.
**Impacto:** la usan el carrito, el resumen de pago y los cupones; `CarritoRepository` persiste el total calculado.

| Caso | Descripción | Clasificación | Motivo |
|---|---|---|---|
| C01 | Iniciar sesión con credenciales válidas | Smoke | Ruta crítica |
| C02 | Abrir el catálogo y ver la lista de productos | Smoke | Ruta crítica |
| C03 | Completar una compra de punta a punta | Smoke | Ruta crítica |
| C04 | Cambiar la cantidad y ver el total actualizado | Sanity | Verifica el fix |
| C05 | Eliminar un producto y ver el total recalculado | Sanity | Verifica el fix |
| C06 | Aplicar cupón del 10% sobre el total | Regresión selectiva | Cupones usan `recalcularTotal()` |
| C07 | Resumen de pago muestra el mismo total que el carrito | Regresión selectiva | Resumen usa `recalcularTotal()` |
| C08 | Carrito conserva productos y total al reabrir la app | Regresión selectiva | `CarritoRepository` persiste el total |
| C09 | Cambiar la foto de perfil | No ejecutar | Fuera del impacto |
| C10 | Recibir notificación push de promoción | No ejecutar | Fuera del impacto |
| C11 | Cambiar el idioma de la app | No ejecutar | Fuera del impacto |
