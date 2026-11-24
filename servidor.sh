#!/usr/bin/env bash
# Compila y arranca el servidor. Uso: bash servidor.sh [puerto]
set -e
mkdir -p out && javac -encoding UTF-8 -d out src/chat/*.java
java -cp out chat.Servidor "$@"
