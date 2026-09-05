#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

# shellcheck source=scripts/classpath.sh
. "$ROOT_DIR/scripts/classpath.sh"

"$ROOT_DIR/scripts/compile.sh"

java -cp "out/main${CP_SEPARATOR}out/test" com.mauricio.porciones.core.PorcionesCalculatorTest
java -cp "out/main${CP_SEPARATOR}out/test" com.mauricio.porciones.core.PorcionesSocketIntegrationTest

echo "Todas las pruebas pasaron."
