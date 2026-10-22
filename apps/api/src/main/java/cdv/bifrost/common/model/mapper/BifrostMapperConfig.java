package cdv.bifrost.common.model.mapper;

import org.mapstruct.MapperConfig;

@MapperConfig(
        componentModel = "spring",
        uses = DateTimeMapper.class
)
public interface BifrostMapperConfig {
}
