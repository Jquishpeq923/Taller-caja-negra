# Taller Autónomo: Modelado Combinatorio y All-Pairs
**Sistema bajo prueba:** Configurador Web Global — SmartDrive Motors
**Entorno de ejecución:** Testing ortogonal y análisis combinatorial

---

## 1. Matriz de Parámetros y Valores (P&V)

Para descomponer el espacio de configuración del vehículo en un modelo matemático discreto, identificamos 5 parámetros clave con 3 valores posibles para cada uno (n = 3)[cite: 4, 5, 12].

| ID | Parámetro (P) | Valores Posibles (V) | n (Opciones) |
| :---: | :--- | :--- | :---: |
| **P1** | Motor | Gasolina, Híbrido, Eléctrico | 3 |
| **P2** | Transmisión | Manual, Automática, Monomarcha | 3 |
| **P3** | Frenos | Estándar, ABS, Regenerativo | 3 |
| **P4** | Mercado | América, Europa, Asia | 3 |
| **P5** | Modo de Conducción | Eco, Sport, Autónomo | 3 |

---

## 2. Peligro de la Explosión Combinatoria

### Cálculo Matemático Exhaustivo:
El número total de configuraciones posibles evaluadas mediante fuerza bruta corresponde al producto cartesiano de los valores discretos de cada parámetro[cite: 6]:

$$Total = v_1 \times v_2 \times v_3 \times v_4 \times v_5 = 3^5 = 243 \text{ combinaciones puras}$$
[cite: 6]

### Estimación Operativa:
* **Duración promedio por prueba:** 15 minutos[cite: 6].
* **Tiempo total estimado:** $243 \times 15 \text{ min} = 3645 \text{ minutos}$ (60.75 horas continuas de ejecución).

### Justificación de Inviabilidad Financiera y Operativa:
Dedicar más de 60 horas netas de trabajo técnico a un solo ciclo de validación representa más de una semana laboral completa de un equipo de QA asignada únicamente a un módulo. Este enfoque no es escalable: ralentiza la entrega de valor al mercado (time-to-market), satura los recursos disponibles y dispara los costos de nómina en pruebas altamente redundantes. Probar de forma exhaustiva en un espacio combinatorial de esta magnitud es financieramente inviable y operativamente insostenible[cite: 6].

---

## 3. Suite Reducida All-Pairs (t = 2)

Aplicando el criterio de cobertura ortogonal por pares (Pairwise Testing), reducimos el volumen de 243 configuraciones a un conjunto optimizado de 18 casos de prueba[cite: 7, 9]. Esta selección garantiza que cualquier combinación de dos parámetros posibles ($t = 2$) se encuentre presente al menos una vez en la matriz.

| Caso de Prueba | Motor | Transmisión | Frenos | Mercado | Modo de Conducción |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **TC-01** | Gasolina | Manual | Estándar | América | Eco |
| **TC-02** | Gasolina | Automática | ABS | Europa | Sport |
| **TC-03** | Gasolina | Monomarcha | Regenerativo | Asia | Autónomo |
| **TC-04** | Híbrido | Manual | ABS | Asia | Sport |
| **TC-05** | Híbrido | Automática | Regenerativo | América | Autónomo |
| **TC-06** | Híbrido | Monomarcha | Estándar | Europa | Eco |
| **TC-07** | Eléctrico | Manual | Regenerativo | Europa | Autónomo |
| **TC-08** | Eléctrico | Automática | Estándar | Asia | Eco |
| **TC-09** | Eléctrico | Monomarcha | ABS | América | Sport |
| **TC-10** | Gasolina | Manual | Regenerativo | Europa | Sport |
| **TC-11** | Gasolina | Automática | Estándar | Asia | Autónomo |
| **TC-12** | Gasolina | Monomarcha | ABS | América | Eco |
| **TC-13** | Híbrido | Manual | Estándar | Asia | Autónomo |
| **TC-14** | Híbrido | Automática | ABS | América | Eco |
| **TC-15** | Híbrido | Monomarcha | Regenerativo | Europa | Sport |
| **TC-16** | Eléctrico | Manual | ABS | América | Eco |
| **TC-17** | Eléctrico | Automática | Regenerativo | Europa | Sport |
| **TC-18** | Eléctrico | Monomarcha | Estándar | Asia | Autónomo |

---

## 4. Identificación de Restricciones Lógicas (Constraints)

Las matrices ortogonales puras asumen independencia total entre variables, pero los procesos de manufactura reales exigen validar compatibilidad física y regulatoria. Se definen las siguientes reglas de negocio en pseudocódigo formal[cite: 11]:

```pseudocode
// REGLA 1: La arquitectura eléctrica pura requiere tracción de relación única y recuperación energética
IF Motor == "Eléctrico" 
THEN Transmisión == "Monomarcha" AND Frenos == "Regenerativo";

// REGLA 2: Incompatibilidad física de sistemas regenerativos en trenes de combustión pura
IF Motor == "Gasolina" 
THEN Frenos != "Regenerativo";

// REGLA 3: Requisitos de redundancia de frenado asistido para módulos de conducción autónoma
IF Modo_Conducción == "Autónomo" 
THEN Frenos == "ABS" OR Frenos == "Regenerativo";
