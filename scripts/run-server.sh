#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [ ! -d out/servidor/main ]; then
  "$ROOT_DIR/scripts/compile.sh"
fi

java -cp out/servidor/main com.mauricio.Main
