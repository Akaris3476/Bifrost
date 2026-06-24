# Posthook Contract

Use this contract when wiring `mcp-postflow-proposal` into a wrapper, workflow runner, or posthook.

## Goal

Force one final proposal step before the agent is allowed to finish.

## Required metadata

- `POSTFLOW_SOURCE`: `codex` or `claude`
- `POSTFLOW_MODE`: `posthook`

## Optional metadata

- `POSTFLOW_TASK_ID`
- `POSTFLOW_RUN_ID`
- `POSTFLOW_MCP_URL`
- `POSTFLOW_CHANGED_FILES`
- `POSTFLOW_CONTEXT_JSON`

## Expected behavior

1. The hook calls the skill at the end of the flow.
2. The skill reads the latest MCP-backed state if available.
3. The skill emits a proposal in markdown.
4. The caller surfaces that proposal before finalizing the run.

The repo-local entrypoint for this contract is [`scripts/postflow-hook.sh`](../../../scripts/postflow-hook.sh).

## Output contract

The proposal should include:

- `Current state`
- `Proposed update`
- `Why this proposal exists`
- `Action status`

## Exit conventions

- `0`: proposal emitted
- `2`: blocked by missing MCP access or missing state
- non-zero: hook failure

## Notes for Codex and Claude

- Use the same contract in both systems.
- Pass only the source identifier as the agent-specific difference.
- Do not make the output format depend on the caller.
- Keep the hook mandatory in the orchestration layer, not just in the prompt.
- Prefer a single shared posthook wrapper for both agents so behavior stays aligned.
