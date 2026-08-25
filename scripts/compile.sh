#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

rm -rf out
mkdir -p out/main out/test

javac --release 8 -encoding UTF-8 -d out/main \
  $(find src/main/java -name '*.java' -print)

javac --release 8 -encoding UTF-8 -cp out/main -d out/test \
  $(find src/test/java -name '*.java' -print)

echo "Compilación completada en $ROOT_DIR/out"
