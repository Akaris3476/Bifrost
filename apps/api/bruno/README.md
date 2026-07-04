# Bifrost API — Bruno Collection

API requests for the Bifrost backend, versioned as plain text in this repo.

## Run

### Desktop

Open `apps/api/bruno` in Bruno desktop. Select the `Local` environment from the top-right dropdown.

### CLI

```bash
cd apps/api/bruno
npm install
npm test                    # runs all requests in Local environment
npm run test:auth           # Auth folder only
npm run test:invitations    # Workspace-Invitations folder only
npm run test:health         # Health folder only
```

`npm install` pulls `@usebruno/cli` so the `bru` binary is available.

## Environments

Only `Local` is checked in.

| Var                   | Local default                          | Notes                                     |
|-----------------------|----------------------------------------|-------------------------------------------|
| `baseUrl`             | `http://localhost:8080`                | Spring Boot default port                  |
| `testUserEmail`       | `dev@bifrost.local`                    | Seed user                                 |
| `testUserPassword`    | `LocalDev123!`                         | Seed password (committed for local dev)   |
| `testUserDisplayName` | `Local Dev User`                       | Used in Register                          |
| `testWorkspaceId`     | `00000000-0000-0000-0000-000000000000` | **Replace with a real workspace UUID**    |
| `testInvitationEmail` | `invitee@bifrost.local`                | Must match the user that will call Accept |
| `testInvitationRole`  | `MEMBER`                               | `ADMIN` or `MEMBER`                       |

Staging/Production environments are intentionally not created. Add them in a follow-up when those environments exist.

## Endpoints

| Folder                | Request             | Method                                        | Auth           | CSRF     |
|-----------------------|---------------------|-----------------------------------------------|----------------|----------|
| Health                | `Get Health`        | GET `/actuator/health`                        | public         | no       |
| Auth                  | `Register`          | POST `/auth/register`                         | public         | no       |
| Auth                  | `Login`             | POST `/auth/login`                            | public         | no       |
| Auth                  | `Logout`            | POST `/auth/logout`                           | session cookie | no       |
| Auth                  | `Get Me`            | GET `/auth/me`                                | session cookie | no       |
| Workspace-Invitations | `List Invitations`  | GET `/workspaces/{id}/invitations`            | OWNER/ADMIN    | no (GET) |
| Workspace-Invitations | `Create Invitation` | POST `/workspaces/{id}/invitations`           | OWNER/ADMIN    | yes      |
| Workspace-Invitations | `Revoke Invitation` | DELETE `/workspaces/{id}/invitations/{invId}` | OWNER/ADMIN    | yes      |
| Workspace-Invitations | `Accept Invitation` | POST `/workspace-invitations/accept`          | session cookie | yes      |

## Auth flow (session cookie, not JWT)

`POST /auth/login` returns a `Set-Cookie: BIFROST_SESSION` header (HttpOnly, Secure, SameSite=Lax, 7d). Bruno stores the
cookie in the environment's cookie jar, so every subsequent request in the same run auto-includes it. The `Login`
post-response script also extracts `BIFROST_SESSION` and `XSRF-TOKEN` into collection vars as a convenience, but the
requests themselves read straight from the cookie jar.

**Always run `Login` before any protected request in the same collection run** — without the cookie,
`Identity.getSessionHash()` returns null and the server returns 401.

## CSRF

Spring Security is configured with `CookieCsrfTokenRepository.withHttpOnlyFalse()`. State-changing requests must echo
the `XSRF-TOKEN` cookie value in the `X-XSRF-TOKEN` header. `Login`, `Register`, and `Logout` are excluded from CSRF in
`SecurityConfig`; everything else is not.

Each protected request (Create, Revoke, Accept Invitation) has the same pre-request snippet that reads the `XSRF-TOKEN`
cookie from the jar and sets the header. If you ever see a 403 with a CSRF error, the cookie has expired or the request
ran in a different environment — re-run `Login`.

## Collection vars set at runtime

These are populated by post-response scripts and consumed by other requests — not declared in `Local.bru`.

| Var                | Set by                                             | Consumed by                                    |
|--------------------|----------------------------------------------------|------------------------------------------------|
| `authCookie`       | `Login`                                            | (informational; cookie jar is the real source) |
| `xsrfToken`        | `Login`                                            | (informational; cookie jar is the real source) |
| `currentUserId`    | `Get Me`                                           | informational                                  |
| `lastInvitationId` | `Create Invitation`                                | `Revoke Invitation` URL                        |
| `invitationToken`  | `Create Invitation` (parsed from `data.inviteUrl`) | `Accept Invitation` body                       |

## Prerequisite: seed a test workspace

There is no `WorkspaceController` in the API yet, and the Flyway migrations do not seed a workspace. Before running the
invitation requests, create a workspace in the DB and link `dev@bifrost.local` to it as `OWNER`. Run the app at least
once with `dev@bifrost.local` registered (via `Register.bru`) so the user exists.

```sql
-- 1. Find or create the user via Register.bru first
-- 2. Then seed a workspace and the membership:
INSERT INTO workspaces (name, package_id, created_by_user_id)
SELECT 'Local Dev Workspace', p.id, u.id
FROM packages p,
     users u
WHERE p.name = 'Free'
  AND u.email = 'dev@bifrost.local'
LIMIT 1;

INSERT INTO workspace_members (workspace_id, user_id, role_id)
SELECT w.id, u.id, r.id
FROM workspaces w,
     users u,
     roles r
WHERE w.name = 'Local Dev Workspace'
  AND u.email = 'dev@bifrost.local'
  AND r.name = 'OWNER';
```

Copy the new workspace's UUID into `Local.bru` as `testWorkspaceId`.

## Typical run order

1. `Get Health` — confirm the API is up.
2. `Register` — create the test user (run once; skip if already registered).
3. `Login` — establish the session and CSRF cookies.
4. `Get Me` — sanity check that the session works; sets `currentUserId`.
5. `List Invitations` — should return `{ data: [] }` on a fresh workspace.
6. `Create Invitation` — sets `lastInvitationId` and `invitationToken`.
7. `Accept Invitation` — only works if the logged-in user is the invitee (`invitee@bifrost.local`). To exercise this
   end-to-end, register a second user and call `Login` from a different environment, or use a second Bruno runner.
8. `Revoke Invitation` — tests that DELETE works against a PENDING invitation. After step 7 the invitation is no longer
   PENDING, so re-run `Create Invitation` first.

## Troubleshooting

- **403 with CSRF error on first protected call** — re-run `Login`, then retry. The first POST after a fresh session
  sometimes races the cookie set.
- **401 on `Get Me` after `Login`** — the session cookie is `Secure: true`, so over plain `http://localhost` Bruno may
  not persist it. Confirm the `baseUrl` is `http://` (not `https://`) and that the `BIFROST_SESSION` cookie is actually
  in the jar.
- **403 on `Create Invitation` with role error** — only `ADMIN` and `MEMBER` are invitable. `OWNER` cannot be invited.
- **404 on `Accept Invitation`** — the `invitationToken` is per-invitation and one-shot. Re-run `Create Invitation` to
  issue a new token.
