package cdv.bifrost.auth.model;

import cdv.bifrost.common.model.BaseDomainModel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class AuthSession extends BaseDomainModel {
    private String id;
    private String userId;
    private String sessionHash;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime lastSeenAt;
    private LocalDateTime expiresAt;
    private LocalDateTime revokedAt;

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired(LocalDateTime now) {
        return expiresAt != null && !expiresAt.isAfter(now);
    }

    public boolean isActive(LocalDateTime now) {
        return !isRevoked() && !isExpired(now);
    }
}
