#!/bin/bash
set -e

DIR="$(cd "$(dirname "$0")" && pwd)"

echo "=== SFI - Sistema de Facturacion Integrado ==="

if ! command -v java &>/dev/null; then
    echo "ERROR: Java no encontrado. Instala Java 17+ con:"
    echo "  sudo apt install openjdk-17-jdk"
    exit 1
fi

JAVA_VER=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VER" -lt 17 ]; then
    echo "ERROR: Se requiere Java 17+. Version detectada: $(java -version 2>&1 | head -1)"
    exit 1
fi

install_mysql() {
    echo "Instalando MySQL..."
    sudo apt update
    sudo apt install -y mysql-server
    sudo systemctl start mysql
    sudo systemctl enable mysql
    echo "MySQL instalado."
}

if ! command -v mysql &>/dev/null; then
    echo "MySQL no instalado. Instalando..."
    install_mysql
else
    if ! systemctl is-active --quiet mysql 2>/dev/null && ! pgrep -x mysqld >/dev/null; then
        echo "Iniciando MySQL..."
        sudo systemctl start mysql 2>/dev/null || sudo service mysql start 2>/dev/null || true
    fi
fi

SCHEMA="$DIR/schema.sql"
JAR="$DIR/facturador-sfi-1.0.0.jar"

if [ ! -f "$SCHEMA" ]; then
    echo "ERROR: No se encuentra schema.sql en $DIR"
    exit 1
fi
if [ ! -f "$JAR" ]; then
    echo "ERROR: No se encuentra facturador-sfi-1.0.0.jar en $DIR"
    exit 1
fi

echo "Configurando base de datos..."
sudo mysql < "$SCHEMA"

echo "Iniciando SFI..."
java -cp "$JAR:$DIR/lib/*" com.sfi.App
