#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

# shellcheck source=scripts/classpath.sh
. "$ROOT_DIR/scripts/classpath.sh"

"$ROOT_DIR/scripts/compile.sh"

run_test() {
  local proyecto="$1"
  local clase="$2"
  java -cp "out/$proyecto/main${CP_SEPARATOR}out/$proyecto/test" "com.mauricio.porciones.$clase"
}

echo "== Servidor =="
run_test servidor DominioTest
run_test servidor AplicacionTest
run_test servidor AdaptadoresTest
run_test servidor RedUdpIntegracionTest

echo "== Cliente =="
run_test cliente DominioYProtocoloTest
run_test cliente ClienteServiceTest
run_test cliente RedIntegracionTest

echo "Todas las pruebas pasaron."
