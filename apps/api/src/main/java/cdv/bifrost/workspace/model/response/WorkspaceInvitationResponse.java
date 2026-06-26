package cdv.bifrost.workspace.model.response;

import cdv.bifrost.workspace.model.entity.WorkspaceInvitationEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WorkspaceInvitationResponse(
        UUID id,
        UUID workspaceId,
        String email,
        String roleName,
        String status,
        UUID invitedByUserId,
        OffsetDateTime expiresAt,
        OffsetDateTime acceptedAt,
        OffsetDateTime createdAt,
        String inviteUrl
) {

    public static WorkspaceInvitationResponse from(WorkspaceInvitationEntity invitation, String inviteUrl) {
        UUID invitedByUserId = invitation.getInvitedByUser() == null ? null : invitation.getInvitedByUser().getId();

        return new WorkspaceInvitationResponse(
                invitation.getId(),
                invitation.getWorkspace().getId(),
                invitation.getEmail(),
                invitation.getRole().getName(),
                invitation.getStatus().name(),
                invitedByUserId,
                invitation.getExpiresAt(),
                invitation.getAcceptedAt(),
                invitation.getCreatedAt(),
                inviteUrl
        );
    }
}
