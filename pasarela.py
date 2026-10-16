"""Sistema bajo prueba: pasarela de pagos PagoYa (version del taller)."""

ENVIO_USD = {"Estandar": 3, "Express": 8, "Retiro": 0}
DESCUENTO = {"Ninguno": 0.00, "Bienvenida": 0.05, "Estudiante": 0.10}
MONEDA_DEL_PAIS = {"Ecuador": "USD", "Colombia": "COP", "Peru": "PEN", "Otro": "USD"}


def procesar_pago(dispositivo, pais, metodo, moneda, envio, cupon, monto):
    """Devuelve {"estado": "APROBADO" | "RECHAZADO", "total": float}."""
    monto = float(monto)

    # Reglas de negocio: si se violan, el pago se rechaza
    if moneda != MONEDA_DEL_PAIS[pais]:
        return {"estado": "RECHAZADO", "total": 0.0}
    if metodo == "Contraentrega" and (pais not in ("Ecuador", "Colombia") or monto > 500):
        return {"estado": "RECHAZADO", "total": 0.0}
    if metodo == "Billetera" and dispositivo == "Laptop":
        return {"estado": "RECHAZADO", "total": 0.0}
    if envio == "Express" and pais == "Otro":
        return {"estado": "RECHAZADO", "total": 0.0}
    if cupon == "Bienvenida" and monto < 50:
        return {"estado": "RECHAZADO", "total": 0.0}
    if monto >= 2000 and metodo not in ("Tarjeta", "Transferencia"):
        return {"estado": "RECHAZADO", "total": 0.0}

    # Calculo del total
    # Corregido: antes, con cupon Bienvenida y envio Express el descuento
    # se ponia en 0 y el cliente pagaba el monto completo.
    descuento = DESCUENTO[cupon]
    total = round(monto * (1 - descuento) + ENVIO_USD[envio], 2)
    return {"estado": "APROBADO", "total": total}
