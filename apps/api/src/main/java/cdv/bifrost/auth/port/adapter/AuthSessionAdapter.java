package cdv.bifrost.auth.port.adapter;

import cdv.bifrost.auth.model.AuthSession;
import cdv.bifrost.auth.model.entity.AuthSessionEntity;
import cdv.bifrost.auth.model.mapper.AuthSessionDomainToEntityMapper;
import cdv.bifrost.auth.model.mapper.AuthSessionEntityToDomainMapper;
import cdv.bifrost.auth.port.SessionReadPort;
import cdv.bifrost.auth.port.SessionRevokePort;
import cdv.bifrost.auth.port.SessionSavePort;
import cdv.bifrost.auth.repository.AuthSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
class AuthSessionAdapter implements SessionReadPort, SessionSavePort, SessionRevokePort {

    private final AuthSessionRepository authSessionRepository;
    private final AuthSessionDomainToEntityMapper domainToEntityMapper;
    private final AuthSessionEntityToDomainMapper entityToDomainMapper;

    @Override
    public Optional<AuthSession> findByHash(String sessionHash) {
        return authSessionRepository.findBySessionHash(sessionHash)
                .map(entityToDomainMapper::map);
    }

    @Override
    public AuthSession save(AuthSession session) {
        AuthSessionEntity entity = domainToEntityMapper.map(session);
        AuthSessionEntity saved = authSessionRepository.save(entity);
        return entityToDomainMapper.map(saved);
    }

    @Override
    @Transactional
    public void touch(String sessionHash) {
        authSessionRepository.findBySessionHash(sessionHash)
                .ifPresent(entity -> {
                    entity.setLastSeenAt(OffsetDateTime.now());
                    authSessionRepository.save(entity);
                });
    }

    @Override
    @Transactional
    public void revokeByHash(String sessionHash) {
        authSessionRepository.findBySessionHash(sessionHash)
                .filter(entity -> entity.getRevokedAt() == null)
                .ifPresent(entity -> {
                    entity.setRevokedAt(OffsetDateTime.now());
                    authSessionRepository.save(entity);
                });
    }
}
