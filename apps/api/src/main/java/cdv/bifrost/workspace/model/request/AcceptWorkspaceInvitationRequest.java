package cdv.bifrost.workspace.model.request;

import jakarta.validation.constraints.NotBlank;

public record AcceptWorkspaceInvitationRequest(
        @NotBlank
        String token
) {
}
