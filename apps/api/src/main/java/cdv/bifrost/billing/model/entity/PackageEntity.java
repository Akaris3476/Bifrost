package cdv.bifrost.billing.model.entity;

import cdv.bifrost.billing.model.enums.BillingPeriod;
import cdv.bifrost.common.model.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "packages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer maxServers;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillingPeriod billingPeriod;

    @Column(nullable = false)
    private Boolean isPublic;

    @Column(nullable = false)
    private Boolean isActive;
}
