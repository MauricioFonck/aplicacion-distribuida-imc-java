#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

"$ROOT_DIR/scripts/compile.sh"

java -cp out/main:out/test com.mauricio.imc.core.ImcCalculatorTest
java -cp out/main:out/test com.mauricio.imc.core.ImcSocketIntegrationTest

echo "Todas las pruebas pasaron."
