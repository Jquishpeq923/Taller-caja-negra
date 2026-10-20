#!/usr/bin/env bash
# ==============================================================================
# SCRIPT: ejecutar_todo.sh
# QUÉ HACE: Compila todo el proyecto Java 17 con 0 errores, corre las 3 suites
# de auditoría de Caja Blanca (McCabe, Test Zombi y Mutación) y opcionalmente
# levanta el servidor web en el puerto 3000.
# ==============================================================================
set -e # Detiene la ejecución inmediatamente si ocurriera algún error

mkdir -p out # Crea la carpeta de salida para los archivos compilados .class

# Detecta si 'javac' está en el PATH o usa el módulo compilador interno de OpenJDK 17
if command -v javac >/dev/null 2>&1; then
  JAVAC_CMD="javac"
else
  JAVAC_CMD="java -m jdk.compiler/com.sun.tools.javac.Main"
fi

echo "1. Compilando código fuente Java 17 y suites de prueba..."
$JAVAC_CMD -encoding UTF-8 -d out src/main/java/com/mediqa/*.java src/test/java/com/mediqa/*.java
echo "   ✔ Compilación exitosa (0 errores)."
echo ""

echo "2. Ejecutando Suite del Auditor QA (19 Tests McCabe + 100% Cobertura)..."
java -cp out com.mediqa.ReglasCitasTest
echo ""

echo "3. Ejecutando Demostración del Test Zombi / Falso 100% (Taller Paso 2)..."
java -cp out com.mediqa.TrampaZombiTest
echo ""

echo "4. Ejecutando Laboratorio de Pruebas de Mutación bajo Modelo PIE (Diapositiva 12)..."
java -cp out com.mediqa.MutacionQATest
echo ""

if [ "$1" = "--server" ]; then
  echo "5. Iniciando Servidor Web Java 17 en http://localhost:3000 ..."
  java -cp out com.mediqa.ServidorCitasJava
else
  echo "Para levantar también el servidor web en el puerto 3000, ejecuta:"
  echo "  bash ejecutar_todo.sh --server"
  echo "O directamente:"
  echo "  java -cp out com.mediqa.ServidorCitasJava"
fi
