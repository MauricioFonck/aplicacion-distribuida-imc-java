#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"
[ -d out/servidor/main ] || "$ROOT_DIR/scripts/compile.sh"
java -cp out/servidor/main com.mauricio.porciones.entrypoint.ServerMain
