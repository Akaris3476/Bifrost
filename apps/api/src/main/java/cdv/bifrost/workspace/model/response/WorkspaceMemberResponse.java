package cdv.bifrost.workspace.model.response;

import cdv.bifrost.workspace.model.entity.WorkspaceMemberEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WorkspaceMemberResponse(
        UUID workspaceId,
        UUID userId,
        String email,
        String displayName,
        String roleName,
        OffsetDateTime createdAt
) {

    public static WorkspaceMemberResponse from(WorkspaceMemberEntity member) {
        return new WorkspaceMemberResponse(
                member.getWorkspace().getId(),
                member.getUser().getId(),
                member.getUser().getEmail(),
                member.getUser().getDisplayName(),
                member.getRole().getName(),
                member.getCreatedAt()
        );
    }
}
