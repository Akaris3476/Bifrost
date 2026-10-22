# Bifrost API — Bruno Collection

API requests for the Bifrost backend, versioned as plain text in this repo. The collection covers all current `/auth`,
`/workspaces`, and `/workspace-invitations` endpoints, plus health.

## Run

### Desktop

Open `apps/api/bruno` in Bruno desktop. Select the `Local` environment from the top-right dropdown.

### CLI

```bash
cd apps/api/bruno
npm install
npm test                    # runs all requests in Local environment
npm run test:auth           # Auth folder only
npm run test:workspaces     # Workspaces folder only
npm run test:invitations    # Workspace-Invitations folder only
npm run test:health         # Health folder only
```

`npm install` pulls `@usebruno/cli` so the `bru` binary is available.

## Environments

Only `Local` is checked in.

| Var                    | Local default                          | Notes                                     |
|------------------------|----------------------------------------|-------------------------------------------|
| `baseUrl`              | `http://localhost:8080`                | Spring Boot default port                  |
| `testUserEmail`        | `dev@bifrost.local`                    | Register once, then use for Login         |
| `testUserPassword`     | `LocalDev123!`                         | Seed password (committed for local dev)   |
| `testUserDisplayName`  | `Local Dev User`                       | Used in Register                          |
| `testWorkspaceName`    | `Local Dev Workspace`                  | Used by Create Workspace                  |
| `updatedWorkspaceName` | `Local Dev Workspace Updated`          | Used by Update Workspace                  |
| `testMemberEmail`      | `invitee@bifrost.local`                | Used by Add Workspace Member              |
| `testMemberRole`       | `MEMBER`                               | Role used by member management            |
| `testMemberId`         | zero UUID                              | Replace with an existing member user UUID |
| `testWorkspaceId`      | `00000000-0000-0000-0000-000000000000` | **Replace with a real workspace UUID**    |
| `testInvitationEmail`  | `invitee@bifrost.local`                | Must match the user that will call Accept |
| `testInvitationRole`   | `MEMBER`                               | `ADMIN` or `MEMBER`                       |

Staging/Production environments are intentionally not created. Add them in a follow-up when those environments exist.

## Endpoints

| Folder                | Request             | Method                                        | Auth           | CSRF     |
|-----------------------|---------------------|-----------------------------------------------|----------------|----------|
| Health                | `Get Health`        | GET `/actuator/health`                        | public         | no       |
| Auth                  | `Register`          | POST `/auth/register`                         | public         | no       |
| Auth                  | `Login`             | POST `/auth/login`                            | public         | no       |
| Auth                  | `Get Me`            | GET `/auth/me`                                | session cookie | no       |
| Auth                  | `Get CSRF Token`    | GET `/csrf`                                   | public         | no       |
| Auth                  | `Logout`            | POST `/auth/logout`                           | session cookie | no       |
| Workspaces            | `List Workspaces`   | GET `/workspaces`                             | session cookie | no       |
| Workspaces            | `Create Workspace`  | POST `/workspaces`                            | session cookie | yes      |
| Workspaces            | `Update Workspace`  | PATCH `/workspaces/{id}`                      | session cookie | yes      |
| Workspaces            | `Delete Workspace`  | DELETE `/workspaces/{id}`                     | session cookie | yes      |
| Workspaces            | `List Members`      | GET `/workspaces/{id}/users`                  | session cookie | no       |
| Workspaces            | `Add Member`        | POST `/workspaces/{id}/users`                 | session cookie | yes      |
| Workspaces            | `Update Member`     | PATCH `/workspaces/{id}/users/{userId}`       | session cookie | yes      |
| Workspaces            | `Remove Member`     | DELETE `/workspaces/{id}/users/{userId}`      | session cookie | yes      |
| Workspace-Invitations | `List Invitations`  | GET `/workspaces/{id}/invitations`            | OWNER/ADMIN    | no (GET) |
| Workspace-Invitations | `Create Invitation` | POST `/workspaces/{id}/invitations`           | OWNER/ADMIN    | yes      |
| Workspace-Invitations | `Revoke Invitation` | DELETE `/workspaces/{id}/invitations/{invId}` | OWNER/ADMIN    | yes      |
| Workspace-Invitations | `Accept Invitation` | POST `/workspace-invitations/accept`          | session cookie | yes      |

## Auth flow (session cookie, not JWT)

`POST /auth/login` returns a `Set-Cookie: BIFROST_SESSION` header (HttpOnly, Secure, SameSite=Lax, 7d). Bruno stores the
cookie in the environment's cookie jar, so every subsequent request in the same run auto-includes it.

**Always run `Login` before any protected request in the same collection run** — without the cookie,
`Identity.getSessionHash()` returns null and the server returns 401.

## CSRF

Spring Security is configured with `CookieCsrfTokenRepository.withHttpOnlyFalse()`. The token is deferred, so call
`GET /csrf` (`Auth / Get CSRF Token`) once to initialize it and receive the `XSRF-TOKEN` cookie. State-changing
requests must echo that cookie value in the `X-XSRF-TOKEN` header. `Login`, `Register`, and `Logout` are excluded from
CSRF in `SecurityConfig`; everything else is not.

Each protected mutation has the same pre-request snippet that reads the `XSRF-TOKEN`
cookie from the jar and sets the header. If you see a 403 with a CSRF error, run `Get CSRF Token` again and retry.

## Collection vars set at runtime

These are populated by post-response scripts and consumed by other requests — not declared in `Local.bru`.

| Var                | Set by                                             | Consumed by                                    |
|--------------------|----------------------------------------------------|------------------------------------------------|
| `authCookie`       | `Login`                                            | (informational; cookie jar is the real source) |
| `xsrfToken`        | `Get CSRF Token`                                   | (informational; cookie jar is the real source) |
| `currentUserId`    | `Get Me`                                           | informational                                  |
| `testWorkspaceId`  | `Create Workspace`                                 | Workspace and invitation request URLs          |
| `testMemberId`     | `Add Member`                                       | Update/Remove Member URL                       |
| `lastInvitationId` | `Create Invitation`                                | `Revoke Invitation` URL                        |
| `invitationToken`  | `Create Invitation` (parsed from `data.inviteUrl`) | `Accept Invitation` body                       |

## Workspace request binding

Workspace management request DTOs are currently bound from query/form parameters (the controller methods do not declare
`@RequestBody`). The Bruno requests use query parameters accordingly. `Create Workspace` uses Spring map binding with
`members[<email>]=<role>`; add at least one existing user entry. The current implementation creates invitations for
those
members. Set `testWorkspaceId` to a real workspace UUID and `testMemberId` to a real member user UUID before using the
related requests.

## Typical run order

1. `Get Health` — confirm the API is up.
2. `Register` — create the test user (run once; skip if already registered).
3. `Login` — establish the session.
4. `Get Me` — sanity check that the session works; sets `currentUserId`.
5. `Get CSRF Token` — initialize the CSRF cookie before protected mutations.
6. `List Workspaces` / `Create Workspace` — create a workspace and set its UUID as `testWorkspaceId`.
7. `List Members` — inspect members. Add/update/remove need existing users and a valid `testMemberId`.
8. `List Invitations` — inspect invitations for the selected workspace.
9. `Create Invitation` — sets `lastInvitationId` and `invitationToken`.
10. `Accept Invitation` — only works if the logged-in user is the invitee (`invitee@bifrost.local`). To exercise this
   end-to-end, register a second user and call `Login` from a different environment, or use a second Bruno runner.
11. `Revoke Invitation` — tests that DELETE works against a PENDING invitation. After step 10 the invitation is no
    longer
   PENDING, so re-run `Create Invitation` first.

## Troubleshooting

- **403 with CSRF error on a protected mutation** — run `Get CSRF Token`, then retry the request.
- **401 on `Get Me` after `Login`** — the session cookie is `Secure: true`, so over plain `http://localhost` Bruno may
  not persist it. Confirm the `baseUrl` is `http://` (not `https://`) and that the `BIFROST_SESSION` cookie is actually
  in the jar.
- **403 on `Create Invitation` with role error** — only `ADMIN` and `MEMBER` are invitable. `OWNER` cannot be invited.
- **404 on `Accept Invitation`** — the `invitationToken` is per-invitation and one-shot. Re-run `Create Invitation` to
  issue a new token.
