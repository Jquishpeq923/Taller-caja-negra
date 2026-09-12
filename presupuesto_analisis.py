def realizar_calculos(presupuesto, socios, meses):
    """
    Función pura que contiene la lógica de negocio.
    Se separó de los inputs del usuario para permitir la automatización de pruebas en la nube.
    """
    # Fórmula estandarizada para que coincida con las pruebas unitarias
    intereses = presupuesto * 0.1 * meses
    
    # Suma del capital inicial más los intereses generados
    total = presupuesto + intereses
    
    # Manejo de la división por cero para que PyTest capture la excepción correctamente
    if socios == 0:
        raise ZeroDivisionError("El número de socios no puede ser cero.")
        
    # División equitativa del total entre todos los socios
    cuota_por_socio = total / socios
    
    # Retorna los tres valores calculados para que otra función los pueda usar
    return intereses, total, cuota_por_socio

def calcular_presupuesto():
    """
    Función de interfaz de usuario.
    Maneja exclusivamente las entradas (inputs) y salidas (prints) en la consola.
    """
    print("=== Sistema de Análisis de Presupuesto ===\n")
    
    # Recolección de datos desde la terminal (pausa la ejecución hasta recibir respuesta)
    presupuesto = float(input("Ingrese el presupuesto total: "))
    socios = int(input("Ingrese el número de socios: "))
    meses = int(input("Ingrese los meses de inversión: "))

    # Llamada a la función matemática enviando los datos recolectados por el usuario
    intereses, total, cuota_por_socio = realizar_calculos(presupuesto, socios, meses)

    # Impresión de resultados con formato de 2 decimales (.2f)
    print(f"\nPresupuesto inicial: ${presupuesto:.2f}")
    print(f"Intereses generados: ${intereses:.2f}")
    print(f"Total con intereses: ${total:.2f}")
    print(f"Cuota por socio ({socios} socios): ${cuota_por_socio:.2f}")

# Condición de entrada principal que ejecuta el programa solo si se corre manualmente
if __name__ == "__main__":
    calcular_presupuesto()