#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
output="$root_dir/HANDBOOK.md"

{
  printf '# Comprehensive Software Engineering Interview Handbook\n\n'
  printf '> Generated from the modular source files in `parts/`.\n\n'
  cat "$root_dir/front-matter.md"
  printf '\n\n'
  for part in "$root_dir"/parts/part-*.md; do
    cat "$part"
    printf '\n\n'
  done
} > "$output"

perl -0pi -e 's/\n+\z/\n/' "$output"
printf 'Generated %s\n' "$output"
