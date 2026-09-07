from presupuesto_analisis import realizar_calculos

def test_calculos_correctos():
    # Enviamos: 1000 de presupuesto, 2 socios, 5 meses
    intereses, total, cuota = realizar_calculos(1000, 2, 5)
    
    # Verificamos que la matemática coincida con los resultados esperados
    assert intereses == 500.0
    assert total == 1500.0
    assert cuota == 750.0

def test_calculos_cero_meses():
    # Si los meses son 0, los intereses deben ser 0
    intereses, total, cuota = realizar_calculos(1000, 2, 0)
    
    assert intereses == 0.0
    assert total == 1000.0
    assert cuota == 500.0