package cdv.bifrost.workspace.security;

import cdv.bifrost.common.security.AuthUserPrincipal;
import cdv.bifrost.workspace.repository.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component("workspaceAuthorization")
@RequiredArgsConstructor
public class WorkspaceAuthorization {

    private final WorkspaceMemberRepository workspaceMemberRepository;

    @Transactional(readOnly = true)
    public boolean isAdmin(UUID workspaceId, Authentication authentication) {
        if (authentication == null
                || !(authentication.getPrincipal() instanceof AuthUserPrincipal principal)) {
            return false;
        }

        UUID userId = UUID.fromString(principal.userId());

        return workspaceMemberRepository
                .findByWorkspace_IdAndUser_Id(workspaceId, userId)
                .map(member -> "ADMIN".equals(member.getRole().getName()))
                .orElse(false);
    }
}

