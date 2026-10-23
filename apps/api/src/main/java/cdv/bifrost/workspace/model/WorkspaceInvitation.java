package cdv.bifrost.workspace.model;

import cdv.bifrost.auth.model.Role;
import cdv.bifrost.auth.model.User;
import cdv.bifrost.common.model.BaseDomainModel;
import cdv.bifrost.workspace.model.enums.WorkspaceInvitationStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class WorkspaceInvitation extends BaseDomainModel {
    private Workspace workspace;
    private String email;
    private Role role;
    @Builder.Default
    private WorkspaceInvitationStatus status = WorkspaceInvitationStatus.PENDING;
    @Builder.Default
    private OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(7);
    private User invitedBy;
    private String tokenHash;
}
