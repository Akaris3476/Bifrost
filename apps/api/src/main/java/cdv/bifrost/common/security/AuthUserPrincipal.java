package cdv.bifrost.common.security;

public record AuthUserPrincipal(String userId, String email, String sessionHash) {
}
