package cdv.bifrost.workspace.model.mapper;

import cdv.bifrost.common.model.mapper.BifrostMapperConfig;
import cdv.bifrost.workspace.model.WorkspaceInvitation;
import cdv.bifrost.workspace.model.entity.WorkspaceInvitationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BifrostMapperConfig.class)
public interface WorkspaceInvitationToWorkspaceInvitationEntityMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    WorkspaceInvitationEntity map(WorkspaceInvitation workspace);
}

