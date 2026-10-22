package cdv.bifrost.auth.model;

import cdv.bifrost.auth.model.enums.UserStatus;
import cdv.bifrost.common.model.BaseDomainModel;
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
public class User extends BaseDomainModel {
    private String id;
    private String email;
    private String name;
    private String passwordHash;
    private UserStatus status;

//    private List<Workspace> workspaceList;
}
