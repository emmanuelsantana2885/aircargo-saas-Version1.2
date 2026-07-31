#!/usr/bin/env bash
# ────────────────────────────────────────────────────────────────
# Aircargo environment loader — source this from scripts.
# Loads secrets from the gitignored .env at the repo root and
# validates that required variables are present.
#
#   . ./aircargo-env.sh
# ────────────────────────────────────────────────────────────────

set -a
AIR_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [ -f "$AIR_ROOT/.env" ]; then
  . "$AIR_ROOT/.env"
fi
set +a

if [ -z "${JWT_SECRET:-}" ]; then
  echo "❌ JWT_SECRET no está definido. Crea el archivo .env desde .env.example:" >&2
  echo "     cp .env.example .env" >&2
  echo "   y genera un secreto con: openssl rand -base64 64" >&2
  exit 1
fi

if [ -z "${POSTGRES_PASSWORD:-}" ]; then
  echo "❌ POSTGRES_PASSWORD no está definido en .env" >&2
  exit 1
fi

if [ -z "${RABBITMQ_PASSWORD:-}" ]; then
  echo "❌ RABBITMQ_PASSWORD no está definido en .env" >&2
  exit 1
fi

export JWT_SECRET RABBITMQ_PASSWORD POSTGRES_PASSWORD POSTGRES_DB POSTGRES_USER RABBITMQ_USER
