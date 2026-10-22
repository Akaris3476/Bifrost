package cdv.bifrost.workspace.model.mapper;

import cdv.bifrost.common.model.mapper.BifrostMapperConfig;
import cdv.bifrost.workspace.model.Workspace;
import cdv.bifrost.workspace.model.entity.WorkspaceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BifrostMapperConfig.class)
public interface WorkspaceToWorkspaceEntityMapper {
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    WorkspaceEntity map(Workspace workspace);
}

