# Importamos la función matemática desde el archivo principal para poder probarla
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