package cdv.bifrost.operation.model.entity;

import cdv.bifrost.auth.model.entity.UserEntity;
import cdv.bifrost.common.model.entity.BaseEntity;
import cdv.bifrost.operation.model.enums.OperationStatus;
import cdv.bifrost.server.model.entity.ServerEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "operation_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "server_id", nullable = false)
    private ServerEntity server;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(nullable = false, length = 100)
    private String operationKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OperationStatus status;

    @Column(columnDefinition = "TEXT")
    private String output;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false)
    private OffsetDateTime initiatedAt;

    private OffsetDateTime completedAt;
}
