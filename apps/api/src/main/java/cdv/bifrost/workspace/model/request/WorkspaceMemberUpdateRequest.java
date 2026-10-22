package cdv.bifrost.workspace.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkspaceMemberUpdateRequest {
    @NotBlank
    private String role;
}
