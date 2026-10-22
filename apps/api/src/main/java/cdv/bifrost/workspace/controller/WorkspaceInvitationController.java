package cdv.bifrost.workspace.controller;

import cdv.bifrost.common.model.response.SuccessResponse;
import cdv.bifrost.common.security.Identity;
import cdv.bifrost.workspace.model.WorkspaceInvitation;
import cdv.bifrost.workspace.model.request.AcceptWorkspaceInvitationRequest;
import cdv.bifrost.workspace.model.request.CreateWorkspaceInvitationRequest;
import cdv.bifrost.workspace.model.response.WorkspaceInvitationResponse;
import cdv.bifrost.workspace.model.response.WorkspaceMemberResponse;
import cdv.bifrost.workspace.service.WorkspaceInvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class WorkspaceInvitationController {

    private final WorkspaceInvitationService workspaceInvitationService;
    private final Identity identity;

    @GetMapping("/invitations")
    public SuccessResponse<List<WorkspaceInvitation>> getMyInvitations() {
        return SuccessResponse.of(workspaceInvitationService.listMyInvitations(identity));
    }

    @PostMapping("/workspaces/{workspaceId}/invitations")
    public SuccessResponse<WorkspaceInvitationResponse> createInvitation(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateWorkspaceInvitationRequest request
    ) {
        return SuccessResponse.of(workspaceInvitationService.createInvitation(workspaceId, request, identity));
    }

    @GetMapping("/workspaces/{workspaceId}/invitations")
    public SuccessResponse<List<WorkspaceInvitationResponse>> listInvitations(
            @PathVariable UUID workspaceId
    ) {
        return SuccessResponse.of(workspaceInvitationService.listInvitations(workspaceId, identity));
    }

    @DeleteMapping("/workspaces/{workspaceId}/invitations/{invitationId}")
    public SuccessResponse<Void> revokeInvitation(
            @PathVariable UUID workspaceId,
            @PathVariable UUID invitationId
    ) {
        workspaceInvitationService.revokeInvitation(workspaceId, invitationId, identity);
        return SuccessResponse.ok();
    }

    @PostMapping("/workspace-invitations/accept")
    public SuccessResponse<WorkspaceMemberResponse> acceptInvitation(
            @Valid @RequestBody AcceptWorkspaceInvitationRequest request
    ) {
        return SuccessResponse.of(workspaceInvitationService.acceptInvitation(request.token(), identity));
    }
}
