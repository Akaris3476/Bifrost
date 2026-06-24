#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

MIGRATION_DIR="$PROJECT_ROOT/src/main/resources/db/migration"

NAME="$1"

mkdir -p "$MIGRATION_DIR"

NEXT=$(find "$MIGRATION_DIR" -name 'V*__*.sql' \
  | sed -E 's#.*/V([0-9]+)__.*#\1#' \
  | sort -n \
  | tail -1)

NEXT=${NEXT:-0}
NEXT=$((NEXT + 1))

FILE="$MIGRATION_DIR/V${NEXT}__${NAME}.sql"

touch "$FILE"

echo "Created $FILE"