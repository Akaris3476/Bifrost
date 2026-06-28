package cdv.bifrost.security;

public record AuthUserPrincipal(String userId, String email, String sessionHash) {
}
