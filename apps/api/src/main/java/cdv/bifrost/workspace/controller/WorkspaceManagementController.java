package cdv.bifrost.workspace.controller;


import cdv.bifrost.common.model.response.SuccessResponse;
import cdv.bifrost.common.security.Identity;
import cdv.bifrost.workspace.model.Workspace;
import cdv.bifrost.workspace.model.WorkspaceMember;
import cdv.bifrost.workspace.model.request.WorkspaceCreateRequest;
import cdv.bifrost.workspace.model.request.WorkspaceMemberAddRequest;
import cdv.bifrost.workspace.model.request.WorkspaceMemberUpdateRequest;
import cdv.bifrost.workspace.model.request.WorkspaceUpdateRequest;
import cdv.bifrost.workspace.service.WorkspaceManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class WorkspaceManagementController {

    private final WorkspaceManagementService workspaceManagementService;
    private final Identity identity;

    @GetMapping("/workspaces")
    public SuccessResponse<List<Workspace>> getAllWorkspaces() {
        return SuccessResponse.of(workspaceManagementService.getAllWorkspaces(identity));
    }

    @PostMapping("/workspaces")
    public SuccessResponse<Workspace> createWorkspace(@Valid WorkspaceCreateRequest request) {
        return SuccessResponse.of(workspaceManagementService.createWorkspace(request, identity));
    }

    @PatchMapping("/workspaces/{workspaceId}")
    public SuccessResponse<Workspace> updateWorkspace(@PathVariable UUID workspaceId,
                                                      @Valid @RequestBody WorkspaceUpdateRequest request) {
        return SuccessResponse.of(workspaceManagementService.updateWorkspace(workspaceId, request));
    }

    @DeleteMapping("/workspaces/{workspaceId}")
    public SuccessResponse<Void> deleteWorkspace(@PathVariable UUID workspaceId) {
        workspaceManagementService.deleteWorkspace(workspaceId);
        return SuccessResponse.ok();
    }

    @GetMapping("/workspaces/{workspaceId}/users")
    public SuccessResponse<List<WorkspaceMember>> getAllWorkspaceMembers(@PathVariable UUID workspaceId) {
        return SuccessResponse.of(workspaceManagementService.getAllWorkspaceMembers(identity, workspaceId));
    }

    @PostMapping("/workspaces/{workspaceId}/users")
    public SuccessResponse<WorkspaceMember> addMemberToWorkspace(@PathVariable UUID workspaceId,
                                                                 @Valid @RequestBody WorkspaceMemberAddRequest request) {
        return SuccessResponse.of(workspaceManagementService.addWorkspaceMember(workspaceId, request));
    }

    @PatchMapping("/workspaces/{workspaceId}/users/{memberId}")
    public SuccessResponse<Void> updateWorkspaceMember(@PathVariable UUID workspaceId,
                                                       @PathVariable UUID memberId,
                                                       @Valid WorkspaceMemberUpdateRequest request
    ) {
        workspaceManagementService.updateWorkspaceMember(workspaceId, memberId, request);
        return SuccessResponse.ok();
    }

    @DeleteMapping("/workspaces/{workspaceId}/users/{memberId}")
    public SuccessResponse<Void> deleteWorkspaceMember(@PathVariable UUID workspaceId,
                                                       @PathVariable UUID memberId
    ) {
        workspaceManagementService.deleteWorkspaceMember(workspaceId, memberId);
        return SuccessResponse.ok();
    }
}
