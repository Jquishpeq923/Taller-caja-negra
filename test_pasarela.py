import csv
from pathlib import Path

import pytest

from pasarela import procesar_pago

ARCHIVOS = ["checkout_t2.tsv", "checkout_t3.tsv"]

# Oraculo: valores esperados escritos AQUI, independientes del sistema bajo prueba
ENVIO_ESPERADO = {"Estandar": 3, "Express": 8, "Retiro": 0}
DESCUENTO_ESPERADO = {"Ninguno": 0.00, "Bienvenida": 0.05, "Estudiante": 0.10}
MONEDA_ESPERADA = {"Ecuador": "USD", "Colombia": "COP", "Peru": "PEN", "Otro": "USD"}


def cargar_casos(nombre_archivo):
    """Lee un archivo .tsv generado por PICT y devuelve una lista de diccionarios."""
    ruta = Path(__file__).parent / nombre_archivo
    with open(ruta, newline="", encoding="utf-8-sig") as f:
        return list(csv.DictReader(f, delimiter="\t"))


def todos_los_casos():
    casos = []
    for archivo in ARCHIVOS:
        for i, fila in enumerate(cargar_casos(archivo), start=1):
            casos.append(pytest.param(fila, id=f"{archivo[9:11]}-caso{i:02d}"))
    return casos


@pytest.mark.parametrize("caso", todos_los_casos())
def test_pago_valido_es_aprobado(caso):
    resultado = procesar_pago(**{k.lower(): v for k, v in caso.items()})
    assert resultado["estado"] == "APROBADO"


@pytest.mark.parametrize("caso", todos_los_casos())
def test_total_correcto(caso):
    resultado = procesar_pago(**{k.lower(): v for k, v in caso.items()})
    esperado = round(
        float(caso["Monto"]) * (1 - DESCUENTO_ESPERADO[caso["Cupon"]])
        + ENVIO_ESPERADO[caso["Envio"]],
        2,
    )
    assert resultado["total"] == esperado


@pytest.mark.parametrize("caso", todos_los_casos())
def test_moneda_coherente_con_el_pais(caso):
    assert caso["Moneda"] == MONEDA_ESPERADA[caso["Pais"]]


# ---------------------------------------------------------------------------
# Tests propios del equipo (Parte 3.5)
# ---------------------------------------------------------------------------

@pytest.mark.parametrize("caso", todos_los_casos())
def test_total_nunca_negativo(caso):
    """Propiedad siempre cierta: ningun pago aprobado tiene total negativo."""
    resultado = procesar_pago(**{k.lower(): v for k, v in caso.items()})
    assert resultado["total"] >= 0


@pytest.mark.parametrize(
    "caso", [p for p in todos_los_casos() if p.values[0]["Envio"] == "Retiro"]
)
def test_retiro_no_supera_el_monto(caso):
    """Propiedad: con envio Retiro no se cobra envio, asi que el total nunca supera el monto."""
    resultado = procesar_pago(**{k.lower(): v for k, v in caso.items()})
    assert resultado["total"] <= float(caso["Monto"])


# Pruebas negativas: combinaciones prohibidas escritas a mano
# (PICT solo genera combinaciones validas, por eso estas no salen en los .tsv)
CASOS_PROHIBIDOS = [
    pytest.param(dict(dispositivo="Laptop", pais="Ecuador", metodo="Billetera", moneda="USD",
                      envio="Estandar", cupon="Ninguno", monto="50"), id="R3-laptop-billetera"),
    pytest.param(dict(dispositivo="Celular", pais="Colombia", metodo="Tarjeta", moneda="USD",
                      envio="Estandar", cupon="Ninguno", monto="50"), id="R1-moneda-incorrecta"),
    pytest.param(dict(dispositivo="Celular", pais="Peru", metodo="Contraentrega", moneda="PEN",
                      envio="Estandar", cupon="Ninguno", monto="50"), id="R2-contraentrega-peru"),
    pytest.param(dict(dispositivo="Celular", pais="Ecuador", metodo="Contraentrega", moneda="USD",
                      envio="Estandar", cupon="Ninguno", monto="2000"), id="R2-contraentrega-2000"),
    pytest.param(dict(dispositivo="Tablet", pais="Otro", metodo="Tarjeta", moneda="USD",
                      envio="Express", cupon="Ninguno", monto="50"), id="R4-express-otro"),
    pytest.param(dict(dispositivo="Celular", pais="Ecuador", metodo="Tarjeta", moneda="USD",
                      envio="Estandar", cupon="Bienvenida", monto="10"), id="R5-bienvenida-10"),
    pytest.param(dict(dispositivo="Celular", pais="Ecuador", metodo="Billetera", moneda="USD",
                      envio="Estandar", cupon="Ninguno", monto="2000"), id="R6-billetera-2000"),
]


@pytest.mark.parametrize("datos", CASOS_PROHIBIDOS)
def test_combinacion_prohibida_es_rechazada(datos):
    resultado = procesar_pago(**datos)
    assert resultado["estado"] == "RECHAZADO"
    assert resultado["total"] == 0.0
