#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"
[ -d out/cliente/main ] || "$ROOT_DIR/scripts/compile.sh"
java -cp out/cliente/main com.mauricio.porciones.entrypoint.ClientWindow
