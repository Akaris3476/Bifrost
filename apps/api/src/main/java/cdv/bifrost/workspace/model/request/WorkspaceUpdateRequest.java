package cdv.bifrost.workspace.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkspaceUpdateRequest {

    @NotBlank
    private String name;

}
