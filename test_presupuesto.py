# Habilita herramientas avanzadas del framework como la captura de errores esperados
import pytest  

# Importa desde el archivo principal la función de cálculo que vamos a evaluar
from presupuesto_analisis import realizar_calculos

# Define la primera función de prueba enfocada en el caso feliz o estándar
def test_calculos_correctos():
    """
    Prueba de validación estándar.
    Verifica que la fórmula matemática funcione correctamente con valores típicos.
    """
    # Ejecuta la función inyectando datos normales y guarda los tres resultados devueltos
    intereses, total, cuota = realizar_calculos(1000, 2, 5)
    
    # Comprueba mediante 'assert' que los intereses calculados sean exactamente iguales al valor esperado (500.0)
    assert intereses == 500.0
    
    # Comprueba mediante 'assert' que el total general sea exactamente igual al valor esperado (1500.0)
    assert total == 1500.0
    
    # Comprueba mediante 'assert' que la cuota por socio sea exactamente igual al valor esperado (750.0)
    assert cuota == 750.0

# Define la segunda función de prueba enfocada en un caso límite (edge case)
def test_calculos_cero_meses():
    """
    Prueba de caso límite (edge case).
    Verifica el comportamiento lógico del sistema cuando el tiempo de inversión es 0.
    """
    # Ejecuta la función enviando los datos iniciales pero configurando los meses en cero
    intereses, total, cuota = realizar_calculos(1000, 2, 0)
    
    # Verifica que al no pasar tiempo, los intereses generados sean obligatoriamente cero
    assert intereses == 0.0
    
    # Verifica que el total general no sufra alteraciones respecto al presupuesto original
    assert total == 1000.0
    
    # Verifica que la cuota se divida de manera limpia entre los socios sin sumar intereses
    assert cuota == 500.0

# Define la tercera función de prueba encargada de validar excepciones críticas
def test_division_por_cero():
    # Inicia una estructura de protección en PyTest para anticipar y atrapar un error matemático esperado
    with pytest.raises(ZeroDivisionError):
        
        # Ejecuta la calculadora pasando cero en el número de socios para forzar el error de división por cero
        realizar_calculos(1000, 0, 5)

# Define la cuarta función de prueba enfocada en validar la resistencia ante entradas anómalas
def test_inputs_negativos():
    # Ejecuta la calculadora inyectando un presupuesto inicial negativo para simular una deuda y almacena el resultado
    intereses, total, cuota = realizar_calculos(-1000, 2, 5)
    
    # Utiliza 'assert' para comprobar lógicamente que el dinero total resultante mantenga su naturaleza negativa
    assert total < 0