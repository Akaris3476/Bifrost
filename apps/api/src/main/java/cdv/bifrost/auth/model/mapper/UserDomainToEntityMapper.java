package cdv.bifrost.auth.model.mapper;

import cdv.bifrost.auth.model.User;
import cdv.bifrost.auth.model.entity.UserEntity;
import cdv.bifrost.common.model.mapper.BifrostMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BifrostMapperConfig.class)
public interface UserDomainToEntityMapper {

    @Mapping(target = "displayName", source = "name")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserEntity map(User user);
}
