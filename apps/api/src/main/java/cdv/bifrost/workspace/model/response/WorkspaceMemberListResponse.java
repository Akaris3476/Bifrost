package cdv.bifrost.workspace.model.response;

import cdv.bifrost.workspace.model.Workspace;
import cdv.bifrost.workspace.model.WorkspaceMember;

import java.util.List;

public record WorkspaceMemberListResponse(
        List<WorkspaceMember> workspaceMemberList,
        Workspace workspace
) {
}
