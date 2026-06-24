package cdv.bifrost.monitoring.model.entity;

import cdv.bifrost.server.model.entity.ServerEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "server_health_snapshots")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerHealthSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "server_id", nullable = false)
    private ServerEntity server;

    @Column(precision = 5, scale = 2)
    private BigDecimal cpuUsagePct;

    private Long memUsedMb;
    private Long memTotalMb;

    @Column(precision = 12, scale = 3)
    private BigDecimal diskUsedGb;

    @Column(precision = 12, scale = 3)
    private BigDecimal diskTotalGb;

    private Long uptimeSeconds;

    @Column(precision = 8, scale = 4)
    private BigDecimal load1m;

    @Column(precision = 8, scale = 4)
    private BigDecimal load5m;

    @Column(precision = 8, scale = 4)
    private BigDecimal load15m;

    @Column(nullable = false)
    private OffsetDateTime capturedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
