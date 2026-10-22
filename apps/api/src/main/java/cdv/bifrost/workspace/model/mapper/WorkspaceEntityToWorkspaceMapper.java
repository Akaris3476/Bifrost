package cdv.bifrost.workspace.model.mapper;

import cdv.bifrost.common.model.mapper.BifrostMapperConfig;
import cdv.bifrost.workspace.model.Workspace;
import cdv.bifrost.workspace.model.entity.WorkspaceEntity;
import org.mapstruct.Mapper;

@Mapper(config = BifrostMapperConfig.class)
public interface WorkspaceEntityToWorkspaceMapper {
    Workspace map(WorkspaceEntity workspace);
}

