#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Usage: scripts/postflow-hook.sh [--source codex|claude] [--task-id ID] [--run-id ID] [--context-json JSON]

Environment overrides:
  POSTFLOW_SOURCE
  POSTFLOW_MODE
  POSTFLOW_TASK_ID
  POSTFLOW_RUN_ID
  POSTFLOW_CONTEXT_JSON
  POSTFLOW_ECHO_ONLY=1

This script is the mandatory post-flow entrypoint for Codex and Claude.
It prints a markdown proposal to stdout so the caller can surface it before finalizing the run.
EOF
}

source_name="${POSTFLOW_SOURCE:-}"
mode_name="${POSTFLOW_MODE:-posthook}"
task_id="${POSTFLOW_TASK_ID:-}"
run_id="${POSTFLOW_RUN_ID:-}"
context_json="${POSTFLOW_CONTEXT_JSON:-}"
echo_only="${POSTFLOW_ECHO_ONLY:-0}"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --source)
      source_name="$2"
      shift 2
      ;;
    --task-id)
      task_id="$2"
      shift 2
      ;;
    --run-id)
      run_id="$2"
      shift 2
      ;;
    --context-json)
      context_json="$2"
      shift 2
      ;;
    --echo-only)
      echo_only=1
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

if [[ -z "$source_name" ]]; then
  echo "Missing POSTFLOW_SOURCE or --source" >&2
  exit 1
fi

emit_blocked_template() {
  cat <<EOF
## MCP Proposal
### Current state
- No external writeback occurs from the posthook entrypoint.
- Caller source: ${source_name}
- Mode: ${mode_name}
$( [[ -n "$task_id" ]] && printf '%s\n' "- Task ID: ${task_id}" )
$( [[ -n "$run_id" ]] && printf '%s\n' "- Run ID: ${run_id}" )
$( [[ -n "$context_json" ]] && printf '%s\n' "- Context provided: yes" )

### Proposed update
- Use the agent's existing MCP tools to read the latest state before finalizing.
- Keep the caller source unchanged so Codex and Claude share the same contract.

### Why this proposal exists
- The flow is ending and the mandatory proposal step still has to run.

### Action status
- proposed
EOF
}

if [[ "$echo_only" == "1" ]]; then
  emit_blocked_template
  exit 2
fi

emit_blocked_template
