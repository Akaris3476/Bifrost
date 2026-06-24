---
name: mcp-postflow-proposal
description: Generate a mandatory end-of-flow proposal from the latest MCP knowledge-base state after any agentic task, especially when a task updates docs, facts, or project memory. Use this whenever a workflow is about to finish and you need to surface the newest MCP-backed state as a proposal instead of silently ending the turn. Prefer this skill for posthook-like behavior, workflow runners, or toolchains in Codex or Claude that must always emit a refreshed proposal before completion.
---

# MCP Post-Flow Proposal

Use this skill to make sure every agentic flow ends with a fresh proposal that reflects the latest MCP knowledge-base
state.

This skill is not a write-back mechanism. Its job is to:

- read the newest available MCP-backed knowledge
- compare it to the current task context
- produce a concise proposal for what should be updated or confirmed
- make the proposal visible before the flow is considered complete

If you need hard enforcement, wire this skill into a posthook, task runner, or wrapper endpoint. A skill alone is the
contract; a hook is the enforcement layer.

For Codex and Claude, keep the hook contract identical and vary only the caller metadata.
See [references/posthook-contract.md](references/posthook-contract.md) for the minimal input/output contract.
For local installs and refreshes, use [`scripts/sync_skills.sh`](../../scripts/sync_skills.sh) to copy this skill into
the Codex and Claude skill directories and package the `.skill` archive.
For a mandatory posthook entrypoint, call [`scripts/postflow-hook.sh`](../../scripts/postflow-hook.sh) from your runner
and let it print the proposal to stdout.

## When to run

Run this skill at the end of any agentic flow, especially when:

- the task changed docs, specs, notes, or indexed knowledge
- a tool call may have updated external state
- the user asked for a summary, handoff, or next-step recommendation
- the session is about to return a final answer

If there is any doubt, run the proposal. Missing a proposal is worse than proposing a no-op.

## Operating rules

1. Read the latest MCP-accessible knowledge first.
2. Identify what changed during the flow.
3. Distinguish confirmed facts from inferred suggestions.
4. Do not invent updates when the source is unchanged.
5. Do not silently skip the proposal stage.
6. If the MCP source is unavailable, say so explicitly and propose the missing wiring or retry path.
7. If a real write-back endpoint exists, stop at the proposal unless the user or workflow explicitly authorizes
   mutation.

## Required output

Always produce a proposal with these sections:

### 1. Current state

- What the MCP source currently says
- Which docs, entries, or records are relevant
- Any notable changes since the last known state

### 2. Proposed update

- What should be changed, added, or confirmed
- Keep this as a proposal, not as if it already happened
- Use short, actionable language

### 3. Why this proposal exists

- Explain the concrete trigger from the current task
- Mention any uncertainty or missing data

### 4. Action status

- `proposed`
- `needs approval`
- `blocked: missing MCP access`
- `no change required`

## Output format

Use this exact structure unless the user explicitly asked for a different format:

```markdown
## MCP Proposal
### Current state
- ...

### Proposed update
- ...

### Why this proposal exists
- ...

### Action status
- proposed
```

If nothing changed, still emit the proposal and set status to `no change required`.

## Decision logic

If the flow updated knowledge:

- summarize the latest state
- propose the delta
- highlight the affected source or document set

If the flow did not update knowledge:

- say the state is unchanged
- still produce a proposal shell so the handoff is explicit

If MCP access is missing:

- report the blocker
- state what input or endpoint is needed
- propose the next operational step

## Integration guidance

Choose the strongest integration available:

1. Posthook or workflow runner
    - Best option for mandatory execution
    - Invoke this skill automatically before finalization

2. MCP endpoint
    - Best when a central service should compute the proposal
    - Good for shared state and auditability

3. Skill-only usage
    - Acceptable when hooks are unavailable
    - Require the agent to call this skill before every final response

## Posthook behavior

When this skill is used from a posthook:

- treat it as the final mandatory step before completion
- include the caller source (`codex` or `claude`) in the hook metadata
- pass the latest task context if available, but do not depend on it being complete
- emit the proposal even when the underlying MCP state is unchanged
- do not let the caller suppress the proposal unless the hook explicitly failed

The hook should be source-agnostic. Codex and Claude should both call the same contract so the proposal format stays
stable across agents.

## Quality bar

The proposal should be:

- short enough to read quickly
- specific enough to act on
- explicit about what is known versus inferred
- conservative when the source data is unclear

Avoid broad summaries that do not change any decision. The point is to surface a refreshed proposal that can be
approved, corrected, or ignored with intent.
