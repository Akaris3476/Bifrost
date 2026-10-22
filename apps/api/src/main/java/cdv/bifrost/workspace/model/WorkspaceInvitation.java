package cdv.bifrost.workspace.model;

import cdv.bifrost.auth.model.Role;
import cdv.bifrost.auth.model.User;
import cdv.bifrost.common.model.BaseDomainModel;
import cdv.bifrost.workspace.model.enums.WorkspaceInvitationStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class WorkspaceInvitation extends BaseDomainModel {
    private Workspace workspace;
    private String email;
    private Role role;
    private WorkspaceInvitationStatus status;
    private User invitedBy;
    private String tokenHash;
}
