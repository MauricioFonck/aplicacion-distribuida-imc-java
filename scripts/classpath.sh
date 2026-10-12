#!/usr/bin/env bash
# Separador de classpath: ";" en Windows (Git Bash, MSYS, Cygwin) y ":" en el resto.
case "$(uname -s)" in
  MINGW*|MSYS*|CYGWIN*) CP_SEPARATOR=";" ;;
  *) CP_SEPARATOR=":" ;;
esac
