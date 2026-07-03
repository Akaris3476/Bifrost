package cdv.bifrost.auth.model.mapper;

import cdv.bifrost.auth.model.User;
import cdv.bifrost.auth.model.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface UserEntityToDomainMapper {

    @Mapping(target = "name", source = "displayName")
    @Mapping(target = "createdUser", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User map(UserEntity userEntity);

    default LocalDateTime map(OffsetDateTime value) {
        return value == null ? null : value.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }
}
