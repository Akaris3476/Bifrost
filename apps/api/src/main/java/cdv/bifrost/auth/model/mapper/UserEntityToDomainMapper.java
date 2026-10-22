package cdv.bifrost.auth.model.mapper;

import cdv.bifrost.auth.model.User;
import cdv.bifrost.auth.model.entity.UserEntity;
import cdv.bifrost.common.model.mapper.BifrostMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BifrostMapperConfig.class)
public interface UserEntityToDomainMapper {

    @Mapping(target = "name", source = "displayName")
    @Mapping(target = "createdUser", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User map(UserEntity userEntity);
}
