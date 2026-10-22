package cdv.bifrost.workspace.model.mapper;

import cdv.bifrost.common.model.mapper.BifrostMapperConfig;
import cdv.bifrost.workspace.model.WorkspaceInvitation;
import cdv.bifrost.workspace.model.entity.WorkspaceInvitationEntity;
import org.mapstruct.Mapper;

@Mapper(config = BifrostMapperConfig.class)
public interface WorkspaceInvitationEntityToWorkspaceInvitationMapper {
    WorkspaceInvitation map(WorkspaceInvitationEntity workspace);
}

