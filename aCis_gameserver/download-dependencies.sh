#!/bin/bash

echo "Downloading L2Guard dependencies..."
echo ""

cd libs

echo "[1/4] Downloading gson-2.10.1.jar..."
curl -L -o gson-2.10.1.jar https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar

echo "[2/4] Downloading slf4j-api-2.0.9.jar..."
curl -L -o slf4j-api-2.0.9.jar https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar

echo "[3/4] Downloading logback-classic-1.4.11.jar..."
curl -L -o logback-classic-1.4.11.jar https://repo1.maven.org/maven2/ch/qos/logback/logback-classic/1.4.11/logback-classic-1.4.11.jar

echo "[4/4] Downloading logback-core-1.4.11.jar..."
curl -L -o logback-core-1.4.11.jar https://repo1.maven.org/maven2/ch/qos/logback/logback-core/1.4.11/logback-core-1.4.11.jar

echo ""
echo "========================================"
echo "Download complete!"
echo "========================================"
echo ""
echo "Downloaded files:"
ls -lh *.jar
echo ""
echo "Now refresh your Eclipse project (F5) and clean/rebuild!"
