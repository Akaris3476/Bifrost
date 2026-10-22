package cdv.bifrost.common.model.mapper;

import org.mapstruct.Builder;
import org.mapstruct.MapperConfig;

@MapperConfig(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        uses = DateTimeMapper.class
)
public interface BifrostMapperConfig {
}
