#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Usage: scripts/sync_skills.sh [--source DIR] [--codex-dir DIR] [--claude-dir DIR] [--dist-dir DIR] [--skip-package]

Sync skills from a repo-local skills directory into Codex and Claude skill locations.
Also packages each skill into a .skill archive unless --skip-package is set.

Defaults:
  source      ./skills
  codex-dir   ${CODEX_HOME:-$HOME/.codex}/skills
  claude-dir  ${CLAUDE_SKILLS_DIR:-$HOME/.claude/skills}
  dist-dir    ./dist

Environment overrides:
  CODEX_HOME
  CLAUDE_SKILLS_DIR
  SYNC_SKILLS_FORCE_RSYNC=1
EOF
}

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source_dir="$repo_root/skills"
codex_dir="${CODEX_HOME:-$HOME/.codex}/skills"
claude_dir="${CLAUDE_SKILLS_DIR:-$HOME/.claude/skills}"
dist_dir="$repo_root/dist"
skip_package=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --source)
      source_dir="$2"
      shift 2
      ;;
    --codex-dir)
      codex_dir="$2"
      shift 2
      ;;
    --claude-dir)
      claude_dir="$2"
      shift 2
      ;;
    --dist-dir)
      dist_dir="$2"
      shift 2
      ;;
    --skip-package)
      skip_package=1
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage >&2
      exit 1
      ;;
  esac
done

if [[ ! -d "$source_dir" ]]; then
  echo "Source skills directory not found: $source_dir" >&2
  exit 1
fi

mkdir -p "$codex_dir" "$claude_dir" "$dist_dir"

have_rsync=0
if command -v rsync >/dev/null 2>&1; then
  have_rsync=1
fi

copy_tree() {
  local src="$1"
  local dest="$2"

  mkdir -p "$dest"

  if [[ "$have_rsync" -eq 1 || "${SYNC_SKILLS_FORCE_RSYNC:-0}" == "1" ]]; then
    if rsync -a --delete "$src"/ "$dest"/; then
      return
    fi
  fi

  find "$dest" -mindepth 1 -maxdepth 1 -exec rm -rf {} +
  cp -R "$src"/. "$dest"/
}

package_skill() {
  local skill_dir="$1"
  local out_dir="$2"
  local skill_name
  skill_name="$(basename "$skill_dir")"
  local output="$out_dir/$skill_name.skill"

  python3 - "$skill_dir" "$output" <<'PY'
import fnmatch
import sys
import zipfile
from pathlib import Path

skill_dir = Path(sys.argv[1]).resolve()
output = Path(sys.argv[2]).resolve()

exclude_dirs = {"__pycache__", "node_modules"}
exclude_files = {".DS_Store"}
exclude_globs = ("*.pyc",)
root_exclude_dirs = {"evals"}

output.parent.mkdir(parents=True, exist_ok=True)

with zipfile.ZipFile(output, "w", zipfile.ZIP_DEFLATED) as zf:
    for path in skill_dir.rglob("*"):
        if not path.is_file():
            continue
        rel = path.relative_to(skill_dir.parent)
        parts = rel.parts
        if any(part in exclude_dirs for part in parts):
            continue
        if len(parts) > 1 and parts[1] in root_exclude_dirs:
            continue
        if path.name in exclude_files or any(fnmatch.fnmatch(path.name, pat) for pat in exclude_globs):
            continue
        zf.write(path, rel)

print(output)
PY
}

skill_count=0
for skill_dir in "$source_dir"/*; do
  [[ -d "$skill_dir" ]] || continue
  [[ -f "$skill_dir/SKILL.md" ]] || continue

  skill_name="$(basename "$skill_dir")"
  echo "Syncing $skill_name"

  copy_tree "$skill_dir" "$codex_dir/$skill_name"
  copy_tree "$skill_dir" "$claude_dir/$skill_name"

  if [[ "$skip_package" -eq 0 ]]; then
    package_skill "$skill_dir" "$dist_dir" >/dev/null
  fi

  skill_count=$((skill_count + 1))
done

if [[ "$skill_count" -eq 0 ]]; then
  echo "No skills found in: $source_dir" >&2
  exit 1
fi

echo "Synced $skill_count skill(s)."
echo "Codex skills: $codex_dir"
echo "Claude skills: $claude_dir"
echo "Packages: $dist_dir"
