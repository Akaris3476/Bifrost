package cdv.bifrost.auth.model;

import cdv.bifrost.common.model.BaseDomainModel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class User extends BaseDomainModel {
    private String id;
    private String name;
    private Role status;

    private List<Workspace> workspaceList;
}
