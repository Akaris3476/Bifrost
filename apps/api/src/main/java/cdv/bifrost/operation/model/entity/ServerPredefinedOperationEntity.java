package cdv.bifrost.operation.model.entity;

import cdv.bifrost.common.model.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "server_predefined_operations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerPredefinedOperationEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String key;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean requiresConfirmation;

    @Column(nullable = false)
    private Boolean isActive;
}
