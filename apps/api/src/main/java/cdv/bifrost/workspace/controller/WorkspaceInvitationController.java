package cdv.bifrost.workspace.controller;

import cdv.bifrost.auth.model.entity.UserEntity;
import cdv.bifrost.common.model.response.SuccessResponse;
import cdv.bifrost.security.CurrentUserResolver;
import cdv.bifrost.workspace.model.request.AcceptWorkspaceInvitationRequest;
import cdv.bifrost.workspace.model.request.CreateWorkspaceInvitationRequest;
import cdv.bifrost.workspace.model.response.WorkspaceInvitationResponse;
import cdv.bifrost.workspace.model.response.WorkspaceMemberResponse;
import cdv.bifrost.workspace.service.WorkspaceInvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class WorkspaceInvitationController {

    private final WorkspaceInvitationService workspaceInvitationService;
    private final CurrentUserResolver currentUserResolver;

    @PostMapping("/workspaces/{workspaceId}/invitations")
    public SuccessResponse<WorkspaceInvitationResponse> createInvitation(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateWorkspaceInvitationRequest request,
            Authentication authentication
    ) {
        UserEntity currentUser = currentUserResolver.resolve(authentication);
        return SuccessResponse.of(workspaceInvitationService.createInvitation(workspaceId, request, currentUser));
    }

    @GetMapping("/workspaces/{workspaceId}/invitations")
    public SuccessResponse<List<WorkspaceInvitationResponse>> listInvitations(
            @PathVariable UUID workspaceId,
            Authentication authentication
    ) {
        UserEntity currentUser = currentUserResolver.resolve(authentication);
        return SuccessResponse.of(workspaceInvitationService.listInvitations(workspaceId, currentUser));
    }

    @DeleteMapping("/workspaces/{workspaceId}/invitations/{invitationId}")
    public SuccessResponse<Void> revokeInvitation(
            @PathVariable UUID workspaceId,
            @PathVariable UUID invitationId,
            Authentication authentication
    ) {
        UserEntity currentUser = currentUserResolver.resolve(authentication);
        workspaceInvitationService.revokeInvitation(workspaceId, invitationId, currentUser);
        return SuccessResponse.ok();
    }

    @PostMapping("/workspace-invitations/accept")
    public SuccessResponse<WorkspaceMemberResponse> acceptInvitation(
            @Valid @RequestBody AcceptWorkspaceInvitationRequest request,
            Authentication authentication
    ) {
        UserEntity currentUser = currentUserResolver.resolve(authentication);
        return SuccessResponse.of(workspaceInvitationService.acceptInvitation(request.token(), currentUser));
    }
}
