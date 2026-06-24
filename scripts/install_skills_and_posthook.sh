#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Usage: scripts/install_skills_and_posthook.sh [options]

Options:
  --source DIR            Skills source directory (default: ./skills)
  --codex-dir DIR         Codex skills directory (default: ${CODEX_HOME:-$HOME/.codex}/skills)
  --claude-dir DIR        Claude skills directory (default: ${CLAUDE_SKILLS_DIR:-$HOME/.claude/skills})
  --dist-dir DIR          Package output directory (default: ./dist)
  --codex-hook-dir DIR    Optional Codex posthook target directory
  --claude-hook-dir DIR   Optional Claude posthook target directory
  --bundle-dir DIR        Optional bundle directory for copied hook files (default: ./dist/posthook)
  --skip-package          Skip .skill packaging
  -h, --help              Show help

Environment overrides:
  CODEX_HOME
  CLAUDE_SKILLS_DIR
  CODEX_POSTHOOK_DIR
  CLAUDE_POSTHOOK_DIR
  POSTHOOK_BUNDLE_DIR
EOF
}

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source_dir="$repo_root/skills"
codex_dir="${CODEX_HOME:-$HOME/.codex}/skills"
claude_dir="${CLAUDE_SKILLS_DIR:-$HOME/.claude/skills}"
dist_dir="$repo_root/dist"
codex_hook_dir="${CODEX_POSTHOOK_DIR:-}"
claude_hook_dir="${CLAUDE_POSTHOOK_DIR:-}"
bundle_dir="${POSTHOOK_BUNDLE_DIR:-$repo_root/dist/posthook}"
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
    --codex-hook-dir)
      codex_hook_dir="$2"
      shift 2
      ;;
    --claude-hook-dir)
      claude_hook_dir="$2"
      shift 2
      ;;
    --bundle-dir)
      bundle_dir="$2"
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

hook_src="$repo_root/scripts/postflow-hook.sh"

if [[ ! -f "$hook_src" ]]; then
  echo "Hook script not found: $hook_src" >&2
  exit 1
fi

echo "Installing skills..."
sync_args=(
  --source "$source_dir"
  --codex-dir "$codex_dir"
  --claude-dir "$claude_dir"
  --dist-dir "$dist_dir"
)
if [[ "$skip_package" -eq 1 ]]; then
  sync_args+=(--skip-package)
fi
"$repo_root/scripts/sync_skills.sh" "${sync_args[@]}"

mkdir -p "$bundle_dir"
install -m 0755 "$hook_src" "$bundle_dir/postflow-hook.sh"

if [[ -n "$codex_hook_dir" ]]; then
  mkdir -p "$codex_hook_dir"
  install -m 0755 "$hook_src" "$codex_hook_dir/postflow-hook.sh"
fi

if [[ -n "$claude_hook_dir" ]]; then
  mkdir -p "$claude_hook_dir"
  install -m 0755 "$hook_src" "$claude_hook_dir/postflow-hook.sh"
fi

cat > "$bundle_dir/install-manifest.txt" <<EOF
Skills source: $source_dir
Codex skills: $codex_dir
Claude skills: $claude_dir
Bundle dir: $bundle_dir
Codex hook dir: ${codex_hook_dir:-not set}
Claude hook dir: ${claude_hook_dir:-not set}
Hook entrypoint: $hook_src
EOF

echo "Installed hook bundle to: $bundle_dir"
if [[ -n "$codex_hook_dir" ]]; then
  echo "Installed Codex hook to: $codex_hook_dir"
fi
if [[ -n "$claude_hook_dir" ]]; then
  echo "Installed Claude hook to: $claude_hook_dir"
fi
