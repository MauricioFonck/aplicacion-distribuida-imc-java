#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

rm -rf out

# El servidor y el cliente son proyectos independientes: cada uno se compila por separado.
for proyecto in servidor cliente; do
  mkdir -p "out/$proyecto/main" "out/$proyecto/test"

  javac --release 17 -encoding UTF-8 -d "out/$proyecto/main" \
    $(find "$proyecto/src/main/java" -name '*.java' -print)

  javac --release 17 -encoding UTF-8 -cp "out/$proyecto/main" -d "out/$proyecto/test" \
    $(find "$proyecto/src/test/java" -name '*.java' -print)
done

echo "Compilación completada en $ROOT_DIR/out"
