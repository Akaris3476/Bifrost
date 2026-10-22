package cdv.bifrost.workspace.model.mapper;

import cdv.bifrost.common.model.mapper.BifrostMapperConfig;
import cdv.bifrost.workspace.model.WorkspaceMember;
import cdv.bifrost.workspace.model.entity.WorkspaceMemberEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BifrostMapperConfig.class)
public interface WorkspaceMemberToWorkspaceMemberEntityMapper {

    @Mapping(target = "id.workspaceId", source = "workspace.id")
    @Mapping(target = "id.userId", source = "user.id")
    WorkspaceMemberEntity map(WorkspaceMember entity);
}

