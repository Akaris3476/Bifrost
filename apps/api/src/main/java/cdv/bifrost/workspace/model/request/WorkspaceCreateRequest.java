package cdv.bifrost.workspace.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class WorkspaceCreateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private Map<String, String> members;

}
