# Casos de Prueba de Caja Negra — presupuesto_analisis.py

flowchart TD

    %% ==============================
    %% 🌸 CADENA DE CAUSALIDAD
    %% ==============================
    subgraph Cadena["🌸 Cadena de causalidad"]
        direction LR

        E["🌷 <b>Error</b><br/><small>Falla humana</small>"]
        D["🍓 <b>Defecto</b><br/><small>Línea de código</small>"]
        F["✨ <b>Fallo</b><br/><small>Se ve al ejecutar</small>"]

        E --> D --> F
    end


    %% ==============================
    %% 🐰 ROLES OPERATIVOS
    %% ==============================
    subgraph Roles["🐰 Roles operativos"]
        direction LR

        QA["🌸 <b>QA</b><br/><small>Mejora el proceso</small>"]
        QC["🧸 <b>QC</b><br/><small>Revisa el producto</small>"]
        T["🎀 <b>Testing</b><br/><small>Ejecuta las pruebas</small>"]

        QA ~~~ QC
        QC ~~~ T
    end


    %% ==============================
    %% 🍰 PRINCIPIOS ISTQB
    %% ==============================
    subgraph Principios["🍰 7 principios ISTQB"]
        direction TB

        P1["🌱 <b>Detecta, no elimina</b><br/><small>Muestra que hay bugs</small>"]
        P2["🫧 <b>Nunca se prueba todo</b><br/><small>Cobertura total es imposible</small>"]
        P3["💗 <b>Antes cuesta menos</b><br/><small>Detectar temprano ahorra</small>"]
        P4["🍓 <b>Los bugs se agrupan</b><br/><small>Concentrados en pocas zonas</small>"]
        P5["🔄 <b>Repetir pierde efecto</b><br/><small>La misma prueba deja de servir</small>"]
        P6["🌈 <b>Cada contexto es distinto</b><br/><small>No hay una receta única</small>"]
        P7["🌸 <b>Sin bugs no es perfecto</b><br/><small>Puede seguir sin resolver el problema</small>"]

        P1 ~~~ P2
        P2 ~~~ P3
        P3 ~~~ P4
        P4 ~~~ P5
        P5 ~~~ P6
        P6 ~~~ P7
    end


    %% ==============================
    %% 🔗 RELACIONES
    %% ==============================
    Cadena -->|"💫 se detecta durante"| Roles
    Roles -->|"🎀 guiado por"| Principios


    %% ==============================
    %% 🎨 ESTILOS KAWAII
    %% ==============================

    classDef causal fill:#FFF0F6,stroke:#E88BB5,stroke-width:2px,color:#5C3A4A;
    classDef roles fill:#F0F7FF,stroke:#8DB8E8,stroke-width:2px,color:#384A5C;
    classDef principles fill:#FFF9E6,stroke:#E5C66B,stroke-width:2px,color:#5C5132;

    class E,D,F causal;
    class QA,QC,T roles;
    class P1,P2,P3,P4,P5,P6,P7 principles;

    style Cadena fill:#FFF8FC,stroke:#E88BB5,stroke-width:3px
    style Roles fill:#F7FBFF,stroke:#8DB8E8,stroke-width:3px
    style Principios fill:#FFFDF3,stroke:#E5C66B,stroke-width:3px

    linkStyle default stroke:#C79AB0,stroke-width:2px

**Notas del equipo:**
- Error → Defecto → Fallo: el error es la equivocación humana, el defecto es la línea física incorrecta, el fallo es lo que se observa al ejecutar.
- QA es preventivo (mejora el proceso), QC es correctivo (inspecciona el producto), Testing es la actividad concreta dentro de QC.
- Los 7 principios ISTQB guían cómo se diseña y ejecuta el testing.

---

## Tabla de Casos de Prueba

| ID | Descripción | Precondición | Entrada | Esperado | Real | Estado |
|----|---|---|---|---|---|---|
| CP-01 | Comportamiento sin socios | Sistema iniciado, presupuesto > 0 | presupuesto=10000, socios=0, meses=12 | Mensaje controlado de error, sin cerrar el programa | `ZeroDivisionError` en la línea `cuota_por_socio = total / socios` — el programa se detiene | Failed |
| CP-02 | Comportamiento con número negativo de socios | Sistema iniciado, presupuesto > 0 | presupuesto=10000, socios=-2, meses=12 | Mensaje controlado de error, sin cerrar el programa | Calcula una cuota por socio negativa, sin ninguna advertencia | Failed |
| CP-03 | Comportamiento con plazo de inversión extremo | Sistema iniciado, presupuesto > 0, socios > 0 | presupuesto=10000, socios=4, meses=100 | Cálculo correcto o mensaje de advertencia por valor fuera de rango razonable | Calcula un interés desproporcionado ($2,000,000.00) sin ninguna advertencia | Failed |

---

## Reportes de Defecto

**CP-01** → El Fallo ocurre porque el Defecto está en la línea `cuota_por_socio = total / socios`: el código nunca valida que `socios` sea distinto de cero antes de dividir.

**CP-02** → El Fallo ocurre porque no existe ninguna validación de que `socios` sea un número positivo. El programa acepta valores negativos y produce un resultado financiero sin sentido de forma silenciosa (no lanza excepción).

**CP-03** → El Fallo ocurre porque no hay validación de límite superior para `meses`. El término `meses ** 2` crece cuadráticamente sin ningún techo, generando intereses desproporcionados sin advertencia alguna.

---

## Nota metodológica

Los tres defectos representan dos tipos distintos de fallo:

- **Fallo explícito** (CP-01): el programa se detiene con un `Traceback` visible.
- **Fallo silencioso** (CP-02 y CP-03): el programa entrega un resultado, pero ese resultado es incorrecto o carece de sentido — mucho más difícil de detectar sin un plan de pruebas deliberado.
