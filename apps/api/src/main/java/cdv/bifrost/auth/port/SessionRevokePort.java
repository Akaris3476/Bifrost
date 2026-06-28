package cdv.bifrost.auth.port;

public interface SessionRevokePort {
    void revokeByHash(String sessionHash);
}
