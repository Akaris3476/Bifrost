package cdv.bifrost.workspace.model;

import cdv.bifrost.auth.model.Role;
import cdv.bifrost.auth.model.User;
import cdv.bifrost.common.model.BaseDomainModel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class WorkspaceMember extends BaseDomainModel {

    private Workspace workspace;
    private User user;
    private Role role;
}
