#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"
rm -rf out
mkdir -p out/servidor/main out/servidor/test out/cliente/main out/cliente/test
javac --release 8 -encoding UTF-8 -d out/servidor/main $(find servidor/src/main/java -name '*.java' -print)
javac --release 8 -encoding UTF-8 -d out/cliente/main $(find cliente/src/main/java -name '*.java' -print)
javac --release 8 -encoding UTF-8 -cp out/servidor/main -d out/servidor/test $(find servidor/src/test/java -name '*.java' -print)
javac --release 8 -encoding UTF-8 -cp out/cliente/main:out/servidor/main -d out/cliente/test $(find cliente/src/test/java -name '*.java' -print)
echo "Compilación RMI completada."
