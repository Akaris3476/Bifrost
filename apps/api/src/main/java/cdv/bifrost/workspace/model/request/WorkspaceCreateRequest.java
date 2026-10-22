package cdv.bifrost.workspace.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class WorkspaceCreateRequest {

    @NotBlank
    private String name;

    @NotEmpty
    private Map<String, String> members;

}
