package cdv.bifrost.server.model.entity;

import cdv.bifrost.common.model.entity.BaseEntity;
import cdv.bifrost.server.model.enums.ServerStatus;
import cdv.bifrost.workspace.model.entity.WorkspaceEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "servers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private WorkspaceEntity workspace;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 253)
    private String hostname;

    @Column(nullable = false)
    private Integer port;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ServerStatus status;

    @Column(nullable = false, length = 255)
    private String probeSecret;

    private OffsetDateTime lastSeenAt;
}
