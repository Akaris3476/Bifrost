package cdv.bifrost.auth.model.mapper;

import cdv.bifrost.auth.model.AuthSession;
import cdv.bifrost.auth.model.entity.AuthSessionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface AuthSessionEntityToDomainMapper {

    @Mapping(target = "createdUser", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AuthSession map(AuthSessionEntity entity);

    default LocalDateTime map(OffsetDateTime value) {
        return value == null ? null : value.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }
}
