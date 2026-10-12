#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
printf '%s\n' "$ROOT_DIR/out/servidor/main:$ROOT_DIR/out/cliente/main"
