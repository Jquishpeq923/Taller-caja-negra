# Habilita herramientas avanzadas del framework como la captura de errores esperados
import pytest
from presupuesto_analisis import realizar_calculos


def test_calculos_correctos():
    """
    Prueba de validación estándar.
    Verifica que la fórmula matemática funcione correctamente con valores típicos.
    """
    # Paso 1: Ejecutar la función inyectando datos simulados (Presupuesto: 1000, Socios: 2, Meses: 5)
    # Paso 2: Guardar los resultados reales en las variables
    intereses, total, cuota = realizar_calculos(1000, 2, 5)
    
    # Paso 3: Validar usando 'assert'
    # Si el resultado real calculado por el código no es exactamente igual al esperado, la prueba marca FAIL
    assert intereses == 500.0
    assert total == 1500.0
    assert cuota == 750.0

def test_calculos_cero_meses():
    """
    Prueba de caso límite (edge case).
    Verifica el comportamiento lógico del sistema cuando el tiempo de inversión es 0.
    """
    # Enviamos los mismos datos iniciales pero simulando 0 meses de espera
    intereses, total, cuota = realizar_calculos(1000, 2, 0)
    
    # Los intereses generados deben ser obligatoriamente cero
    assert intereses == 0.0
    # El total general no debe cambiar respecto al presupuesto inicial
    assert total == 1000.0
    # La cuota se divide normalmente sin el impacto de los intereses
    assert cuota == 500.0


def test_division_por_cero():
    # Anticipa y atrapa el error matematico para que la prueba pase con exito
    with pytest.raises(ZeroDivisionError):
        # Fuerza el fallo calculando con 0 socios
        realizar_calculos(1000, 0, 5)

def test_inputs_negativos():
    # Inyecta un presupuesto inicial negativo simulando una deuda
    intereses, total, cuota = realizar_calculos(-1000, 2, 5)
    
    # Confirma que el dinero total calculado mantenga su valor negativo
    assert total < 0