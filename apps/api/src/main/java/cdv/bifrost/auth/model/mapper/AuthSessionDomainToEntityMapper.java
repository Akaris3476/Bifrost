package cdv.bifrost.auth.model.mapper;

import cdv.bifrost.auth.model.AuthSession;
import cdv.bifrost.auth.model.entity.AuthSessionEntity;
import cdv.bifrost.common.model.mapper.BifrostMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BifrostMapperConfig.class)
public interface AuthSessionDomainToEntityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AuthSessionEntity map(AuthSession session);

}
