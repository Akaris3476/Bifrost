package cdv.bifrost.auth.model.entity;

import cdv.bifrost.auth.model.enums.UserStatus;
import cdv.bifrost.common.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "users")
@Getter
@Setter
@SuperBuilder
public class UserEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 100)
    private String displayName;

    @Column(length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status;
}
