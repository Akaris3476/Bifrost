package cdv.bifrost.auth.service.impl;

import cdv.bifrost.auth.constant.SessionConstant;
import cdv.bifrost.auth.model.AuthSession;
import cdv.bifrost.auth.model.User;
import cdv.bifrost.auth.port.SessionReadPort;
import cdv.bifrost.auth.port.SessionRevokePort;
import cdv.bifrost.auth.port.SessionSavePort;
import cdv.bifrost.auth.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final SessionReadPort sessionReadPort;
    private final SessionSavePort sessionSavePort;
    private final SessionRevokePort sessionRevokePort;

    @Override
    @Transactional
    public String create(User user, String ipAddress, String userAgent) {
        String rawSessionId = generateRawSessionId();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        AuthSession session = AuthSession.builder()
                .id(UUID.randomUUID().toString())
                .userId(user.getId())
                .sessionHash(hash(rawSessionId))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .lastSeenAt(now)
                .expiresAt(now.plus(SessionConstant.SESSION_TTL))
                .build();

        sessionSavePort.save(session);
        return rawSessionId;
    }

    @Override
    public AuthSession findActiveByRawId(String rawSessionId) {
        return sessionReadPort.findByHash(hash(rawSessionId))
                .filter(s -> s.isActive(LocalDateTime.now(ZoneOffset.UTC)))
                .orElse(null);
    }

    @Override
    @Transactional
    public void touchByRawId(String rawSessionId) {
        sessionSavePort.touch(hash(rawSessionId));
    }

    @Override
    @Transactional
    public void revokeByRawId(String rawSessionId) {
        sessionRevokePort.revokeByHash(hash(rawSessionId));
    }

    @Override
    public String hash(String rawSessionId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawSessionId.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String generateRawSessionId() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
