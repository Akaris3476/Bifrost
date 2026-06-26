package cdv.bifrost.common.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public abstract class BaseDomainModel {

    protected String createdUser;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

}
