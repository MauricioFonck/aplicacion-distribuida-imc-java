#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"
"$ROOT_DIR/scripts/compile.sh"
java -cp out/servidor/main:out/servidor/test com.mauricio.porciones.DominioRmiTest
java -cp out/cliente/main:out/servidor/main:out/cliente/test com.mauricio.porciones.ClienteRmiTest
echo "Todas las pruebas RMI pasaron."
