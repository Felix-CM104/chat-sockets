#!/usr/bin/env bash
# Arranca un cliente. Uso: bash cliente.sh [host] [puerto]
set -e
[ -d out ] || (mkdir -p out && javac -encoding UTF-8 -d out src/chat/*.java)
java -cp out chat.Cliente "$@"
