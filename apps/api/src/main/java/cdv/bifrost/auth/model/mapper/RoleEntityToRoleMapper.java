package cdv.bifrost.auth.model.mapper;

import cdv.bifrost.auth.model.Role;
import cdv.bifrost.authorization.model.entity.RoleEntity;
import cdv.bifrost.common.model.mapper.BifrostMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = BifrostMapperConfig.class)
public interface RoleEntityToRoleMapper {
    Role map(RoleEntity role);
}
