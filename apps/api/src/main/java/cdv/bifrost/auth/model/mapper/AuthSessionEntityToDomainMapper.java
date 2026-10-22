package cdv.bifrost.auth.model.mapper;

import cdv.bifrost.auth.model.AuthSession;
import cdv.bifrost.auth.model.entity.AuthSessionEntity;
import cdv.bifrost.common.model.mapper.BifrostMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BifrostMapperConfig.class)
public interface AuthSessionEntityToDomainMapper {

    @Mapping(target = "createdUser", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AuthSession map(AuthSessionEntity entity);
}
