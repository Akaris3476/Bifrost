package cdv.bifrost.workspace.model;

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
public class Workspace extends BaseDomainModel {
    private String id;
    private String name;
    private String slug;
}
