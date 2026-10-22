package cdv.bifrost.workspace.model.mapper;

import cdv.bifrost.common.model.mapper.BifrostMapperConfig;
import cdv.bifrost.workspace.model.WorkspaceMember;
import cdv.bifrost.workspace.model.entity.WorkspaceMemberEntity;
import org.mapstruct.Mapper;

@Mapper(config = BifrostMapperConfig.class)
public interface WorkspaceMemberEntityToWorkspaceMemberMapper {
    WorkspaceMember map(WorkspaceMemberEntity workspaceMembers);
}

